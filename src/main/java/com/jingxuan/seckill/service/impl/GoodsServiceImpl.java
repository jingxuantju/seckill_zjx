package com.jingxuan.seckill.service.impl;

import com.jingxuan.seckill.pojo.Goods;
import com.jingxuan.seckill.mapper.GoodsMapper;
import com.jingxuan.seckill.service.IGoodsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jingxuan.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author zjx
 * @since 2022-03-23
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {

    @Autowired
    private GoodsMapper GoodsMapper;

    @Override
    public List<GoodsVo> findGoodsVo() {
        return GoodsMapper.findGoodsVo();
    }

    @Override
    public GoodsVo findGoodsVobyGoodsId(Long goodsId) {
        return GoodsMapper.findGoodsVobyGoodsId(goodsId);
    }
}
