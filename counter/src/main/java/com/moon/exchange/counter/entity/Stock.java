package com.moon.exchange.counter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "t_stock")
public class Stock {

    @Id
    private Long code;

    private String name;

    private String abbrName;

    private Integer status;
}
