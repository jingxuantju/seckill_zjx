package com.jingxuan.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jingxuan.seckill.exception.GlobalException;
import com.jingxuan.seckill.pojo.Order;
import com.jingxuan.seckill.mapper.OrderMapper;
import com.jingxuan.seckill.pojo.SeckillGoods;
import com.jingxuan.seckill.pojo.SeckillOrder;
import com.jingxuan.seckill.pojo.User;
import com.jingxuan.seckill.service.IGoodsService;
import com.jingxuan.seckill.service.IOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jingxuan.seckill.service.ISeckillGoodsService;
import com.jingxuan.seckill.service.ISeckillOrderService;
import com.jingxuan.seckill.utils.MD5Util;
import com.jingxuan.seckill.utils.UUIDUtil;
import com.jingxuan.seckill.vo.GoodsVo;
import com.jingxuan.seckill.vo.OrderDeatilVo;
import com.jingxuan.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zjx
 * @since 2022-03-23
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    private ISeckillGoodsService SeckillGoodsService;

    @Autowired
    private ISeckillOrderService SeckillOrderService;

    @Autowired
    private OrderMapper OrderMapper;

    @Autowired
    private IGoodsService GoodsService;

    @Autowired
    private RedisTemplate redisTemplate;


    @Transactional
    @Override
    public Order secKill(User user, GoodsVo goodsVo) {
        ValueOperations valueOperations = redisTemplate.opsForValue();

        SeckillGoods seckillGoods = SeckillGoodsService.getOne(new QueryWrapper<SeckillGoods>()
                .eq("goods_id", goodsVo.getId()));
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
//        SeckillGoodsService.updateById(seckillGoods);
//        boolean seckillGoodsResult = SeckillGoodsService.update(new UpdateWrapper<SeckillGoods>()
//                .set("stock_count", seckillGoods.getStockCount())
//                .eq("id", seckillGoods.getId())
//                .gt("stock_count", 0)
//        );
        boolean seckillGoodsResult = SeckillGoodsService.update(new UpdateWrapper<SeckillGoods>()
                .setSql("stock_count = " + "stock_count-1")
                .eq("goods_id", goodsVo.getId())
                .gt("stock_count", 0)
        );
//        if (!seckillGoodsResult) {
//            return null;
//        }

        if (seckillGoods.getStockCount() < 1) {
            //判断是否还有库存
            valueOperations.set("isStockEmpty:" + goodsVo.getId(), "0");
            return null;
        }

        //生成订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goodsVo.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goodsVo.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        OrderMapper.insert(order);
        //生成秒杀订单
        SeckillOrder SeckillOrder = new SeckillOrder();
        SeckillOrder.setUserId(user.getId());
        SeckillOrder.setOrderId(order.getId());
        SeckillOrder.setGoodsId(goodsVo.getId());
        SeckillOrderService.save(SeckillOrder);
        redisTemplate.opsForValue().set("order:" + user.getId() + ":" + goodsVo.getId(), SeckillOrder);
        return order;
    }

    @Override
    public OrderDeatilVo detail(Long orderId) {
        if (orderId == null) {
            throw new GlobalException(RespBeanEnum.ORDER_NOT_EXIST);
        }
        Order Order = OrderMapper.selectById(orderId);
        GoodsVo goodsVobyGoodsId = GoodsService.findGoodsVobyGoodsId(Order.getGoodsId());
        OrderDeatilVo orderDeatilVo = new OrderDeatilVo();
        orderDeatilVo.setTOrder(Order);
        orderDeatilVo.setGoodsVo(goodsVobyGoodsId);
        return orderDeatilVo;
    }

    @Override
    public String createPath(User user, Long goodsId) {
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        redisTemplate.opsForValue().set("seckillPath:" + user.getId() + ":" + goodsId, str, 1, TimeUnit.MINUTES);
        return str;
    }

    @Override
    public boolean checkPath(User user, Long goodsId, String path) {
        if (user == null || goodsId < 0 || StringUtils.isEmpty(path)) {
            return false;
        }
        String redisPath = (String) redisTemplate.opsForValue().get("seckillPath:" + user.getId() + ":" + goodsId);
        return path.equals(redisPath);
    }


    @Override
    public boolean checkCaptcha(User user, Long goodsId, String captcha) {
        if (user == null || goodsId < 0 || StringUtils.isEmpty(captcha)) {
            return false;
        }
        String redisCaptcha = (String) redisTemplate.opsForValue()
                .get("captcha:" + user.getId() + ":" + goodsId);
        return captcha.equals(redisCaptcha);
    }
}
