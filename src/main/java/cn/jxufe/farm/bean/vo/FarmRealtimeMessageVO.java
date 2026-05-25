package cn.jxufe.farm.bean.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Setter
@Getter
public class FarmRealtimeMessageVO {

    private String event;

    private Long userId;

    private OffsetDateTime serverTime;

    private Boolean cropStatusChanged;

    private MyFarmOverviewVO overview;

}
