package com.metaverse.planti_be.file.service;

import com.metaverse.planti_be.file.domain.File;
import com.metaverse.planti_be.file.repository.FileRepository;
import com.metaverse.planti_be.post.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;

    @Value("${file.upload-dir.posts}")
    private String postsUploadDir;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public void uploadFile(Post post, MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return;
        }

        String originalFileName = multipartFile.getOriginalFilename();
        String storedFileName = UUID.randomUUID() + "_" + originalFileName;

        // ✅ 참고 코드 방식: 절대 경로 생성 및 정규화
        Path uploadPath = Paths.get(postsUploadDir).toAbsolutePath().normalize();

        // ✅ 디렉토리 생성
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 디렉토리 생성 실패: " + uploadPath, e);
        }

        // ✅ 파일 저장
        Path filePath = uploadPath.resolve(storedFileName);

        try {
            multipartFile.transferTo(filePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + filePath, e);
        }

        // ✅ 웹 접근 URL 생성
        String fileUrl = baseUrl + "/api/uploads/posts/" + storedFileName;

        // ✅ DB 저장
        File fileEntity = new File(originalFileName, storedFileName, fileUrl, post);
        fileRepository.save(fileEntity);

        System.out.println("📝 파일 업로드 완료:");
        System.out.println("   - 원본: " + originalFileName);
        System.out.println("   - 저장: " + storedFileName);
        System.out.println("   - 경로: " + filePath);
        System.out.println("   - URL: " + fileUrl);
    }
}