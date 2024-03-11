package org.qiyu.live.gift.provider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.idea.qiyu.live.framework.redis.starter.key.GiftProviderCacheKeyBuilder;
import org.qiyu.live.common.interfaces.enums.CommonStatusEum;
import org.qiyu.live.gift.provider.dao.mapper.SkuInfoMapper;
import org.qiyu.live.gift.provider.dao.po.AnchorShopInfoPO;
import org.qiyu.live.gift.provider.dao.po.SkuInfoPO;
import org.qiyu.live.gift.provider.service.ISkuInfoService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author kangxi
 * @Date: Created in 20:08 2023/10/3
 * @Description
 */
@Service
public class SkuInfoServiceImpl implements ISkuInfoService {

    @Resource
    private SkuInfoMapper skuInfoMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private GiftProviderCacheKeyBuilder cacheKeyBuilder;

    @Override
    public List<SkuInfoPO> queryBySkuIds(List<Long> skuIdList) {
        if(CollectionUtils.isEmpty(skuIdList)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SkuInfoPO> qw = new LambdaQueryWrapper<>();
        qw.in(SkuInfoPO::getSkuId, skuIdList);
        qw.eq(SkuInfoPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        return skuInfoMapper.selectList(qw);
    }

    @Override
    public SkuInfoPO queryBySkuId(Long skuId) {
        LambdaQueryWrapper<SkuInfoPO> qw = new LambdaQueryWrapper<>();
        qw.eq(SkuInfoPO::getSkuId, skuId);
        qw.eq(SkuInfoPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        qw.last("limit 1");
        return skuInfoMapper.selectOne(qw);
    }

    @Override
    public SkuInfoPO queryBySkuIdFromCache(Long skuId) {
        String detailKey = cacheKeyBuilder.buildSkuDetail(skuId);
        Object skuInfoCacheObj = redisTemplate.opsForValue().get(detailKey);
        if (skuInfoCacheObj != null) {
            return (SkuInfoPO) skuInfoCacheObj;
        }
        SkuInfoPO skuInfoPO = this.queryBySkuId(skuId);
        if(skuInfoPO == null) {
            return null;
        }
        redisTemplate.opsForValue().set(detailKey,skuInfoPO,1, TimeUnit.DAYS);
        return skuInfoPO;
    }
}
