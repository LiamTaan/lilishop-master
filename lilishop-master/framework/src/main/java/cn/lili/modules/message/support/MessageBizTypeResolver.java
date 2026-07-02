package cn.lili.modules.message.support;

import cn.lili.modules.message.entity.enums.MessageBizTypeEnum;
import cn.lili.modules.message.entity.enums.NoticeMessageNodeEnum;

/**
 * 消息业务分类解析
 */
public final class MessageBizTypeResolver {

    private MessageBizTypeResolver() {
    }

    public static String fromNoticeNode(NoticeMessageNodeEnum nodeEnum) {
        if (nodeEnum == null) {
            return MessageBizTypeEnum.PLATFORM.name();
        }
        switch (nodeEnum) {
            case ORDER_CREATE_SUCCESS:
            case ORDER_CANCEL_SUCCESS:
            case ORDER_PAY_SUCCESS:
            case ORDER_PAY_ERROR:
            case ORDER_DELIVER:
            case ORDER_COMPLETE:
            case ORDER_EVALUATION:
                return MessageBizTypeEnum.TRADE.name();
            case AFTER_SALE_CREATE_SUCCESS:
            case RETURN_GOODS_PASS:
            case RETURN_MONEY_PASS:
            case RETURN_GOODS_REFUSE:
            case RETURN_MONEY_REFUSE:
            case AFTER_SALE_ROG_PASS:
            case AFTER_SALE_ROG_REFUSE:
            case AFTER_SALE_COMPLETE:
                return MessageBizTypeEnum.AFTER_SALE.name();
            case PINTUAN_CREATE:
            case PINTUAN_ERROR:
            case PINTUAN_SUCCESS:
                return MessageBizTypeEnum.ACTIVITY.name();
            case POINT_CHANGE:
            case WALLET_CHANGE:
            case WALLET_WITHDRAWAL_CREATE:
            case WALLET_WITHDRAWAL_SUCCESS:
            case WALLET_WITHDRAWAL_ERROR:
            case WALLET_WITHDRAWAL_AUDIT_ERROR:
            case WALLET_WITHDRAWAL_AUDIT_SUCCESS:
                return MessageBizTypeEnum.ASSET.name();
            default:
                return MessageBizTypeEnum.PLATFORM.name();
        }
    }
}
