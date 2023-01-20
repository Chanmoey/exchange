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
@Table(name = "t_order")
public class Transfer {

    @Id
    private Integer id;

    private Long uid;

    private String date;

    private String time;

    private String bank;

    private Integer type;

    private Integer moneyType;

    private Long money;

    @Transient
    private String name;
}
