package com.kwz.starter.mybatis.page;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;
import java.util.function.Function;

/**
 * 统一分页响应
 */
@Data
public class PageResult<T> {

    private List<T> records;

    private long total;

    private long pageNo;

    private long pageSize;

    private long pages;

    public static <T> PageResult<T> of(Page<T> page) {
        return of(page, Function.identity());
    }

    public static <E, V> PageResult<V> of(Page<E> page, Function<E, V> converter) {
        PageResult<V> result = new PageResult<>();
        result.setRecords(page.getRecords().stream().map(converter).toList());
        result.setTotal(page.getTotal());
        result.setPageNo(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setPages(page.getPages());
        return result;
    }
}
