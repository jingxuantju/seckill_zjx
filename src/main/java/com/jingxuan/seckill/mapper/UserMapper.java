package com.jingxuan.seckill.mapper;

import com.jingxuan.seckill.pojo.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author zjx
 * @since 2022-03-22
 */
@Mapper
@Repository
public interface UserMapper extends BaseMapper<User> {

}
