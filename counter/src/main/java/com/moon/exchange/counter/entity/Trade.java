package com.moon.exchange.counter.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

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
@Table(name = "t_trade")
public class Trade {

    @Id
    private Long id;

    private Long uid;

    private Integer code;

    private Integer direction;

    private Long price;

    private Long count;

    private int oid;

    private String date;

    private String time;

    @Transient
    private String name;
}
