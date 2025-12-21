package com.system.brands.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.public-url:}")
    private String publicUrlBase;

    /**
     * Uploads a file to S3 and returns the S3 key (path)
     * 
     * @param file   The file to upload
     * @param folder The folder path in S3 (e.g., "brands" or "products")
     * @return The S3 key (path) of the uploaded file
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String filename = UUID.randomUUID().toString() + extension;
            String s3Key = folder + "/" + filename;

            // Determine content type
            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // Create PutObjectRequest
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .build();

            // Upload file
            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("File uploaded to S3: bucket={}, key={}, size={} bytes",
                    bucketName, s3Key, file.getSize());

            return s3Key;

        } catch (S3Exception e) {
            log.error("Error uploading file to S3: {}", e.getMessage(), e);
            throw new IOException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a file from S3
     * 
     * @param s3Key The S3 key (path) of the file to delete
     */
    public void deleteFile(String s3Key) {
        if (s3Key == null || s3Key.isEmpty()) {
            return;
        }

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

            log.info("File deleted from S3: bucket={}, key={}", bucketName, s3Key);

        } catch (S3Exception e) {
            log.error("Error deleting file from S3: {}", e.getMessage(), e);
            // Don't throw exception - file might not exist
        }
    }

    /**
     * Gets the public URL for an S3 object
     * 
     * @param s3Key The S3 key (path) of the file
     * @return The public URL
     */
    public String getFileUrl(String s3Key) {
        if (s3Key == null || s3Key.isEmpty()) {
            return null;
        }

        try {
            // If public URL base is configured, use it
            if (publicUrlBase != null && !publicUrlBase.isEmpty()) {
                return publicUrlBase + "/" + s3Key;
            }

            // Otherwise, generate URL
            GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            URL url = s3Client.utilities().getUrl(getUrlRequest);
            return url.toString();

        } catch (S3Exception e) {
            log.error("Error getting file URL from S3: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Checks if a file exists in S3
     * 
     * @param s3Key The S3 key (path) of the file
     * @return true if file exists
     */
    public boolean fileExists(String s3Key) {
        if (s3Key == null || s3Key.isEmpty()) {
            return false;
        }

        try {
            s3Client.headObject(builder -> builder
                    .bucket(bucketName)
                    .key(s3Key)
                    .build());
            return true;
        } catch (S3Exception e) {
            return false;
        }
    }
}
