package com.travelmind.controller;

import com.travelmind.common.PageResp;
import com.travelmind.common.Result;
import com.travelmind.dto.CommentReq;
import com.travelmind.dto.CommentView;
import com.travelmind.model.TravelNote;
import com.travelmind.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {
    private final NoteService noteService;

    @GetMapping
    public Result<PageResp<TravelNote>> page(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "20") long size,
                                             @RequestParam(required = false) String keyword) {
        return Result.ok(noteService.page(page, size, keyword));
    }

    @GetMapping("/mine")
    public Result<PageResp<TravelNote>> mine(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "20") long size) {
        return Result.ok(noteService.mine(page, size));
    }

    @GetMapping("/{id}")
    public Result<TravelNote> detail(@PathVariable Long id) {
        return Result.ok(noteService.detail(id));
    }

    @PostMapping
    public Result<TravelNote> create(@RequestBody TravelNote note) {
        return Result.ok(noteService.create(note));
    }

    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        noteService.like(id);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noteService.delete(id);
        return Result.ok();
    }

    @PostMapping("/{id}/delete")
    public Result<Void> deleteByPost(@PathVariable Long id) {
        noteService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}/comments")
    public Result<List<CommentView>> comments(@PathVariable Long id) {
        return Result.ok(noteService.comments(id));
    }

    @PostMapping("/{id}/comments")
    public Result<CommentView> comment(@PathVariable Long id, @RequestBody CommentReq req) {
        return Result.ok(noteService.createComment(id, req));
    }

    @PutMapping("/{noteId}/comments/{commentId}")
    public Result<CommentView> updateComment(@PathVariable Long noteId,
                                             @PathVariable Long commentId,
                                             @RequestBody CommentReq req) {
        return Result.ok(noteService.updateComment(noteId, commentId, req));
    }

    @DeleteMapping("/{noteId}/comments/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long noteId, @PathVariable Long commentId) {
        noteService.deleteComment(noteId, commentId);
        return Result.ok();
    }

    @PostMapping("/{noteId}/comments/{commentId}/delete")
    public Result<Void> deleteCommentByPost(@PathVariable Long noteId, @PathVariable Long commentId) {
        noteService.deleteComment(noteId, commentId);
        return Result.ok();
    }
}
