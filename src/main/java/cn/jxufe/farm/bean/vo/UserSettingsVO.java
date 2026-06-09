package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSettingsVO implements Serializable {

  private Long userId;
  private Boolean loggedIn;
  private Boolean effectEnabled;
  private Double effectVolume;
  private Boolean bgmEnabled;
  private Double bgmVolume;
  private String preferencesJson;
}
