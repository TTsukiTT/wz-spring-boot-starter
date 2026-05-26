package com.kwz.starter.mybatis.page;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * 统一分页查询参数
 */
@Data
public class PageQuery {

    private long pageNo = 1;

    private long pageSize = 10;

    public <T> Page<T> toPage() {
        return new Page<>(Math.max(pageNo, 1), Math.max(pageSize, 1));
    }
}
