package com.smartteaching.common.result;

import lombok.Data;
import java.util.List;

/**
 * 分页返回对象
 */
@Data
public class PageResult<T> {
    private Long total;
    private Long pages;
    private Long current;
    private Long size;
    private List<T> records;

    public List<T> getList(){
        return records;
    }

    public static <T> PageResult<T> build(Long total, Long pages, Long current, Long size, List<T> records) {
        PageResult<T> page = new PageResult<>();
        page.setTotal(total);
        page.setPages(pages);
        page.setCurrent(current);
        page.setSize(size);
        page.setRecords(records);
        return page;
    }
}