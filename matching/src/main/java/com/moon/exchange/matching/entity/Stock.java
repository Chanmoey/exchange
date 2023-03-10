package com.moon.exchange.matching.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
    private Integer code;

    private String name;

    private String abbrName;

    private Integer status;

    @Override
    public int hashCode() {
        int hsCode = code;
        hsCode = 31 * hsCode + name.hashCode();
        hsCode = 31 * hsCode + abbrName.hashCode();
        return hsCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Stock other = (Stock) o;
        if (!code.equals(other.getCode())) {
            return false;
        }
        if (!name.equals(other.getName())) {
            return false;
        }
        return abbrName.equals(other.getAbbrName());
    }
}
