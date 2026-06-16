package cn.jxufe.farm.service.imp;

import cn.jxufe.farm.bean.dto.MyFarmOverviewDTO;
import cn.jxufe.farm.bean.dto.PlotExpandDTO;
import cn.jxufe.farm.bean.dto.PlotExpandOptionsQueryDTO;
import cn.jxufe.farm.bean.dto.PlotStatusQueryDTO;
import cn.jxufe.farm.bean.dto.PlotTradeQueryDTO;
import cn.jxufe.farm.bean.dto.PlotUnlockDTO;
import cn.jxufe.farm.bean.vo.MyFarmOverviewVO;
import cn.jxufe.farm.bean.vo.PlotExpandOptionVO;
import cn.jxufe.farm.bean.vo.PlotExpandOptionsVO;
import cn.jxufe.farm.bean.vo.PlotExpandResultVO;
import cn.jxufe.farm.bean.vo.PlotStatusVO;
import cn.jxufe.farm.bean.vo.PlotTradeBizTypeOptionVO;
import cn.jxufe.farm.bean.vo.PlotTradeRecordVO;
import cn.jxufe.farm.bean.vo.PlotUnlockResultVO;
import cn.jxufe.farm.common.enums.BizErrorCode;
import cn.jxufe.farm.common.exceptions.ServiceException;
import cn.jxufe.farm.common.pages.PageResult;
import cn.jxufe.farm.common.utils.ServiceGuardUtils;
import cn.jxufe.farm.config.properties.GameplayPolicyProperties;
import cn.jxufe.farm.dao.SoilTypeDao;
import cn.jxufe.farm.dao.UserAssetFlowDao;
import cn.jxufe.farm.dao.UserDao;
import cn.jxufe.farm.dao.UserPlotDao;
import cn.jxufe.farm.entity.SoilType;
import cn.jxufe.farm.entity.User;
import cn.jxufe.farm.entity.UserPlot;
import cn.jxufe.farm.service.CropLifecycleService;
import cn.jxufe.farm.service.GameplayCoreService;
import cn.jxufe.farm.service.PlotCostService;
import cn.jxufe.farm.service.PlotManagementService;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PlotManagementServiceImp implements PlotManagementService {

  private final UserDao userDao;

  private final UserPlotDao userPlotDao;

  private final SoilTypeDao soilTypeDao;

  private final UserAssetFlowDao userAssetFlowDao;

  private final CropLifecycleService cropLifecycleService;

  private final GameplayCoreService gameplayCoreService;

  private final PlotCostService plotCostService;

  private final GameplayPolicyProperties gameplayPolicyProperties;

  public PlotManagementServiceImp(
      UserDao userDao,
      UserPlotDao userPlotDao,
      SoilTypeDao soilTypeDao,
      UserAssetFlowDao userAssetFlowDao,
      CropLifecycleService cropLifecycleService,
      GameplayCoreService gameplayCoreService,
      PlotCostService plotCostService,
      GameplayPolicyProperties gameplayPolicyProperties) {
    this.userDao = userDao;
    this.userPlotDao = userPlotDao;
    this.soilTypeDao = soilTypeDao;
    this.userAssetFlowDao = userAssetFlowDao;
    this.cropLifecycleService = cropLifecycleService;
    this.gameplayCoreService = gameplayCoreService;
    this.plotCostService = plotCostService;
    this.gameplayPolicyProperties = gameplayPolicyProperties;
  }

  /* =========================================================
   *  Context Helper (提取冗余的校验与扣款逻辑)
   * ========================================================= */

  private class UserActionContext {
    final Long userId;
    final OffsetDateTime now;
    final long currentExperience;
    final long currentCoin;

    UserActionContext(Long rawUserId) {
      this.userId =
          ServiceGuardUtils.requirePositive(rawUserId, BizErrorCode.PARAM_INVALID, "用户ID无效");
      User user = validateUserExists();
      this.currentExperience = gameplayCoreService.safeLong(user.getExperience());
      this.currentCoin = gameplayCoreService.safeLong(user.getCoin());
      this.now = OffsetDateTime.now();
    }

    void deductCoin(long cost) {
      if (cost > 0 && userDao.decreaseCoinIfEnough(userId, cost, userId, now) <= 0) {
        throw new ServiceException(BizErrorCode.COIN_NOT_ENOUGH, "金币不足");
      }
    }

    long getLatestCoin() {
      User user =
          ServiceGuardUtils.requirePresent(
              userDao.findByIdAndIsDeletedFalse(userId), BizErrorCode.USER_NOT_FOUND, "用户不存在");
      return gameplayCoreService.safeLong(user.getCoin());
    }

    private User validateUserExists() {
      return ServiceGuardUtils.requirePresent(
          userDao.findByIdAndIsDeletedFalse(userId), BizErrorCode.USER_NOT_FOUND, "用户不存在");
    }
  }

  /* =========================================================
   *  Public Methods
   * ========================================================= */

  @Override
  @Transactional
  public PlotUnlockResultVO unlockPlot(PlotUnlockDTO params) {
    ServiceGuardUtils.requireNotNull(params, BizErrorCode.PARAM_INVALID, "请求参数不能为空");
    Long plotId =
        ServiceGuardUtils.requirePositive(params.getPlotId(), BizErrorCode.PARAM_INVALID, "地块ID无效");
    UserActionContext ctx = new UserActionContext(params.getUserId());

    UserPlot plot =
        ServiceGuardUtils.requirePresent(
            userPlotDao.findByIdAndUserIdAndIsDeletedFalse(plotId, ctx.userId),
            BizErrorCode.PLOT_NOT_FOUND,
            "地块不存在");
    if (isUnlocked(plot)) {
      throw new ServiceException(BizErrorCode.PLOT_ALREADY_UNLOCKED, "地块已解锁");
    }
    long unlockRequiredExperience =
        gameplayCoreService.safeLong(plot.getUnlockExperienceRequired());
    if (ctx.currentExperience < unlockRequiredExperience) {
      throw new ServiceException(BizErrorCode.EXPERIENCE_NOT_ENOUGH, "经验不足，无法解锁该地块");
    }

    UserPlot nextUnlockPlot = findNextLockedPlot(ctx.userId);
    if (nextUnlockPlot != null && !nextUnlockPlot.getId().equals(plot.getId())) {
      throw new ServiceException(BizErrorCode.PLOT_UNLOCK_ORDER_INVALID, "请先解锁前置地块");
    }

    long unlockCostCoin = plotCostService.calculateUnlockCostCoin(plot.getPlotIndex());
    ctx.deductCoin(unlockCostCoin);
    long afterCoin = ctx.getLatestCoin();
    long beforeCoin = afterCoin + unlockCostCoin;

    plot.setIsLocked(false);
    plot.setUnlockedAt(ctx.now);
    plot.setLockReason(null);
    gameplayCoreService.touchForUpdate(plot, ctx.userId, ctx.now);
    userPlotDao.save(plot);

    if (unlockCostCoin > 0) {
      userAssetFlowDao.save(
          gameplayCoreService.buildAssetFlow(
              ctx.userId,
              "COIN",
              "EXPENSE",
              unlockCostCoin,
              beforeCoin,
              afterCoin,
              "UNLOCK_PLOT",
              plot.getId() + ":" + ctx.now.toEpochSecond(),
              ctx.now,
              "{\"plotId\":"
                  + plot.getId()
                  + ",\"plotIndex\":"
                  + plot.getPlotIndex()
                  + ",\"unlockCost\":"
                  + unlockCostCoin
                  + "}"));
    }

    List<UserPlot> plots = userPlotDao.findByUserIdAndIsDeletedFalseOrderByPlotIndexAsc(ctx.userId);
    int totalPlots = plots.size();
    int unlockedPlots = (int) plots.stream().filter(this::isUnlocked).count();

    PlotUnlockResultVO result = new PlotUnlockResultVO();
    result.setUserId(ctx.userId);
    result.setPlotId(plot.getId());
    result.setPlotIndex(plot.getPlotIndex());
    result.setUnlockRequiredExperience(unlockRequiredExperience);
    result.setCurrentExperience(ctx.currentExperience);
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
    ServiceGuardUtils.requireNotNull(params, BizErrorCode.PARAM_INVALID, "请求参数不能为空");
    UserActionContext ctx = new UserActionContext(params.getUserId());

    SoilType soilType =
        (params.getSoilTypeId() != null && params.getSoilTypeId() > 0)
            ? ServiceGuardUtils.requirePresent(
                soilTypeDao.findByIdAndIsDeletedFalse(params.getSoilTypeId()),
                BizErrorCode.SOIL_TYPE_NOT_FOUND,
                "土壤类型不存在")
            : ServiceGuardUtils.requirePresent(
                soilTypeDao.findFirstByIsDeletedFalseOrderByLevelAscIdAsc(),
                BizErrorCode.SOIL_TYPE_NOT_FOUND,
                "默认土壤类型未配置");

    long soilUnlockRequiredExperience =
        gameplayCoreService.safeLong(soilType.getUnlockExperienceRequired());
    if (ctx.currentExperience < soilUnlockRequiredExperience) {
      throw new ServiceException(BizErrorCode.EXPERIENCE_NOT_ENOUGH, "经验不足，无法扩展为该品质土地");
    }

    List<UserPlot> currentPlots =
        userPlotDao.findByUserIdAndIsDeletedFalseOrderByPlotIndexAsc(ctx.userId);
    int currentTotalPlots = currentPlots.size();

    long expandCostCoin = soilType.getExpandCostCoin() != null ? soilType.getExpandCostCoin() : 0L;
    ctx.deductCoin(expandCostCoin);
    long afterCoin = ctx.getLatestCoin();
    long beforeCoin = afterCoin + expandCostCoin;

    short nextPlotIndex =
        currentPlots.stream()
            .map(UserPlot::getPlotIndex)
            .max(Short::compareTo)
            .map(val -> (short) (val + 1))
            .orElse((short) 1);

    UserPlot newPlot = new UserPlot();
    gameplayCoreService.initNewEntity(newPlot, ctx.userId, ctx.now);
    newPlot.setUserId(ctx.userId);
    newPlot.setSoilTypeId(soilType.getId());
    newPlot.setPlotIndex(nextPlotIndex);
    newPlot.setUnlockExperienceRequired(calculatePlotUnlockRequiredExperience(nextPlotIndex));
    newPlot.setIsLocked(true);
    newPlot.setUnlockedAt(null);
    newPlot.setLockReason("待解锁");
    UserPlot savedPlot = userPlotDao.save(newPlot);

    if (expandCostCoin > 0) {
      userAssetFlowDao.save(
          gameplayCoreService.buildAssetFlow(
              ctx.userId,
              "COIN",
              "EXPENSE",
              expandCostCoin,
              beforeCoin,
              afterCoin,
              "EXPAND_PLOT",
              savedPlot.getId() + ":" + ctx.now.toEpochSecond(),
              ctx.now,
              "{\"plotId\":"
                  + savedPlot.getId()
                  + ",\"plotIndex\":"
                  + savedPlot.getPlotIndex()
                  + ",\"expandCost\":"
                  + expandCostCoin
                  + "}"));
    }

    int totalPlots = currentTotalPlots + 1;
    int unlockedPlots = (int) currentPlots.stream().filter(this::isUnlocked).count();

    PlotExpandResultVO result = new PlotExpandResultVO();
    result.setUserId(ctx.userId);
    result.setPlotId(savedPlot.getId());
    result.setPlotIndex(savedPlot.getPlotIndex());
    result.setSoilTypeId(soilType.getId());
    result.setSoilName(gameplayCoreService.safeString(soilType.getName()));
    result.setSoilUnlockRequiredExperience(soilUnlockRequiredExperience);
    result.setCurrentExperience(ctx.currentExperience);
    result.setExpandCostCoin(expandCostCoin);
    result.setBeforeCoin(beforeCoin);
    result.setAfterCoin(afterCoin);
    result.setTotalPlots(totalPlots);
    result.setUnlockedPlots(unlockedPlots);
    result.setLockedPlots(totalPlots - unlockedPlots);
    return result;
  }

  @Override
  public PlotExpandOptionsVO listPlotExpandOptions(PlotExpandOptionsQueryDTO params) {
    ServiceGuardUtils.requireNotNull(params, BizErrorCode.PARAM_INVALID, "请求参数不能为空");
    UserActionContext ctx = new UserActionContext(params.getUserId());

    List<UserPlot> currentPlots =
        userPlotDao.findByUserIdAndIsDeletedFalseOrderByPlotIndexAsc(ctx.userId);
    int currentTotalPlots = currentPlots.size();
    short nextPlotIndex =
        currentPlots.stream()
            .map(UserPlot::getPlotIndex)
            .max(Short::compareTo)
            .map(val -> (short) (val + 1))
            .orElse((short) 1);

    List<PlotExpandOptionVO> options =
        soilTypeDao.findByIsDeletedFalseOrderByIdAsc().stream()
            .map(item -> buildPlotExpandOption(item, ctx))
            .collect(Collectors.toList());

    // For summary, use the cheapest available expand cost
    long minExpandCostCoin =
        options.stream()
            .map(PlotExpandOptionVO::getExpandCostCoin)
            .filter(cost -> cost != null)
            .mapToLong(Long::longValue)
            .min()
            .orElse(0L);

    PlotExpandOptionsVO result = new PlotExpandOptionsVO();
    result.setUserId(ctx.userId);
    result.setCurrentExperience(ctx.currentExperience);
    result.setCurrentCoin(ctx.currentCoin);
    result.setCurrentTotalPlots(currentTotalPlots);
    result.setNextPlotIndex(nextPlotIndex);
    result.setExpandCostCoin(minExpandCostCoin);
    result.setOptions(options);
    return result;
  }

  @Override
  public PlotStatusVO plotStatus(PlotStatusQueryDTO params) {
    Long userId =
        ServiceGuardUtils.requirePositive(
            params == null ? null : params.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");

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
    User user =
        ServiceGuardUtils.requirePresent(
            userDao.findByIdAndIsDeletedFalse(userId), BizErrorCode.USER_NOT_FOUND, "用户不存在");
    result.setCurrentExperience(gameplayCoreService.safeLong(user.getExperience()));
    result.setNextExpandCostCoin(overviewVO.getNextExpandCostCoin());
    result.setPlots(overviewVO.getPlots());

    if (overviewVO.getPlots() != null) {
      overviewVO.getPlots().stream()
          .filter(
              plot ->
                  Boolean.TRUE.equals(plot.getLocked()) && Boolean.TRUE.equals(plot.getCanUnlock()))
          .findFirst()
          .ifPresent(
              plot -> {
                result.setNextUnlockPlotId(plot.getPlotId());
                result.setNextUnlockPlotIndex(plot.getPlotIndex());
                result.setNextUnlockRequiredExperience(plot.getUnlockRequiredExperience());
                result.setNextUnlockableByExperience(plot.getUnlockableByExperience());
              });
    }
    return result;
  }

  @Override
  public PageResult<PlotTradeRecordVO> pagePlotTrades(PlotTradeQueryDTO params) {
    PlotTradeQueryDTO request = params == null ? new PlotTradeQueryDTO() : params;
    Long userId =
        ServiceGuardUtils.requirePositive(
            request.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
    ServiceGuardUtils.requirePresent(
        userDao.findByIdAndIsDeletedFalse(userId), BizErrorCode.USER_NOT_FOUND, "用户不存在");

    int pageNo = gameplayCoreService.normalizePageNo(request.getPage());
    int pageSize = gameplayCoreService.normalizePageSize(request.getRows());
    String bizTypeFilter = gameplayCoreService.normalizePlotBizType(request.getBizType());

    if (request.getBizType() != null
        && !request.getBizType().isBlank()
        && bizTypeFilter.isBlank()) {
      throw new ServiceException(
          BizErrorCode.PLOT_BIZ_TYPE_UNSUPPORTED, "bizType 仅支持 UNLOCK_PLOT 或 EXPAND_PLOT");
    }

    List<PlotTradeRecordVO> records =
        userAssetFlowDao.findByUserIdAndIsDeletedFalseOrderByOccurredAtDesc(userId).stream()
            .filter(
                flow -> {
                  String bizType =
                      gameplayCoreService.safeString(flow.getBizType()).trim().toUpperCase();
                  return gameplayCoreService.isPlotBizType(bizType)
                      && (bizTypeFilter.isBlank() || bizTypeFilter.equals(bizType));
                })
            .map(
                flow -> {
                  String bizType =
                      gameplayCoreService.safeString(flow.getBizType()).trim().toUpperCase();
                  PlotTradeRecordVO record = new PlotTradeRecordVO();
                  record.setBizId(gameplayCoreService.safeString(flow.getBizId()));
                  record.setBizType(bizType);
                  record.setBizTypeLabel("UNLOCK_PLOT".equals(bizType) ? "UNLOCK" : "EXPAND");
                  record.setCoinChangeAmount(gameplayCoreService.safeLong(flow.getChangeAmount()));
                  record.setCoinOperationType(
                      gameplayCoreService.safeString(flow.getOperationType()));
                  record.setBeforeCoin(gameplayCoreService.safeLong(flow.getBeforeAmount()));
                  record.setAfterCoin(gameplayCoreService.safeLong(flow.getAfterAmount()));
                  record.setOccurredAt(flow.getOccurredAt());

                  Long plotId =
                      gameplayCoreService.extractLongFromExtData(flow.getExtData(), "plotId");
                  Long plotIndex =
                      gameplayCoreService.extractLongFromExtData(flow.getExtData(), "plotIndex");
                  record.setPlotId(plotId);
                  record.setPlotIndex(plotIndex == null ? null : plotIndex.shortValue());
                  return record;
                })
            .collect(Collectors.toList());

    return PageResult.of(records, pageNo, pageSize);
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

  /* =========================================================
   *  Private Utility Helpers
   * ========================================================= */

  private long calculatePlotUnlockRequiredExperience(short plotIndex) {
    short initialUnlocked = gameplayPolicyProperties.getPlot().getDefaults().getUnlockedPlotCount();
    if (plotIndex <= initialUnlocked) {
      return 0L;
    }
    long base = gameplayPolicyProperties.getPlot().getUnlock().getBaseRequiredExperience();
    long step = gameplayPolicyProperties.getPlot().getUnlock().getRequiredExperienceStep();
    long stepTimes = Math.max(0, plotIndex - initialUnlocked - 1);
    return Math.max(0L, base + stepTimes * step);
  }

  private PlotExpandOptionVO buildPlotExpandOption(SoilType soilType, UserActionContext ctx) {
    long expandCostCoin = soilType.getExpandCostCoin() != null ? soilType.getExpandCostCoin() : 0L;
    long unlockExperienceRequired =
        gameplayCoreService.safeLong(soilType.getUnlockExperienceRequired());
    boolean unlockableByExperience = ctx.currentExperience >= unlockExperienceRequired;
    boolean unlockableByCoin = ctx.currentCoin >= expandCostCoin;

    PlotExpandOptionVO option = new PlotExpandOptionVO();
    option.setSoilTypeId(soilType.getId());
    option.setSoilName(gameplayCoreService.safeString(soilType.getName()));
    option.setSoilBitCode(soilType.getBitCode());
    option.setSoilLevel(soilType.getLevel());
    option.setCoverImageUrl(gameplayCoreService.safeString(soilType.getCoverImageUrl()));
    option.setDescription(gameplayCoreService.safeString(soilType.getDescription()));
    option.setUnlockExperienceRequired(unlockExperienceRequired);
    option.setExpandCostCoin(expandCostCoin);
    option.setUnlockableByExperience(unlockableByExperience);
    option.setUnlockableByCoin(unlockableByCoin);
    option.setExpandable(unlockableByExperience && unlockableByCoin);
    return option;
  }

  private UserPlot findNextLockedPlot(Long userId) {
    return userPlotDao.findByUserIdAndIsDeletedFalseOrderByPlotIndexAsc(userId).stream()
        .filter(this::isLocked)
        .findFirst()
        .orElse(null);
  }

  private boolean isLocked(UserPlot plot) {
    return Boolean.TRUE.equals(plot.getIsLocked());
  }

  private boolean isUnlocked(UserPlot plot) {
    return !isLocked(plot);
  }
}
