package com.example.miniofiles.service;

import com.example.miniofiles.util.Range;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface MediaService {

    UUID save(MultipartFile media);

    DefaultMediaService.ChunkWithMetadata fetchChunk(UUID uuid, Range range);
}