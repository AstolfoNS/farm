package cn.jxufe.farm.imp;

import cn.jxufe.farm.config.LocalFileStorageProperties;
import cn.jxufe.farm.model.bean.EasyUIData;
import cn.jxufe.farm.model.bean.EasyUIDataPageRequest;
import cn.jxufe.farm.model.bean.Message;
import cn.jxufe.farm.model.dao.UserDao;
import cn.jxufe.farm.model.entity.User;
import cn.jxufe.farm.service.FileService;
import cn.jxufe.farm.service.UserService;
import cn.jxufe.farm.util.FilePathUtils;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserImp implements UserService {

    private final UserDao userDao;
    private final FileService fileService;
    private final LocalFileStorageProperties fileStorageProperties;

    public UserImp(UserDao userDao, FileService fileService, LocalFileStorageProperties fileStorageProperties) {
        this.userDao = userDao;
        this.fileService = fileService;
        this.fileStorageProperties = fileStorageProperties;
    }

    @Override
    public EasyUIData list() {
        EasyUIData data = new EasyUIData();
        List<User> users = userDao.findByIsDeletedFalseOrderByIdAsc();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (User user : users) {
            rows.add(buildGridRow(user));
        }
        data.setTotal(rows.size());
        data.setRows(rows);
        return data;
    }

    @Override
    public EasyUIData gridDataFilterSortPage(String name, EasyUIDataPageRequest pageRequest) {
        EasyUIData data = new EasyUIData();
        String keyword = safeString(name).trim();
        EasyUIDataPageRequest request = pageRequest == null ? new EasyUIDataPageRequest() : pageRequest;
        int pageNumber = Math.max(request.getPage() - 1, 0);
        int pageSize = request.getRows() <= 0 ? 5 : request.getRows();
        Sort.Direction direction = "desc".equalsIgnoreCase(request.getOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, safeSortField(request.getSort())));
        Page<User> userPage = userDao.findByIsDeletedFalseAndUsernameContainingIgnoreCase(keyword, pageable);

        List<Map<String, Object>> rows = new ArrayList<>();
        for (User user : userPage.getContent()) {
            rows.add(buildGridRow(user));
        }
        data.setTotal(userPage.getTotalElements());
        data.setRows(rows);
        return data;
    }

    @Override
    @Transactional
    public Message addOrUpdate(Map<String, String> params) {
        Message message = new Message();
        try {
            Long id = parseLong(params.get("id"), 0L);
            String username = safeString(params.get("username")).trim();
            if (username.isBlank()) {
                message.setCode(1);
                message.setMsg("用户名不能为空");
                return message;
            }
            String nickname = safeString(params.get("nickname")).trim();
            if (nickname.isBlank()) {
                nickname = username;
            }

            Optional<User> duplicated = userDao.findByUsernameAndIsDeletedFalse(username);
            if (duplicated.isPresent() && (id == null || !duplicated.get().getId().equals(id))) {
                message.setCode(1);
                message.setMsg("用户名已存在");
                return message;
            }

            User entity;
            if (id != null && id > 0) {
                Optional<User> optional = userDao.findByIdAndIsDeletedFalse(id);
                if (optional.isEmpty()) {
                    message.setCode(1);
                    message.setMsg("用户不存在");
                    return message;
                }
                entity = optional.get();
            } else {
                entity = new User();
                entity.setCreatedAt(OffsetDateTime.now());
                entity.setCreatedBy(0L);
                entity.setStatus((short) 1);
                entity.setIsDeleted(false);
                entity.setOptLockVersion(0);
                entity.setPasswordHash("123456");
                entity.setEmail(username + "_" + System.currentTimeMillis() + "@farm.local");
                entity.setAvatarUrl("images/headImages/none.png");
            }

            entity.setUsername(username);
            entity.setNickname(nickname);
            entity.setExperience(parseLong(params.get("experience"), parseLong(params.get("exp"), 0L)));
            entity.setScore(parseLong(params.get("score"), 0L));
            entity.setCoin(parseLong(params.get("coin"), 0L));
            String avatarPath = normalizeAvatarPath(params.get("avatarPath"));
            if (!avatarPath.isBlank()) {
                entity.setAvatarUrl(avatarPath);
            }

            if (entity.getExperience() == null || entity.getExperience() < 0) {
                entity.setExperience(0L);
            }
            if (entity.getScore() == null || entity.getScore() < 0) {
                entity.setScore(0L);
            }
            if (entity.getCoin() == null || entity.getCoin() < 0) {
                entity.setCoin(0L);
            }

            entity.setUpdatedAt(OffsetDateTime.now());
            entity.setUpdatedBy(0L);
            User saved = userDao.save(entity);

            message.setCode(0);
            message.setMsg("保存成功");
            message.setData(buildGridRow(saved));
            return message;
        } catch (Exception ex) {
            message.setCode(1);
            message.setMsg("保存失败: " + ex.getMessage());
            return message;
        }
    }

    @Override
    @Transactional
    public Message delete(Long id) {
        Message message = new Message();
        if (id == null || id <= 0) {
            message.setCode(1);
            message.setMsg("缺少用户ID");
            return message;
        }
        Optional<User> optional = userDao.findByIdAndIsDeletedFalse(id);
        if (optional.isEmpty()) {
            message.setCode(1);
            message.setMsg("用户不存在");
            return message;
        }

        User user = optional.get();
        user.setIsDeleted(true);
        user.setUpdatedAt(OffsetDateTime.now());
        user.setUpdatedBy(0L);
        userDao.save(user);

        message.setCode(0);
        message.setMsg("删除成功");
        return message;
    }

    @Override
    @Transactional
    public Message updateAvatar(Long id, String avatarPath) {
        Message message = new Message();
        if (id == null || id <= 0) {
            message.setCode(1);
            message.setMsg("缺少用户ID");
            return message;
        }
        Optional<User> optional = userDao.findByIdAndIsDeletedFalse(id);
        if (optional.isEmpty()) {
            message.setCode(1);
            message.setMsg("用户不存在");
            return message;
        }

        User user = optional.get();
        String normalizedPath = normalizeAvatarPath(avatarPath);
        if (!normalizedPath.isBlank()) {
            user.setAvatarUrl(normalizedPath);
        }
        user.setUpdatedAt(OffsetDateTime.now());
        user.setUpdatedBy(0L);
        userDao.save(user);

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", user.getId());
        payload.put("avatarPath", safeString(user.getAvatarUrl()));
        payload.put("head", resolveAvatarAccessUrl(user.getAvatarUrl()));
        message.setCode(0);
        message.setMsg("保存成功");
        message.setData(payload);
        return message;
    }

    private String safeSortField(String sort) {
        String value = safeString(sort).trim();
        if ("id".equalsIgnoreCase(value)) {
            return "id";
        }
        if ("username".equalsIgnoreCase(value)) {
            return "username";
        }
        if ("nickname".equalsIgnoreCase(value)) {
            return "nickname";
        }
        if ("experience".equalsIgnoreCase(value)) {
            return "experience";
        }
        if ("score".equalsIgnoreCase(value)) {
            return "score";
        }
        if ("coin".equalsIgnoreCase(value)) {
            return "coin";
        }
        return "id";
    }

    private Map<String, Object> buildGridRow(User user) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", user.getId());
        row.put("username", safeString(user.getUsername()));
        row.put("nickname", safeString(user.getNickname()));
        row.put("experience", user.getExperience() == null ? 0L : user.getExperience());
        row.put("score", user.getScore() == null ? 0L : user.getScore());
        row.put("coin", user.getCoin() == null ? 0L : user.getCoin());
        row.put("avatarPath", safeString(user.getAvatarUrl()));
        row.put("head", resolveAvatarAccessUrl(user.getAvatarUrl()));
        return row;
    }

    private String resolveAvatarAccessUrl(String avatarPath) {
        String value = safeString(avatarPath).trim();
        if (value.isBlank()) {
            return "/images/unknownUser.png";
        }
        String lower = value.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return value;
        }
        if (value.startsWith("/images/")) {
            return value;
        }
        return fileService.buildAccessUrl(value);
    }

    private String normalizeAvatarPath(String avatarPath) {
        String input = safeString(avatarPath).trim();
        if (input.isBlank()) {
            return "";
        }
        try {
            URI uri = URI.create(input);
            if (uri.getScheme() != null && uri.getPath() != null) {
                input = uri.getPath();
            }
        } catch (Exception ignored) {
        }
        String normalized = FilePathUtils.sanitizeToRelativePath(input);
        String publicPrefix = normalizePublicPrefix(fileStorageProperties.getPublicPrefix());
        String publicPrefixClean = FilePathUtils.sanitizeToRelativePath(publicPrefix);
        if (normalized.startsWith(publicPrefixClean + "/")) {
            return normalized.substring(publicPrefixClean.length() + 1);
        }
        return normalized;
    }

    private String normalizePublicPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "/oss";
        }
        String trimmed = prefix.trim();
        if (!trimmed.startsWith("/")) {
            trimmed = "/" + trimmed;
        }
        if (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private Long parseLong(String value, Long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String text = value.trim();
        if (text.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(text);
        } catch (Exception ex) {
            return defaultValue;
        }
    }
}
