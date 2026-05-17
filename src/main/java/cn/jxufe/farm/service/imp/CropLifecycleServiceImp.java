package cn.jxufe.farm.service.imp;

import cn.jxufe.farm.bean.dto.CareCropDTO;
import cn.jxufe.farm.bean.dto.HarvestCropDTO;
import cn.jxufe.farm.bean.dto.MyFarmOverviewDTO;
import cn.jxufe.farm.bean.dto.MyPlantingPanelDTO;
import cn.jxufe.farm.bean.dto.PlantCropDTO;
import cn.jxufe.farm.bean.dto.SeedPlantablePlotsDTO;
import cn.jxufe.farm.bean.vo.CareResultVO;
import cn.jxufe.farm.bean.vo.CropOverviewVO;
import cn.jxufe.farm.bean.vo.HarvestResultVO;
import cn.jxufe.farm.bean.vo.MyFarmOverviewVO;
import cn.jxufe.farm.bean.vo.MyPlantingPanelVO;
import cn.jxufe.farm.bean.vo.PlantResultVO;
import cn.jxufe.farm.bean.vo.PlantablePlotVO;
import cn.jxufe.farm.bean.vo.PlotOverviewVO;
import cn.jxufe.farm.bean.vo.SeedBackpackItemVO;
import cn.jxufe.farm.bean.vo.SeedPlantablePlotsVO;
import cn.jxufe.farm.common.constants.BizErrorCode;
import cn.jxufe.farm.common.exception.ServiceException;
import cn.jxufe.farm.common.utils.ServiceGuard;
import cn.jxufe.farm.config.properties.GameplayPolicyProperties;
import cn.jxufe.farm.dao.SeedGrowthStageDao;
import cn.jxufe.farm.dao.SeedTypeDao;
import cn.jxufe.farm.dao.SoilTypeDao;
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
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CropLifecycleServiceImp implements CropLifecycleService {

    private static final short GROW_STATUS_GROWING = 1;
    private static final short GROW_STATUS_RIPE = 2;
    private static final short GROW_STATUS_WITHERED = 3;

    private final UserDao userDao;
    private final UserPlotDao userPlotDao;
    private final UserCropDao userCropDao;
    private final UserSeedDao userSeedDao;
    private final UserFruitDao userFruitDao;
    private final SeedTypeDao seedTypeDao;
    private final SoilTypeDao soilTypeDao;
    private final SeedGrowthStageDao seedGrowthStageDao;
    private final UserInventoryFlowDao userInventoryFlowDao;
    private final UserCropActionLogDao userCropActionLogDao;
    private final GameplayCoreService gameplayCoreService;
    private final GameplayPolicyProperties gameplayPolicyProperties;

    public CropLifecycleServiceImp(UserDao userDao,
                                   UserPlotDao userPlotDao,
                                   UserCropDao userCropDao,
                                   UserSeedDao userSeedDao,
                                   UserFruitDao userFruitDao,
                                   SeedTypeDao seedTypeDao,
                                   SoilTypeDao soilTypeDao,
                                   SeedGrowthStageDao seedGrowthStageDao,
                                   UserInventoryFlowDao userInventoryFlowDao,
                                   UserCropActionLogDao userCropActionLogDao,
                                   GameplayCoreService gameplayCoreService,
                                   GameplayPolicyProperties gameplayPolicyProperties) {
        this.userDao = userDao;
        this.userPlotDao = userPlotDao;
        this.userCropDao = userCropDao;
        this.userSeedDao = userSeedDao;
        this.userFruitDao = userFruitDao;
        this.seedTypeDao = seedTypeDao;
        this.soilTypeDao = soilTypeDao;
        this.seedGrowthStageDao = seedGrowthStageDao;
        this.userInventoryFlowDao = userInventoryFlowDao;
        this.userCropActionLogDao = userCropActionLogDao;
        this.gameplayCoreService = gameplayCoreService;
        this.gameplayPolicyProperties = gameplayPolicyProperties;
    }

    @Override
    @Transactional
    public PlantResultVO plant(PlantCropDTO params) {
        ServiceGuard.requireNotNull(params, BizErrorCode.PARAM_INVALID, "请求参数不能为空");
        Long userId = ServiceGuard.requirePositive(params.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
        Long plotId = ServiceGuard.requirePositive(params.getPlotId(), BizErrorCode.PARAM_INVALID, "地块ID无效");
        Long seedTypeId = ServiceGuard.requirePositive(params.getSeedTypeId(), BizErrorCode.PARAM_INVALID, "参数 seedTypeId 无效");

        ServiceGuard.requirePresent(userDao.findByIdAndIsDeletedFalse(userId), BizErrorCode.USER_NOT_FOUND, "用户不存在");
        UserPlot plot = ServiceGuard.requirePresent(
                userPlotDao.findByIdAndUserIdAndIsDeletedFalse(plotId, userId),
                BizErrorCode.PLOT_NOT_FOUND, "地块不存在"
        );
        if (Boolean.TRUE.equals(plot.getIsLocked())) {
            throw new ServiceException(BizErrorCode.PLOT_LOCKED, "地块已锁定");
        }

        SeedType seedType = ServiceGuard.requirePresent(
                seedTypeDao.findByIdAndIsDeletedFalse(seedTypeId),
                BizErrorCode.SEED_TYPE_NOT_FOUND, "种子类型不存在"
        );
        SoilType soilType = ServiceGuard.requirePresent(
                soilTypeDao.findByIdAndIsDeletedFalse(plot.getSoilTypeId()),
                BizErrorCode.SOIL_TYPE_NOT_FOUND, "土壤类型不存在"
        );
        if (!isSoilCompatible(seedType, soilType)) {
            throw new ServiceException(BizErrorCode.SOIL_NOT_COMPATIBLE, "种子与土壤不兼容");
        }

        if (userCropDao.findByUserIdAndPlotIdAndIsDeletedFalse(userId, plotId).isPresent()) {
            throw new ServiceException(BizErrorCode.PLOT_ALREADY_HAS_CROP, "地块已有作物");
        }

        UserSeed userSeed = ServiceGuard.requirePresent(
                userSeedDao.findByUserIdAndSeedTypeIdAndIsDeletedFalse(userId, seedTypeId),
                BizErrorCode.SEED_INVENTORY_NOT_FOUND, "种子库存不存在"
        );
        long beforeSeedAmount = safeLong(userSeed.getQuantity());
        long beforeFrozenSeedAmount = safeLong(userSeed.getFrozenQuantity());
        long availableSeed = beforeSeedAmount - beforeFrozenSeedAmount;
        if (availableSeed <= 0) {
            throw new ServiceException(BizErrorCode.SEED_NOT_ENOUGH, "种子库存不足");
        }

        List<SeedGrowthStage> stages = seedGrowthStageDao.findBySeedTypeIdAndIsDeletedFalseOrderByStageIndexAsc(seedTypeId);
        if (stages.isEmpty()) {
            throw new ServiceException(BizErrorCode.SEED_GROWTH_STAGE_NOT_CONFIGURED, "未配置种子生长阶段");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Short firstStageIndex = resolveFirstStageIndex(stages);
        int growSeconds = calculateDurationSeconds(stages, null, normalizeMultiplier(soilType.getGrowSpeedMultiplier()));
        int witherWindowSeconds = Math.max(3600, Math.max(growSeconds, 0));

        int seedUpdated = userSeedDao.decreaseAvailableQuantityIfEnough(userSeed.getId(), 1L, userId, now);
        if (seedUpdated <= 0) {
            throw new ServiceException(BizErrorCode.SEED_NOT_ENOUGH, "种子库存不足");
        }
        UserSeed latestUserSeed = ServiceGuard.requirePresent(
                userSeedDao.findByUserIdAndSeedTypeIdAndIsDeletedFalse(userId, seedTypeId),
                BizErrorCode.SEED_INVENTORY_NOT_FOUND,
                "种子库存不存在"
        );
        long afterSeedAmount = safeLong(latestUserSeed.getQuantity());

        UserCrop crop = new UserCrop();
        gameplayCoreService.initNewEntity(crop, userId, now);
        crop.setUserId(userId);
        crop.setPlotId(plotId);
        crop.setSeedTypeId(seedTypeId);
        crop.setPlantedAt(now);
        crop.setStageStartedAt(now);
        crop.setCurrentStageIndex(firstStageIndex);
        crop.setHarvestCount((short) 0);
        crop.setBugCount((short) 0);
        crop.setLastBugAt(null);
        crop.setLastCareAt(null);
        crop.setLastHarvestAt(null);
        crop.setMaturedAt(growSeconds <= 0 ? now : null);
        crop.setWitheredAt(null);
        crop.setGrowStatus(growSeconds <= 0 ? GROW_STATUS_RIPE : GROW_STATUS_GROWING);
        crop.setExpectedRipeAt(now.plusSeconds(Math.max(growSeconds, 0)));
        crop.setExpectedWitheredAt(crop.getExpectedRipeAt().plusSeconds(witherWindowSeconds));
        UserCrop savedCrop = userCropDao.save(crop);

        String bizId = "PLANT:" + savedCrop.getId() + ":" + now.toEpochSecond();
        userInventoryFlowDao.save(gameplayCoreService.buildInventoryFlow(
                userId,
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
                "{\"plotId\":" + plotId + ",\"cropId\":" + savedCrop.getId() + "}"
        ));
        userCropActionLogDao.save(gameplayCoreService.buildCropActionLog(
                userId,
                plotId,
                savedCrop.getId(),
                seedTypeId,
                "PLANT",
                "SUCCESS",
                now,
                "{\"seedTypeId\":" + seedTypeId + ",\"remainSeed\":" + afterSeedAmount + "}"
        ));

        PlantResultVO result = new PlantResultVO();
        result.setUserId(userId);
        result.setPlotId(plotId);
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
        ServiceGuard.requireNotNull(params, BizErrorCode.PARAM_INVALID, "请求参数不能为空");
        Long userId = ServiceGuard.requirePositive(params.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
        Long plotId = ServiceGuard.requirePositive(params.getPlotId(), BizErrorCode.PARAM_INVALID, "地块ID无效");

        User user = ServiceGuard.requirePresent(
                userDao.findByIdAndIsDeletedFalse(userId),
                "用户不存在"
        );
        UserPlot plot = ServiceGuard.requirePresent(
                userPlotDao.findByIdAndUserIdAndIsDeletedFalse(plotId, userId),
                BizErrorCode.PLOT_NOT_FOUND, "地块不存在"
        );
        if (Boolean.TRUE.equals(plot.getIsLocked())) {
            throw new ServiceException(BizErrorCode.PLOT_LOCKED, "地块已锁定");
        }

        UserCrop crop = ServiceGuard.requirePresent(
                userCropDao.findByUserIdAndPlotIdAndIsDeletedFalse(userId, plotId),
                BizErrorCode.CROP_NOT_FOUND, "地块上作物不存在"
        );
        SeedType seedType = ServiceGuard.requirePresent(
                seedTypeDao.findByIdAndIsDeletedFalse(crop.getSeedTypeId()),
                BizErrorCode.SEED_TYPE_NOT_FOUND, "作物对应种子类型不存在"
        );

        OffsetDateTime now = OffsetDateTime.now();
        syncCropStatus(crop, now);
        if (crop.getGrowStatus() == GROW_STATUS_WITHERED) {
            gameplayCoreService.touchForUpdate(crop, userId, now);
            userCropDao.save(crop);
            userCropActionLogDao.save(gameplayCoreService.buildCropActionLog(
                    userId, plotId, crop.getId(), crop.getSeedTypeId(),
                    "HARVEST", "FAIL", now, "{\"reason\":\"WITHERED\"}"
            ));
            throw new ServiceException(BizErrorCode.CROP_WITHERED, "作物已枯萎");
        }
        if (crop.getGrowStatus() != GROW_STATUS_RIPE) {
            gameplayCoreService.touchForUpdate(crop, userId, now);
            userCropDao.save(crop);
            userCropActionLogDao.save(gameplayCoreService.buildCropActionLog(
                    userId, plotId, crop.getId(), crop.getSeedTypeId(),
                    "HARVEST", "FAIL", now, "{\"reason\":\"NOT_RIPE\"}"
            ));
            throw new ServiceException(BizErrorCode.CROP_NOT_RIPE, "作物未成熟");
        }

        long baseFruitGain = Math.max(0L, safeInteger(seedType.getHarvestFruitNumber()));
        short bugCountBefore = safeShort(crop.getBugCount());
        long fruitGain = Math.max(0L, baseFruitGain - bugCountBefore);
        long expGain = Math.max(0L, safeLong(seedType.getHarvestExperience()));
        long scoreGain = Math.max(0L, safeLong(seedType.getHarvestScore()));

        UserFruit userFruit = userFruitDao.findByUserIdAndSeedTypeIdAndIsDeletedFalse(userId, crop.getSeedTypeId())
                .orElseGet(() -> gameplayCoreService.createUserFruit(userId, crop.getSeedTypeId(), now));
        long beforeFruitAmount = safeLong(userFruit.getQuantity());
        long beforeFrozenFruitAmount = safeLong(userFruit.getFrozenQuantity());
        int fruitUpdated = userFruitDao.increaseQuantity(userFruit.getId(), fruitGain, userId, now);
        if (fruitUpdated <= 0) {
            throw new ServiceException(BizErrorCode.FRUIT_INVENTORY_NOT_FOUND, "果实库存不存在");
        }
        UserFruit latestUserFruit = ServiceGuard.requirePresent(
                userFruitDao.findByUserIdAndSeedTypeIdAndIsDeletedFalse(userId, crop.getSeedTypeId()),
                BizErrorCode.FRUIT_INVENTORY_NOT_FOUND,
                "果实库存不存在"
        );
        long afterFruitAmount = safeLong(latestUserFruit.getQuantity());
        long beforeFruitAmountAccurate = afterFruitAmount - fruitGain;

        int profileUpdated = userDao.increaseExperienceAndScore(userId, expGain, scoreGain, userId, now);
        if (profileUpdated <= 0) {
            throw new ServiceException(BizErrorCode.USER_NOT_FOUND, "用户不存在");
        }
        User latestUser = ServiceGuard.requirePresent(
                userDao.findByIdAndIsDeletedFalse(userId),
                BizErrorCode.USER_NOT_FOUND,
                "用户不存在"
        );
        long afterExp = safeLong(latestUser.getExperience());
        long afterScore = safeLong(latestUser.getScore());
        long beforeExp = afterExp - expGain;
        long beforeScore = afterScore - scoreGain;

        short maxHarvestCount = safeShort(seedType.getMaxHarvestCount()) <= 0 ? 1 : safeShort(seedType.getMaxHarvestCount());
        short nextHarvestCount = (short) (safeShort(crop.getHarvestCount()) + 1);
        crop.setHarvestCount(nextHarvestCount);
        crop.setLastHarvestAt(now);

        boolean cropCleared = nextHarvestCount >= maxHarvestCount;
        Short nextGrowStatus;
        Short nextStageIndex;
        OffsetDateTime nextExpectedRipeAt;
        OffsetDateTime nextExpectedWitheredAt;
        short bugCountAfter;
        if (cropCleared) {
            crop.setIsDeleted(true);
            gameplayCoreService.touchForUpdate(crop, userId, now);
            userCropDao.save(crop);
            nextGrowStatus = null;
            nextStageIndex = null;
            nextExpectedRipeAt = null;
            nextExpectedWitheredAt = null;
            bugCountAfter = 0;
        } else {
            List<SeedGrowthStage> stages = seedGrowthStageDao.findBySeedTypeIdAndIsDeletedFalseOrderByStageIndexAsc(seedType.getId());
            if (stages.isEmpty()) {
                throw new ServiceException(BizErrorCode.SEED_GROWTH_STAGE_NOT_CONFIGURED, "未配置种子生长阶段");
            }
            SoilType soilType = soilTypeDao.findByIdAndIsDeletedFalse(plot.getSoilTypeId()).orElse(null);
            BigDecimal multiplier = soilType == null ? BigDecimal.ONE : normalizeMultiplier(soilType.getGrowSpeedMultiplier());
            Short regrowStageIndex = seedType.getRegrowStageIndex() == null
                    ? resolveFirstStageIndex(stages)
                    : seedType.getRegrowStageIndex();
            int regrowSeconds = calculateDurationSeconds(stages, regrowStageIndex, multiplier);
            int witherWindowSeconds = Math.max(3600, Math.max(regrowSeconds, 0));
            nextExpectedRipeAt = now.plusSeconds(Math.max(regrowSeconds, 0));
            nextExpectedWitheredAt = nextExpectedRipeAt.plusSeconds(witherWindowSeconds);
            nextGrowStatus = regrowSeconds <= 0 ? GROW_STATUS_RIPE : GROW_STATUS_GROWING;
            nextStageIndex = regrowStageIndex;

            crop.setStageStartedAt(now);
            crop.setCurrentStageIndex(regrowStageIndex);
            crop.setGrowStatus(nextGrowStatus);
            crop.setMaturedAt(regrowSeconds <= 0 ? now : null);
            crop.setWitheredAt(null);
            crop.setExpectedRipeAt(nextExpectedRipeAt);
            crop.setExpectedWitheredAt(nextExpectedWitheredAt);
            crop.setBugCount((short) 0);
            crop.setLastBugAt(null);
            crop.setLastCareAt(null);
            gameplayCoreService.touchForUpdate(crop, userId, now);
            userCropDao.save(crop);
            bugCountAfter = 0;
        }

        String bizId = "HARVEST:" + crop.getId() + ":" + now.toEpochSecond();
        userInventoryFlowDao.save(gameplayCoreService.buildInventoryFlow(
                userId,
                "FRUIT",
                crop.getSeedTypeId(),
                "INCOME",
                fruitGain,
                beforeFruitAmountAccurate,
                afterFruitAmount,
                beforeFrozenFruitAmount,
                beforeFrozenFruitAmount,
                "HARVEST",
                bizId,
                now,
                "{\"plotId\":" + plotId + ",\"cropId\":" + crop.getId() + "}"
        ));
        userCropActionLogDao.save(gameplayCoreService.buildCropActionLog(
                userId, plotId, crop.getId(), crop.getSeedTypeId(),
                "HARVEST", "SUCCESS", now,
                "{\"fruitGain\":" + fruitGain + ",\"cropCleared\":" + cropCleared + "}"
        ));

        HarvestResultVO result = new HarvestResultVO();
        result.setUserId(userId);
        result.setPlotId(plotId);
        result.setCropId(crop.getId());
        result.setSeedTypeId(crop.getSeedTypeId());
        result.setHarvestFruitNumber(fruitGain);
        result.setTotalFruitQuantity(afterFruitAmount);
        result.setExperienceGain(expGain);
        result.setScoreGain(scoreGain);
        result.setCurrentExperience(afterExp);
        result.setCurrentScore(afterScore);
        result.setBugCountBefore(bugCountBefore);
        result.setBugCountAfter(bugCountAfter);
        result.setCropCleared(cropCleared);
        result.setNextGrowStatus(nextGrowStatus);
        result.setNextStageIndex(nextStageIndex);
        result.setNextExpectedRipeAt(nextExpectedRipeAt);
        result.setNextExpectedWitheredAt(nextExpectedWitheredAt);
        return result;
    }

    @Override
    @Transactional
    public CareResultVO care(CareCropDTO params) {
        ServiceGuard.requireNotNull(params, BizErrorCode.PARAM_INVALID, "请求参数不能为空");
        Long userId = ServiceGuard.requirePositive(params.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
        Long plotId = ServiceGuard.requirePositive(params.getPlotId(), BizErrorCode.PARAM_INVALID, "地块ID无效");

        ServiceGuard.requirePresent(userDao.findByIdAndIsDeletedFalse(userId), BizErrorCode.USER_NOT_FOUND, "用户不存在");
        UserPlot plot = ServiceGuard.requirePresent(
                userPlotDao.findByIdAndUserIdAndIsDeletedFalse(plotId, userId),
                BizErrorCode.PLOT_NOT_FOUND, "地块不存在"
        );
        if (Boolean.TRUE.equals(plot.getIsLocked())) {
            throw new ServiceException(BizErrorCode.PLOT_LOCKED, "地块已锁定");
        }

        UserCrop crop = ServiceGuard.requirePresent(
                userCropDao.findByUserIdAndPlotIdAndIsDeletedFalse(userId, plotId),
                BizErrorCode.CROP_NOT_FOUND, "地块上作物不存在"
        );
        SeedType seedType = ServiceGuard.requirePresent(
                seedTypeDao.findByIdAndIsDeletedFalse(crop.getSeedTypeId()),
                BizErrorCode.SEED_TYPE_NOT_FOUND, "作物对应种子类型不存在"
        );

        OffsetDateTime now = OffsetDateTime.now();
        syncCropStatus(crop, now);
        short bugCountBefore = safeShort(crop.getBugCount());
        if (crop.getGrowStatus() == GROW_STATUS_WITHERED) {
            userCropActionLogDao.save(gameplayCoreService.buildCropActionLog(
                    userId, plotId, crop.getId(), crop.getSeedTypeId(),
                    "CARE", "FAIL", now, "{\"reason\":\"WITHERED\"}"
            ));
            throw new ServiceException(BizErrorCode.CROP_WITHERED, "作物已枯萎");
        }

        short bugCountAfter = bugCountBefore > 0 ? (short) (bugCountBefore - 1) : 0;
        crop.setBugCount(bugCountAfter);
        crop.setLastCareAt(now);
        gameplayCoreService.touchForUpdate(crop, userId, now);
        userCropDao.save(crop);

        userCropActionLogDao.save(gameplayCoreService.buildCropActionLog(
                userId, plotId, crop.getId(), crop.getSeedTypeId(),
                "CARE", "SUCCESS", now,
                "{\"bugCountBefore\":" + bugCountBefore + ",\"bugCountAfter\":" + bugCountAfter + "}"
        ));

        CareResultVO result = new CareResultVO();
        result.setUserId(userId);
        result.setPlotId(plotId);
        result.setCropId(crop.getId());
        result.setSeedTypeId(seedType.getId());
        result.setBugCountBefore(bugCountBefore);
        result.setBugCountAfter(bugCountAfter);
        result.setCurrentStageIndex(crop.getCurrentStageIndex());
        result.setGrowStatus(crop.getGrowStatus());
        result.setLastCareAt(crop.getLastCareAt());
        return result;
    }

    @Override
    public MyFarmOverviewVO myFarmOverview(MyFarmOverviewDTO params) {
        Long userId = ServiceGuard.requirePositive(params == null ? null : params.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
        ServiceGuard.requirePresent(userDao.findByIdAndIsDeletedFalse(userId), BizErrorCode.USER_NOT_FOUND, "用户不存在");

        OffsetDateTime now = OffsetDateTime.now();
        List<UserPlot> plots = userPlotDao.findByUserIdAndIsDeletedFalseOrderByPlotIndexAsc(userId);
        List<UserCrop> crops = userCropDao.findByUserIdAndIsDeletedFalseOrderByIdAsc(userId);
        Map<Long, UserCrop> cropByPlotId = new HashMap<>();
        for (UserCrop crop : crops) {
            cropByPlotId.put(crop.getPlotId(), crop);
        }
        Long nextUnlockPlotId = null;
        Short nextUnlockPlotIndex = null;
        for (UserPlot plot : plots) {
            if (Boolean.TRUE.equals(plot.getIsLocked())) {
                nextUnlockPlotId = plot.getId();
                nextUnlockPlotIndex = plot.getPlotIndex();
                break;
            }
        }

        Map<Long, SoilType> soilTypeMap = buildSoilTypeMap();
        Map<Long, SeedType> seedTypeMap = buildSeedTypeMap();
        List<PlotOverviewVO> plotVOs = new ArrayList<>();
        int unlockedPlots = 0;
        int lockedPlots = 0;
        int occupiedPlots = 0;
        int harvestableCount = 0;
        for (UserPlot plot : plots) {
            PlotOverviewVO plotVO = new PlotOverviewVO();
            plotVO.setPlotId(plot.getId());
            plotVO.setUserId(plot.getUserId());
            plotVO.setPlotIndex(plot.getPlotIndex());
            plotVO.setLocked(Boolean.TRUE.equals(plot.getIsLocked()));
            plotVO.setLockReason(gameplayCoreService.safeString(plot.getLockReason()));
            plotVO.setSoilTypeId(plot.getSoilTypeId());
            SoilType soilType = soilTypeMap.get(plot.getSoilTypeId());
            plotVO.setSoilBitCode(soilType == null ? null : soilType.getBitCode());
            plotVO.setSoilName(soilType == null ? "" : gameplayCoreService.safeString(soilType.getName()));
            long unlockCostCoin = calculateUnlockCostCoin(plot.getPlotIndex());
            plotVO.setUnlockCostCoin(unlockCostCoin);
            plotVO.setCanUnlock(
                    Boolean.TRUE.equals(plot.getIsLocked())
                            && nextUnlockPlotId != null
                            && nextUnlockPlotIndex != null
                            && nextUnlockPlotId.equals(plot.getId())
                            && nextUnlockPlotIndex.equals(plot.getPlotIndex())
            );

            UserCrop crop = cropByPlotId.get(plot.getId());
            boolean hasCrop = crop != null && !Boolean.TRUE.equals(crop.getIsDeleted());
            plotVO.setHasCrop(hasCrop);
            plotVO.setOccupied(hasCrop && !Boolean.TRUE.equals(plot.getIsLocked()));
            plotVO.setPlantable(!Boolean.TRUE.equals(plot.getIsLocked()) && !hasCrop);

            if (Boolean.TRUE.equals(plot.getIsLocked())) {
                lockedPlots++;
            } else {
                unlockedPlots++;
            }
            if (hasCrop && !Boolean.TRUE.equals(plot.getIsLocked())) {
                occupiedPlots++;
                CropOverviewVO cropVO = buildCropOverview(crop, seedTypeMap.get(crop.getSeedTypeId()), now);
                plotVO.setCrop(cropVO);
                if (Boolean.TRUE.equals(cropVO.getHarvestable())) {
                    harvestableCount++;
                }
            } else {
                plotVO.setCrop(null);
            }
            plotVOs.add(plotVO);
        }

        int totalPlots = plots.size();
        MyFarmOverviewVO result = new MyFarmOverviewVO();
        result.setUserId(userId);
        result.setServerTime(now);
        result.setTotalPlots(totalPlots);
        result.setUnlockedPlots(unlockedPlots);
        result.setLockedPlots(lockedPlots);
        result.setOccupiedPlots(occupiedPlots);
        result.setEmptyUnlockedPlots(Math.max(unlockedPlots - occupiedPlots, 0));
        result.setHarvestableCount(harvestableCount);
        result.setNextExpandCostCoin(calculateExpandCostCoin(totalPlots));
        result.setPlots(plotVOs);
        return result;
    }

    @Override
    public MyPlantingPanelVO myPlantingPanel(MyPlantingPanelDTO params) {
        Long userId = ServiceGuard.requirePositive(params == null ? null : params.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
        ServiceGuard.requirePresent(userDao.findByIdAndIsDeletedFalse(userId), BizErrorCode.USER_NOT_FOUND, "用户不存在");

        OffsetDateTime now = OffsetDateTime.now();
        List<UserPlot> plots = userPlotDao.findByUserIdAndIsDeletedFalseOrderByPlotIndexAsc(userId);
        List<UserCrop> crops = userCropDao.findByUserIdAndIsDeletedFalseOrderByIdAsc(userId);
        List<UserSeed> userSeeds = userSeedDao.findByUserIdAndIsDeletedFalseOrderByIdAsc(userId);
        Map<Long, UserCrop> cropByPlotId = new HashMap<>();
        for (UserCrop crop : crops) {
            cropByPlotId.put(crop.getPlotId(), crop);
        }
        Map<Long, SeedType> seedTypeMap = buildSeedTypeMap();

        int unlockedPlotCount = 0;
        int plantablePlotCount = 0;
        for (UserPlot plot : plots) {
            if (!Boolean.TRUE.equals(plot.getIsLocked())) {
                unlockedPlotCount++;
                if (!cropByPlotId.containsKey(plot.getId())) {
                    plantablePlotCount++;
                }
            }
        }

        List<SeedBackpackItemVO> seeds = new ArrayList<>();
        int selectableSeedTypeCount = 0;
        for (UserSeed userSeed : userSeeds) {
            SeedType seedType = seedTypeMap.get(userSeed.getSeedTypeId());
            if (seedType == null) {
                continue;
            }
            long quantity = safeLong(userSeed.getQuantity());
            long frozenQuantity = safeLong(userSeed.getFrozenQuantity());
            long availableQuantity = Math.max(quantity - frozenQuantity, 0L);
            boolean selectable = availableQuantity > 0 && plantablePlotCount > 0;
            if (selectable) {
                selectableSeedTypeCount++;
            }

            SeedBackpackItemVO itemVO = new SeedBackpackItemVO();
            itemVO.setUserSeedId(userSeed.getId());
            itemVO.setSeedTypeId(userSeed.getSeedTypeId());
            itemVO.setSeedTypeName(gameplayCoreService.safeString(seedType.getName()));
            itemVO.setQuantity(quantity);
            itemVO.setFrozenQuantity(frozenQuantity);
            itemVO.setAvailableQuantity(availableQuantity);
            itemVO.setSelectable(selectable);
            seeds.add(itemVO);
        }

        MyPlantingPanelVO result = new MyPlantingPanelVO();
        result.setUserId(userId);
        result.setServerTime(now);
        result.setTotalPlotCount(plots.size());
        result.setUnlockedPlotCount(unlockedPlotCount);
        result.setPlantablePlotCount(plantablePlotCount);
        result.setBackpackSeedTypeCount(seeds.size());
        result.setSelectableSeedTypeCount(selectableSeedTypeCount);
        result.setSeeds(seeds);
        return result;
    }

    @Override
    public SeedPlantablePlotsVO seedPlantablePlots(SeedPlantablePlotsDTO params) {
        ServiceGuard.requireNotNull(params, BizErrorCode.PARAM_INVALID, "请求参数不能为空");
        Long userId = ServiceGuard.requirePositive(params.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
        Long seedTypeId = ServiceGuard.requirePositive(params.getSeedTypeId(), BizErrorCode.PARAM_INVALID, "参数 seedTypeId 无效");

        ServiceGuard.requirePresent(userDao.findByIdAndIsDeletedFalse(userId), BizErrorCode.USER_NOT_FOUND, "用户不存在");
        SeedType seedType = ServiceGuard.requirePresent(seedTypeDao.findByIdAndIsDeletedFalse(seedTypeId), BizErrorCode.SEED_TYPE_NOT_FOUND, "种子类型不存在");

        List<UserPlot> plots = userPlotDao.findByUserIdAndIsDeletedFalseOrderByPlotIndexAsc(userId);
        List<UserCrop> crops = userCropDao.findByUserIdAndIsDeletedFalseOrderByIdAsc(userId);
        Map<Long, UserCrop> cropByPlotId = new HashMap<>();
        for (UserCrop crop : crops) {
            cropByPlotId.put(crop.getPlotId(), crop);
        }

        Map<Long, SoilType> soilTypeMap = buildSoilTypeMap();
        List<PlantablePlotVO> list = new ArrayList<>();
        for (UserPlot plot : plots) {
            if (Boolean.TRUE.equals(plot.getIsLocked())) {
                continue;
            }
            if (cropByPlotId.containsKey(plot.getId())) {
                continue;
            }
            SoilType soilType = soilTypeMap.get(plot.getSoilTypeId());
            if (soilType == null || !isSoilCompatible(seedType, soilType)) {
                continue;
            }
            PlantablePlotVO plotVO = new PlantablePlotVO();
            plotVO.setPlotId(plot.getId());
            plotVO.setPlotIndex(plot.getPlotIndex());
            plotVO.setSoilTypeId(plot.getSoilTypeId());
            plotVO.setSoilBitCode(soilType.getBitCode());
            plotVO.setSoilName(gameplayCoreService.safeString(soilType.getName()));
            list.add(plotVO);
        }

        SeedPlantablePlotsVO result = new SeedPlantablePlotsVO();
        result.setUserId(userId);
        result.setSeedTypeId(seedTypeId);
        result.setServerTime(OffsetDateTime.now());
        result.setPlantableCount(list.size());
        result.setPlots(list);
        return result;
    }

    private CropOverviewVO buildCropOverview(UserCrop crop, SeedType seedType, OffsetDateTime now) {
        CropOverviewVO cropVO = new CropOverviewVO();
        cropVO.setCropId(crop.getId());
        cropVO.setSeedTypeId(crop.getSeedTypeId());
        cropVO.setSeedTypeName(seedType == null ? "" : gameplayCoreService.safeString(seedType.getName()));
        cropVO.setGrowStatus(calculateGrowStatus(crop, now));
        cropVO.setCurrentStageIndex(crop.getCurrentStageIndex());
        cropVO.setHarvestCount(crop.getHarvestCount());
        cropVO.setPlantedAt(crop.getPlantedAt());
        cropVO.setExpectedRipeAt(crop.getExpectedRipeAt());
        cropVO.setExpectedWitheredAt(crop.getExpectedWitheredAt());
        cropVO.setRemainMatureSeconds(calcRemainSeconds(now, crop.getExpectedRipeAt()));
        cropVO.setRemainWitherSeconds(calcRemainSeconds(now, crop.getExpectedWitheredAt()));
        cropVO.setBugCount(crop.getBugCount());
        cropVO.setMaxBugLimit(seedType == null ? (short) 0 : safeShort(seedType.getMaxBugLimit()));
        cropVO.setCanCare(safeShort(crop.getBugCount()) > 0 && cropVO.getGrowStatus() != GROW_STATUS_WITHERED);
        cropVO.setHarvestable(cropVO.getGrowStatus() == GROW_STATUS_RIPE);
        return cropVO;
    }

    private void syncCropStatus(UserCrop crop, OffsetDateTime now) {
        Short runtimeStatus = calculateGrowStatus(crop, now);
        if (runtimeStatus != null && !runtimeStatus.equals(crop.getGrowStatus())) {
            crop.setGrowStatus(runtimeStatus);
            if (runtimeStatus == GROW_STATUS_RIPE && crop.getMaturedAt() == null) {
                crop.setMaturedAt(now);
            }
            if (runtimeStatus == GROW_STATUS_WITHERED && crop.getWitheredAt() == null) {
                crop.setWitheredAt(now);
            }
        }
    }

    private Short calculateGrowStatus(UserCrop crop, OffsetDateTime now) {
        if (crop == null) {
            return null;
        }
        if (crop.getExpectedWitheredAt() != null && now.isAfter(crop.getExpectedWitheredAt())) {
            return GROW_STATUS_WITHERED;
        }
        if (crop.getExpectedRipeAt() != null && !now.isBefore(crop.getExpectedRipeAt())) {
            return GROW_STATUS_RIPE;
        }
        return crop.getGrowStatus() == null ? GROW_STATUS_GROWING : crop.getGrowStatus();
    }

    private boolean isSoilCompatible(SeedType seedType, SoilType soilType) {
        if (seedType == null || soilType == null || soilType.getBitCode() == null || soilType.getBitCode() <= 0) {
            return false;
        }
        long bits = safeLong(seedType.getEnableSoilTypeBits());
        int bitCode = soilType.getBitCode();
        return (bits & bitCode) == bitCode;
    }

    private BigDecimal normalizeMultiplier(BigDecimal multiplier) {
        if (multiplier == null || multiplier.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ONE;
        }
        return multiplier;
    }

    private int calculateDurationSeconds(List<SeedGrowthStage> stages, Short startStageIndex, BigDecimal multiplier) {
        int totalRawSeconds = 0;
        for (SeedGrowthStage stage : stages) {
            if (startStageIndex != null && safeShort(stage.getStageIndex()) < startStageIndex) {
                continue;
            }
            totalRawSeconds += safeInteger(stage.getDurationSeconds());
        }
        if (totalRawSeconds <= 0) {
            return 0;
        }
        BigDecimal adjusted = BigDecimal.valueOf(totalRawSeconds).divide(
                normalizeMultiplier(multiplier),
                0,
                RoundingMode.HALF_UP
        );
        return Math.max(adjusted.intValue(), 0);
    }

    private Short resolveFirstStageIndex(List<SeedGrowthStage> stages) {
        if (stages == null || stages.isEmpty()) {
            return 1;
        }
        Short index = stages.get(0).getStageIndex();
        return index == null || index <= 0 ? 1 : index;
    }

    private long calculateExpandCostCoin(int currentTotalPlots) {
        int freeExpandPlotCountLimit = gameplayPolicyProperties.getPlot().getExpand().getFreePlotCountLimit();
        long baseCostCoin = gameplayPolicyProperties.getPlot().getExpand().getBaseCostCoin();
        long costStepCoin = gameplayPolicyProperties.getPlot().getExpand().getCostStepCoin();
        if (currentTotalPlots < freeExpandPlotCountLimit) {
            return 0L;
        }
        long costSteps = currentTotalPlots - freeExpandPlotCountLimit;
        return baseCostCoin + costSteps * costStepCoin;
    }

    private long calculateUnlockCostCoin(Short plotIndex) {
        int freeUnlockPlotIndexLimit = gameplayPolicyProperties.getPlot().getUnlock().getFreePlotIndexLimit();
        long baseCostCoin = gameplayPolicyProperties.getPlot().getUnlock().getBaseCostCoin();
        long costStepCoin = gameplayPolicyProperties.getPlot().getUnlock().getCostStepCoin();
        short safePlotIndex = plotIndex == null || plotIndex <= 0 ? 1 : plotIndex;
        if (safePlotIndex <= freeUnlockPlotIndexLimit) {
            return 0L;
        }
        long costSteps = safePlotIndex - freeUnlockPlotIndexLimit - 1L;
        return baseCostCoin + costSteps * costStepCoin;
    }

    private Map<Long, SoilType> buildSoilTypeMap() {
        Map<Long, SoilType> map = new HashMap<>();
        for (SoilType soilType : soilTypeDao.findByIsDeletedFalseOrderByIdAsc()) {
            map.put(soilType.getId(), soilType);
        }
        return map;
    }

    private Map<Long, SeedType> buildSeedTypeMap() {
        Map<Long, SeedType> map = new HashMap<>();
        for (SeedType seedType : seedTypeDao.findByIsDeletedFalseOrderByIdAsc()) {
            map.put(seedType.getId(), seedType);
        }
        return map;
    }

    private long calcRemainSeconds(OffsetDateTime now, OffsetDateTime target) {
        if (target == null) {
            return 0L;
        }
        long seconds = Duration.between(now, target).getSeconds();
        return Math.max(seconds, 0L);
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





