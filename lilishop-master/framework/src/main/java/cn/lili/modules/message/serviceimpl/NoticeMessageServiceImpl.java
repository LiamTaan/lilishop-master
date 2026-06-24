package cn.lili.modules.message.serviceimpl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.common.utils.SnowFlake;
import cn.lili.common.enums.SwitchEnum;
import cn.lili.common.vo.PageVO;
import cn.lili.modules.message.entity.dos.MemberMessage;
import cn.lili.modules.message.entity.dos.NoticeMessage;
import cn.lili.modules.message.entity.dto.MessageTemplateAggregateDTO;
import cn.lili.modules.message.entity.dto.NoticeMessageDTO;
import cn.lili.modules.message.entity.enums.MessageStatusEnum;
import cn.lili.modules.message.entity.enums.NoticeMessageNodeEnum;
import cn.lili.modules.message.entity.enums.NoticeMessageParameterEnum;
import cn.lili.modules.message.mapper.NoticeMessageTemplateMapper;
import cn.lili.modules.message.service.MemberMessageService;
import cn.lili.modules.message.service.NoticeMessageService;
import cn.lili.modules.wechat.entity.dos.WechatMPMessage;
import cn.lili.modules.wechat.entity.dos.WechatMessage;
import cn.lili.modules.wechat.service.WechatMPMessageService;
import cn.lili.modules.wechat.service.WechatMessageService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 通知类消息模板业务层实现
 *
 * @author Bulbasaur
 * @since 2020/12/8 9:48
 */
@Service
public class NoticeMessageServiceImpl extends ServiceImpl<NoticeMessageTemplateMapper, NoticeMessage> implements NoticeMessageService {

    @Autowired
    private MemberMessageService memberMessageService;
    @Autowired
    private WechatMessageService wechatMessageService;
    @Autowired
    private WechatMPMessageService wechatMPMessageService;

    @Override
    public IPage<NoticeMessage> getMessageTemplate(PageVO pageVO, String type) {
        LambdaQueryWrapper<NoticeMessage> wrapper = new LambdaQueryWrapper<>();
        applyTypeFilter(wrapper, type);
        wrapper.orderByDesc(NoticeMessage::getCreateTime);
        return this.page(PageUtil.initPage(pageVO), wrapper);

    }

    @Override
    public void noticeMessage(NoticeMessageDTO noticeMessageDTO) {
        if (noticeMessageDTO == null) {
            return;
        }
        try {
            NoticeMessage noticeMessage = this.getOne(
                    new LambdaQueryWrapper<NoticeMessage>()
                            .eq(NoticeMessage::getNoticeNode
                                    , noticeMessageDTO.getNoticeMessageNodeEnum().getDescription().trim()),false);
            //如果通知类站内信开启的情况下
            if (noticeMessage != null && noticeMessage.getNoticeStatus().equals(SwitchEnum.OPEN.name())) {
                MemberMessage memberMessage = new MemberMessage();
                memberMessage.setMemberId(noticeMessageDTO.getMemberId());
                memberMessage.setTitle(noticeMessage.getNoticeTitle());
                memberMessage.setContent(noticeMessage.getNoticeContent());
                //参数不为空，替换内容
                if (noticeMessageDTO.getParameter() != null) {
                    memberMessage.setContent(replaceNoticeContent(noticeMessage.getNoticeContent(), noticeMessageDTO.getParameter()));
                } else {
                    memberMessage.setContent(noticeMessage.getNoticeContent());
                }
                memberMessage.setStatus(MessageStatusEnum.UN_READY.name());
                //添加站内信
                memberMessageService.save(memberMessage);
            }
        } catch (Exception e) {
            log.error("站内信发送失败：", e);
        }

    }

