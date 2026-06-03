package cn.jxufe.farm.service.imp;

import cn.jxufe.farm.bean.dto.IdDTO;
import cn.jxufe.farm.bean.dto.SeedAddOrUpdateDTO;
import cn.jxufe.farm.bean.dto.SeedFruitInventoryQueryDTO;
import cn.jxufe.farm.bean.dto.SeedInventoryQueryDTO;
import cn.jxufe.farm.bean.dto.SeedShopBuyDTO;
import cn.jxufe.farm.bean.dto.SeedShopHomeQueryDTO;
import cn.jxufe.farm.bean.dto.SeedShopOverviewDTO;
import cn.jxufe.farm.bean.dto.SeedShopQueryDTO;
import cn.jxufe.farm.bean.dto.SeedShopSellFruitDTO;
import cn.jxufe.farm.bean.dto.SeedShopTradeQueryDTO;
import cn.jxufe.farm.bean.dto.SeedStageAddOrUpdateDTO;
import cn.jxufe.farm.bean.dto.SeedStageQueryDTO;
import cn.jxufe.farm.bean.dto.SeedTypeQueryDTO;
import cn.jxufe.farm.bean.vo.OptionVO;
import cn.jxufe.farm.bean.vo.SeedFruitInventoryItemVO;
import cn.jxufe.farm.bean.vo.SeedGridVO;
import cn.jxufe.farm.bean.vo.SeedInventoryItemVO;
import cn.jxufe.farm.bean.vo.SeedShopBuyResultVO;
import cn.jxufe.farm.bean.vo.SeedShopHomeVO;
import cn.jxufe.farm.bean.vo.SeedShopItemVO;
import cn.jxufe.farm.bean.vo.SeedShopOverviewVO;
import cn.jxufe.farm.bean.vo.SeedShopSellFruitResultVO;
import cn.jxufe.farm.bean.vo.SeedShopTradeRecordVO;
import cn.jxufe.farm.bean.vo.SeedStageGridVO;
import cn.jxufe.farm.bean.vo.SoilOptionVO;
import cn.jxufe.farm.common.constants.AssetDefaultKeys;
import cn.jxufe.farm.common.enums.BizErrorCode;
import cn.jxufe.farm.common.exception.ServiceException;
import cn.jxufe.farm.common.pages.PageResult;
import cn.jxufe.farm.dao.GrowthStageDao;
import cn.jxufe.farm.dao.SeedGrowthStageDao;
import cn.jxufe.farm.dao.SeedTypeDao;
import cn.jxufe.farm.dao.UserDao;
import cn.jxufe.farm.dao.UserCropDao;
import cn.jxufe.farm.dao.UserFruitDao;
import cn.jxufe.farm.dao.UserInventoryFlowDao;
import cn.jxufe.farm.dao.UserSeedDao;
import cn.jxufe.farm.entity.GrowthStage;
import cn.jxufe.farm.entity.SeedGrowthStage;
import cn.jxufe.farm.entity.SeedType;
import cn.jxufe.farm.entity.User;
import cn.jxufe.farm.service.SeedService;
import cn.jxufe.farm.service.support.AssetDefaultProvider;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Primary
public class SeedServiceGuardedDecorator implements SeedService {

    private static final int STAGE_WIDTH_MIN = 1;
    private static final int STAGE_WIDTH_MAX = 4096;
    private static final int STAGE_HEIGHT_MIN = 1;
    private static final int STAGE_HEIGHT_MAX = 4096;
    private static final int STAGE_OFFSET_MIN = -2048;
    private static final int STAGE_OFFSET_MAX = 2048;

    private final SeedService delegate;
    private final SeedTypeDao seedTypeDao;
    private final GrowthStageDao growthStageDao;
    private final UserDao userDao;
    private final SeedGrowthStageDao seedGrowthStageDao;
    private final UserSeedDao userSeedDao;
    private final UserFruitDao userFruitDao;
    private final UserCropDao userCropDao;
    private final UserInventoryFlowDao userInventoryFlowDao;
    private final AssetDefaultProvider assetDefaultProvider;

