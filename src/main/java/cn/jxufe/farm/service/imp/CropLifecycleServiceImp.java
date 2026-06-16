package cn.jxufe.farm.service.imp;

import cn.jxufe.farm.bean.dto.CareCropDTO;
import cn.jxufe.farm.bean.dto.ClearCropDTO;
import cn.jxufe.farm.bean.dto.HarvestCropDTO;
import cn.jxufe.farm.bean.dto.MyFarmOverviewDTO;
import cn.jxufe.farm.bean.dto.MyPlantingPanelDTO;
import cn.jxufe.farm.bean.dto.PlantCropDTO;
import cn.jxufe.farm.bean.dto.SeedPlantablePlotsDTO;
import cn.jxufe.farm.bean.vo.CareResultVO;
import cn.jxufe.farm.bean.vo.ClearResultVO;
import cn.jxufe.farm.bean.vo.CropOverviewVO;
import cn.jxufe.farm.bean.vo.HarvestResultVO;
import cn.jxufe.farm.bean.vo.MyFarmOverviewVO;
import cn.jxufe.farm.bean.vo.MyPlantingPanelVO;
import cn.jxufe.farm.bean.vo.PlantResultVO;
import cn.jxufe.farm.bean.vo.PlantablePlotVO;
import cn.jxufe.farm.bean.vo.PlotOverviewVO;
import cn.jxufe.farm.bean.vo.SeedBackpackItemVO;
import cn.jxufe.farm.bean.vo.SeedPlantablePlotsVO;
import cn.jxufe.farm.common.constants.PlotRuleConstants;
import cn.jxufe.farm.common.enums.BizErrorCode;
import cn.jxufe.farm.common.enums.CropStatus;
import cn.jxufe.farm.common.exceptions.ServiceException;
import cn.jxufe.farm.common.utils.ServiceGuardUtils;
import cn.jxufe.farm.config.properties.GameplayPolicyProperties;
import cn.jxufe.farm.dao.PlotPolicyDao;
import cn.jxufe.farm.dao.SeedGrowthStageDao;
import cn.jxufe.farm.dao.SeedTypeDao;
import cn.jxufe.farm.dao.SoilTypeDao;
import cn.jxufe.farm.dao.UserAssetFlowDao;
import cn.jxufe.farm.dao.UserCropActionLogDao;
import cn.jxufe.farm.dao.UserCropDao;
import cn.jxufe.farm.dao.UserDao;
import cn.jxufe.farm.dao.UserFruitDao;
import cn.jxufe.farm.dao.UserInventoryFlowDao;
import cn.jxufe.farm.dao.UserPlotDao;
import cn.jxufe.farm.dao.UserSeedDao;
import cn.jxufe.farm.entity.SeedGrowthStage;
import cn.jxufe.farm.entity.SeedType;
import cn.jxufe.farm.entity.SoilType;
import cn.jxufe.farm.entity.User;
import cn.jxufe.farm.entity.UserCrop;
import cn.jxufe.farm.entity.UserFruit;
import cn.jxufe.farm.entity.UserPlot;
import cn.jxufe.farm.entity.UserSeed;
import cn.jxufe.farm.service.CropLifecycleService;
import cn.jxufe.farm.service.GameplayCoreService;
import cn.jxufe.farm.service.PlotCostService;
import cn.jxufe.farm.service.support.SeedStageRuleSupport;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CropLifecycleServiceImp implements CropLifecycleService {

  private final UserDao userDao;

  private final UserPlotDao userPlotDao;

  private final UserCropDao userCropDao;

  private final UserSeedDao userSeedDao;

  private final UserFruitDao userFruitDao;

  private final SeedTypeDao seedTypeDao;

  private final SoilTypeDao soilTypeDao;

  private final PlotPolicyDao plotPolicyDao;

  private final SeedGrowthStageDao seedGrowthStageDao;

  private final UserInventoryFlowDao userInventoryFlowDao;

  private final UserAssetFlowDao userAssetFlowDao;

  private final UserCropActionLogDao userCropActionLogDao;

  private final GameplayCoreService gameplayCoreService;

  private final PlotCostService plotCostService;

  private final GameplayPolicyProperties gameplayPolicyProperties;

  public CropLifecycleServiceImp(
      UserDao userDao,
      UserPlotDao userPlotDao,
      UserCropDao userCropDao,
      UserSeedDao userSeedDao,
      UserFruitDao userFruitDao,
      SeedTypeDao seedTypeDao,
      SoilTypeDao soilTypeDao,
      PlotPolicyDao plotPolicyDao,
      SeedGrowthStageDao seedGrowthStageDao,
      UserInventoryFlowDao userInventoryFlowDao,
      UserAssetFlowDao userAssetFlowDao,
      UserCropActionLogDao userCropActionLogDao,
      GameplayCoreService gameplayCoreService,
      PlotCostService plotCostService,
      GameplayPolicyProperties gameplayPolicyProperties) {
    this.userDao = userDao;
    this.userPlotDao = userPlotDao;
    this.userCropDao = userCropDao;
    this.userSeedDao = userSeedDao;
    this.userFruitDao = userFruitDao;
    this.seedTypeDao = seedTypeDao;
    this.soilTypeDao = soilTypeDao;
    this.plotPolicyDao = plotPolicyDao;
    this.seedGrowthStageDao = seedGrowthStageDao;
    this.userInventoryFlowDao = userInventoryFlowDao;
    this.userAssetFlowDao = userAssetFlowDao;
    this.userCropActionLogDao = userCropActionLogDao;
    this.gameplayCoreService = gameplayCoreService;
    this.plotCostService = plotCostService;
    this.gameplayPolicyProperties = gameplayPolicyProperties;
  }

  /* =========================================================
   *  Context Helpers
   * ========================================================= */

  private void requireNotNull(Object params) {
    ServiceGuardUtils.requireNotNull(
        params, BizErrorCode.PARAM_INVALID, "Request params must not be null");
  }

  /** 封装单次作物操作所需的用户、地块和作物上下文，并在构造阶段完成基础校验。 */
  private class CropActionContext {
    final Long userId;
    final Long plotId;
    final UserPlot plot;
    final UserCrop crop;

    CropActionContext(Long rawUserId, Long rawPlotId, boolean requireCropExists) {
      this.userId =
          ServiceGuardUtils.requirePositive(
              rawUserId, BizErrorCode.PARAM_INVALID, "User ID is invalid");
      this.plotId =
          ServiceGuardUtils.requirePositive(
              rawPlotId, BizErrorCode.PARAM_INVALID, "Plot ID is invalid");
      validateUser(this.userId);
      this.plot = getAndValidateUnlockedPlot(this.userId, this.plotId);

      if (requireCropExists) {
        this.crop = getAndValidateCrop(this.userId, this.plotId);
      } else {
        if (userCropDao
            .findByUserIdAndPlotIdAndIsDeletedFalse(this.userId, this.plotId)
            .isPresent()) {
          throw new ServiceException(BizErrorCode.PLOT_ALREADY_HAS_CROP, "Plot already has crop");
        }
        this.crop = null;
      }
    }
  }

  /** 封装农场视图渲染所需的地块、作物、阶段和用户资产上下文。 */
  private class FarmViewContext {
    final Long userId;
    final OffsetDateTime now;
    final long currentExperience;
    final long currentCoin;
    final List<UserPlot> plots;
    final Map<Long, UserCrop> cropByPlotId;
    final Map<Long, List<SeedGrowthStage>> stagesBySeedTypeId;

    FarmViewContext(Long rawUserId) {
      this.userId =
          ServiceGuardUtils.requirePositive(
              rawUserId, BizErrorCode.PARAM_INVALID, "User ID is invalid");
      User user = validateUser(this.userId);
      this.currentExperience = safeLong(user.getExperience());
      this.currentCoin = safeLong(user.getCoin());
      this.now = OffsetDateTime.now();
      this.plots = userPlotDao.findByUserIdAndIsDeletedFalseOrderByPlotIndexAsc(this.userId);
      this.cropByPlotId =
          userCropDao.findByUserIdAndIsDeletedFalseOrderByIdAsc(this.userId).stream()
              .collect(Collectors.toMap(UserCrop::getPlotId, Function.identity()));
      List<Long> seedTypeIds =
          this.cropByPlotId.values().stream()
              .map(UserCrop::getSeedTypeId)
              .filter(id -> id != null && id > 0)
              .distinct()
              .toList();
      this.stagesBySeedTypeId =
          seedTypeIds.isEmpty()
              ? Map.of()
              : seedGrowthStageDao
                  .findBySeedTypeIdInAndIsDeletedFalseOrderBySeedTypeIdAscStageIndexAsc(seedTypeIds)
                  .stream()
                  .collect(
                      Collectors.groupingBy(
                          SeedGrowthStage::getSeedTypeId, LinkedHashMap::new, Collectors.toList()));
    }
  }

  /* =========================================================
   *  Public Methods
   * ========================================================= */

  @Override
  @Transactional
  public PlantResultVO plant(PlantCropDTO params) {
    requireNotNull(params);
    Long seedTypeId =
        ServiceGuardUtils.requirePositive(
            params.getSeedTypeId(), BizErrorCode.PARAM_INVALID, "Param seedTypeId is invalid");
    CropActionContext ctx = new CropActionContext(params.getUserId(), params.getPlotId(), false);

    SeedType seedType =
        ServiceGuardUtils.requirePresent(
            seedTypeDao.findByIdAndIsDeletedFalse(seedTypeId),
            BizErrorCode.SEED_TYPE_NOT_FOUND,
            "Seed type not found");
    SoilType soilType =
        ServiceGuardUtils.requirePresent(
            soilTypeDao.findByIdAndIsDeletedFalse(ctx.plot.getSoilTypeId()),
            BizErrorCode.SOIL_TYPE_NOT_FOUND,
            "Soil type not found");

    if (isSoilIncompatible(seedType, soilType)) {
      throw new ServiceException(
          BizErrorCode.SOIL_NOT_COMPATIBLE, "Seed type is not compatible with soil type");
    }

    UserSeed userSeed = getAndValidateUserSeed(ctx.userId, seedTypeId);
    long beforeSeedAmount = safeLong(userSeed.getQuantity());
    long beforeFrozenSeedAmount = safeLong(userSeed.getFrozenQuantity());
    if (beforeSeedAmount - beforeFrozenSeedAmount <= 0) {
      throw new ServiceException(BizErrorCode.SEED_NOT_ENOUGH, "Seed inventory is not enough");
    }

    List<SeedGrowthStage> stages =
        seedGrowthStageDao.findBySeedTypeIdAndIsDeletedFalseOrderByStageIndexAsc(seedTypeId);
    if (stages.isEmpty()) {
      throw new ServiceException(
          BizErrorCode.SEED_GROWTH_STAGE_NOT_CONFIGURED, "Seed growth stages are not configured");
    }

    OffsetDateTime now = OffsetDateTime.now();
    BigDecimal multiplier = normalizeMultiplier(soilType.getGrowSpeedMultiplier());
    SeedStageRuleSupport.ResolvedStageRule stageRule = resolveStageRule(seedType, stages);
    int safeGrowSeconds =
        Math.max(
            calculateSecondsUntilStage(
                stages, stageRule.firstStageIndex(), stageRule.harvestStageIndex(), multiplier),
            0);
    int witherAtSeconds =
        Math.max(
            calculateSecondsUntilStage(
                stages, stageRule.firstStageIndex(), stageRule.witherStageIndex(), multiplier),
            safeGrowSeconds);

    if (userSeedDao.decreaseAvailableQuantityIfEnough(userSeed.getId(), 1L, ctx.userId, now) <= 0) {
      throw new ServiceException(BizErrorCode.SEED_NOT_ENOUGH, "Seed inventory is not enough");
    }

    long afterSeedAmount = safeLong(getAndValidateUserSeed(ctx.userId, seedTypeId).getQuantity());

    UserCrop crop = new UserCrop();
    gameplayCoreService.initNewEntity(crop, ctx.userId, now);
    crop.setUserId(ctx.userId);
    crop.setPlotId(ctx.plotId);
    crop.setSeedTypeId(seedTypeId);
    crop.setPlantedAt(now);
    crop.setStageStartedAt(now);
    crop.setCurrentStageIndex(stageRule.firstStageIndex());
    crop.setHarvestCount((short) 0);
    crop.setBugCount((short) 0);
    crop.setGrowStatus(
        safeGrowSeconds == 0 ? CropStatus.RIPE.getCode() : CropStatus.GROWING.getCode());
    crop.setMaturedAt(safeGrowSeconds == 0 ? now : null);
    crop.setExpectedRipeAt(now.plusSeconds(safeGrowSeconds));
    crop.setExpectedWitheredAt(now.plusSeconds(witherAtSeconds));
    UserCrop savedCrop = userCropDao.save(crop);

    String bizId = "PLANT:" + savedCrop.getId() + ":" + now.toEpochSecond();
    userInventoryFlowDao.save(
        gameplayCoreService.buildInventoryFlow(
            ctx.userId,
            "SEED",
            seedTypeId,
            "EXPENSE",
            1L,
            beforeSeedAmount,
            afterSeedAmount,
            beforeFrozenSeedAmount,
            beforeFrozenSeedAmount,
            "PLANT",
            bizId,
            now,
            "{\"plotId\":" + ctx.plotId + ",\"cropId\":" + savedCrop.getId() + "}"));

    userCropActionLogDao.save(
        gameplayCoreService.buildCropActionLog(
            ctx.userId,
            ctx.plotId,
            savedCrop.getId(),
            seedTypeId,
            "PLANT",
            "SUCCESS",
            now,
            "{\"seedTypeId\":" + seedTypeId + ",\"remainSeed\":" + afterSeedAmount + "}"));

    PlantResultVO result = new PlantResultVO();
    result.setUserId(ctx.userId);
    result.setPlotId(ctx.plotId);
    result.setCropId(savedCrop.getId());
    result.setSeedTypeId(seedTypeId);
    result.setRemainSeedQuantity(afterSeedAmount);
    result.setGrowStatus(savedCrop.getGrowStatus());
    result.setCurrentStageIndex(savedCrop.getCurrentStageIndex());
    result.setExpectedRipeAt(savedCrop.getExpectedRipeAt());
    result.setExpectedWitheredAt(savedCrop.getExpectedWitheredAt());
    return result;
  }

  @Override
  @Transactional
  public HarvestResultVO harvest(HarvestCropDTO params) {
    requireNotNull(params);
    CropActionContext ctx = new CropActionContext(params.getUserId(), params.getPlotId(), true);

    if (ctx.crop == null) {
      throw new ServiceException(BizErrorCode.CROP_NOT_FOUND, "Crop not found");
    }

    SeedType seedType =
        ServiceGuardUtils.requirePresent(
            seedTypeDao.findByIdAndIsDeletedFalse(ctx.crop.getSeedTypeId()),
            BizErrorCode.SEED_TYPE_NOT_FOUND,
            "Seed type for crop not found");

    OffsetDateTime now = OffsetDateTime.now();
    syncCropStatus(ctx.crop, now);

    if (CropStatus.isWithered(ctx.crop.getGrowStatus())) {
      saveHarvestFailLog(
          ctx.userId,
          ctx.plotId,
          ctx.crop,
          now,
          "WITHERED",
          BizErrorCode.CROP_WITHERED,
          "Crop is withered");
    }
    if (!CropStatus.isRipe(ctx.crop.getGrowStatus())) {
      saveHarvestFailLog(
          ctx.userId,
          ctx.plotId,
          ctx.crop,
          now,
          "NOT_RIPE",
          BizErrorCode.CROP_NOT_RIPE,
          "Crop is not ripe");
    }

    long baseFruitGain = Math.max(0L, safeInteger(seedType.getHarvestFruitNumber()));
    short bugCountBefore = safeShort(ctx.crop.getBugCount());
    long bugPenaltyPerBug =
        Math.max(0L, seedType.getFruitLossPerBug() == null ? 1 : seedType.getFruitLossPerBug());
    long totalBugPenaltyFruit = gameplayCoreService.safeMultiply(bugCountBefore, bugPenaltyPerBug);
    long fruitGain = Math.max(0L, baseFruitGain - totalBugPenaltyFruit);
    long expGain = Math.max(0L, safeLong(seedType.getHarvestExperience()));
    long scoreGain = Math.max(0L, safeLong(seedType.getHarvestScore()));
    UserFruit userFruit =
        userFruitDao
            .findByUserIdAndSeedTypeIdAndIsDeletedFalse(ctx.userId, ctx.crop.getSeedTypeId())
            .orElse(null);
    long beforeFrozenFruitAmount;
    long afterFruitAmount;
    if (userFruit == null) {
      UserFruit entity =
          gameplayCoreService.createUserFruit(ctx.userId, ctx.crop.getSeedTypeId(), now);
      entity.setQuantity(fruitGain);
      UserFruit savedFruit = userFruitDao.save(entity);
      beforeFrozenFruitAmount = 0L;
      afterFruitAmount = safeLong(savedFruit.getQuantity());
    } else {
      beforeFrozenFruitAmount = safeLong(userFruit.getFrozenQuantity());
      if (userFruitDao.increaseQuantity(userFruit.getId(), fruitGain, ctx.userId, now) <= 0) {
        throw new ServiceException(
            BizErrorCode.FRUIT_INVENTORY_NOT_FOUND, "Fruit inventory not found");
      }
      UserFruit latestUserFruit =
          ServiceGuardUtils.requirePresent(
              userFruitDao.findByUserIdAndSeedTypeIdAndIsDeletedFalse(
                  ctx.userId, ctx.crop.getSeedTypeId()),
              BizErrorCode.FRUIT_INVENTORY_NOT_FOUND,
              "Fruit inventory not found");
      afterFruitAmount = safeLong(latestUserFruit.getQuantity());
    }
    if (userDao.increaseExperienceAndScore(ctx.userId, expGain, scoreGain, ctx.userId, now) <= 0) {
      throw new ServiceException(BizErrorCode.USER_NOT_FOUND, "User not found");
    }

    User latestUser = validateUser(ctx.userId);

    short maxHarvestCount =
        safeShort(seedType.getMaxHarvestCount()) <= 0
            ? 1
            : safeShort(seedType.getMaxHarvestCount());
    short nextHarvestCount = (short) (safeShort(ctx.crop.getHarvestCount()) + 1);
    ctx.crop.setHarvestCount(nextHarvestCount);
    ctx.crop.setLastHarvestAt(now);

    boolean cropCleared = nextHarvestCount >= maxHarvestCount;
    Short nextGrowStatus = null;
    Short nextStageIndex = null;
    OffsetDateTime nextExpectedRipeAt = null;
    OffsetDateTime nextExpectedWitheredAt = null;

    if (cropCleared) {
      ctx.crop.setIsDeleted(true);
    } else {
      List<SeedGrowthStage> stages =
          seedGrowthStageDao.findBySeedTypeIdAndIsDeletedFalseOrderByStageIndexAsc(
              seedType.getId());
      if (stages.isEmpty()) {
        throw new ServiceException(
            BizErrorCode.SEED_GROWTH_STAGE_NOT_CONFIGURED, "Seed growth stages are not configured");
      }

      SoilType soilType =
          soilTypeDao.findByIdAndIsDeletedFalse(ctx.plot.getSoilTypeId()).orElse(null);
      BigDecimal multiplier =
          soilType == null
              ? BigDecimal.ONE
              : normalizeMultiplier(soilType.getGrowSpeedMultiplier());
      SeedStageRuleSupport.ResolvedStageRule stageRule = resolveStageRule(seedType, stages);
      short regrowStageIndex = stageRule.regrowStageIndex();

      int safeRegrowSeconds =
          Math.max(
              calculateSecondsUntilStage(
                  stages, regrowStageIndex, stageRule.harvestStageIndex(), multiplier),
              0);
      int witherAtSeconds =
          Math.max(
              calculateSecondsUntilStage(
                  stages, regrowStageIndex, stageRule.witherStageIndex(), multiplier),
              safeRegrowSeconds);

      nextExpectedRipeAt = now.plusSeconds(safeRegrowSeconds);
      nextExpectedWitheredAt = now.plusSeconds(witherAtSeconds);
      nextGrowStatus =
          safeRegrowSeconds == 0 ? CropStatus.RIPE.getCode() : CropStatus.GROWING.getCode();
      nextStageIndex = regrowStageIndex;

      ctx.crop.setStageStartedAt(now);
      ctx.crop.setCurrentStageIndex(regrowStageIndex);
      ctx.crop.setGrowStatus(nextGrowStatus);
      ctx.crop.setMaturedAt(safeRegrowSeconds == 0 ? now : null);
      ctx.crop.setWitheredAt(null);
      ctx.crop.setExpectedRipeAt(nextExpectedRipeAt);
      ctx.crop.setExpectedWitheredAt(nextExpectedWitheredAt);
      ctx.crop.setBugCount((short) 0);
      ctx.crop.setLastBugAt(null);
      ctx.crop.setLastCareAt(null);
    }
    gameplayCoreService.touchForUpdate(ctx.crop, ctx.userId, now);
    userCropDao.save(ctx.crop);

    String bizId = "HARVEST:" + ctx.crop.getId() + ":" + now.toEpochSecond();
    userInventoryFlowDao.save(
        gameplayCoreService.buildInventoryFlow(
            ctx.userId,
            "FRUIT",
            ctx.crop.getSeedTypeId(),
            "INCOME",
            fruitGain,
            afterFruitAmount - fruitGain,
            afterFruitAmount,
            beforeFrozenFruitAmount,
            beforeFrozenFruitAmount,
            "HARVEST",
            bizId,
            now,
            "{\"plotId\":" + ctx.plotId + ",\"cropId\":" + ctx.crop.getId() + "}"));

    userCropActionLogDao.save(
        gameplayCoreService.buildCropActionLog(
            ctx.userId,
            ctx.plotId,
            ctx.crop.getId(),
            ctx.crop.getSeedTypeId(),
            "HARVEST",
            "SUCCESS",
            now,
            "{\"baseFruitGain\":"
                + baseFruitGain
                + ",\"bugCountBefore\":"
                + bugCountBefore
                + ",\"bugPenaltyPerBug\":"
                + bugPenaltyPerBug
                + ",\"totalBugPenaltyFruit\":"
                + totalBugPenaltyFruit
                + ",\"fruitGain\":"
                + fruitGain
                + ",\"cropCleared\":"
                + cropCleared
                + "}"));

    HarvestResultVO result = new HarvestResultVO();
    result.setUserId(ctx.userId);
    result.setPlotId(ctx.plotId);
    result.setCropId(ctx.crop.getId());
    result.setSeedTypeId(ctx.crop.getSeedTypeId());
    result.setBaseHarvestFruitNumber(baseFruitGain);
    result.setBugPenaltyPerBug(bugPenaltyPerBug);
    result.setTotalBugPenaltyFruit(totalBugPenaltyFruit);
    result.setHarvestFruitNumber(fruitGain);
    result.setTotalFruitQuantity(afterFruitAmount);
    result.setExperienceGain(expGain);
    result.setScoreGain(scoreGain);
    result.setCurrentExperience(safeLong(latestUser.getExperience()));
    result.setCurrentScore(safeLong(latestUser.getScore()));
    result.setBugCountBefore(bugCountBefore);
    result.setBugCountAfter((short) 0);
    result.setCropCleared(cropCleared);
    result.setNextGrowStatus(nextGrowStatus);
    result.setNextStageIndex(nextStageIndex);
    result.setNextExpectedRipeAt(nextExpectedRipeAt);
    result.setNextExpectedWitheredAt(nextExpectedWitheredAt);
    return result;
  }

  @Override
  @Transactional
  public ClearResultVO clear(ClearCropDTO params) {
    requireNotNull(params);
    CropActionContext ctx = new CropActionContext(params.getUserId(), params.getPlotId(), true);

    if (ctx.crop == null) {
      throw new ServiceException(BizErrorCode.CROP_NOT_FOUND, "Crop not found");
    }

    OffsetDateTime now = OffsetDateTime.now();
    syncCropStatus(ctx.crop, now);

    Short growStatusBefore = ctx.crop.getGrowStatus();
    Short stageIndexBefore = ctx.crop.getCurrentStageIndex();
    Short bugCountBefore = ctx.crop.getBugCount();
    Long seedTypeId = ctx.crop.getSeedTypeId();
    Long cropId = ctx.crop.getId();

    ctx.crop.setIsDeleted(true);
    gameplayCoreService.touchForUpdate(ctx.crop, ctx.userId, now);
    userCropDao.save(ctx.crop);

    userCropActionLogDao.save(
        gameplayCoreService.buildCropActionLog(
            ctx.userId,
            ctx.plotId,
            cropId,
            seedTypeId,
            "CLEAR",
            "SUCCESS",
            now,
            "{\"growStatusBefore\":"
                + safeShort(growStatusBefore)
                + ",\"stageIndexBefore\":"
                + safeShort(stageIndexBefore)
                + ",\"bugCountBefore\":"
                + safeShort(bugCountBefore)
                + "}"));

    ClearResultVO result = new ClearResultVO();
    result.setUserId(ctx.userId);
    result.setPlotId(ctx.plotId);
    result.setCropId(cropId);
    result.setSeedTypeId(seedTypeId);
    result.setGrowStatusBefore(growStatusBefore);
    result.setStageIndexBefore(stageIndexBefore);
    result.setBugCountBefore(bugCountBefore);
    result.setCleared(true);
    result.setClearedAt(now);
    return result;
  }

  @Override
  @Transactional
  public CareResultVO care(CareCropDTO params) {
    requireNotNull(params);
    CropActionContext ctx = new CropActionContext(params.getUserId(), params.getPlotId(), true);

    if (ctx.crop == null) {
      throw new ServiceException(BizErrorCode.CROP_NOT_FOUND, "Crop not found");
    }

    SeedType seedType =
        ServiceGuardUtils.requirePresent(
            seedTypeDao.findByIdAndIsDeletedFalse(ctx.crop.getSeedTypeId()),
            BizErrorCode.SEED_TYPE_NOT_FOUND,
            "Seed type for crop not found");

    OffsetDateTime now = OffsetDateTime.now();
    syncCropStatus(ctx.crop, now);

    short bugCountBefore = safeShort(ctx.crop.getBugCount());
    if (CropStatus.isWithered(ctx.crop.getGrowStatus())) {
      userCropActionLogDao.save(
          gameplayCoreService.buildCropActionLog(
              ctx.userId,
              ctx.plotId,
              ctx.crop.getId(),
              ctx.crop.getSeedTypeId(),
              "CARE",
              "FAIL",
              now,
              "{\"reason\":\"WITHERED\"}"));
      throw new ServiceException(BizErrorCode.CROP_WITHERED, "Crop is withered");
    }

    short bugRemovedCount = (short) (bugCountBefore > 0 ? 1 : 0);
    short bugCountAfter = (short) Math.max(bugCountBefore - bugRemovedCount, 0);
    long coinGain =
        bugRemovedCount > 0 ? Math.max(0L, safeLong(seedType.getBugKillCoinReward())) : 0L;
    long experienceGain =
        bugRemovedCount > 0 ? Math.max(0L, safeLong(seedType.getBugKillExperienceReward())) : 0L;
    long scoreGain =
        bugRemovedCount > 0 ? Math.max(0L, safeLong(seedType.getBugKillScoreReward())) : 0L;

    User beforeUser = validateUser(ctx.userId);
    long beforeCoin = safeLong(beforeUser.getCoin());
    long beforeExperience = safeLong(beforeUser.getExperience());
    long beforeScore = safeLong(beforeUser.getScore());

    ctx.crop.setBugCount(bugCountAfter);
    ctx.crop.setLastCareAt(now);
    gameplayCoreService.touchForUpdate(ctx.crop, ctx.userId, now);
    userCropDao.save(ctx.crop);

    if (coinGain > 0 && userDao.increaseCoin(ctx.userId, coinGain, ctx.userId, now) <= 0) {
      throw new ServiceException(BizErrorCode.USER_NOT_FOUND, "User not found");
    }
    if ((experienceGain > 0 || scoreGain > 0)
        && userDao.increaseExperienceAndScore(
                ctx.userId, experienceGain, scoreGain, ctx.userId, now)
            <= 0) {
      throw new ServiceException(BizErrorCode.USER_NOT_FOUND, "User not found");
    }

    User afterUser =
        (coinGain > 0 || experienceGain > 0 || scoreGain > 0)
            ? validateUser(ctx.userId)
            : beforeUser;
    long afterCoin = safeLong(afterUser.getCoin());
    long afterExperience = safeLong(afterUser.getExperience());
    long afterScore = safeLong(afterUser.getScore());

    String bizId = "CARE:" + ctx.crop.getId() + ":" + now.toEpochSecond();
    if (coinGain > 0) {
      userAssetFlowDao.save(
          gameplayCoreService.buildAssetFlow(
              ctx.userId,
              "COIN",
              "INCOME",
              coinGain,
              beforeCoin,
              afterCoin,
              "CARE",
              bizId,
              now,
              "{\"plotId\":"
                  + ctx.plotId
                  + ",\"cropId\":"
                  + ctx.crop.getId()
                  + ",\"bugRemoved\":"
                  + bugRemovedCount
                  + "}"));
    }
    if (experienceGain > 0) {
      userAssetFlowDao.save(
          gameplayCoreService.buildAssetFlow(
              ctx.userId,
              "EXPERIENCE",
              "INCOME",
              experienceGain,
              beforeExperience,
              afterExperience,
              "CARE",
              bizId,
              now,
              "{\"plotId\":"
                  + ctx.plotId
                  + ",\"cropId\":"
                  + ctx.crop.getId()
                  + ",\"bugRemoved\":"
                  + bugRemovedCount
                  + "}"));
    }
    if (scoreGain > 0) {
      userAssetFlowDao.save(
          gameplayCoreService.buildAssetFlow(
              ctx.userId,
              "SCORE",
              "INCOME",
              scoreGain,
              beforeScore,
              afterScore,
              "CARE",
              bizId,
              now,
              "{\"plotId\":"
                  + ctx.plotId
                  + ",\"cropId\":"
                  + ctx.crop.getId()
                  + ",\"bugRemoved\":"
                  + bugRemovedCount
                  + "}"));
    }

    userCropActionLogDao.save(
        gameplayCoreService.buildCropActionLog(
            ctx.userId,
            ctx.plotId,
            ctx.crop.getId(),
            ctx.crop.getSeedTypeId(),
            "CARE",
            "SUCCESS",
            now,
            "{\"bugCountBefore\":"
                + bugCountBefore
                + ",\"bugCountAfter\":"
                + bugCountAfter
                + ",\"bugRemovedCount\":"
                + bugRemovedCount
                + ",\"coinGain\":"
                + coinGain
                + ",\"experienceGain\":"
                + experienceGain
                + ",\"scoreGain\":"
                + scoreGain
                + "}"));

    CareResultVO result = new CareResultVO();
    result.setUserId(ctx.userId);
    result.setPlotId(ctx.plotId);
    result.setCropId(ctx.crop.getId());
    result.setSeedTypeId(ctx.crop.getSeedTypeId());
    result.setBugCountBefore(bugCountBefore);
    result.setBugCountAfter(bugCountAfter);
    result.setBugRemovedCount(bugRemovedCount);
    result.setCoinGain(coinGain);
    result.setExperienceGain(experienceGain);
    result.setScoreGain(scoreGain);
    result.setCurrentCoin(afterCoin);
    result.setCurrentExperience(afterExperience);
    result.setCurrentScore(afterScore);
    result.setCurrentStageIndex(ctx.crop.getCurrentStageIndex());
    result.setGrowStatus(ctx.crop.getGrowStatus());
    result.setLastCareAt(ctx.crop.getLastCareAt());
    return result;
  }

  @Override
  public MyFarmOverviewVO myFarmOverview(MyFarmOverviewDTO params) {
    FarmViewContext ctx = new FarmViewContext(params == null ? null : params.getUserId());

    UserPlot nextUnlockPlot = ctx.plots.stream().filter(this::isLocked).findFirst().orElse(null);
    Map<Long, SoilType> soilTypeMap = getSoilTypeMap();
    Map<Long, SeedType> seedTypeMap = getSeedTypeMap();
    String lockSource = resolvePlotLockSource(ctx.userId);
    String lockRuleCode = resolvePlotLockRuleCode(lockSource);

    List<PlotOverviewVO> plotVOs = new ArrayList<>();
    int unlockedPlots = 0, lockedPlots = 0, occupiedPlots = 0, harvestableCount = 0;

    for (UserPlot plot : ctx.plots) {
      PlotOverviewVO plotVO = new PlotOverviewVO();
      plotVO.setPlotId(plot.getId());
      plotVO.setUserId(plot.getUserId());
      plotVO.setPlotIndex(plot.getPlotIndex());
      plotVO.setLocked(isLocked(plot));
      plotVO.setLockReason(gameplayCoreService.safeString(plot.getLockReason()));
      plotVO.setLockSource(lockSource);
      plotVO.setLockRuleCode(lockRuleCode);
      plotVO.setSoilTypeId(plot.getSoilTypeId());

      SoilType soilType = soilTypeMap.get(plot.getSoilTypeId());
      plotVO.setSoilBitCode(soilType == null ? null : soilType.getBitCode());
      plotVO.setSoilName(
          soilType == null ? "" : gameplayCoreService.safeString(soilType.getName()));
      plotVO.setSoilCoverImageUrl(
          soilType == null ? "" : gameplayCoreService.safeString(soilType.getCoverImageUrl()));
      long unlockCostCoin = plotCostService.calculateUnlockCostCoin(plot.getPlotIndex());
      plotVO.setUnlockCostCoin(unlockCostCoin);
      long unlockRequiredExperience = safeLong(plot.getUnlockExperienceRequired());
      boolean unlockableByExperience = ctx.currentExperience >= unlockRequiredExperience;
      boolean unlockableByCoin = ctx.currentCoin >= unlockCostCoin;
      plotVO.setUnlockRequiredExperience(unlockRequiredExperience);
      plotVO.setUnlockableByExperience(unlockableByExperience);
      plotVO.setUnlockableByCoin(unlockableByCoin);
      plotVO.setCanUnlock(
          nextUnlockPlot != null
              && nextUnlockPlot.getId().equals(plot.getId())
              && unlockableByExperience
              && unlockableByCoin);

      UserCrop crop = ctx.cropByPlotId.get(plot.getId());
      boolean hasCrop = crop != null && !isDeleted(crop);

      plotVO.setHasCrop(hasCrop);
      plotVO.setOccupied(hasCrop && isUnlocked(plot));
      plotVO.setPlantable(isUnlocked(plot) && !hasCrop);

      if (isLocked(plot)) {
        lockedPlots++;
      } else {
        unlockedPlots++;
      }

      if (hasCrop && isUnlocked(plot)) {
        occupiedPlots++;
        CropOverviewVO cropVO =
            buildCropOverview(
                crop,
                seedTypeMap.get(crop.getSeedTypeId()),
                ctx.stagesBySeedTypeId.getOrDefault(crop.getSeedTypeId(), List.of()),
                ctx.now);
        plotVO.setCrop(cropVO);
        if (Boolean.TRUE.equals(cropVO.getHarvestable())) {
          harvestableCount++;
        }
      } else {
        plotVO.setCrop(null);
      }
      plotVOs.add(plotVO);
    }

    MyFarmOverviewVO result = new MyFarmOverviewVO();
    result.setUserId(ctx.userId);
    result.setServerTime(ctx.now);
    result.setTotalPlots(ctx.plots.size());
    result.setUnlockedPlots(unlockedPlots);
    result.setLockedPlots(lockedPlots);
    result.setOccupiedPlots(occupiedPlots);
    result.setEmptyUnlockedPlots(Math.max(unlockedPlots - occupiedPlots, 0));
    result.setHarvestableCount(harvestableCount);
    result.setNextExpandCostCoin(calculateExpandCostCoin(ctx.plots.size()));
    result.setPlots(plotVOs);
    return result;
  }

  @Override
  public MyPlantingPanelVO myPlantingPanel(MyPlantingPanelDTO params) {
    FarmViewContext ctx = new FarmViewContext(params == null ? null : params.getUserId());
    List<UserSeed> userSeeds = userSeedDao.findByUserIdAndIsDeletedFalseOrderByIdAsc(ctx.userId);
    Map<Long, SeedType> seedTypeMap = getSeedTypeMap();

    int unlockedPlotCount = (int) ctx.plots.stream().filter(this::isUnlocked).count();
    int plantablePlotCount =
        (int)
            ctx.plots.stream()
                .filter(plot -> isUnlocked(plot) && !ctx.cropByPlotId.containsKey(plot.getId()))
                .count();

    List<SeedBackpackItemVO> seeds = new ArrayList<>();
    int selectableSeedTypeCount = 0;

    for (UserSeed userSeed : userSeeds) {
      SeedType seedType = seedTypeMap.get(userSeed.getSeedTypeId());
      if (seedType == null) {
        continue;
      }

      long availableQuantity =
          Math.max(safeLong(userSeed.getQuantity()) - safeLong(userSeed.getFrozenQuantity()), 0L);
      boolean selectable = availableQuantity > 0 && plantablePlotCount > 0;
      if (selectable) {
        selectableSeedTypeCount++;
      }

      SeedBackpackItemVO itemVO = new SeedBackpackItemVO();
      itemVO.setUserSeedId(userSeed.getId());
      itemVO.setSeedTypeId(userSeed.getSeedTypeId());
      itemVO.setSeedTypeName(gameplayCoreService.safeString(seedType.getName()));
      itemVO.setQuantity(safeLong(userSeed.getQuantity()));
      itemVO.setFrozenQuantity(safeLong(userSeed.getFrozenQuantity()));
      itemVO.setAvailableQuantity(availableQuantity);
      itemVO.setSelectable(selectable);
      seeds.add(itemVO);
    }

    MyPlantingPanelVO result = new MyPlantingPanelVO();
    result.setUserId(ctx.userId);
    result.setServerTime(ctx.now);
    result.setTotalPlotCount(ctx.plots.size());
    result.setUnlockedPlotCount(unlockedPlotCount);
    result.setPlantablePlotCount(plantablePlotCount);
    result.setBackpackSeedTypeCount(seeds.size());
    result.setSelectableSeedTypeCount(selectableSeedTypeCount);
    result.setSeeds(seeds);
    return result;
  }

  @Override
  public SeedPlantablePlotsVO seedPlantablePlots(SeedPlantablePlotsDTO params) {
    requireNotNull(params);
    Long seedTypeId =
        ServiceGuardUtils.requirePositive(
            params.getSeedTypeId(), BizErrorCode.PARAM_INVALID, "Param seedTypeId is invalid");
    FarmViewContext ctx = new FarmViewContext(params.getUserId());

    SeedType seedType =
        ServiceGuardUtils.requirePresent(
            seedTypeDao.findByIdAndIsDeletedFalse(seedTypeId),
            BizErrorCode.SEED_TYPE_NOT_FOUND,
            "Seed type not found");
    Map<Long, SoilType> soilTypeMap = getSoilTypeMap();

    List<PlantablePlotVO> list =
        ctx.plots.stream()
            .filter(this::isUnlocked)
            .filter(plot -> !ctx.cropByPlotId.containsKey(plot.getId()))
            .filter(plot -> !isSoilIncompatible(seedType, soilTypeMap.get(plot.getSoilTypeId())))
            .map(
                plot -> {
                  SoilType soilType = soilTypeMap.get(plot.getSoilTypeId());
                  PlantablePlotVO plotVO = new PlantablePlotVO();
                  plotVO.setPlotId(plot.getId());
                  plotVO.setPlotIndex(plot.getPlotIndex());
                  plotVO.setSoilTypeId(plot.getSoilTypeId());
                  plotVO.setSoilBitCode(soilType.getBitCode());
                  plotVO.setSoilName(gameplayCoreService.safeString(soilType.getName()));
                  return plotVO;
                })
            .collect(Collectors.toList());

    SeedPlantablePlotsVO result = new SeedPlantablePlotsVO();
    result.setUserId(ctx.userId);
    result.setSeedTypeId(seedTypeId);
    result.setServerTime(ctx.now);
    result.setPlantableCount(list.size());
    result.setPlots(list);
    return result;
  }

  /* =========================================================
   *  Private Core Logic Helpers
   * ========================================================= */
  // Core helper methods for user, plot, crop, stage and reward validation live below.

  private User validateUser(Long userId) {
    return ServiceGuardUtils.requirePresent(
        userDao.findByIdAndIsDeletedFalse(userId), BizErrorCode.USER_NOT_FOUND, "User not found");
  }

  private UserPlot getAndValidateUnlockedPlot(Long userId, Long plotId) {
    UserPlot plot =
        ServiceGuardUtils.requirePresent(
            userPlotDao.findByIdAndUserIdAndIsDeletedFalse(plotId, userId),
            BizErrorCode.PLOT_NOT_FOUND,
            "Plot not found");
    if (isLocked(plot)) {
      throw new ServiceException(BizErrorCode.PLOT_LOCKED, "Plot is locked");
    }
    return plot;
  }

  private UserCrop getAndValidateCrop(Long userId, Long plotId) {
    return ServiceGuardUtils.requirePresent(
        userCropDao.findByUserIdAndPlotIdAndIsDeletedFalse(userId, plotId),
        BizErrorCode.CROP_NOT_FOUND,
        "Crop on plot not found");
  }

  private UserSeed getAndValidateUserSeed(Long userId, Long seedTypeId) {
    return ServiceGuardUtils.requirePresent(
        userSeedDao.findByUserIdAndSeedTypeIdAndIsDeletedFalse(userId, seedTypeId),
        BizErrorCode.SEED_INVENTORY_NOT_FOUND,
        "Seed inventory not found");
  }

  private void saveHarvestFailLog(
      Long userId,
      Long plotId,
      UserCrop crop,
      OffsetDateTime now,
      String reason,
      BizErrorCode errorCode,
      String errorMsg) {
    gameplayCoreService.touchForUpdate(crop, userId, now);
    userCropDao.save(crop);
    userCropActionLogDao.save(
        gameplayCoreService.buildCropActionLog(
            userId,
            plotId,
            crop.getId(),
            crop.getSeedTypeId(),
            "HARVEST",
            "FAIL",
            now,
            "{\"reason\":\"" + reason + "\"}"));
    throw new ServiceException(errorCode, errorMsg);
  }

  private CropOverviewVO buildCropOverview(
      UserCrop crop, SeedType seedType, List<SeedGrowthStage> stages, OffsetDateTime now) {
    CropOverviewVO cropVO = new CropOverviewVO();
    Short runtimeStatus = calculateGrowStatus(crop, now);
    SeedGrowthStage stage = resolveDisplayStage(crop, seedType, stages, runtimeStatus);
    cropVO.setCropId(crop.getId());
    cropVO.setSeedTypeId(crop.getSeedTypeId());
    cropVO.setSeedTypeName(
        seedType == null ? "" : gameplayCoreService.safeString(seedType.getName()));
    cropVO.setGrowStatus(runtimeStatus);
    cropVO.setCurrentStageIndex(
        stage == null ? crop.getCurrentStageIndex() : stage.getStageIndex());
    cropVO.setHarvestCount(crop.getHarvestCount());
    cropVO.setPlantedAt(crop.getPlantedAt());
    cropVO.setExpectedRipeAt(crop.getExpectedRipeAt());
    cropVO.setExpectedWitheredAt(crop.getExpectedWitheredAt());
    cropVO.setRemainMatureSeconds(calcRemainSeconds(now, crop.getExpectedRipeAt()));
    cropVO.setRemainWitherSeconds(calcRemainSeconds(now, crop.getExpectedWitheredAt()));
    cropVO.setBugCount(crop.getBugCount());
    cropVO.setMaxBugLimit(seedType == null ? (short) 0 : safeShort(seedType.getMaxBugLimit()));
    cropVO.setCanCare(
        safeShort(crop.getBugCount()) > 0 && !CropStatus.isWithered(cropVO.getGrowStatus()));
    cropVO.setHarvestable(CropStatus.isRipe(cropVO.getGrowStatus()));
    cropVO.setStageAssetUrl(
        stage == null ? "" : gameplayCoreService.safeString(stage.getAssetUrl()));
    cropVO.setStageWidth(stage == null ? 0 : safeInteger(stage.getWidth()));
    cropVO.setStageHeight(stage == null ? 0 : safeInteger(stage.getHeight()));
    cropVO.setStageOffsetX(stage == null ? 0 : safeInteger(stage.getOffsetX()));
    cropVO.setStageOffsetY(stage == null ? 0 : safeInteger(stage.getOffsetY()));
    return cropVO;
  }

  private void syncCropStatus(UserCrop crop, OffsetDateTime now) {
    Short runtimeStatus = calculateGrowStatus(crop, now);
    if (runtimeStatus != null && !runtimeStatus.equals(crop.getGrowStatus())) {
      crop.setGrowStatus(runtimeStatus);
      if (CropStatus.isRipe(runtimeStatus) && crop.getMaturedAt() == null) {
        crop.setMaturedAt(now);
      }
      if (CropStatus.isWithered(runtimeStatus) && crop.getWitheredAt() == null) {
        crop.setWitheredAt(now);
      }
    }
    alignCropStageToRuntime(crop, runtimeStatus, now);
  }

  private Short calculateGrowStatus(UserCrop crop, OffsetDateTime now) {
    return CropStatusSchedulerServiceImp.calculateGrowStatus(crop, now);
  }

  private boolean isSoilIncompatible(SeedType seedType, SoilType soilType) {
    if (seedType == null
        || soilType == null
        || soilType.getBitCode() == null
        || soilType.getBitCode() <= 0) {
      return true;
    }
    long bits = safeLong(seedType.getEnableSoilTypeBits());
    int bitCode = soilType.getBitCode();
    return (bits & bitCode) != bitCode;
  }

  private BigDecimal normalizeMultiplier(BigDecimal multiplier) {
    return (multiplier == null || multiplier.compareTo(BigDecimal.ZERO) <= 0)
        ? BigDecimal.ONE
        : multiplier;
  }

  private SeedGrowthStage resolveDisplayStage(
      UserCrop crop, SeedType seedType, List<SeedGrowthStage> stages, Short runtimeStatus) {
    if (stages == null || stages.isEmpty()) {
      return null;
    }
    SeedStageRuleSupport.ResolvedStageRule stageRule = resolveStageRule(seedType, stages);
    Short displayStageIndex = crop.getCurrentStageIndex();
    if (CropStatus.isWithered(runtimeStatus)) {
      displayStageIndex = stageRule.witherStageIndex();
    } else if (CropStatus.isRipe(runtimeStatus)
        && SeedStageRuleSupport.findStagePos(stages, displayStageIndex)
            < SeedStageRuleSupport.findStagePos(stages, stageRule.harvestStageIndex())) {
      displayStageIndex = stageRule.harvestStageIndex();
    }
    return SeedStageRuleSupport.findStageByIndex(stages, displayStageIndex);
  }

  private void alignCropStageToRuntime(UserCrop crop, Short runtimeStatus, OffsetDateTime now) {
    if (crop == null || crop.getSeedTypeId() == null) {
      return;
    }
    List<SeedGrowthStage> stages =
        seedGrowthStageDao.findBySeedTypeIdAndIsDeletedFalseOrderByStageIndexAsc(
            crop.getSeedTypeId());
    if (stages.isEmpty()) {
      return;
    }
    SeedType seedType = seedTypeDao.findByIdAndIsDeletedFalse(crop.getSeedTypeId()).orElse(null);
    SeedStageRuleSupport.ResolvedStageRule stageRule = resolveStageRule(seedType, stages);
    if (CropStatus.isWithered(runtimeStatus)
        && safeShort(crop.getCurrentStageIndex()) != stageRule.witherStageIndex()) {
      crop.setCurrentStageIndex(stageRule.witherStageIndex());
      crop.setStageStartedAt(
          crop.getExpectedWitheredAt() == null ? now : crop.getExpectedWitheredAt());
      return;
    }
    if (CropStatus.isRipe(runtimeStatus)
        && SeedStageRuleSupport.findStagePos(stages, crop.getCurrentStageIndex())
            < SeedStageRuleSupport.findStagePos(stages, stageRule.harvestStageIndex())) {
      crop.setCurrentStageIndex(stageRule.harvestStageIndex());
      crop.setStageStartedAt(crop.getExpectedRipeAt() == null ? now : crop.getExpectedRipeAt());
    }
  }

  private SeedStageRuleSupport.ResolvedStageRule resolveStageRule(
      SeedType seedType, List<SeedGrowthStage> stages) {
    if (stages == null || stages.isEmpty()) {
      throw new ServiceException(
          BizErrorCode.SEED_GROWTH_STAGE_NOT_CONFIGURED, "Seed growth stages are not configured");
    }
    return SeedStageRuleSupport.resolve(seedType, stages);
  }

  private int calculateSecondsUntilStage(
      List<SeedGrowthStage> stages,
      short startStageIndex,
      short targetStageIndex,
      BigDecimal multiplier) {
    return SeedStageRuleSupport.calculateSecondsUntilStage(
        stages, startStageIndex, targetStageIndex, normalizeMultiplier(multiplier));
  }

  private long calculateExpandCostCoin(int currentTotalPlots) {
    // Return the minimum expand cost among available soil types
    return soilTypeDao.findByIsDeletedFalseOrderByIdAsc().stream()
        .map(SoilType::getExpandCostCoin)
        .filter(cost -> cost != null)
        .mapToLong(Long::longValue)
        .min()
        .orElse(0L);
  }

  private Map<Long, SoilType> getSoilTypeMap() {
    return soilTypeDao.findByIsDeletedFalseOrderByIdAsc().stream()
        .collect(Collectors.toMap(SoilType::getId, Function.identity()));
  }

  private Map<Long, SeedType> getSeedTypeMap() {
    return seedTypeDao.findByIsDeletedFalseOrderByIdAsc().stream()
        .collect(Collectors.toMap(SeedType::getId, Function.identity()));
  }

  private boolean isLocked(UserPlot plot) {
    return Boolean.TRUE.equals(plot.getIsLocked());
  }

  private boolean isUnlocked(UserPlot plot) {
    return !isLocked(plot);
  }

  private boolean isDeleted(UserCrop crop) {
    return Boolean.TRUE.equals(crop.getIsDeleted());
  }

  private String resolvePlotLockSource(Long userId) {
    if (plotPolicyDao.findFirstByActiveTrueAndIsDeletedFalseOrderByIdAsc().isPresent()) {
      return PlotRuleConstants.LOCK_SOURCE_GLOBAL_POLICY;
    }
    return PlotRuleConstants.LOCK_SOURCE_SYSTEM;
  }

  private String resolvePlotLockRuleCode(String lockSource) {
    if (PlotRuleConstants.LOCK_SOURCE_GLOBAL_POLICY.equals(lockSource)) {
      return plotPolicyDao
          .findFirstByActiveTrueAndIsDeletedFalseOrderByIdAsc()
          .map(item -> gameplayCoreService.safeString(item.getDefaultLockRuleCode()))
          .filter(text -> !text.isEmpty())
          .orElse(PlotRuleConstants.LOCK_RULE_DEFAULT_LOCKED);
    }
    return PlotRuleConstants.LOCK_RULE_SYSTEM_COMPAT;
  }

  private long calcRemainSeconds(OffsetDateTime now, OffsetDateTime target) {
    return target == null ? 0L : Math.max(Duration.between(now, target).getSeconds(), 0L);
  }

  private long safeLong(Long value) {
    return gameplayCoreService.safeLong(value);
  }

  private int safeInteger(Integer value) {
    return gameplayCoreService.safeInteger(value);
  }

  private short safeShort(Short value) {
    return gameplayCoreService.safeShort(value);
  }
}
