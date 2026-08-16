package com.smartteaching.common.result;

import lombok.Data;
import java.util.List;

/**
 * 分页返回对象
 */
@Data
public class PageResult<T> {
    private Long total;       //总记录数
    private Long pages;       //总页数
    private Long current;     //当前页
    private Long size;        //每页条数
    private List<T> records;  //数据列表

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
