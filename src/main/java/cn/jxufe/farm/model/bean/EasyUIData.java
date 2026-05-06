package cn.jxufe.farm.model.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Schema(description = "反馈给EasyUI Grid的对象结构")
public class EasyUIData {
    @Schema(description = "总记录数")
    private long total = 0;
    @Schema(description = "具体数据对象列表")
    private List<?> rows = new ArrayList<>();
}
