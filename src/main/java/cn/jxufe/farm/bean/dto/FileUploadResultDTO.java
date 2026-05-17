package cn.jxufe.farm.bean.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class FileUploadResultDTO implements Serializable {

    private String relativePath;

    private String accessUrl;

    private String originalName;

    private Long size;

    private String contentType;

}
