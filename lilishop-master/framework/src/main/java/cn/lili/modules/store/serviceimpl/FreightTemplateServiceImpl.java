package cn.lili.modules.store.serviceimpl;

import cn.lili.cache.Cache;
import cn.lili.cache.CachePrefix;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.utils.BeanUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.modules.store.entity.dos.FreightTemplate;
import cn.lili.modules.store.entity.dos.FreightTemplateChild;
import cn.lili.modules.store.entity.vos.FreightTemplateVO;
import cn.lili.modules.store.mapper.FreightTemplateMapper;
import cn.lili.modules.store.service.FreightTemplateChildService;
import cn.lili.modules.store.service.FreightTemplateService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.hutool.core.text.CharSequenceUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 店铺运费模板业务层实现
 *
 * @author Bulbasaur
 * @since 2020/11/22 16:00
 */
@Service
public class FreightTemplateServiceImpl extends ServiceImpl<FreightTemplateMapper, FreightTemplate> implements FreightTemplateService {
    /**
     * 配送子模板
     */
    @Autowired
    private FreightTemplateChildService freightTemplateChildService;
    /**
     * 缓存
     */
    @Autowired
    private Cache cache;


    @Override
    public List<FreightTemplateVO> getFreightTemplateList(String storeId) {
        //先从缓存中获取运费模板，如果有则直接返回，如果没有则查询数据后再返回
        List<FreightTemplateVO> list = (List<FreightTemplateVO>) cache.get(CachePrefix.SHIP_TEMPLATE.getPrefix() + storeId);
        if (list != null) {
            return list;
        }
        list = new ArrayList<>();
        //查询运费模板
        LambdaQueryWrapper<FreightTemplate> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(FreightTemplate::getStoreId, storeId);
        List<FreightTemplate> freightTemplates = this.baseMapper.selectList(lambdaQueryWrapper);
        if (!freightTemplates.isEmpty()) {
            //如果模板不为空则查询子模板信息
            for (FreightTemplate freightTemplate : freightTemplates) {
                list.add(this.buildFreightTemplateVO(freightTemplate));
            }
        }
        cache.put(CachePrefix.SHIP_TEMPLATE.getPrefix() + storeId, list);
        return list;

    }

    @Override
    public IPage<FreightTemplate> getFreightTemplate(PageVO pageVo) {
        LambdaQueryWrapper<FreightTemplate> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(FreightTemplate::getStoreId, UserContext.getCurrentUser().getStoreId());
        return this.baseMapper.selectPage(PageUtil.initPage(pageVo), lambdaQueryWrapper);
    }

    @Override
    public FreightTemplateVO getFreightTemplate(String id) {
        //获取运费模板
        FreightTemplate freightTemplate = this.getById(id);
        if (freightTemplate == null) {
            return new FreightTemplateVO();
        }
        return this.buildFreightTemplateVO(freightTemplate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FreightTemplateVO addFreightTemplate(FreightTemplateVO freightTemplateVO) {
        //获取当前登录商家账号
        AuthUser tokenUser = UserContext.getCurrentUser();
        return this.addFreightTemplate(tokenUser.getStoreId(), freightTemplateVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FreightTemplateVO addFreightTemplate(String storeId, FreightTemplateVO freightTemplateVO) {
        if (CharSequenceUtil.isBlank(storeId)) {
            throw new ServiceException(ResultCode.PARAMS_ERROR, "店铺ID不能为空");
        }
        FreightTemplate freightTemplate = new FreightTemplate();
        freightTemplateVO.setStoreId(storeId);
        BeanUtils.copyProperties(freightTemplateVO, freightTemplate);
        this.save(freightTemplate);
        this.replaceTemplateChildren(freightTemplate.getId(), freightTemplateVO.getFreightTemplateChildList());
        this.clearStoreTemplateCache(storeId);
        return this.getFreightTemplate(freightTemplate.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FreightTemplateVO editFreightTemplate(FreightTemplateVO freightTemplateVO) {
        //获取当前登录商家账号
        AuthUser tokenUser = UserContext.getCurrentUser();
        return this.editFreightTemplate(tokenUser.getStoreId(), freightTemplateVO.getId(), freightTemplateVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FreightTemplateVO editFreightTemplate(String storeId, String id, FreightTemplateVO freightTemplateVO) {
        FreightTemplate source = this.requireStoreTemplate(storeId, id);
        FreightTemplate freightTemplate = new FreightTemplate();
        freightTemplateVO.setId(source.getId());
        freightTemplateVO.setStoreId(source.getStoreId());
        BeanUtils.copyProperties(freightTemplateVO, freightTemplate);
        this.updateById(freightTemplate);
        this.replaceTemplateChildren(id, freightTemplateVO.getFreightTemplateChildList());
        this.clearStoreTemplateCache(source.getStoreId());
        return this.getFreightTemplate(id);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeFreightTemplate(String id) {
        //获取当前登录商家账号
        AuthUser tokenUser = UserContext.getCurrentUser();
        return this.removeFreightTemplate(tokenUser.getStoreId(), id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeFreightTemplate(String storeId, String id) {
        FreightTemplate source = this.requireStoreTemplate(storeId, id);
        LambdaQueryWrapper<FreightTemplate> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(FreightTemplate::getStoreId, source.getStoreId());
        lambdaQueryWrapper.eq(FreightTemplate::getId, id);
        //如果删除成功则删除运费模板子项
        if (this.remove(lambdaQueryWrapper)) {
            this.clearStoreTemplateCache(source.getStoreId());
            return freightTemplateChildService.removeFreightTemplate(id);
        }
        return false;
    }

    private FreightTemplateVO buildFreightTemplateVO(FreightTemplate freightTemplate) {
        FreightTemplateVO freightTemplateVO = new FreightTemplateVO();
        BeanUtil.copyProperties(freightTemplate, freightTemplateVO);
        List<FreightTemplateChild> freightTemplateChildren = freightTemplateChildService.getFreightTemplateChild(freightTemplate.getId());
        if (!freightTemplateChildren.isEmpty()) {
            freightTemplateVO.setFreightTemplateChildList(freightTemplateChildren);
        }
        return freightTemplateVO;
    }

    private FreightTemplate requireStoreTemplate(String storeId, String id) {
        if (CharSequenceUtil.isBlank(storeId) || CharSequenceUtil.isBlank(id)) {
            throw new ServiceException(ResultCode.PARAMS_ERROR, "运费模板参数不完整");
        }
        FreightTemplate freightTemplate = this.getById(id);
        if (freightTemplate == null) {
            throw new ServiceException(ResultCode.FREIGHT_TEMPLATE_NOT_EXIST);
        }
        if (!storeId.equals(freightTemplate.getStoreId())) {
            throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
        return freightTemplate;
    }

    private void replaceTemplateChildren(String templateId, List<FreightTemplateChild> children) {
        freightTemplateChildService.removeFreightTemplate(templateId);
        if (children == null || children.isEmpty()) {
            return;
        }
        List<FreightTemplateChild> list = new ArrayList<>();
        for (FreightTemplateChild freightTemplateChild : children) {
            if (freightTemplateChild == null) {
                continue;
            }
            freightTemplateChild.setFreightTemplateId(templateId);
            list.add(freightTemplateChild);
        }
        if (!list.isEmpty()) {
            freightTemplateChildService.addFreightTemplateChild(list);
        }
    }

    private void clearStoreTemplateCache(String storeId) {
        cache.remove(CachePrefix.SHIP_TEMPLATE.getPrefix() + storeId);
    }
}
