package cn.jxufe.farm.oss;

import cn.jxufe.farm.model.bean.FileUploadResult;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileUploadResult upload(MultipartFile file, String category);

    boolean deleteByRelativePath(String relativePath);

    String buildAccessUrl(String relativePath);

    boolean exists(String relativePath);
}
