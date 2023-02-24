package com.za.roomfinder.service.datasource;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import io.github.cdimascio.dotenv.Dotenv;

import java.math.BigInteger;
import java.security.SecureRandom;

import java.net.URL;
import java.util.Date;

public class S3Bucket {

    private static final Dotenv dotenv = Dotenv.load();

    private final static String bucketName = dotenv.get("AWS_BUCKET_NAME");
    private final static String accessKey = dotenv.get("AWS_ACCESS_KEY");
    private final static String secretKey = dotenv.get("AWS_SECRET_ACCESS_KEY");
    private final static String region = dotenv.get("AWS_REGION_NAME");


    public static URL getSignedUrl(){

        String objectKey = generateHexKey();

        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .withRegion(region)
                .build();

        // Set the expiration time of the URL
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 60 * 1000; // 1 minute
        expiration.setTime(expTimeMillis);

        // Generate the signed URL
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, objectKey)
                        .withMethod(HttpMethod.PUT)
                        .withExpiration(expiration);

        return s3.generatePresignedUrl(generatePresignedUrlRequest);

    }

    private static String generateHexKey() {
        byte[] randomBytes = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(randomBytes);
        BigInteger number = new BigInteger(1, randomBytes);
        return String.format("%0" + (16 << 1) + "x", number);
    }


}
