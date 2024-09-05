package com.muybien.youtube_clone.s3aws;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@RequiredArgsConstructor
public class S3Config {

    private final Region region = Region.of(System.getenv("AWS_DEFAULT_REGION"));

    @Bean
    public EnvironmentVariableCredentialsProvider credentialsProvider() {
        return EnvironmentVariableCredentialsProvider.create();
    }

    @Bean
    public S3Client s3Client(EnvironmentVariableCredentialsProvider credentialsProvider) {
        return S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }
}