package com.jingxuan.seckill.service;

import com.jingxuan.seckill.pojo.Goods;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jingxuan.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 * 商品表 服务类
 * </p>
 *
 * @author zjx
 * @since 2022-03-23
 */
public interface IGoodsService extends IService<Goods> {

    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVobyGoodsId(Long goodsId);
}
