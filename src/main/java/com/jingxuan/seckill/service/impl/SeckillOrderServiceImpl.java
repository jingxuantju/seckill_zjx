package com.jingxuan.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jingxuan.seckill.pojo.SeckillOrder;
import com.jingxuan.seckill.mapper.SeckillOrderMapper;
import com.jingxuan.seckill.pojo.User;
import com.jingxuan.seckill.service.ISeckillOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 秒杀订单表 服务实现类
 * </p>
 *
 * @author zjx
 * @since 2022-03-23
 */
@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements ISeckillOrderService {

    @Autowired
    private SeckillOrderMapper SeckillOrderMapper;
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public Long getResult(User User, Long goodsId) {

        SeckillOrder SeckillOrder = SeckillOrderMapper.selectOne(new QueryWrapper<SeckillOrder>()
                .eq("user_id", User.getId())
                .eq("goods_id", goodsId));
        if (null != SeckillOrder) {
            return SeckillOrder.getOrderId();
        } else if (redisTemplate.hasKey("isStockEmpty:" + goodsId)) {
            return -1L;
        } else {
            return 0L;
        }

    }
}
