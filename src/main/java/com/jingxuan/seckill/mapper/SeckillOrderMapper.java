package com.jingxuan.seckill.mapper;

import com.jingxuan.seckill.pojo.SeckillOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 秒杀订单表 Mapper 接口
 * </p>
 *
 * @author zjx
 * @since 2022-03-23
 */
@Mapper
@Repository
public interface SeckillOrderMapper extends BaseMapper<SeckillOrder> {

}
