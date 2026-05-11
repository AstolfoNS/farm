package cn.jxufe.farm.imp;

import cn.jxufe.farm.config.LocalFileStorageProperties;
import cn.jxufe.farm.model.bean.FileUploadResult;
import cn.jxufe.farm.service.FileService;
import cn.jxufe.farm.util.FilePathUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileImp implements FileService {

    private final LocalFileStorageProperties fileStorageProperties;

    public FileImp(LocalFileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    @Override
    public FileUploadResult upload(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        String normalizedCategory = FilePathUtils.normalizeCategory(category);
        String extension = FilePathUtils.extractExtension(file.getOriginalFilename());
        String datePath = FilePathUtils.buildDatePath();
        String storedName = FilePathUtils.buildStoredFileName(extension);
        String relativePath = normalizedCategory + "/" + datePath + "/" + storedName;

        Path rootPath = getStorageRootPath();
        Path targetPath = resolveSafePath(rootPath, relativePath);
        try {
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath);
        } catch (IOException ex) {
            throw new RuntimeException("保存文件失败: " + ex.getMessage(), ex);
        }

        FileUploadResult result = new FileUploadResult();
        result.setRelativePath(relativePath);
        result.setAccessUrl(buildAccessUrl(relativePath));
        result.setOriginalName(file.getOriginalFilename());
        result.setSize(file.getSize());
        result.setContentType(file.getContentType());
        return result;
    }

    @Override
    public boolean deleteByRelativePath(String relativePath) {
        Path rootPath = getStorageRootPath();
        Path targetPath = resolveSafePath(rootPath, normalizeIncomingPath(relativePath));
        if (!Files.exists(targetPath) || !Files.isRegularFile(targetPath)) {
            return false;
        }
        try {
            Files.delete(targetPath);
            return true;
        } catch (IOException ex) {
            throw new RuntimeException("删除文件失败: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String buildAccessUrl(String relativePath) {
        String normalized = normalizeIncomingPath(relativePath);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        String publicPrefix = normalizePublicPrefix(fileStorageProperties.getPublicPrefix());
        return publicPrefix + "/" + normalized;
    }

    @Override
    public boolean exists(String relativePath) {
        Path rootPath = getStorageRootPath();
        Path targetPath = resolveSafePath(rootPath, normalizeIncomingPath(relativePath));
        return Files.exists(targetPath) && Files.isRegularFile(targetPath);
    }

    private Path getStorageRootPath() {
        Path rootPath = Paths.get(fileStorageProperties.getStorageRoot()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootPath);
        } catch (IOException ex) {
            throw new RuntimeException("初始化文件目录失败: " + ex.getMessage(), ex);
        }
        return rootPath;
    }

    private Path resolveSafePath(Path rootPath, String relativePath) {
        String normalizedRelative = FilePathUtils.sanitizeToRelativePath(relativePath);
        if (normalizedRelative.isBlank()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        Path targetPath = rootPath.resolve(normalizedRelative).normalize();
        if (!targetPath.startsWith(rootPath)) {
            throw new IllegalArgumentException("非法文件路径");
        }
        return targetPath;
    }

    private String normalizeIncomingPath(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String raw = input.trim();
        try {
            URI uri = URI.create(raw);
            if (uri.getScheme() != null && uri.getPath() != null) {
                raw = uri.getPath();
            }
        } catch (Exception ignored) {
        }

        int queryIndex = raw.indexOf('?');
        if (queryIndex >= 0) {
            raw = raw.substring(0, queryIndex);
        }
        int fragmentIndex = raw.indexOf('#');
        if (fragmentIndex >= 0) {
            raw = raw.substring(0, fragmentIndex);
        }

        String relative = FilePathUtils.sanitizeToRelativePath(raw);
        String publicPrefix = normalizePublicPrefix(fileStorageProperties.getPublicPrefix());
        String normalizedPrefix = FilePathUtils.sanitizeToRelativePath(publicPrefix);
        if (relative.startsWith(normalizedPrefix + "/")) {
            return relative.substring(normalizedPrefix.length() + 1);
        }
        return relative;
    }

    private String normalizePublicPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "/oss";
        }
        String trimmed = prefix.trim();
        if (!trimmed.startsWith("/")) {
            trimmed = "/" + trimmed;
        }
        if (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
