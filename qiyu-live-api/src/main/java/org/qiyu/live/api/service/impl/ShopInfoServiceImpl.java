package org.qiyu.live.api.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.qiyu.live.api.error.ApiErrorEnum;
import org.qiyu.live.api.service.IShopInfoService;
import org.qiyu.live.api.vo.PrepareOrderVO;
import org.qiyu.live.api.vo.req.ShopCarReqVO;
import org.qiyu.live.api.vo.req.SkuInfoReqVO;
import org.qiyu.live.api.vo.resp.ShopCarRespVO;
import org.qiyu.live.api.vo.resp.SkuDetailInfoVO;
import org.qiyu.live.api.vo.resp.SkuInfoVO;
import org.qiyu.live.common.interfaces.utils.ConvertBeanUtils;
import org.qiyu.live.gift.dto.*;
import org.qiyu.live.gift.interfaces.IShopCarRPC;
import org.qiyu.live.gift.interfaces.ISkuInfoRPC;
import org.qiyu.live.gift.interfaces.ISkuOrderInfoRPC;
import org.qiyu.live.living.interfaces.dto.LivingRoomRespDTO;
import org.qiyu.live.living.interfaces.rpc.ILivingRoomRpc;
import org.qiyu.live.web.starter.context.QiyuRequestContext;
import org.qiyu.live.web.starter.error.BizBaseErrorEnum;
import org.qiyu.live.web.starter.error.ErrorAssert;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author kangxi
 * @Date: Created in 20:15 2023/10/3
 * @Description
 */
@Service
public class ShopInfoServiceImpl implements IShopInfoService {

    @DubboReference
    private ILivingRoomRpc livingRoomRpc;
    @DubboReference
    private ISkuInfoRPC skuInfoRPC;
    @DubboReference
    private IShopCarRPC shopCarRPC;
    @DubboReference
    private ISkuOrderInfoRPC skuOrderInfoRPC;

    @Override
    public List<SkuInfoVO> queryByRoomId(Integer roomId) {
        LivingRoomRespDTO livingRoomRespDTO = livingRoomRpc.queryByRoomId(roomId);
        ErrorAssert.isNotNull(livingRoomRespDTO, BizBaseErrorEnum.PARAM_ERROR);
        Long anchorId = livingRoomRespDTO.getAnchorId();
        List<SkuInfoDTO> skuInfoDTOS = skuInfoRPC.queryByAnchorId(anchorId);
        ErrorAssert.isTure(CollectionUtils.isNotEmpty(skuInfoDTOS),BizBaseErrorEnum.PARAM_ERROR);
        return ConvertBeanUtils.convertList(skuInfoDTOS,SkuInfoVO.class);
    }

    @Override
    public SkuDetailInfoVO detail(SkuInfoReqVO skuInfoReqVO) {
        return ConvertBeanUtils.convert(skuInfoRPC.queryBySkuId(skuInfoReqVO.getSkuId()),SkuDetailInfoVO.class);
    }

    @Override
    public Boolean addCar(ShopCarReqVO shopCarReqVO) {
        ShopCarReqDTO shopCarReqDTO = ConvertBeanUtils.convert(shopCarReqVO, ShopCarReqDTO.class);
        shopCarReqDTO.setUserId(QiyuRequestContext.getUserId());
        return shopCarRPC.addCar(shopCarReqDTO);
    }

    @Override
    public ShopCarRespVO getCarInfo(ShopCarReqVO shopCarReqVO) {
        ShopCarReqDTO shopCarReqDTO = ConvertBeanUtils.convert(shopCarReqVO, ShopCarReqDTO.class);
        shopCarReqDTO.setUserId(QiyuRequestContext.getUserId());
        return ConvertBeanUtils.convert(shopCarRPC.getCarInfo(shopCarReqDTO),ShopCarRespVO.class);
    }

    @Override
    public Boolean removeFromCar(ShopCarReqVO shopCarReqVO) {
        ShopCarReqDTO shopCarReqDTO = ConvertBeanUtils.convert(shopCarReqVO, ShopCarReqDTO.class);
        shopCarReqDTO.setUserId(QiyuRequestContext.getUserId());
        return shopCarRPC.removeFromCar(shopCarReqDTO);
    }

    @Override
    public Boolean clearShopCar(ShopCarReqVO shopCarReqVO) {
        ShopCarReqDTO shopCarReqDTO = ConvertBeanUtils.convert(shopCarReqVO, ShopCarReqDTO.class);
        shopCarReqDTO.setUserId(QiyuRequestContext.getUserId());
        return shopCarRPC.clearShopCar(shopCarReqDTO);
    }

    @Override
    public SkuPrepareOrderInfoDTO prepareOrder(PrepareOrderVO prepareOrderVO) {
        PrepareOrderReqDTO reqDTO = new PrepareOrderReqDTO();
        reqDTO.setUserId(QiyuRequestContext.getUserId());
        reqDTO.setRoomId(prepareOrderVO.getRoomId());
        SkuPrepareOrderInfoDTO skuPrepareOrderInfoDTO = skuOrderInfoRPC.prepareOrder(reqDTO);
        ErrorAssert.isNotNull(skuPrepareOrderInfoDTO, ApiErrorEnum.SKU_IS_NOT_ENOUGH);
        return skuPrepareOrderInfoDTO;
    }

    @Override
    public boolean payNow(PrepareOrderVO prepareOrderVO) {
        prepareOrderVO.setUserId(QiyuRequestContext.getUserId());
        boolean status = skuOrderInfoRPC.payNow(ConvertBeanUtils.convert(prepareOrderVO,PayNowReqDTO.class));
        ErrorAssert.isTure(status,ApiErrorEnum.PAY_ERROR);
        return status;
    }
}