    public SeedServiceGuardedDecorator(
            @Qualifier("seedServiceImp") SeedService delegate,
            SeedTypeDao seedTypeDao,
            GrowthStageDao growthStageDao,
            UserDao userDao,
            SeedGrowthStageDao seedGrowthStageDao,
            UserSeedDao userSeedDao,
            UserFruitDao userFruitDao,
            UserCropDao userCropDao,
            UserInventoryFlowDao userInventoryFlowDao,
            AssetDefaultProvider assetDefaultProvider
    ) {
        this.delegate = delegate;
        this.seedTypeDao = seedTypeDao;
        this.growthStageDao = growthStageDao;
        this.userDao = userDao;
        this.seedGrowthStageDao = seedGrowthStageDao;
        this.userSeedDao = userSeedDao;
        this.userFruitDao = userFruitDao;
        this.userCropDao = userCropDao;
        this.userInventoryFlowDao = userInventoryFlowDao;
        this.assetDefaultProvider = assetDefaultProvider;
    }

    @Override
    public PageResult<SeedGridVO> pageSeedTypes(SeedTypeQueryDTO query) {
        PageResult<SeedGridVO> result = delegate.pageSeedTypes(query);
        applySeedGridDefaults(result);
        return result;
    }

    @Override
    public PageResult<SeedShopItemVO> pageSeedShop(SeedShopQueryDTO query) {
        PageResult<SeedShopItemVO> result = delegate.pageSeedShop(query);
        Long userExperience = resolveUserExperience(query == null ? null : query.getUserId());
        applySeedShopDefaults(result, userExperience);
        return result;
    }

    @Override
    public SeedShopBuyResultVO buySeed(SeedShopBuyDTO params) {
        validateSeedUnlockByExperience(
                params == null ? null : params.getUserId(),
                params == null ? null : params.getSeedTypeId()
        );
        return delegate.buySeed(params);
    }

    @Override
    public SeedShopSellFruitResultVO sellFruit(SeedShopSellFruitDTO params) {
        return delegate.sellFruit(params);
    }

    @Override
    public PageResult<SeedShopTradeRecordVO> pageShopTrades(SeedShopTradeQueryDTO query) {
        return delegate.pageShopTrades(query);
    }

    @Override
    public PageResult<SeedFruitInventoryItemVO> pageFruitInventory(SeedFruitInventoryQueryDTO query) {
        PageResult<SeedFruitInventoryItemVO> result = delegate.pageFruitInventory(query);
        applyFruitInventoryDefaults(result);
        return result;
    }

    @Override
    public PageResult<SeedInventoryItemVO> pageSeedInventory(SeedInventoryQueryDTO query) {
        if (query == null || query.getUserId() == null || query.getUserId() <= 0) {
            throw new ServiceException(BizErrorCode.PARAM_INVALID, "鐢ㄦ埛ID鏃犳晥");
        }
        userDao.findByIdAndIsDeletedFalse(query.getUserId())
                .orElseThrow(() -> new ServiceException(BizErrorCode.USER_NOT_FOUND, "用户不存在"));

        int pageNo = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int pageSize = query.getRows() == null || query.getRows() < 1 ? 10 : Math.min(query.getRows(), 100);
        String nameKeyword = query.getName() == null ? "" : query.getName().trim().toLowerCase();

        List<SeedType> seedTypes = seedTypeDao.findByIsDeletedFalseOrderByIdAsc();
        Map<Long, SeedType> seedTypeMap = seedTypes.stream().collect(Collectors.toMap(SeedType::getId, item -> item));

        List<SeedInventoryItemVO> rows = userSeedDao.findByUserIdAndIsDeletedFalseOrderByIdAsc(query.getUserId()).stream()
                .filter(item -> seedTypeMap.containsKey(item.getSeedTypeId()))
                .map(item -> {
                    SeedType seedType = seedTypeMap.get(item.getSeedTypeId());
                    SeedInventoryItemVO vo = new SeedInventoryItemVO();
                    long quantity = item.getQuantity() == null ? 0L : Math.max(item.getQuantity(), 0L);
                    long frozen = item.getFrozenQuantity() == null ? 0L : Math.max(item.getFrozenQuantity(), 0L);
                    vo.setSeedTypeId(seedType.getId());
                    vo.setSeedName(seedType.getName() == null ? "" : seedType.getName());
                    vo.setCoverImageUrl(normalizeSeedCoverUrl(seedType.getCoverImageUrl()));
                    vo.setQuantity(quantity);
                    vo.setFrozenQuantity(frozen);
                    vo.setAvailableQuantity(Math.max(quantity - frozen, 0L));
                    vo.setUnitBuyPrice(seedType.getPrice() == null ? 0L : Math.max(seedType.getPrice(), 0L));
                    vo.setUnlockExperienceRequired(resolveUnlockExperienceRequired(seedType));
                    return vo;
                })
                .filter(item -> nameKeyword.isEmpty() || item.getSeedName().toLowerCase().contains(nameKeyword))
                .collect(Collectors.toList());

        return PageResult.of(rows, pageNo, pageSize);
    }

