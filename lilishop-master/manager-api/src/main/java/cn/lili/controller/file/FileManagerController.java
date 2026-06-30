package cn.lili.controller.file;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.file.entity.File;
import cn.lili.modules.file.entity.dto.FileOwnerDTO;
import cn.lili.modules.file.service.FileService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 管理端,文件管理管理接口
 *
 * @author Chopper
 * @since 2020/11/26 15:41
 */
@RestController
@RequestMapping("/manager/common/file")
public class FileManagerController {

    @Autowired
    private FileService fileService;


    @GetMapping
    public ResultMessage<IPage<File>> adminFiles(
 FileOwnerDTO fileOwnerDTO) {

        return ResultUtil.data(fileService.customerPage(fileOwnerDTO));
    }


    @PostMapping("/rename")
    public ResultMessage<File> upload(
 String id, 
 String newName) {
        File file = fileService.getById(id);
        file.setName(newName);
        fileService.updateById(file);
        return ResultUtil.data(file);
    }

    @DeleteMapping("/delete/{ids}")
    public ResultMessage delete(
 @PathVariable List<String> ids) {
        fileService.batchDelete(ids);
        return ResultUtil.success();
    }

}
