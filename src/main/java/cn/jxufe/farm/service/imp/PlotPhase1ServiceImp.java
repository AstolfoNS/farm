package cn.jxufe.farm.service.imp;

import cn.jxufe.farm.bean.dto.IdDTO;
import cn.jxufe.farm.bean.dto.PlotPolicyActivateDTO;
import cn.jxufe.farm.bean.dto.PlotPolicySaveDTO;
import cn.jxufe.farm.bean.dto.SoilTypeQueryDTO;
import cn.jxufe.farm.bean.dto.SoilTypeSaveDTO;
import cn.jxufe.farm.bean.vo.PlotPolicyVO;
import cn.jxufe.farm.bean.vo.SoilTypeGridVO;
import cn.jxufe.farm.common.enums.BizErrorCode;
import cn.jxufe.farm.common.exception.ServiceException;
import cn.jxufe.farm.common.pages.PageResult;
import cn.jxufe.farm.common.constants.AssetDefaultKeys;
import cn.jxufe.farm.dao.PlotPolicyDao;
import cn.jxufe.farm.dao.SoilTypeDao;
import cn.jxufe.farm.dao.UserPlotDao;
import cn.jxufe.farm.entity.PlotPolicy;
import cn.jxufe.farm.entity.SoilType;
import cn.jxufe.farm.service.PlotPhase1Service;
import cn.jxufe.farm.service.support.AssetDefaultProvider;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PlotPhase1ServiceImp implements PlotPhase1Service {

    private static final String SCOPE_NEW_USER_ONLY = "NEW_USER_ONLY";
    private static final String SCOPE_MANUAL_APPLY = "MANUAL_APPLY";
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_ARCHIVED = "ARCHIVED";

    private final SoilTypeDao soilTypeDao;
    private final PlotPolicyDao plotPolicyDao;
    private final UserPlotDao userPlotDao;
    private final AssetDefaultProvider assetDefaultProvider;

    public PlotPhase1ServiceImp(
            SoilTypeDao soilTypeDao,
            PlotPolicyDao plotPolicyDao,
            UserPlotDao userPlotDao,
            AssetDefaultProvider assetDefaultProvider
    ) {
        this.soilTypeDao = soilTypeDao;
        this.plotPolicyDao = plotPolicyDao;
        this.userPlotDao = userPlotDao;
        this.assetDefaultProvider = assetDefaultProvider;
    }

    // ======================== Soil Management ========================

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
        entity.setCoverImageUrl(defaultString(params.getCoverImageUrl(), defaultSoilCover()));
        entity.setLevel((short) Math.max(1, params.getLevel() == null ? 1 : params.getLevel()));
        entity.setUnlockExperienceRequired(Math.max(0L, params.getUnlockExperienceRequired() == null ? 0L : params.getUnlockExperienceRequired()));
        entity.setExpandCostCoin(Math.max(0L, params.getExpandCostCoin() == null ? 0L : params.getExpandCostCoin()));
        entity.setGrowSpeedMultiplier(parseGrowMultiplier(params.getGrowSpeedMultiplier()));
        entity.setDescription(safeString(params.getDescription()));
        touchForUpdate(entity);
        return soilTypeDao.save(entity).getId();
    }

    @Override
    @Transactional
    public void removeSoilType(IdDTO params) {
        Long id = requirePositiveId(params == null ? null : params.getId(), "soilTypeId");
        SoilType soilType = soilTypeDao.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ServiceException(BizErrorCode.SOIL_TYPE_NOT_FOUND, "soil type not found"));

        if (userPlotDao.existsBySoilTypeIdAndIsDeletedFalse(id)) {
            throw new ServiceException(BizErrorCode.SOIL_TYPE_NOT_FOUND, "soil type is referenced by user plots");
        }

        soilType.setIsDeleted(true);
        touchForUpdate(soilType);
        soilTypeDao.save(soilType);
    }

    // ======================== Policy Management ========================

    @Override
    public PlotPolicyVO currentPolicy() {
        Optional<PlotPolicy> policyOpt = plotPolicyDao.findFirstByActiveTrueAndIsDeletedFalseOrderByIdAsc();
        if (policyOpt.isEmpty()) {
            List<PlotPolicy> all = plotPolicyDao.findByIsDeletedFalseOrderByIdAsc();
            if (all.isEmpty()) {
                return buildFallbackPolicyVO();
            }
            return toPolicyVO(all.get(0));
        }
        return toPolicyVO(policyOpt.get());
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
        entity.setDefaultLockRuleCode(defaultString(params.getDefaultLockRuleCode(), "DEFAULT_LOCKED"));
        entity.setDefaultLockReason(defaultString(params.getDefaultLockReason(), "pending unlock"));
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
        return target.getId();
    }

    // ======================== Private Helpers ========================

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

    private String safeSoilSort(String sort) {
        String field = safeString(sort).trim().toLowerCase();
        return switch (field) {
            case "name" -> "name";
            case "bitcode", "bit_code" -> "bitCode";
            case "level" -> "level";
            default -> "id";
        };
    }

    private SoilTypeGridVO toSoilGridVO(SoilType item) {
        SoilTypeGridVO vo = new SoilTypeGridVO();
        vo.setId(item.getId());
        vo.setName(safeString(item.getName()));
        vo.setBitCode(item.getBitCode());
        vo.setCoverImageUrl(defaultString(item.getCoverImageUrl(), defaultSoilCover()));
        vo.setLevel(item.getLevel());
        vo.setUnlockExperienceRequired(item.getUnlockExperienceRequired());
        vo.setExpandCostCoin(item.getExpandCostCoin() == null ? 0L : item.getExpandCostCoin());
        vo.setGrowSpeedMultiplier(item.getGrowSpeedMultiplier() == null ? "1.00" : item.getGrowSpeedMultiplier().toPlainString());
        vo.setDescription(safeString(item.getDescription()));
        return vo;
    }

    private PlotPolicyVO buildFallbackPolicyVO() {
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
        return fallback;
    }

    private PlotPolicyVO toPolicyVO(PlotPolicy policy) {
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
        vo.setDefaultLockRuleCode(safeString(policy.getDefaultLockRuleCode()));
        vo.setDefaultLockReason(safeString(policy.getDefaultLockReason()));
        return vo;
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
        entity.setCoverImageUrl(defaultSoilCover());
        entity.setGrowSpeedMultiplier(BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP));
        entity.setLevel((short) 1);
        entity.setUnlockExperienceRequired(0L);
        entity.setExpandCostCoin(0L);
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

    private String defaultSoilCover() {
        return safeString(assetDefaultProvider.get(AssetDefaultKeys.SOIL_COVER)).trim();
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
