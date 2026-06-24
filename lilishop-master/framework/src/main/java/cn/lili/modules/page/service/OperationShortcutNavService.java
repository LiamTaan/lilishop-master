package cn.lili.modules.page.service;

import cn.lili.common.vo.PageVO;
import cn.lili.modules.page.entity.dos.OperationShortcutNav;
import cn.lili.modules.page.entity.dto.OperationShortcutNavDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 首页分类配置业务层
 *
 * @author dawn
 * @since 2026/6/17
 */
public interface OperationShortcutNavService extends IService<OperationShortcutNav> {

    /**
     * 分页查询首页分类配置
     *
     * @param pageVO 查询分页
     * @param query  查询条件
     * @return 分页结果
     */
    IPage<OperationShortcutNav> pageData(PageVO pageVO, OperationShortcutNavDTO query);

    /**
     * 买家端获取可展示首页分类
     *
     * @param clientType 客户端类型
     * @return 首页分类列表
     */
    List<OperationShortcutNav> listBuyerNav(String clientType);
}
