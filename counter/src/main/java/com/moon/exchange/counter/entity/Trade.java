package com.moon.exchange.counter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
@Table(name = "t_trade")
public class Trade {

    @Id
    private Integer id;

    private Long uid;

    private Long code;

    private Integer direction;

    private Long price;

    private Long count;

    private int oid;

    private String date;

    private String time;

    @Transient
    private String name;
}
