package cn.jxufe.farm.service.imp;

import cn.jxufe.farm.bean.dto.IdDTO;
import cn.jxufe.farm.bean.dto.PageQueryDTO;
import cn.jxufe.farm.bean.dto.SetCurUserDTO;
import cn.jxufe.farm.bean.dto.UserAddOrUpdateDTO;
import cn.jxufe.farm.bean.dto.UserAvatarUpdateDTO;
import cn.jxufe.farm.bean.vo.CurUserVO;
import cn.jxufe.farm.bean.vo.UserAvatarVO;
import cn.jxufe.farm.bean.vo.UserInfoVO;
import cn.jxufe.farm.common.constants.BizErrorCode;
import cn.jxufe.farm.common.constants.SessionKeys;
import cn.jxufe.farm.common.exception.ServiceException;
import cn.jxufe.farm.common.pages.PageResult;
import cn.jxufe.farm.common.utils.FileAccessPathUtils;
import cn.jxufe.farm.common.utils.ServiceGuard;
import cn.jxufe.farm.config.properties.GameplayPolicyProperties;
import cn.jxufe.farm.config.properties.LocalFileStorageProperties;
import cn.jxufe.farm.dao.SoilTypeDao;
import cn.jxufe.farm.dao.UserDao;
import cn.jxufe.farm.dao.UserPlotDao;
import cn.jxufe.farm.entity.SoilType;
import cn.jxufe.farm.entity.User;
import cn.jxufe.farm.entity.UserPlot;
import cn.jxufe.farm.oss.FileService;
import cn.jxufe.farm.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImp implements UserService {

    private final UserDao userDao;
    private final UserPlotDao userPlotDao;
    private final SoilTypeDao soilTypeDao;
    private final FileService fileService;
    private final LocalFileStorageProperties fileStorageProperties;
    private final GameplayPolicyProperties gameplayPolicyProperties;

    public UserServiceImp(UserDao userDao,
                          UserPlotDao userPlotDao,
                          SoilTypeDao soilTypeDao,
                          FileService fileService,
                          LocalFileStorageProperties fileStorageProperties,
                          GameplayPolicyProperties gameplayPolicyProperties) {
        this.userDao = userDao;
        this.userPlotDao = userPlotDao;
        this.soilTypeDao = soilTypeDao;
        this.fileService = fileService;
        this.fileStorageProperties = fileStorageProperties;
        this.gameplayPolicyProperties = gameplayPolicyProperties;
    }

    @Override
    public PageResult<UserInfoVO> list() {
        List<User> users = userDao.findByIsDeletedFalseOrderByIdAsc();
        List<UserInfoVO> rows = new ArrayList<>();
        for (User user : users) {
            rows.add(buildGridRow(user));
        }
        return new PageResult<>(1L, (long) rows.size(), rows.size(), rows);
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

        List<UserInfoVO> rows = new ArrayList<>();
        for (User user : userPage.getContent()) {
            rows.add(buildGridRow(user));
        }
        return new PageResult<>(request.getPage().longValue(), (long) pageSize, userPage.getTotalElements(), rows);
    }

    @Override
    @Transactional
    public UserInfoVO addOrUpdate(UserAddOrUpdateDTO params) {
        ServiceGuard.requireNotNull(params, BizErrorCode.PARAM_INVALID, "请求参数不能为空");
        Long id = parseLong(params.getId(), 0L);
        String username = safeString(params.getUsername()).trim();
        if (username.isBlank()) {
            throw new ServiceException(BizErrorCode.USERNAME_REQUIRED, "用户名不能为空");
        }
        String nickname = safeString(params.getNickname()).trim();
        if (nickname.isBlank()) {
            nickname = username;
        }

        Optional<User> duplicated = userDao.findByUsernameAndIsDeletedFalse(username);
        if (duplicated.isPresent() && (id == null || !duplicated.get().getId().equals(id))) {
            throw new ServiceException(BizErrorCode.DUPLICATE_USERNAME, "用户名已存在");
        }

        User entity;
        boolean isNewUser = false;
        if (id != null && id > 0) {
            entity = ServiceGuard.requirePresent(
                    userDao.findByIdAndIsDeletedFalse(id),
                    BizErrorCode.USER_NOT_FOUND,
                    "用户不存在"
            );
        } else {
            entity = new User();
            isNewUser = true;
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
        entity.setExperience(parseLong(params.getExperience(), parseLong(params.getExp(), 0L)));
        entity.setScore(parseLong(params.getScore(), 0L));
        entity.setCoin(parseLong(params.getCoin(), 0L));
        String avatarPath = normalizeAvatarPath(params.getAvatarPath());
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
        if (entity.getAvatarUrl() == null || entity.getAvatarUrl().isBlank()) {
            entity.setAvatarUrl("images/headImages/none.png");
        }

        entity.setUpdatedAt(OffsetDateTime.now());
        entity.setUpdatedBy(0L);
        User saved = userDao.save(entity);
        if (isNewUser) {
            initDefaultPlotsForNewUser(saved.getId());
        }
        return buildGridRow(saved);
    }

    @Override
    @Transactional
    public void delete(IdDTO params) {
        Long id = ServiceGuard.requirePositive(
                params == null ? null : params.getId(),
                BizErrorCode.PARAM_INVALID,
                "用户ID无效"
        );
        User user = ServiceGuard.requirePresent(
                userDao.findByIdAndIsDeletedFalse(id),
                BizErrorCode.USER_NOT_FOUND,
                "用户不存在"
        );

        user.setIsDeleted(true);
        user.setUpdatedAt(OffsetDateTime.now());
        user.setUpdatedBy(0L);
        userDao.save(user);
    }

    @Override
    @Transactional
    public UserAvatarVO updateAvatar(UserAvatarUpdateDTO params) {
        Long id = ServiceGuard.requirePositive(
                params == null ? null : params.getId(),
                BizErrorCode.PARAM_INVALID,
                "用户ID无效"
        );
        User user = ServiceGuard.requirePresent(
                userDao.findByIdAndIsDeletedFalse(id),
                BizErrorCode.USER_NOT_FOUND,
                "用户不存在"
        );

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
        List<User> users = userDao.findByIsDeletedFalseOrderByIdAsc();
        List<UserInfoVO> result = new ArrayList<>();
        for (User user : users) {
            result.add(buildLoginOption(user));
        }
        return result;
    }

    @Override
    public CurUserVO setCurUser(HttpSession session, SetCurUserDTO user) {
        Long userId = ServiceGuard.requirePositive(
                user == null ? null : user.getId(),
                BizErrorCode.PARAM_INVALID,
                "用户ID无效"
        );
        User curUser = ServiceGuard.requirePresent(
                userDao.findByIdAndIsDeletedFalse(userId),
                BizErrorCode.USER_NOT_FOUND,
                "用户不存在"
        );
        session.setAttribute(SessionKeys.CUR_USER, curUser);
        return buildCurUserData(curUser);
    }

    @Override
    public CurUserVO getCurUser(HttpSession session) {
        Object sessionUser = session.getAttribute(SessionKeys.CUR_USER);
        if (!(sessionUser instanceof User)) {
            return buildGuestData();
        }
        User user = (User) sessionUser;
        Optional<User> optional = userDao.findByIdAndIsDeletedFalse(user.getId());
        if (optional.isEmpty()) {
            session.removeAttribute(SessionKeys.CUR_USER);
            return buildGuestData();
        }
        return buildCurUserData(optional.get());
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

    private UserInfoVO buildGridRow(User user) {
        UserInfoVO row = new UserInfoVO();
        row.setId(user.getId());
        row.setUsername(safeString(user.getUsername()));
        row.setNickname(safeString(user.getNickname()));
        row.setExperience(user.getExperience() == null ? 0L : user.getExperience());
        row.setScore(user.getScore() == null ? 0L : user.getScore());
        row.setCoin(user.getCoin() == null ? 0L : user.getCoin());
        row.setAvatarPath(safeString(user.getAvatarUrl()));
        row.setHead(resolveAvatarAccessUrl(user.getAvatarUrl()));
        return row;
    }

    private UserInfoVO buildLoginOption(User user) {
        UserInfoVO option = new UserInfoVO();
        option.setId(user.getId());
        option.setUsername(safeString(user.getUsername()));
        option.setNickname(safeString(user.getNickname()));
        option.setExperience(user.getExperience() == null ? 0L : user.getExperience());
        option.setScore(user.getScore() == null ? 0L : user.getScore());
        option.setCoin(user.getCoin() == null ? 0L : user.getCoin());
        option.setAvatarPath(safeString(user.getAvatarUrl()));
        option.setHead(resolveAvatarAccessUrl(user.getAvatarUrl()));
        return option;
    }

    private CurUserVO buildCurUserData(User user) {
        CurUserVO data = new CurUserVO();
        data.setId(user.getId());
        data.setUsername(safeString(user.getUsername()));
        data.setNickname(safeString(user.getNickname()));
        data.setExperience(user.getExperience() == null ? 0L : user.getExperience());
        data.setScore(user.getScore() == null ? 0L : user.getScore());
        data.setCoin(user.getCoin() == null ? 0L : user.getCoin());
        data.setAvatarPath(safeString(user.getAvatarUrl()));
        data.setHead(resolveAvatarAccessUrl(user.getAvatarUrl()));
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
        return FileAccessPathUtils.normalizeIncomingRelativePath(input, fileStorageProperties.getPublicPrefix());
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

    private void initDefaultPlotsForNewUser(Long userId) {
        if (userId == null || userId <= 0) {
            return;
        }
        List<UserPlot> exists = userPlotDao.findByUserIdAndIsDeletedFalseOrderByPlotIndexAsc(userId);
        if (!exists.isEmpty()) {
            return;
        }
        List<SoilType> soils = soilTypeDao.findByIsDeletedFalseOrderByIdAsc();
        long defaultSoilTypeId = soils.isEmpty() ? 1L : soils.get(0).getId();
        OffsetDateTime now = OffsetDateTime.now();

        List<UserPlot> initPlots = new ArrayList<>();
        short totalPlotCount = gameplayPolicyProperties.getPlot().getDefaults().getTotalPlotCount();
        short unlockedPlotCount = gameplayPolicyProperties.getPlot().getDefaults().getUnlockedPlotCount();
        short safeTotalPlotCount = totalPlotCount <= 0 ? 1 : totalPlotCount;
        short safeUnlockedPlotCount = unlockedPlotCount <= 0 ? 1 : unlockedPlotCount;
        if (safeUnlockedPlotCount > safeTotalPlotCount) {
            safeUnlockedPlotCount = safeTotalPlotCount;
        }

        for (short index = 1; index <= safeTotalPlotCount; index++) {
            UserPlot plot = new UserPlot();
            plot.setUserId(userId);
            plot.setSoilTypeId(defaultSoilTypeId);
            plot.setPlotIndex(index);
            boolean locked = index > safeUnlockedPlotCount;
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
            initPlots.add(plot);
        }
        userPlotDao.saveAll(initPlots);
    }
}
