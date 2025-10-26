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
import java.util.Base64;
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

        Path uploadPath = Paths.get(postsUploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 디렉토리 생성 실패: " + uploadPath, e);
        }

        Path filePath = uploadPath.resolve(storedFileName);

        try {
            multipartFile.transferTo(filePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + filePath, e);
        }

        String fileUrl = baseUrl + "/api/uploads/posts/" + storedFileName;

        File fileEntity = new File(originalFileName, storedFileName, fileUrl, post);
        fileRepository.save(fileEntity);

        System.out.println("📁 파일 업로드 완료:");
        System.out.println("   - 원본: " + originalFileName);
        System.out.println("   - 저장: " + storedFileName);
        System.out.println("   - 경로: " + filePath);
        System.out.println("   - URL: " + fileUrl);
    }

    @Transactional
    public void deleteFilesByPost(Post post) {
        if (post.getFiles() == null || post.getFiles().isEmpty()) {
            return;
        }

        post.getFiles().forEach(file -> {
            try {
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

        fileRepository.deleteAll(post.getFiles());
        post.getFiles().clear();

        System.out.println("게시글의 모든 파일 삭제 완료");
    }

    public String uploadAiArtImage(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String originalFileName = multipartFile.getOriginalFilename();
        String storedFileName = UUID.randomUUID() + "_" + originalFileName;

        Path uploadPath = Paths.get(aiArtsUploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("AI 아트 이미지 저장 디렉토리 생성 실패: " + uploadPath, e);
        }

        Path filePath = uploadPath.resolve(storedFileName);

        try {
            multipartFile.transferTo(filePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("AI 아트 이미지 저장 실패: " + filePath, e);
        }

        String fileUrl = baseUrl + "/api/uploads/ai-arts/" + storedFileName;

        System.out.println("🎨 AI 아트 이미지 업로드 완료:");
        System.out.println("   - 원본: " + originalFileName);
        System.out.println("   - 저장: " + storedFileName);
        System.out.println("   - 경로: " + filePath);
        System.out.println("   - URL: " + fileUrl);

        return fileUrl;
    }

    /**
     * 로컬 이미지를 Base64 Data URL로 변환
     * OpenAI Vision API에서 직접 사용 가능한 형식
     */
    public String getAiArtImageAsBase64(String storedFileName) {
        try {
            Path uploadPath = Paths.get(aiArtsUploadDir).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(storedFileName);

            if (!Files.exists(filePath)) {
                throw new IllegalArgumentException("파일을 찾을 수 없습니다: " + storedFileName);
            }

            byte[] fileBytes = Files.readAllBytes(filePath);
            String base64Image = Base64.getEncoder().encodeToString(fileBytes);

            String mimeType = "image/jpeg";
            String lowerFileName = storedFileName.toLowerCase();
            if (lowerFileName.endsWith(".png")) {
                mimeType = "image/png";
            } else if (lowerFileName.endsWith(".gif")) {
                mimeType = "image/gif";
            } else if (lowerFileName.endsWith(".webp")) {
                mimeType = "image/webp";
            }

            String dataUrl = "data:" + mimeType + ";base64," + base64Image;

            System.out.println("✅ Base64 변환 완료 - 파일: " + storedFileName);
            System.out.println("   - MIME 타입: " + mimeType);
            System.out.println("   - Base64 길이: " + base64Image.length() + " chars");

            return dataUrl;

        } catch (IOException e) {
            throw new RuntimeException("이미지를 Base64로 인코딩하는데 실패했습니다: " + storedFileName, e);
        }
    }
}