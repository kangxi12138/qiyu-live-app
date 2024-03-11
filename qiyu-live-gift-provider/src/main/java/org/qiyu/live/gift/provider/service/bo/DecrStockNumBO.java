package org.qiyu.live.gift.provider.service.bo;

/**
 * @Author kangxi
 * @Date: Created in 09:14 2023/10/5
 * @Description
 */
public class DecrStockNumBO {

    private boolean isSuccess;
    private boolean noStock;

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public boolean isNoStock() {
        return noStock;
    }

    public void setNoStock(boolean noStock) {
        this.noStock = noStock;
    }
}
