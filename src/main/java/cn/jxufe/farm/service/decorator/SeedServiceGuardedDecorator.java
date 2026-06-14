package cn.jxufe.farm.service.decorator;

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
import cn.jxufe.farm.common.utils.FileAccessPathUtils;
import cn.jxufe.farm.config.properties.LocalFileStorageProperties;
import cn.jxufe.farm.dao.GrowthStageDao;
import cn.jxufe.farm.dao.SeedGrowthStageDao;
import cn.jxufe.farm.dao.SeedTypeDao;
import cn.jxufe.farm.dao.UserCropDao;
import cn.jxufe.farm.dao.UserDao;
import cn.jxufe.farm.dao.UserFruitDao;
import cn.jxufe.farm.dao.UserInventoryFlowDao;
import cn.jxufe.farm.dao.UserSeedDao;
import cn.jxufe.farm.entity.GrowthStage;
import cn.jxufe.farm.entity.SeedGrowthStage;
import cn.jxufe.farm.entity.SeedType;
import cn.jxufe.farm.entity.User;
import cn.jxufe.farm.service.SeedService;
import cn.jxufe.farm.service.support.AssetDefaultProvider;
import cn.jxufe.farm.service.support.SeedStageConfigValidator;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

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
  private final LocalFileStorageProperties fileStorageProperties;

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
      AssetDefaultProvider assetDefaultProvider,
      LocalFileStorageProperties fileStorageProperties) {
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
    this.fileStorageProperties = fileStorageProperties;
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
        params == null ? null : params.getUserId(), params == null ? null : params.getSeedTypeId());
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
    PageResult<SeedInventoryItemVO> result = delegate.pageSeedInventory(query);
    applySeedInventoryDefaults(result);
    return result;
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
      params.setUnlockExperienceRequired(
          normalizeUnlockExperienceRequired(
              params.getUnlockExperienceRequired(), params.getLevel()));
    }
    Long seedTypeId = delegate.saveSeedType(params);
    if (seedTypeId != null && seedTypeId > 0) {
      SeedType seedType =
          seedTypeDao
              .findByIdAndIsDeletedFalse(seedTypeId)
              .orElseThrow(() -> new ServiceException(BizErrorCode.SEED_TYPE_NOT_FOUND, "种子类型不存在"));
      long unlockRequired =
          params == null
              ? normalizeUnlockExperienceRequired(null, seedType.getLevel())
              : normalizeUnlockExperienceRequired(
                  params.getUnlockExperienceRequired(), seedType.getLevel());
      seedType.setUnlockExperienceRequired(unlockRequired);
      seedType.setHarvestStageIndex(
          normalizeStagePointer(params == null ? null : params.getHarvestStageIndex()));
      seedType.setRegrowStageIndex(normalizeRegrowPointer(params));
      seedTypeDao.save(seedType);
    }
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
  }

  @Override
  @Transactional
  public void removeSeedStage(IdDTO params) {
    Long stageId = params == null ? null : params.getId();
    if (stageId == null || stageId <= 0) {
      throw new ServiceException(BizErrorCode.PARAM_INVALID, "阶段ID无效");
    }
    delegate.removeSeedStage(params);
  }

  @Override
  public void validateSeedStages(IdDTO params) {
    Long seedTypeId = params == null ? null : params.getId();
    delegate.validateSeedStages(params);
    validateStageRules(seedTypeId, true);
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
    List<SeedGrowthStage> stages =
        seedGrowthStageDao.findBySeedTypeIdAndIsDeletedFalseOrderByStageIndexAsc(seedTypeId);
    SeedType seedType =
        seedTypeDao
            .findByIdAndIsDeletedFalse(seedTypeId)
            .orElseThrow(() -> new ServiceException(BizErrorCode.SEED_TYPE_NOT_FOUND, "种子类型不存在"));
    Map<Long, String> growthStageNameMap = buildGrowthStageNameMap();
    if (requireCompleteConfig) {
      SeedStageConfigValidator.validateComplete(seedType, stages, growthStageNameMap);
    } else {
      SeedStageConfigValidator.validateDraft(seedType, stages, growthStageNameMap);
    }
  }

  private void applySeedGridDefaults(PageResult<SeedGridVO> pageResult) {
    if (pageResult == null || pageResult.getRecords() == null) {
      return;
    }
    Map<Long, SeedType> seedTypeMap =
        buildSeedTypeMap(
            pageResult.getRecords().stream().map(SeedGridVO::getId).collect(Collectors.toSet()));
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
    Map<Long, SeedType> seedTypeMap =
        buildSeedTypeMap(
            pageResult.getRecords().stream()
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

  private void applySeedInventoryDefaults(PageResult<SeedInventoryItemVO> pageResult) {
    if (pageResult == null || pageResult.getRecords() == null) {
      return;
    }
    Map<Long, SeedType> seedTypeMap =
        buildSeedTypeMap(
            pageResult.getRecords().stream()
                .map(SeedInventoryItemVO::getSeedTypeId)
                .collect(Collectors.toSet()));
    for (SeedInventoryItemVO row : pageResult.getRecords()) {
      if (row != null) {
        row.setCoverImageUrl(normalizeSeedCoverUrl(row.getCoverImageUrl()));
        row.setUnlockExperienceRequired(
            resolveUnlockExperienceRequired(seedTypeMap.get(row.getSeedTypeId())));
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
    return normalizeManagedAssetUrl(rawUrl, defaultSeedCoverUrl());
  }

  private String normalizeStageAssetUrl(String rawUrl) {
    return normalizeManagedAssetUrl(rawUrl, defaultSeedStageUrl());
  }

  private String normalizeManagedAssetUrl(String rawUrl, String defaultUrl) {
    String value = rawUrl == null ? "" : rawUrl.trim();
    if (value.isEmpty()) {
      return defaultUrl;
    }
    if (value.startsWith("http://")
        || value.startsWith("https://")
        || value.startsWith("/resources/")) {
      return value;
    }
    if (value.startsWith("resources/")) {
      return "/" + value;
    }
    String publicPrefix = filePublicPrefix();
    if (value.startsWith(publicPrefix + "/")) {
      return value;
    }
    if (value.startsWith("/")) {
      if (value.startsWith("/oss/")) {
        return publicPrefix + value.substring("/oss".length());
      }
      return value;
    }
    String relative = FileAccessPathUtils.normalizeIncomingRelativePath(value, publicPrefix);
    if (relative.startsWith("oss/")) {
      relative = relative.substring("oss/".length());
    }
    return publicPrefix + "/" + relative;
  }

  private String filePublicPrefix() {
    return FileAccessPathUtils.normalizePublicPrefix(fileStorageProperties.getPublicPrefix());
  }

  private Map<Long, SeedType> buildSeedTypeMap(Set<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return Map.of();
    }
    List<Long> idList = ids.stream().filter(id -> id != null && id > 0).distinct().toList();
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
    return normalizeUnlockExperienceRequired(
        seedType.getUnlockExperienceRequired(), seedType.getLevel());
  }

  private long normalizeUnlockExperienceRequired(Long unlockExperienceRequired, Short level) {
    if (unlockExperienceRequired != null && unlockExperienceRequired >= 0) {
      return unlockExperienceRequired;
    }
    int lv = level == null ? 1 : Math.max(level, (short) 1);
    if (lv == 1) {
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
    User user =
        userDao
            .findByIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> new ServiceException(BizErrorCode.USER_NOT_FOUND, "用户不存在"));
    SeedType seedType =
        seedTypeDao
            .findByIdAndIsDeletedFalse(seedTypeId)
            .orElseThrow(() -> new ServiceException(BizErrorCode.SEED_TYPE_NOT_FOUND, "种子类型不存在"));
    long unlockRequired = resolveUnlockExperienceRequired(seedType);
    long userExperience = user.getExperience() == null ? 0L : Math.max(user.getExperience(), 0L);
    if (userExperience < unlockRequired) {
      throw new ServiceException(
          BizErrorCode.EXPERIENCE_NOT_ENOUGH,
          "经验不足，尚未解锁该种子。需要经验 " + unlockRequired + "，当前经验 " + userExperience);
    }
  }

  private void validateStageAssetUrl(String assetUrl) {
    String value = assetUrl == null ? "" : assetUrl.trim();
    if (value.isEmpty()) {
      return;
    }
    String publicPrefix = filePublicPrefix();
    if (value.startsWith("/resources/")
        || value.startsWith(publicPrefix + "/")
        || value.startsWith("/oss/")) {
      return;
    }
    throw new ServiceException(
        BizErrorCode.SEED_ASSET_URL_INVALID, "资源路径必须位于 /resources 或 " + publicPrefix + " 下");
  }

  private void validateStageLayout(SeedStageAddOrUpdateDTO params) {
    if (params == null) {
      throw new ServiceException(BizErrorCode.PARAM_INVALID, "请求参数不能为空");
    }
    int width = params.getWidth() == null ? 0 : params.getWidth();
    int height = params.getHeight() == null ? 0 : params.getHeight();
    int offsetX = params.getOffsetX() == null ? 0 : params.getOffsetX();
    int offsetY = params.getOffsetY() == null ? 0 : params.getOffsetY();
    if (width < STAGE_WIDTH_MIN || width > STAGE_WIDTH_MAX) {
      throw new ServiceException(BizErrorCode.SEED_STAGE_LAYOUT_INVALID, "width 超出允许范围");
    }
    if (height < STAGE_HEIGHT_MIN || height > STAGE_HEIGHT_MAX) {
      throw new ServiceException(BizErrorCode.SEED_STAGE_LAYOUT_INVALID, "height 超出允许范围");
    }
    if (offsetX < STAGE_OFFSET_MIN || offsetX > STAGE_OFFSET_MAX) {
      throw new ServiceException(BizErrorCode.SEED_STAGE_LAYOUT_INVALID, "offsetX 超出允许范围");
    }
    if (offsetY < STAGE_OFFSET_MIN || offsetY > STAGE_OFFSET_MAX) {
      throw new ServiceException(BizErrorCode.SEED_STAGE_LAYOUT_INVALID, "offsetY 超出允许范围");
    }
    BigDecimal probability =
        params.getBugProbability() != null
            ? params.getBugProbability()
            : params.getPestProbability();
    if (probability != null
        && (probability.compareTo(BigDecimal.ZERO) < 0
            || probability.compareTo(BigDecimal.ONE) > 0)) {
      throw new ServiceException(BizErrorCode.SEED_STAGE_LAYOUT_INVALID, "虫害概率必须在 0 到 1 之间");
    }
  }

  private Map<Long, String> buildGrowthStageNameMap() {
    return growthStageDao.findByIsDeletedFalseOrderByIdAsc().stream()
        .collect(Collectors.toMap(GrowthStage::getId, item -> safeString(item.getName())));
  }

  private Short normalizeStagePointer(Short value) {
    return SeedStageConfigValidator.normalizeStagePointer(value);
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
    return SeedStageConfigValidator.normalizeMaxHarvestCount(value);
  }
}
