package cn.jxufe.farm.bean.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
public class SeedShopTradeRecordVO implements Serializable {

    private String bizId;
    private String tradeType;
    private Long seedTypeId;
    private String seedName;
    private Long itemQuantity;
    private String itemType;
    private String itemOperationType;
    private Long coinChangeAmount;
    private String coinOperationType;
    private OffsetDateTime occurredAt;
}
