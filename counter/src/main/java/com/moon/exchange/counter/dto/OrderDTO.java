package com.moon.exchange.counter.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Chanmoey
 * @date 2023年01月21日
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private int type;

    private long timestamp;

    private int code;

    private int direction;

    private long price;

    private long volume;

    private int orderType;
}
