package cn.lili.modules.member.entity.dto;

import cn.lili.modules.member.entity.enums.LoginSessionChannelEnum;
import cn.lili.modules.member.entity.vo.LoginIdentityOptionVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 登录会话缓存对象
 *
 * @author OpenAI
 */
@Data
public class LoginSessionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;

    private String memberId;

    private String mobile;

    private LoginSessionChannelEnum channel;

    private List<LoginIdentityOptionVO> identityOptions;

    private Long expireAt;
}
