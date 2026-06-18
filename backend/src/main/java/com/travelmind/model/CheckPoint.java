package com.travelmind.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("check_point")
@EqualsAndHashCode(callSuper = true)
public class CheckPoint extends BaseEntity {
    private Long attractionId;
    private String name;
    private String description;
    private String photo;
    private String location;
    private String bestTime;
}
