package com.moon.exchange.counter.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
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
