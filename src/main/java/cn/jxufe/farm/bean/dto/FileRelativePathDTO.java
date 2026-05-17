package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class FileRelativePathDTO implements Serializable {

    @NotBlank(message = "relativePath不能为空")
    private String relativePath;
}
