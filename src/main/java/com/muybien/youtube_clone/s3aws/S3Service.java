package com.muybien.youtube_clone.s3aws;

import com.muybien.youtube_clone.handler.FileDeletionException;
import com.muybien.youtube_clone.handler.FileUploadException;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Builder
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final String bucketName = System.getenv("AWS_BUCKET_NAME");

    public String uploadFileAndFetchFileUrl(MultipartFile file) {
        String filenameExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String fileKey = UUID.randomUUID() + filenameExtension;

        try {
            Map<String, String> metadata = new HashMap<>();
            metadata.put(file.getOriginalFilename(), file.getContentType());
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .metadata(metadata)
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            GetUrlRequest urlRequest = GetUrlRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            URL url = s3Client.utilities().getUrl(urlRequest);

            return url.toString();
        } catch (S3Exception e) {
            throw new FileUploadException("Failed to upload file " + file.getOriginalFilename(), e);
        } catch (IOException e) {
            throw new FileUploadException("Failed to read file input stream", e);
        }
    }

    public void deleteFileFromS3(String fileUrl) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileUrl)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception e) {
            throw new FileDeletionException("Failed to delete file " + fileUrl, e);
        }
    }
}