package com.travelmind.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travelmind.common.PageResp;
import com.travelmind.common.Result;
import com.travelmind.mapper.TravelNoteCommentMapper;
import com.travelmind.mapper.TravelNoteMapper;
import com.travelmind.model.TravelNote;
import com.travelmind.model.TravelNoteComment;
import com.travelmind.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;
import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaUpdate;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {
    private final TravelNoteMapper mapper;
    private final TravelNoteCommentMapper commentMapper;

    @GetMapping
    public Result<PageResp<TravelNote>> page(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long size) {
        return Result.ok(PageResp.of(mapper.selectPage(Page.of(page, size),
                lambdaQuery(TravelNote.class).orderByDesc(TravelNote::getLikeCount, TravelNote::getCreateTime))));
    }

    @GetMapping("/{id}")
    public Result<TravelNote> detail(@PathVariable Long id) {
        mapper.update(null, lambdaUpdate(TravelNote.class).eq(TravelNote::getId, id)
                .setSql("view_count = view_count + 1"));
        return Result.ok(mapper.selectById(id));
    }

    @PostMapping
    public Result<TravelNote> create(@RequestBody TravelNote note) {
        note.setUserId(LoginUser.id());
        note.setViewCount(0);
        note.setLikeCount(0);
        if ((note.getCover() == null || note.getCover().isBlank()) && note.getImages() != null && !note.getImages().isBlank()) {
            var first = note.getImages().replace("[", "").replace("]", "").replace("\"", "").split(",", 2)[0].trim();
            if (!first.isBlank()) note.setCover(first);
        }
        mapper.insert(note);
        return Result.ok(note);
    }

    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        mapper.update(null, lambdaUpdate(TravelNote.class).eq(TravelNote::getId, id)
                .setSql("like_count = like_count + 1"));
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deleteByOwner(id);
        return Result.ok();
    }

    @PostMapping("/{id}/delete")
    public Result<Void> deleteByPost(@PathVariable Long id) {
        deleteByOwner(id);
        return Result.ok();
    }

    private void deleteByOwner(Long id) {
        var note = mapper.selectById(id);
        if (note == null) return;
        if (!note.getUserId().equals(LoginUser.id())) {
            throw new IllegalArgumentException("只能删除自己发布的游记");
        }
        commentMapper.delete(lambdaQuery(TravelNoteComment.class).eq(TravelNoteComment::getNoteId, id));
        mapper.deleteById(id);
    }

    @GetMapping("/{id}/comments")
    public Result<?> comments(@PathVariable Long id) {
        return Result.ok(commentMapper.selectList(lambdaQuery(TravelNoteComment.class)
                .eq(TravelNoteComment::getNoteId, id)
                .orderByDesc(TravelNoteComment::getCreateTime)));
    }

    @PostMapping("/{id}/comments")
    public Result<TravelNoteComment> comment(@PathVariable Long id, @RequestBody TravelNoteComment comment) {
        if (comment.getContent() == null || comment.getContent().isBlank()) {
            throw new IllegalArgumentException("评论不能为空");
        }
        comment.setNoteId(id);
        comment.setUserId(LoginUser.id());
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);
        return Result.ok(comment);
    }
}
