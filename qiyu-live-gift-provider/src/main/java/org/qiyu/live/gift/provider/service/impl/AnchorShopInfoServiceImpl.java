package org.qiyu.live.gift.provider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.qiyu.live.common.interfaces.enums.CommonStatusEum;
import org.qiyu.live.gift.provider.dao.mapper.AnchorShopInfoMapper;
import org.qiyu.live.gift.provider.dao.po.AnchorShopInfoPO;
import org.qiyu.live.gift.provider.service.IAnchorShopInfoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author kangxi
 * @Date: Created in 20:05 2023/10/3
 * @Description
 */
@Service
public class AnchorShopInfoServiceImpl implements IAnchorShopInfoService {

    @Resource
    private AnchorShopInfoMapper anchorShopInfoMapper;

    @Override
    public List<Long> querySkuIdByAnchorId(Long anchorId) {
        LambdaQueryWrapper<AnchorShopInfoPO> qw = new LambdaQueryWrapper<>();
        qw.eq(AnchorShopInfoPO::getAnchorId,anchorId);
        qw.eq(AnchorShopInfoPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        return anchorShopInfoMapper.selectList(qw).stream().map(AnchorShopInfoPO::getSkuId).collect(Collectors.toList());
    }

    @Override
    public List<Long> queryAllValidAnchorId() {
        LambdaQueryWrapper<AnchorShopInfoPO> qw = new LambdaQueryWrapper<>();
        qw.eq(AnchorShopInfoPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        return anchorShopInfoMapper.selectList(qw).stream().map(AnchorShopInfoPO::getAnchorId).collect(Collectors.toList());
    }
}
