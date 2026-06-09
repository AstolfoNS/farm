package cn.jxufe.farm.config;

import cn.jxufe.farm.common.utils.FileAccessPathUtils;
import cn.jxufe.farm.config.properties.LocalFileStorageProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final LocalFileStorageProperties fileStorageProperties;

    public StaticResourceConfig(LocalFileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String publicPrefix = normalizePublicPrefix(fileStorageProperties.getPublicPrefix());
        Path rootPath = Paths.get(fileStorageProperties.getStorageRoot()).toAbsolutePath().normalize();
        String location = rootPath.toUri().toString();
        registry.addResourceHandler(publicPrefix + "/**")
                .addResourceLocations(location);
    }

    private String normalizePublicPrefix(String prefix) {
        return FileAccessPathUtils.normalizePublicPrefix(prefix);
    }
}
