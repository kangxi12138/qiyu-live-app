package org.qiyu.live.gift.provider.rpc;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.qiyu.live.common.interfaces.utils.ConvertBeanUtils;
import org.qiyu.live.gift.dto.SkuDetailInfoDTO;
import org.qiyu.live.gift.dto.SkuInfoDTO;
import org.qiyu.live.gift.interfaces.ISkuInfoRPC;
import org.qiyu.live.gift.provider.dao.po.SkuInfoPO;
import org.qiyu.live.gift.provider.service.IAnchorShopInfoService;
import org.qiyu.live.gift.provider.service.ISkuInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author kangxi
 * @Date: Created in 20:11 2023/10/3
 * @Description
 */
@DubboService
public class SkuInfoRPCImpl implements ISkuInfoRPC {

    @Resource
    private ISkuInfoService skuInfoService;
    @Resource
    private IAnchorShopInfoService anchorShopInfoService;

    @Override
    public List<SkuInfoDTO> queryByAnchorId(Long anchorId) {
        List<Long> skuIdLIst = anchorShopInfoService.querySkuIdByAnchorId(anchorId);
        List<SkuInfoPO> skuInfoPOS = skuInfoService.queryBySkuIds(skuIdLIst);
        return ConvertBeanUtils.convertList(skuInfoPOS,SkuInfoDTO.class);
    }

    @Override
    public SkuDetailInfoDTO queryBySkuId(Long skuId) {
        SkuInfoPO skuInfoPO = skuInfoService.queryBySkuIdFromCache(skuId);
        return ConvertBeanUtils.convert(skuInfoPO,SkuDetailInfoDTO.class);
    }
}
