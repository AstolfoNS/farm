package cn.jxufe.farm.imp;

import cn.jxufe.farm.model.bean.EasyUIData;
import cn.jxufe.farm.model.bean.EasyUIDataPageRequest;
import cn.jxufe.farm.model.bean.Message;
import cn.jxufe.farm.model.dao.GrowthStageDao;
import cn.jxufe.farm.model.dao.SeedGrowthStageDao;
import cn.jxufe.farm.model.dao.SeedQualityDao;
import cn.jxufe.farm.model.dao.SeedTypeDao;
import cn.jxufe.farm.model.dao.SoilTypeDao;
import cn.jxufe.farm.model.entity.GrowthStage;
import cn.jxufe.farm.model.entity.SeedGrowthStage;
import cn.jxufe.farm.model.entity.SeedQuality;
import cn.jxufe.farm.model.entity.SeedType;
import cn.jxufe.farm.model.entity.SoilType;
import cn.jxufe.farm.service.SeedService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SeedImp implements SeedService {

    private final SeedTypeDao seedTypeDao;
    private final SeedQualityDao seedQualityDao;
    private final SoilTypeDao soilTypeDao;
    private final GrowthStageDao growthStageDao;
    private final SeedGrowthStageDao seedGrowthStageDao;

    public SeedImp(SeedTypeDao seedTypeDao,
                   SeedQualityDao seedQualityDao,
                   SoilTypeDao soilTypeDao,
                   GrowthStageDao growthStageDao,
                   SeedGrowthStageDao seedGrowthStageDao) {
        this.seedTypeDao = seedTypeDao;
        this.seedQualityDao = seedQualityDao;
        this.soilTypeDao = soilTypeDao;
        this.growthStageDao = growthStageDao;
        this.seedGrowthStageDao = seedGrowthStageDao;
    }

    @Override
    public EasyUIData gridDataFilterSortPage(String name, EasyUIDataPageRequest pageRequest) {
        EasyUIData data = new EasyUIData();

        Sort.Direction direction = "desc".equalsIgnoreCase(pageRequest.getOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(Math.max(pageRequest.getPage() - 1, 0), pageRequest.getRows(), Sort.by(direction, safeSortField(pageRequest.getSort())));
        Page<SeedType> seedPage = seedTypeDao.findByIsDeletedFalseAndNameContainingIgnoreCase(name, pageable);

        Map<Long, SeedQuality> qualityMap = new HashMap<>();
        List<SeedQuality> qualities = seedQualityDao.findByIsDeletedFalseOrderByIdAsc();
        for (SeedQuality quality : qualities) {
            qualityMap.put(quality.getId(), quality);
        }

        Map<Integer, String> soilNameByBitCode = new LinkedHashMap<>();
        List<SoilType> soilTypes = soilTypeDao.findByIsDeletedFalseOrderByIdAsc();
        for (SoilType soilType : soilTypes) {
            soilNameByBitCode.put(soilType.getBitCode(), soilType.getName());
        }

        List<Map<String, Object>> rowList = new ArrayList<>();
        for (SeedType seedType : seedPage.getContent()) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", seedType.getId());
            row.put("seedId", safeString(seedType.getCode()));
            row.put("seedName", safeString(seedType.getName()));
            row.put("cropType", seedType.getLevel() == null ? "" : seedType.getLevel() + "季作物");
            row.put("seedLevel", seedType.getLevel());
            row.put("seedQualityId", seedType.getSeedQualityId());
            row.put("seedQualityName", getSeedQualityName(qualityMap, seedType.getSeedQualityId()));
            row.put("exp", seedType.getHarvestExperience());
            row.put("growTime", getTotalGrowSeconds(seedType.getId()));
            row.put("harvestCount", seedType.getHarvestFruitNumber());
            row.put("buyPrice", seedType.getPrice());
            row.put("fruitPrice", seedType.getFruitPrice());
            row.put("landRequirement", resolveSoilName(seedType.getEnableSoilTypeBits(), soilNameByBitCode));
            row.put("soilTypeBits", seedType.getEnableSoilTypeBits());
            row.put("points", seedType.getHarvestScore());
            row.put("tips", safeString(seedType.getDescription()));
            rowList.add(row);
        }

        data.setTotal(seedPage.getTotalElements());
        data.setRows(rowList);
        return data;
    }

    @Override
    @Transactional
    public Message addOrUpdate(Map<String, String> params) {
        Message message = new Message();
        try {
            Long id = parseLong(params.get("id"), 0L);
            SeedType entity;
            if (id != null && id > 0) {
                Optional<SeedType> optional = seedTypeDao.findByIdAndIsDeletedFalse(id);
                if (optional.isEmpty()) {
                    message.setCode(1);
                    message.setMsg("未找到要编辑的种子数据");
                    return message;
                }
                entity = optional.get();
            } else {
                entity = new SeedType();
                entity.setStatus((short) 1);
                entity.setIsDeleted(false);
                entity.setOptLockVersion(0);
                entity.setCreatedAt(OffsetDateTime.now());
                entity.setCreatedBy(0L);
                entity.setMaxBugLimit((short) 0);
                entity.setMaxHarvestCount((short) 1);
                entity.setRegrowStageIndex(null);
            }

            entity.setCode(nonEmpty(params.get("seedId"), "0"));
            entity.setName(nonEmpty(params.get("name"), ""));
            Short level = parseShort(params.get("level"), null);
            if (level == null) {
                level = parseShort(params.get("season"), (short) 1);
            }
            entity.setLevel(level);
            entity.setSeedQualityId(parseLong(params.get("seedQualityId"), 1L));
            entity.setHarvestExperience(parseLong(params.get("exp"), 0L));
            entity.setHarvestFruitNumber(parseInteger(params.get("harvestCount"), 0));
            entity.setPrice(parseLong(params.get("buyPrice"), 0L));
            entity.setFruitPrice(parseLong(params.get("fruitPrice"), 0L));
            entity.setHarvestScore(parseLong(params.get("score"), 0L));
            entity.setDescription(nonEmpty(params.get("tips"), ""));
            entity.setBugProbability(parseBigDecimal(params.get("bugProbability"), "0.0000"));
            entity.setEnableSoilTypeBits(resolveSoilBits(params));
            if (entity.getMaxBugLimit() == null) {
                entity.setMaxBugLimit((short) 0);
            }
            if (entity.getMaxHarvestCount() == null) {
                entity.setMaxHarvestCount((short) 1);
            }
            if (entity.getHarvestExperience() == null) {
                entity.setHarvestExperience(0L);
            }
            if (entity.getHarvestFruitNumber() == null) {
                entity.setHarvestFruitNumber(0);
            }
            if (entity.getPrice() == null) {
                entity.setPrice(0L);
            }
            if (entity.getFruitPrice() == null) {
                entity.setFruitPrice(0L);
            }
            if (entity.getHarvestScore() == null) {
                entity.setHarvestScore(0L);
            }
            if (entity.getSeedQualityId() == null) {
                entity.setSeedQualityId(1L);
            }
            entity.setUpdatedAt(OffsetDateTime.now());
            entity.setUpdatedBy(0L);

            SeedType saved = seedTypeDao.save(entity);
            autoCreateStageWhenMissing(saved.getId(), parseInteger(params.get("timeNeeded"), 0));

            message.setCode(0);
            message.setMsg("保存成功");
            message.setData(saved.getId());
            return message;
        } catch (Exception ex) {
            message.setCode(1);
            message.setMsg("保存失败: " + ex.getMessage());
            return message;
        }
    }

    @Override
    @Transactional
    public Message delete(Long id) {
        Message message = new Message();
        Optional<SeedType> optional = seedTypeDao.findByIdAndIsDeletedFalse(id);
        if (optional.isEmpty()) {
            message.setCode(1);
            message.setMsg("删除失败：记录不存在");
            return message;
        }
        SeedType entity = optional.get();
        entity.setIsDeleted(true);
        entity.setUpdatedAt(OffsetDateTime.now());
        entity.setUpdatedBy(0L);
        seedTypeDao.save(entity);
        message.setCode(0);
        message.setMsg("删除成功");
        return message;
    }

    @Override
    public List<Map<String, Object>> qualityOptions() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<SeedQuality> list = seedQualityDao.findByIsDeletedFalseOrderByIdAsc();
        for (SeedQuality item : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("text", item.getName());
            result.add(map);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> soilOptions() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<SoilType> list = soilTypeDao.findByIsDeletedFalseOrderByIdAsc();
        for (SoilType item : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("text", item.getName());
            map.put("bitCode", item.getBitCode());
            result.add(map);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> growthStageOptions() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<GrowthStage> list = growthStageDao.findByIsDeletedFalseOrderByIdAsc();
        for (GrowthStage item : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("text", item.getName());
            result.add(map);
        }
        return result;
    }

    @Override
    public EasyUIData stageGridDataFilterSortPage(Long seedTypeId) {
        EasyUIData data = new EasyUIData();
        List<SeedGrowthStage> list = seedGrowthStageDao.findBySeedTypeIdAndIsDeletedFalseOrderByStageIndexAsc(seedTypeId);
        Map<Long, String> growthNameMap = new HashMap<>();
        List<GrowthStage> growthStages = growthStageDao.findByIsDeletedFalseOrderByIdAsc();
        for (GrowthStage growthStage : growthStages) {
            growthNameMap.put(growthStage.getId(), growthStage.getName());
        }

        BigDecimal bugProbability = BigDecimal.ZERO;
        Optional<SeedType> seedTypeOptional = seedTypeDao.findByIdAndIsDeletedFalse(seedTypeId);
        if (seedTypeOptional.isPresent() && seedTypeOptional.get().getBugProbability() != null) {
            bugProbability = seedTypeOptional.get().getBugProbability();
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (SeedGrowthStage item : list) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", item.getId());
            row.put("seedTypeId", item.getSeedTypeId());
            row.put("seedId", seedTypeOptional.map(SeedType::getCode).orElse(""));
            row.put("growthStage", item.getStageIndex());
            row.put("growthStageId", item.getGrowthStageId());
            row.put("growthStageTitle", safeString(growthNameMap.get(item.getGrowthStageId())));
            row.put("durationSeconds", item.getDurationSeconds());
            row.put("pestProbability", bugProbability);
            row.put("width", item.getWidth());
            row.put("height", item.getHeight());
            row.put("offsetX", item.getOffsetX());
            row.put("offsetY", item.getOffsetY());
            row.put("cropStatus", safeString(growthNameMap.get(item.getGrowthStageId())));
            row.put("assetUrl", safeString(item.getAssetUrl()));
            rows.add(row);
        }

        data.setTotal(rows.size());
        data.setRows(rows);
        return data;
    }

    @Override
    @Transactional
    public Message stageAddOrUpdate(Map<String, String> params) {
        Message message = new Message();
        try {
            Long id = parseLong(params.get("id"), 0L);
            Long seedTypeId = parseLong(params.get("seedTypeId"), 0L);
            if (seedTypeId == null || seedTypeId <= 0) {
                message.setCode(1);
                message.setMsg("缺少 seedTypeId");
                return message;
            }

            SeedGrowthStage entity;
            if (id != null && id > 0) {
                Optional<SeedGrowthStage> optional = seedGrowthStageDao.findByIdAndIsDeletedFalse(id);
                if (optional.isEmpty()) {
                    message.setCode(1);
                    message.setMsg("未找到要编辑的成长阶段");
                    return message;
                }
                entity = optional.get();
            } else {
                entity = new SeedGrowthStage();
                entity.setSeedTypeId(seedTypeId);
                entity.setStatus((short) 1);
                entity.setIsDeleted(false);
                entity.setOptLockVersion(0);
                entity.setCreatedAt(OffsetDateTime.now());
                entity.setCreatedBy(0L);
            }

            entity.setSeedTypeId(seedTypeId);
            entity.setGrowthStageId(parseLong(params.get("growthStageId"), 1L));
            entity.setStageIndex(parseShort(params.get("growthStage"), (short) 0));
            entity.setDurationSeconds(parseInteger(params.get("durationSeconds"), 0));
            entity.setWidth(parseInteger(params.get("width"), 0));
            entity.setHeight(parseInteger(params.get("height"), 0));
            entity.setOffsetX(parseInteger(params.get("offsetX"), 0));
            entity.setOffsetY(parseInteger(params.get("offsetY"), 0));
            entity.setAssetUrl(nonEmpty(params.get("assetUrl"), ""));
            entity.setUpdatedAt(OffsetDateTime.now());
            entity.setUpdatedBy(0L);

            if (entity.getGrowthStageId() == null) {
                entity.setGrowthStageId(1L);
            }
            if (entity.getStageIndex() == null) {
                entity.setStageIndex((short) 0);
            }
            if (entity.getDurationSeconds() == null) {
                entity.setDurationSeconds(0);
            }
            if (entity.getWidth() == null) {
                entity.setWidth(0);
            }
            if (entity.getHeight() == null) {
                entity.setHeight(0);
            }
            if (entity.getOffsetX() == null) {
                entity.setOffsetX(0);
            }
            if (entity.getOffsetY() == null) {
                entity.setOffsetY(0);
            }

            seedGrowthStageDao.save(entity);
            updateSeedBugProbability(seedTypeId, parseBigDecimal(params.get("pestProbability"), "0"));

            message.setCode(0);
            message.setMsg("保存成功");
            return message;
        } catch (Exception ex) {
            message.setCode(1);
            message.setMsg("保存失败: " + ex.getMessage());
            return message;
        }
    }

    @Override
    @Transactional
    public Message stageDelete(Long id) {
        Message message = new Message();
        Optional<SeedGrowthStage> optional = seedGrowthStageDao.findByIdAndIsDeletedFalse(id);
        if (optional.isEmpty()) {
            message.setCode(1);
            message.setMsg("删除失败：记录不存在");
            return message;
        }
        SeedGrowthStage entity = optional.get();
        entity.setIsDeleted(true);
        entity.setUpdatedAt(OffsetDateTime.now());
        entity.setUpdatedBy(0L);
        seedGrowthStageDao.save(entity);
        message.setCode(0);
        message.setMsg("删除成功");
        return message;
    }

    private void autoCreateStageWhenMissing(Long seedTypeId, Integer durationSeconds) {
        List<SeedGrowthStage> stages = seedGrowthStageDao.findBySeedTypeIdAndIsDeletedFalseOrderByStageIndexAsc(seedTypeId);
        if (!stages.isEmpty()) {
            return;
        }
        SeedGrowthStage seedGrowthStage = new SeedGrowthStage();
        seedGrowthStage.setSeedTypeId(seedTypeId);
        seedGrowthStage.setGrowthStageId(1L);
        seedGrowthStage.setStageIndex((short) 0);
        seedGrowthStage.setDurationSeconds(durationSeconds == null ? 0 : durationSeconds);
        seedGrowthStage.setAssetUrl("");
        seedGrowthStage.setWidth(100);
        seedGrowthStage.setHeight(100);
        seedGrowthStage.setOffsetX(0);
        seedGrowthStage.setOffsetY(0);
        seedGrowthStage.setCreatedAt(OffsetDateTime.now());
        seedGrowthStage.setUpdatedAt(OffsetDateTime.now());
        seedGrowthStage.setCreatedBy(0L);
        seedGrowthStage.setUpdatedBy(0L);
        seedGrowthStage.setStatus((short) 1);
        seedGrowthStage.setIsDeleted(false);
        seedGrowthStage.setOptLockVersion(0);
        seedGrowthStageDao.save(seedGrowthStage);
    }

    private void updateSeedBugProbability(Long seedTypeId, BigDecimal pestProbability) {
        Optional<SeedType> optional = seedTypeDao.findByIdAndIsDeletedFalse(seedTypeId);
        if (optional.isPresent()) {
            SeedType seedType = optional.get();
            seedType.setBugProbability(pestProbability == null ? BigDecimal.ZERO : pestProbability);
            seedType.setUpdatedAt(OffsetDateTime.now());
            seedType.setUpdatedBy(0L);
            seedTypeDao.save(seedType);
        }
    }

    private String safeSortField(String sortField) {
        List<String> allowSortFields = List.of("id", "name", "level", "harvestExperience", "price", "harvestScore");
        if (allowSortFields.contains(sortField)) {
            return sortField;
        }
        return "id";
    }

    private String getSeedQualityName(Map<Long, SeedQuality> qualityMap, Long qualityId) {
        SeedQuality quality = qualityMap.get(qualityId);
        return quality == null ? "" : safeString(quality.getName());
    }

    private Long resolveSoilBits(Map<String, String> params) {
        String soilTypeIdStr = params.get("soilTypeId");
        if (soilTypeIdStr != null && !soilTypeIdStr.isBlank()) {
            Long soilTypeId = parseLong(soilTypeIdStr, 0L);
            if (soilTypeId != null && soilTypeId > 0) {
                List<SoilType> soilTypes = soilTypeDao.findByIsDeletedFalseOrderByIdAsc();
                for (SoilType soilType : soilTypes) {
                    if (soilTypeId.equals(soilType.getId())) {
                        return soilType.getBitCode() == null ? 0L : soilType.getBitCode().longValue();
                    }
                }
            }
        }
        return parseLong(params.get("soilTypeBits"), 0L);
    }

    private String resolveSoilName(Long soilBits, Map<Integer, String> soilNameByBitCode) {
        if (soilBits == null || soilBits <= 0) {
            return "";
        }
        for (Map.Entry<Integer, String> entry : soilNameByBitCode.entrySet()) {
            Long bit = entry.getKey().longValue();
            if ((soilBits & bit) == bit) {
                return entry.getValue();
            }
        }
        return "";
    }

    private Integer getTotalGrowSeconds(Long seedTypeId) {
        List<SeedGrowthStage> list = seedGrowthStageDao.findBySeedTypeIdAndIsDeletedFalseOrderByStageIndexAsc(seedTypeId);
        int total = 0;
        for (SeedGrowthStage item : list) {
            if (item.getDurationSeconds() != null) {
                total += item.getDurationSeconds();
            }
        }
        return total;
    }

    private Long parseLong(String value, Long defaultValue) {
        try {
            if (value == null || value.isBlank()) {
                return defaultValue;
            }
            return Long.parseLong(value.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private Integer parseInteger(String value, Integer defaultValue) {
        try {
            if (value == null || value.isBlank()) {
                return defaultValue;
            }
            return Integer.parseInt(value.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private Short parseShort(String value, Short defaultValue) {
        try {
            if (value == null || value.isBlank()) {
                return defaultValue;
            }
            return Short.parseShort(value.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private BigDecimal parseBigDecimal(String value, String defaultValue) {
        try {
            String actualValue = value;
            if (actualValue == null || actualValue.isBlank()) {
                actualValue = defaultValue;
            }
            return new BigDecimal(actualValue.trim());
        } catch (Exception ex) {
            return new BigDecimal(defaultValue);
        }
    }

    private String nonEmpty(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }
}