    @Override
    public SeedShopOverviewVO shopOverview(SeedShopOverviewDTO query) {
        return delegate.shopOverview(query);
    }

    @Override
    public SeedShopHomeVO shopHome(SeedShopHomeQueryDTO query) {
        SeedShopHomeVO result = delegate.shopHome(query);
        if (result != null) {
            Long userExperience = resolveUserExperience(query == null ? null : query.getUserId());
            applySeedShopDefaults(result.getShopPage(), userExperience);
        }
        return result;
    }

    @Override
    @Transactional
    public Long saveSeedType(SeedAddOrUpdateDTO params) {
        if (params != null) {
            params.setCoverImageUrl(normalizeSeedCoverUrl(params.getCoverImageUrl()));
            params.setUnlockExperienceRequired(normalizeUnlockExperienceRequired(params.getUnlockExperienceRequired(), params.getLevel()));
        }
        Long seedTypeId = delegate.saveSeedType(params);
        if (seedTypeId != null && seedTypeId > 0) {
            SeedType seedType = seedTypeDao.findByIdAndIsDeletedFalse(seedTypeId)
                    .orElseThrow(() -> new ServiceException(BizErrorCode.SEED_TYPE_NOT_FOUND, "种子类型不存在"));
            long unlockRequired = params == null
                    ? normalizeUnlockExperienceRequired(null, seedType.getLevel())
                    : normalizeUnlockExperienceRequired(params.getUnlockExperienceRequired(), seedType.getLevel());
            seedType.setUnlockExperienceRequired(unlockRequired);
            seedType.setHarvestStageIndex(normalizeStagePointer(params == null ? null : params.getHarvestStageIndex()));
            seedType.setRegrowStageIndex(normalizeRegrowPointer(params));
            seedTypeDao.save(seedType);
        }
        validateStageRules(seedTypeId, true);
        return seedTypeId;
    }

    @Override
    @Transactional
    public void removeSeedType(IdDTO params) {
        Long seedTypeId = params == null ? null : params.getId();
        if (seedTypeId == null || seedTypeId <= 0) {
            throw new ServiceException(BizErrorCode.PARAM_INVALID, "种子类型ID无效");
        }
        ensureSeedTypeNotReferenced(seedTypeId);
        delegate.removeSeedType(params);
    }

    @Override
    public List<OptionVO> listSeedQualityOptions() {
        return delegate.listSeedQualityOptions();
    }

    @Override
    public List<SoilOptionVO> listSoilOptions() {
        return delegate.listSoilOptions();
    }

    @Override
    public List<OptionVO> listGrowthStageOptions() {
        return delegate.listGrowthStageOptions();
    }

    @Override
    public PageResult<SeedStageGridVO> pageSeedStages(SeedStageQueryDTO query) {
        PageResult<SeedStageGridVO> result = delegate.pageSeedStages(query);
        applySeedStageDefaults(result);
        return result;
    }

    @Override
    @Transactional
    public void saveSeedStage(SeedStageAddOrUpdateDTO params) {
        if (params != null) {
            params.setAssetUrl(normalizeStageAssetUrl(params.getAssetUrl()));
        }
        validateStageAssetUrl(params == null ? null : params.getAssetUrl());
        validateStageLayout(params);
        delegate.saveSeedStage(params);
        Long seedTypeId = params == null ? null : params.getSeedTypeId();
        validateStageRules(seedTypeId, false);
    }

