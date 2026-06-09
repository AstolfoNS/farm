package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvatarUploadVO implements Serializable {

  private String relativePath;
  private String accessUrl;
  private String path;
}
