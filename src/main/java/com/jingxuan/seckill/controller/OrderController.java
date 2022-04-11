package com.jingxuan.seckill.controller;


import com.jingxuan.seckill.pojo.User;
import com.jingxuan.seckill.service.IOrderService;
import com.jingxuan.seckill.vo.OrderDeatilVo;
import com.jingxuan.seckill.vo.RespBean;
import com.jingxuan.seckill.vo.RespBeanEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zjx
 * @since 2022-03-23
 */
@RestController
@RequestMapping("/order")
@Api(value = "订单", tags = "订单")
public class OrderController {

    @Autowired
    private IOrderService OrderService;


    @ApiOperation("订单")
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @ResponseBody
    public RespBean detail(User tUser, Long orderId) {
        if (tUser == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        OrderDeatilVo orderDeatilVo = OrderService.detail(orderId);
        return RespBean.success(orderDeatilVo);
    }
}