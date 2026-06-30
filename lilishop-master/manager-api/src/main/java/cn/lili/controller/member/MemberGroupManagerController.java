package cn.lili.controller.member;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.member.entity.dos.MemberGroup;
import cn.lili.modules.member.entity.dos.MemberGroupUser;
import cn.lili.modules.member.entity.dto.MemberGroupUsersUpdateDTO;
import cn.lili.modules.member.entity.dto.MemberUserGroupsUpdateDTO;
import cn.lili.modules.member.service.MemberGroupService;
import cn.lili.modules.member.service.MemberGroupUserService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manager/member/memberGroup")
public class MemberGroupManagerController {

    @Autowired
    private MemberGroupService memberGroupService;

    @Autowired
    private MemberGroupUserService memberGroupUserService;

    @GetMapping("/get/{id}")
    public ResultMessage<MemberGroup> get(@PathVariable String id) {
        return ResultUtil.data(memberGroupService.getById(id));
    }

    @GetMapping("/getByPage")
    public ResultMessage<IPage<MemberGroup>> getByPage(PageVO page) {
        return ResultUtil.data(memberGroupService.page(PageUtil.initPage(page)));
    }

    @PostMapping
    public ResultMessage<Object> add(@Validated @RequestBody MemberGroup memberGroup) {
        if (memberGroupService.save(memberGroup)) {
            return ResultUtil.success(ResultCode.SUCCESS);
        }
        return ResultUtil.error(ResultCode.ERROR);
    }

    @PutMapping("/update/{id}")
    public ResultMessage<Object> update(@PathVariable String id, @RequestBody MemberGroup memberGroup) {
        memberGroup.setId(id);
        if (memberGroupService.updateById(memberGroup)) {
            return ResultUtil.success(ResultCode.SUCCESS);
        }
        return ResultUtil.error(ResultCode.ERROR);
    }

    @DeleteMapping("/delete/{id}")
    public ResultMessage<Object> delete(@PathVariable String id) {
        long count = memberGroupUserService.countByGroupId(id);
        if (count > 0) {
            return ResultUtil.error(ResultCode.ERROR.code(), "请移除分组下的用户后再删除");
        }
        if (memberGroupService.removeById(id)) {
            return ResultUtil.success(ResultCode.SUCCESS);
        }
        return ResultUtil.error(ResultCode.ERROR);
    }

    @PostMapping("/{groupId}/users")
    public ResultMessage<Object> addUsers(@PathVariable String groupId, @RequestBody @Validated MemberGroupUsersUpdateDTO updateDTO) {
        if (memberGroupUserService.updateGroupUsers(groupId, updateDTO.getMemberIds())) {
            return ResultUtil.success(ResultCode.SUCCESS);
        }
        return ResultUtil.error(ResultCode.ERROR);
    }

    @GetMapping("/{groupId}/users")
    public ResultMessage<IPage<MemberGroupUser>> getGroupUsers(@PathVariable String groupId, PageVO page) {
        return ResultUtil.data(memberGroupUserService.pageByGroupId(groupId, page));
    }

    @DeleteMapping("/{groupId}/user/{memberId}")
    public ResultMessage<Object> removeUser(@PathVariable String groupId, @PathVariable String memberId) {
        if (memberGroupUserService.removeByGroupAndMember(groupId, memberId)) {
            return ResultUtil.success(ResultCode.SUCCESS);
        }
        return ResultUtil.error(ResultCode.ERROR);
    }

    @PostMapping("/user/{memberId}/groups")
    public ResultMessage<Object> setUserGroups(@PathVariable String memberId, @RequestBody @Validated MemberUserGroupsUpdateDTO updateDTO) {
        if (memberGroupUserService.updateUserGroups(memberId, updateDTO.getGroupIds())) {
            return ResultUtil.success(ResultCode.SUCCESS);
        }
        return ResultUtil.error(ResultCode.ERROR);
    }
}
