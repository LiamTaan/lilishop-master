package cn.lili.modules.member.serviceimpl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.security.enums.UserEnums;
import cn.lili.common.vo.PageVO;
import cn.lili.modules.member.entity.dos.Member;
import cn.lili.modules.member.entity.dos.MemberAddress;
import cn.lili.modules.member.entity.dto.MemberAddressQueryDTO;
import cn.lili.modules.member.entity.vo.MemberAddressManagerVO;
import cn.lili.modules.member.mapper.MemberAddressMapper;
import cn.lili.modules.member.service.MemberAddressService;
import cn.lili.modules.member.service.MemberService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 收货地址业务层实现
 *
 * @author Chopper
 * @since 2020/11/18 9:44 上午
 */
@Service
public class MemberAddressServiceImpl extends ServiceImpl<MemberAddressMapper, MemberAddress> implements MemberAddressService {

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^0?(13[0-9]|14[0-9]|15[0-9]|16[0-9]|17[0-9]|18[0-9]|19[0-9])[0-9]{8}$");

    @Autowired
    private MemberService memberService;

    @Override
    public IPage<MemberAddress> getAddressByMember(PageVO page, String memberId) {
        return this.page(PageUtil.initPage(page),
                new QueryWrapper<MemberAddress>()
                        .eq("member_id", memberId));

    }

