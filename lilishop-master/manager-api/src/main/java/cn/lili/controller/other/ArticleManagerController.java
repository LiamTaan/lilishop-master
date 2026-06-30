package cn.lili.controller.other;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.page.entity.dos.Article;
import cn.lili.modules.page.entity.dto.ArticleSearchParams;
import cn.lili.modules.page.entity.enums.ArticleEnum;
import cn.lili.modules.page.entity.vos.ArticleVO;
import cn.lili.modules.page.service.ArticleService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 管理端,文章接口
 *
 * @author pikachu
 * @since 2020-05-06 15:18:56
 */
@RestController
@RequestMapping("/manager/other/article")
public class ArticleManagerController {

    /**
     * 文章
     */
    @Autowired
    private ArticleService articleService;

    @GetMapping("/{id}")
    public ResultMessage<Article> get(
            @PathVariable String id) {

        return ResultUtil.data(articleService.getById(id));
    }

    @GetMapping("/type/{type}")
    public ResultMessage<Article> getByType(
            @PathVariable String type) {

        return ResultUtil.data(articleService.customGetByType(type));
    }

    @GetMapping("/getByPage")
    public ResultMessage<IPage<ArticleVO>> getByPage(ArticleSearchParams articleSearchParams) {
        return ResultUtil.data(articleService.managerArticlePage(articleSearchParams));
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResultMessage<Article> save(@RequestBody Article article) {
        article.setType(ArticleEnum.OTHER.name());
        articleService.save(article);
        return ResultUtil.data(article);
    }

    @PutMapping(value="/update/{id}", consumes = "application/json", produces = "application/json")
    public ResultMessage<Article> update(@RequestBody Article article, 
            @PathVariable("id") String id) {
        article.setId(id);
        return ResultUtil.data(articleService.updateArticle(article));
    }

    @PutMapping(value = "/updateArticle/{type}/{id}", consumes = "application/json", produces = "application/json")
    public ResultMessage<Article> updateArticle(@RequestBody Article article, 
            @PathVariable("type") String type, @PathVariable("id") String id) {
        article.setId(id);
        article.setType(type);
        return ResultUtil.data(articleService.updateArticleType(article));
    }

    @PutMapping("update/status/{id}")
    public ResultMessage<Article> updateStatus(
            @PathVariable("id") String id, 
            @RequestParam("status") boolean status) {
        articleService.updateArticleStatus(id, status);
        return ResultUtil.success();
    }


    @DeleteMapping("/delByIds/{id}")
    public ResultMessage<Object> delAllByIds(
 @PathVariable String id) {
        articleService.customRemove(id);
        return ResultUtil.success();
    }


}
