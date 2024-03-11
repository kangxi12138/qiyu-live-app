package org.qiyu.live.bank.provider.service;

import org.qiyu.live.bank.provider.dao.po.PayTopicPO;

/**
 * @Author kangxi
 * @Date: Created in 22:08 2024/1/29
 * @Description
 */
public interface IPayTopicService {

    /**
     * 根据code查询
     *
     * @param code
     * @return
     */
    PayTopicPO getByCode(Integer code);
}
