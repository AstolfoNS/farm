package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.AssetDefault;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetDefaultDao extends JpaRepository<AssetDefault, Long> {

    Optional<AssetDefault> findByAssetKeyAndIsDeletedFalse(String assetKey);

    List<AssetDefault> findByIsDeletedFalseOrderByIdAsc();
}
