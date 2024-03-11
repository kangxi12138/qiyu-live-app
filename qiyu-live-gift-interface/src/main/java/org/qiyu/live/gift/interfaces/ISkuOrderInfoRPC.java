package org.qiyu.live.gift.interfaces;

import org.qiyu.live.gift.dto.*;

/**
 * @Author kangxi
 * @Date: Created in 07:09 2023/10/16
 * @Description
 */
public interface ISkuOrderInfoRPC {

    /**
     * 支持多直播间内用户下单的订单查询
     *
     * @param userId
     * @param roomId
     */
    SkuOrderInfoRespDTO queryByUserIdAndRoomId(Long userId, Integer roomId);

    /**
     * 插入一条订单信息
     *
     * @param skuOrderInfoReqDTO
     */
    boolean insertOne(SkuOrderInfoReqDTO skuOrderInfoReqDTO);

    /**
     * 根据订单id修改状态
     *
     * @param reqDTO
     */
    boolean updateOrderStatus(SkuOrderInfoReqDTO reqDTO);

    /**
     * 预支付订单生成
     *
     * @param prepareOrderReqDTO
     */
    SkuPrepareOrderInfoDTO prepareOrder(PrepareOrderReqDTO prepareOrderReqDTO);

    /**
     * 立即支付
     * @param payNowReqDTO
     */
    boolean payNow(PayNowReqDTO payNowReqDTO);
}
