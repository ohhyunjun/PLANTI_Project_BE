package com.metaverse.planti_be.file.service;

import com.metaverse.planti_be.file.domain.File;
import com.metaverse.planti_be.file.repository.FileRepository;
import com.metaverse.planti_be.post.domain.Post;
import com.metaverse.planti_be.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Transactional
    public void uploadFile(Post post, MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 비어 있습니다.");
        }

        String originalFileName = multipartFile.getOriginalFilename();
        String storedFileName = UUID.randomUUID() + "_" + originalFileName;

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 디렉토리 생성에 실패했습니다: " + uploadPath.toString(), e);
        }

        Path filePath = uploadPath.resolve(storedFileName);

        try {
            multipartFile.transferTo(filePath);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다: " + filePath.toString(), e);
        }

        File fileEntity = new File(originalFileName, storedFileName, filePath.toString(), post);
        fileRepository.save(fileEntity);
    }
}
