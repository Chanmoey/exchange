package com.moon.exchange.counter.entity;

import jakarta.persistence.*;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Long uid;

    private Integer code;

    private Long cost;

    private Long count;

    @Transient
    private String name;
}
