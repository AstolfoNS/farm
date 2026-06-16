package cn.jxufe.farm.controller;

import cn.jxufe.farm.bean.dto.FileRelativePathDTO;
import cn.jxufe.farm.bean.dto.FileUploadResultDTO;
import cn.jxufe.farm.bean.vo.AvatarUploadVO;
import cn.jxufe.farm.bean.vo.FileUrlVO;
import cn.jxufe.farm.common.apis.R;
import cn.jxufe.farm.common.exceptions.ServiceException;
import cn.jxufe.farm.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "文件模块", description = "本地文件上传、删除与访问 URL 转换")
@RestController
@RequestMapping(value = "/file", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class FileController {

  private final FileService fileService;

  public FileController(FileService fileService) {
    this.fileService = fileService;
  }

  @Operation(summary = "上传文件", description = "按分类白名单上传文件到本地存储。category 默认为 other。返回相对路径和访问 URL")
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public R<FileUploadResultDTO> upload(
      @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
      @Parameter(description = "分类目录")
          @RequestParam(value = "category", required = false, defaultValue = "other")
          String category) {
    return R.ok(fileService.upload(file, category), "上传成功");
  }

  @Operation(summary = "上传头像", description = "上传头像文件，固定分类为 avatar。返回相对路径和访问 URL")
  @PostMapping(value = "/saveHeadImg", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public R<AvatarUploadVO> saveHeadImg(
      @Parameter(description = "头像文件") @RequestParam("file") MultipartFile file) {
    FileUploadResultDTO result = fileService.upload(file, "avatar");
    AvatarUploadVO payload = new AvatarUploadVO();
    payload.setRelativePath(result.getRelativePath());
    payload.setAccessUrl(result.getAccessUrl());
    payload.setPath(result.getRelativePath());
    return R.ok(payload, "上传成功");
  }

  @Operation(summary = "删除文件", description = "按相对路径删除文件。文件不存在时抛异常")
  @PostMapping(value = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<Boolean> delete(@Valid @RequestBody FileRelativePathDTO params) {
    boolean deleted = fileService.deleteByRelativePath(params.getRelativePath());
    if (!deleted) {
      throw new ServiceException("文件不存在或已删除");
    }
    return R.ok(true, "删除成功");
  }

  @Operation(summary = "获取文件 URL", description = "按相对路径转换为完整访问 URL，同时返回文件是否存在")
  @PostMapping(value = "/url", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<FileUrlVO> url(@Valid @RequestBody FileRelativePathDTO params) {
    FileUrlVO payload = new FileUrlVO();
    payload.setRelativePath(params.getRelativePath());
    payload.setAccessUrl(fileService.buildAccessUrl(params.getRelativePath()));
    payload.setExists(fileService.exists(params.getRelativePath()));
    return R.ok(payload, "获取成功");
  }
}
