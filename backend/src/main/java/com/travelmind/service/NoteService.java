package com.travelmind.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travelmind.common.PageResp;
import com.travelmind.dto.CommentReq;
import com.travelmind.dto.CommentView;
import com.travelmind.mapper.TravelNoteCommentMapper;
import com.travelmind.mapper.TravelNoteMapper;
import com.travelmind.mapper.UserMapper;
import com.travelmind.model.TravelNote;
import com.travelmind.model.TravelNoteComment;
import com.travelmind.model.User;
import com.travelmind.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;
import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaUpdate;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final TravelNoteMapper noteMapper;
    private final TravelNoteCommentMapper commentMapper;
    private final UserMapper userMapper;

    public PageResp<TravelNote> page(long page, long size, String keyword) {
        var query = lambdaQuery(TravelNote.class)
                .and(keyword != null && !keyword.isBlank(), q -> q
                        .like(TravelNote::getTitle, keyword)
                        .or()
                        .like(TravelNote::getContent, keyword))
                .orderByDesc(TravelNote::getCreateTime);
        return PageResp.of(noteMapper.selectPage(Page.of(page, size), query));
    }

    public PageResp<TravelNote> mine(long page, long size) {
        return PageResp.of(noteMapper.selectPage(Page.of(page, size),
                lambdaQuery(TravelNote.class)
                        .eq(TravelNote::getUserId, LoginUser.id())
                        .orderByDesc(TravelNote::getCreateTime)));
    }

    public TravelNote detail(Long id) {
        noteMapper.update(null, lambdaUpdate(TravelNote.class)
                .eq(TravelNote::getId, id)
                .setSql("view_count = view_count + 1"));
        return noteMapper.selectById(id);
    }

    public TravelNote create(TravelNote note) {
        note.setUserId(LoginUser.id());
        note.setViewCount(0);
        note.setLikeCount(0);
        if ((note.getCover() == null || note.getCover().isBlank()) && note.getImages() != null && !note.getImages().isBlank()) {
            var first = note.getImages().replace("[", "").replace("]", "").replace("\"", "").split(",", 2)[0].trim();
            if (!first.isBlank()) note.setCover(first);
        }
        noteMapper.insert(note);
        return note;
    }

    public void like(Long id) {
        noteMapper.update(null, lambdaUpdate(TravelNote.class)
                .eq(TravelNote::getId, id)
                .setSql("like_count = like_count + 1"));
    }

    @Transactional
    public void delete(Long id) {
        var note = noteMapper.selectById(id);
        if (note == null) return;
        if (!Objects.equals(note.getUserId(), LoginUser.id())) {
            throw new IllegalArgumentException("只能删除自己发布的游记");
        }
        commentMapper.delete(lambdaQuery(TravelNoteComment.class).eq(TravelNoteComment::getNoteId, id));
        noteMapper.deleteById(id);
    }

    public java.util.List<CommentView> comments(Long noteId) {
        var comments = commentMapper.selectList(lambdaQuery(TravelNoteComment.class)
                .eq(TravelNoteComment::getNoteId, noteId)
                .orderByDesc(TravelNoteComment::getCreateTime));
        var parentIds = comments.stream().map(TravelNoteComment::getParentId).filter(Objects::nonNull).distinct().toList();
        var parents = parentIds.isEmpty()
                ? Map.<Long, TravelNoteComment>of()
                : commentMapper.selectBatchIds(parentIds).stream().collect(Collectors.toMap(TravelNoteComment::getId, Function.identity()));
        var userIds = comments.stream()
                .flatMap(comment -> java.util.stream.Stream.of(comment.getUserId(), parents.get(comment.getParentId()) == null ? null : parents.get(comment.getParentId()).getUserId()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, User> users = userIds.isEmpty()
                ? Map.of()
                : userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, Function.identity()));
        return comments.stream().map(comment -> toView(comment, users.get(comment.getUserId()), parents.get(comment.getParentId()), users)).toList();
    }

    public CommentView createComment(Long noteId, CommentReq req) {
        var content = cleanContent(req);
        var user = LoginUser.get();
        var comment = new TravelNoteComment();

        // Core binding: user identity comes from JWT only, never from request body.
        comment.setNoteId(noteId);
        comment.setUserId(user.getId());
        comment.setParentId(parentId(noteId, req));
        comment.setUsername(user.getUsername());
        comment.setAvatar(user.getAvatar());
        comment.setContent(content);
        comment.setCreateTime(LocalDateTime.now());

        commentMapper.insert(comment);
        return toView(comment, user, commentMapper.selectById(comment.getParentId()), latestUserMap(comment.getParentId()));
    }

    public CommentView updateComment(Long noteId, Long commentId, CommentReq req) {
        var comment = ownedComment(noteId, commentId);
        comment.setContent(cleanContent(req));
        commentMapper.updateById(comment);
        return toView(comment, userMapper.selectById(comment.getUserId()), commentMapper.selectById(comment.getParentId()), latestUserMap(comment.getParentId()));
    }

    public void deleteComment(Long noteId, Long commentId) {
        ownedComment(noteId, commentId);
        commentMapper.deleteById(commentId);
    }

    private TravelNoteComment ownedComment(Long noteId, Long commentId) {
        var comment = commentMapper.selectById(commentId);
        if (comment == null || !Objects.equals(noteId, comment.getNoteId())) {
            throw new IllegalArgumentException("评论不存在");
        }
        if (!Objects.equals(LoginUser.id(), comment.getUserId())) {
            throw new IllegalArgumentException("只能操作自己的评论");
        }
        return comment;
    }

    private String cleanContent(CommentReq req) {
        if (req == null || req.content() == null || req.content().isBlank()) {
            throw new IllegalArgumentException("评论不能为空");
        }
        return req.content().trim();
    }

    private Long parentId(Long noteId, CommentReq req) {
        if (req == null || req.parentId() == null) return null;
        var parent = commentMapper.selectById(req.parentId());
        if (parent == null || !Objects.equals(parent.getNoteId(), noteId)) {
            throw new IllegalArgumentException("回复的评论不存在");
        }
        return parent.getId();
    }

    private Map<Long, User> latestUserMap(Long parentId) {
        if (parentId == null) return Map.of();
        var parent = commentMapper.selectById(parentId);
        if (parent == null) return Map.of();
        var parentUser = userMapper.selectById(parent.getUserId());
        return parentUser == null ? Map.of() : Map.of(parentUser.getId(), parentUser);
    }

    private CommentView toView(TravelNoteComment comment, User user, TravelNoteComment parent, Map<Long, User> users) {
        var username = user == null || user.getUsername() == null || user.getUsername().isBlank()
                ? fallback(comment.getUsername(), "ID " + comment.getUserId())
                : user.getUsername();
        var avatar = user == null || user.getAvatar() == null || user.getAvatar().isBlank()
                ? fallback(comment.getAvatar(), "")
                : user.getAvatar();
        var replyUser = parent == null ? null : users.get(parent.getUserId());
        var replyUsername = parent == null ? "" : (
                replyUser == null || replyUser.getUsername() == null || replyUser.getUsername().isBlank()
                        ? fallback(parent.getUsername(), "ID " + parent.getUserId())
                        : replyUser.getUsername()
        );
        var replyAvatar = parent == null ? "" : (
                replyUser == null || replyUser.getAvatar() == null || replyUser.getAvatar().isBlank()
                        ? fallback(parent.getAvatar(), "")
                        : replyUser.getAvatar()
        );
        return new CommentView(
                comment.getId(),
                comment.getNoteId(),
                comment.getUserId(),
                comment.getParentId(),
                parent == null ? null : parent.getUserId(),
                replyUsername,
                replyAvatar,
                username,
                avatar,
                comment.getContent(),
                comment.getCreateTime()
        );
    }

    private String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
