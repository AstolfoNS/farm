package cn.jxufe.farm.bean.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
public class PlotTradeRecordVO implements Serializable {

    private String bizId;
    private String bizType;
    private String bizTypeLabel;
    private Long plotId;
    private Short plotIndex;
    private Long coinChangeAmount;
    private String coinOperationType;
    private Long beforeCoin;
    private Long afterCoin;
    private OffsetDateTime occurredAt;
}
