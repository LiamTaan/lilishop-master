package cn.lili.modules.member.serviceimpl;

import cn.lili.common.vo.PageVO;
import cn.lili.modules.member.entity.dos.Member;
import cn.lili.modules.member.entity.dos.MemberReceipt;
import cn.lili.modules.member.entity.vo.MemberReceiptAddVO;
import cn.lili.modules.member.entity.vo.MemberReceiptVO;
import cn.lili.modules.member.mapper.MemberReceiptMapper;
import cn.lili.modules.member.service.MemberService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberReceiptServiceImplTest {

    @Spy
    @InjectMocks
    private MemberReceiptServiceImpl memberReceiptService;

    @Mock
    private MemberReceiptMapper memberReceiptMapper;

    @Mock
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(memberReceiptService, "baseMapper", memberReceiptMapper);
    }

    @Test
    void getPageShouldQueryNonDeletedReceipts() {
        MemberReceipt receipt = new MemberReceipt();
        receipt.setId("receipt-1");
        receipt.setMemberId("member-1");
        receipt.setDeleteFlag(false);

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Page<MemberReceipt> page = invocation.getArgument(0);
            page.setRecords(List.of(receipt));
            page.setTotal(1);
            return page;
        }).when(memberReceiptService).page(any(Page.class), any(Wrapper.class));

        MemberReceiptVO query = new MemberReceiptVO();
        query.setMemberId("member-1");
        PageVO pageVO = new PageVO();
        pageVO.setPageNumber(1);
        pageVO.setPageSize(10);

        IPage<MemberReceipt> result = memberReceiptService.getPage(query, pageVO);

        Assertions.assertEquals(1L, result.getTotal());
        Assertions.assertEquals(1, result.getRecords().size());
        Assertions.assertEquals("receipt-1", result.getRecords().getFirst().getId());
        Assertions.assertFalse(result.getRecords().getFirst().getDeleteFlag());
    }

    @Test
    void addMemberReceiptShouldResetOtherDefaultsAndSaveNewDefault() {
        MemberReceiptAddVO addVO = new MemberReceiptAddVO();
        addVO.setReceiptTitle("QA Receipt");
        addVO.setTaxpayerId("91310000123456789X");
        addVO.setReceiptContent("goods");
        addVO.setReceiptType("PERSONAL");
        addVO.setIsDefault(1);

        Member member = new Member();
        member.setId("member-1");
        member.setUsername("buyer_test_002");

        MemberReceipt oldReceipt = new MemberReceipt();
        oldReceipt.setId("old-1");
        oldReceipt.setMemberId("member-1");
        oldReceipt.setIsDefault(1);
        oldReceipt.setDeleteFlag(false);

        List<MemberReceipt> inserted = new ArrayList<>();

        when(memberReceiptMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(List.of())
                .thenReturn(List.of(oldReceipt));
        when(memberService.getById("member-1")).thenReturn(member);
        doReturn(true).when(memberReceiptService).update(argThat(wrapper -> wrapper.getSqlSet() != null && wrapper.getSqlSet().contains("is_default")));
        when(memberReceiptMapper.insert(any(MemberReceipt.class))).thenAnswer(invocation -> {
            MemberReceipt receipt = invocation.getArgument(0);
            inserted.add(receipt);
            return 1;
        });

        Boolean result = memberReceiptService.addMemberReceipt(addVO, "member-1");

        Assertions.assertTrue(result);
        Assertions.assertEquals(1, inserted.size());
        MemberReceipt saved = inserted.getFirst();
        Assertions.assertEquals("member-1", saved.getMemberId());
        Assertions.assertEquals("buyer_test_002", saved.getMemberName());
        Assertions.assertEquals(1, saved.getIsDefault());
    }

    @Test
    void deleteMemberReceiptShouldMarkDeleteFlagTrue() {
        MemberReceipt receipt = new MemberReceipt();
        receipt.setId("receipt-1");
        receipt.setDeleteFlag(false);

        when(memberReceiptMapper.selectById("receipt-1")).thenReturn(receipt);
        when(memberReceiptMapper.updateById(any(MemberReceipt.class))).thenAnswer(invocation -> {
            MemberReceipt updated = invocation.getArgument(0);
            Assertions.assertTrue(updated.getDeleteFlag());
            return 1;
        });

        Boolean result = memberReceiptService.deleteMemberReceipt("receipt-1");

        Assertions.assertTrue(result);
    }
}
