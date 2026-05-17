package cn.jxufe.farm.controller;

import cn.jxufe.farm.bean.dto.FileUploadResultDTO;
import cn.jxufe.farm.bean.dto.FileRelativePathDTO;
import cn.jxufe.farm.bean.vo.AvatarUploadVO;
import cn.jxufe.farm.bean.vo.FileUrlVO;
import cn.jxufe.farm.common.apis.R;
import cn.jxufe.farm.common.exception.ServiceException;
import cn.jxufe.farm.service.FileService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/file", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<FileUploadResultDTO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", required = false, defaultValue = "other") String category) {
        return R.ok(fileService.upload(file, category), "上传成功");
    }

    @PostMapping(value = "/saveHeadImg", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<AvatarUploadVO> saveHeadImg(@RequestParam("file") MultipartFile file) {
        FileUploadResultDTO result = fileService.upload(file, "avatar");
        AvatarUploadVO payload = new AvatarUploadVO();
        payload.setRelativePath(result.getRelativePath());
        payload.setAccessUrl(result.getAccessUrl());
        payload.setPath(result.getRelativePath());
        return R.ok(payload, "上传成功");
    }

    @PostMapping(value = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<Boolean> delete(@Valid @RequestBody FileRelativePathDTO params) {
        boolean deleted = fileService.deleteByRelativePath(params.getRelativePath());
        if (!deleted) {
            throw new ServiceException("文件不存在或已删除");
        }
        return R.ok(true, "删除成功");
    }

    @PostMapping(value = "/url", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<FileUrlVO> url(@Valid @RequestBody FileRelativePathDTO params) {
        FileUrlVO payload = new FileUrlVO();
        payload.setRelativePath(params.getRelativePath());
        payload.setAccessUrl(fileService.buildAccessUrl(params.getRelativePath()));
        payload.setExists(fileService.exists(params.getRelativePath()));
        return R.ok(payload, "获取成功");
    }
}
