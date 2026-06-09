package cn.jxufe.farm.service.support;

import cn.jxufe.farm.dao.AssetDefaultDao;
import cn.jxufe.farm.entity.AssetDefault;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AssetDefaultProvider {

  private final AssetDefaultDao assetDefaultDao;

  public AssetDefaultProvider(AssetDefaultDao assetDefaultDao) {
    this.assetDefaultDao = assetDefaultDao;
  }

  public String get(String key) {
    String safeKey = safeString(key).trim();
    if (safeKey.isEmpty()) {
      return "";
    }
    return assetDefaultDao
        .findByAssetKeyAndIsDeletedFalse(safeKey)
        .map(AssetDefault::getAssetUrl)
        .map(this::safeString)
        .map(String::trim)
        .orElse("");
  }

  public Map<String, String> getAll() {
    List<AssetDefault> rows = assetDefaultDao.findByIsDeletedFalseOrderByIdAsc();
    Map<String, String> map = new LinkedHashMap<>();
    for (AssetDefault row : rows) {
      if (row == null) {
        continue;
      }
      String key = safeString(row.getAssetKey()).trim();
      if (key.isEmpty()) {
        continue;
      }
      map.putIfAbsent(key, safeString(row.getAssetUrl()).trim());
    }
    return map;
  }

  private String safeString(String value) {
    return value == null ? "" : value;
  }
}
