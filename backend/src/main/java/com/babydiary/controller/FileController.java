package com.babydiary.controller;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @GetMapping("/baby-diary/**")
    public ResponseEntity<?> serveFile(HttpServletRequest request) {
        String path = request.getRequestURI();
        String objectName = path.substring(path.indexOf("/baby-diary/") + "/baby-diary/".length());
        if (objectName.isEmpty()) return ResponseEntity.notFound().build();
        try {
            var stat = minioClient.statObject(StatObjectArgs.builder().bucket(bucket).object(objectName).build());
            InputStream stream = minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(objectName).build());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(stat.contentType()))
                    .body(new InputStreamResource(stream));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
