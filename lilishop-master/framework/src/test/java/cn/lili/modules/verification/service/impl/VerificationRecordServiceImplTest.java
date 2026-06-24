package cn.lili.modules.verification.service.impl;

import cn.lili.common.vo.PageVO;
import cn.lili.modules.verification.entity.dos.VerificationRecord;
import cn.lili.modules.verification.entity.params.VerificationRecordSearchParams;
import cn.lili.modules.verification.entity.vos.VerificationRecordVO;
import cn.lili.modules.verification.mapper.VerificationRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationRecordServiceImplTest {

    @Spy
    @InjectMocks
    private VerificationRecordServiceImpl verificationRecordService;

    @Mock
    private VerificationRecordMapper verificationRecordMapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(verificationRecordService, "baseMapper", verificationRecordMapper);
    }

    @Test
    void exceptionPageShouldUseManualPaginationAndConvertRecords() {
        VerificationRecord record = new VerificationRecord();
        record.setId("vr-1");
        record.setOrderSn("order-1");
        record.setStoreId("store-1");
        record.setStoreName("store-name");
        record.setMemberId("member-1");
        record.setMemberName("member-name");
        record.setVerificationCode("code-1");
        record.setOperatorId("op-1");
        record.setOperatorName("operator");
        record.setSourceType("STORE");
        record.setResultType("FAIL");
        record.setRemark("remark");

        when(verificationRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        when(verificationRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(record));

        PageVO pageVO = new PageVO();
        pageVO.setPageNumber(1);
        pageVO.setPageSize(10);

        IPage<VerificationRecordVO> result = verificationRecordService.exceptionPage(new VerificationRecordSearchParams(), pageVO);

        Assertions.assertEquals(1L, result.getTotal());
        Assertions.assertEquals(1, result.getRecords().size());
        VerificationRecordVO vo = result.getRecords().get(0);
        Assertions.assertEquals("vr-1", vo.getId());
        Assertions.assertEquals("order-1", vo.getOrderSn());
        Assertions.assertEquals("store-name", vo.getStoreName());
        Assertions.assertEquals("member-name", vo.getMemberName());
        Assertions.assertEquals("code-1", vo.getVerificationCode());
        Assertions.assertEquals("operator", vo.getOperatorName());
        Assertions.assertEquals("FAIL", vo.getResultType());
        Assertions.assertEquals("remark", vo.getRemark());
    }

    @Test
    void exceptionListShouldConvertWithoutBeanUtilDependency() {
        VerificationRecord record = new VerificationRecord();
        record.setId("vr-2");
        record.setOrderSn("order-2");
        record.setResultType("FAIL");
        record.setRemark("remark-2");

        doReturn(List.of(record)).when(verificationRecordService).list(any(LambdaQueryWrapper.class));

        List<VerificationRecordVO> result = verificationRecordService.exceptionList(new VerificationRecordSearchParams());

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("vr-2", result.getFirst().getId());
        Assertions.assertEquals("order-2", result.getFirst().getOrderSn());
        Assertions.assertEquals("FAIL", result.getFirst().getResultType());
        Assertions.assertEquals("remark-2", result.getFirst().getRemark());
    }
}
