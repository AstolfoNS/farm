package cn.jxufe.farm.service;

import cn.jxufe.farm.model.bean.EasyUIData;
import cn.jxufe.farm.model.bean.EasyUIDataPageRequest;
import cn.jxufe.farm.model.bean.Message;

import java.util.List;
import java.util.Map;

public interface SeedService {
    EasyUIData gridDataFilterSortPage(String name, EasyUIDataPageRequest pageRequest);

    Message addOrUpdate(Map<String, String> params);

    Message delete(Long id);

    List<Map<String, Object>> qualityOptions();

    List<Map<String, Object>> soilOptions();

    List<Map<String, Object>> growthStageOptions();

    EasyUIData stageGridDataFilterSortPage(Long seedTypeId);

    Message stageAddOrUpdate(Map<String, String> params);

    Message stageDelete(Long id);
}
