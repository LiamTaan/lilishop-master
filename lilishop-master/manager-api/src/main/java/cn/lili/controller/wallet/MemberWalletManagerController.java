package cn.lili.controller.wallet;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.wallet.entity.dto.MemberWalletSearchParams;
import cn.lili.modules.wallet.entity.dto.MemberWalletUpdateDTO;
import cn.lili.modules.wallet.entity.enums.DepositServiceTypeEnum;
import cn.lili.modules.wallet.entity.vo.MemberWalletPageVO;
import cn.lili.modules.wallet.entity.vo.MemberWalletVO;
import cn.lili.modules.wallet.service.MemberWalletService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端,预存款接口
 *
 * @author pikachu
 * @since 2020/11/16 10:07 下午
 */
@RestController
@RequestMapping("/manager/wallet/wallet")
public class MemberWalletManagerController {
    @Autowired
    private MemberWalletService memberWalletService;

    @GetMapping("/page")
    public ResultMessage<IPage<MemberWalletPageVO>> page(MemberWalletSearchParams searchParams) {
        return ResultUtil.data(memberWalletService.pageWallets(searchParams));
    }

    @GetMapping
    public ResultMessage<MemberWalletVO> get(@RequestParam("memberId") String memberId) {
        return ResultUtil.data(memberWalletService.getMemberWallet(memberId));
    }

    @PutMapping("/increase")
    public ResultMessage<Object> increase(String memberId ,Double rechargeMoney) {

        MemberWalletUpdateDTO memberWalletUpdateDTO=new MemberWalletUpdateDTO(rechargeMoney,memberId,"运营后台手动充值:"+rechargeMoney, DepositServiceTypeEnum.WALLET_RECHARGE.name());
        if(memberWalletService.increase(memberWalletUpdateDTO)){
            return ResultUtil.success();
        }
        return ResultUtil.error();
    }




}
