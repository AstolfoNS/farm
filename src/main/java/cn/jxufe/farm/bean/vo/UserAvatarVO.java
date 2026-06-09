package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAvatarVO implements Serializable {

  private Long id;
  private String avatarPath;
  private String head;
}
