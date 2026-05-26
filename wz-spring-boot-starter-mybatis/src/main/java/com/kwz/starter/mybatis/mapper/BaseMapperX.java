package com.kwz.starter.mybatis.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kwz.starter.mybatis.page.PageQuery;

/**
 * 扩展 MyBatis-Plus {@link BaseMapper}
 */
public interface BaseMapperX<T> extends BaseMapper<T> {

    default Page<T> selectPage(PageQuery query, Wrapper<T> wrapper) {
        return selectPage(query.toPage(), wrapper);
    }
}