    @Override
    public IPage<MemberAddressManagerVO> queryAddressPage(PageVO page, MemberAddressQueryDTO queryDTO) {
        LambdaQueryWrapper<MemberAddress> queryWrapper = Wrappers.lambdaQuery();
        String keyword = queryDTO == null ? null : CharSequenceUtil.trim(queryDTO.getKeyword());
        if (CharSequenceUtil.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like(MemberAddress::getName, keyword)
                    .or()
                    .like(MemberAddress::getMobile, keyword)
                    .or()
                    .like(MemberAddress::getAlias, keyword)
                    .or()
                    .like(MemberAddress::getDetail, keyword));
        }
        if (queryDTO != null) {
            queryWrapper.eq(CharSequenceUtil.isNotBlank(queryDTO.getMemberId()),
                    MemberAddress::getMemberId, CharSequenceUtil.trim(queryDTO.getMemberId()));
            queryWrapper.eq(queryDTO.getIsDefault() != null, MemberAddress::getIsDefault, queryDTO.getIsDefault());

            List<String> memberIds = resolveMemberIdsByKeyword(queryDTO.getMemberKeyword());
            if (memberIds != null) {
                if (memberIds.isEmpty()) {
                    return emptyAddressPage(page);
                }
                queryWrapper.in(MemberAddress::getMemberId, memberIds);
            }
        }
        if (CharSequenceUtil.isBlank(page.getSort())) {
            page.setSort("createTime");
            page.setOrder("desc");
        }
        IPage<MemberAddress> addressPage = this.page(PageUtil.initPage(page), queryWrapper);
        return buildManagerPage(addressPage);
    }

    @Override
    public MemberAddress getMemberAddress(String id) {
        AuthUser authUser = UserContext.getCurrentUser();
        if (authUser.getIsSuper() || UserEnums.MANAGER.equals(authUser.getRole())){
            return this.getOne(new QueryWrapper<MemberAddress>().eq("id", id));
        }else{
            return this.getOne(
                    new QueryWrapper<MemberAddress>()
                            .eq("member_id", Objects.requireNonNull(UserContext.getCurrentUser()).getId())
                            .eq("id", id));
        }
    }

    /**
     * 根据地址ID获取当前客户地址信息
     *
     * @return 当前客户的地址信息
     */
    @Override
    public MemberAddress getDefaultMemberAddress() {
        return this.getOne(
                new QueryWrapper<MemberAddress>()
                        .eq("member_id", Objects.requireNonNull(UserContext.getCurrentUser()).getId())
                        .eq("is_default", true));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberAddress saveMemberAddress(MemberAddress memberAddress) {
        normalizeAddressMobile(memberAddress, null);
        //判断当前地址是否为默认地址，如果为默认需要将其他的地址修改为非默认
        removeDefaultAddress(memberAddress);
        //添加客户地址
        this.save(memberAddress);

        return memberAddress;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberAddress updateMemberAddress(MemberAddress memberAddress) {
        MemberAddress originalMemberAddress = this.getMemberAddress(memberAddress.getId());
        if (originalMemberAddress != null) {
            normalizeAddressMobile(memberAddress, originalMemberAddress);
            if (memberAddress.getIsDefault() == null) {
                memberAddress.setIsDefault(false);
            }
            if (CharSequenceUtil.isBlank(memberAddress.getMemberId())) {
                memberAddress.setMemberId(originalMemberAddress.getMemberId());
            }
            //判断当前地址是否为默认地址，如果为默认需要将其他的地址修改为非默认
            removeDefaultAddress(memberAddress);
            this.saveOrUpdate(memberAddress);
        }

        return memberAddress;
    }

    @Override
    public boolean removeMemberAddress(String id) {
        return this.remove(new QueryWrapper<MemberAddress>()
                .eq("id", id));
    }

    /**
     * 修改客户默认收件地址
     *
     * @param memberAddress 收件地址
     */
    private void removeDefaultAddress(MemberAddress memberAddress) {
        //如果不是默认地址不需要处理
        if (Boolean.TRUE.equals(memberAddress.getIsDefault())) {
            //将客户的地址修改为非默认地址
            LambdaUpdateWrapper<MemberAddress> lambdaUpdateWrapper = Wrappers.lambdaUpdate();
            lambdaUpdateWrapper.set(MemberAddress::getIsDefault, false);
            lambdaUpdateWrapper.eq(MemberAddress::getMemberId, memberAddress.getMemberId());
            this.update(lambdaUpdateWrapper);
        }

    }

    private void normalizeAddressMobile(MemberAddress memberAddress, MemberAddress originalMemberAddress) {
        String mobile = CharSequenceUtil.trim(memberAddress.getMobile());
        if (CharSequenceUtil.isBlank(mobile)) {
            memberAddress.setMobile("");
            return;
        }
        if (isMaskedMobile(mobile)) {
            if (originalMemberAddress != null) {
                memberAddress.setMobile(originalMemberAddress.getMobile());
                return;
            }
            throw new ServiceException("手机号已脱敏，请输入完整手机号后再保存");
        }
        if (!PHONE_PATTERN.matcher(mobile).matches()) {
            throw new ServiceException("手机号格式不正确");
        }
        memberAddress.setMobile(mobile);
    }

    private boolean isMaskedMobile(String mobile) {
        return mobile.contains("*");
    }

    private IPage<MemberAddressManagerVO> buildManagerPage(IPage<MemberAddress> addressPage) {
        Page<MemberAddressManagerVO> result = new Page<>(addressPage.getCurrent(), addressPage.getSize(), addressPage.getTotal());
        if (addressPage.getRecords() == null || addressPage.getRecords().isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }
        List<String> memberIds = addressPage.getRecords().stream()
                .map(MemberAddress::getMemberId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .toList();
        Map<String, Member> memberMap = memberIds.isEmpty()
                ? Collections.emptyMap()
                : memberService.listByIds(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity(), (left, right) -> left));

        List<MemberAddressManagerVO> records = addressPage.getRecords().stream().map(address -> {
            MemberAddressManagerVO vo = BeanUtil.copyProperties(address, MemberAddressManagerVO.class);
            Member member = memberMap.get(address.getMemberId());
            if (member != null) {
                vo.setMemberNickName(member.getNickName());
                vo.setMemberUsername(member.getUsername());
            }
            return vo;
        }).toList();
        result.setRecords(records);
        return result;
    }

    private List<String> resolveMemberIdsByKeyword(String memberKeyword) {
        String keyword = CharSequenceUtil.trim(memberKeyword);
        if (CharSequenceUtil.isBlank(keyword)) {
            return null;
        }
        LambdaQueryWrapper<Member> memberQuery = Wrappers.lambdaQuery();
        if (keyword.matches("^1\\d{10}$")) {
            memberQuery.eq(Member::getMobile, keyword);
        } else {
            memberQuery.and(wrapper -> wrapper.like(Member::getNickName, keyword)
                    .or()
                    .like(Member::getUsername, keyword)
                    .or()
                    .like(Member::getMobile, keyword));
        }
        return memberService.list(memberQuery).stream()
                .map(Member::getId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .toList();
    }

    private IPage<MemberAddressManagerVO> emptyAddressPage(PageVO page) {
        Page<MemberAddressManagerVO> result = new Page<>(page.getPageNumber(), page.getPageSize(), 0);
        result.setRecords(Collections.emptyList());
        return result;
    }
}
