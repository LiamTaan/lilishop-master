package cn.lili.modules.message.serviceimpl;


import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.vo.PageVO;
import cn.lili.modules.message.entity.dos.StoreMessage;
import cn.lili.modules.message.entity.enums.MessageStatusEnum;
import cn.lili.modules.message.entity.vos.StoreMessageQueryVO;
import cn.lili.modules.message.mapper.StoreMessageMapper;
import cn.lili.modules.message.service.StoreMessageService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 消息发送业务层实现
 *
 * @author Chopper
 * @since 2020/11/17 3:48 下午
 */
@Service
public class StoreMessageServiceImpl extends ServiceImpl<StoreMessageMapper, StoreMessage> implements StoreMessageService {

    @Override
    public boolean deleteByMessageId(String messageId) {
        StoreMessage storeMessage = this.getById(messageId);
        if (storeMessage != null) {
            if (!storeMessage.getStoreId().equals(UserContext.getCurrentUser().getStoreId())) {
                throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
            }
            if (!MessageStatusEnum.ALREADY_REMOVE.name().equals(storeMessage.getStatus())) {
                throw new ServiceException("仅回收站消息支持彻底删除");
            }
            return this.removeById(messageId);
        }
        return false;

    }

    @Override
    public IPage<StoreMessage> getPage(StoreMessageQueryVO storeMessageQueryVO, PageVO pageVO) {
        return this.baseMapper.queryByParams(PageUtil.initPage(pageVO), storeMessageQueryVO.getStoreId(), storeMessageQueryVO.getStatus());

    }

    @Override
    public boolean save(List<StoreMessage> messages) {
        return saveBatch(messages);
    }

    @Override
    public boolean editStatus(String status, String id) {
        StoreMessage storeMessage = this.getById(id);
        if (storeMessage != null) {
            //校验权限
            if (!storeMessage.getStoreId().equals(UserContext.getCurrentUser().getStoreId())) {
                throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR.message());
            }
            String currentStatus = storeMessage.getStatus();
            if (MessageStatusEnum.ALREADY_READY.name().equals(status)
                    && !MessageStatusEnum.UN_READY.name().equals(currentStatus)
                    && !MessageStatusEnum.ALREADY_REMOVE.name().equals(currentStatus)) {
                throw new ServiceException("当前消息状态不支持此操作");
            }
            if (MessageStatusEnum.ALREADY_REMOVE.name().equals(status)
                    && MessageStatusEnum.ALREADY_REMOVE.name().equals(currentStatus)) {
                throw new ServiceException("消息已在回收站中");
            }
            storeMessage.setStatus(status);
            return this.updateById(storeMessage);
        }
        return false;
    }
}
