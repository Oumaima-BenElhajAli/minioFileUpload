package com.example.miniofiles.binarystorage;

import com.example.miniofiles.config.MinioConfig;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minioClient;

    @Value("${minio.put-object-part-size}")
    private Long putObjectPartSize;

    public void save(MultipartFile file, UUID uuid, String path) throws Exception {
        minioClient.putObject(
                PutObjectArgs
                        .builder()
                        .bucket(MinioConfig.COMMON_BUCKET_NAME)
                        .object(path+uuid.toString())
                        .stream(file.getInputStream(), file.getSize(), putObjectPartSize)
                        .build()
        );
    }

    public InputStream getInputStream(UUID uuid, long offset, long length , String path) throws Exception {
        return minioClient.getObject(
                GetObjectArgs
                        .builder()
                        .bucket(MinioConfig.COMMON_BUCKET_NAME)
                        .offset(offset)
                        .length(length)
                        .object(path +uuid.toString())
                        .build());
    }
    public void deleteMediaFile(String objectName , String path) throws Exception {
        RemoveObjectArgs args = RemoveObjectArgs.builder()
                .bucket(MinioConfig.COMMON_BUCKET_NAME)
                .object(path+objectName)
                .build();
        minioClient.removeObject(args);
    }

}