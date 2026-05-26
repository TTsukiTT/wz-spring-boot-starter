package com.kwz.starter.mybatis.page;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResultTest {

    @Test
    void shouldConvertPageRecords() {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<String> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10, 2);
        page.setRecords(List.of("a", "b"));

        PageResult<Integer> result = PageResult.of(page, String::length);

        assertThat(result.getRecords()).containsExactly(1, 1);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getPageNo()).isEqualTo(1);
        assertThat(result.getPageSize()).isEqualTo(10);
    }
}
