package com.travelmind.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@TableName("travel_plan")
@EqualsAndHashCode(callSuper = true)
public class TravelPlan extends BaseEntity {
    private Long userId;
    private String title;
    private BigDecimal budget;
    private Integer days;
    private String destination;
    private String season;
    private String content;
}
