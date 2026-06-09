package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileUrlVO implements Serializable {

  private String relativePath;
  private String accessUrl;
  private Boolean exists;
}
