package cn.jxufe.farm.service.imp;

import cn.jxufe.farm.bean.dto.IdDTO;
import cn.jxufe.farm.bean.dto.PageQueryDTO;
import cn.jxufe.farm.bean.dto.SetCurUserDTO;
import cn.jxufe.farm.bean.dto.UserAddOrUpdateDTO;
import cn.jxufe.farm.bean.dto.UserAvatarUpdateDTO;
import cn.jxufe.farm.bean.vo.CurUserVO;
import cn.jxufe.farm.bean.vo.UserAvatarVO;
import cn.jxufe.farm.bean.vo.UserInfoVO;
import cn.jxufe.farm.common.enums.BizErrorCode;
import cn.jxufe.farm.common.constants.SessionKeys;
import cn.jxufe.farm.common.exception.ServiceException;
import cn.jxufe.farm.common.pages.PageResult;
import cn.jxufe.farm.common.utils.FileAccessPathUtils;
import cn.jxufe.farm.common.utils.ServiceGuardUtils;
import cn.jxufe.farm.config.properties.GameplayPolicyProperties;
import cn.jxufe.farm.config.properties.LocalFileStorageProperties;
import cn.jxufe.farm.dao.SoilTypeDao;
import cn.jxufe.farm.dao.UserDao;
import cn.jxufe.farm.dao.UserPlotDao;
import cn.jxufe.farm.entity.SoilType;
import cn.jxufe.farm.entity.User;
import cn.jxufe.farm.entity.UserPlot;
import cn.jxufe.farm.service.FileService;
import cn.jxufe.farm.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class UserServiceImp implements UserService {

    private final UserDao userDao;

    private final UserPlotDao userPlotDao;

    private final SoilTypeDao soilTypeDao;

    private final FileService fileService;

    private final LocalFileStorageProperties fileStorageProperties;

    private final GameplayPolicyProperties gameplayPolicyProperties;

    public UserServiceImp(
            UserDao userDao,
            UserPlotDao userPlotDao,
            SoilTypeDao soilTypeDao,
            FileService fileService,
            LocalFileStorageProperties fileStorageProperties,
            GameplayPolicyProperties gameplayPolicyProperties
    ) {
        this.userDao = userDao;
        this.userPlotDao = userPlotDao;
        this.soilTypeDao = soilTypeDao;
        this.fileService = fileService;
        this.fileStorageProperties = fileStorageProperties;
        this.gameplayPolicyProperties = gameplayPolicyProperties;
    }

    @Override
    public PageResult<UserInfoVO> list() {
        List<UserInfoVO> rows = userDao.findByIsDeletedFalseOrderByIdAsc().stream()
                .map(this::buildUserInfoVO)
                .collect(Collectors.toList());
        return new PageResult<>(1L, rows.size(), rows.size(), rows);
    }

    @Override
    public PageResult<UserInfoVO> gridDataFilterSortPage(String name, PageQueryDTO pageRequest) {
        String keyword = safeString(name).trim();
        PageQueryDTO request = pageRequest == null ? new PageQueryDTO() : pageRequest;

        int pageNumber = Math.max(request.getPage() - 1, 0);
        int pageSize = request.getRows() <= 0 ? 5 : request.getRows();
        Sort.Direction direction = "desc".equalsIgnoreCase(request.getOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, safeSortField(request.getSort())));

        Page<User> userPage = userDao.findByIsDeletedFalseAndUsernameContainingIgnoreCase(keyword, pageable);

        List<UserInfoVO> rows = userPage.getContent().stream()
                .map(this::buildUserInfoVO)
                .collect(Collectors.toList());

        return new PageResult<>(request.getPage().longValue(), pageSize, userPage.getTotalElements(), rows);
    }

    @Override
    @Transactional
    public UserInfoVO addOrUpdate(UserAddOrUpdateDTO params) {
        ServiceGuardUtils.requireNotNull(params, BizErrorCode.PARAM_INVALID, "请求参数不能为空");
        Long id = parseLong(params.getId(), 0L);
        String username = safeString(params.getUsername()).trim();
        if (username.isBlank()) {
            throw new ServiceException(BizErrorCode.USERNAME_REQUIRED, "用户名不能为空");
        }

        String nickname = safeString(params.getNickname()).trim();
        if (nickname.isBlank()) {
            nickname = username;
        }

        userDao.findByUsernameAndIsDeletedFalse(username)
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> { throw new ServiceException(BizErrorCode.DUPLICATE_USERNAME, "用户名已存在"); });

        boolean isNewUser = (id == null || id <= 0);
        User entity = isNewUser ? createNewUser(username) : ServiceGuardUtils.requirePresent(
                userDao.findByIdAndIsDeletedFalse(id), BizErrorCode.USER_NOT_FOUND, "用户不存在"
        );

        entity.setUsername(username);
        entity.setNickname(nickname);
        entity.setExperience(Math.max(0L, parseLong(params.getExperience(), parseLong(params.getExp(), 0L))));
        entity.setScore(Math.max(0L, parseLong(params.getScore(), 0L)));
        entity.setCoin(Math.max(0L, parseLong(params.getCoin(), 0L)));

        String avatarPath = normalizeAvatarPath(params.getAvatarPath());
        entity.setAvatarUrl(avatarPath.isBlank() ? "images/headImages/none.png" : avatarPath);

        entity.setUpdatedAt(OffsetDateTime.now());
        entity.setUpdatedBy(0L);

        User saved = userDao.save(entity);
        if (isNewUser) {
            initDefaultPlotsForNewUser(saved.getId());
        }
        return buildUserInfoVO(saved);
    }

    @Override
    @Transactional
    public void delete(IdDTO params) {
        Long id = ServiceGuardUtils.requirePositive(params == null ? null : params.getId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
        User user = ServiceGuardUtils.requirePresent(userDao.findByIdAndIsDeletedFalse(id), BizErrorCode.USER_NOT_FOUND, "用户不存在");

        user.setIsDeleted(true);
        user.setUpdatedAt(OffsetDateTime.now());
        user.setUpdatedBy(0L);
        userDao.save(user);
    }

    @Override
    @Transactional
    public UserAvatarVO updateAvatar(UserAvatarUpdateDTO params) {
        Long id = ServiceGuardUtils.requirePositive(params == null ? null : params.getId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
        User user = ServiceGuardUtils.requirePresent(userDao.findByIdAndIsDeletedFalse(id), BizErrorCode.USER_NOT_FOUND, "用户不存在");

        String normalizedPath = normalizeAvatarPath(params.getAvatarPath());
        if (!normalizedPath.isBlank()) {
            user.setAvatarUrl(normalizedPath);
        }
        user.setUpdatedAt(OffsetDateTime.now());
        user.setUpdatedBy(0L);
        userDao.save(user);

        UserAvatarVO payload = new UserAvatarVO();
        payload.setId(user.getId());
        payload.setAvatarPath(safeString(user.getAvatarUrl()));
        payload.setHead(resolveAvatarAccessUrl(user.getAvatarUrl()));
        return payload;
    }

    @Override
    public List<UserInfoVO> loginUserOptions() {
        return userDao.findByIsDeletedFalseOrderByIdAsc().stream()
                .map(this::buildUserInfoVO)
                .collect(Collectors.toList());
    }

    @Override
    public CurUserVO setCurUser(HttpSession session, SetCurUserDTO user) {
        Long userId = ServiceGuardUtils.requirePositive(user == null ? null : user.getId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
        User curUser = ServiceGuardUtils.requirePresent(userDao.findByIdAndIsDeletedFalse(userId), BizErrorCode.USER_NOT_FOUND, "用户不存在");

        session.setAttribute(SessionKeys.CUR_USER, curUser);
        return buildCurUserData(curUser);
    }

    @Override
    public CurUserVO getCurUser(HttpSession session) {
        Object sessionUser = session.getAttribute(SessionKeys.CUR_USER);
        if (!(sessionUser instanceof User user)) {
            return buildGuestData();
        }

        return userDao.findByIdAndIsDeletedFalse(user.getId())
                .map(this::buildCurUserData)
                .orElseGet(() -> {
                    session.removeAttribute(SessionKeys.CUR_USER);
                    return buildGuestData();
                });
    }

    /* =========================================================
     *  Private Utility Helpers
     * ========================================================= */

    private User createNewUser(String username) {
        User entity = new User();
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setCreatedBy(0L);
        entity.setStatus((short) 1);
        entity.setIsDeleted(false);
        entity.setOptLockVersion(0);
        entity.setPasswordHash("123456");
        entity.setEmail(username + "_" + System.currentTimeMillis() + "@farm.local");
        return entity;
    }

    private String safeSortField(String sort) {
        return switch (safeString(sort).trim().toLowerCase()) {
            case "username" -> "username";
            case "nickname" -> "nickname";
            case "experience" -> "experience";
            case "score" -> "score";
            case "coin" -> "coin";
            default -> "id";
        };
    }

    private UserInfoVO buildUserInfoVO(User user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(safeString(user.getUsername()));
        vo.setNickname(safeString(user.getNickname()));
        vo.setExperience(user.getExperience() == null ? 0L : user.getExperience());
        vo.setScore(user.getScore() == null ? 0L : user.getScore());
        vo.setCoin(user.getCoin() == null ? 0L : user.getCoin());
        vo.setAvatarPath(safeString(user.getAvatarUrl()));
        vo.setHead(resolveAvatarAccessUrl(user.getAvatarUrl()));
        return vo;
    }

    private CurUserVO buildCurUserData(User user) {
        CurUserVO data = new CurUserVO();
        BeanUtils.copyProperties(buildUserInfoVO(user), data);
        data.setLoggedIn(true);
        return data;
    }

    private CurUserVO buildGuestData() {
        CurUserVO guest = new CurUserVO();
        guest.setId(0L);
        guest.setUsername("");
        guest.setNickname("游客");
        guest.setExperience(0L);
        guest.setScore(0L);
        guest.setCoin(0L);
        guest.setAvatarPath("");
        guest.setHead("/images/unknownUser.png");
        guest.setLoggedIn(false);
        return guest;
    }

    private String resolveAvatarAccessUrl(String avatarPath) {
        String value = safeString(avatarPath).trim();
        if (value.isBlank()) return "/images/unknownUser.png";

        String lower = value.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://") || value.startsWith("/images/")) {
            return value;
        }
        return fileService.buildAccessUrl(value);
    }

    private String normalizeAvatarPath(String avatarPath) {
        String input = safeString(avatarPath).trim();
        return input.isBlank() ? "" : FileAccessPathUtils.normalizeIncomingRelativePath(input, fileStorageProperties.getPublicPrefix());
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private Long parseLong(String value, Long defaultValue) {
        if (value == null || value.trim().isBlank()) return defaultValue;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private void initDefaultPlotsForNewUser(Long userId) {
        if (userId == null || userId <= 0) return;
        if (!userPlotDao.findByUserIdAndIsDeletedFalseOrderByPlotIndexAsc(userId).isEmpty()) return;

        List<SoilType> soils = soilTypeDao.findByIsDeletedFalseOrderByIdAsc();
        long defaultSoilTypeId = soils.isEmpty() ? 1L : soils.getFirst().getId();
        OffsetDateTime now = OffsetDateTime.now();

        short totalPlotCount = (short) Math.max(1, gameplayPolicyProperties.getPlot().getDefaults().getTotalPlotCount());
        short unlockedPlotCount = (short) Math.clamp(gameplayPolicyProperties.getPlot().getDefaults().getUnlockedPlotCount(), 1, totalPlotCount);

        List<UserPlot> initPlots = IntStream.rangeClosed(1, totalPlotCount)
                .mapToObj(index -> {
                    UserPlot plot = new UserPlot();
                    boolean locked = index > unlockedPlotCount;

                    plot.setUserId(userId);
                    plot.setSoilTypeId(defaultSoilTypeId);
                    plot.setPlotIndex((short) index);
                    plot.setIsLocked(locked);
                    plot.setLockReason(locked ? "待解锁" : null);
                    plot.setUnlockedAt(locked ? null : now);
                    plot.setStatus((short) 1);
                    plot.setIsDeleted(false);
                    plot.setOptLockVersion(0);
                    plot.setCreatedAt(now);
                    plot.setCreatedBy(userId);
                    plot.setUpdatedAt(now);
                    plot.setUpdatedBy(userId);
                    return plot;
                })
                .collect(Collectors.toList());

        userPlotDao.saveAll(initPlots);
    }
}
