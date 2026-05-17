package cn.jxufe.farm.bean.vo;

import java.io.Serializable;

public class AvatarUploadVO implements Serializable {

    private String relativePath;
    private String accessUrl;
    private String path;

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
