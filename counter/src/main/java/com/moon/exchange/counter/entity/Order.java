package com.moon.exchange.counter.entity;

import javax.persistence.*;
import lombok.*;

/**
 * @author Chanmoey
 * @date 2023年01月20日
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "t_order")
public class Order {

    @Id
    private Integer id;

    private Long uid;

    private Integer code;

    private Integer direction;

    private Integer type;

    private Long price;

    /**
     * 原始委托的数量
     */
    private Long count;

    private Integer status;

    private String date;

    private String time;

    @Transient
    private String name;

    /**
     * 最终成交的数量
     */
    @Transient
    private Long tradeCount;
}
