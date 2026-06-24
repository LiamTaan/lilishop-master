package cn.lili.modules.member.serviceimpl;

import cn.lili.common.exception.ServiceException;
import cn.lili.modules.member.entity.dos.MemberNoticeSenter;
import cn.lili.modules.member.mapper.MemberNoticeSenterMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MemberNoticeSenterServiceImplTest {

    @Spy
    @InjectMocks
    private MemberNoticeSenterServiceImpl memberNoticeSenterService;

    @Mock
    private MemberNoticeSenterMapper memberNoticeSenterMapper;

    @Test
    void customSaveShouldThrowWhenSendTypeIsMissing() {
        ReflectionTestUtils.setField(memberNoticeSenterService, "baseMapper", memberNoticeSenterMapper);
        MemberNoticeSenter notice = new MemberNoticeSenter();
        notice.setTitle("qa");
        notice.setContent("hello");
        notice.setMemberIds("1");

        Assertions.assertThrows(ServiceException.class, () -> memberNoticeSenterService.customSave(notice));
    }

    @Test
    void customSaveShouldThrowWhenSendTypeIsInvalid() {
        ReflectionTestUtils.setField(memberNoticeSenterService, "baseMapper", memberNoticeSenterMapper);
        MemberNoticeSenter notice = new MemberNoticeSenter();
        notice.setTitle("qa");
        notice.setContent("hello");
        notice.setMemberIds("1");
        notice.setSendType("UNKNOWN");

        Assertions.assertThrows(ServiceException.class, () -> memberNoticeSenterService.customSave(notice));
    }
}
