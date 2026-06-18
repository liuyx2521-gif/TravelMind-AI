package com.travelmind.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@TableName("hotel")
@EqualsAndHashCode(callSuper = true)
public class Hotel extends BaseEntity {
    private String name;
    private String city;
    private String address;
    private BigDecimal price;
    private BigDecimal score;
    private String cover;
    private BigDecimal longitude;
    private BigDecimal latitude;
}
