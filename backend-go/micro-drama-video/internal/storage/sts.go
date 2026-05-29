// Package storage — STS 临时凭证（阿里云 RAM AssumeRole）。
package storage

import (
	"encoding/json"
	"fmt"
	"strings"
	"time"

	"github.com/aliyun/alibaba-cloud-sdk-go/sdk/requests"
	"github.com/aliyun/alibaba-cloud-sdk-go/services/sts"

	"micro-drama-video/internal/config"
)

// STSCredentials 浏览器直传 OSS 所需的临时凭证。
type STSCredentials struct {
	AccessKeyID     string
	AccessKeySecret string
	SecurityToken   string
	Expiration      time.Time
}

// AssumeRoleForObject 为指定 objectKey 签发仅允许 PutObject 的 STS 临时凭证。
func AssumeRoleForObject(cfg *config.Config, objectKey string) (*STSCredentials, error) {
	roleARN := strings.TrimSpace(cfg.OSS.STSRoleARN)
	if roleARN == "" {
		return nil, fmt.Errorf("oss_sts_role_arn is not configured in Consul KV")
	}
	region := strings.TrimSpace(cfg.OSS.STSRegion)
	if region == "" {
		region = strings.TrimSpace(cfg.OSS.Region)
	}
	if region == "" {
		return nil, fmt.Errorf("oss_region or oss_sts_region is required for STS")
	}

	bucket := strings.TrimSpace(cfg.OSS.Bucket)
	objectKey = strings.TrimPrefix(strings.TrimSpace(objectKey), "/")
	resource := fmt.Sprintf("acs:oss:*:*:%s/%s", bucket, objectKey)

	policy := map[string]any{
		"Version": "1",
		"Statement": []map[string]any{
			{
				"Effect": "Allow",
				"Action": []string{
					"oss:PutObject",
					"oss:AbortMultipartUpload",
					"oss:ListParts",
					"oss:InitiateMultipartUpload",
					"oss:UploadPart",
					"oss:CompleteMultipartUpload",
				},
				"Resource": []string{resource},
			},
		},
	}
	policyJSON, err := json.Marshal(policy)
	if err != nil {
		return nil, err
	}

	client, err := sts.NewClientWithAccessKey(region, cfg.OSS.AccessKey, cfg.OSS.SecretKey)
	if err != nil {
		return nil, fmt.Errorf("sts client: %w", err)
	}

	req := sts.CreateAssumeRoleRequest()
	req.Scheme = "https"
	req.RoleArn = roleARN
	req.RoleSessionName = strings.TrimSpace(cfg.OSS.STSSessionName)
	if req.RoleSessionName == "" {
		req.RoleSessionName = "micro-drama-video-upload"
	}
	req.DurationSeconds = requests.NewInteger(cfg.OSS.STSDurationSeconds)
	req.Policy = string(policyJSON)

	resp, err := client.AssumeRole(req)
	if err != nil {
		return nil, fmt.Errorf("assume role: %w", err)
	}
	cred := resp.Credentials
	exp, err := time.Parse(time.RFC3339, cred.Expiration)
	if err != nil {
		exp = time.Now().Add(time.Duration(cfg.OSS.STSDurationSeconds) * time.Second)
	}
	return &STSCredentials{
		AccessKeyID:     cred.AccessKeyId,
		AccessKeySecret: cred.AccessKeySecret,
		SecurityToken:   cred.SecurityToken,
		Expiration:      exp,
	}, nil
}


func BuildBucketEndpoint(cfg *config.Config) string {
	bucket := strings.TrimSpace(cfg.OSS.Bucket)
	region := NormalizeOSSRegion(cfg.OSS.Region, bucket)
	if region == "" {
		region = NormalizeOSSRegion(cfg.OSS.STSRegion, bucket)
	}
	if region != "" {
		return fmt.Sprintf("https://%s.%s.aliyuncs.com", bucket, region)
	}
	ep := strings.TrimSpace(cfg.OSS.Endpoint)
	ep = strings.TrimPrefix(ep, "https://")
	ep = strings.TrimPrefix(ep, "http://")
	if ep != "" && strings.Contains(ep, ".") {
		return fmt.Sprintf("https://%s.%s", bucket, ep)
	}
	return fmt.Sprintf("https://%s", bucket)
}
