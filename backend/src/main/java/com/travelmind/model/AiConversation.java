package com.travelmind.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("ai_conversation")
@EqualsAndHashCode(callSuper = true)
public class AiConversation extends BaseEntity {
    private Long userId;
    private String title;
}
