package com.metaverse.planti_be.file.service;

import com.metaverse.planti_be.file.domain.File;
import com.metaverse.planti_be.file.repository.FileRepository;
import com.metaverse.planti_be.post.domain.Post;
import com.metaverse.planti_be.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final PostRepository postRepository;

    @Transactional
    public void uploadFile(Long postId, MultipartFile multipartFile) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 게시글(ID: " + postId + ")을 찾을 수 없습니다.")
                );

        String originalFileName = multipartFile.getOriginalFilename();
        String storedFileName = UUID.randomUUID() + "_" + originalFileName;
        String filePath = "uploads/" + storedFileName;

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(multipartFile.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }

        File file = new File(originalFileName, storedFileName, filePath, post);
        fileRepository.save(file);
    }
}
