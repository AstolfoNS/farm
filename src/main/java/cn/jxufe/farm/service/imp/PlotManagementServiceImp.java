package cn.jxufe.farm.service.imp;

import cn.jxufe.farm.bean.dto.MyFarmOverviewDTO;
import cn.jxufe.farm.bean.dto.PlotExpandDTO;
import cn.jxufe.farm.bean.dto.PlotStatusQueryDTO;
import cn.jxufe.farm.bean.dto.PlotTradeQueryDTO;
import cn.jxufe.farm.bean.dto.PlotUnlockDTO;
import cn.jxufe.farm.bean.vo.MyFarmOverviewVO;
import cn.jxufe.farm.bean.vo.PlotExpandResultVO;
import cn.jxufe.farm.bean.vo.PlotStatusVO;
import cn.jxufe.farm.bean.vo.PlotTradeBizTypeOptionVO;
import cn.jxufe.farm.bean.vo.PlotTradeRecordVO;
import cn.jxufe.farm.bean.vo.PlotUnlockResultVO;
import cn.jxufe.farm.common.constants.BizErrorCode;
import cn.jxufe.farm.common.exception.ServiceException;
import cn.jxufe.farm.common.pages.PageResult;
import cn.jxufe.farm.common.utils.ServiceGuard;
import cn.jxufe.farm.config.properties.GameplayPolicyProperties;
import cn.jxufe.farm.dao.SoilTypeDao;
import cn.jxufe.farm.dao.UserAssetFlowDao;
import cn.jxufe.farm.dao.UserDao;
import cn.jxufe.farm.dao.UserPlotDao;
import cn.jxufe.farm.entity.SoilType;
import cn.jxufe.farm.entity.User;
import cn.jxufe.farm.entity.UserAssetFlow;
import cn.jxufe.farm.entity.UserPlot;
import cn.jxufe.farm.entity.base.BaseEntity;
import cn.jxufe.farm.service.CropLifecycleService;
import cn.jxufe.farm.service.GameplayCoreService;
import cn.jxufe.farm.service.PlotManagementService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PlotManagementServiceImp implements PlotManagementService {

    private final UserDao userDao;
    private final UserPlotDao userPlotDao;
    private final SoilTypeDao soilTypeDao;
    private final UserAssetFlowDao userAssetFlowDao;
    private final CropLifecycleService cropLifecycleService;
    private final GameplayCoreService gameplayCoreService;
    private final GameplayPolicyProperties gameplayPolicyProperties;

    public PlotManagementServiceImp(UserDao userDao,
                                    UserPlotDao userPlotDao,
                                    SoilTypeDao soilTypeDao,
                                    UserAssetFlowDao userAssetFlowDao,
                                    CropLifecycleService cropLifecycleService,
                                    GameplayCoreService gameplayCoreService,
                                    GameplayPolicyProperties gameplayPolicyProperties) {
        this.userDao = userDao;
        this.userPlotDao = userPlotDao;
        this.soilTypeDao = soilTypeDao;
        this.userAssetFlowDao = userAssetFlowDao;
        this.cropLifecycleService = cropLifecycleService;
        this.gameplayCoreService = gameplayCoreService;
        this.gameplayPolicyProperties = gameplayPolicyProperties;
    }

    @Override
    @Transactional
    public PlotUnlockResultVO unlockPlot(PlotUnlockDTO params) {
        ServiceGuard.requireNotNull(params, BizErrorCode.PARAM_INVALID, "请求参数不能为空");
        Long userId = ServiceGuard.requirePositive(params.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
        Long plotId = ServiceGuard.requirePositive(params.getPlotId(), BizErrorCode.PARAM_INVALID, "地块ID无效");

        User user = ServiceGuard.requirePresent(
                userDao.findByIdAndIsDeletedFalse(userId),
                BizErrorCode.USER_NOT_FOUND,
                "用户不存在"
        );
        UserPlot plot = ServiceGuard.requirePresent(
                userPlotDao.findByIdAndUserIdAndIsDeletedFalse(plotId, userId),
                BizErrorCode.PLOT_NOT_FOUND,
                "地块不存在"
        );
        if (!Boolean.TRUE.equals(plot.getIsLocked())) {
            throw new ServiceException(BizErrorCode.PLOT_ALREADY_UNLOCKED, "地块已解锁");
        }
        UserPlot nextUnlockPlot = findNextLockedPlot(userId);
        if (nextUnlockPlot != null && !nextUnlockPlot.getId().equals(plot.getId())) {
            throw new ServiceException(
                    BizErrorCode.PLOT_UNLOCK_ORDER_INVALID,
                    "请先解锁前置地块"
            );
        }

        long unlockCostCoin = calculateUnlockCostCoin(plot.getPlotIndex());
        OffsetDateTime now = OffsetDateTime.now();
        int coinUpdated = userDao.decreaseCoinIfEnough(userId, unlockCostCoin, userId, now);
        if (coinUpdated <= 0) {
            throw new ServiceException(BizErrorCode.COIN_NOT_ENOUGH, "金币不足");
        }
        User latestUser = ServiceGuard.requirePresent(
                userDao.findByIdAndIsDeletedFalse(userId),
                BizErrorCode.USER_NOT_FOUND,
                "用户不存在"
        );
        long afterCoin = safeLong(latestUser.getCoin());
        long beforeCoin = afterCoin + unlockCostCoin;

        plot.setIsLocked(false);
        plot.setUnlockedAt(now);
        plot.setLockReason(null);
        touchForUpdate(plot, userId, now);
        userPlotDao.save(plot);

        if (unlockCostCoin > 0) {
            userAssetFlowDao.save(buildAssetFlow(
                    userId,
                    "COIN",
                    "EXPENSE",
                    unlockCostCoin,
                    beforeCoin,
                    afterCoin,
                    "UNLOCK_PLOT",
                    plot.getId() + ":" + now.toEpochSecond(),
                    now,
                    "{\"plotId\":" + plot.getId() + ",\"plotIndex\":" + plot.getPlotIndex() + ",\"unlockCost\":" + unlockCostCoin + "}"
            ));
        }

        List<UserPlot> plots = userPlotDao.findByUserIdAndIsDeletedFalseOrderByPlotIndexAsc(userId);
        int totalPlots = plots.size();
        int unlockedPlots = 0;
        for (UserPlot userPlot : plots) {
            if (!Boolean.TRUE.equals(userPlot.getIsLocked())) {
                unlockedPlots++;
            }
        }

        PlotUnlockResultVO result = new PlotUnlockResultVO();
        result.setUserId(userId);
        result.setPlotId(plot.getId());
        result.setPlotIndex(plot.getPlotIndex());
        result.setUnlockCostCoin(unlockCostCoin);
        result.setBeforeCoin(beforeCoin);
        result.setAfterCoin(afterCoin);
        result.setTotalPlots(totalPlots);
        result.setUnlockedPlots(unlockedPlots);
        result.setLockedPlots(totalPlots - unlockedPlots);
        return result;
    }

    @Override
    @Transactional
    public PlotExpandResultVO expandPlot(PlotExpandDTO params) {
        ServiceGuard.requireNotNull(params, BizErrorCode.PARAM_INVALID, "请求参数不能为空");
        Long userId = ServiceGuard.requirePositive(params.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");

        User user = ServiceGuard.requirePresent(
                userDao.findByIdAndIsDeletedFalse(userId),
                BizErrorCode.USER_NOT_FOUND,
                "用户不存在"
        );

        SoilType soilType;
        if (params.getSoilTypeId() != null && params.getSoilTypeId() > 0) {
            soilType = ServiceGuard.requirePresent(
                    soilTypeDao.findByIdAndIsDeletedFalse(params.getSoilTypeId()),
                    BizErrorCode.SOIL_TYPE_NOT_FOUND,
                    "土壤类型不存在"
            );
        } else {
            soilType = ServiceGuard.requirePresent(
                    soilTypeDao.findFirstByIsDeletedFalseOrderByLevelAscIdAsc(),
                    BizErrorCode.SOIL_TYPE_NOT_FOUND,
                    "默认土壤类型未配置"
            );
        }

        List<UserPlot> currentPlots = userPlotDao.findByUserIdAndIsDeletedFalseOrderByPlotIndexAsc(userId);
        int currentTotalPlots = currentPlots.size();
        long expandCostCoin = calculateExpandCostCoin(currentTotalPlots);
        OffsetDateTime now = OffsetDateTime.now();
        int coinUpdated = userDao.decreaseCoinIfEnough(userId, expandCostCoin, userId, now);
        if (coinUpdated <= 0) {
            throw new ServiceException(BizErrorCode.COIN_NOT_ENOUGH, "金币不足");
        }
        User latestUser = ServiceGuard.requirePresent(
                userDao.findByIdAndIsDeletedFalse(userId),
                BizErrorCode.USER_NOT_FOUND,
                "用户不存在"
        );
        long afterCoin = safeLong(latestUser.getCoin());
        long beforeCoin = afterCoin + expandCostCoin;

        Optional<UserPlot> maxPlotOptional = userPlotDao.findTopByUserIdAndIsDeletedFalseOrderByPlotIndexDesc(userId);
        short nextPlotIndex = maxPlotOptional.map(UserPlot::getPlotIndex).map(value -> (short) (value + 1)).orElse((short) 1);

        UserPlot newPlot = new UserPlot();
        initNewEntity(newPlot, userId, now);
        newPlot.setUserId(userId);
        newPlot.setSoilTypeId(soilType.getId());
        newPlot.setPlotIndex(nextPlotIndex);
        newPlot.setIsLocked(false);
        newPlot.setUnlockedAt(now);
        newPlot.setLockReason(null);
        UserPlot savedPlot = userPlotDao.save(newPlot);

        if (expandCostCoin > 0) {
            userAssetFlowDao.save(buildAssetFlow(
                    userId,
                    "COIN",
                    "EXPENSE",
                    expandCostCoin,
                    beforeCoin,
                    afterCoin,
                    "EXPAND_PLOT",
                    savedPlot.getId() + ":" + now.toEpochSecond(),
                    now,
                    "{\"plotId\":" + savedPlot.getId() + ",\"plotIndex\":" + savedPlot.getPlotIndex() + ",\"expandCost\":" + expandCostCoin + "}"
            ));
        }

        int totalPlots = currentTotalPlots + 1;
        int unlockedPlots = 1;
        for (UserPlot userPlot : currentPlots) {
            if (!Boolean.TRUE.equals(userPlot.getIsLocked())) {
                unlockedPlots++;
            }
        }

        PlotExpandResultVO result = new PlotExpandResultVO();
        result.setUserId(userId);
        result.setPlotId(savedPlot.getId());
        result.setPlotIndex(savedPlot.getPlotIndex());
        result.setSoilTypeId(soilType.getId());
        result.setSoilName(gameplayCoreService.safeString(soilType.getName()));
        result.setExpandCostCoin(expandCostCoin);
        result.setBeforeCoin(beforeCoin);
        result.setAfterCoin(afterCoin);
        result.setTotalPlots(totalPlots);
        result.setUnlockedPlots(unlockedPlots);
        result.setLockedPlots(totalPlots - unlockedPlots);
        return result;
    }

    @Override
    public PlotStatusVO plotStatus(PlotStatusQueryDTO params) {
        Long userId = ServiceGuard.requirePositive(
                params == null ? null : params.getUserId(),
                BizErrorCode.PARAM_INVALID,
                "用户ID无效"
        );
        MyFarmOverviewDTO overviewDTO = new MyFarmOverviewDTO();
        overviewDTO.setUserId(userId);
        MyFarmOverviewVO overviewVO = cropLifecycleService.myFarmOverview(overviewDTO);
        PlotStatusVO result = new PlotStatusVO();
        result.setUserId(overviewVO.getUserId());
        result.setServerTime(overviewVO.getServerTime());
        result.setTotalPlots(overviewVO.getTotalPlots());
        result.setUnlockedPlots(overviewVO.getUnlockedPlots());
        result.setLockedPlots(overviewVO.getLockedPlots());
        result.setOccupiedPlots(overviewVO.getOccupiedPlots());
        result.setEmptyUnlockedPlots(overviewVO.getEmptyUnlockedPlots());
        result.setHarvestablePlots(overviewVO.getHarvestableCount());
        result.setNextExpandCostCoin(overviewVO.getNextExpandCostCoin());
        if (overviewVO.getPlots() != null) {
            for (var plot : overviewVO.getPlots()) {
                if (Boolean.TRUE.equals(plot.getLocked()) && Boolean.TRUE.equals(plot.getCanUnlock())) {
                    result.setNextUnlockPlotId(plot.getPlotId());
                    result.setNextUnlockPlotIndex(plot.getPlotIndex());
                    break;
                }
            }
        }
        result.setPlots(overviewVO.getPlots());
        return result;
    }

    @Override
    public PageResult<PlotTradeRecordVO> pagePlotTrades(PlotTradeQueryDTO params) {
        PlotTradeQueryDTO request = params == null ? new PlotTradeQueryDTO() : params;
        Long userId = ServiceGuard.requirePositive(request.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
        ServiceGuard.requirePresent(
                userDao.findByIdAndIsDeletedFalse(userId),
                BizErrorCode.USER_NOT_FOUND,
                "用户不存在"
        );

        int pageNo = gameplayCoreService.normalizePageNo(request.getPage());
        int pageSize = gameplayCoreService.normalizePageSize(request.getRows());
        String bizTypeFilter = gameplayCoreService.normalizePlotBizType(request.getBizType());
        if (request.getBizType() != null && !request.getBizType().isBlank() && bizTypeFilter.isBlank()) {
            throw new ServiceException(BizErrorCode.PLOT_BIZ_TYPE_UNSUPPORTED, "bizType 仅支持 UNLOCK_PLOT 或 EXPAND_PLOT");
        }

        List<UserAssetFlow> flows = userAssetFlowDao.findByUserIdAndIsDeletedFalseOrderByOccurredAtDesc(userId);
        List<PlotTradeRecordVO> records = new ArrayList<>();
        for (UserAssetFlow flow : flows) {
            String bizType = gameplayCoreService.safeString(flow.getBizType()).trim().toUpperCase();
            if (!gameplayCoreService.isPlotBizType(bizType)) {
                continue;
            }
            if (!bizTypeFilter.isBlank() && !bizTypeFilter.equals(bizType)) {
                continue;
            }

            PlotTradeRecordVO record = new PlotTradeRecordVO();
            record.setBizId(gameplayCoreService.safeString(flow.getBizId()));
            record.setBizType(bizType);
            record.setBizTypeLabel("UNLOCK_PLOT".equals(bizType) ? "UNLOCK" : "EXPAND");
            record.setCoinChangeAmount(gameplayCoreService.safeLong(flow.getChangeAmount()));
            record.setCoinOperationType(gameplayCoreService.safeString(flow.getOperationType()));
            record.setBeforeCoin(gameplayCoreService.safeLong(flow.getBeforeAmount()));
            record.setAfterCoin(gameplayCoreService.safeLong(flow.getAfterAmount()));
            record.setOccurredAt(flow.getOccurredAt());

            Long plotId = gameplayCoreService.extractLongFromExtData(flow.getExtData(), "plotId");
            Long plotIndex = gameplayCoreService.extractLongFromExtData(flow.getExtData(), "plotIndex");
            record.setPlotId(plotId);
            record.setPlotIndex(plotIndex == null ? null : plotIndex.shortValue());
            records.add(record);
        }

        long total = records.size();
        int fromIndex = Math.min((pageNo - 1) * pageSize, records.size());
        int toIndex = Math.min(fromIndex + pageSize, records.size());
        List<PlotTradeRecordVO> pageRecords = records.subList(fromIndex, toIndex);
        return new PageResult<>((long) pageNo, (long) pageSize, total, pageRecords);
    }

    @Override
    public List<PlotTradeBizTypeOptionVO> listPlotTradeBizTypeOptions() {
        List<PlotTradeBizTypeOptionVO> result = new ArrayList<>();
        PlotTradeBizTypeOptionVO unlock = new PlotTradeBizTypeOptionVO();
        unlock.setBizType("UNLOCK_PLOT");
        unlock.setText("解锁地块");
        result.add(unlock);

        PlotTradeBizTypeOptionVO expand = new PlotTradeBizTypeOptionVO();
        expand.setBizType("EXPAND_PLOT");
        expand.setText("扩展地块");
        result.add(expand);
        return result;
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

    private UserAssetFlow buildAssetFlow(Long userId,
                                         String assetType,
                                         String operationType,
                                         Long changeAmount,
                                         Long beforeAmount,
                                         Long afterAmount,
                                         String bizType,
                                         String bizId,
                                         OffsetDateTime now,
                                         String extData) {
        UserAssetFlow flow = new UserAssetFlow();
        initNewEntity(flow, userId, now);
        flow.setUserId(userId);
        flow.setAssetType(assetType);
        flow.setOperationType(operationType);
        flow.setChangeAmount(changeAmount);
        flow.setBeforeAmount(beforeAmount);
        flow.setAfterAmount(afterAmount);
        flow.setBizType(bizType);
        flow.setBizId(bizId);
        flow.setOccurredAt(now);
        flow.setExtData(extData);
        return flow;
    }

    private void initNewEntity(BaseEntity entity, Long operatorId, OffsetDateTime now) {
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy(operatorId);
        entity.setUpdatedBy(operatorId);
        entity.setStatus((short) 1);
        entity.setIsDeleted(false);
        entity.setOptLockVersion(0);
    }

    private void touchForUpdate(BaseEntity entity, Long operatorId, OffsetDateTime now) {
        entity.setUpdatedAt(now);
        entity.setUpdatedBy(operatorId);
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private UserPlot findNextLockedPlot(Long userId) {
        List<UserPlot> plots = userPlotDao.findByUserIdAndIsDeletedFalseOrderByPlotIndexAsc(userId);
        for (UserPlot item : plots) {
            if (Boolean.TRUE.equals(item.getIsLocked())) {
                return item;
            }
        }
        return null;
    }
}
