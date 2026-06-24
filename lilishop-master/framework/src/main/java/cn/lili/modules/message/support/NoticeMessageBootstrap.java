package cn.lili.modules.message.support;

import cn.lili.modules.message.service.NoticeMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 消息模板启动自愈，保证空库或漏初始化环境具备可治理的基础模板。
 */
@Slf4j
@Component
public class NoticeMessageBootstrap implements ApplicationRunner {

    private final NoticeMessageService noticeMessageService;

    public NoticeMessageBootstrap(NoticeMessageService noticeMessageService) {
        this.noticeMessageService = noticeMessageService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            noticeMessageService.initDefaultTemplates();
        } catch (Exception e) {
            log.error("初始化默认消息模板失败", e);
        }
    }
}
