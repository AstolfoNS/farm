package cn.jxufe.farm.bean.vo;

import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FarmRealtimeMessageVO {

  private String event;

  private Long userId;

  private OffsetDateTime serverTime;

  private Boolean cropStatusChanged;

  private MyFarmOverviewVO overview;
}