    /**
     * 替换站内信内容
     *
     * @param noticeContent 站内信内容
     * @param parameter     参数
     * @return 替换后站内信内容
     */
    String replaceNoticeContent(String noticeContent, Map<String, String> parameter) {
        for (Map.Entry<String, String> entry : parameter.entrySet()) {
            String description = NoticeMessageParameterEnum.getValueByType(entry.getKey());
            if (description != null && entry.getValue() != null) {
                noticeContent = noticeContent.replace("#{" + description + "}".trim(), entry.getValue());
            }
        }
        return noticeContent;
    }

    @Override
    public IPage<MessageTemplateAggregateDTO> getMessageTemplateAggregatePage(PageVO pageVO, String type) {
        IPage<NoticeMessage> noticePage = this.getMessageTemplate(pageVO, type);
        List<MessageTemplateAggregateDTO> rows = new ArrayList<>(noticePage.getRecords().size());
        for (NoticeMessage notice : noticePage.getRecords()) {
            MessageTemplateAggregateDTO dto = new MessageTemplateAggregateDTO();
            dto.setNotice(notice);
            String scene = resolveSceneCode(notice);
            if (CharSequenceUtil.isNotBlank(scene)) {
                dto.setWechatOa(wechatMessageService.getOne(
                        new LambdaQueryWrapper<WechatMessage>().eq(WechatMessage::getSceneCode, scene).last("LIMIT 1"),
                        false));
                dto.setWechatMp(wechatMPMessageService.getOne(
                        new LambdaQueryWrapper<WechatMPMessage>().eq(WechatMPMessage::getSceneCode, scene).last("LIMIT 1"),
                        false));
            }
            rows.add(dto);
        }
        Page<MessageTemplateAggregateDTO> out = new Page<>(noticePage.getCurrent(), noticePage.getSize(), noticePage.getTotal());
        out.setRecords(rows);
        return out;
    }

