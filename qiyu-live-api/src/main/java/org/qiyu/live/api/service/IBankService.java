package org.qiyu.live.api.service;

import org.qiyu.live.api.vo.req.PayProductReqVO;
import org.qiyu.live.api.vo.resp.PayProductRespVO;
import org.qiyu.live.api.vo.resp.PayProductVO;

/**
 * @Author kangxi
 * @Date: Created in 08:26 2024/1/27
 * @Description
 */
public interface IBankService {

    /**
     * 查询相关的产品列表信息
     *
     * @param type
     * @return
     */
    PayProductVO products(Integer type);

    /**
     * 发起支付
     *
     * @param payProductReqVO
     * @return
     */
    PayProductRespVO payProduct(PayProductReqVO payProductReqVO);
}
