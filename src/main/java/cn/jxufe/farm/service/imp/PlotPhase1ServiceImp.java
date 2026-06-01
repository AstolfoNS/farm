package cn.jxufe.farm.service.imp;

import com.fasterxml.jackson.databind.ObjectMapper;
import cn.jxufe.farm.bean.dto.IdDTO;
import cn.jxufe.farm.bean.dto.PlotPolicyActivateDTO;
import cn.jxufe.farm.bean.dto.PlotPolicySaveDTO;
import cn.jxufe.farm.bean.dto.PlotTypeQueryDTO;
import cn.jxufe.farm.bean.dto.PlotTypeSaveDTO;
import cn.jxufe.farm.bean.dto.SoilTypeQueryDTO;
import cn.jxufe.farm.bean.dto.SoilTypeSaveDTO;
import cn.jxufe.farm.bean.dto.UserPlotAllocationQueryDTO;
import cn.jxufe.farm.bean.dto.UserPlotAllocationUpdateDTO;
import cn.jxufe.farm.bean.vo.PlotPolicyVO;
import cn.jxufe.farm.bean.vo.PlotTypeGridVO;
import cn.jxufe.farm.bean.vo.SoilTypeGridVO;
import cn.jxufe.farm.bean.vo.UserPlotAllocationGridVO;
import cn.jxufe.farm.common.enums.BizErrorCode;
import cn.jxufe.farm.common.exception.ServiceException;
import cn.jxufe.farm.common.pages.PageResult;
import cn.jxufe.farm.dao.PlotPolicyApplyLogDao;
import cn.jxufe.farm.dao.PlotPolicyDao;
import cn.jxufe.farm.dao.PlotTypeDao;
import cn.jxufe.farm.dao.SoilTypeDao;
import cn.jxufe.farm.dao.UserDao;
import cn.jxufe.farm.dao.UserPlotAllocationDao;
import cn.jxufe.farm.dao.UserPlotDao;
import cn.jxufe.farm.entity.PlotPolicy;
import cn.jxufe.farm.entity.PlotPolicyApplyLog;
import cn.jxufe.farm.entity.PlotType;
import cn.jxufe.farm.entity.SoilType;
import cn.jxufe.farm.entity.User;
import cn.jxufe.farm.entity.UserPlotAllocation;
import cn.jxufe.farm.service.PlotPhase1Service;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PlotPhase1ServiceImp implements PlotPhase1Service {

    private static final String DEFAULT_SOIL_COVER = "/oss/defaults/soil/soil-default.png";
    private static final String DEFAULT_PLOT_COVER = "/oss/defaults/plot/plot-cover-default.png";
    private static final String DEFAULT_PLOT_ICON = "/oss/defaults/plot/plot-icon-default.png";

    private static final String SCOPE_NEW_USER_ONLY = "NEW_USER_ONLY";
    private static final String SCOPE_MANUAL_APPLY = "MANUAL_APPLY";
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_ARCHIVED = "ARCHIVED";

    private final SoilTypeDao soilTypeDao;
    private final PlotTypeDao plotTypeDao;
    private final PlotPolicyDao plotPolicyDao;
    private final UserPlotAllocationDao userPlotAllocationDao;
    private final UserPlotDao userPlotDao;
    private final UserDao userDao;
    private final PlotPolicyApplyLogDao plotPolicyApplyLogDao;
    private final ObjectMapper objectMapper;

    public PlotPhase1ServiceImp(
            SoilTypeDao soilTypeDao,
            PlotTypeDao plotTypeDao,
            PlotPolicyDao plotPolicyDao,
            UserPlotAllocationDao userPlotAllocationDao,
            UserPlotDao userPlotDao,
            UserDao userDao,
            PlotPolicyApplyLogDao plotPolicyApplyLogDao,
            ObjectMapper objectMapper
    ) {
        this.soilTypeDao = soilTypeDao;
        this.plotTypeDao = plotTypeDao;
        this.plotPolicyDao = plotPolicyDao;
        this.userPlotAllocationDao = userPlotAllocationDao;
        this.userPlotDao = userPlotDao;
        this.userDao = userDao;
        this.plotPolicyApplyLogDao = plotPolicyApplyLogDao;
        this.objectMapper = objectMapper;
    }

    @Override
    public PageResult<SoilTypeGridVO> pageSoilTypes(SoilTypeQueryDTO query) {
        SoilTypeQueryDTO request = query == null ? new SoilTypeQueryDTO() : query;
        int pageNo = Math.max(1, request.getPage() == null ? 1 : request.getPage());
        int pageSize = Math.max(1, request.getRows() == null ? 10 : Math.min(request.getRows(), 100));
        Sort.Direction direction = "desc".equalsIgnoreCase(request.getOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(direction, safeSoilSort(request.getSort())));

        Page<SoilType> page = soilTypeDao.findByIsDeletedFalseAndNameContainingIgnoreCase(safeString(request.getName()).trim(), pageable);
        List<SoilTypeGridVO> records = page.getContent().stream()
                .map(this::toSoilGridVO)
                .collect(Collectors.toList());
        return new PageResult<>(pageNo, pageSize, page.getTotalElements(), records);
    }

    @Override
    public SoilTypeGridVO getSoilType(IdDTO params) {
        Long id = requirePositiveId(params == null ? null : params.getId(), "soilTypeId");
        SoilType entity = soilTypeDao.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ServiceException(BizErrorCode.SOIL_TYPE_NOT_FOUND, "soil type not found"));
        return toSoilGridVO(entity);
    }

    @Override
    @Transactional
    public Long saveSoilType(SoilTypeSaveDTO params) {
        if (params == null) {
            throw new ServiceException(BizErrorCode.PARAM_INVALID, "request body is required");
        }
        String name = safeString(params.getName()).trim();
        if (name.isEmpty()) {
            throw new ServiceException(BizErrorCode.SOIL_TYPE_REQUIRED, "soil type name is required");
        }
        boolean isNew = params.getId() == null || params.getId() <= 0;

        soilTypeDao.findByNameAndIsDeletedFalse(name)
                .filter(item -> !Objects.equals(item.getId(), params.getId()))
                .ifPresent(item -> {
                    throw new ServiceException(BizErrorCode.PARAM_INVALID, "duplicate soil type name");
                });

        SoilType entity = !isNew
                ? soilTypeDao.findByIdAndIsDeletedFalse(params.getId()).orElseThrow(() -> new ServiceException(BizErrorCode.SOIL_TYPE_NOT_FOUND, "soil type not found"))
                : newSoilTypeEntity();
        int bitCode = isNew
                ? allocateNextSoilBitCode()
                : defaultInt(entity.getBitCode(), allocateNextSoilBitCode());

        entity.setName(name);
        entity.setBitCode(bitCode);
        entity.setCoverImageUrl(defaultString(params.getCoverImageUrl(), DEFAULT_SOIL_COVER));
        entity.setLevel((short) Math.max(1, params.getLevel() == null ? 1 : params.getLevel()));
        entity.setUnlockExperienceRequired(Math.max(0L, params.getUnlockExperienceRequired() == null ? 0L : params.getUnlockExperienceRequired()));
        entity.setGrowSpeedMultiplier(parseGrowMultiplier(params.getGrowSpeedMultiplier()));
        entity.setDescription(safeString(params.getDescription()));
        touchForUpdate(entity);
        return soilTypeDao.save(entity).getId();
    }

    private int allocateNextSoilBitCode() {
        Set<Integer> usedCodes = soilTypeDao.findByIsDeletedFalseOrderByIdAsc().stream()
                .map(SoilType::getBitCode)
                .filter(Objects::nonNull)
                .filter(code -> code > 0)
                .collect(Collectors.toSet());
        int candidate = 1;
        while (usedCodes.contains(candidate)) {
            if (candidate >= (1 << 30)) {
                throw new ServiceException(BizErrorCode.PARAM_INVALID, "soil type bitCode exhausted");
            }
            candidate = candidate << 1;
        }
        return candidate;
    }

    @Override
    @Transactional
    public void removeSoilType(IdDTO params) {
        Long id = requirePositiveId(params == null ? null : params.getId(), "soilTypeId");
        SoilType soilType = soilTypeDao.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ServiceException(BizErrorCode.SOIL_TYPE_NOT_FOUND, "soil type not found"));

        if (plotTypeDao.existsBySoilTypeIdAndIsDeletedFalse(id)) {
            throw new ServiceException(BizErrorCode.SOIL_TYPE_NOT_FOUND, "soil type is referenced by plot types");
        }
        if (userPlotDao.existsBySoilTypeIdAndIsDeletedFalse(id)) {
            throw new ServiceException(BizErrorCode.SOIL_TYPE_NOT_FOUND, "soil type is referenced by user plots");
        }

        soilType.setIsDeleted(true);
        touchForUpdate(soilType);
        soilTypeDao.save(soilType);
    }

    @Override
    public PageResult<PlotTypeGridVO> pagePlotTypes(PlotTypeQueryDTO query) {
        PlotTypeQueryDTO request = query == null ? new PlotTypeQueryDTO() : query;
        int pageNo = Math.max(1, request.getPage() == null ? 1 : request.getPage());
        int pageSize = Math.max(1, request.getRows() == null ? 10 : Math.min(request.getRows(), 100));
        Sort.Direction direction = "desc".equalsIgnoreCase(request.getOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(direction, safePlotTypeSort(request.getSort())));

        Page<PlotType> page = plotTypeDao.findByIsDeletedFalseAndNameContainingIgnoreCase(safeString(request.getName()).trim(), pageable);
        Map<Long, String> soilNameMap = soilTypeDao.findByIsDeletedFalseOrderByIdAsc().stream()
                .collect(Collectors.toMap(SoilType::getId, SoilType::getName, (a, b) -> a));
        List<PlotTypeGridVO> records = page.getContent().stream()
                .map(item -> toPlotTypeGridVO(item, soilNameMap))
                .collect(Collectors.toList());
        return new PageResult<>(pageNo, pageSize, page.getTotalElements(), records);
    }

    @Override
    public PlotTypeGridVO getPlotType(IdDTO params) {
        Long id = requirePositiveId(params == null ? null : params.getId(), "plotTypeId");
        PlotType entity = plotTypeDao.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ServiceException(BizErrorCode.PLOT_TYPE_NOT_FOUND, "plot type not found"));
        return toPlotTypeGridVO(entity, plotTypeNameMapBySoilId());
    }

    @Override
    @Transactional
    public Long savePlotType(PlotTypeSaveDTO params) {
        if (params == null) {
            throw new ServiceException(BizErrorCode.PARAM_INVALID, "request body is required");
        }
        String name = safeString(params.getName()).trim();
        if (name.isEmpty()) {
            throw new ServiceException(BizErrorCode.PLOT_TYPE_NAME_REQUIRED, "plot type name is required");
        }
        Long soilTypeId = requirePositiveId(params.getSoilTypeId(), "soilTypeId");
        soilTypeDao.findByIdAndIsDeletedFalse(soilTypeId)
                .orElseThrow(() -> new ServiceException(BizErrorCode.SOIL_TYPE_NOT_FOUND, "soil type not found"));

        plotTypeDao.findByNameAndIsDeletedFalse(name)
                .filter(item -> !Objects.equals(item.getId(), params.getId()))
                .ifPresent(item -> {
                    throw new ServiceException(BizErrorCode.PLOT_TYPE_NAME_DUPLICATE, "duplicate plot type name");
                });

        PlotType entity = (params.getId() != null && params.getId() > 0)
                ? plotTypeDao.findByIdAndIsDeletedFalse(params.getId()).orElseThrow(() -> new ServiceException(BizErrorCode.PLOT_TYPE_NOT_FOUND, "plot type not found"))
                : newPlotTypeEntity();

        String iconUrl = defaultString(params.getIconUrl(), DEFAULT_PLOT_ICON);
        String coverImageUrl = defaultString(params.getCoverImageUrl(), iconUrl.isEmpty() ? DEFAULT_PLOT_COVER : iconUrl);

        entity.setName(name);
        entity.setIconUrl(iconUrl);
        entity.setCoverImageUrl(coverImageUrl);
        entity.setSoilTypeId(soilTypeId);
        entity.setUnlockRequired(defaultBool(params.getUnlockRequired(), true));
        entity.setDefaultUsable(defaultBool(params.getDefaultUsable(), true));
        entity.setDefaultPlotUnlockExperienceConfig(Math.max(0L, params.getDefaultPlotUnlockExperienceConfig() == null ? 0L : params.getDefaultPlotUnlockExperienceConfig()));
        entity.setSortOrder(params.getSortOrder() == null ? 0 : params.getSortOrder());
        entity.setDescription(safeString(params.getDescription()));
        touchForUpdate(entity);
        return plotTypeDao.save(entity).getId();
    }

    @Override
    @Transactional
    public void removePlotType(IdDTO params) {
        Long id = requirePositiveId(params == null ? null : params.getId(), "plotTypeId");
        PlotType plotType = plotTypeDao.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ServiceException(BizErrorCode.PLOT_TYPE_NOT_FOUND, "plot type not found"));

        if (plotPolicyDao.existsByDefaultPlotTypeIdAndIsDeletedFalse(id)) {
            throw new ServiceException(BizErrorCode.PLOT_TYPE_IN_USE, "plot type is referenced by plot policies");
        }
        if (userPlotAllocationDao.existsByDefaultPlotTypeIdAndIsDeletedFalse(id)) {
            throw new ServiceException(BizErrorCode.PLOT_TYPE_IN_USE, "plot type is referenced by user allocations");
        }

        plotType.setIsDeleted(true);
        touchForUpdate(plotType);
        plotTypeDao.save(plotType);
    }

    @Override
    public PlotPolicyVO currentPolicy() {
        Optional<PlotPolicy> policyOpt = plotPolicyDao.findFirstByActiveTrueAndIsDeletedFalseOrderByIdAsc();
        if (policyOpt.isEmpty()) {
            List<PlotPolicy> all = plotPolicyDao.findByIsDeletedFalseOrderByIdAsc();
            if (all.isEmpty()) {
                PlotPolicyVO fallback = new PlotPolicyVO();
                fallback.setPolicyName("default-policy");
                fallback.setPolicyVersion("v1");
                fallback.setEffectiveScope(SCOPE_NEW_USER_ONLY);
                fallback.setPublishStatus(STATUS_DRAFT);
                fallback.setActive(false);
                fallback.setDefaultTotalPlotCount((short) 6);
                fallback.setDefaultUnlockedPlotCount((short) 1);
                fallback.setDefaultLockedPlotCount((short) 5);
                fallback.setDefaultLockRuleCode("DEFAULT_LOCKED");
                fallback.setDefaultLockReason("pending unlock");
                fallback.setAllocationRuleJson("{}");
                return fallback;
            }
            return toPolicyVO(all.get(0), plotTypeNameMap());
        }
        return toPolicyVO(policyOpt.get(), plotTypeNameMap());
    }

    @Override
    @Transactional
    public Long savePolicy(PlotPolicySaveDTO params) {
        if (params == null) {
            throw new ServiceException(BizErrorCode.PARAM_INVALID, "request body is required");
        }
        String policyName = safeString(params.getPolicyName()).trim();
        if (policyName.isEmpty()) {
            throw new ServiceException(BizErrorCode.PLOT_POLICY_INVALID, "policyName is required");
        }
        if (params.getDefaultPlotTypeId() != null && params.getDefaultPlotTypeId() > 0) {
            plotTypeDao.findByIdAndIsDeletedFalse(params.getDefaultPlotTypeId())
                    .orElseThrow(() -> new ServiceException(BizErrorCode.PLOT_TYPE_NOT_FOUND, "default plot type not found"));
        }

        short total = (short) Math.max(1, params.getDefaultTotalPlotCount() == null ? 6 : params.getDefaultTotalPlotCount());
        short unlocked = (short) Math.max(0, params.getDefaultUnlockedPlotCount() == null ? 1 : params.getDefaultUnlockedPlotCount());
        if (unlocked > total) {
            throw new ServiceException(BizErrorCode.PLOT_POLICY_INVALID, "unlocked count cannot be greater than total");
        }
        short locked = (short) (total - unlocked);
        if (params.getDefaultLockedPlotCount() != null && params.getDefaultLockedPlotCount() != locked) {
            throw new ServiceException(BizErrorCode.PLOT_POLICY_INVALID, "locked count must be total - unlocked");
        }

        PlotPolicy entity = (params.getId() != null && params.getId() > 0)
                ? plotPolicyDao.findByIdAndIsDeletedFalse(params.getId()).orElseThrow(() -> new ServiceException(BizErrorCode.PLOT_POLICY_INVALID, "policy not found"))
                : newPlotPolicyEntity();

        String scope = normalizeScope(params.getEffectiveScope());
        String status = normalizePublishStatus(params.getPublishStatus());
        boolean active = defaultBool(params.getActive(), false);
        if (active) {
            status = STATUS_ACTIVE;
            if (scope.isEmpty()) {
                scope = SCOPE_NEW_USER_ONLY;
            }
        }
        if (scope.isEmpty()) {
            scope = SCOPE_NEW_USER_ONLY;
        }

        entity.setPolicyName(policyName);
        entity.setPolicyVersion(defaultString(params.getPolicyVersion(), "v1"));
        entity.setActive(active);
        entity.setEffectiveScope(scope);
        entity.setPublishStatus(status);
        entity.setDefaultTotalPlotCount(total);
        entity.setDefaultUnlockedPlotCount(unlocked);
        entity.setDefaultLockedPlotCount(locked);
        entity.setDefaultPlotTypeId(params.getDefaultPlotTypeId());
        entity.setDefaultLockRuleCode(defaultString(params.getDefaultLockRuleCode(), "DEFAULT_LOCKED"));
        entity.setDefaultLockReason(defaultString(params.getDefaultLockReason(), "pending unlock"));
        entity.setAllocationRuleJson(normalizeJson(params.getAllocationRuleJson()));
        touchForUpdate(entity);
        PlotPolicy saved = plotPolicyDao.save(entity);

        if (Boolean.TRUE.equals(saved.getActive())) {
            deactivateOtherPolicies(saved.getId());
        }
        return saved.getId();
    }

    @Override
    @Transactional
    public Long activatePolicy(PlotPolicyActivateDTO params) {
        if (params == null) {
            throw new ServiceException(BizErrorCode.PARAM_INVALID, "request body is required");
        }
        Long id = requirePositiveId(params.getId(), "id");
        PlotPolicy target = plotPolicyDao.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ServiceException(BizErrorCode.PLOT_POLICY_INVALID, "policy not found"));

        target.setActive(true);
        target.setEffectiveScope(SCOPE_NEW_USER_ONLY);
        target.setPublishStatus(STATUS_ACTIVE);
        if (safeString(target.getPolicyVersion()).isEmpty()) {
            target.setPolicyVersion("v1");
        }
        touchForUpdate(target);
        plotPolicyDao.save(target);
        deactivateOtherPolicies(target.getId());

        PlotPolicyApplyLog log = newPlotPolicyApplyLogEntity();
        log.setPolicyId(target.getId());
        log.setAppliedScope(SCOPE_NEW_USER_ONLY);
        log.setTargetUserCount(0);
        log.setSuccessUserCount(0);
        log.setFailedUserCount(0);
        log.setRequestPayloadJson("{\"action\":\"activate\",\"scope\":\"NEW_USER_ONLY\"}");
        log.setResultSnapshotJson("{\"message\":\"activated for new users only\"}");
        log.setAppliedBy(0L);
        log.setAppliedAt(OffsetDateTime.now());
        touchForUpdate(log);
        plotPolicyApplyLogDao.save(log);
        return target.getId();
    }

    @Override
    public PageResult<UserPlotAllocationGridVO> pageUserAllocations(UserPlotAllocationQueryDTO query) {
        UserPlotAllocationQueryDTO request = query == null ? new UserPlotAllocationQueryDTO() : query;
        int pageNo = Math.max(1, request.getPage() == null ? 1 : request.getPage());
        int pageSize = Math.max(1, request.getRows() == null ? 10 : Math.min(request.getRows(), 100));

        List<User> users = userDao.findByIsDeletedFalseOrderByIdAsc();
        if (request.getUserId() != null && request.getUserId() > 0) {
            users = users.stream().filter(item -> Objects.equals(item.getId(), request.getUserId())).collect(Collectors.toList());
        }
        String keyword = safeString(request.getUsername()).trim().toLowerCase();
        if (!keyword.isEmpty()) {
            users = users.stream()
                    .filter(item -> safeString(item.getUsername()).toLowerCase().contains(keyword) || safeString(item.getNickname()).toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
        }

        Map<Long, UserPlotAllocation> allocationMap = userPlotAllocationDao.findByIsDeletedFalse(Pageable.unpaged())
                .stream()
                .collect(Collectors.toMap(UserPlotAllocation::getUserId, item -> item, (a, b) -> a));
        Map<Long, String> plotTypeNameMap = plotTypeNameMap();

        List<UserPlotAllocationGridVO> allRows = users.stream()
                .map(user -> toUserAllocationGridVO(user, allocationMap.get(user.getId()), plotTypeNameMap))
                .sorted(resolveAllocationComparator(request.getSort(), request.getOrder()))
                .collect(Collectors.toList());
        return PageResult.of(allRows, pageNo, pageSize);
    }

    @Override
    @Transactional
    public Long updateUserAllocation(UserPlotAllocationUpdateDTO params) {
        if (params == null) {
            throw new ServiceException(BizErrorCode.PARAM_INVALID, "request body is required");
        }
        Long userId = requirePositiveId(params.getUserId(), "userId");
        userDao.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ServiceException(BizErrorCode.USER_NOT_FOUND, "user not found"));

        if (params.getDefaultPlotTypeId() != null && params.getDefaultPlotTypeId() > 0) {
            plotTypeDao.findByIdAndIsDeletedFalse(params.getDefaultPlotTypeId())
                    .orElseThrow(() -> new ServiceException(BizErrorCode.PLOT_TYPE_NOT_FOUND, "default plot type not found"));
        }

        int currentTotal = (int) userPlotDao.countByUserIdAndIsDeletedFalse(userId);
        int currentUnlocked = (int) userPlotDao.countByUserIdAndIsLockedFalseAndIsDeletedFalse(userId);

        UserPlotAllocation entity = resolveUserAllocationEntity(params, userId);
        int nextTotal = Math.max(1, params.getTotalPlotCount() == null ? defaultInt(entity.getTotalPlotCount(), Math.max(currentTotal, 1)) : params.getTotalPlotCount());
        int nextUnlocked = Math.max(0, params.getUnlockedPlotCount() == null ? defaultInt(entity.getUnlockedPlotCount(), currentUnlocked) : params.getUnlockedPlotCount());
        if (nextUnlocked > nextTotal) {
            throw new ServiceException(BizErrorCode.PLOT_ALLOCATION_INVALID, "unlocked count cannot be greater than total");
        }
        if (nextUnlocked < currentUnlocked) {
            throw new ServiceException(BizErrorCode.PLOT_ALLOCATION_INVALID, "cannot rollback already unlocked user plots");
        }
        if (nextTotal < currentTotal) {
            throw new ServiceException(BizErrorCode.PLOT_ALLOCATION_INVALID, "cannot set total plots less than current user plots");
        }

        entity.setUserId(userId);
        entity.setActive(defaultBool(params.getActive(), true));
        entity.setTotalPlotCount((short) nextTotal);
        entity.setUnlockedPlotCount((short) nextUnlocked);
        entity.setLockedPlotCount((short) (nextTotal - nextUnlocked));
        entity.setDefaultPlotTypeId(params.getDefaultPlotTypeId());
        entity.setLockRuleCode(defaultString(params.getLockRuleCode(), "DEFAULT_LOCKED"));
        entity.setLockReason(defaultString(params.getLockReason(), "pending unlock"));
        entity.setAllocationRuleJson(normalizeJson(params.getAllocationRuleJson()));
        if (entity.getAppliedAt() == null) {
            entity.setAppliedAt(OffsetDateTime.now());
        }
        touchForUpdate(entity);
        return userPlotAllocationDao.save(entity).getId();
    }

    private UserPlotAllocation resolveUserAllocationEntity(UserPlotAllocationUpdateDTO params, Long userId) {
        if (params.getId() != null && params.getId() > 0) {
            return userPlotAllocationDao.findByIdAndIsDeletedFalse(params.getId())
                    .orElseThrow(() -> new ServiceException(BizErrorCode.PLOT_ALLOCATION_NOT_FOUND, "user plot allocation not found"));
        }
        return userPlotAllocationDao.findByUserIdAndIsDeletedFalse(userId).orElseGet(this::newUserPlotAllocationEntity);
    }

    private String safeSoilSort(String sort) {
        String field = safeString(sort).trim().toLowerCase();
        return switch (field) {
            case "name" -> "name";
            case "bitcode", "bit_code" -> "bitCode";
            case "level" -> "level";
            default -> "id";
        };
    }

    private String safePlotTypeSort(String sort) {
        String field = safeString(sort).trim().toLowerCase();
        return switch (field) {
            case "name" -> "name";
            case "sortorder", "sort_order" -> "sortOrder";
            case "soiltypeid", "soil_type_id" -> "soilTypeId";
            default -> "id";
        };
    }

    private Comparator<UserPlotAllocationGridVO> resolveAllocationComparator(String sort, String order) {
        String field = safeString(sort).trim().toLowerCase();
        Comparator<UserPlotAllocationGridVO> comparator = switch (field) {
            case "username" -> Comparator.comparing(item -> safeString(item.getUsername()));
            case "totalplotcount", "total_plot_count" -> Comparator.comparing(item -> defaultInt(item.getTotalPlotCount(), 0));
            case "unlockedplotcount", "unlocked_plot_count" -> Comparator.comparing(item -> defaultInt(item.getUnlockedPlotCount(), 0));
            case "currentunlockedplots", "current_unlocked_plots" -> Comparator.comparing(item -> defaultInt(item.getCurrentUnlockedPlots(), 0));
            default -> Comparator.comparing(item -> item.getUserId() == null ? 0L : item.getUserId());
        };
        return "desc".equalsIgnoreCase(order) ? comparator.reversed() : comparator;
    }

    private SoilTypeGridVO toSoilGridVO(SoilType item) {
        SoilTypeGridVO vo = new SoilTypeGridVO();
        vo.setId(item.getId());
        vo.setName(safeString(item.getName()));
        vo.setBitCode(item.getBitCode());
        vo.setCoverImageUrl(defaultString(item.getCoverImageUrl(), DEFAULT_SOIL_COVER));
        vo.setLevel(item.getLevel());
        vo.setUnlockExperienceRequired(item.getUnlockExperienceRequired());
        vo.setGrowSpeedMultiplier(item.getGrowSpeedMultiplier() == null ? "1.00" : item.getGrowSpeedMultiplier().toPlainString());
        vo.setDescription(safeString(item.getDescription()));
        return vo;
    }

    private PlotTypeGridVO toPlotTypeGridVO(PlotType item, Map<Long, String> soilNameMap) {
        PlotTypeGridVO vo = new PlotTypeGridVO();
        vo.setId(item.getId());
        vo.setName(safeString(item.getName()));
        vo.setIconUrl(defaultString(item.getIconUrl(), DEFAULT_PLOT_ICON));
        vo.setCoverImageUrl(defaultString(item.getCoverImageUrl(), defaultString(item.getIconUrl(), DEFAULT_PLOT_COVER)));
        vo.setSoilTypeId(item.getSoilTypeId());
        vo.setSoilTypeName(safeString(soilNameMap.get(item.getSoilTypeId())));
        vo.setUnlockRequired(defaultBool(item.getUnlockRequired(), true));
        vo.setDefaultUsable(defaultBool(item.getDefaultUsable(), true));
        vo.setDefaultPlotUnlockExperienceConfig(item.getDefaultPlotUnlockExperienceConfig() == null ? 0L : item.getDefaultPlotUnlockExperienceConfig());
        vo.setSortOrder(item.getSortOrder() == null ? 0 : item.getSortOrder());
        vo.setDescription(safeString(item.getDescription()));
        return vo;
    }

    private UserPlotAllocationGridVO toUserAllocationGridVO(User user, UserPlotAllocation allocation, Map<Long, String> plotTypeNameMap) {
        UserPlotAllocationGridVO vo = new UserPlotAllocationGridVO();
        vo.setUserId(user.getId());
        vo.setUsername(safeString(user.getUsername()));
        vo.setNickname(safeString(user.getNickname()));
        vo.setCurrentTotalPlots((int) userPlotDao.countByUserIdAndIsDeletedFalse(user.getId()));
        vo.setCurrentUnlockedPlots((int) userPlotDao.countByUserIdAndIsLockedFalseAndIsDeletedFalse(user.getId()));

        if (allocation != null) {
            vo.setId(allocation.getId());
            vo.setActive(defaultBool(allocation.getActive(), true));
            vo.setTotalPlotCount(allocation.getTotalPlotCount());
            vo.setUnlockedPlotCount(allocation.getUnlockedPlotCount());
            vo.setLockedPlotCount(allocation.getLockedPlotCount());
            vo.setDefaultPlotTypeId(allocation.getDefaultPlotTypeId());
            vo.setDefaultPlotTypeName(safeString(plotTypeNameMap.get(allocation.getDefaultPlotTypeId())));
            vo.setLockRuleCode(safeString(allocation.getLockRuleCode()));
            vo.setLockReason(safeString(allocation.getLockReason()));
            vo.setAllocationRuleJson(defaultString(allocation.getAllocationRuleJson(), "{}"));
            vo.setAppliedAt(allocation.getAppliedAt());
            return vo;
        }

        vo.setActive(false);
        vo.setTotalPlotCount((short) vo.getCurrentTotalPlots().intValue());
        vo.setUnlockedPlotCount((short) vo.getCurrentUnlockedPlots().intValue());
        vo.setLockedPlotCount((short) Math.max(vo.getCurrentTotalPlots() - vo.getCurrentUnlockedPlots(), 0));
        vo.setAllocationRuleJson("{}");
        return vo;
    }

    private PlotPolicyVO toPolicyVO(PlotPolicy policy, Map<Long, String> plotTypeNameMap) {
        PlotPolicyVO vo = new PlotPolicyVO();
        vo.setId(policy.getId());
        vo.setPolicyName(safeString(policy.getPolicyName()));
        vo.setPolicyVersion(defaultString(policy.getPolicyVersion(), "v1"));
        vo.setActive(defaultBool(policy.getActive(), false));
        vo.setEffectiveScope(defaultString(policy.getEffectiveScope(), SCOPE_NEW_USER_ONLY));
        vo.setPublishStatus(defaultString(policy.getPublishStatus(), STATUS_DRAFT));
        vo.setDefaultTotalPlotCount(policy.getDefaultTotalPlotCount());
        vo.setDefaultUnlockedPlotCount(policy.getDefaultUnlockedPlotCount());
        vo.setDefaultLockedPlotCount(policy.getDefaultLockedPlotCount());
        vo.setDefaultPlotTypeId(policy.getDefaultPlotTypeId());
        vo.setDefaultPlotTypeName(safeString(plotTypeNameMap.get(policy.getDefaultPlotTypeId())));
        vo.setDefaultLockRuleCode(safeString(policy.getDefaultLockRuleCode()));
        vo.setDefaultLockReason(safeString(policy.getDefaultLockReason()));
        vo.setAllocationRuleJson(defaultString(policy.getAllocationRuleJson(), "{}"));
        return vo;
    }

    private Map<Long, String> plotTypeNameMap() {
        return plotTypeDao.findByIsDeletedFalseOrderBySortOrderAscIdAsc().stream()
                .collect(Collectors.toMap(PlotType::getId, item -> safeString(item.getName()), (a, b) -> a, LinkedHashMap::new));
    }

    private Map<Long, String> plotTypeNameMapBySoilId() {
        return soilTypeDao.findByIsDeletedFalseOrderByIdAsc().stream()
                .collect(Collectors.toMap(SoilType::getId, item -> safeString(item.getName()), (a, b) -> a, LinkedHashMap::new));
    }

    private void deactivateOtherPolicies(Long activePolicyId) {
        List<PlotPolicy> policies = plotPolicyDao.findByIsDeletedFalseOrderByIdAsc();
        List<PlotPolicy> toUpdate = new ArrayList<>();
        for (PlotPolicy item : policies) {
            if (Objects.equals(item.getId(), activePolicyId)) {
                continue;
            }
            if (Boolean.TRUE.equals(item.getActive())) {
                item.setActive(false);
                if (STATUS_ACTIVE.equalsIgnoreCase(safeString(item.getPublishStatus()))) {
                    item.setPublishStatus(STATUS_ARCHIVED);
                }
                touchForUpdate(item);
                toUpdate.add(item);
            }
        }
        if (!toUpdate.isEmpty()) {
            plotPolicyDao.saveAll(toUpdate);
        }
    }

    private String normalizeScope(String scope) {
        String value = safeString(scope).trim().toUpperCase();
        if (value.equals(SCOPE_NEW_USER_ONLY) || value.equals(SCOPE_MANUAL_APPLY)) {
            return value;
        }
        return "";
    }

    private String normalizePublishStatus(String status) {
        String value = safeString(status).trim().toUpperCase();
        Set<String> allowed = Set.of(STATUS_DRAFT, STATUS_ACTIVE, STATUS_ARCHIVED);
        if (allowed.contains(value)) {
            return value;
        }
        return STATUS_DRAFT;
    }

    private String normalizeJson(String json) {
        String raw = safeString(json).trim();
        if (raw.isEmpty()) {
            return "{}";
        }
        try {
            objectMapper.readTree(raw);
            return raw;
        } catch (Exception ex) {
            throw new ServiceException(BizErrorCode.PARAM_INVALID, "invalid json payload");
        }
    }

    private BigDecimal parseGrowMultiplier(String text) {
        String raw = safeString(text).trim();
        if (raw.isEmpty()) {
            return BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
        }
        try {
            BigDecimal value = new BigDecimal(raw);
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                return BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
            }
            return value.setScale(2, RoundingMode.HALF_UP);
        } catch (Exception ex) {
            return BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
        }
    }

    private SoilType newSoilTypeEntity() {
        SoilType entity = new SoilType();
        initBaseEntity(entity);
        entity.setCoverImageUrl(DEFAULT_SOIL_COVER);
        entity.setGrowSpeedMultiplier(BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP));
        entity.setLevel((short) 1);
        entity.setUnlockExperienceRequired(0L);
        return entity;
    }

    private PlotType newPlotTypeEntity() {
        PlotType entity = new PlotType();
        initBaseEntity(entity);
        entity.setIconUrl(DEFAULT_PLOT_ICON);
        entity.setCoverImageUrl(DEFAULT_PLOT_COVER);
        entity.setUnlockRequired(true);
        entity.setDefaultUsable(true);
        entity.setDefaultPlotUnlockExperienceConfig(0L);
        entity.setSortOrder(0);
        return entity;
    }

    private PlotPolicy newPlotPolicyEntity() {
        PlotPolicy entity = new PlotPolicy();
        initBaseEntity(entity);
        entity.setActive(false);
        entity.setPolicyVersion("v1");
        entity.setEffectiveScope(SCOPE_NEW_USER_ONLY);
        entity.setPublishStatus(STATUS_DRAFT);
        entity.setDefaultTotalPlotCount((short) 6);
        entity.setDefaultUnlockedPlotCount((short) 1);
        entity.setDefaultLockedPlotCount((short) 5);
        entity.setDefaultLockRuleCode("DEFAULT_LOCKED");
        entity.setDefaultLockReason("pending unlock");
        entity.setAllocationRuleJson("{}");
        return entity;
    }

    private UserPlotAllocation newUserPlotAllocationEntity() {
        UserPlotAllocation entity = new UserPlotAllocation();
        initBaseEntity(entity);
        entity.setActive(true);
        entity.setTotalPlotCount((short) 1);
        entity.setUnlockedPlotCount((short) 0);
        entity.setLockedPlotCount((short) 1);
        entity.setLockRuleCode("DEFAULT_LOCKED");
        entity.setLockReason("pending unlock");
        entity.setAllocationRuleJson("{}");
        entity.setAppliedAt(OffsetDateTime.now());
        return entity;
    }

    private PlotPolicyApplyLog newPlotPolicyApplyLogEntity() {
        PlotPolicyApplyLog entity = new PlotPolicyApplyLog();
        initBaseEntity(entity);
        entity.setAppliedScope(SCOPE_MANUAL_APPLY);
        entity.setTargetUserCount(0);
        entity.setSuccessUserCount(0);
        entity.setFailedUserCount(0);
        entity.setRequestPayloadJson("{}");
        entity.setResultSnapshotJson("{}");
        entity.setAppliedAt(OffsetDateTime.now());
        return entity;
    }

    private void initBaseEntity(cn.jxufe.farm.entity.base.BaseEntity entity) {
        OffsetDateTime now = OffsetDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy(0L);
        entity.setUpdatedBy(0L);
        entity.setRemark("");
        entity.setStatus((short) 1);
        entity.setIsDeleted(false);
        entity.setOptLockVersion(0);
    }

    private void touchForUpdate(cn.jxufe.farm.entity.base.BaseEntity entity) {
        entity.setUpdatedAt(OffsetDateTime.now());
        entity.setUpdatedBy(0L);
        if (entity.getStatus() == null) {
            entity.setStatus((short) 1);
        }
        if (entity.getIsDeleted() == null) {
            entity.setIsDeleted(false);
        }
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(OffsetDateTime.now());
        }
        if (entity.getCreatedBy() == null) {
            entity.setCreatedBy(0L);
        }
        if (entity.getOptLockVersion() == null) {
            entity.setOptLockVersion(0);
        }
    }

    private Long requirePositiveId(Long id, String field) {
        if (id == null || id <= 0) {
            throw new ServiceException(BizErrorCode.PARAM_INVALID, field + " must be > 0");
        }
        return id;
    }

    private int defaultInt(Number value, int fallback) {
        return value == null ? fallback : value.intValue();
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private String defaultString(String value, String fallback) {
        String safe = safeString(value).trim();
        if (!safe.isEmpty()) {
            return safe;
        }
        return fallback == null ? "" : fallback;
    }

    private boolean defaultBool(Boolean value, boolean fallback) {
        return value == null ? fallback : value;
    }
}
