package com.jingxuan.seckill.mapper;

import com.jingxuan.seckill.pojo.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zjx
 * @since 2022-03-23
 */
@Mapper
@Repository
public interface OrderMapper extends BaseMapper<Order> {

}