    @Override
    @Transactional
    public void removeSeedStage(IdDTO params) {
        Long stageId = params == null ? null : params.getId();
        if (stageId == null || stageId <= 0) {
            throw new ServiceException(BizErrorCode.PARAM_INVALID, "闃舵ID鏃犳晥");
        }
        SeedGrowthStage target = seedGrowthStageDao.findByIdAndIsDeletedFalse(stageId)
                .orElseThrow(() -> new ServiceException(BizErrorCode.SEED_STAGE_NOT_FOUND, "种子阶段配置不存在"));
        List<SeedGrowthStage> stages = seedGrowthStageDao.findBySeedTypeIdAndIsDeletedFalseOrderByStageIndexAsc(target.getSeedTypeId());
        short maxStageIndex = stages.stream()
                .map(SeedGrowthStage::getStageIndex)
                .filter(idx -> idx != null && idx > 0)
                .max(Short::compareTo)
                .orElse((short) 0);
        if (!target.getStageIndex().equals(maxStageIndex)) {
            throw new ServiceException(BizErrorCode.SEED_STAGE_SEQUENCE_INVALID, "鍙兘鍒犻櫎鏈€鍚庝竴涓樁娈碉紝閬垮厤闃舵搴忓彿鏂。");
        }

        delegate.removeSeedStage(params);
        validateStageRules(target.getSeedTypeId(), false);
    }

    private void ensureSeedTypeNotReferenced(Long seedTypeId) {
        boolean usedByUserSeed = userSeedDao.existsBySeedTypeIdAndIsDeletedFalse(seedTypeId);
        boolean usedByUserFruit = userFruitDao.existsBySeedTypeIdAndIsDeletedFalse(seedTypeId);
        boolean usedByUserCrop = userCropDao.existsBySeedTypeIdAndIsDeletedFalse(seedTypeId);
        boolean usedByFlow = userInventoryFlowDao.existsBySeedTypeIdAndIsDeletedFalse(seedTypeId);
        if (usedByUserSeed || usedByUserFruit || usedByUserCrop || usedByFlow) {
            throw new ServiceException(BizErrorCode.SEED_TYPE_IN_USE, "种子类型存在库存/种植/交易记录引用，禁止删除");
        }
    }

    private void validateStageRules(Long seedTypeId, boolean requireCompleteConfig) {
        if (seedTypeId == null || seedTypeId <= 0) {
            throw new ServiceException(BizErrorCode.PARAM_INVALID, "种子类型ID无效");
        }
        List<SeedGrowthStage> stages = seedGrowthStageDao.findBySeedTypeIdAndIsDeletedFalseOrderByStageIndexAsc(seedTypeId);
        if (stages.isEmpty()) {
            return;
        }
        short expect = 1;
        Set<Short> stageIndexSet = new HashSet<>();
        Map<Long, String> growthStageNameMap = buildGrowthStageNameMap();
        short lastStageIndex = 0;
        short witherStageCount = 0;
        Short witherStageIndex = null;
        for (SeedGrowthStage stage : stages) {
            short actual = stage.getStageIndex() == null ? 0 : stage.getStageIndex();
            if (actual != expect) {
                throw new ServiceException(BizErrorCode.SEED_STAGE_SEQUENCE_INVALID, "阶段序号必须连续为 1..N");
            }
            stageIndexSet.add(actual);
            lastStageIndex = actual;
            String growthStageName = safeString(growthStageNameMap.get(stage.getGrowthStageId())).trim();
            if ("枯萎".equals(growthStageName)) {
                witherStageCount++;
                witherStageIndex = actual;
            }
            expect++;
        }
        if (witherStageCount > 1) {
            throw new ServiceException(BizErrorCode.SEED_STAGE_SEQUENCE_INVALID, "同一种子只能配置一个枯萎阶段");
        }
        if (witherStageIndex != null && witherStageIndex != lastStageIndex) {
            throw new ServiceException(BizErrorCode.SEED_STAGE_SEQUENCE_INVALID, "枯萎阶段必须位于最后一阶段");
        }
        SeedType seedType = seedTypeDao.findByIdAndIsDeletedFalse(seedTypeId)
                .orElseThrow(() -> new ServiceException(BizErrorCode.SEED_TYPE_NOT_FOUND, "种子类型不存在"));
        Short harvest = normalizeStagePointer(seedType.getHarvestStageIndex());
        Short regrow = normalizeStagePointer(seedType.getRegrowStageIndex());
        if (!requireCompleteConfig) {
            validateOptionalStagePointers(stageIndexSet, lastStageIndex, witherStageIndex, harvest, regrow);
            return;
        }
        if (witherStageIndex == null) {
            throw new ServiceException(BizErrorCode.SEED_STAGE_SEQUENCE_INVALID, "请为该种子补充最后一个“枯萎”阶段");
        }
        if (harvest == null || !stageIndexSet.contains(harvest)) {
            throw new ServiceException(BizErrorCode.SEED_HARVEST_STAGE_INVALID, "收获阶段必须存在于该种子的阶段集合中");
        }
        if (harvest >= witherStageIndex) {
            throw new ServiceException(BizErrorCode.SEED_HARVEST_STAGE_INVALID, "收获阶段必须早于最后的枯萎阶段");
        }
        short maxHarvestCount = normalizeMaxHarvestCount(seedType.getMaxHarvestCount());
        if (maxHarvestCount > 1) {
            if (regrow == null || !stageIndexSet.contains(regrow)) {
                throw new ServiceException(BizErrorCode.SEED_REGROW_STAGE_INVALID, "多次收获作物必须配置存在的再生阶段");
            }
            if (regrow >= harvest) {
                throw new ServiceException(BizErrorCode.SEED_REGROW_STAGE_INVALID, "收获阶段序号必须大于再生阶段序号");
            }
        } else if (regrow != null && stageIndexSet.contains(regrow) && regrow >= harvest) {
            throw new ServiceException(BizErrorCode.SEED_REGROW_STAGE_INVALID, "再生阶段序号必须小于收获阶段序号");
        }
    }

