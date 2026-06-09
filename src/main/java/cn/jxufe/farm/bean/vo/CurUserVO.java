package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurUserVO implements Serializable {

  private Long id;
  private String username;
  private String nickname;
  private Long experience;
  private Long score;
  private Long coin;
  private String avatarPath;
  private String head;
  private Boolean loggedIn;
  private Map<String, String> defaultAssets;
}
