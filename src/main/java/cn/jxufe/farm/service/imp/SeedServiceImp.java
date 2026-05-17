package cn.jxufe.farm.service.imp;

import cn.jxufe.farm.bean.dto.IdDTO;
import cn.jxufe.farm.bean.dto.SeedAddOrUpdateDTO;
import cn.jxufe.farm.bean.dto.SeedFruitInventoryQueryDTO;
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
import cn.jxufe.farm.bean.vo.SeedShopBuyResultVO;
import cn.jxufe.farm.bean.vo.SeedShopHomeVO;
import cn.jxufe.farm.bean.vo.SeedShopItemVO;
import cn.jxufe.farm.bean.vo.SeedShopOverviewVO;
import cn.jxufe.farm.bean.vo.SeedShopSellFruitResultVO;
import cn.jxufe.farm.bean.vo.SeedShopTradeRecordVO;
import cn.jxufe.farm.bean.vo.SeedStageGridVO;
import cn.jxufe.farm.bean.vo.SoilOptionVO;
import cn.jxufe.farm.common.constants.BizErrorCode;
import cn.jxufe.farm.common.exception.ServiceException;
import cn.jxufe.farm.common.pages.PageResult;
import cn.jxufe.farm.common.utils.ServiceGuard;
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
import cn.jxufe.farm.entity.base.BaseEntity;
import cn.jxufe.farm.service.SeedService;
import cn.jxufe.farm.service.RequestIdempotencyService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SeedServiceImp implements SeedService {

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

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

    public SeedServiceImp(SeedTypeDao seedTypeDao,
                   SeedQualityDao seedQualityDao,
                   SoilTypeDao soilTypeDao,
                   GrowthStageDao growthStageDao,
                   SeedGrowthStageDao seedGrowthStageDao,
                   UserDao userDao,
                   UserSeedDao userSeedDao,
                   UserFruitDao userFruitDao,
                   UserAssetFlowDao userAssetFlowDao,
                   UserInventoryFlowDao userInventoryFlowDao,
                   RequestIdempotencyService requestIdempotencyService) {
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
    }

    @Override
    public PageResult<SeedGridVO> pageSeedTypes(SeedTypeQueryDTO query) {
        SeedTypeQueryDTO request = query == null ? new SeedTypeQueryDTO() : query;
        int pageNo = normalizePageNo(request.getPage());
        int pageSize = normalizePageSize(request.getRows());
        int pageIndex = pageNo - 1;
        Sort.Direction direction = "desc".equalsIgnoreCase(request.getOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by(direction, safeSeedSortField(request.getSort())));
        Page<SeedType> seedPage = seedTypeDao.findByIsDeletedFalseAndNameContainingIgnoreCase(safeString(request.getName()).trim(), pageable);

        Map<Long, SeedQuality> qualityMap = buildSeedQualityMap();
        Map<Integer, String> soilNameByBitCode = buildSoilNameByBitCode();
        List<SeedGridVO> records = new ArrayList<>();
        for (SeedType seedType : seedPage.getContent()) {
            records.add(buildSeedGridVO(seedType, qualityMap, soilNameByBitCode));
        }
        return new PageResult<>(pageNo, pageSize, seedPage.getTotalElements(), records);
    }

    @Override
    public PageResult<SeedShopItemVO> pageSeedShop(SeedShopQueryDTO query) {
        SeedShopQueryDTO request = query == null ? new SeedShopQueryDTO() : query;
        int pageNo = normalizePageNo(request.getPage());
        int pageSize = normalizePageSize(request.getRows());

        Map<Long, SeedQuality> qualityMap = buildSeedQualityMap();
        Map<Integer, String> soilNameByBitCode = buildSoilNameByBitCode();
        List<SeedType> allSeedTypes = seedTypeDao.findByIsDeletedFalseOrderByIdAsc();
        List<SeedShopItemVO> allItems = new ArrayList<>();
        String nameKeyword = safeString(request.getName()).trim().toLowerCase();
        for (SeedType seedType : allSeedTypes) {
            if (!nameKeyword.isBlank() && !safeString(seedType.getName()).toLowerCase().contains(nameKeyword)) {
                continue;
            }
            if (request.getSeedQualityId() != null && !request.getSeedQualityId().equals(seedType.getSeedQualityId())) {
                continue;
            }
            if (request.getLevel() != null && !request.getLevel().equals(seedType.getLevel())) {
                continue;
            }
            allItems.add(buildSeedShopItemVO(seedType, qualityMap, soilNameByBitCode));
        }

        allItems.sort(resolveShopComparator(request.getSort(), request.getOrder()));
        long total = allItems.size();
        int fromIndex = Math.min((pageNo - 1) * pageSize, allItems.size());
        int toIndex = Math.min(fromIndex + pageSize, allItems.size());
        List<SeedShopItemVO> pageRecords = allItems.subList(fromIndex, toIndex);
        return new PageResult<>(pageNo, pageSize, total, pageRecords);
    }

    @Override
    @Transactional
    public SeedShopBuyResultVO buySeed(SeedShopBuyDTO params) {
        ServiceGuard.requireNotNull(params, BizErrorCode.PARAM_INVALID, "请求参数不能为空");
        Long userId = ServiceGuard.requirePositive(params.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
        Long seedTypeId = ServiceGuard.requirePositive(params.getSeedTypeId(), BizErrorCode.PARAM_INVALID, "种子类型ID无效");
        long buyQuantity = defaultLong(params.getQuantity(), 0L);
        if (buyQuantity <= 0) {
            throw new ServiceException(BizErrorCode.AMOUNT_INVALID, "购买数量必须大于0");
        }
        SeedShopBuyResultVO cached = requestIdempotencyService.getCachedSuccessResult(
                userId,
                "BUY_SEED",
                params.getRequestId(),
                SeedShopBuyResultVO.class
        );
        if (cached != null) {
            return cached;
        }
        RequestIdempotency idempotency = requestIdempotencyService.claimProcessing(userId, "BUY_SEED", params.getRequestId());
        try {
            User user = ServiceGuard.requirePresent(
                    userDao.findByIdAndIsDeletedFalse(userId),
                    BizErrorCode.USER_NOT_FOUND,
                    "用户不存在"
            );
            SeedType seedType = ServiceGuard.requirePresent(
                    seedTypeDao.findByIdAndIsDeletedFalse(seedTypeId),
                    BizErrorCode.SEED_TYPE_NOT_FOUND,
                    "种子类型不存在"
            );

            long unitPrice = defaultLong(seedType.getPrice(), 0L);
            long totalCost;
            try {
                totalCost = Math.multiplyExact(unitPrice, buyQuantity);
            } catch (ArithmeticException ex) {
                throw new ServiceException(BizErrorCode.AMOUNT_OVERFLOW, "购买金额计算溢出");
            }
            OffsetDateTime now = OffsetDateTime.now();
            int coinUpdated = userDao.decreaseCoinIfEnough(userId, totalCost, userId, now);
            if (coinUpdated <= 0) {
                throw new ServiceException(BizErrorCode.COIN_NOT_ENOUGH, "金币不足");
            }
            User latestUser = ServiceGuard.requirePresent(
                    userDao.findByIdAndIsDeletedFalse(userId),
                    BizErrorCode.USER_NOT_FOUND,
                    "用户不存在"
            );
            long afterCoin = defaultLong(latestUser.getCoin(), 0L);
            long beforeCoin = afterCoin + totalCost;

            UserSeed userSeed = userSeedDao.findByUserIdAndSeedTypeIdAndIsDeletedFalse(user.getId(), seedType.getId())
                    .orElseGet(() -> createUserSeed(user.getId(), seedType.getId(), now));
            if (userSeed.getId() == null) {
                userSeedDao.save(userSeed);
            }
            long frozenSeedQuantity = defaultLong(userSeed.getFrozenQuantity(), 0L);
            int seedUpdated = userSeedDao.increaseQuantity(userSeed.getId(), buyQuantity, userId, now);
            if (seedUpdated <= 0) {
                throw new ServiceException(BizErrorCode.SEED_INVENTORY_NOT_FOUND, "种子库存不存在");
            }
            UserSeed latestUserSeed = ServiceGuard.requirePresent(
                    userSeedDao.findByUserIdAndSeedTypeIdAndIsDeletedFalse(user.getId(), seedType.getId()),
                    BizErrorCode.SEED_INVENTORY_NOT_FOUND,
                    "种子库存不存在"
            );
            long afterSeedQuantity = defaultLong(latestUserSeed.getQuantity(), 0L);
            long beforeSeedQuantity = afterSeedQuantity - buyQuantity;

            UserAssetFlow assetFlow = new UserAssetFlow();
            initNewEntity(assetFlow, userId, now);
            assetFlow.setUserId(user.getId());
            assetFlow.setAssetType("COIN");
            assetFlow.setOperationType("EXPENSE");
            assetFlow.setChangeAmount(totalCost);
            assetFlow.setBeforeAmount(beforeCoin);
            assetFlow.setAfterAmount(afterCoin);
            assetFlow.setBizType("BUY_SEED");
            assetFlow.setBizId(seedType.getId() + ":" + now.toEpochSecond());
            assetFlow.setOccurredAt(now);
            assetFlow.setExtData("{\"seedTypeId\":" + seedType.getId() + ",\"buyQuantity\":" + buyQuantity + "}");
            userAssetFlowDao.save(assetFlow);

            UserInventoryFlow inventoryFlow = new UserInventoryFlow();
            initNewEntity(inventoryFlow, userId, now);
            inventoryFlow.setUserId(user.getId());
            inventoryFlow.setItemType("SEED");
            inventoryFlow.setSeedTypeId(seedType.getId());
            inventoryFlow.setOperationType("INCOME");
            inventoryFlow.setChangeAmount(buyQuantity);
            inventoryFlow.setBeforeAmount(beforeSeedQuantity);
            inventoryFlow.setAfterAmount(afterSeedQuantity);
            inventoryFlow.setBeforeFrozenAmount(frozenSeedQuantity);
            inventoryFlow.setAfterFrozenAmount(frozenSeedQuantity);
            inventoryFlow.setBizType("BUY_SEED");
            inventoryFlow.setBizId(assetFlow.getBizId());
            inventoryFlow.setOccurredAt(now);
            inventoryFlow.setExtData("{\"seedTypeId\":" + seedType.getId() + ",\"totalCostCoin\":" + totalCost + "}");
            userInventoryFlowDao.save(inventoryFlow);

            SeedShopBuyResultVO result = new SeedShopBuyResultVO();
            result.setUserId(user.getId());
            result.setSeedTypeId(seedType.getId());
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
        ServiceGuard.requireNotNull(params, BizErrorCode.PARAM_INVALID, "请求参数不能为空");
        Long userId = ServiceGuard.requirePositive(params.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
        Long seedTypeId = ServiceGuard.requirePositive(params.getSeedTypeId(), BizErrorCode.PARAM_INVALID, "种子类型ID无效");
        long sellQuantity = defaultLong(params.getQuantity(), 0L);
        if (sellQuantity <= 0) {
            throw new ServiceException(BizErrorCode.AMOUNT_INVALID, "出售数量必须大于0");
        }
        SeedShopSellFruitResultVO cached = requestIdempotencyService.getCachedSuccessResult(
                userId,
                "SELL_FRUIT",
                params.getRequestId(),
                SeedShopSellFruitResultVO.class
        );
        if (cached != null) {
            return cached;
        }
        RequestIdempotency idempotency = requestIdempotencyService.claimProcessing(userId, "SELL_FRUIT", params.getRequestId());
        try {
            User user = ServiceGuard.requirePresent(
                    userDao.findByIdAndIsDeletedFalse(userId),
                    BizErrorCode.USER_NOT_FOUND,
                    "用户不存在"
            );
            SeedType seedType = ServiceGuard.requirePresent(
                    seedTypeDao.findByIdAndIsDeletedFalse(seedTypeId),
                    BizErrorCode.SEED_TYPE_NOT_FOUND,
                    "种子类型不存在"
            );
            UserFruit userFruit = ServiceGuard.requirePresent(
                    userFruitDao.findByUserIdAndSeedTypeIdAndIsDeletedFalse(userId, seedTypeId),
                    BizErrorCode.FRUIT_INVENTORY_NOT_FOUND,
                    "果实库存不存在"
            );

            long beforeFruitQuantity = defaultLong(userFruit.getQuantity(), 0L);
            long frozenFruitQuantity = defaultLong(userFruit.getFrozenQuantity(), 0L);
            long availableFruit = beforeFruitQuantity - frozenFruitQuantity;
            if (availableFruit < sellQuantity) {
                throw new ServiceException(BizErrorCode.FRUIT_NOT_ENOUGH, "果实库存不足");
            }

            long unitFruitPrice = defaultLong(seedType.getFruitPrice(), 0L);
            long totalIncomeCoin;
            try {
                totalIncomeCoin = Math.multiplyExact(unitFruitPrice, sellQuantity);
            } catch (ArithmeticException ex) {
                throw new ServiceException(BizErrorCode.AMOUNT_OVERFLOW, "出售金额计算溢出");
            }

            OffsetDateTime now = OffsetDateTime.now();
            int fruitUpdated = userFruitDao.decreaseAvailableQuantityIfEnough(userFruit.getId(), sellQuantity, userId, now);
            if (fruitUpdated <= 0) {
                throw new ServiceException(BizErrorCode.FRUIT_NOT_ENOUGH, "果实库存不足");
            }
            int coinUpdated = userDao.increaseCoin(userId, totalIncomeCoin, userId, now);
            if (coinUpdated <= 0) {
                throw new ServiceException(BizErrorCode.USER_NOT_FOUND, "用户不存在");
            }
            UserFruit latestUserFruit = ServiceGuard.requirePresent(
                    userFruitDao.findByUserIdAndSeedTypeIdAndIsDeletedFalse(userId, seedTypeId),
                    BizErrorCode.FRUIT_INVENTORY_NOT_FOUND,
                    "果实库存不存在"
            );
            long afterFruitQuantity = defaultLong(latestUserFruit.getQuantity(), 0L);
            long beforeFruitQuantityAccurate = afterFruitQuantity + sellQuantity;
            User latestUser = ServiceGuard.requirePresent(
                    userDao.findByIdAndIsDeletedFalse(userId),
                    BizErrorCode.USER_NOT_FOUND,
                    "用户不存在"
            );
            long afterCoin = defaultLong(latestUser.getCoin(), 0L);
            long beforeCoin = afterCoin - totalIncomeCoin;

            String bizId = seedType.getId() + ":" + now.toEpochSecond();
            UserInventoryFlow inventoryFlow = new UserInventoryFlow();
            initNewEntity(inventoryFlow, userId, now);
            inventoryFlow.setUserId(user.getId());
            inventoryFlow.setItemType("FRUIT");
            inventoryFlow.setSeedTypeId(seedType.getId());
            inventoryFlow.setOperationType("EXPENSE");
            inventoryFlow.setChangeAmount(sellQuantity);
            inventoryFlow.setBeforeAmount(beforeFruitQuantityAccurate);
            inventoryFlow.setAfterAmount(afterFruitQuantity);
            inventoryFlow.setBeforeFrozenAmount(frozenFruitQuantity);
            inventoryFlow.setAfterFrozenAmount(frozenFruitQuantity);
            inventoryFlow.setBizType("SELL_FRUIT");
            inventoryFlow.setBizId(bizId);
            inventoryFlow.setOccurredAt(now);
            inventoryFlow.setExtData("{\"seedTypeId\":" + seedType.getId() + ",\"unitFruitPrice\":" + unitFruitPrice + "}");
            userInventoryFlowDao.save(inventoryFlow);

            UserAssetFlow assetFlow = new UserAssetFlow();
            initNewEntity(assetFlow, userId, now);
            assetFlow.setUserId(user.getId());
            assetFlow.setAssetType("COIN");
            assetFlow.setOperationType("INCOME");
            assetFlow.setChangeAmount(totalIncomeCoin);
            assetFlow.setBeforeAmount(beforeCoin);
            assetFlow.setAfterAmount(afterCoin);
            assetFlow.setBizType("SELL_FRUIT");
            assetFlow.setBizId(bizId);
            assetFlow.setOccurredAt(now);
            assetFlow.setExtData("{\"seedTypeId\":" + seedType.getId() + ",\"sellQuantity\":" + sellQuantity + "}");
            userAssetFlowDao.save(assetFlow);

            SeedShopSellFruitResultVO result = new SeedShopSellFruitResultVO();
            result.setUserId(user.getId());
            result.setSeedTypeId(seedType.getId());
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
        Long userId = ServiceGuard.requirePositive(request.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
        ServiceGuard.requirePresent(userDao.findByIdAndIsDeletedFalse(userId), BizErrorCode.USER_NOT_FOUND, "用户不存在");
        int pageNo = normalizePageNo(request.getPage());
        int pageSize = normalizePageSize(request.getRows());
        String tradeTypeFilter = normalizeTradeType(request.getTradeType());
        if (request.getTradeType() != null && !request.getTradeType().isBlank() && tradeTypeFilter.isBlank()) {
            throw new ServiceException(BizErrorCode.TRADE_TYPE_UNSUPPORTED, "tradeType 仅支持 BUY 或 SELL");
        }

        List<UserAssetFlow> assetFlows = userAssetFlowDao.findByUserIdAndIsDeletedFalseOrderByOccurredAtDesc(userId);
        Map<String, UserInventoryFlow> inventoryByBizId = new HashMap<>();
        List<UserInventoryFlow> seedFlows = userInventoryFlowDao.findByUserIdAndItemTypeAndIsDeletedFalseOrderByOccurredAtDesc(userId, "SEED");
        List<UserInventoryFlow> fruitFlows = userInventoryFlowDao.findByUserIdAndItemTypeAndIsDeletedFalseOrderByOccurredAtDesc(userId, "FRUIT");
        for (UserInventoryFlow flow : seedFlows) {
            if (flow.getBizId() != null && !inventoryByBizId.containsKey(flow.getBizId())) {
                inventoryByBizId.put(flow.getBizId(), flow);
            }
        }
        for (UserInventoryFlow flow : fruitFlows) {
            if (flow.getBizId() != null && !inventoryByBizId.containsKey(flow.getBizId())) {
                inventoryByBizId.put(flow.getBizId(), flow);
            }
        }

        Map<Long, SeedType> seedTypeMap = buildSeedTypeMap();
        List<SeedShopTradeRecordVO> records = new ArrayList<>();
        for (UserAssetFlow assetFlow : assetFlows) {
            String bizType = safeString(assetFlow.getBizType()).toUpperCase();
            if (!"BUY_SEED".equals(bizType) && !"SELL_FRUIT".equals(bizType)) {
                continue;
            }
            String tradeType = "BUY_SEED".equals(bizType) ? "BUY" : "SELL";
            if (!tradeTypeFilter.isBlank() && !tradeTypeFilter.equals(tradeType)) {
                continue;
            }
            UserInventoryFlow inventoryFlow = inventoryByBizId.get(assetFlow.getBizId());
            Long seedTypeId = inventoryFlow == null ? extractLongFromExtData(assetFlow.getExtData(), "seedTypeId") : inventoryFlow.getSeedTypeId();
            SeedType seedType = seedTypeId == null ? null : seedTypeMap.get(seedTypeId);

            SeedShopTradeRecordVO record = new SeedShopTradeRecordVO();
            record.setBizId(safeString(assetFlow.getBizId()));
            record.setTradeType(tradeType);
            record.setSeedTypeId(seedTypeId);
            record.setSeedName(seedType == null ? "" : safeString(seedType.getName()));
            record.setItemQuantity(inventoryFlow == null ? 0L : defaultLong(inventoryFlow.getChangeAmount(), 0L));
            record.setItemType(inventoryFlow == null ? ("BUY".equals(tradeType) ? "SEED" : "FRUIT") : safeString(inventoryFlow.getItemType()));
            record.setItemOperationType(inventoryFlow == null ? "" : safeString(inventoryFlow.getOperationType()));
            record.setCoinChangeAmount(defaultLong(assetFlow.getChangeAmount(), 0L));
            record.setCoinOperationType(safeString(assetFlow.getOperationType()));
            record.setOccurredAt(assetFlow.getOccurredAt());
            records.add(record);
        }

        long total = records.size();
        int fromIndex = Math.min((pageNo - 1) * pageSize, records.size());
        int toIndex = Math.min(fromIndex + pageSize, records.size());
        List<SeedShopTradeRecordVO> pageRecords = records.subList(fromIndex, toIndex);
        return new PageResult<>(pageNo, pageSize, total, pageRecords);
    }

    @Override
    public PageResult<SeedFruitInventoryItemVO> pageFruitInventory(SeedFruitInventoryQueryDTO query) {
        SeedFruitInventoryQueryDTO request = query == null ? new SeedFruitInventoryQueryDTO() : query;
        Long userId = ServiceGuard.requirePositive(request.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
        ServiceGuard.requirePresent(userDao.findByIdAndIsDeletedFalse(userId), BizErrorCode.USER_NOT_FOUND, "用户不存在");
        int pageNo = normalizePageNo(request.getPage());
        int pageSize = normalizePageSize(request.getRows());
        String nameKeyword = safeString(request.getName()).trim().toLowerCase();

        List<UserFruit> fruits = userFruitDao.findByUserIdAndIsDeletedFalseOrderByIdAsc(userId);
        Map<Long, SeedType> seedTypeMap = buildSeedTypeMap();
        List<SeedFruitInventoryItemVO> items = new ArrayList<>();
        for (UserFruit fruit : fruits) {
            SeedType seedType = seedTypeMap.get(fruit.getSeedTypeId());
            if (seedType == null) {
                continue;
            }
            String seedName = safeString(seedType.getName());
            if (!nameKeyword.isBlank() && !seedName.toLowerCase().contains(nameKeyword)) {
                continue;
            }

            long fruitQuantity = defaultLong(fruit.getQuantity(), 0L);
            long frozenQuantity = defaultLong(fruit.getFrozenQuantity(), 0L);
            long availableQuantity = Math.max(fruitQuantity - frozenQuantity, 0L);
            long unitFruitPrice = defaultLong(seedType.getFruitPrice(), 0L);
            long estimatedIncomeCoin;
            try {
                estimatedIncomeCoin = Math.multiplyExact(availableQuantity, unitFruitPrice);
            } catch (ArithmeticException ex) {
                estimatedIncomeCoin = Long.MAX_VALUE;
            }

            SeedFruitInventoryItemVO item = new SeedFruitInventoryItemVO();
            item.setSeedTypeId(seedType.getId());
            item.setSeedName(seedName);
            item.setCoverImageUrl(safeString(seedType.getCoverImageUrl()));
            item.setFruitQuantity(fruitQuantity);
            item.setFrozenQuantity(frozenQuantity);
            item.setAvailableQuantity(availableQuantity);
            item.setUnitFruitPrice(unitFruitPrice);
            item.setEstimatedIncomeCoin(estimatedIncomeCoin);
            items.add(item);
        }

        long total = items.size();
        int fromIndex = Math.min((pageNo - 1) * pageSize, items.size());
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        List<SeedFruitInventoryItemVO> pageRecords = items.subList(fromIndex, toIndex);
        return new PageResult<>(pageNo, pageSize, total, pageRecords);
    }

    @Override
    public SeedShopOverviewVO shopOverview(SeedShopOverviewDTO query) {
        Long userId = ServiceGuard.requirePositive(query == null ? null : query.getUserId(), BizErrorCode.PARAM_INVALID, "用户ID无效");
        User user = ServiceGuard.requirePresent(userDao.findByIdAndIsDeletedFalse(userId), BizErrorCode.USER_NOT_FOUND, "用户不存在");

        long currentCoin = defaultLong(user.getCoin(), 0L);
        List<UserFruit> fruits = userFruitDao.findByUserIdAndIsDeletedFalseOrderByIdAsc(userId);
        Map<Long, SeedType> seedTypeMap = buildSeedTypeMap();
        long sellableTotalValue = 0L;
        long sellableFruitTotalCount = 0L;
        for (UserFruit fruit : fruits) {
            SeedType seedType = seedTypeMap.get(fruit.getSeedTypeId());
            if (seedType == null) {
                continue;
            }
            long quantity = defaultLong(fruit.getQuantity(), 0L);
            long frozen = defaultLong(fruit.getFrozenQuantity(), 0L);
            long sellable = Math.max(quantity - frozen, 0L);
            long unitPrice = defaultLong(seedType.getFruitPrice(), 0L);
            sellableFruitTotalCount += sellable;
            try {
                sellableTotalValue = Math.addExact(sellableTotalValue, Math.multiplyExact(sellable, unitPrice));
            } catch (ArithmeticException ex) {
                sellableTotalValue = Long.MAX_VALUE;
            }
        }

        int purchasableSeedTypeCount = 0;
        List<SeedType> seedTypes = seedTypeDao.findByIsDeletedFalseOrderByIdAsc();
        for (SeedType seedType : seedTypes) {
            if (defaultLong(seedType.getPrice(), 0L) <= currentCoin) {
                purchasableSeedTypeCount++;
            }
        }

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
        SeedShopOverviewVO overview = shopOverview(overviewDTO);

        SeedShopQueryDTO pageDTO = new SeedShopQueryDTO();
        pageDTO.setName(request.getName());
        pageDTO.setSeedQualityId(request.getSeedQualityId());
        pageDTO.setLevel(request.getLevel());
        pageDTO.setPage(request.getPage());
        pageDTO.setRows(request.getRows());
        pageDTO.setSort(request.getSort());
        pageDTO.setOrder(request.getOrder());
        PageResult<SeedShopItemVO> shopPage = pageSeedShop(pageDTO);

        SeedShopHomeVO result = new SeedShopHomeVO();
        result.setOverview(overview);
        result.setShopPage(shopPage);
        return result;
    }

    @Override
    @Transactional
    public Long saveSeedType(SeedAddOrUpdateDTO params) {
        ServiceGuard.requireNotNull(params, BizErrorCode.PARAM_INVALID, "请求参数不能为空");
        String name = safeString(params.getName()).trim();
        if (name.isBlank()) {
            throw new ServiceException(BizErrorCode.SEED_NAME_REQUIRED, "种子名称不能为空");
        }

        Long id = params.getId();
        SeedType entity;
        if (id != null && id > 0) {
            entity = ServiceGuard.requirePresent(seedTypeDao.findByIdAndIsDeletedFalse(id), BizErrorCode.SEED_TYPE_NOT_FOUND, "种子类型不存在");
        } else {
            entity = new SeedType();
            initNewEntity(entity, 0L, OffsetDateTime.now());
        }

        Long seedQualityId = params.getSeedQualityId() == null || params.getSeedQualityId() <= 0 ? 1L : params.getSeedQualityId();
        ServiceGuard.requirePresent(seedQualityDao.findByIdAndIsDeletedFalse(seedQualityId), BizErrorCode.SEED_QUALITY_NOT_FOUND, "种子品质不存在");
        Long enableSoilTypeBits = resolveSoilTypeBits(params);
        if (enableSoilTypeBits <= 0) {
            throw new ServiceException(BizErrorCode.SOIL_TYPE_REQUIRED, "至少需要选择一种土壤类型");
        }

        entity.setName(name);
        entity.setCoverImageUrl(nonBlank(params.getCoverImageUrl(), ""));
        entity.setSeedQualityId(seedQualityId);
        entity.setEnableSoilTypeBits(enableSoilTypeBits);
        entity.setLevel(resolveSeedLevel(params));
        entity.setDescription(nonBlank(params.getDescription(), nonBlank(params.getTips(), "")));
        entity.setMaxBugLimit(params.getMaxBugLimit() == null ? (short) 0 : params.getMaxBugLimit());
        entity.setMaxHarvestCount(params.getMaxHarvestCount() == null ? (short) 1 : params.getMaxHarvestCount());
        entity.setRegrowStageIndex(params.getRegrowStageIndex());
        entity.setPrice(resolveLong(params.getPrice(), params.getBuyPrice(), 0L));
        entity.setHarvestExperience(resolveLong(params.getHarvestExperience(), params.getExp(), 0L));
        entity.setHarvestFruitNumber(resolveInt(params.getHarvestFruitNumber(), params.getHarvestCount(), 0));
        entity.setFruitPrice(defaultLong(params.getFruitPrice(), 0L));
        entity.setHarvestScore(resolveLong(params.getHarvestScore(), params.getScore(), 0L));
        touchForUpdate(entity, 0L, OffsetDateTime.now());
        return seedTypeDao.save(entity).getId();
    }

    @Override
    @Transactional
    public void removeSeedType(IdDTO params) {
        Long id = ServiceGuard.requirePositive(params == null ? null : params.getId(), BizErrorCode.PARAM_INVALID, "阶段ID无效");
        SeedType seedType = ServiceGuard.requirePresent(seedTypeDao.findByIdAndIsDeletedFalse(id), BizErrorCode.SEED_TYPE_NOT_FOUND, "种子类型不存在");
        seedType.setIsDeleted(true);
        touchForUpdate(seedType, 0L, OffsetDateTime.now());
        seedTypeDao.save(seedType);
    }

    @Override
    public List<OptionVO> listSeedQualityOptions() {
        List<SeedQuality> list = seedQualityDao.findByIsDeletedFalseOrderByIdAsc();
        List<OptionVO> result = new ArrayList<>();
        for (SeedQuality item : list) {
            OptionVO vo = new OptionVO();
            vo.setId(item.getId());
            vo.setText(safeString(item.getName()));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<SoilOptionVO> listSoilOptions() {
        List<SoilType> list = soilTypeDao.findByIsDeletedFalseOrderByIdAsc();
        List<SoilOptionVO> result = new ArrayList<>();
        for (SoilType item : list) {
            SoilOptionVO vo = new SoilOptionVO();
            vo.setId(item.getId());
            vo.setText(safeString(item.getName()));
            vo.setBitCode(item.getBitCode());
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<OptionVO> listGrowthStageOptions() {
        List<GrowthStage> list = growthStageDao.findByIsDeletedFalseOrderByIdAsc();
        List<OptionVO> result = new ArrayList<>();
        for (GrowthStage item : list) {
            OptionVO vo = new OptionVO();
            vo.setId(item.getId());
            vo.setText(safeString(item.getName()));
            result.add(vo);
        }
        return result;
    }

    @Override
    public PageResult<SeedStageGridVO> pageSeedStages(SeedStageQueryDTO query) {
        Long seedTypeId = ServiceGuard.requirePositive(query == null ? null : query.getSeedTypeId(), BizErrorCode.PARAM_INVALID, "种子类型ID无效");
        SeedType seedType = ServiceGuard.requirePresent(seedTypeDao.findByIdAndIsDeletedFalse(seedTypeId), BizErrorCode.SEED_TYPE_NOT_FOUND, "种子类型不存在");
        List<SeedGrowthStage> list = seedGrowthStageDao.findBySeedTypeIdAndIsDeletedFalseOrderByStageIndexAsc(seedTypeId);
        Map<Long, String> growthNameMap = buildGrowthStageNameMap();
        List<SeedStageGridVO> records = new ArrayList<>();
        for (SeedGrowthStage item : list) {
            SeedStageGridVO row = new SeedStageGridVO();
            row.setId(item.getId());
            row.setSeedTypeId(item.getSeedTypeId());
            row.setSeedName(safeString(seedType.getName()));
            row.setGrowthStageId(item.getGrowthStageId());
            row.setGrowthStageName(safeString(growthNameMap.get(item.getGrowthStageId())));
            row.setStageIndex(item.getStageIndex());
            row.setDurationSeconds(item.getDurationSeconds());
            row.setBugProbability(item.getBugProbability());
            row.setWidth(item.getWidth());
            row.setHeight(item.getHeight());
            row.setOffsetX(item.getOffsetX());
            row.setOffsetY(item.getOffsetY());
            row.setAssetUrl(safeString(item.getAssetUrl()));
            records.add(row);
        }
        return new PageResult<>(1L, records.size(), records.size(), records);
    }

    @Override
    @Transactional
    public void saveSeedStage(SeedStageAddOrUpdateDTO params) {
        ServiceGuard.requireNotNull(params, BizErrorCode.PARAM_INVALID, "请求参数不能为空");
        Long seedTypeId = ServiceGuard.requirePositive(params.getSeedTypeId(), BizErrorCode.PARAM_INVALID, "种子类型ID无效");
        SeedType seedType = ServiceGuard.requirePresent(seedTypeDao.findByIdAndIsDeletedFalse(seedTypeId), BizErrorCode.SEED_TYPE_NOT_FOUND, "种子类型不存在");

        Long growthStageId = ServiceGuard.requirePositive(params.getGrowthStageId(), BizErrorCode.PARAM_INVALID, "生长阶段ID无效");
        ServiceGuard.requirePresent(growthStageDao.findByIdAndIsDeletedFalse(growthStageId), BizErrorCode.GROWTH_STAGE_NOT_FOUND, "生长阶段不存在");

        short stageIndex = params.getStageIndex() != null && params.getStageIndex() > 0
                ? params.getStageIndex()
                : (params.getGrowthStage() == null || params.getGrowthStage() <= 0 ? (short) 1 : params.getGrowthStage());

        SeedGrowthStage entity;
        if (params.getId() != null && params.getId() > 0) {
            entity = ServiceGuard.requirePresent(seedGrowthStageDao.findByIdAndIsDeletedFalse(params.getId()), BizErrorCode.SEED_STAGE_NOT_FOUND, "种子阶段配置不存在");
        } else {
            entity = seedGrowthStageDao.findBySeedTypeIdAndStageIndexAndIsDeletedFalse(seedTypeId, stageIndex).orElseGet(SeedGrowthStage::new);
            if (entity.getId() == null) {
                initNewEntity(entity, 0L, OffsetDateTime.now());
            }
        }

        entity.setSeedTypeId(seedType.getId());
        entity.setGrowthStageId(growthStageId);
        entity.setStageIndex(stageIndex);
        entity.setDurationSeconds(defaultInt(params.getDurationSeconds(), 0));
        entity.setBugProbability(defaultBigDecimal(params.getBugProbability(), params.getPestProbability(), BigDecimal.ZERO));
        entity.setWidth(defaultInt(params.getWidth(), 0));
        entity.setHeight(defaultInt(params.getHeight(), 0));
        entity.setOffsetX(defaultInt(params.getOffsetX(), 0));
        entity.setOffsetY(defaultInt(params.getOffsetY(), 0));
        entity.setAssetUrl(nonBlank(params.getAssetUrl(), ""));
        touchForUpdate(entity, 0L, OffsetDateTime.now());
        seedGrowthStageDao.save(entity);
    }

    @Override
    @Transactional
    public void removeSeedStage(IdDTO params) {
        Long id = ServiceGuard.requirePositive(params == null ? null : params.getId(), BizErrorCode.PARAM_INVALID, "阶段ID无效");
        SeedGrowthStage entity = ServiceGuard.requirePresent(seedGrowthStageDao.findByIdAndIsDeletedFalse(id), BizErrorCode.SEED_STAGE_NOT_FOUND, "种子阶段配置不存在");
        entity.setIsDeleted(true);
        touchForUpdate(entity, 0L, OffsetDateTime.now());
        seedGrowthStageDao.save(entity);
    }

    private SeedGridVO buildSeedGridVO(SeedType seedType,
                                       Map<Long, SeedQuality> qualityMap,
                                       Map<Integer, String> soilNameByBitCode) {
        SeedGridVO vo = new SeedGridVO();
        vo.setId(seedType.getId());
        vo.setName(safeString(seedType.getName()));
        vo.setCoverImageUrl(safeString(seedType.getCoverImageUrl()));
        vo.setSeedQualityId(seedType.getSeedQualityId());
        vo.setSeedQualityName(resolveSeedQualityName(qualityMap, seedType.getSeedQualityId()));
        vo.setEnableSoilTypeBits(defaultLong(seedType.getEnableSoilTypeBits(), 0L));
        vo.setEnableSoilTypeNames(resolveSoilName(seedType.getEnableSoilTypeBits(), soilNameByBitCode));
        vo.setLevel(seedType.getLevel());
        vo.setDescription(safeString(seedType.getDescription()));
        vo.setMaxBugLimit(seedType.getMaxBugLimit());
        vo.setMaxHarvestCount(seedType.getMaxHarvestCount());
        vo.setRegrowStageIndex(seedType.getRegrowStageIndex());
        vo.setPrice(defaultLong(seedType.getPrice(), 0L));
        vo.setHarvestExperience(defaultLong(seedType.getHarvestExperience(), 0L));
        vo.setHarvestFruitNumber(defaultInt(seedType.getHarvestFruitNumber(), 0));
        vo.setFruitPrice(defaultLong(seedType.getFruitPrice(), 0L));
        vo.setHarvestScore(defaultLong(seedType.getHarvestScore(), 0L));
        vo.setTotalGrowSeconds(getTotalGrowSeconds(seedType.getId()));
        return vo;
    }

    private SeedShopItemVO buildSeedShopItemVO(SeedType seedType,
                                               Map<Long, SeedQuality> qualityMap,
                                               Map<Integer, String> soilNameByBitCode) {
        SeedShopItemVO vo = new SeedShopItemVO();
        vo.setId(seedType.getId());
        vo.setName(safeString(seedType.getName()));
        vo.setCoverImageUrl(safeString(seedType.getCoverImageUrl()));
        vo.setSeedQualityId(seedType.getSeedQualityId());
        vo.setSeedQualityName(resolveSeedQualityName(qualityMap, seedType.getSeedQualityId()));
        vo.setLevel(seedType.getLevel());
        vo.setEnableSoilTypeBits(defaultLong(seedType.getEnableSoilTypeBits(), 0L));
        vo.setEnableSoilTypeNames(resolveSoilName(seedType.getEnableSoilTypeBits(), soilNameByBitCode));
        vo.setDescription(safeString(seedType.getDescription()));
        vo.setPrice(defaultLong(seedType.getPrice(), 0L));
        vo.setHarvestFruitNumber(defaultInt(seedType.getHarvestFruitNumber(), 0));
        vo.setFruitPrice(defaultLong(seedType.getFruitPrice(), 0L));
        vo.setHarvestExperience(defaultLong(seedType.getHarvestExperience(), 0L));
        vo.setHarvestScore(defaultLong(seedType.getHarvestScore(), 0L));
        vo.setMaxHarvestCount(seedType.getMaxHarvestCount() == null ? (short) 1 : seedType.getMaxHarvestCount());
        vo.setTotalGrowSeconds(getTotalGrowSeconds(seedType.getId()));
        long singleHarvestFruitValue = safeMultiply(defaultInt(seedType.getHarvestFruitNumber(), 0), defaultLong(seedType.getFruitPrice(), 0L));
        long totalHarvestFruitValue = safeMultiply(singleHarvestFruitValue, seedType.getMaxHarvestCount() == null ? 1L : (long) seedType.getMaxHarvestCount());
        vo.setSingleHarvestFruitValue(singleHarvestFruitValue);
        vo.setTotalHarvestFruitValue(totalHarvestFruitValue);
        vo.setEstimatedNetValue(totalHarvestFruitValue - defaultLong(seedType.getPrice(), 0L));
        return vo;
    }

    private Map<Long, SeedQuality> buildSeedQualityMap() {
        Map<Long, SeedQuality> result = new HashMap<>();
        for (SeedQuality quality : seedQualityDao.findByIsDeletedFalseOrderByIdAsc()) {
            result.put(quality.getId(), quality);
        }
        return result;
    }

    private Map<Long, SeedType> buildSeedTypeMap() {
        Map<Long, SeedType> result = new HashMap<>();
        for (SeedType seedType : seedTypeDao.findByIsDeletedFalseOrderByIdAsc()) {
            result.put(seedType.getId(), seedType);
        }
        return result;
    }

    private Map<Integer, String> buildSoilNameByBitCode() {
        Map<Integer, String> map = new LinkedHashMap<>();
        for (SoilType soilType : soilTypeDao.findByIsDeletedFalseOrderByIdAsc()) {
            map.put(soilType.getBitCode(), safeString(soilType.getName()));
        }
        return map;
    }

    private Map<Long, String> buildGrowthStageNameMap() {
        Map<Long, String> map = new HashMap<>();
        for (GrowthStage stage : growthStageDao.findByIsDeletedFalseOrderByIdAsc()) {
            map.put(stage.getId(), safeString(stage.getName()));
        }
        return map;
    }

    private String resolveSeedQualityName(Map<Long, SeedQuality> qualityMap, Long seedQualityId) {
        SeedQuality quality = qualityMap.get(seedQualityId);
        return quality == null ? "" : safeString(quality.getName());
    }

    private String resolveSoilName(Long soilBits, Map<Integer, String> soilNameByBitCode) {
        long bits = defaultLong(soilBits, 0L);
        if (bits <= 0) {
            return "";
        }
        List<String> names = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : soilNameByBitCode.entrySet()) {
            Integer bitCode = entry.getKey();
            if (bitCode == null || bitCode <= 0) {
                continue;
            }
            if ((bits & bitCode) == bitCode) {
                names.add(entry.getValue());
            }
        }
        return String.join("/", names);
    }

    private int getTotalGrowSeconds(Long seedTypeId) {
        if (seedTypeId == null || seedTypeId <= 0) {
            return 0;
        }
        int total = 0;
        List<SeedGrowthStage> list = seedGrowthStageDao.findBySeedTypeIdAndIsDeletedFalseOrderByStageIndexAsc(seedTypeId);
        for (SeedGrowthStage stage : list) {
            total += defaultInt(stage.getDurationSeconds(), 0);
        }
        return total;
    }

    private Comparator<SeedShopItemVO> resolveShopComparator(String sort, String order) {
        String sortField = safeString(sort).trim().toLowerCase();
        Comparator<SeedShopItemVO> comparator;
        if ("price".equals(sortField)) {
            comparator = Comparator.comparing(item -> defaultLong(item.getPrice(), 0L));
        } else if ("fruitprice".equals(sortField)) {
            comparator = Comparator.comparing(item -> defaultLong(item.getFruitPrice(), 0L));
        } else if ("level".equals(sortField)) {
            comparator = Comparator.comparing(item -> item.getLevel() == null ? 0 : item.getLevel().intValue());
        } else if ("totalharvestfruitvalue".equals(sortField)) {
            comparator = Comparator.comparing(item -> defaultLong(item.getTotalHarvestFruitValue(), 0L));
        } else if ("estimatednetvalue".equals(sortField)) {
            comparator = Comparator.comparing(item -> defaultLong(item.getEstimatedNetValue(), 0L));
        } else {
            comparator = Comparator.comparing(item -> defaultLong(item.getId(), 0L));
        }
        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    private String safeSeedSortField(String sort) {
        String value = safeString(sort).trim();
        if ("id".equalsIgnoreCase(value)) {
            return "id";
        }
        if ("name".equalsIgnoreCase(value)) {
            return "name";
        }
        if ("level".equalsIgnoreCase(value)) {
            return "level";
        }
        if ("price".equalsIgnoreCase(value)) {
            return "price";
        }
        if ("fruitPrice".equalsIgnoreCase(value)) {
            return "fruitPrice";
        }
        if ("harvestScore".equalsIgnoreCase(value)) {
            return "harvestScore";
        }
        return "id";
    }

    private int normalizePageNo(Integer pageNo) {
        return pageNo == null || pageNo <= 0 ? DEFAULT_PAGE_NO : pageNo;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private String normalizeTradeType(String tradeType) {
        String value = safeString(tradeType).trim().toUpperCase();
        if (value.isBlank()) {
            return "";
        }
        if ("BUY".equals(value) || "BUY_SEED".equals(value)) {
            return "BUY";
        }
        if ("SELL".equals(value) || "SELL_FRUIT".equals(value)) {
            return "SELL";
        }
        return "";
    }

    private Long extractLongFromExtData(String extData, String key) {
        String text = safeString(extData);
        if (text.isBlank()) {
            return null;
        }
        String marker = "\"" + key + "\":";
        int startIndex = text.indexOf(marker);
        if (startIndex < 0) {
            return null;
        }
        int begin = startIndex + marker.length();
        int end = begin;
        while (end < text.length() && Character.isDigit(text.charAt(end))) {
            end++;
        }
        if (end <= begin) {
            return null;
        }
        try {
            return Long.parseLong(text.substring(begin, end));
        } catch (Exception ex) {
            return null;
        }
    }

    private long safeMultiply(long a, long b) {
        try {
            return Math.multiplyExact(a, b);
        } catch (ArithmeticException ex) {
            return Long.MAX_VALUE;
        }
    }

    private UserSeed createUserSeed(Long userId, Long seedTypeId, OffsetDateTime now) {
        UserSeed entity = new UserSeed();
        initNewEntity(entity, userId, now);
        entity.setUserId(userId);
        entity.setSeedTypeId(seedTypeId);
        entity.setQuantity(0L);
        entity.setFrozenQuantity(0L);
        return entity;
    }

    private Long resolveSoilTypeBits(SeedAddOrUpdateDTO params) {
        if (params.getEnableSoilTypeBits() != null && params.getEnableSoilTypeBits() > 0) {
            return params.getEnableSoilTypeBits();
        }
        List<Long> soilTypeIds = parseIds(params.getSoilTypeIds());
        if (soilTypeIds.isEmpty() && params.getSoilTypeId() != null && !params.getSoilTypeId().isBlank()) {
            soilTypeIds = parseIds(params.getSoilTypeId());
        }
        if (soilTypeIds.isEmpty()) {
            return 0L;
        }
        long bits = 0L;
        for (Long soilTypeId : soilTypeIds) {
            if (soilTypeId == null || soilTypeId <= 0) {
                continue;
            }
            Optional<SoilType> optional = soilTypeDao.findByIdAndIsDeletedFalse(soilTypeId);
            if (optional.isPresent() && optional.get().getBitCode() != null && optional.get().getBitCode() > 0) {
                bits |= optional.get().getBitCode();
            }
        }
        return bits;
    }

    private List<Long> parseIds(String text) {
        List<Long> result = new ArrayList<>();
        String value = safeString(text).trim();
        if (value.isBlank()) {
            return result;
        }
        String[] parts = value.split(",");
        for (String part : parts) {
            String trim = part.trim();
            if (trim.isBlank()) {
                continue;
            }
            try {
                result.add(Long.parseLong(trim));
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    private Short resolveSeedLevel(SeedAddOrUpdateDTO params) {
        if (params.getLevel() != null && params.getLevel() > 0) {
            return params.getLevel();
        }
        String season = safeString(params.getSeason()).trim();
        if (!season.isBlank()) {
            try {
                short parsed = Short.parseShort(season);
                if (parsed > 0) {
                    return parsed;
                }
            } catch (Exception ignored) {
            }
        }
        return (short) 1;
    }

    private Long resolveLong(Long first, Long second, Long defaultValue) {
        if (first != null) {
            return first;
        }
        if (second != null) {
            return second;
        }
        return defaultValue;
    }

    private Integer resolveInt(Integer first, Integer second, Integer defaultValue) {
        if (first != null) {
            return first;
        }
        if (second != null) {
            return second;
        }
        return defaultValue;
    }

    private BigDecimal defaultBigDecimal(BigDecimal primary, BigDecimal fallback, BigDecimal defaultValue) {
        if (primary != null) {
            return primary;
        }
        if (fallback != null) {
            return fallback;
        }
        return defaultValue;
    }

    private String nonBlank(String value, String defaultValue) {
        String text = safeString(value).trim();
        return text.isBlank() ? defaultValue : text;
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private Long defaultLong(Long value, Long defaultValue) {
        return value == null ? defaultValue : value;
    }

    private Integer defaultInt(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
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
}

