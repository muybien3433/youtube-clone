package com.muybien.youtube_clone.s3aws;

import com.muybien.youtube_clone.handler.FileDeletionException;
import com.muybien.youtube_clone.handler.FileUploadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class S3ServiceTest {

    @Mock private S3Client s3Client;
    @Mock private S3Utilities s3Utilities;
    @Mock private MultipartFile file;
    @InjectMocks private S3Service s3Service;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        Field field = s3Service.getClass().getDeclaredField("bucketName");
        field.setAccessible(true);
        field.set(s3Service, "test-bucket");

        when(s3Client.utilities()).thenReturn(s3Utilities);
    }

    @Test
    public void testUploadFileAndFetchFileUrl() throws Exception {
        String originalFilename = "original-filename.txt";
        String contentType = "text/plain";
        String filenameExtension = ".txt";
        UUID mockUUID = UUID.randomUUID();
        String fileKey = mockUUID + filenameExtension;

        URI expectedUrl = new URI("https://example.com/" + "test-bucket" + "/" + fileKey);

        when(s3Client.utilities().getUrl(any(GetUrlRequest.class))).thenReturn(expectedUrl.toURL());
        when(file.getOriginalFilename()).thenReturn(originalFilename);
        when(file.getContentType()).thenReturn(contentType);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("text-content".getBytes()));
        when(file.getSize()).thenReturn((long) "test-content".getBytes().length);

        String fileUrl = s3Service.uploadFileAndFetchFileUrl(file);

        assertNotNull(fileUrl);
        assertTrue(fileUrl.contains("test-bucket"));
        assertTrue(fileUrl.contains(fileKey));
    }

    @Test
    public void testUploadFileAndFetchFileUrlWhenThrowsException() throws Exception {
        String originalFilename = "original-filename.txt";
        when(file.getOriginalFilename()).thenReturn(originalFilename);
        when(file.getInputStream()).thenThrow(new IOException("Mocked exception"));

        Exception e = assertThrows(FileUploadException.class, () -> s3Service.uploadFileAndFetchFileUrl(file));
        assertEquals("Failed to read file input stream", e.getMessage());
    }

    @Test
    public void testDeleteFileFromS3() {
        String fileUrl = "https://example.com/";

        s3Service.deleteFileFromS3(fileUrl);

        verify(s3Client).deleteObject(DeleteObjectRequest.builder()
                .bucket("test-bucket")
                .key(fileUrl)
                .build());
    }

    @Test
    public void testDeleteFileFromS3WhenThrowsException() {
        String fileUrl = "https://example.com/";

        doThrow(S3Exception.builder().message("S3 Error").build())
                .when(s3Client)
                .deleteObject(DeleteObjectRequest.builder()
                        .bucket("test-bucket")
                        .key(fileUrl)
                        .build());

        assertThrows(FileDeletionException.class, () -> s3Service.deleteFileFromS3(fileUrl));
    }
}
