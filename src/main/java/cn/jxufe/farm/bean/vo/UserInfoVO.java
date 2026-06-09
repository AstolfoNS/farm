package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoVO implements Serializable {

  private Long id;
  private String username;
  private String nickname;
  private Long experience;
  private Long score;
  private Long coin;
  private String avatarPath;
  private String head;
}
