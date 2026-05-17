package cn.jxufe.farm.bean.vo;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
public class AvatarUploadVO implements Serializable {

    private String relativePath;
    private String accessUrl;
    private String path;
}
