package cn.jxufe.farm.service.imp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.jxufe.farm.bean.dto.IdDTO;
import cn.jxufe.farm.bean.dto.PlotPolicySaveDTO;
import cn.jxufe.farm.bean.dto.PlotTypeQueryDTO;
import cn.jxufe.farm.bean.dto.PlotTypeSaveDTO;
import cn.jxufe.farm.bean.dto.UserPlotAllocationApplyDTO;
import cn.jxufe.farm.bean.dto.UserPlotAllocationQueryDTO;
import cn.jxufe.farm.bean.dto.UserPlotAllocationSaveDTO;
import cn.jxufe.farm.bean.vo.OptionVO;
import cn.jxufe.farm.bean.vo.PlotPolicyVO;
import cn.jxufe.farm.bean.vo.PlotTypeVO;
import cn.jxufe.farm.bean.vo.UserPlotAllocationApplyResultVO;
import cn.jxufe.farm.bean.vo.UserPlotAllocationVO;
import cn.jxufe.farm.common.constants.PlotRuleConstants;
import cn.jxufe.farm.common.enums.BizErrorCode;
import cn.jxufe.farm.common.exception.ServiceException;
import cn.jxufe.farm.common.pages.PageResult;
import cn.jxufe.farm.common.utils.ServiceGuardUtils;
import cn.jxufe.farm.config.properties.GameplayPolicyProperties;
import cn.jxufe.farm.dao.PlotPolicyDao;
import cn.jxufe.farm.dao.PlotTypeDao;
import cn.jxufe.farm.dao.SoilTypeDao;
import cn.jxufe.farm.dao.UserDao;
import cn.jxufe.farm.dao.UserPlotAllocationDao;
import cn.jxufe.farm.dao.UserPlotDao;
import cn.jxufe.farm.entity.PlotPolicy;
import cn.jxufe.farm.entity.PlotType;
import cn.jxufe.farm.entity.SoilType;
import cn.jxufe.farm.entity.User;
import cn.jxufe.farm.entity.UserPlot;
import cn.jxufe.farm.entity.UserPlotAllocation;
import cn.jxufe.farm.service.PlotAdminService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PlotAdminServiceImp implements PlotAdminService {

    private final PlotTypeDao plotTypeDao;
    private final PlotPolicyDao plotPolicyDao;
    private final UserPlotAllocationDao userPlotAllocationDao;
    private final SoilTypeDao soilTypeDao;
    private final UserDao userDao;
    private final UserPlotDao userPlotDao;
    private final GameplayPolicyProperties gameplayPolicyProperties;
    private final ObjectMapper objectMapper;

    public PlotAdminServiceImp(
            PlotTypeDao plotTypeDao,
            PlotPolicyDao plotPolicyDao,
            UserPlotAllocationDao userPlotAllocationDao,
            SoilTypeDao soilTypeDao,
            UserDao userDao,
            UserPlotDao userPlotDao,
            GameplayPolicyProperties gameplayPolicyProperties,
            ObjectMapper objectMapper
    ) {
        this.plotTypeDao = plotTypeDao;
        this.plotPolicyDao = plotPolicyDao;
        this.userPlotAllocationDao = userPlotAllocationDao;
        this.soilTypeDao = soilTypeDao;
        this.userDao = userDao;
        this.userPlotDao = userPlotDao;
        this.gameplayPolicyProperties = gameplayPolicyProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<OptionVO> listPlotTypeOptions() {
        return plotTypeDao.findByIsDeletedFalseOrderBySortOrderAscIdAsc().stream()
                .map(item -> buildOption(item.getId(), safeString(item.getName())))
                .collect(Collectors.toList());
    }

    @Override
    public List<OptionVO> listUserOptions() {
        return userDao.findByIsDeletedFalseOrderByIdAsc().stream()
                .map(user -> buildOption(user.getId(), "[" + safeString(user.getUsername()) + "]" + safeString(user.getNickname())))
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<PlotTypeVO> pagePlotTypes(PlotTypeQueryDTO query) {
        PlotTypeQueryDTO request = query == null ? new PlotTypeQueryDTO() : query;
        int pageNo = Math.max(1, request.getPage() == null ? 1 : request.getPage());
        int pageSize = Math.max(1, request.getRows() == null ? 10 : request.getRows());
        Sort.Direction direction = "desc".equalsIgnoreCase(request.getOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(direction, safePlotTypeSort(request.getSort())));

        Page<PlotType> page = plotTypeDao.findByIsDeletedFalseAndNameContainingIgnoreCase(
                safeString(request.getName()).trim(), pageable
        );
        Map<Long, String> soilNameMap = soilTypeDao.findByIsDeletedFalseOrderByIdAsc().stream()
                .collect(Collectors.toMap(SoilType::getId, s -> safeString(s.getName()), (a, b) -> a));

        List<PlotTypeVO> records = page.getContent().stream()
                .map(item -> buildPlotTypeVO(item, soilNameMap))
                .collect(Collectors.toList());
        return new PageResult<>(pageNo, pageSize, page.getTotalElements(), records);
    }

    @Override
    @Transactional
    public Long savePlotType(PlotTypeSaveDTO params) {
        ServiceGuardUtils.requireNotNull(params, BizErrorCode.PARAM_INVALID, "请求参数不能为空");
        String name = safeString(params.getName()).trim();
        if (name.isEmpty()) {
            throw new ServiceException(BizErrorCode.PLOT_TYPE_NAME_REQUIRED, "地块类型名称不能为空");
        }
        Long soilTypeId = ServiceGuardUtils.requirePositive(params.getSoilTypeId(), BizErrorCode.PARAM_INVALID, "soilTypeId无效");
        ServiceGuardUtils.requirePresent(soilTypeDao.findByIdAndIsDeletedFalse(soilTypeId), BizErrorCode.SOIL_TYPE_NOT_FOUND, "土壤类型不存在");

        plotTypeDao.findByNameAndIsDeletedFalse(name)
                .filter(item -> !Objects.equals(item.getId(), params.getId()))
                .ifPresent(item -> {
                    throw new ServiceException(BizErrorCode.PLOT_TYPE_NAME_DUPLICATE, "地块类型名称重复");
                });

        PlotType entity = (params.getId() != null && params.getId() > 0)
                ? ServiceGuardUtils.requirePresent(plotTypeDao.findByIdAndIsDeletedFalse(params.getId()), BizErrorCode.PLOT_TYPE_NOT_FOUND, "地块类型不存在")
                : newPlotTypeEntity();

        entity.setName(name);
        entity.setIconUrl(safeString(params.getIconUrl()));
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
        Long id = ServiceGuardUtils.requirePositive(params == null ? null : params.getId(), BizErrorCode.PARAM_INVALID, "地块类型ID无效");
        PlotType plotType = ServiceGuardUtils.requirePresent(plotTypeDao.findByIdAndIsDeletedFalse(id), BizErrorCode.PLOT_TYPE_NOT_FOUND, "地块类型不存在");

        boolean policyUsed = plotPolicyDao.findAll().stream()
                .filter(item -> !Boolean.TRUE.equals(item.getIsDeleted()))
                .anyMatch(policy -> Objects.equals(policy.getDefaultPlotTypeId(), id));
        if (policyUsed) {
            throw new ServiceException(BizErrorCode.PLOT_TYPE_IN_USE, "地块类型已被全局策略引用，禁止删除");
        }

        boolean allocationUsed = userPlotAllocationDao.findByIsDeletedFalse(Pageable.unpaged()).stream()
                .anyMatch(item -> Objects.equals(item.getDefaultPlotTypeId(), id));
        if (allocationUsed) {
            throw new ServiceException(BizErrorCode.PLOT_TYPE_IN_USE, "地块类型已被用户分配策略引用，禁止删除");
        }

        plotType.setIsDeleted(true);
        touchForUpdate(plotType);
        plotTypeDao.save(plotType);
    }

    @Override
    public PlotPolicyVO getPlotPolicy() {
        PlotPolicy policy = plotPolicyDao.findFirstByActiveTrueAndIsDeletedFalseOrderByIdAsc().orElse(null);
        if (policy == null) {
            return fallbackPlotPolicyVO();
        }
        Map<Long, PlotType> plotTypeMap = plotTypeDao.findByIsDeletedFalseOrderBySortOrderAscIdAsc().stream()
                .collect(Collectors.toMap(PlotType::getId, item -> item, (a, b) -> a));
        return buildPlotPolicyVO(policy, plotTypeMap);
    }

    @Override
    @Transactional
    public Long savePlotPolicy(PlotPolicySaveDTO params) {
        ServiceGuardUtils.requireNotNull(params, BizErrorCode.PARAM_INVALID, "请求参数不能为空");
        String policyName = safeString(params.getPolicyName()).trim();
        if (policyName.isEmpty()) {
            throw new ServiceException(BizErrorCode.PLOT_POLICY_INVALID, "策略名称不能为空");
        }
        short total = safeShort(params.getDefaultTotalPlotCount(), (short) Math.max(1, gameplayPolicyProperties.getPlot().getDefaults().getTotalPlotCount()));
        short unlocked = safeShort(params.getDefaultUnlockedPlotCount(), (short) Math.min(total, gameplayPolicyProperties.getPlot().getDefaults().getUnlockedPlotCount()));
        if (unlocked > total) {
            throw new ServiceException(BizErrorCode.PLOT_POLICY_INVALID, "默认解锁地块数量不能超过总地块数量");
        }
        short locked = safeShort(params.getDefaultLockedPlotCount(), (short) (total - unlocked));
        if (locked != (short) (total - unlocked)) {
            throw new ServiceException(BizErrorCode.PLOT_POLICY_INVALID, "默认锁定地块数量必须等于总地块减去解锁地块");
        }
        if (params.getDefaultPlotTypeId() != null && params.getDefaultPlotTypeId() > 0) {
            ServiceGuardUtils.requirePresent(
                    plotTypeDao.findByIdAndIsDeletedFalse(params.getDefaultPlotTypeId()),
                    BizErrorCode.PLOT_TYPE_NOT_FOUND,
                    "默认地块类型不存在"
            );
        }

        PlotPolicy entity = (params.getId() != null && params.getId() > 0)
                ? ServiceGuardUtils.requirePresent(plotPolicyDao.findByIdAndIsDeletedFalse(params.getId()), BizErrorCode.PLOT_POLICY_INVALID, "策略不存在")
                : newPlotPolicyEntity();

        entity.setPolicyName(policyName);
        entity.setActive(defaultBool(params.getActive(), true));
        entity.setDefaultTotalPlotCount(total);
        entity.setDefaultUnlockedPlotCount(unlocked);
        entity.setDefaultLockedPlotCount(locked);
        entity.setDefaultPlotTypeId(params.getDefaultPlotTypeId());
        entity.setDefaultLockRuleCode(defaultString(params.getDefaultLockRuleCode(), PlotRuleConstants.LOCK_RULE_DEFAULT_LOCKED));
        entity.setDefaultLockReason(defaultString(params.getDefaultLockReason(), "待解锁"));
        entity.setAllocationRuleJson(validateAndNormalizeAllocationRuleJson(
                params.getAllocationRuleJson(),
                total,
                BizErrorCode.PLOT_POLICY_INVALID
        ));
        touchForUpdate(entity);
        PlotPolicy saved = plotPolicyDao.save(entity);

        if (Boolean.TRUE.equals(saved.getActive())) {
            deactivateOtherPolicies(saved.getId());
        }
        return saved.getId();
    }

    @Override
    public PageResult<UserPlotAllocationVO> pageUserPlotAllocations(UserPlotAllocationQueryDTO query) {
        UserPlotAllocationQueryDTO request = query == null ? new UserPlotAllocationQueryDTO() : query;
        int pageNo = Math.max(1, request.getPage() == null ? 1 : request.getPage());
        int pageSize = Math.max(1, request.getRows() == null ? 10 : request.getRows());
        Sort.Direction direction = "desc".equalsIgnoreCase(request.getOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(direction, safeAllocationSort(request.getSort())));

        Page<UserPlotAllocation> page = userPlotAllocationDao.findByIsDeletedFalse(pageable);
        List<UserPlotAllocation> filtered = page.getContent();
        String usernameKeyword = safeString(request.getUsername()).trim().toLowerCase();

        if ((request.getUserId() != null && request.getUserId() > 0) || !usernameKeyword.isEmpty()) {
            filtered = userPlotAllocationDao.findByIsDeletedFalse(Pageable.unpaged()).stream()
                    .filter(item -> request.getUserId() == null || request.getUserId() <= 0 || Objects.equals(item.getUserId(), request.getUserId()))
                    .filter(item -> {
                        if (usernameKeyword.isEmpty()) {
                            return true;
                        }
                        return userDao.findByIdAndIsDeletedFalse(item.getUserId())
                                .map(user -> safeString(user.getUsername()).toLowerCase().contains(usernameKeyword))
                                .orElse(false);
                    })
                    .sorted(allocationComparator(request.getSort(), request.getOrder()))
                    .collect(Collectors.toList());
            return PageResult.of(mapAllocationRows(filtered), pageNo, pageSize);
        }

        return new PageResult<>(pageNo, pageSize, page.getTotalElements(), mapAllocationRows(filtered));
    }

    @Override
    @Transactional
    public Long saveUserPlotAllocation(UserPlotAllocationSaveDTO params) {
        ServiceGuardUtils.requireNotNull(params, BizErrorCode.PARAM_INVALID, "请求参数不能为空");
        Long userId = ServiceGuardUtils.requirePositive(params.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
        ServiceGuardUtils.requirePresent(userDao.findByIdAndIsDeletedFalse(userId), BizErrorCode.USER_NOT_FOUND, "用户不存在");

        short total = safeShort(params.getTotalPlotCount(), (short) 1);
        short unlocked = safeShort(params.getUnlockedPlotCount(), (short) 0);
        if (unlocked > total) {
            throw new ServiceException(BizErrorCode.PLOT_ALLOCATION_INVALID, "解锁地块数量不能超过总地块数量");
        }
        short locked = safeShort(params.getLockedPlotCount(), (short) (total - unlocked));
        if (locked != (short) (total - unlocked)) {
            throw new ServiceException(BizErrorCode.PLOT_ALLOCATION_INVALID, "锁定地块数量必须等于总地块减去解锁地块");
        }

        int existingPlotCount = userPlotDao.findByUserIdAndIsDeletedFalseOrderByPlotIndexAsc(userId).size();
        if (total < existingPlotCount) {
            throw new ServiceException(BizErrorCode.PLOT_ALLOCATION_INVALID, "分配总地块数量不能小于当前已有地块数量");
        }

        if (params.getDefaultPlotTypeId() != null && params.getDefaultPlotTypeId() > 0) {
            ServiceGuardUtils.requirePresent(
                    plotTypeDao.findByIdAndIsDeletedFalse(params.getDefaultPlotTypeId()),
                    BizErrorCode.PLOT_TYPE_NOT_FOUND,
                    "默认地块类型不存在"
            );
        }

        UserPlotAllocation entity = resolveUpsertAllocationEntity(params);
        entity.setUserId(userId);
        entity.setActive(defaultBool(params.getActive(), true));
        entity.setTotalPlotCount(total);
        entity.setUnlockedPlotCount(unlocked);
        entity.setLockedPlotCount(locked);
        entity.setDefaultPlotTypeId(params.getDefaultPlotTypeId());
        entity.setLockRuleCode(defaultString(params.getLockRuleCode(), PlotRuleConstants.LOCK_RULE_DEFAULT_LOCKED));
        entity.setLockReason(defaultString(params.getLockReason(), "待解锁"));
        entity.setAllocationRuleJson(validateAndNormalizeAllocationRuleJson(
                params.getAllocationRuleJson(),
                total,
                BizErrorCode.PLOT_ALLOCATION_INVALID
        ));
        touchForUpdate(entity);
        return userPlotAllocationDao.save(entity).getId();
    }

    @Override
    @Transactional
    public UserPlotAllocationApplyResultVO applyUserPlotAllocation(UserPlotAllocationApplyDTO params) {
        ServiceGuardUtils.requireNotNull(params, BizErrorCode.PARAM_INVALID, "请求参数不能为空");
        Long userId = ServiceGuardUtils.requirePositive(params.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
        ServiceGuardUtils.requirePresent(userDao.findByIdAndIsDeletedFalse(userId), BizErrorCode.USER_NOT_FOUND, "用户不存在");

        List<UserPlot> existing = userPlotDao.findByUserIdAndIsDeletedFalseOrderByPlotIndexAsc(userId);
        EffectiveAllocation effective = resolveEffectiveAllocation(userId);

        if (effective.totalPlotCount < existing.size()) {
            throw new ServiceException(BizErrorCode.PLOT_ALLOCATION_APPLY_FAILED, "目标地块数量不能小于当前已有地块数量");
        }

        int created = 0;
        int updated = 0;
        OffsetDateTime now = OffsetDateTime.now();

        Map<Short, UserPlot> plotByIndex = new HashMap<>();
        for (UserPlot item : existing) {
            plotByIndex.put(item.getPlotIndex(), item);
        }

        Map<Long, PlotType> plotTypeMap = plotTypeDao.findByIsDeletedFalseOrderBySortOrderAscIdAsc().stream()
                .collect(Collectors.toMap(PlotType::getId, item -> item, (a, b) -> a, LinkedHashMap::new));
        Long fallbackSoilTypeId = resolveFallbackSoilTypeId();
        List<Long> typeSequence = buildPlotTypeSequence(effective, plotTypeMap);

        List<UserPlot> toSave = new ArrayList<>();
        for (short i = 1; i <= effective.totalPlotCount; i++) {
            UserPlot plot = plotByIndex.get(i);
            boolean isNew = false;
            if (plot == null) {
                plot = newUserPlotEntity(userId, i, now);
                isNew = true;
            }
            boolean locked = i > effective.unlockedPlotCount;
            Long seqTypeId = typeSequence.get(i - 1);
            PlotType plotType = plotTypeMap.get(seqTypeId);
            Long soilTypeId = plotType != null ? plotType.getSoilTypeId() : fallbackSoilTypeId;
            if (soilTypeId == null || soilTypeId <= 0) {
                throw new ServiceException(BizErrorCode.PLOT_ALLOCATION_APPLY_FAILED, "无法为地块分配土壤类型");
            }

            plot.setSoilTypeId(soilTypeId);
            plot.setUnlockExperienceRequired(resolveUnlockExperienceRequired(i, locked, plotType));
            plot.setIsLocked(locked);
            plot.setLockReason(locked ? effective.lockReason : null);
            plot.setUnlockedAt(locked ? null : now);
            plot.setUpdatedAt(now);
            plot.setUpdatedBy(userId);

            toSave.add(plot);
            if (isNew) {
                created++;
            } else {
                updated++;
            }
        }

        userPlotDao.saveAll(toSave);

        userPlotAllocationDao.findByUserIdAndIsDeletedFalse(userId).ifPresent(item -> {
            item.setAppliedAt(now);
            touchForUpdate(item);
            userPlotAllocationDao.save(item);
        });

        UserPlotAllocationApplyResultVO result = new UserPlotAllocationApplyResultVO();
        result.setUserId(userId);
        result.setBeforeTotalPlots(existing.size());
        result.setAfterTotalPlots((int) effective.totalPlotCount);
        result.setCreatedPlots(created);
        result.setUpdatedPlots(updated);
        result.setLockSource(effective.lockSource);
        return result;
    }

    private UserPlotAllocation resolveUpsertAllocationEntity(UserPlotAllocationSaveDTO params) {
        if (params.getId() != null && params.getId() > 0) {
            return ServiceGuardUtils.requirePresent(
                    userPlotAllocationDao.findByIdAndIsDeletedFalse(params.getId()),
                    BizErrorCode.PLOT_ALLOCATION_NOT_FOUND,
                    "用户地块分配策略不存在"
            );
        }
        return userPlotAllocationDao.findByUserIdAndIsDeletedFalse(params.getUserId()).orElseGet(this::newAllocationEntity);
    }

    private EffectiveAllocation resolveEffectiveAllocation(Long userId) {
        Optional<UserPlotAllocation> userAllocationOpt = userPlotAllocationDao.findByUserIdAndActiveTrueAndIsDeletedFalse(userId);
        if (userAllocationOpt.isPresent()) {
            UserPlotAllocation item = userAllocationOpt.get();
            return new EffectiveAllocation(
                    item.getTotalPlotCount(),
                    item.getUnlockedPlotCount(),
                    item.getDefaultPlotTypeId(),
                    defaultString(item.getLockRuleCode(), PlotRuleConstants.LOCK_RULE_DEFAULT_LOCKED),
                    defaultString(item.getLockReason(), "待解锁"),
                    defaultAllocationJson(item.getAllocationRuleJson(), BizErrorCode.PLOT_ALLOCATION_INVALID),
                    PlotRuleConstants.LOCK_SOURCE_USER_ALLOCATION
            );
        }

        Optional<PlotPolicy> policyOpt = plotPolicyDao.findFirstByActiveTrueAndIsDeletedFalseOrderByIdAsc();
        if (policyOpt.isPresent()) {
            PlotPolicy policy = policyOpt.get();
            return new EffectiveAllocation(
                    policy.getDefaultTotalPlotCount(),
                    policy.getDefaultUnlockedPlotCount(),
                    policy.getDefaultPlotTypeId(),
                    defaultString(policy.getDefaultLockRuleCode(), PlotRuleConstants.LOCK_RULE_DEFAULT_LOCKED),
                    defaultString(policy.getDefaultLockReason(), "待解锁"),
                    defaultAllocationJson(policy.getAllocationRuleJson(), BizErrorCode.PLOT_POLICY_INVALID),
                    PlotRuleConstants.LOCK_SOURCE_GLOBAL_POLICY
            );
        }

        short total = (short) Math.max(1, gameplayPolicyProperties.getPlot().getDefaults().getTotalPlotCount());
        short unlocked = (short) clamp(
                gameplayPolicyProperties.getPlot().getDefaults().getUnlockedPlotCount(),
                0,
                total
        );
        return new EffectiveAllocation(
                total,
                unlocked,
                null,
                PlotRuleConstants.LOCK_RULE_SYSTEM_COMPAT,
                "待解锁",
                "{\"default\":{\"total\":" + total + ",\"locked\":" + (total - unlocked) + "}}",
                PlotRuleConstants.LOCK_SOURCE_SYSTEM
        );
    }

    private List<Long> buildPlotTypeSequence(EffectiveAllocation allocation, Map<Long, PlotType> plotTypeMap) {
        List<Long> typeIds = new ArrayList<>();
        if (allocation.allocationRuleJson != null && !allocation.allocationRuleJson.trim().isEmpty()) {
            try {
                JsonNode root = objectMapper.readTree(allocation.allocationRuleJson);
                if (root != null && root.isObject()) {
                    root.fields().forEachRemaining(entry -> {
                        Long typeId = parseLong(entry.getKey(), 0L);
                        JsonNode node = entry.getValue();
                        int count = node == null ? 0 : Math.max(0, node.path("total").asInt(0));
                        if (typeId > 0 && count > 0) {
                            for (int i = 0; i < count; i++) {
                                typeIds.add(typeId);
                            }
                        }
                    });
                }
            } catch (Exception ignored) {
            }
        }

        Long fallbackTypeId = allocation.defaultPlotTypeId;
        if ((fallbackTypeId == null || fallbackTypeId <= 0) && !plotTypeMap.isEmpty()) {
            fallbackTypeId = plotTypeMap.keySet().iterator().next();
        }

        while (typeIds.size() < allocation.totalPlotCount) {
            typeIds.add(fallbackTypeId == null ? 0L : fallbackTypeId);
        }
        if (typeIds.size() > allocation.totalPlotCount) {
            return typeIds.subList(0, allocation.totalPlotCount);
        }
        return typeIds;
    }

    private long resolveUnlockExperienceRequired(short plotIndex, boolean locked, PlotType plotType) {
        if (!locked) {
            return 0L;
        }
        long legacy = calculateLegacyUnlockRequiredExperience(plotIndex);
        if (plotType == null) {
            return legacy;
        }
        if (!Boolean.TRUE.equals(plotType.getUnlockRequired())) {
            return 0L;
        }
        return Math.max(legacy, Math.max(0L, plotType.getDefaultPlotUnlockExperienceConfig() == null ? 0L : plotType.getDefaultPlotUnlockExperienceConfig()));
    }

    private long calculateLegacyUnlockRequiredExperience(short plotIndex) {
        short initialUnlocked = gameplayPolicyProperties.getPlot().getDefaults().getUnlockedPlotCount();
        if (plotIndex <= initialUnlocked) {
            return 0L;
        }
        long base = gameplayPolicyProperties.getPlot().getUnlock().getBaseRequiredExperience();
        long step = gameplayPolicyProperties.getPlot().getUnlock().getRequiredExperienceStep();
        long stepTimes = Math.max(0, plotIndex - initialUnlocked - 1);
        return Math.max(0L, base + stepTimes * step);
    }

    private PlotTypeVO buildPlotTypeVO(PlotType item, Map<Long, String> soilNameMap) {
        PlotTypeVO vo = new PlotTypeVO();
        vo.setId(item.getId());
        vo.setName(safeString(item.getName()));
        vo.setIconUrl(safeString(item.getIconUrl()));
        vo.setSoilTypeId(item.getSoilTypeId());
        vo.setSoilTypeName(safeString(soilNameMap.get(item.getSoilTypeId())));
        vo.setUnlockRequired(defaultBool(item.getUnlockRequired(), true));
        vo.setDefaultUsable(defaultBool(item.getDefaultUsable(), true));
        vo.setDefaultPlotUnlockExperienceConfig(Math.max(0L, item.getDefaultPlotUnlockExperienceConfig() == null ? 0L : item.getDefaultPlotUnlockExperienceConfig()));
        vo.setSortOrder(item.getSortOrder() == null ? 0 : item.getSortOrder());
        vo.setDescription(safeString(item.getDescription()));
        return vo;
    }

    private PlotPolicyVO buildPlotPolicyVO(PlotPolicy policy, Map<Long, PlotType> plotTypeMap) {
        PlotPolicyVO vo = new PlotPolicyVO();
        vo.setId(policy.getId());
        vo.setPolicyName(safeString(policy.getPolicyName()));
        vo.setActive(defaultBool(policy.getActive(), true));
        vo.setDefaultTotalPlotCount(policy.getDefaultTotalPlotCount());
        vo.setDefaultUnlockedPlotCount(policy.getDefaultUnlockedPlotCount());
        vo.setDefaultLockedPlotCount(policy.getDefaultLockedPlotCount());
        vo.setDefaultPlotTypeId(policy.getDefaultPlotTypeId());
        PlotType plotType = policy.getDefaultPlotTypeId() == null ? null : plotTypeMap.get(policy.getDefaultPlotTypeId());
        vo.setDefaultPlotTypeName(plotType == null ? "" : safeString(plotType.getName()));
        vo.setDefaultLockRuleCode(defaultString(policy.getDefaultLockRuleCode(), PlotRuleConstants.LOCK_RULE_DEFAULT_LOCKED));
        vo.setDefaultLockReason(defaultString(policy.getDefaultLockReason(), "待解锁"));
        vo.setAllocationRuleJson(defaultAllocationJson(policy.getAllocationRuleJson(), BizErrorCode.PLOT_POLICY_INVALID));
        return vo;
    }

    private PlotPolicyVO fallbackPlotPolicyVO() {
        short total = (short) Math.max(1, gameplayPolicyProperties.getPlot().getDefaults().getTotalPlotCount());
        short unlocked = (short) clamp(gameplayPolicyProperties.getPlot().getDefaults().getUnlockedPlotCount(), 0, total);

        PlotPolicyVO vo = new PlotPolicyVO();
        vo.setId(0L);
        vo.setPolicyName("legacy-default-policy");
        vo.setActive(true);
        vo.setDefaultTotalPlotCount(total);
        vo.setDefaultUnlockedPlotCount(unlocked);
        vo.setDefaultLockedPlotCount((short) (total - unlocked));
        vo.setDefaultPlotTypeId(null);
        vo.setDefaultPlotTypeName("");
        vo.setDefaultLockRuleCode(PlotRuleConstants.LOCK_RULE_SYSTEM_COMPAT);
        vo.setDefaultLockReason("待解锁");
        vo.setAllocationRuleJson("{\"default\":{\"total\":" + total + ",\"locked\":" + (total - unlocked) + "}}");
        return vo;
    }

    private List<UserPlotAllocationVO> mapAllocationRows(List<UserPlotAllocation> rows) {
        Map<Long, User> userMap = userDao.findByIsDeletedFalseOrderByIdAsc().stream()
                .collect(Collectors.toMap(User::getId, item -> item, (a, b) -> a));
        Map<Long, PlotType> plotTypeMap = plotTypeDao.findByIsDeletedFalseOrderBySortOrderAscIdAsc().stream()
                .collect(Collectors.toMap(PlotType::getId, item -> item, (a, b) -> a));

        List<UserPlotAllocationVO> records = new ArrayList<>();
        for (UserPlotAllocation row : rows) {
            User user = userMap.get(row.getUserId());
            PlotType plotType = row.getDefaultPlotTypeId() == null ? null : plotTypeMap.get(row.getDefaultPlotTypeId());
            UserPlotAllocationVO vo = new UserPlotAllocationVO();
            vo.setId(row.getId());
            vo.setUserId(row.getUserId());
            vo.setUsername(user == null ? "" : safeString(user.getUsername()));
            vo.setNickname(user == null ? "" : safeString(user.getNickname()));
            vo.setActive(defaultBool(row.getActive(), true));
            vo.setTotalPlotCount(row.getTotalPlotCount());
            vo.setUnlockedPlotCount(row.getUnlockedPlotCount());
            vo.setLockedPlotCount(row.getLockedPlotCount());
            vo.setDefaultPlotTypeId(row.getDefaultPlotTypeId());
            vo.setDefaultPlotTypeName(plotType == null ? "" : safeString(plotType.getName()));
            vo.setLockRuleCode(defaultString(row.getLockRuleCode(), PlotRuleConstants.LOCK_RULE_DEFAULT_LOCKED));
            vo.setLockReason(defaultString(row.getLockReason(), "待解锁"));
            vo.setAllocationRuleJson(defaultAllocationJson(row.getAllocationRuleJson(), BizErrorCode.PLOT_ALLOCATION_INVALID));
            vo.setAppliedAt(row.getAppliedAt());
            records.add(vo);
        }
        return records;
    }

    private void deactivateOtherPolicies(Long currentPolicyId) {
        List<PlotPolicy> all = plotPolicyDao.findAll();
        for (PlotPolicy item : all) {
            if (Boolean.TRUE.equals(item.getIsDeleted())) {
                continue;
            }
            if (Objects.equals(item.getId(), currentPolicyId)) {
                continue;
            }
            if (Boolean.TRUE.equals(item.getActive())) {
                item.setActive(false);
                touchForUpdate(item);
                plotPolicyDao.save(item);
            }
        }
    }

    private OptionVO buildOption(Long id, String text) {
        OptionVO vo = new OptionVO();
        vo.setId(id);
        vo.setText(safeString(text));
        return vo;
    }

    private String validateAndNormalizeAllocationRuleJson(String raw, short totalPlotCount, BizErrorCode invalidCode) {
        String normalized = defaultAllocationJson(raw, invalidCode);
        if ("{}".equals(normalized)) {
            return normalized;
        }
        try {
            JsonNode root = objectMapper.readTree(normalized);
            if (root == null || !root.isObject()) {
                throw new ServiceException(invalidCode, "allocationRuleJson必须是JSON对象");
            }

            Set<Long> validPlotTypeIds = new HashSet<>(plotTypeDao.findByIsDeletedFalseOrderBySortOrderAscIdAsc().stream()
                    .map(PlotType::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet()));

            int configuredTotal = 0;
            var fields = root.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = safeString(entry.getKey()).trim();
                JsonNode node = entry.getValue();
                if (node == null || !node.isObject()) {
                    throw new ServiceException(invalidCode, "allocationRuleJson每个配置项都必须是对象");
                }
                if (!"default".equalsIgnoreCase(key)) {
                    Long typeId = parseLong(key, 0L);
                    if (typeId == null || typeId <= 0 || !validPlotTypeIds.contains(typeId)) {
                        throw new ServiceException(invalidCode, "allocationRuleJson存在无效地块类型ID: " + key);
                    }
                }

                int itemTotal = node.path("total").asInt(-1);
                int itemLocked = node.path("locked").asInt(0);
                if (itemTotal < 0) {
                    throw new ServiceException(invalidCode, "allocationRuleJson.total不能小于0");
                }
                if (itemLocked < 0) {
                    throw new ServiceException(invalidCode, "allocationRuleJson.locked不能小于0");
                }
                if (itemLocked > itemTotal) {
                    throw new ServiceException(invalidCode, "allocationRuleJson.locked不能大于total");
                }
                configuredTotal += itemTotal;
            }

            if (configuredTotal > totalPlotCount) {
                throw new ServiceException(invalidCode, "allocationRuleJson配置总数不能超过总地块数量");
            }
            return root.toString();
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException(invalidCode, "allocationRuleJson不是合法JSON");
        }
    }

    private PlotType newPlotTypeEntity() {
        PlotType entity = new PlotType();
        OffsetDateTime now = OffsetDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy(0L);
        entity.setUpdatedBy(0L);
        entity.setStatus((short) 1);
        entity.setIsDeleted(false);
        entity.setOptLockVersion(0);
        return entity;
    }

    private PlotPolicy newPlotPolicyEntity() {
        PlotPolicy entity = new PlotPolicy();
        OffsetDateTime now = OffsetDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy(0L);
        entity.setUpdatedBy(0L);
        entity.setStatus((short) 1);
        entity.setIsDeleted(false);
        entity.setOptLockVersion(0);
        return entity;
    }

    private UserPlotAllocation newAllocationEntity() {
        UserPlotAllocation entity = new UserPlotAllocation();
        OffsetDateTime now = OffsetDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy(0L);
        entity.setUpdatedBy(0L);
        entity.setStatus((short) 1);
        entity.setIsDeleted(false);
        entity.setOptLockVersion(0);
        return entity;
    }

    private UserPlot newUserPlotEntity(Long userId, short plotIndex, OffsetDateTime now) {
        UserPlot plot = new UserPlot();
        plot.setUserId(userId);
        plot.setPlotIndex(plotIndex);
        plot.setStatus((short) 1);
        plot.setIsDeleted(false);
        plot.setOptLockVersion(0);
        plot.setCreatedAt(now);
        plot.setCreatedBy(userId);
        plot.setUpdatedAt(now);
        plot.setUpdatedBy(userId);
        return plot;
    }

    private Long resolveFallbackSoilTypeId() {
        return soilTypeDao.findFirstByIsDeletedFalseOrderByLevelAscIdAsc()
                .map(SoilType::getId)
                .orElse(null);
    }

    private void touchForUpdate(cn.jxufe.farm.entity.base.BaseEntity entity) {
        entity.setUpdatedAt(OffsetDateTime.now());
        entity.setUpdatedBy(0L);
    }

    private String safePlotTypeSort(String sort) {
        return switch (safeString(sort).trim().toLowerCase()) {
            case "name" -> "name";
            case "sortorder" -> "sortOrder";
            case "soiltypeid" -> "soilTypeId";
            default -> "id";
        };
    }

    private String safeAllocationSort(String sort) {
        return switch (safeString(sort).trim().toLowerCase()) {
            case "userid" -> "userId";
            case "totalplotcount" -> "totalPlotCount";
            case "unlockedplotcount" -> "unlockedPlotCount";
            case "appliedat" -> "appliedAt";
            default -> "id";
        };
    }

    private Comparator<UserPlotAllocation> allocationComparator(String sort, String order) {
        Comparator<UserPlotAllocation> comparator = switch (safeString(sort).trim().toLowerCase()) {
            case "userid" -> Comparator.comparing(item -> item.getUserId() == null ? 0L : item.getUserId());
            case "totalplotcount" -> Comparator.comparing(item -> item.getTotalPlotCount() == null ? (short) 0 : item.getTotalPlotCount());
            case "unlockedplotcount" -> Comparator.comparing(item -> item.getUnlockedPlotCount() == null ? (short) 0 : item.getUnlockedPlotCount());
            case "appliedat" -> Comparator.comparing(item -> item.getAppliedAt() == null ? OffsetDateTime.MIN : item.getAppliedAt());
            default -> Comparator.comparing(item -> item.getId() == null ? 0L : item.getId());
        };
        return "desc".equalsIgnoreCase(order) ? comparator.reversed() : comparator;
    }

    private short safeShort(Integer value, short defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value < 0) {
            return 0;
        }
        if (value > Short.MAX_VALUE) {
            return Short.MAX_VALUE;
        }
        return value.shortValue();
    }

    private int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    private boolean defaultBool(Boolean value, boolean defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String defaultString(String value, String defaultValue) {
        String text = safeString(value).trim();
        return text.isEmpty() ? defaultValue : text;
    }

    private String defaultAllocationJson(String raw, BizErrorCode invalidCode) {
        String json = safeString(raw).trim();
        if (json.isEmpty()) {
            return "{}";
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            return node == null ? "{}" : node.toString();
        } catch (Exception ex) {
            throw new ServiceException(invalidCode, "allocationRuleJson不是合法JSON");
        }
    }

    private Long parseLong(String value, Long defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private record EffectiveAllocation(
            short totalPlotCount,
            short unlockedPlotCount,
            Long defaultPlotTypeId,
            String lockRuleCode,
            String lockReason,
            String allocationRuleJson,
            String lockSource
    ) {
    }
}
