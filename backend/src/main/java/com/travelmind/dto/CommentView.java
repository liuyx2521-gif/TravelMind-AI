package com.travelmind.dto;

import java.time.LocalDateTime;

public record CommentView(
        Long id,
        Long noteId,
        Long userId,
        Long parentId,
        Long replyToUserId,
        String replyToUsername,
        String replyToAvatar,
        String username,
        String avatar,
        String content,
        LocalDateTime createTime
) {
}
