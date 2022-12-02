package com.charlee.sns.data;

/**
 * Amazon S3存储所需的配置信息
 */
public class S3Config {
    private final String awsAccessKeyId;
    private final String awsSecretAccessKey;
    private final String bucket;
    private final String baseurl;
    private final String endpoint;

    public S3Config(String awsAccessKeyId, String awsSecretAccessKey, String bucket, String baseurl, String endpoint) {
        this.awsAccessKeyId = awsAccessKeyId;
        this.awsSecretAccessKey = awsSecretAccessKey;
        this.bucket = bucket;
        this.baseurl = baseurl;
        this.endpoint = endpoint;
    }

    public String getAwsAccessKeyId() {
        return awsAccessKeyId;
    }

    public String getAwsSecretAccessKey() {
        return awsSecretAccessKey;
    }

    public String getBucket() {
        return bucket;
    }

    public String getBaseurl() {
        return baseurl;
    }

    public String getEndpoint() {
        return endpoint;
    }

}
