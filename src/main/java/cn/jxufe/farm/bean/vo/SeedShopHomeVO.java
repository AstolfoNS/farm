package cn.jxufe.farm.bean.vo;

import cn.jxufe.farm.common.pages.PageResult;
import lombok.Data;

import java.io.Serializable;

@Data
public class SeedShopHomeVO implements Serializable {

    private SeedShopOverviewVO overview;
    private PageResult<SeedShopItemVO> shopPage;
}
