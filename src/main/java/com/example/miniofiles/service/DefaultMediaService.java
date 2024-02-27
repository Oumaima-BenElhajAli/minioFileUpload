package com.example.miniofiles.service;

import com.example.miniofiles.binarystorage.MinioStorageService;
import com.example.miniofiles.entity.FileMetadataEntity;
import com.example.miniofiles.util.Range;
import com.example.miniofiles.exception.StorageException;
import com.example.miniofiles.repository.FileMetadataRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultMediaService implements MediaService {

    private final MinioStorageService storageService;

    private final FileMetadataRepository fileMetadataRepository;

    @Override
    @Transactional
    public UUID save(MultipartFile media) {
        try {
            UUID fileUuid = UUID.randomUUID();
            FileMetadataEntity metadata = FileMetadataEntity.builder()
                    .id(fileUuid.toString())
                    .size(media.getSize())
                    .httpContentType(media.getContentType())
                    .build();
            fileMetadataRepository.save(metadata);
            storageService.save(media, fileUuid);
            return fileUuid;
        } catch (Exception ex) {
            log.error("Exception occurred when trying to save the file", ex);
            throw new StorageException(ex);
        }
    }
    @Override
    public void deleteFileById(String id) throws Exception{
            storageService.deleteMediaFile(id);
            fileMetadataRepository.deleteById(id);
    }

    @Override
    public ChunkWithMetadata fetchChunk(UUID uuid, Range range) {
        FileMetadataEntity fileMetadata = fileMetadataRepository.findById(uuid.toString()).orElseThrow();
        return new ChunkWithMetadata(fileMetadata, readChunk(uuid, range, fileMetadata.getSize()));
    }

    private byte[] readChunk(UUID uuid, Range range, long fileSize) {
        long startPosition = range.getRangeStart();
        long endPosition = range.getRangeEnd(fileSize);
        int chunkSize = (int) (endPosition - startPosition + 1);
        try(InputStream inputStream = storageService.getInputStream(uuid, startPosition, chunkSize)) {
            return inputStream.readAllBytes();
        } catch (Exception exception) {
            log.error("Exception occurred when trying to read file with ID = {}", uuid);
            throw new StorageException(exception);
        }
    }

    public record ChunkWithMetadata(
            FileMetadataEntity metadata,
            byte[] chunk
    ) {}
}