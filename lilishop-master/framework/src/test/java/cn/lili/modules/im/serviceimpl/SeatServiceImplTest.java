package cn.lili.modules.im.serviceimpl;

import cn.lili.modules.im.entity.dos.Seat;
import cn.lili.modules.im.entity.enums.OnlineStatusEnum;
import cn.lili.modules.im.entity.vo.SeatVO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class SeatServiceImplTest {

    @Test
    void seatVoListShouldConvertSeatToVoAndFillOnlineStatus() {
        SeatServiceImpl seatService = new SeatServiceImpl() {
            @Override
            public List<Seat> list(com.baomidou.mybatisplus.core.conditions.Wrapper<Seat> queryWrapper) {
                Seat seat = new Seat();
                seat.setId("seat-1");
                seat.setTenantId("store-1");
                seat.setUsername("seat_user");
                seat.setNickName("Seat User");
                return List.of(seat);
            }
        };

        List<SeatVO> result = seatService.seatVoList("store-1");

        assertEquals(1, result.size());
        SeatVO seatVO = result.get(0);
        assertInstanceOf(SeatVO.class, seatVO);
        assertEquals("seat-1", seatVO.getId());
        assertEquals("store-1", seatVO.getTenantId());
        assertEquals("seat_user", seatVO.getUsername());
        assertEquals("Seat User", seatVO.getNickName());
        assertEquals(OnlineStatusEnum.ONLINE.name(), seatVO.getOnlineStatus());
    }
}
