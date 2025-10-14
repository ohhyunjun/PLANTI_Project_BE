package com.metaverse.planti_be.file.dto;

import com.metaverse.planti_be.file.domain.File;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FileResponseDto {
    private Long id;
    private String originalFileName;
    private String storedFileName;

    // ✅ 프론트엔드에서 사용할 URL
    private String fileUrl;

    public FileResponseDto(File file){
        this.id = file.getId();
        this.originalFileName = file.getOriginalFileName();
        this.storedFileName = file.getStoredFileName();
        // ✅ filePath에 URL이 저장되어 있음
        this.fileUrl = file.getFilePath();
    }
}
