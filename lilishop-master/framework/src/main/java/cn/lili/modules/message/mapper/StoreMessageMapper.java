package cn.lili.modules.message.mapper;

import cn.lili.modules.message.entity.dos.StoreMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 店铺接收到消息发送数据处理层
 *
 * @author Chopper
 * @since 2021/1/30 4:17 下午
 */
public interface StoreMessageMapper extends BaseMapper<StoreMessage> {

    /**
     * 店铺消息分页
     *
     * @param page         分页
     * @param storeId      店铺ID
     * @param status       消息状态
     * @return 店铺消息分页
     */
    @Select({
            "<script>",
            "select me.title, me.content, sp.create_time, sp.store_name, sp.store_id, sp.id, sp.status, sp.message_id ",
            "from li_store_message sp ",
            "left join li_message me on me.id = sp.message_id ",
            "where sp.store_id = #{storeId} ",
            "<if test='status != null and status != \"\"'>",
            "  and sp.status = #{status} ",
            "</if>",
            "order by sp.create_time desc",
            "</script>"
    })
    IPage<StoreMessage> queryByParams(IPage<StoreMessage> page, @Param("storeId") String storeId, @Param("status") String status);
}
