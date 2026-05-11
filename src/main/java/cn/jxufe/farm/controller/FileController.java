package cn.jxufe.farm.controller;

import cn.jxufe.farm.model.bean.FileUploadResult;
import cn.jxufe.farm.model.bean.Message;
import cn.jxufe.farm.service.FileService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Message upload(@RequestParam("file") MultipartFile file,
                          @RequestParam(value = "category", required = false, defaultValue = "other") String category) {
        Message message = new Message();
        try {
            FileUploadResult result = fileService.upload(file, category);
            message.setCode(0);
            message.setMsg("上传成功");
            message.setData(result);
            return message;
        } catch (Exception ex) {
            message.setCode(1);
            message.setMsg("上传失败: " + ex.getMessage());
            return message;
        }
    }

    @PostMapping(value = "/saveHeadImg", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Message saveHeadImg(@RequestParam("filePathName") MultipartFile file) {
        Message message = new Message();
        try {
            FileUploadResult result = fileService.upload(file, "avatar");
            Map<String, Object> payload = new HashMap<>();
            payload.put("relativePath", result.getRelativePath());
            payload.put("accessUrl", result.getAccessUrl());
            payload.put("path", result.getRelativePath());
            message.setCode(0);
            message.setMsg("上传成功");
            message.setData(payload);
            return message;
        } catch (Exception ex) {
            message.setCode(1);
            message.setMsg("上传失败: " + ex.getMessage());
            return message;
        }
    }

    @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Message delete(@RequestParam("relativePath") String relativePath) {
        Message message = new Message();
        try {
            boolean deleted = fileService.deleteByRelativePath(relativePath);
            message.setCode(deleted ? 0 : 1);
            message.setMsg(deleted ? "删除成功" : "文件不存在");
            return message;
        } catch (Exception ex) {
            message.setCode(1);
            message.setMsg("删除失败: " + ex.getMessage());
            return message;
        }
    }

    @GetMapping(value = "/url", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Message url(@RequestParam("relativePath") String relativePath) {
        Message message = new Message();
        try {
            String accessUrl = fileService.buildAccessUrl(relativePath);
            Map<String, Object> payload = new HashMap<>();
            payload.put("relativePath", relativePath);
            payload.put("accessUrl", accessUrl);
            payload.put("exists", fileService.exists(relativePath));
            message.setCode(0);
            message.setMsg("获取成功");
            message.setData(payload);
            return message;
        } catch (Exception ex) {
            message.setCode(1);
            message.setMsg("获取失败: " + ex.getMessage());
            return message;
        }
    }
}
