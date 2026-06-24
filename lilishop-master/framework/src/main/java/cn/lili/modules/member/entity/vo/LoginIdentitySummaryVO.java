package cn.lili.modules.member.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 登录身份汇总
 *
 * @author OpenAI
 */
@Data
public class LoginIdentitySummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "当前账号所有身份选项的状态汇总。通常用于登录完成后继续展示身份切换入口。")
    private List<LoginIdentityOptionVO> identityOptions;
}
