package cn.jxufe.farm.bean.vo;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
public class FileUrlVO implements Serializable {

    private String relativePath;
    private String accessUrl;
    private Boolean exists;
}
