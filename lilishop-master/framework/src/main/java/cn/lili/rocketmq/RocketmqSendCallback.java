package cn.lili.rocketmq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;

/**
 * @author paulG
 * @since 2020/11/4
 **/
@Slf4j
public class RocketmqSendCallback implements SendCallback {

    @Override
    public void onSuccess(SendResult sendResult) {
        log.info("async onSuccess SendResult={}", sendResult);
    }

    @Override
    public void onException(Throwable throwable) {
        String msg = throwable == null ? null : throwable.getMessage();
        if (msg != null && (msg.contains("No route info of this topic") || msg.contains("No route info"))) {
            log.error("async onException: topic route missing or topic not found. Verify NameServer and create topic on broker", throwable);
        } else {
            log.error("async onException Throwable", throwable);
        }
    }
}
