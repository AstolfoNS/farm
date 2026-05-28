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
import cn.jxufe.farm.common.enums.BizErrorCode;
import cn.jxufe.farm.common.exception.ServiceException;
import cn.jxufe.farm.common.pages.PageResult;
import cn.jxufe.farm.dao.SeedGrowthStageDao;
import cn.jxufe.farm.dao.SeedTypeDao;
import cn.jxufe.farm.dao.UserCropDao;
import cn.jxufe.farm.dao.UserFruitDao;
import cn.jxufe.farm.dao.UserInventoryFlowDao;
import cn.jxufe.farm.dao.UserSeedDao;
import cn.jxufe.farm.entity.SeedGrowthStage;
import cn.jxufe.farm.entity.SeedType;
import cn.jxufe.farm.service.SeedService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private final SeedGrowthStageDao seedGrowthStageDao;
    private final UserSeedDao userSeedDao;
    private final UserFruitDao userFruitDao;
    private final UserCropDao userCropDao;
    private final UserInventoryFlowDao userInventoryFlowDao;

    public SeedServiceGuardedDecorator(
            @Qualifier("seedServiceImp") SeedService delegate,
            SeedTypeDao seedTypeDao,
            SeedGrowthStageDao seedGrowthStageDao,
            UserSeedDao userSeedDao,
            UserFruitDao userFruitDao,
            UserCropDao userCropDao,
            UserInventoryFlowDao userInventoryFlowDao
    ) {
        this.delegate = delegate;
        this.seedTypeDao = seedTypeDao;
        this.seedGrowthStageDao = seedGrowthStageDao;
        this.userSeedDao = userSeedDao;
        this.userFruitDao = userFruitDao;
        this.userCropDao = userCropDao;
        this.userInventoryFlowDao = userInventoryFlowDao;
    }

    @Override
    public PageResult<SeedGridVO> pageSeedTypes(SeedTypeQueryDTO query) {
        return delegate.pageSeedTypes(query);
    }

    @Override
    public PageResult<SeedShopItemVO> pageSeedShop(SeedShopQueryDTO query) {
        return delegate.pageSeedShop(query);
    }

    @Override
    public SeedShopBuyResultVO buySeed(SeedShopBuyDTO params) {
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
        return delegate.pageFruitInventory(query);
    }

    @Override
    public SeedShopOverviewVO shopOverview(SeedShopOverviewDTO query) {
        return delegate.shopOverview(query);
    }

    @Override
    public SeedShopHomeVO shopHome(SeedShopHomeQueryDTO query) {
        return delegate.shopHome(query);
    }

    @Override
    @Transactional
    public Long saveSeedType(SeedAddOrUpdateDTO params) {
        Long seedTypeId = delegate.saveSeedType(params);
        validateStageSequenceAndRegrow(seedTypeId);
        validateRegrowStageIndex(seedTypeId, params == null ? null : params.getRegrowStageIndex());
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
        return delegate.pageSeedStages(query);
    }

    @Override
    @Transactional
    public void saveSeedStage(SeedStageAddOrUpdateDTO params) {
        validateStageAssetUrl(params == null ? null : params.getAssetUrl());
        validateStageLayout(params);
        delegate.saveSeedStage(params);
        Long seedTypeId = params == null ? null : params.getSeedTypeId();
        validateStageSequenceAndRegrow(seedTypeId);
    }

    @Override
    @Transactional
    public void removeSeedStage(IdDTO params) {
        Long stageId = params == null ? null : params.getId();
        if (stageId == null || stageId <= 0) {
            throw new ServiceException(BizErrorCode.PARAM_INVALID, "阶段ID无效");
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
            throw new ServiceException(BizErrorCode.SEED_STAGE_SEQUENCE_INVALID, "只能删除最后一个阶段，避免阶段序号断档");
        }

        delegate.removeSeedStage(params);
        validateStageSequenceAndRegrow(target.getSeedTypeId());
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

    private void validateStageSequenceAndRegrow(Long seedTypeId) {
        if (seedTypeId == null || seedTypeId <= 0) {
            throw new ServiceException(BizErrorCode.PARAM_INVALID, "种子类型ID无效");
        }
        List<SeedGrowthStage> stages = seedGrowthStageDao.findBySeedTypeIdAndIsDeletedFalseOrderByStageIndexAsc(seedTypeId);
        if (stages.isEmpty()) {
            return;
        }
        short expect = 1;
        Set<Short> stageIndexSet = new HashSet<>();
        for (SeedGrowthStage stage : stages) {
            short actual = stage.getStageIndex() == null ? 0 : stage.getStageIndex();
            if (actual != expect) {
                throw new ServiceException(BizErrorCode.SEED_STAGE_SEQUENCE_INVALID, "阶段序号必须连续为 1..N");
            }
            stageIndexSet.add(actual);
            expect++;
        }
        SeedType seedType = seedTypeDao.findByIdAndIsDeletedFalse(seedTypeId)
                .orElseThrow(() -> new ServiceException(BizErrorCode.SEED_TYPE_NOT_FOUND, "种子类型不存在"));
        Short regrow = seedType.getRegrowStageIndex();
        if (regrow != null && !stageIndexSet.contains(regrow)) {
            throw new ServiceException(BizErrorCode.SEED_REGROW_STAGE_INVALID, "再生阶段必须存在于该种子的阶段集合中");
        }
    }

    private void validateRegrowStageIndex(Long seedTypeId, Short regrowStageIndex) {
        if (seedTypeId == null || seedTypeId <= 0 || regrowStageIndex == null) {
            return;
        }
        if (regrowStageIndex <= 0) {
            throw new ServiceException(BizErrorCode.SEED_REGROW_STAGE_INVALID, "再生阶段必须大于0");
        }
        boolean exists = seedGrowthStageDao.findBySeedTypeIdAndIsDeletedFalseOrderByStageIndexAsc(seedTypeId).stream()
                .map(SeedGrowthStage::getStageIndex)
                .anyMatch(idx -> idx != null && idx.equals(regrowStageIndex));
        if (!exists) {
            throw new ServiceException(BizErrorCode.SEED_REGROW_STAGE_INVALID, "再生阶段必须存在于该种子的阶段集合中");
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
            throw new ServiceException(BizErrorCode.PARAM_INVALID, "请求参数不能为空");
        }
        int width = params.getWidth() == null ? 0 : params.getWidth();
        int height = params.getHeight() == null ? 0 : params.getHeight();
        int offsetX = params.getOffsetX() == null ? 0 : params.getOffsetX();
        int offsetY = params.getOffsetY() == null ? 0 : params.getOffsetY();
        if (width < STAGE_WIDTH_MIN || width > STAGE_WIDTH_MAX) {
            throw new ServiceException(BizErrorCode.SEED_STAGE_LAYOUT_INVALID, "width超出允许范围");
        }
        if (height < STAGE_HEIGHT_MIN || height > STAGE_HEIGHT_MAX) {
            throw new ServiceException(BizErrorCode.SEED_STAGE_LAYOUT_INVALID, "height超出允许范围");
        }
        if (offsetX < STAGE_OFFSET_MIN || offsetX > STAGE_OFFSET_MAX) {
            throw new ServiceException(BizErrorCode.SEED_STAGE_LAYOUT_INVALID, "offsetX超出允许范围");
        }
        if (offsetY < STAGE_OFFSET_MIN || offsetY > STAGE_OFFSET_MAX) {
            throw new ServiceException(BizErrorCode.SEED_STAGE_LAYOUT_INVALID, "offsetY超出允许范围");
        }
        BigDecimal probability = params.getBugProbability() != null ? params.getBugProbability() : params.getPestProbability();
        if (probability != null && (probability.compareTo(BigDecimal.ZERO) < 0 || probability.compareTo(BigDecimal.ONE) > 0)) {
            throw new ServiceException(BizErrorCode.SEED_STAGE_LAYOUT_INVALID, "虫害概率必须在0到1之间");
        }
    }
}
