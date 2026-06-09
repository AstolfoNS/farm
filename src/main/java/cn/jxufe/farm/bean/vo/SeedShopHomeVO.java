package cn.jxufe.farm.bean.vo;

import cn.jxufe.farm.common.pages.PageResult;
import java.io.Serializable;
import lombok.Data;

@Data
public class SeedShopHomeVO implements Serializable {

  private SeedShopOverviewVO overview;
  private PageResult<SeedShopItemVO> shopPage;
}
