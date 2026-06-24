package cn.lili.modules.agent.serviceimpl;

import cn.lili.common.exception.ServiceException;
import cn.lili.modules.agent.entity.dto.ProfitSharingRuleDTO;
import cn.lili.modules.agent.mapper.ProfitSharingRuleMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ProfitSharingRuleServiceImplTest {

    @Spy
    @InjectMocks
    private ProfitSharingRuleServiceImpl profitSharingRuleService;

    @Mock
    private ProfitSharingRuleMapper profitSharingRuleMapper;

    @Test
    void updateShouldThrowWhenRuleDoesNotExist() {
        ReflectionTestUtils.setField(profitSharingRuleService, "baseMapper", profitSharingRuleMapper);
        doReturn(null).when(profitSharingRuleService).getById("missing");

        Assertions.assertThrows(ServiceException.class,
                () -> profitSharingRuleService.update("missing", new ProfitSharingRuleDTO()));
    }

    @Test
    void deleteShouldThrowWhenRuleDoesNotExist() {
        ReflectionTestUtils.setField(profitSharingRuleService, "baseMapper", profitSharingRuleMapper);
        doReturn(null).when(profitSharingRuleService).getById("missing");

        Assertions.assertThrows(ServiceException.class,
                () -> profitSharingRuleService.delete("missing"));
    }
}
