package cn.jxufe.farm.bean.vo;

import java.io.Serializable;

public class FileUrlVO implements Serializable {

    private String relativePath;
    private String accessUrl;
    private Boolean exists;

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }

    public Boolean getExists() {
        return exists;
    }

    public void setExists(Boolean exists) {
        this.exists = exists;
    }
}
