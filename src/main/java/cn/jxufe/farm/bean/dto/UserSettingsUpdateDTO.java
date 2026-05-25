package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UserSettingsUpdateDTO implements Serializable {

    private Boolean effectEnabled;

    @DecimalMin(value = "0.0", message = "音效音量不能小于0")
    @DecimalMax(value = "1.0", message = "音效音量不能大于1")
    private Double effectVolume;

    private Boolean bgmEnabled;

    @DecimalMin(value = "0.0", message = "背景音乐音量不能小于0")
    @DecimalMax(value = "1.0", message = "背景音乐音量不能大于1")
    private Double bgmVolume;
}
