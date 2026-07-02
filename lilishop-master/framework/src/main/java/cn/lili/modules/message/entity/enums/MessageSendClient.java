package cn.lili.modules.message.entity.enums;

/**
 * 消息发送客户端
 *
 * @author pikachu
 * @since 2020/12/8 9:46
 */
public enum MessageSendClient {

    MEMBER("会员"),
    STORE("店铺"),
    ALL("全部"),
    CONSUMER("消费者"),
    AGENT("代理商"),
    SUPPLIER("供货商");

    private final String description;

    MessageSendClient(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }


}
