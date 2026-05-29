package storage

import "strings"

// NormalizeOSSRegion 将 Consul 中的 oss_region 规范为 ali-oss / STS 所需格式，如 oss-ap-southeast-1。
// 常见误配：把「bucket.区域」写成 region，会导致 endpoint 出现重复 bucket 主机名。
func NormalizeOSSRegion(region, bucket string) string {
	region = strings.TrimSpace(region)
	bucket = strings.TrimSpace(bucket)
	if region == "" {
		return ""
	}

	region = strings.TrimPrefix(region, "https://")
	region = strings.TrimPrefix(region, "http://")
	if i := strings.Index(region, ".aliyuncs.com"); i > 0 {
		region = region[:i]
	}

	if bucket != "" {
		prefix := bucket + "."
		if strings.HasPrefix(region, prefix) {
			region = strings.TrimPrefix(region, prefix)
		}
	}

	if strings.HasPrefix(region, "oss-") {
		return region
	}
	// ap-southeast-1 → oss-ap-southeast-1
	if strings.Contains(region, "-") && !strings.Contains(region, ".") {
		return "oss-" + region
	}
	return region
}
