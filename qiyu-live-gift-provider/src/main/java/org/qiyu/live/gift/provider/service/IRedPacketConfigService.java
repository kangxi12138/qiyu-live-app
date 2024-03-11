package org.qiyu.live.gift.provider.service;

import org.qiyu.live.gift.dto.RedPacketConfigReqDTO;
import org.qiyu.live.gift.dto.RedPacketReceiveDTO;
import org.qiyu.live.gift.provider.dao.po.RedPacketConfigPO;

/**
 * @Author kangxi
 * @Date: Created in 08:14 2023/9/5
 * @Description
 */
public interface IRedPacketConfigService {

    /**
     * 支持根据主播id查询是否有红包雨配置特权
     *
     * @param anchorId
     */
    RedPacketConfigPO queryByAnchorId(Long anchorId);

    /**
     * 根据红包雨配置code检索信息
     *
     * @param configCode
     */
    RedPacketConfigPO queryByConfigCode(String configCode);

    /**
     * 新增红包配置
     *
     * @param redPacketConfigPO
     */
    boolean addOne(RedPacketConfigPO redPacketConfigPO);

    /**
     * 更新红包雨配置
     *
     * @param redPacketConfigPO
     * @return
     */
    boolean updateById(RedPacketConfigPO redPacketConfigPO);


    /**
     * 提前生成红包雨的数据
     *
     * @param anchorId
     */
    boolean prepareRedPacket(Long anchorId);


    /**
     * 领取红包
     *
     * @param reqDTO
     * @return
     */
    RedPacketReceiveDTO receiveRedPacket(RedPacketConfigReqDTO reqDTO);

    /**
     * 领红包之后的处理
     *
     * @param reqDTO
     * @param price
     */
    void receiveRedPacketHandle(RedPacketConfigReqDTO reqDTO,Integer price);

    /**
     * 广播直播间用户，开始抢红包
     *
     * @param reqDTO
     */
    Boolean startRedPacket(RedPacketConfigReqDTO reqDTO);
}