    private String resolveSceneCode(NoticeMessage notice) {
        if (CharSequenceUtil.isNotBlank(notice.getSceneCode())) {
            return notice.getSceneCode();
        }
        NoticeMessageNodeEnum en = NoticeMessageNodeEnum.fromNoticeNodeLabel(notice.getNoticeNode());
        return en != null ? en.name() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initDefaultTemplates() {
        List<NoticeMessage> existing = this.list();
        Set<String> existingSceneCodes = new HashSet<>();
        Set<String> existingNoticeNodes = new HashSet<>();
        List<NoticeMessage> toUpdate = new ArrayList<>();
        for (NoticeMessage noticeMessage : existing) {
            if (CharSequenceUtil.isNotBlank(noticeMessage.getSceneCode())) {
                existingSceneCodes.add(noticeMessage.getSceneCode());
            }
            if (CharSequenceUtil.isNotBlank(noticeMessage.getNoticeNode())) {
                String noticeNode = noticeMessage.getNoticeNode().replace("\r", "").replace("\n", "").trim();
                existingNoticeNodes.add(noticeNode);
                if (CharSequenceUtil.isBlank(noticeMessage.getSceneCode())) {
                    NoticeMessageNodeEnum nodeEnum = NoticeMessageNodeEnum.fromNoticeNodeLabel(noticeNode);
                    if (nodeEnum != null) {
                        noticeMessage.setSceneCode(nodeEnum.name());
                    }
                }
            }
            if (CharSequenceUtil.isBlank(noticeMessage.getEmailStatus())) {
                noticeMessage.setEmailStatus(SwitchEnum.CLOSE.name());
            }
            if (CharSequenceUtil.isNotBlank(noticeMessage.getSceneCode()) || CharSequenceUtil.isNotBlank(noticeMessage.getEmailStatus())) {
                toUpdate.add(noticeMessage);
            }
        }
        if (!toUpdate.isEmpty()) {
            this.updateBatchById(toUpdate);
        }

        List<NoticeMessage> toInsert = new ArrayList<>();
        for (NoticeMessage template : defaultTemplates()) {
            if (existingSceneCodes.contains(template.getSceneCode()) || existingNoticeNodes.contains(template.getNoticeNode())) {
                continue;
            }
            toInsert.add(template);
        }
        if (!toInsert.isEmpty()) {
            this.saveBatch(toInsert);
        }
    }

    private void applyTypeFilter(LambdaQueryWrapper<NoticeMessage> wrapper, String type) {
        if (CharSequenceUtil.isBlank(type)) {
            return;
        }
        String normalizedType = type.trim().toUpperCase();
        switch (normalizedType) {
            case "ORDER":
            case "GOODS":
                wrapper.and(w -> w.likeRight(NoticeMessage::getSceneCode, "ORDER_"));
                break;
            case "AFTER_SALE":
                wrapper.and(w -> w.likeRight(NoticeMessage::getSceneCode, "AFTER_SALE_")
                        .or().likeRight(NoticeMessage::getSceneCode, "RETURN_"));
                break;
            case "PINTUAN":
                wrapper.and(w -> w.likeRight(NoticeMessage::getSceneCode, "PINTUAN_"));
                break;
            case "POINT":
            case "POINTS":
                wrapper.and(w -> w.likeRight(NoticeMessage::getSceneCode, "POINT_"));
                break;
            case "WALLET":
                wrapper.and(w -> w.likeRight(NoticeMessage::getSceneCode, "WALLET_"));
                break;
            default:
                wrapper.and(w -> w.eq(NoticeMessage::getSceneCode, normalizedType)
                        .or().like(NoticeMessage::getNoticeNode, type.trim()));
                break;
        }
    }

    private List<NoticeMessage> defaultTemplates() {
        return Arrays.asList(
                template(NoticeMessageNodeEnum.ORDER_CREATE_SUCCESS, "订单提交成功通知", "订单 #{商品名称}已经提交成功，请及时付款哦～", "goods"),
                template(NoticeMessageNodeEnum.ORDER_CANCEL_SUCCESS, "订单取消通知", "订单#{商品名称}已取消，取消原因为 #{取消原因}，请及时关注您的订单状态哦~", "goods,cancel_reason"),
                template(NoticeMessageNodeEnum.ORDER_PAY_SUCCESS, "订单支付成功通知", "订单#{商品名称}支付成功，我们将尽快为您安排发货哦~", "goods"),
                template(NoticeMessageNodeEnum.ORDER_PAY_ERROR, "支付失败自动退款通知", "订单#{商品名称}支付失败，系统已为您发起退款，请及时关注退款状态哦~", "goods"),
                template(NoticeMessageNodeEnum.ORDER_DELIVER, "订单发货通知", "订单#{商品名称}已发货，请注意物流进度哦~", "goods"),
                template(NoticeMessageNodeEnum.ORDER_COMPLETE, "订单完成通知", "订单#{商品名称}已完成，期待您分享商品评价与购物心得哦~", "goods"),
                template(NoticeMessageNodeEnum.ORDER_EVALUATION, "订单评价提醒", "商品#{商品名称}还没有收到您的评价呢，期待您与我们分享哦~", "goods"),
                template(NoticeMessageNodeEnum.AFTER_SALE_CREATE_SUCCESS, "售后提交成功通知", "售后单 #{商品名称} 已经提交成功，需要商家审核，请耐心等待哦~", "goods"),
                template(NoticeMessageNodeEnum.RETURN_GOODS_PASS, "退货审核通过通知", "售后单 #{商品名称} 已通过退货审核，请尽快安排将货物退还至商家哦~", "goods"),
                template(NoticeMessageNodeEnum.RETURN_MONEY_PASS, "退款审核通过通知", "售后单 #{商品名称} 已通过退款审核，将在1-3个工作日内自动退款~", "goods"),
                template(NoticeMessageNodeEnum.RETURN_GOODS_REFUSE, "退货审核未通过通知", "很抱歉，售后单#{商品名称}未通过商家退货审核，原因是：#{拒绝原因}", "goods,refuse"),
                template(NoticeMessageNodeEnum.RETURN_MONEY_REFUSE, "退款审核未通过通知", "很抱歉，售后单#{商品名称}未通过商家退款审核，原因是：#{拒绝原因}", "goods,refuse"),
                template(NoticeMessageNodeEnum.AFTER_SALE_ROG_PASS, "退货物品签收通知", "订单 #{商品名称} 商家已签收哦，请关注后续退款状态，将在1-3个工作日内自动退款~", "goods"),
                template(NoticeMessageNodeEnum.AFTER_SALE_ROG_REFUSE, "退货物品拒收通知", "很抱歉，您的退货物品#{商品名称}被商家拒收，原因是：#{拒绝原因}", "goods,refuse"),
                template(NoticeMessageNodeEnum.AFTER_SALE_COMPLETE, "售后完成通知", "售后单 #{商品名称} 已经完成，请及时关注售后单状态哦~", "goods"),
                template(NoticeMessageNodeEnum.PINTUAN_CREATE, "开团成功通知", "您已成功发起#{商品名称}拼团，快邀请好友一起参团吧~", "goods"),
                template(NoticeMessageNodeEnum.PINTUAN_ERROR, "拼团失败通知", "很遗憾，您的#{商品名称}拼团失败，款项将按原路退回，请及时关注退款状态哦~", "goods"),
                template(NoticeMessageNodeEnum.PINTUAN_SUCCESS, "拼团成功通知", "恭喜，您的拼团#{商品名称}已成团，我们将尽快为您安排发货哦~", "goods"),
                template(NoticeMessageNodeEnum.POINT_CHANGE, "积分变更通知", "您当前到账#{获得积分}积分，消费#{消费积分}积分，积分可以抵现金哦~", "income_points,expenditure_points"),
                template(NoticeMessageNodeEnum.WALLET_CHANGE, "余额账户变更通知", "您的余额账户今日收入¥#{收入金额}，支出¥#{支出金额}，如遇异常变动，请及时联系我们哦~", "income,expenditure"),
                template(NoticeMessageNodeEnum.WALLET_WITHDRAWAL_CREATE, "提现申请提交成功通知", "恭喜您，您的提现金额为¥#{金额}的申请已经提交审核，请及时关注审核动态哦~", "price"),
                template(NoticeMessageNodeEnum.WALLET_WITHDRAWAL_SUCCESS, "余额提现成功通知", "恭喜您，您的提现处理成功，提现金额为¥#{收入金额}，请及时关注余额变动哦~", "income"),
                template(NoticeMessageNodeEnum.WALLET_WITHDRAWAL_ERROR, "余额提现申请失败通知", "很抱歉，您的提现申请处理失败，请稍后重试或联系管理员哦~", "price"),
                template(NoticeMessageNodeEnum.WALLET_WITHDRAWAL_AUDIT_ERROR, "提现申请驳回通知", "很抱歉，您的提现金额为¥#{金额}的申请已被拒绝，如有问题请及时联系管理员哦~", "price"),
                template(NoticeMessageNodeEnum.WALLET_WITHDRAWAL_AUDIT_SUCCESS, "余额提现申请通过通知", "恭喜您，您的提现金额为¥#{金额}的申请已审核通过，请及时关注到账结果哦~", "price")
        );
    }

    private NoticeMessage template(NoticeMessageNodeEnum nodeEnum, String title, String content, String variable) {
        NoticeMessage noticeMessage = new NoticeMessage();
        noticeMessage.setId(SnowFlake.getIdStr());
        noticeMessage.setNoticeNode(nodeEnum.getDescription());
        noticeMessage.setNoticeTitle(title);
        noticeMessage.setNoticeContent(content);
        noticeMessage.setNoticeStatus(SwitchEnum.OPEN.name());
        noticeMessage.setVariable(variable);
        noticeMessage.setSceneCode(nodeEnum.name());
        noticeMessage.setEmailStatus(SwitchEnum.CLOSE.name());
        return noticeMessage;
    }

}
