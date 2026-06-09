package cn.jxufe.farm.bean.dto;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileUploadResultDTO implements Serializable {

  private String relativePath;

  private String accessUrl;

  private String originalName;

  private Long size;

  private String contentType;
}
