package com.travelmind.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("travel_note_comment")
public class TravelNoteComment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long noteId;
    private Long userId;
    private String content;
    private LocalDateTime createTime;
}
