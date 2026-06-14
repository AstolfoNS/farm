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
import cn.jxufe.farm.common.enums.BizErrorCode;
import cn.jxufe.farm.common.exception.ServiceException;
import cn.jxufe.farm.common.pages.PageResult;
import cn.jxufe.farm.common.utils.ServiceGuardUtils;
import cn.jxufe.farm.dao.GrowthStageDao;
import cn.jxufe.farm.dao.SeedGrowthStageDao;
import cn.jxufe.farm.dao.SeedQualityDao;
import cn.jxufe.farm.dao.SeedTypeDao;
import cn.jxufe.farm.dao.SoilTypeDao;
import cn.jxufe.farm.dao.UserAssetFlowDao;
import cn.jxufe.farm.dao.UserDao;
import cn.jxufe.farm.dao.UserFruitDao;
import cn.jxufe.farm.dao.UserInventoryFlowDao;
import cn.jxufe.farm.dao.UserSeedDao;
import cn.jxufe.farm.entity.GrowthStage;
import cn.jxufe.farm.entity.RequestIdempotency;
import cn.jxufe.farm.entity.SeedGrowthStage;
import cn.jxufe.farm.entity.SeedQuality;
import cn.jxufe.farm.entity.SeedType;
import cn.jxufe.farm.entity.SoilType;
import cn.jxufe.farm.entity.User;
import cn.jxufe.farm.entity.UserAssetFlow;
import cn.jxufe.farm.entity.UserFruit;
import cn.jxufe.farm.entity.UserInventoryFlow;
import cn.jxufe.farm.entity.UserSeed;
import cn.jxufe.farm.service.GameplayCoreService;
import cn.jxufe.farm.service.RequestIdempotencyService;
import cn.jxufe.farm.service.SeedService;
import cn.jxufe.farm.service.support.SeedStageConfigValidator;
import cn.jxufe.farm.service.support.SeedViewAssembler;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class SeedServiceImp implements SeedService {
  private final SeedTypeDao seedTypeDao;
  private final SeedQualityDao seedQualityDao;
  private final SoilTypeDao soilTypeDao;
  private final GrowthStageDao growthStageDao;
  private final SeedGrowthStageDao seedGrowthStageDao;
  private final UserDao userDao;
  private final UserSeedDao userSeedDao;
  private final UserFruitDao userFruitDao;
  private final UserAssetFlowDao userAssetFlowDao;
  private final UserInventoryFlowDao userInventoryFlowDao;
  private final RequestIdempotencyService requestIdempotencyService;
  private final GameplayCoreService gameplayCoreService;
  private final SeedViewAssembler seedViewAssembler;

  public SeedServiceImp(
      SeedTypeDao seedTypeDao,
      SeedQualityDao seedQualityDao,
      SoilTypeDao soilTypeDao,
      GrowthStageDao growthStageDao,
      SeedGrowthStageDao seedGrowthStageDao,
      UserDao userDao,
      UserSeedDao userSeedDao,
      UserFruitDao userFruitDao,
      UserAssetFlowDao userAssetFlowDao,
      UserInventoryFlowDao userInventoryFlowDao,
      RequestIdempotencyService requestIdempotencyService,
      GameplayCoreService gameplayCoreService,
      SeedViewAssembler seedViewAssembler) {
    this.seedTypeDao = seedTypeDao;
    this.seedQualityDao = seedQualityDao;
    this.soilTypeDao = soilTypeDao;
    this.growthStageDao = growthStageDao;
    this.seedGrowthStageDao = seedGrowthStageDao;
    this.userDao = userDao;
    this.userSeedDao = userSeedDao;
    this.userFruitDao = userFruitDao;
    this.userAssetFlowDao = userAssetFlowDao;
    this.userInventoryFlowDao = userInventoryFlowDao;
    this.requestIdempotencyService = requestIdempotencyService;
    this.gameplayCoreService = gameplayCoreService;
    this.seedViewAssembler = seedViewAssembler;
  }

  @Override
  public PageResult<SeedGridVO> pageSeedTypes(SeedTypeQueryDTO query) {
    SeedTypeQueryDTO request = query == null ? new SeedTypeQueryDTO() : query;
    int pageNo = gameplayCoreService.normalizePageNo(request.getPage());
    int pageSize = gameplayCoreService.normalizePageSize(request.getRows());
    Sort.Direction direction =
        "desc".equalsIgnoreCase(request.getOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
    Pageable pageable =
        PageRequest.of(
            pageNo - 1, pageSize, Sort.by(direction, safeSeedSortField(request.getSort())));
    Page<SeedType> seedPage =
        seedTypeDao.findByIsDeletedFalseAndNameContainingIgnoreCase(
            gameplayCoreService.safeString(request.getName()).trim(), pageable);
    Map<Long, SeedQuality> qualityMap = buildSeedQualityMap();
    Map<Integer, String> soilNameByBitCode = buildSoilNameByBitCode();
    Map<Long, Integer> totalGrowSecondsMap = buildTotalGrowSecondsMap(seedPage.getContent());
    List<SeedGridVO> records =
        seedPage.getContent().stream()
            .map(
                seedType ->
                    seedViewAssembler.seedGrid(
                        seedType, qualityMap, soilNameByBitCode, totalGrowSecondsMap))
            .collect(Collectors.toList());
    return new PageResult<>(pageNo, pageSize, seedPage.getTotalElements(), records);
  }

  @Override
  public PageResult<SeedShopItemVO> pageSeedShop(SeedShopQueryDTO query) {
    SeedShopQueryDTO request = query == null ? new SeedShopQueryDTO() : query;
    int pageNo = gameplayCoreService.normalizePageNo(request.getPage());
    int pageSize = gameplayCoreService.normalizePageSize(request.getRows());
    Map<Long, SeedQuality> qualityMap = buildSeedQualityMap();
    Map<Integer, String> soilNameByBitCode = buildSoilNameByBitCode();
    String nameKeyword = gameplayCoreService.safeString(request.getName()).trim().toLowerCase();
    List<SeedType> matchedSeedTypes =
        seedTypeDao.findByIsDeletedFalseOrderByIdAsc().stream()
            .filter(
                s ->
                    nameKeyword.isEmpty()
                        || gameplayCoreService
                            .safeString(s.getName())
                            .toLowerCase()
                            .contains(nameKeyword))
            .filter(
                s ->
                    request.getSeedQualityId() == null
                        || request.getSeedQualityId().equals(s.getSeedQualityId()))
            .filter(s -> request.getLevel() == null || request.getLevel().equals(s.getLevel()))
            .collect(Collectors.toList());
    List<SeedShopItemVO> allItems =
        buildReadySeedShopItems(
                filterShopReadySeedTypes(matchedSeedTypes), qualityMap, soilNameByBitCode)
            .stream()
            .sorted(resolveShopComparator(request.getSort(), request.getOrder()))
            .collect(Collectors.toList());
    return PageResult.of(allItems, pageNo, pageSize);
  }

  @Override
  @Transactional
  public SeedShopBuyResultVO buySeed(SeedShopBuyDTO params) {
    ServiceGuardUtils.requireNotNull(params, BizErrorCode.PARAM_INVALID, "请求参数不能为空");
    Long userId =
        ServiceGuardUtils.requirePositive(params.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
    Long seedTypeId =
        ServiceGuardUtils.requirePositive(
            params.getSeedTypeId(), BizErrorCode.PARAM_INVALID, "种子类型ID无效");
    long buyQuantity = gameplayCoreService.defaultLong(params.getQuantity(), 0L);
    if (buyQuantity <= 0) {
      throw new ServiceException(BizErrorCode.AMOUNT_INVALID, "购买数量必须大于0");
    }
    SeedShopBuyResultVO cached =
        requestIdempotencyService.getCachedSuccessResult(
            userId, "BUY_SEED", params.getRequestId(), SeedShopBuyResultVO.class);
    if (cached != null) {
      return cached;
    }
    RequestIdempotency idempotency =
        requestIdempotencyService.claimProcessing(userId, "BUY_SEED", params.getRequestId());
    try {
      validateUser(userId);
      SeedType seedType = validateSeedType(seedTypeId);
      requireSeedTypeReadyForShop(seedType);
      long unitPrice = gameplayCoreService.defaultLong(seedType.getPrice(), 0L);
      long totalCost = gameplayCoreService.safeMultiply(unitPrice, buyQuantity);
      if (totalCost == Long.MAX_VALUE)
        throw new ServiceException(BizErrorCode.AMOUNT_OVERFLOW, "购买金额计算溢出");
      OffsetDateTime now = OffsetDateTime.now();
      if (userDao.decreaseCoinIfEnough(userId, totalCost, userId, now) <= 0) {
        throw new ServiceException(BizErrorCode.COIN_NOT_ENOUGH, "金币不足");
      }
      long afterCoin = gameplayCoreService.defaultLong(validateUser(userId).getCoin(), 0L);
      long beforeCoin = afterCoin + totalCost;
      UserSeed userSeed =
          userSeedDao.findByUserIdAndSeedTypeIdAndIsDeletedFalse(userId, seedTypeId).orElse(null);
      long frozenSeedQuantity;
      long beforeSeedQuantity;
      long afterSeedQuantity;
      if (userSeed == null) {
        UserSeed entity = new UserSeed();
        gameplayCoreService.initNewEntity(entity, userId, now);
        entity.setUserId(userId);
        entity.setSeedTypeId(seedTypeId);
        entity.setQuantity(buyQuantity);
        entity.setFrozenQuantity(0L);
        UserSeed savedSeed = userSeedDao.save(entity);
        frozenSeedQuantity = 0L;
        beforeSeedQuantity = 0L;
        afterSeedQuantity = gameplayCoreService.defaultLong(savedSeed.getQuantity(), 0L);

      } else {
        frozenSeedQuantity = gameplayCoreService.defaultLong(userSeed.getFrozenQuantity(), 0L);
        if (userSeedDao.increaseQuantity(userSeed.getId(), buyQuantity, userId, now) <= 0) {
          throw new ServiceException(BizErrorCode.SEED_INVENTORY_NOT_FOUND, "种子库存不存在");
        }
        afterSeedQuantity =
            gameplayCoreService.defaultLong(validateUserSeed(userId, seedTypeId).getQuantity(), 0L);
        beforeSeedQuantity = afterSeedQuantity - buyQuantity;
      }
      String bizId = seedTypeId + ":" + now.toEpochSecond();
      userAssetFlowDao.save(
          gameplayCoreService.buildAssetFlow(
              userId,
              "COIN",
              "EXPENSE",
              totalCost,
              beforeCoin,
              afterCoin,
              "BUY_SEED",
              bizId,
              now,
              "{\"seedTypeId\":" + seedTypeId + ",\"buyQuantity\":" + buyQuantity + "}"));
      userInventoryFlowDao.save(
          gameplayCoreService.buildInventoryFlow(
              userId,
              "SEED",
              seedTypeId,
              "INCOME",
              buyQuantity,
              beforeSeedQuantity,
              afterSeedQuantity,
              frozenSeedQuantity,
              frozenSeedQuantity,
              "BUY_SEED",
              bizId,
              now,
              "{\"seedTypeId\":" + seedTypeId + ",\"totalCostCoin\":" + totalCost + "}"));
      SeedShopBuyResultVO result = new SeedShopBuyResultVO();
      result.setUserId(userId);
      result.setSeedTypeId(seedTypeId);
      result.setSeedName(seedType.getName());
      result.setBuyQuantity(buyQuantity);
      result.setUnitPrice(unitPrice);
      result.setTotalCostCoin(totalCost);
      result.setBeforeCoin(beforeCoin);
      result.setAfterCoin(afterCoin);
      result.setBeforeSeedQuantity(beforeSeedQuantity);
      result.setAfterSeedQuantity(afterSeedQuantity);
      requestIdempotencyService.markSuccess(idempotency.getId(), result);
      return result;

    } catch (RuntimeException ex) {
      requestIdempotencyService.markFailed(idempotency.getId(), ex.getMessage());
      throw ex;
    }
  }

  @Override
  @Transactional
  public SeedShopSellFruitResultVO sellFruit(SeedShopSellFruitDTO params) {
    ServiceGuardUtils.requireNotNull(params, BizErrorCode.PARAM_INVALID, "请求参数不能为空");
    Long userId =
        ServiceGuardUtils.requirePositive(params.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
    Long seedTypeId =
        ServiceGuardUtils.requirePositive(
            params.getSeedTypeId(), BizErrorCode.PARAM_INVALID, "种子类型ID无效");
    long sellQuantity = gameplayCoreService.defaultLong(params.getQuantity(), 0L);
    if (sellQuantity <= 0) {
      throw new ServiceException(BizErrorCode.AMOUNT_INVALID, "出售数量必须大于0");
    }
    SeedShopSellFruitResultVO cached =
        requestIdempotencyService.getCachedSuccessResult(
            userId, "SELL_FRUIT", params.getRequestId(), SeedShopSellFruitResultVO.class);
    if (cached != null) {
      return cached;
    }
    RequestIdempotency idempotency =
        requestIdempotencyService.claimProcessing(userId, "SELL_FRUIT", params.getRequestId());
    try {
      validateUser(userId);
      SeedType seedType = validateSeedType(seedTypeId);
      UserFruit userFruit =
          ServiceGuardUtils.requirePresent(
              userFruitDao.findByUserIdAndSeedTypeIdAndIsDeletedFalse(userId, seedTypeId),
              BizErrorCode.FRUIT_INVENTORY_NOT_FOUND,
              "果实库存不存在");
      long frozenFruitQuantity = gameplayCoreService.defaultLong(userFruit.getFrozenQuantity(), 0L);
      long availableFruit =
          gameplayCoreService.defaultLong(userFruit.getQuantity(), 0L) - frozenFruitQuantity;
      if (availableFruit < sellQuantity)
        throw new ServiceException(BizErrorCode.FRUIT_NOT_ENOUGH, "果实库存不足");
      long unitFruitPrice = gameplayCoreService.defaultLong(seedType.getFruitPrice(), 0L);
      long totalIncomeCoin = gameplayCoreService.safeMultiply(unitFruitPrice, sellQuantity);
      if (totalIncomeCoin == Long.MAX_VALUE)
        throw new ServiceException(BizErrorCode.AMOUNT_OVERFLOW, "出售金额计算溢出");
      OffsetDateTime now = OffsetDateTime.now();
      if (userFruitDao.decreaseAvailableQuantityIfEnough(
              userFruit.getId(), sellQuantity, userId, now)
          <= 0) {
        throw new ServiceException(BizErrorCode.FRUIT_NOT_ENOUGH, "果实库存不足");
      }
      if (userDao.increaseCoin(userId, totalIncomeCoin, userId, now) <= 0) {
        throw new ServiceException(BizErrorCode.USER_NOT_FOUND, "用户不存在");
      }
      long afterFruitQuantity =
          gameplayCoreService.defaultLong(validateUserFruit(userId, seedTypeId).getQuantity(), 0L);
      long beforeFruitQuantityAccurate = afterFruitQuantity + sellQuantity;
      long afterCoin = gameplayCoreService.defaultLong(validateUser(userId).getCoin(), 0L);
      long beforeCoin = afterCoin - totalIncomeCoin;
      String bizId = seedTypeId + ":" + now.toEpochSecond();
      userInventoryFlowDao.save(
          gameplayCoreService.buildInventoryFlow(
              userId,
              "FRUIT",
              seedTypeId,
              "EXPENSE",
              sellQuantity,
              beforeFruitQuantityAccurate,
              afterFruitQuantity,
              frozenFruitQuantity,
              frozenFruitQuantity,
              "SELL_FRUIT",
              bizId,
              now,
              "{\"seedTypeId\":" + seedTypeId + ",\"unitFruitPrice\":" + unitFruitPrice + "}"));
      userAssetFlowDao.save(
          gameplayCoreService.buildAssetFlow(
              userId,
              "COIN",
              "INCOME",
              totalIncomeCoin,
              beforeCoin,
              afterCoin,
              "SELL_FRUIT",
              bizId,
              now,
              "{\"seedTypeId\":" + seedTypeId + ",\"sellQuantity\":" + sellQuantity + "}"));
      SeedShopSellFruitResultVO result = new SeedShopSellFruitResultVO();
      result.setUserId(userId);
      result.setSeedTypeId(seedTypeId);
      result.setSeedName(seedType.getName());
      result.setSellQuantity(sellQuantity);
      result.setUnitFruitPrice(unitFruitPrice);
      result.setTotalIncomeCoin(totalIncomeCoin);
      result.setBeforeCoin(beforeCoin);
      result.setAfterCoin(afterCoin);
      result.setBeforeFruitQuantity(beforeFruitQuantityAccurate);
      result.setAfterFruitQuantity(afterFruitQuantity);
      requestIdempotencyService.markSuccess(idempotency.getId(), result);
      return result;

    } catch (RuntimeException ex) {
      requestIdempotencyService.markFailed(idempotency.getId(), ex.getMessage());
      throw ex;
    }
  }

  @Override
  public PageResult<SeedShopTradeRecordVO> pageShopTrades(SeedShopTradeQueryDTO query) {
    SeedShopTradeQueryDTO request = query == null ? new SeedShopTradeQueryDTO() : query;
    Long userId =
        ServiceGuardUtils.requirePositive(
            request.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
    validateUser(userId);
    int pageNo = gameplayCoreService.normalizePageNo(request.getPage());
    int pageSize = gameplayCoreService.normalizePageSize(request.getRows());
    String tradeTypeFilter = normalizeTradeType(request.getTradeType());
    if (request.getTradeType() != null
        && !request.getTradeType().isBlank()
        && tradeTypeFilter.isEmpty()) {
      throw new ServiceException(BizErrorCode.TRADE_TYPE_UNSUPPORTED, "tradeType 仅支持 BUY 或 SELL");
    }
    List<UserAssetFlow> assetFlows =
        userAssetFlowDao.findByUserIdAndIsDeletedFalseOrderByOccurredAtDesc(userId);
    Map<String, UserInventoryFlow> inventoryByBizId =
        Stream.concat(
                userInventoryFlowDao
                    .findByUserIdAndItemTypeAndIsDeletedFalseOrderByOccurredAtDesc(userId, "SEED")
                    .stream(),
                userInventoryFlowDao
                    .findByUserIdAndItemTypeAndIsDeletedFalseOrderByOccurredAtDesc(userId, "FRUIT")
                    .stream())
            .filter(f -> f.getBizId() != null)
            .collect(
                Collectors.toMap(UserInventoryFlow::getBizId, Function.identity(), (v1, v2) -> v1));
    Map<Long, SeedType> seedTypeMap = buildSeedTypeMap();
    List<SeedShopTradeRecordVO> records =
        assetFlows.stream()
            .filter(
                flow ->
                    "BUY_SEED".equalsIgnoreCase(flow.getBizType())
                        || "SELL_FRUIT".equalsIgnoreCase(flow.getBizType()))
            .filter(
                flow ->
                    tradeTypeFilter.isEmpty()
                        || tradeTypeFilter.equals(
                            "BUY_SEED".equalsIgnoreCase(flow.getBizType()) ? "BUY" : "SELL"))
            .map(
                flow -> {
                  String tradeType =
                      "BUY_SEED".equalsIgnoreCase(flow.getBizType()) ? "BUY" : "SELL";
                  UserInventoryFlow invFlow = inventoryByBizId.get(flow.getBizId());
                  Long seedTypeId =
                      invFlow == null
                          ? gameplayCoreService.extractLongFromExtData(
                              flow.getExtData(), "seedTypeId")
                          : invFlow.getSeedTypeId();
                  SeedType seedType = seedTypeId == null ? null : seedTypeMap.get(seedTypeId);
                  SeedShopTradeRecordVO record = new SeedShopTradeRecordVO();
                  record.setBizId(gameplayCoreService.safeString(flow.getBizId()));
                  record.setTradeType(tradeType);
                  record.setSeedTypeId(seedTypeId);
                  record.setSeedName(
                      seedType == null ? "" : gameplayCoreService.safeString(seedType.getName()));
                  record.setItemQuantity(
                      invFlow == null
                          ? 0L
                          : gameplayCoreService.defaultLong(invFlow.getChangeAmount(), 0L));
                  record.setItemType(
                      invFlow == null
                          ? ("BUY".equals(tradeType) ? "SEED" : "FRUIT")
                          : gameplayCoreService.safeString(invFlow.getItemType()));
                  record.setItemOperationType(
                      invFlow == null
                          ? ""
                          : gameplayCoreService.safeString(invFlow.getOperationType()));
                  record.setCoinChangeAmount(
                      gameplayCoreService.defaultLong(flow.getChangeAmount(), 0L));
                  record.setCoinOperationType(
                      gameplayCoreService.safeString(flow.getOperationType()));
                  record.setOccurredAt(flow.getOccurredAt());
                  return record;
                })
            .collect(Collectors.toList());
    return PageResult.of(records, pageNo, pageSize);
  }

  @Override
  public PageResult<SeedFruitInventoryItemVO> pageFruitInventory(SeedFruitInventoryQueryDTO query) {
    SeedFruitInventoryQueryDTO request = query == null ? new SeedFruitInventoryQueryDTO() : query;
    Long userId =
        ServiceGuardUtils.requirePositive(
            request.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
    validateUser(userId);
    int pageNo = gameplayCoreService.normalizePageNo(request.getPage());
    int pageSize = gameplayCoreService.normalizePageSize(request.getRows());
    String nameKeyword = gameplayCoreService.safeString(request.getName()).trim().toLowerCase();
    Map<Long, SeedType> seedTypeMap = buildSeedTypeMap();
    List<SeedFruitInventoryItemVO> items =
        userFruitDao.findByUserIdAndIsDeletedFalseOrderByIdAsc(userId).stream()
            .filter(fruit -> seedTypeMap.containsKey(fruit.getSeedTypeId()))
            .filter(
                fruit ->
                    nameKeyword.isEmpty()
                        || gameplayCoreService
                            .safeString(seedTypeMap.get(fruit.getSeedTypeId()).getName())
                            .toLowerCase()
                            .contains(nameKeyword))
            .map(
                fruit -> {
                  SeedType seedType = seedTypeMap.get(fruit.getSeedTypeId());
                  long availableQuantity =
                      Math.max(
                          gameplayCoreService.defaultLong(fruit.getQuantity(), 0L)
                              - gameplayCoreService.defaultLong(fruit.getFrozenQuantity(), 0L),
                          0L);
                  long estimatedIncome =
                      gameplayCoreService.safeMultiply(
                          availableQuantity,
                          gameplayCoreService.defaultLong(seedType.getFruitPrice(), 0L));
                  SeedFruitInventoryItemVO item = new SeedFruitInventoryItemVO();
                  item.setSeedTypeId(seedType.getId());
                  item.setSeedName(gameplayCoreService.safeString(seedType.getName()));
                  item.setCoverImageUrl(
                      gameplayCoreService.safeString(seedType.getCoverImageUrl()));
                  item.setFruitQuantity(gameplayCoreService.defaultLong(fruit.getQuantity(), 0L));
                  item.setFrozenQuantity(
                      gameplayCoreService.defaultLong(fruit.getFrozenQuantity(), 0L));
                  item.setAvailableQuantity(availableQuantity);
                  item.setUnitFruitPrice(
                      gameplayCoreService.defaultLong(seedType.getFruitPrice(), 0L));
                  item.setEstimatedIncomeCoin(estimatedIncome);
                  return item;
                })
            .collect(Collectors.toList());
    return PageResult.of(items, pageNo, pageSize);
  }

  @Override
  public PageResult<SeedInventoryItemVO> pageSeedInventory(SeedInventoryQueryDTO query) {
    SeedInventoryQueryDTO request = query == null ? new SeedInventoryQueryDTO() : query;
    Long userId =
        ServiceGuardUtils.requirePositive(
            request.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
    validateUser(userId);
    int pageNo = gameplayCoreService.normalizePageNo(request.getPage());
    int pageSize = gameplayCoreService.normalizePageSize(request.getRows());
    String nameKeyword = gameplayCoreService.safeString(request.getName()).trim().toLowerCase();
    Map<Long, SeedType> seedTypeMap = buildSeedTypeMap();

    List<SeedInventoryItemVO> items =
        userSeedDao.findByUserIdAndIsDeletedFalseOrderByIdAsc(userId).stream()
            .filter(seed -> seedTypeMap.containsKey(seed.getSeedTypeId()))
            .filter(
                seed ->
                    nameKeyword.isEmpty()
                        || gameplayCoreService
                            .safeString(seedTypeMap.get(seed.getSeedTypeId()).getName())
                            .toLowerCase()
                            .contains(nameKeyword))
            .map(
                seed -> {
                  SeedType seedType = seedTypeMap.get(seed.getSeedTypeId());
                  long quantity = gameplayCoreService.defaultLong(seed.getQuantity(), 0L);
                  long frozenQuantity =
                      gameplayCoreService.defaultLong(seed.getFrozenQuantity(), 0L);
                  SeedInventoryItemVO item = new SeedInventoryItemVO();
                  item.setSeedTypeId(seedType.getId());
                  item.setSeedName(gameplayCoreService.safeString(seedType.getName()));
                  item.setCoverImageUrl(
                      gameplayCoreService.safeString(seedType.getCoverImageUrl()));
                  item.setQuantity(quantity);
                  item.setFrozenQuantity(frozenQuantity);
                  item.setAvailableQuantity(Math.max(quantity - frozenQuantity, 0L));
                  item.setUnitBuyPrice(gameplayCoreService.defaultLong(seedType.getPrice(), 0L));
                  item.setUnlockExperienceRequired(
                      gameplayCoreService.defaultLong(
                          seedType.getUnlockExperienceRequired(), 0L));
                  return item;
                })
            .collect(Collectors.toList());
    return PageResult.of(items, pageNo, pageSize);
  }

  @Override
  public SeedShopOverviewVO shopOverview(SeedShopOverviewDTO query) {
    Long userId =
        ServiceGuardUtils.requirePositive(
            query == null ? null : query.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
    User user = validateUser(userId);
    Map<Long, SeedType> seedTypeMap = buildSeedTypeMap();
    List<UserFruit> fruits = userFruitDao.findByUserIdAndIsDeletedFalseOrderByIdAsc(userId);
    long sellableFruitTotalCount =
        fruits.stream()
            .filter(f -> seedTypeMap.containsKey(f.getSeedTypeId()))
            .mapToLong(
                f ->
                    Math.max(
                        gameplayCoreService.defaultLong(f.getQuantity(), 0L)
                            - gameplayCoreService.defaultLong(f.getFrozenQuantity(), 0L),
                        0L))
            .sum();
    long sellableTotalValue =
        fruits.stream()
            .filter(f -> seedTypeMap.containsKey(f.getSeedTypeId()))
            .mapToLong(
                f ->
                    gameplayCoreService.safeMultiply(
                        Math.max(
                            gameplayCoreService.defaultLong(f.getQuantity(), 0L)
                                - gameplayCoreService.defaultLong(f.getFrozenQuantity(), 0L),
                            0L),
                        gameplayCoreService.defaultLong(
                            seedTypeMap.get(f.getSeedTypeId()).getFruitPrice(), 0L)))
            .reduce(
                0L,
                (a, b) ->
                    a == Long.MAX_VALUE || b == Long.MAX_VALUE
                        ? Long.MAX_VALUE
                        : gameplayCoreService.safeAdd(a, b));
    long currentCoin = gameplayCoreService.defaultLong(user.getCoin(), 0L);
    int purchasableSeedTypeCount =
        (int)
            seedTypeDao.findByIsDeletedFalseOrderByIdAsc().stream()
                .filter(s -> gameplayCoreService.defaultLong(s.getPrice(), 0L) <= currentCoin)
                .count();
    SeedShopOverviewVO result = new SeedShopOverviewVO();
    result.setUserId(userId);
    result.setCurrentCoin(currentCoin);
    result.setSellableTotalValue(sellableTotalValue);
    result.setSellableFruitTotalCount(sellableFruitTotalCount);
    result.setPurchasableSeedTypeCount(purchasableSeedTypeCount);
    return result;
  }

  @Override
  public SeedShopHomeVO shopHome(SeedShopHomeQueryDTO query) {
    SeedShopHomeQueryDTO request = query == null ? new SeedShopHomeQueryDTO() : query;
    SeedShopOverviewDTO overviewDTO = new SeedShopOverviewDTO();
    overviewDTO.setUserId(request.getUserId());
    SeedShopQueryDTO pageDTO = new SeedShopQueryDTO();
    pageDTO.setName(request.getName());
    pageDTO.setSeedQualityId(request.getSeedQualityId());
    pageDTO.setLevel(request.getLevel());
    pageDTO.setPage(request.getPage());
    pageDTO.setRows(request.getRows());
    pageDTO.setSort(request.getSort());
    pageDTO.setOrder(request.getOrder());
    SeedShopHomeVO result = new SeedShopHomeVO();
    result.setOverview(shopOverview(overviewDTO));
    result.setShopPage(pageSeedShop(pageDTO));
    return result;
  }

  @Override
  @Transactional
  public Long saveSeedType(SeedAddOrUpdateDTO params) {
    ServiceGuardUtils.requireNotNull(params, BizErrorCode.PARAM_INVALID, "请求参数不能为空");
    String name = gameplayCoreService.safeString(params.getName()).trim();
    if (name.isEmpty()) {
      throw new ServiceException(BizErrorCode.SEED_NAME_REQUIRED, "种子名称不能为空");
    }
    SeedType entity;
    if (params.getId() != null && params.getId() > 0) {
      entity =
          ServiceGuardUtils.requirePresent(
              seedTypeDao.findByIdAndIsDeletedFalse(params.getId()),
              BizErrorCode.SEED_TYPE_NOT_FOUND,
              "种子类型不存在");

    } else {
      entity = new SeedType();
      gameplayCoreService.initNewEntity(entity, 0L, OffsetDateTime.now());
    }
    Long seedQualityId =
        params.getSeedQualityId() == null || params.getSeedQualityId() <= 0
            ? 1L
            : params.getSeedQualityId();
    ServiceGuardUtils.requirePresent(
        seedQualityDao.findByIdAndIsDeletedFalse(seedQualityId),
        BizErrorCode.SEED_QUALITY_NOT_FOUND,
        "种子品质不存在");
    Long enableSoilTypeBits = resolveSoilTypeBits(params);
    if (enableSoilTypeBits <= 0)
      throw new ServiceException(BizErrorCode.SOIL_TYPE_REQUIRED, "至少需要选择一种土壤类型");
    entity.setName(name);
    entity.setCoverImageUrl(gameplayCoreService.safeString(params.getCoverImageUrl()));
    entity.setSeedQualityId(seedQualityId);
    entity.setEnableSoilTypeBits(enableSoilTypeBits);
    entity.setLevel(resolveSeedLevel(params));
    entity.setUnlockExperienceRequired(resolveUnlockExperienceRequired(params, entity.getLevel()));
    entity.setDescription(
        params.getDescription() != null
            ? params.getDescription()
            : gameplayCoreService.safeString(params.getTips()));
    entity.setMaxBugLimit(params.getMaxBugLimit() == null ? 0 : params.getMaxBugLimit());
    entity.setMaxHarvestCount(
        params.getMaxHarvestCount() == null ? 1 : params.getMaxHarvestCount());
    entity.setRegrowStageIndex(params.getRegrowStageIndex());
    entity.setPrice(
        params.getPrice() != null
            ? params.getPrice()
            : gameplayCoreService.defaultLong(params.getBuyPrice(), 0L));
    entity.setHarvestExperience(
        params.getHarvestExperience() != null
            ? params.getHarvestExperience()
            : gameplayCoreService.defaultLong(params.getExp(), 0L));
    entity.setHarvestFruitNumber(
        params.getHarvestFruitNumber() != null
            ? params.getHarvestFruitNumber()
            : (params.getHarvestCount() != null ? params.getHarvestCount() : 0));
    entity.setFruitLossPerBug(
        params.getFruitLossPerBug() == null ? 0 : params.getFruitLossPerBug());
    entity.setBugKillCoinReward(gameplayCoreService.defaultLong(params.getBugKillCoinReward(), 0L));
    entity.setBugKillExperienceReward(
        gameplayCoreService.defaultLong(params.getBugKillExperienceReward(), 0L));
    entity.setBugKillScoreReward(
        gameplayCoreService.defaultLong(params.getBugKillScoreReward(), 0L));
    entity.setFruitPrice(gameplayCoreService.defaultLong(params.getFruitPrice(), 0L));
    entity.setHarvestScore(
        params.getHarvestScore() != null
            ? params.getHarvestScore()
            : gameplayCoreService.defaultLong(params.getScore(), 0L));
    gameplayCoreService.touchForUpdate(entity, 0L, OffsetDateTime.now());
    return seedTypeDao.save(entity).getId();
  }

  @Override
  @Transactional
  public void removeSeedType(IdDTO params) {
    Long id =
        ServiceGuardUtils.requirePositive(
            params == null ? null : params.getId(), BizErrorCode.PARAM_INVALID, "阶段ID无效");
    SeedType seedType = validateSeedType(id);
    seedType.setIsDeleted(true);
    gameplayCoreService.touchForUpdate(seedType, 0L, OffsetDateTime.now());
    seedTypeDao.save(seedType);
  }

  @Override
  public List<OptionVO> listSeedQualityOptions() {
    return seedQualityDao.findByIsDeletedFalseOrderByIdAsc().stream()
        .map(item -> seedViewAssembler.option(item.getId(), item.getName()))
        .collect(Collectors.toList());
  }

  @Override
  public List<SoilOptionVO> listSoilOptions() {
    return soilTypeDao.findByIsDeletedFalseOrderByIdAsc().stream()
        .map(item -> seedViewAssembler.soilOption(item.getId(), item.getName(), item.getBitCode()))
        .collect(Collectors.toList());
  }

  @Override
  public List<OptionVO> listGrowthStageOptions() {
    return growthStageDao.findByIsDeletedFalseOrderByIdAsc().stream()
        .map(item -> seedViewAssembler.option(item.getId(), item.getName()))
        .collect(Collectors.toList());
  }

  @Override
  public PageResult<SeedStageGridVO> pageSeedStages(SeedStageQueryDTO query) {
    Long seedTypeId =
        ServiceGuardUtils.requirePositive(
            query == null ? null : query.getSeedTypeId(), BizErrorCode.PARAM_INVALID, "种子类型ID无效");
    SeedType seedType = validateSeedType(seedTypeId);
    Map<Long, String> growthNameMap = buildGrowthStageNameMap();
    List<SeedStageGridVO> records =
        seedGrowthStageDao
            .findBySeedTypeIdAndIsDeletedFalseOrderByStageIndexAsc(seedTypeId)
            .stream()
            .map(
                item -> {
                  SeedStageGridVO row = new SeedStageGridVO();
                  row.setId(item.getId());
                  row.setSeedTypeId(item.getSeedTypeId());
                  row.setSeedName(gameplayCoreService.safeString(seedType.getName()));
                  row.setGrowthStageId(item.getGrowthStageId());
                  row.setGrowthStageName(
                      gameplayCoreService.safeString(growthNameMap.get(item.getGrowthStageId())));
                  row.setStageIndex(item.getStageIndex());
                  row.setDurationSeconds(item.getDurationSeconds());
                  row.setBugProbability(item.getBugProbability());
                  row.setWidth(item.getWidth());
                  row.setHeight(item.getHeight());
                  row.setOffsetX(item.getOffsetX());
                  row.setOffsetY(item.getOffsetY());
                  row.setAssetUrl(gameplayCoreService.safeString(item.getAssetUrl()));
                  return row;
                })
            .collect(Collectors.toList());
    return PageResult.of(records, 1L, records.size());
  }

  @Override
  @Transactional
  public void saveSeedStage(SeedStageAddOrUpdateDTO params) {
    ServiceGuardUtils.requireNotNull(params, BizErrorCode.PARAM_INVALID, "请求参数不能为空");
    Long seedTypeId =
        ServiceGuardUtils.requirePositive(
            params.getSeedTypeId(), BizErrorCode.PARAM_INVALID, "种子类型ID无效");
    validateSeedType(seedTypeId);
    Long growthStageId =
        ServiceGuardUtils.requirePositive(
            params.getGrowthStageId(), BizErrorCode.PARAM_INVALID, "生长阶段ID无效");
    ServiceGuardUtils.requirePresent(
        growthStageDao.findByIdAndIsDeletedFalse(growthStageId),
        BizErrorCode.GROWTH_STAGE_NOT_FOUND,
        "生长阶段不存在");
    short stageIndex =
        params.getStageIndex() != null && params.getStageIndex() > 0
            ? params.getStageIndex()
            : (params.getGrowthStage() == null || params.getGrowthStage() <= 0
                ? 1
                : params.getGrowthStage());
    SeedGrowthStage entity =
        (params.getId() != null && params.getId() > 0)
            ? ServiceGuardUtils.requirePresent(
                seedGrowthStageDao.findByIdAndIsDeletedFalse(params.getId()),
                BizErrorCode.SEED_STAGE_NOT_FOUND,
                "种子阶段配置不存在")
            : seedGrowthStageDao
                .findBySeedTypeIdAndStageIndexAndIsDeletedFalse(seedTypeId, stageIndex)
                .orElseGet(
                    () -> {
                      SeedGrowthStage newStage = new SeedGrowthStage();
                      gameplayCoreService.initNewEntity(newStage, 0L, OffsetDateTime.now());
                      return newStage;
                    });
    entity.setSeedTypeId(seedTypeId);
    entity.setGrowthStageId(growthStageId);
    entity.setStageIndex(stageIndex);
    entity.setDurationSeconds(
        params.getDurationSeconds() == null ? 0 : params.getDurationSeconds());
    entity.setBugProbability(
        params.getBugProbability() != null
            ? params.getBugProbability()
            : (params.getPestProbability() != null
                ? params.getPestProbability()
                : BigDecimal.ZERO));
    entity.setWidth(params.getWidth() == null ? 0 : params.getWidth());
    entity.setHeight(params.getHeight() == null ? 0 : params.getHeight());
    entity.setOffsetX(params.getOffsetX() == null ? 0 : params.getOffsetX());
    entity.setOffsetY(params.getOffsetY() == null ? 0 : params.getOffsetY());
    entity.setAssetUrl(gameplayCoreService.safeString(params.getAssetUrl()));
    gameplayCoreService.touchForUpdate(entity, 0L, OffsetDateTime.now());
    seedGrowthStageDao.save(entity);
  }

  @Override
  @Transactional
  public void removeSeedStage(IdDTO params) {
    Long id =
        ServiceGuardUtils.requirePositive(
            params == null ? null : params.getId(), BizErrorCode.PARAM_INVALID, "阶段ID无效");
    SeedGrowthStage entity =
        ServiceGuardUtils.requirePresent(
            seedGrowthStageDao.findByIdAndIsDeletedFalse(id),
            BizErrorCode.SEED_STAGE_NOT_FOUND,
            "种子阶段配置不存在");
    entity.setIsDeleted(true);
    gameplayCoreService.touchForUpdate(entity, 0L, OffsetDateTime.now());
    seedGrowthStageDao.save(entity);
  }

  @Override
  public void validateSeedStages(IdDTO params) {
    Long seedTypeId =
        ServiceGuardUtils.requirePositive(
            params == null ? null : params.getId(), BizErrorCode.PARAM_INVALID, "种子类型ID无效");
    validateSeedType(seedTypeId);
  }

  /*
   * =========================================================
   *  Private Helper Methods
   * =========================================================
   */
  private List<SeedShopItemVO> buildReadySeedShopItems(
      List<SeedType> seedTypes,
      Map<Long, SeedQuality> qualityMap,
      Map<Integer, String> soilNameByBitCode) {
    Map<Long, Integer> totalGrowSecondsMap = buildTotalGrowSecondsMap(seedTypes);
    return seedTypes.stream()
        .map(
            seed ->
                seedViewAssembler.seedShopItem(
                    seed, qualityMap, soilNameByBitCode, totalGrowSecondsMap))
        .collect(Collectors.toList());
  }

  private Map<Long, Integer> buildTotalGrowSecondsMap(List<SeedType> seedTypes) {
    List<Long> seedTypeIds =
        seedTypes.stream()
            .map(SeedType::getId)
            .filter(id -> id != null)
            .collect(Collectors.toList());
    if (seedTypeIds.isEmpty()) {
      return Map.of();
    }
    return seedGrowthStageDao
        .findBySeedTypeIdInAndIsDeletedFalseOrderBySeedTypeIdAscStageIndexAsc(seedTypeIds)
        .stream()
        .collect(
            Collectors.groupingBy(
                SeedGrowthStage::getSeedTypeId,
                Collectors.summingInt(
                    stage -> stage.getDurationSeconds() == null ? 0 : stage.getDurationSeconds())));
  }

  private Map<Long, SeedQuality> buildSeedQualityMap() {
    return seedQualityDao.findByIsDeletedFalseOrderByIdAsc().stream()
        .collect(Collectors.toMap(SeedQuality::getId, Function.identity()));
  }

  private Map<Long, SeedType> buildSeedTypeMap() {
    return seedTypeDao.findByIsDeletedFalseOrderByIdAsc().stream()
        .collect(Collectors.toMap(SeedType::getId, Function.identity()));
  }

  private Map<Integer, String> buildSoilNameByBitCode() {
    return soilTypeDao.findByIsDeletedFalseOrderByIdAsc().stream()
        .collect(
            Collectors.toMap(
                SoilType::getBitCode,
                s -> gameplayCoreService.safeString(s.getName()),
                (v1, v2) -> v1,
                LinkedHashMap::new));
  }

  private Map<Long, String> buildGrowthStageNameMap() {
    return growthStageDao.findByIsDeletedFalseOrderByIdAsc().stream()
        .collect(
            Collectors.toMap(GrowthStage::getId, s -> gameplayCoreService.safeString(s.getName())));
  }

  private Comparator<SeedShopItemVO> resolveShopComparator(String sort, String order) {
    Comparator<SeedShopItemVO> comparator =
        switch (gameplayCoreService.safeString(sort).trim().toLowerCase()) {
          case "price" ->
              Comparator.comparing(item -> gameplayCoreService.defaultLong(item.getPrice(), 0L));
          case "fruitprice" ->
              Comparator.comparing(
                  item -> gameplayCoreService.defaultLong(item.getFruitPrice(), 0L));
          case "level" ->
              Comparator.comparing(
                  item -> item.getLevel() == null ? 0 : item.getLevel().intValue());
          case "totalharvestfruitvalue" ->
              Comparator.comparing(
                  item -> gameplayCoreService.defaultLong(item.getTotalHarvestFruitValue(), 0L));
          case "estimatednetvalue" ->
              Comparator.comparing(
                  item -> gameplayCoreService.defaultLong(item.getEstimatedNetValue(), 0L));
          default ->
              Comparator.comparing(item -> gameplayCoreService.defaultLong(item.getId(), 0L));
        };
    return "desc".equalsIgnoreCase(order) ? comparator.reversed() : comparator;
  }

  private String safeSeedSortField(String sort) {
    return switch (gameplayCoreService.safeString(sort).trim().toLowerCase()) {
      case "name" -> "name";
      case "level" -> "level";
      case "price" -> "price";
      case "fruitprice" -> "fruitPrice";
      case "harvestscore" -> "harvestScore";
      default -> "id";
    };
  }

  private String normalizeTradeType(String tradeType) {
    return switch (gameplayCoreService.safeString(tradeType).trim().toUpperCase()) {
      case "BUY", "BUY_SEED" -> "BUY";
      case "SELL", "SELL_FRUIT" -> "SELL";
      default -> "";
    };
  }

  private Long resolveSoilTypeBits(SeedAddOrUpdateDTO params) {
    if (params.getEnableSoilTypeBits() != null && params.getEnableSoilTypeBits() > 0) {
      return params.getEnableSoilTypeBits();
    }
    String idStr = gameplayCoreService.safeString(params.getSoilTypeIds()).trim();
    if (idStr.isEmpty()) {
      idStr = gameplayCoreService.safeString(params.getSoilTypeId()).trim();
    }
    if (idStr.isEmpty()) {
      return 0L;
    }
    return Arrays.stream(idStr.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(
            s -> {
              try {
                return Long.parseLong(s);

              } catch (Exception e) {
                return 0L;
              }
            })
        .filter(id -> id > 0)
        .map(id -> soilTypeDao.findByIdAndIsDeletedFalse(id).map(SoilType::getBitCode).orElse(0))
        .reduce(0, (a, b) -> a | b)
        .longValue();
  }

  private Short resolveSeedLevel(SeedAddOrUpdateDTO params) {
    if (params.getLevel() != null && params.getLevel() > 0) {
      return params.getLevel();
    }
    String season = gameplayCoreService.safeString(params.getSeason()).trim();
    if (!season.isEmpty()) {
      try {
        short parsed = Short.parseShort(season);
        if (parsed > 0) {
          return parsed;
        }

      } catch (Exception ignored) {

      }
    }
    return 1;
  }

  private Long resolveUnlockExperienceRequired(SeedAddOrUpdateDTO params, Short level) {
    if (params.getUnlockExperienceRequired() != null && params.getUnlockExperienceRequired() >= 0) {
      return params.getUnlockExperienceRequired();
    }
    int safeLevel = level == null ? 1 : Math.max(level, (short) 1);
    if (safeLevel <= 1) {
      return 0L;
    }
    if (safeLevel == 2) {
      return 300L;
    }
    return 300L + (long) (safeLevel - 2) * 500L;
  }

  private List<SeedType> filterShopReadySeedTypes(List<SeedType> seedTypes) {
    if (seedTypes.isEmpty()) {
      return seedTypes;
    }
    List<Long> seedTypeIds =
        seedTypes.stream()
            .map(SeedType::getId)
            .filter(id -> id != null)
            .collect(Collectors.toList());
    if (seedTypeIds.isEmpty()) {
      return List.of();
    }
    Map<Long, List<SeedGrowthStage>> stagesBySeedTypeId =
        seedGrowthStageDao
            .findBySeedTypeIdInAndIsDeletedFalseOrderBySeedTypeIdAscStageIndexAsc(seedTypeIds)
            .stream()
            .collect(Collectors.groupingBy(SeedGrowthStage::getSeedTypeId));
    Map<Long, String> growthStageNameMap = buildGrowthStageNameMap();
    return seedTypes.stream()
        .filter(
            seedType ->
                isSeedTypeReadyForShop(
                    seedType,
                    stagesBySeedTypeId.getOrDefault(seedType.getId(), List.of()),
                    growthStageNameMap))
        .collect(Collectors.toList());
  }

  private boolean isSeedTypeReadyForShop(
      SeedType seedType, List<SeedGrowthStage> stages, Map<Long, String> growthStageNameMap) {
    try {
      requireSeedTypeReadyForShop(seedType, stages, growthStageNameMap);
      return true;
    } catch (ServiceException ex) {
      return false;
    }
  }

  private void requireSeedTypeReadyForShop(SeedType seedType) {
    List<SeedGrowthStage> stages =
        seedType == null || seedType.getId() == null
            ? List.of()
            : seedGrowthStageDao.findBySeedTypeIdAndIsDeletedFalseOrderByStageIndexAsc(
                seedType.getId());
    requireSeedTypeReadyForShop(seedType, stages, buildGrowthStageNameMap());
  }

  private void requireSeedTypeReadyForShop(
      SeedType seedType, List<SeedGrowthStage> stages, Map<Long, String> growthStageNameMap) {
    if (seedType == null || seedType.getId() == null || seedType.getId() <= 0) {
      throw new ServiceException(BizErrorCode.SEED_TYPE_NOT_FOUND, "种子类型不存在");
    }
    if (gameplayCoreService.safeString(seedType.getName()).trim().isEmpty()) {
      throw new ServiceException(BizErrorCode.SEED_NAME_REQUIRED, "种子名称不能为空");
    }
    if (seedType.getSeedQualityId() == null || seedType.getSeedQualityId() <= 0) {
      throw new ServiceException(BizErrorCode.SEED_QUALITY_NOT_FOUND, "种子品质不存在");
    }
    if (gameplayCoreService.defaultLong(seedType.getEnableSoilTypeBits(), 0L) <= 0) {
      throw new ServiceException(BizErrorCode.SOIL_TYPE_REQUIRED, "至少需要选择一种土壤类型");
    }
    if (seedType.getLevel() == null || seedType.getLevel() <= 0) {
      throw new ServiceException(BizErrorCode.PARAM_INVALID, "种子等级必须大于0");
    }
    if (seedType.getMaxHarvestCount() == null || seedType.getMaxHarvestCount() <= 0) {
      throw new ServiceException(BizErrorCode.PARAM_INVALID, "最大收获次数必须大于0");
    }
    requireNonNegative(seedType.getUnlockExperienceRequired(), "解锁经验不能小于0");
    requireNonNegative(seedType.getPrice(), "种子价格不能小于0");
    requireNonNegative(seedType.getFruitPrice(), "果实单价不能小于0");
    requireNonNegative(seedType.getHarvestExperience(), "收获经验不能小于0");
    requireNonNegative(seedType.getHarvestScore(), "收获积分不能小于0");
    requireNonNegative(seedType.getHarvestFruitNumber(), "果实数量不能小于0");
    requireNonNegative(seedType.getFruitLossPerBug(), "虫害损失不能小于0");
    requireNonNegative(seedType.getMaxBugLimit(), "虫子上限不能小于0");
    requireNonNegative(seedType.getBugKillExperienceReward(), "杀虫经验不能小于0");
    requireNonNegative(seedType.getBugKillScoreReward(), "杀虫积分不能小于0");
    requireNonNegative(seedType.getBugKillCoinReward(), "杀虫金币不能小于0");
    SeedStageConfigValidator.validateComplete(seedType, stages, growthStageNameMap);
  }

  private void requireNonNegative(Number value, String message) {
    if (value == null || value.longValue() < 0) {
      throw new ServiceException(BizErrorCode.PARAM_INVALID, message);
    }
  }

  /*
   * =========================================================
   * Common Entity / Base Helpers
   * =========================================================
   */
  private User validateUser(Long userId) {
    return ServiceGuardUtils.requirePresent(
        userDao.findByIdAndIsDeletedFalse(userId), BizErrorCode.USER_NOT_FOUND, "用户不存在");
  }

  private SeedType validateSeedType(Long seedTypeId) {
    return ServiceGuardUtils.requirePresent(
        seedTypeDao.findByIdAndIsDeletedFalse(seedTypeId),
        BizErrorCode.SEED_TYPE_NOT_FOUND,
        "种子类型不存在");
  }

  private UserSeed validateUserSeed(Long userId, Long seedTypeId) {
    return ServiceGuardUtils.requirePresent(
        userSeedDao.findByUserIdAndSeedTypeIdAndIsDeletedFalse(userId, seedTypeId),
        BizErrorCode.SEED_INVENTORY_NOT_FOUND,
        "种子库存不存在");
  }

  private UserFruit validateUserFruit(Long userId, Long seedTypeId) {
    return ServiceGuardUtils.requirePresent(
        userFruitDao.findByUserIdAndSeedTypeIdAndIsDeletedFalse(userId, seedTypeId),
        BizErrorCode.FRUIT_INVENTORY_NOT_FOUND,
        "果实库存不存在");
  }
}
