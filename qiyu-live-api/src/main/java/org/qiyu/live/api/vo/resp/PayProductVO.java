package org.qiyu.live.api.vo.resp;

import java.util.List;

/**
 * @Author kangxi
 * @Date: Created in 07:29 2024/1/28
 * @Description
 */
public class PayProductVO {

    /**
     * 当前余额
     */
    private Integer currentBalance;

    /**
     * 一系列的付费产品
     */
    private List<PayProductItemVO> payProductItemVOList;

    public Integer getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(Integer currentBalance) {
        this.currentBalance = currentBalance;
    }

    public List<PayProductItemVO> getPayProductItemVOList() {
        return payProductItemVOList;
    }

    public void setPayProductItemVOList(List<PayProductItemVO> payProductItemVOList) {
        this.payProductItemVOList = payProductItemVOList;
    }
}