    private void applySeedGridDefaults(PageResult<SeedGridVO> pageResult) {
        if (pageResult == null || pageResult.getRecords() == null) {
            return;
        }
        Map<Long, SeedType> seedTypeMap = buildSeedTypeMap(pageResult.getRecords().stream()
                .map(SeedGridVO::getId)
                .collect(Collectors.toSet()));
        for (SeedGridVO row : pageResult.getRecords()) {
            if (row != null) {
                row.setCoverImageUrl(normalizeSeedCoverUrl(row.getCoverImageUrl()));
                SeedType seedType = seedTypeMap.get(row.getId());
                row.setUnlockExperienceRequired(resolveUnlockExperienceRequired(seedType));
                row.setHarvestStageIndex(seedType == null ? null : seedType.getHarvestStageIndex());
                row.setRegrowStageIndex(seedType == null ? null : seedType.getRegrowStageIndex());
            }
        }
    }

    private void applySeedShopDefaults(PageResult<SeedShopItemVO> pageResult, Long userExperience) {
        if (pageResult == null || pageResult.getRecords() == null) {
            return;
        }
        long currentExperience = userExperience == null || userExperience < 0 ? 0L : userExperience;
        Map<Long, SeedType> seedTypeMap = buildSeedTypeMap(pageResult.getRecords().stream()
                .map(SeedShopItemVO::getId)
                .collect(Collectors.toSet()));
        for (SeedShopItemVO row : pageResult.getRecords()) {
            if (row != null) {
                row.setCoverImageUrl(normalizeSeedCoverUrl(row.getCoverImageUrl()));
                SeedType seedType = seedTypeMap.get(row.getId());
                long unlockRequired = resolveUnlockExperienceRequired(seedType);
                boolean unlocked = currentExperience >= unlockRequired;
                row.setUnlockExperienceRequired(unlockRequired);
                row.setCurrentUserExperience(currentExperience);
                row.setUnlockedByExperience(unlocked);
                row.setUnlockProgressPercent(calcUnlockProgressPercent(currentExperience, unlockRequired));
            }
        }
    }

    private void applyFruitInventoryDefaults(PageResult<SeedFruitInventoryItemVO> pageResult) {
        if (pageResult == null || pageResult.getRecords() == null) {
            return;
        }
        for (SeedFruitInventoryItemVO row : pageResult.getRecords()) {
            if (row != null) {
                row.setCoverImageUrl(normalizeSeedCoverUrl(row.getCoverImageUrl()));
            }
        }
    }

    private void applySeedStageDefaults(PageResult<SeedStageGridVO> pageResult) {
        if (pageResult == null || pageResult.getRecords() == null) {
            return;
        }
        for (SeedStageGridVO row : pageResult.getRecords()) {
            if (row != null) {
                row.setAssetUrl(normalizeStageAssetUrl(row.getAssetUrl()));
            }
        }
    }

