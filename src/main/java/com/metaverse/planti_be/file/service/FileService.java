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

    @Value("${file.upload-dir.ai-arts:uploads/ai-arts}")
    private String aiArtsUploadDir;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public void uploadFile(Post post, MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return;
        }

        String originalFileName = multipartFile.getOriginalFilename();
        String storedFileName = UUID.randomUUID() + "_" + originalFileName;

        // 절대 경로 생성 및 정규화
        Path uploadPath = Paths.get(postsUploadDir).toAbsolutePath().normalize();

        // 디렉토리 생성
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 디렉토리 생성 실패: " + uploadPath, e);
        }

        // 파일 저장
        Path filePath = uploadPath.resolve(storedFileName);

        try {
            multipartFile.transferTo(filePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + filePath, e);
        }

        // 웹 접근 URL 생성
        String fileUrl = baseUrl + "/api/uploads/posts/" + storedFileName;

        // DB 저장
        File fileEntity = new File(originalFileName, storedFileName, fileUrl, post);
        fileRepository.save(fileEntity);

        System.out.println("📝 파일 업로드 완료:");
        System.out.println("   - 원본: " + originalFileName);
        System.out.println("   - 저장: " + storedFileName);
        System.out.println("   - 경로: " + filePath);
        System.out.println("   - URL: " + fileUrl);
    }

    // 게시글의 모든 파일 삭제
    @Transactional
    public void deleteFilesByPost(Post post) {
        if (post.getFiles() == null || post.getFiles().isEmpty()) {
            return;
        }

        // DB에서 파일 정보 조회 및 물리적 파일 삭제
        post.getFiles().forEach(file -> {
            try {
                // 물리적 파일 삭제
                Path uploadPath = Paths.get(postsUploadDir).toAbsolutePath().normalize();
                Path filePath = uploadPath.resolve(file.getStoredFileName());

                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    System.out.println("파일 삭제 완료: " + file.getStoredFileName());
                } else {
                    System.out.println("파일이 존재하지 않음: " + filePath);
                }
            } catch (IOException e) {
                System.err.println("파일 삭제 실패: " + file.getStoredFileName());
                e.printStackTrace();
            }
        });

        // DB에서 파일 정보 삭제
        fileRepository.deleteAll(post.getFiles());
        post.getFiles().clear();

        System.out.println("게시글의 모든 파일 삭제 완료");
    }

    //AI 아트용 이미지 업로드 (Post와 연결되지 않음)
    public String uploadAiArtImage(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String originalFileName = multipartFile.getOriginalFilename();
        String storedFileName = UUID.randomUUID() + "_" + originalFileName;

        // 절대 경로 생성 및 정규화
        Path uploadPath = Paths.get(aiArtsUploadDir).toAbsolutePath().normalize();

        // 디렉토리 생성
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("AI 아트 이미지 저장 디렉토리 생성 실패: " + uploadPath, e);
        }

        // 파일 저장
        Path filePath = uploadPath.resolve(storedFileName);

        try {
            multipartFile.transferTo(filePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("AI 아트 이미지 저장 실패: " + filePath, e);
        }

        // 웹 접근 URL 생성
        String fileUrl = baseUrl + "/api/uploads/ai-arts/" + storedFileName;

        System.out.println("🎨 AI 아트 이미지 업로드 완료:");
        System.out.println("   - 원본: " + originalFileName);
        System.out.println("   - 저장: " + storedFileName);
        System.out.println("   - 경로: " + filePath);
        System.out.println("   - URL: " + fileUrl);

        return fileUrl;
    }
}