package cn.lili.modules.goods.entity.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GoodsSearchParamsTest {

    @Test
    void shouldMatchCategoryPathByWholePathSegment() {
        GoodsSearchParams params = GoodsSearchParams.builder()
                .categoryPath("100")
                .storeCategoryPath("200,300")
                .build();

        QueryWrapper<Object> wrapper = params.queryWrapper();
        String sqlSegment = wrapper.getSqlSegment();

        Assertions.assertTrue(sqlSegment.contains("CONCAT(',', category_path, ',') LIKE"),
                "categoryPath should match by path segment");
        Assertions.assertTrue(sqlSegment.contains("CONCAT(',', store_category_path, ',') LIKE"),
                "storeCategoryPath should match by path segment");
        Assertions.assertTrue(wrapper.getParamNameValuePairs().containsValue("%,100,%"));
        Assertions.assertTrue(wrapper.getParamNameValuePairs().containsValue("%,200,300,%"));
    }
}
