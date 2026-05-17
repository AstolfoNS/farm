package cn.jxufe.farm.common.pages;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一分页数据封装")
public class PageResult<T> {

    private long pageNo;

    private long pageSize;

    private long total;

    private List<T> records;
}
