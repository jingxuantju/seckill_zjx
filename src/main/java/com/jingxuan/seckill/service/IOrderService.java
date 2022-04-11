package com.jingxuan.seckill.service;

import com.jingxuan.seckill.pojo.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jingxuan.seckill.pojo.User;
import com.jingxuan.seckill.vo.GoodsVo;
import com.jingxuan.seckill.vo.OrderDeatilVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zjx
 * @since 2022-03-23
 */
public interface IOrderService extends IService<Order> {


    /**
     * 秒杀
     *
     * @param user    用户对象
     * @param goodsVo 商品对象
     * @operation add
     **/
    Order secKill(User user, GoodsVo goodsVo);

    OrderDeatilVo detail(Long orderId);

    String createPath(User user, Long goodsId);

    boolean checkPath(User user, Long goodsId, String path);

    boolean checkCaptcha(User user, Long goodsId, String captcha);
}
