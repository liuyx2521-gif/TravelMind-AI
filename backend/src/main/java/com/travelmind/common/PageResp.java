package com.travelmind.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

public record PageResp<T>(long total, long page, long size, List<T> records) {
    public static <T> PageResp<T> of(IPage<T> page) {
        return new PageResp<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }
}
