package cn.jxufe.farm.bean.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;

@Setter
@Getter
public class IdDTO implements Serializable {

    @NotNull(message = "id不能为空")
    @Positive(message = "id必须大于0")
    private Long id;

}
