package cn.jxufe.farm.bean.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserPlotAllocationApplyResultVO implements Serializable {

    private Long userId;
    private Integer beforeTotalPlots;
    private Integer afterTotalPlots;
    private Integer createdPlots;
    private Integer updatedPlots;
    private String lockSource;
}

