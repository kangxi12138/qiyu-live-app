package org.qiyu.live.gift.interfaces;

import org.qiyu.live.gift.dto.SkuDetailInfoDTO;
import org.qiyu.live.gift.dto.SkuInfoDTO;

import java.util.List;

/**
 * @Author kangxi
 * @Date: Created in 20:09 2023/10/3
 * @Description
 */
public interface ISkuInfoRPC {

    /**
     * 根据主播id查询商品信息
     *
     * @param anchorId
     * @return
     */
    List<SkuInfoDTO> queryByAnchorId(Long anchorId);

    /**
     * 查询商品详情
     *
     * @param skuId
     * @return
     */
    SkuDetailInfoDTO queryBySkuId(Long skuId);
}
