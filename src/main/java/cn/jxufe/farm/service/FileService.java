package cn.jxufe.farm.service;

import cn.jxufe.farm.bean.dto.FileUploadResultDTO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

  FileUploadResultDTO upload(MultipartFile file, String category);

  boolean deleteByRelativePath(String relativePath);

  String buildAccessUrl(String relativePath);

  boolean exists(String relativePath);
}
