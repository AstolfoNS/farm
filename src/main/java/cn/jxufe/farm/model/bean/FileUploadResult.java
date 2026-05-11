package cn.jxufe.farm.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileUploadResult {
    private String relativePath;
    private String accessUrl;
    private String originalName;
    private long size;
    private String contentType;
}
