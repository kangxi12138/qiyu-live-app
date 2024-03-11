package org.qiyu.live.api.vo.resp;

/**
 * @Author kangxi
 * @Date: Created in 20:19 2024/1/29
 * @Description
 */
public class PayProductRespVO {

    private String orderId;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }


    @Override
    public String toString() {
        return "PayProductRespVO{" +
                "orderId='" + orderId + '\'' +
                '}';
    }
}
