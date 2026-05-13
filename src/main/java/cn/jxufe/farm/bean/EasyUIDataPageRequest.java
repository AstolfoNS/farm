package cn.jxufe.farm.model.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "EasyUI Grid提交的分页请求结构")
public class EasyUIDataPageRequest {
    @Schema(description = "第几页")
    private int page = 1;
    @Schema(description = "页容量")
    private int rows = 1;
    @Schema(description = "排序字段")
    private String sort = "id";
    @Schema(description = "升序（asc）或降序（desc）")
    private String order = "asc";
}
