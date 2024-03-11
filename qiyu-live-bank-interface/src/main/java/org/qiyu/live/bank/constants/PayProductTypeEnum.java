package org.qiyu.live.bank.constants;

/**
 * @Author kangxi
 * @Date: Created in 08:10 2024/1/27
 * @Description
 */
public enum PayProductTypeEnum {

    QIYU_COIN(0,"直播间充值-旗鱼虚拟币产品");

    PayProductTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    Integer code;
    String desc;

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
