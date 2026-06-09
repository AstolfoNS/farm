package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserAddOrUpdateDTO implements Serializable {

  private String id;

  @NotBlank(message = "用户名不能为空")
  private String username;

  private String nickname;

  private String experience;

  private String exp;

  private String score;

  private String coin;

  private String avatarPath;
}
