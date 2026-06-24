package cn.lili.modules.order.order.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

class ReceiptMapperTest {

    @Test
    void getReceiptShouldUseLeftJoinToKeepDraftReceiptsVisible() throws NoSuchMethodException {
        Method method = ReceiptMapper.class.getMethod("getReceipt", com.baomidou.mybatisplus.core.metadata.IPage.class, com.baomidou.mybatisplus.core.conditions.Wrapper.class);
        org.apache.ibatis.annotations.Select select = method.getAnnotation(org.apache.ibatis.annotations.Select.class);

        Assertions.assertNotNull(select);
        Assertions.assertTrue(select.value()[0].contains("left join li_order"),
                "receipt list query should keep receipts without matched order rows");
    }
}
