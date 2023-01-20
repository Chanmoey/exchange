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
@Table(name = "t_position")
public class Position {

    @Id
    private Integer id;

    private Long uid;

    private Long code;

    private Long cost;

    private Long count;

    @Transient
    private String name;
}
