package cn.lili.controller.other;

import cn.lili.common.aop.annotation.DemoSite;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.common.vo.SearchVO;
import cn.lili.modules.verification.entity.dos.VerificationSource;
import cn.lili.modules.verification.entity.enums.VerificationSourceEnum;
import cn.lili.modules.verification.service.VerificationService;
import cn.lili.modules.verification.service.VerificationSourceService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 管理端,验证码资源维护接口
 *
 * @author Chopper
 * @since 2020/12/7 11:33
 */
@RestController
@RequestMapping("/manager/other/verificationSource")
public class VerificationSourceController {

    @Autowired
    private VerificationSourceService verificationSourceService;

    @Autowired
    private VerificationService verificationService;

    @GetMapping("/{id}")
    public ResultMessage<VerificationSource> get(@PathVariable String id) {

        VerificationSource verificationSource = verificationSourceService.getById(id);
        normalizeType(verificationSource);
        return ResultUtil.data(verificationSource);
    }

    @GetMapping
    public ResultMessage<IPage<VerificationSource>> getByPage(VerificationSource entity,
                                                              SearchVO searchVo,
                                                              PageVO page) {
        IPage<VerificationSource> data = verificationSourceService.getByPage(entity, searchVo, page);
        if (data != null && data.getRecords() != null) {
            data.getRecords().forEach(this::normalizeType);
        }
        return ResultUtil.data(data);
    }

    @PostMapping
    @DemoSite
    public ResultMessage<VerificationSource> save(VerificationSource verificationSource) {
        normalizeType(verificationSource);
        verificationService.checkCreateVerification(verificationSource.getType(), verificationSource.getResource());
        verificationSourceService.save(verificationSource);
        verificationSourceService.initCache();
        return ResultUtil.data(verificationSource);
    }

    @PutMapping("/{id}")

    @DemoSite
 public ResultMessage<VerificationSource> update(@PathVariable String id, VerificationSource verificationSource) {
        verificationSource.setId(id);
        normalizeType(verificationSource);
        verificationService.checkCreateVerification(verificationSource.getType(), verificationSource.getResource());
        verificationSourceService.updateById(verificationSource);
        verificationSourceService.initCache();
        return ResultUtil.data(verificationSource);
    }

    @DeleteMapping("/{ids}")
    @DemoSite
    public ResultMessage<Object> delAllByIds(@PathVariable List<String> ids) {

        verificationSourceService.removeByIds(ids);
        verificationSourceService.initCache();
        return ResultUtil.success();
    }

    private void normalizeType(VerificationSource verificationSource) {
        if (verificationSource == null) {
            return;
        }
        verificationSource.setType(VerificationSourceEnum.normalizeType(verificationSource.getType()));
    }
}
