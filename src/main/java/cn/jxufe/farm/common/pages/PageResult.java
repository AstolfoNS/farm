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

    public static <T> PageResult<T> of(List<T> totalRecords, long pageNo, long pageSize) {
        int fromIndex = Math.toIntExact(Math.min((pageNo - 1) * pageSize, totalRecords.size()));
        int toIndex = Math.toIntExact(Math.min(fromIndex + pageSize, totalRecords.size()));

        return new PageResult<>(pageNo, pageSize, totalRecords.size(), totalRecords.subList(fromIndex, toIndex));
    }

}
