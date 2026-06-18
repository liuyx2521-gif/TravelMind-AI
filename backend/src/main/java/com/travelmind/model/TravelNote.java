package com.travelmind.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("travel_note")
@EqualsAndHashCode(callSuper = true)
public class TravelNote extends BaseEntity {
    private Long userId;
    private String title;
    private String content;
    private String cover;
    private String images;
    private Integer viewCount;
    private Integer likeCount;
}
