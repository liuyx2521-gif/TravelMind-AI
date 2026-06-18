package com.travelmind.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@TableName("attraction")
@EqualsAndHashCode(callSuper = true)
public class Attraction extends BaseEntity {
    private String name;
    private String province;
    private String city;
    private String description;
    private String coverImage;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal price;
    private String bestSeason;
    private String openTime;
    private BigDecimal score;
    private String tags;
}
