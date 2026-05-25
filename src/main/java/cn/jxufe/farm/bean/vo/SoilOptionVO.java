package cn.jxufe.farm.bean.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SoilOptionVO implements Serializable {

    private Long id;
    private String text;
    private Integer bitCode;
    private Short level;
    private Long unlockExperienceRequired;
}
