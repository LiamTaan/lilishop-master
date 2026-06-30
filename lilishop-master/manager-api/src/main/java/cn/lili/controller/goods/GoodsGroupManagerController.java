/**
 * 作者：mike
 * 日期：2026-03-30
 */
package cn.lili.controller.goods;

import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.goods.entity.dos.Goods;
import cn.lili.modules.goods.entity.dos.GoodsGroup;
import cn.lili.modules.goods.entity.dos.GoodsGroupGoods;
import cn.lili.modules.goods.entity.dto.GoodsSearchParams;
import cn.lili.modules.goods.service.GoodsGroupGoodsService;
import cn.lili.modules.goods.service.GoodsGroupService;
import cn.lili.modules.goods.service.GoodsService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理端：商品分组接口。
 * <p>
 * 提供分组的增删改查，以及对分组与商品关联关系的维护与查询。
 * </p>
 *
 * @author mike
 * @date 2026-03-30
 */
@RestController
@RequestMapping("/manager/goods/goodsGroup")
public class GoodsGroupManagerController {

    @Autowired
    private GoodsGroupService goodsGroupService;

    @Autowired
    private GoodsGroupGoodsService goodsGroupGoodsService;

    @Autowired
    private GoodsService goodsService;

    /**
     * 通过ID获取商品分组。
     *
     * @param id 商品分组ID
     * @return 商品分组详情
     */
    @GetMapping("/get/{id}")
    public ResultMessage<GoodsGroup> get(@PathVariable String id) {
        return ResultUtil.data(goodsGroupService.getById(id));
    }

    @GetMapping("/getByPage")
    public ResultMessage<IPage<GoodsGroup>> getByPage(PageVO page) {
        return ResultUtil.data(goodsGroupService.getByPage(page));
    }

    @PostMapping
    public ResultMessage<Object> add(@Validated @RequestBody GoodsGroup goodsGroup) {
        if (goodsGroupService.save(goodsGroup)) {
            return ResultUtil.success(ResultCode.SUCCESS);
        }
        return ResultUtil.error(ResultCode.ERROR);
    }

    @PutMapping("/update/{id}")
    public ResultMessage<Object> update(@PathVariable String id, @RequestBody GoodsGroup goodsGroup) {
        goodsGroup.setId(id);
        if (goodsGroupService.updateById(goodsGroup)) {
            return ResultUtil.success(ResultCode.SUCCESS);
        }
        return ResultUtil.error(ResultCode.ERROR);
    }

    @DeleteMapping("/delete/{id}")
    public ResultMessage<Object> delete(@PathVariable String id) {
        long count = goodsGroupGoodsService.countByGroupId(id);
        if (count > 0) {
            return ResultUtil.error(ResultCode.ERROR.code(), "请先移除分组下的商品后再删除");
        }
        if (goodsGroupService.removeById(id)) {
            return ResultUtil.success(ResultCode.SUCCESS);
        }
        return ResultUtil.error(ResultCode.ERROR);
    }

    @PostMapping("/{groupId}/goods")
    public ResultMessage<Object> addGoods(@PathVariable String groupId,
                                          @RequestParam(required = false) String goodsIds) {
        List<String> goodsIdList = parseIds(goodsIds);
        if (goodsGroupGoodsService.updateGroupGoods(groupId, goodsIdList)) {
            return ResultUtil.success(ResultCode.SUCCESS);
        }
        return ResultUtil.error(ResultCode.ERROR);
    }

    @GetMapping("/{groupId}/goods/getByPage")
    public ResultMessage<IPage<Goods>> getGoodsByGroup(@PathVariable String groupId, GoodsSearchParams goodsSearchParams) {
        goodsSearchParams.setGroupId(groupId);
        return ResultUtil.data(goodsService.queryByParams(goodsSearchParams));
    }

    /**
     * 将逗号分隔的字符串解析为商品ID列表。
     *
     * @param ids 逗号分隔的ID字符串（如：1,2,3）
     * @return 去重后的ID列表；空白输入返回空列表
     */
    private List<String> parseIds(String ids) {
        if (CharSequenceUtil.isBlank(ids)) {
            return Collections.emptyList();
        }
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }
}

