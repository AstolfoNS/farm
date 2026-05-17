package cn.jxufe.farm.bean.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;

@Setter
@Getter
public class UserAvatarUpdateDTO implements Serializable {

    @NotNull(message = "用户ID不能为空")
    @Positive(message = "用户ID必须大于0")
    private Long id;

    private String avatarPath;
}
