package cn.jxufe.farm.oss.imp;

import cn.jxufe.farm.bean.dto.FileUploadResultDTO;
import cn.jxufe.farm.common.utils.FileAccessPathUtils;
import cn.jxufe.farm.common.utils.FilePathUtils;
import cn.jxufe.farm.config.properties.LocalFileStorageProperties;
import cn.jxufe.farm.oss.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileImp implements FileService {

    private final LocalFileStorageProperties fileStorageProperties;

    public FileImp(LocalFileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    @Override
    public FileUploadResultDTO upload(MultipartFile file, String category) {
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
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException("保存文件失败: " + ex.getMessage(), ex);
        }

        FileUploadResultDTO result = new FileUploadResultDTO();
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
        return FileAccessPathUtils.normalizePublicPrefix(fileStorageProperties.getPublicPrefix()) + "/" + normalized;
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
        return FileAccessPathUtils.normalizeIncomingRelativePath(input, fileStorageProperties.getPublicPrefix());
    }
}
