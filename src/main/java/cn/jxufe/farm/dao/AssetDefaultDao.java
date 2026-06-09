package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.AssetDefault;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetDefaultDao extends JpaRepository<AssetDefault, Long> {

  Optional<AssetDefault> findByAssetKeyAndIsDeletedFalse(String assetKey);

  List<AssetDefault> findByIsDeletedFalseOrderByIdAsc();
}
