package com.metaverse.planti_be.file.controller;

import com.metaverse.planti_be.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FIleController {

    private final FileService fileService;

    @PostMapping("posts/{postId}/files")
    public ResponseEntity<String> uploadFile(
            @PathVariable Long postId,
            @RequestParam("file") MultipartFile file) {
        fileService.uploadFile(postId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body("파일 업로드 성공");
    }
}
