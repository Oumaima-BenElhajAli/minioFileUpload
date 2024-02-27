package com.example.miniofiles.repository;


import com.example.miniofiles.entity.FileMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadataEntity, String> {
}
