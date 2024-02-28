package com.example.miniofiles.controller;

import com.example.miniofiles.controller.constants.MediaPaths;
import com.example.miniofiles.service.DefaultMediaService;
import com.example.miniofiles.util.Range;
import com.example.miniofiles.service.MediaService;
import com.example.miniofiles.controller.constants.HttpConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/media")
public class MediaController {

    private final MediaService mediaService;

    @Value("${app.streaming.default-chunk-size}")
    public Integer defaultChunkSize;

    @PostMapping
    public ResponseEntity<UUID> save(@RequestParam("file") MultipartFile file) {
        UUID fileUuid = mediaService.save(file, MediaPaths.USERS);
        return ResponseEntity.ok(fileUuid);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<byte[]> readChunk(
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String range,
            @PathVariable UUID uuid
    ) {
        Range parsedRange = Range.parseHttpRangeString(range, defaultChunkSize);
        DefaultMediaService.ChunkWithMetadata chunkWithMetadata = mediaService.fetchChunk(uuid, parsedRange, MediaPaths.USERS);
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .header(HttpHeaders.CONTENT_TYPE, chunkWithMetadata.metadata().getHttpContentType())
                .header(HttpHeaders.ACCEPT_RANGES, HttpConstants.ACCEPTS_RANGES_VALUE)
                .header(HttpHeaders.CONTENT_LENGTH, calculateContentLengthHeader(parsedRange, chunkWithMetadata.metadata().getSize()))
                .header(HttpHeaders.CONTENT_RANGE, constructContentRangeHeader(parsedRange, chunkWithMetadata.metadata().getSize()))
                .body(chunkWithMetadata.chunk());
    }
    @DeleteMapping("/{objectName}")
    public ResponseEntity<String> deleteObject(@PathVariable String objectName) {
        try {
            mediaService.deleteFileById(objectName, MediaPaths.USERS);
            return ResponseEntity.noContent().build(); // Success response
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting object: " + e.getMessage()); // Error response
        }
    }

    private String calculateContentLengthHeader(Range range, long fileSize) {
        return String.valueOf(range.getRangeEnd(fileSize) - range.getRangeStart() + 1);
    }

    private String constructContentRangeHeader(Range range, long fileSize) {
        return  "bytes " + range.getRangeStart() + "-" + range.getRangeEnd(fileSize) + "/" + fileSize;
    }
}