    private String normalizeSeedCoverUrl(String rawUrl) {
        String value = rawUrl == null ? "" : rawUrl.trim();
        if (value.isEmpty()) {
            return defaultSeedCoverUrl();
        }
        if (value.startsWith("http://") || value.startsWith("https://") || value.startsWith("/")) {
            return value;
        }
        if (value.startsWith("resources/") || value.startsWith("oss/")) {
            return "/" + value;
        }
        return "/oss/" + value.replaceFirst("^/+", "");
    }

    private String normalizeStageAssetUrl(String rawUrl) {
        String value = rawUrl == null ? "" : rawUrl.trim();
        if (value.isEmpty()) {
            return defaultSeedStageUrl();
        }
        if (value.startsWith("http://") || value.startsWith("https://") || value.startsWith("/")) {
            return value;
        }
        if (value.startsWith("resources/") || value.startsWith("oss/")) {
            return "/" + value;
        }
        return "/oss/" + value.replaceFirst("^/+", "");
    }

    private Map<Long, SeedType> buildSeedTypeMap(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        List<Long> idList = ids.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (idList.isEmpty()) {
            return Map.of();
        }
        return seedTypeDao.findByIdInAndIsDeletedFalse(idList).stream()
                .collect(Collectors.toMap(SeedType::getId, item -> item));
    }

    private String defaultSeedCoverUrl() {
        return safeString(assetDefaultProvider.get(AssetDefaultKeys.SEED_COVER)).trim();
    }

    private String defaultSeedStageUrl() {
        return safeString(assetDefaultProvider.get(AssetDefaultKeys.SEED_STAGE)).trim();
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private Long resolveUserExperience(Long userId) {
        if (userId == null || userId <= 0) {
            return 0L;
        }
        User user = userDao.findByIdAndIsDeletedFalse(userId).orElse(null);
        if (user == null || user.getExperience() == null) {
            return 0L;
        }
        return Math.max(user.getExperience(), 0L);
    }

    private long resolveUnlockExperienceRequired(SeedType seedType) {
        if (seedType == null) {
            return 0L;
        }
        return normalizeUnlockExperienceRequired(seedType.getUnlockExperienceRequired(), seedType.getLevel());
    }

    private long normalizeUnlockExperienceRequired(Long unlockExperienceRequired, Short level) {
        if (unlockExperienceRequired != null && unlockExperienceRequired >= 0) {
            return unlockExperienceRequired;
        }
        int lv = level == null ? 1 : Math.max(level, (short) 1);
        if (lv <= 1) {
            return 0L;
        }
        if (lv == 2) {
            return 300L;
        }
        if (lv == 3) {
            return 900L;
        }
        return 1500L + (long) (lv - 4) * 600L;
    }

    private int calcUnlockProgressPercent(long currentExperience, long unlockRequired) {
        if (unlockRequired <= 0) {
            return 100;
        }
        if (currentExperience <= 0) {
            return 0;
        }
        long percent = (currentExperience * 100L) / unlockRequired;
        if (percent < 0L) {
            return 0;
        }
        if (percent > 100L) {
            return 100;
        }
        return (int) percent;
    }

    private void validateSeedUnlockByExperience(Long userId, Long seedTypeId) {
        if (userId == null || userId <= 0 || seedTypeId == null || seedTypeId <= 0) {
            return;
        }
        User user = userDao.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ServiceException(BizErrorCode.USER_NOT_FOUND, "用户不存在"));
        SeedType seedType = seedTypeDao.findByIdAndIsDeletedFalse(seedTypeId)
                .orElseThrow(() -> new ServiceException(BizErrorCode.SEED_TYPE_NOT_FOUND, "种子类型不存在"));
        long unlockRequired = resolveUnlockExperienceRequired(seedType);
        long userExperience = user.getExperience() == null ? 0L : Math.max(user.getExperience(), 0L);
        if (userExperience < unlockRequired) {
            throw new ServiceException(
                    BizErrorCode.EXPERIENCE_NOT_ENOUGH,
                    "经验不足，尚未解锁该种子。需要经验 " + unlockRequired + "，当前经验 " + userExperience
            );
        }
    }

