package cn.lili.controller.other;

import cn.lili.common.aop.annotation.DemoSite;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.page.entity.dos.PageData;
import cn.lili.modules.page.entity.dto.PageDataDTO;
import cn.lili.modules.page.entity.vos.PageDataListVO;
import cn.lili.modules.page.service.PageDataService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 管理端,页面设置管理接口
 *
 * @author paulGao
 * @since 2020-05-06 15:18:56
 */
@RestController
@RequestMapping("/manager/other/pageData")
public class PageDataManagerController {

    @Autowired
    private PageDataService pageDataService;

    @GetMapping("/{id}")
    public ResultMessage<PageData> getPageData(
 @PathVariable String id) {
        return ResultUtil.data(pageDataService.getById(id));
    }

    @PostMapping("/add")
    public ResultMessage<PageData> addPageData(@Valid PageData pageData) {
        return ResultUtil.data(pageDataService.addPageData(pageData));
    }

    @DemoSite
    @PutMapping("/update/{id}")
    public ResultMessage<PageData> updatePageData(@Valid PageData pageData, 
 @NotNull @PathVariable String id) {
        pageData.setId(id);
        return ResultUtil.data(pageDataService.updatePageData(pageData));
    }

    @GetMapping("/pageDataList")
    public ResultMessage<IPage<PageDataListVO>> pageDataList(PageVO pageVO, PageDataDTO pageDataDTO) {
        return ResultUtil.data(pageDataService.getPageDataList(pageVO, pageDataDTO));
    }

    @PutMapping("/release/{id}")
    @DemoSite
    public ResultMessage<PageData> release(
 @PathVariable String id) {
        return ResultUtil.data(pageDataService.releasePageData(id));
    }

    @DemoSite
    @DeleteMapping("/remove/{id}")
    public ResultMessage<Object> remove(
 @PathVariable String id) {
        return ResultUtil.data(pageDataService.removePageData(id));
    }
}
