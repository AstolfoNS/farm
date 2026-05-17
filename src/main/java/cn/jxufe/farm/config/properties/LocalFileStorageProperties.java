package cn.jxufe.farm.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "farm.file")
public class LocalFileStorageProperties {

    private String storageRoot = "./static/oss";

    private String publicPrefix = "/oss";

}
