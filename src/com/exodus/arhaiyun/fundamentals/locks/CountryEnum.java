package com.exodus.arhaiyun.fundamentals.locks;


import lombok.Getter;

/**
 * @author arhaiyun
 * @version 1.0
 * @date 2020/4/17 10:48
 */
public enum CountryEnum {

    ONE(1, "齐"),
    TWO(2, "楚"),
    THREE(3, "燕"),
    FOUR(4, "赵"),
    FIVE(5, "魏"),
    SIX(6, "韩"),
    SEVEN(7, "秦");

    @Getter
    private Integer retCode;
    @Getter
    private String retMsg;

    CountryEnum(Integer retCode, String retMsg) {
        this.retCode = retCode;
        this.retMsg = retMsg;
    }

    public static CountryEnum getElement(int index) {
        CountryEnum[] values = CountryEnum.values();
        for (CountryEnum element : values) {
            if (index == element.getRetCode()) {
                return element;
            }
        }
        return null;
    }
}
