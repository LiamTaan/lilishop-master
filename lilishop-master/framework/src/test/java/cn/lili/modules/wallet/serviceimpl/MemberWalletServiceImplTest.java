package cn.lili.modules.wallet.serviceimpl;

import cn.lili.modules.member.entity.dos.Member;
import cn.lili.modules.wallet.entity.dos.MemberWallet;
import cn.lili.modules.wallet.mapper.MemberWalletMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class MemberWalletServiceImplTest {

    @Spy
    @InjectMocks
    private MemberWalletServiceImpl memberWalletService;

    @Mock
    private MemberWalletMapper memberWalletMapper;

    @Test
    void setMemberWalletPasswordShouldCreateWalletWhenMissing() {
        ReflectionTestUtils.setField(memberWalletService, "baseMapper", memberWalletMapper);
        Member member = new Member();
        member.setId("member-1");
        member.setUsername("qa-user");

        MemberWallet createdWallet = new MemberWallet();
        createdWallet.setId("wallet-1");
        createdWallet.setMemberId("member-1");
        createdWallet.setMemberName("qa-user");

        doReturn(null).when(memberWalletService).getOne(any());
        doReturn(createdWallet).when(memberWalletService).save("member-1", "qa-user");
        doAnswer(invocation -> {
            MemberWallet updated = invocation.getArgument(0);
            Assertions.assertEquals("wallet-1", updated.getId());
            Assertions.assertTrue(new BCryptPasswordEncoder().matches("e10adc3949ba59abbe56e057f20f883e", updated.getWalletPassword()));
            return true;
        }).when(memberWalletService).updateById(any(MemberWallet.class));

        memberWalletService.setMemberWalletPassword(member, "e10adc3949ba59abbe56e057f20f883e");
    }
}
