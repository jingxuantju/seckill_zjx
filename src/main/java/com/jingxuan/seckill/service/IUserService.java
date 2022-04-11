package com.jingxuan.seckill.service;

import com.jingxuan.seckill.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jingxuan.seckill.vo.LoginVo;
import com.jingxuan.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author zjx
 * @since 2022-03-22
 */
public interface IUserService extends IService<User> {

    RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);


    /**
     * 根据cookie获取用户
     *
     * @param userTicket
     **/
    User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response);
}