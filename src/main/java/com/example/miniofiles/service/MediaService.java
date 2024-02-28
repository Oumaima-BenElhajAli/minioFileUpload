package com.example.miniofiles.service;

import com.example.miniofiles.util.Range;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface MediaService {

    UUID save(MultipartFile media, String path);

    DefaultMediaService.ChunkWithMetadata fetchChunk(UUID uuid, Range range, String path);
    void deleteFileById(String id, String path) throws Exception;
}