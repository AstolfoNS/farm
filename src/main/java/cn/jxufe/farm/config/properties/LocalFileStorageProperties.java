package cn.jxufe.farm.config.properties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "farm.file")
public class LocalFileStorageProperties {

    private String storageRoot = "./static/oss";

    private String publicPrefix = "/oss";

}
