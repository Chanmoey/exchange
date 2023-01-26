package com.moon.exchange.counter.dto;

import lombok.*;

/**
 * @author Chanmoey
 * @date 2023年01月20日
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderDTO {

    private int orderId;

    private int code;
}
