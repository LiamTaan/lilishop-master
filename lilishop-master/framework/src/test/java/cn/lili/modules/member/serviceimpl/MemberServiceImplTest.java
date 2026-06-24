package cn.lili.modules.member.serviceimpl;

import cn.lili.common.exception.ServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @InjectMocks
    private MemberServiceImpl memberService;

    @Test
    void updateMemberStatusShouldRejectEmptyMemberIds() {
        Assertions.assertThrows(ServiceException.class,
                () -> memberService.updateMemberStatus(Collections.emptyList(), Boolean.TRUE));
    }

    @Test
    void updateMemberStatusShouldRejectNullStatus() {
        Assertions.assertThrows(ServiceException.class,
                () -> memberService.updateMemberStatus(Collections.singletonList("1"), null));
    }
}
