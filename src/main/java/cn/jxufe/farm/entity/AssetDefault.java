package cn.jxufe.farm.entity;

import cn.jxufe.farm.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "asset_defaults", schema = "farm")
public class AssetDefault extends BaseEntity {

    @Column(name = "asset_key", nullable = false, length = 128)
    private String assetKey;

    @Column(name = "asset_url", nullable = false, length = 1024)
    private String assetUrl;

    @Column(name = "description")
    private String description;
}