    private void validateStageAssetUrl(String assetUrl) {
        String value = assetUrl == null ? "" : assetUrl.trim();
        if (value.isEmpty()) {
            return;
        }
        if (value.startsWith("/resources/") || value.startsWith("/oss/")) {
            return;
        }
        throw new ServiceException(BizErrorCode.SEED_ASSET_URL_INVALID, "资源路径必须位于 /resources 或 /oss 下");
    }

    private void validateStageLayout(SeedStageAddOrUpdateDTO params) {
        if (params == null) {
            throw new ServiceException(BizErrorCode.PARAM_INVALID, "璇锋眰鍙傛暟涓嶈兘涓虹┖");
        }
        int width = params.getWidth() == null ? 0 : params.getWidth();
        int height = params.getHeight() == null ? 0 : params.getHeight();
        int offsetX = params.getOffsetX() == null ? 0 : params.getOffsetX();
        int offsetY = params.getOffsetY() == null ? 0 : params.getOffsetY();
        if (width < STAGE_WIDTH_MIN || width > STAGE_WIDTH_MAX) {
            throw new ServiceException(BizErrorCode.SEED_STAGE_LAYOUT_INVALID, "width瓒呭嚭鍏佽鑼冨洿");
        }
        if (height < STAGE_HEIGHT_MIN || height > STAGE_HEIGHT_MAX) {
            throw new ServiceException(BizErrorCode.SEED_STAGE_LAYOUT_INVALID, "height瓒呭嚭鍏佽鑼冨洿");
        }
        if (offsetX < STAGE_OFFSET_MIN || offsetX > STAGE_OFFSET_MAX) {
            throw new ServiceException(BizErrorCode.SEED_STAGE_LAYOUT_INVALID, "offsetX瓒呭嚭鍏佽鑼冨洿");
        }
        if (offsetY < STAGE_OFFSET_MIN || offsetY > STAGE_OFFSET_MAX) {
            throw new ServiceException(BizErrorCode.SEED_STAGE_LAYOUT_INVALID, "offsetY瓒呭嚭鍏佽鑼冨洿");
        }
        BigDecimal probability = params.getBugProbability() != null ? params.getBugProbability() : params.getPestProbability();
        if (probability != null && (probability.compareTo(BigDecimal.ZERO) < 0 || probability.compareTo(BigDecimal.ONE) > 0)) {
            throw new ServiceException(BizErrorCode.SEED_STAGE_LAYOUT_INVALID, "铏姒傜巼蹇呴』鍦?鍒?涔嬮棿");
        }
    }

    private Map<Long, String> buildGrowthStageNameMap() {
        return growthStageDao.findByIsDeletedFalseOrderByIdAsc().stream()
                .collect(Collectors.toMap(GrowthStage::getId, item -> safeString(item.getName())));
    }

    private void validateOptionalStagePointers(Set<Short> stageIndexSet, short lastStageIndex, Short witherStageIndex, Short harvest, Short regrow) {
        if (harvest != null && stageIndexSet.contains(harvest)) {
            if (witherStageIndex != null && harvest >= witherStageIndex) {
                throw new ServiceException(BizErrorCode.SEED_HARVEST_STAGE_INVALID, "收获阶段必须早于枯萎阶段");
            }
            if (harvest >= lastStageIndex) {
                throw new ServiceException(BizErrorCode.SEED_HARVEST_STAGE_INVALID, "收获阶段不能落在最后一阶段");
            }
        }
        if (regrow != null && harvest != null && stageIndexSet.contains(regrow) && stageIndexSet.contains(harvest) && regrow >= harvest) {
            throw new ServiceException(BizErrorCode.SEED_REGROW_STAGE_INVALID, "再生阶段序号必须小于收获阶段序号");
        }
    }

    private Short normalizeStagePointer(Short value) {
        return value == null || value <= 0 ? null : value;
    }

    private Short normalizeRegrowPointer(SeedAddOrUpdateDTO params) {
        if (params == null) {
            return null;
        }
        short maxHarvestCount = normalizeMaxHarvestCount(params.getMaxHarvestCount());
        if (maxHarvestCount <= 1) {
            return null;
        }
        return normalizeStagePointer(params.getRegrowStageIndex());
    }

    private short normalizeMaxHarvestCount(Short value) {
        return value == null || value <= 0 ? (short) 1 : value;
    }
}


