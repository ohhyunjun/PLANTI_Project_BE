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
    private String filePath;

    public FileResponseDto(File file){
        this.id = file.getId();
        this.originalFileName = file.getOriginalFileName();
        this.storedFileName = file.getStoredFileName();
        this.filePath = file.getFilePath();
    }
}
