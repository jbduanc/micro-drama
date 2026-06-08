// Package consul 封装与 HashiCorp Consul 的交互。
//
// 两大能力（与 Java spring-cloud-starter-consul 对齐）：
//  1. 配置中心：启动时从 KV 读取 YAML，合并到 Viper（MergeRemoteConfig）
//  2. 服务发现：将本 HTTP 服务注册到 Consul Agent（RegisterService）
//
// KV 路径约定：{prefix}/{configName}/{dataKey}
// 默认：config/micro-drama-video/data
package consul

import (
	"fmt"
	"net"
	"os"
	"strconv"
	"strings"

	"github.com/hashicorp/consul/api"
	"github.com/spf13/viper"
	"go.uber.org/zap"
	"gopkg.in/yaml.v3"
)

// MergeRemoteConfig 从 Consul KV 读取 YAML 配置并合并到 Viper。
//
// 当 consul_enabled=false 时直接返回，仅用本地默认值和环境变量。
// 当 KV 不存在且 consul_config_fail_fast=true 时返回错误，阻止启动。
func MergeRemoteConfig(v *viper.Viper, log *zap.Logger) error {
	if !v.GetBool("consul_enabled") {
		log.Info("consul config disabled")
		return nil
	}

	// 优先读与 Java 一致的环境变量，便于同一套 K8s/Compose 配置。
	host := firstNonEmpty(
		os.Getenv("CONSUL_HOST"),
		os.Getenv("SPRING_CLOUD_CONSUL_HOST"),
		v.GetString("consul_host"),
		"consul",
	)
	port := firstNonEmpty(
		os.Getenv("CONSUL_PORT"),
		os.Getenv("SPRING_CLOUD_CONSUL_PORT"),
		v.GetString("consul_port"),
		"8500",
	)
	token := firstNonEmpty(
		os.Getenv("CONSUL_TOKEN"),
		os.Getenv("SPRING_CLOUD_CONSUL_TOKEN"),
		v.GetString("consul_token"),
	)

	// 创建 Consul HTTP API 客户端。
	cfg := api.DefaultConfig()
	cfg.Address = net.JoinHostPort(host, port)
	if token != "" {
		cfg.Token = token // 开启 ACL 时必填
	}

	client, err := api.NewClient(cfg)
	if err != nil {
		return fmt.Errorf("consul client: %w", err)
	}

	// 拼接 KV 键，例如 config/micro-drama-video/data。
	prefix := strings.Trim(v.GetString("consul_config_prefix"), "/")
	name := v.GetString("consul_config_name")
	dataKey := v.GetString("consul_config_data_key")
	if dataKey == "" {
		dataKey = "data"
	}
	kvKey := fmt.Sprintf("%s/%s/%s", prefix, name, dataKey)

	// 从 Consul KV 存储读取配置内容。
	pair, _, err := client.KV().Get(kvKey, nil)
	if err != nil {
		return fmt.Errorf("consul kv get %s: %w", kvKey, err)
	}
	if pair == nil || len(pair.Value) == 0 {
		if v.GetBool("consul_config_fail_fast") {
			return fmt.Errorf("consul kv %s is empty", kvKey)
		}
		log.Warn("consul kv empty, using env/defaults only", zap.String("key", kvKey))
		return nil
	}

	// 将 YAML 解析为嵌套 map，再扁平化写入 Viper（oss.endpoint → oss_endpoint 键风格由 Viper 扁平键表示）。
	var nested map[string]any
	if err := yaml.Unmarshal(pair.Value, &nested); err != nil {
		return fmt.Errorf("parse consul yaml: %w", err)
	}
	flattenIntoViper("", nested, v)
	log.Info("consul config loaded", zap.String("key", kvKey))
	return nil
}

// flattenIntoViper 递归将嵌套 YAML map 扁平化为 Viper 的点分键。
//
// 例如 YAML：kafka: { brokers: "a:9092" } → Viper 键 kafka.brokers。
// 与 unmarshal 中 GetString("kafka_brokers") 的命名需与 Consul YAML 顶层键一致（kafka_brokers 或嵌套 kafka.brokers）。
func flattenIntoViper(prefix string, m map[string]any, v *viper.Viper) {
	for k, val := range m {
		key := k
		if prefix != "" {
			key = prefix + "." + k
		}
		switch t := val.(type) {
		case map[string]any:
			flattenIntoViper(key, t, v)
		default:
			v.Set(key, val)
		}
	}
}

// RegisterService 向 Consul Agent 注册当前 HTTP 服务实例。
//
// 参数：
//   - v：Viper，读取 consul_discovery_enabled、consul_service_name 等
//   - listenAddr：本服务监听地址，如 ":8080"
//   - healthPath：健康检查路径，如 "/healthz"
//
// 返回 deregister 函数，进程退出时调用以注销服务，避免僵尸实例。
func RegisterService(v *viper.Viper, log *zap.Logger, listenAddr, healthPath string) (func(), error) {
	if !v.GetBool("consul_discovery_enabled") {
		// 返回空函数，defer 时无操作。
		return func() {}, nil
	}

	host := firstNonEmpty(os.Getenv("CONSUL_HOST"), os.Getenv("SPRING_CLOUD_CONSUL_HOST"), v.GetString("consul_host"), "consul")
	port := firstNonEmpty(os.Getenv("CONSUL_PORT"), os.Getenv("SPRING_CLOUD_CONSUL_PORT"), v.GetString("consul_port"), "8500")
	token := firstNonEmpty(os.Getenv("CONSUL_TOKEN"), os.Getenv("SPRING_CLOUD_CONSUL_TOKEN"), v.GetString("consul_token"))

	cfg := api.DefaultConfig()
	cfg.Address = net.JoinHostPort(host, port)
	if token != "" {
		cfg.Token = token
	}
	client, err := api.NewClient(cfg)
	if err != nil {
		return nil, fmt.Errorf("consul client: %w", err)
	}

	// 从 ":8080" 解析出端口号 8080。
	_, svcPort, err := net.SplitHostPort(listenAddr)
	if err != nil {
		return nil, fmt.Errorf("parse listen addr %q: %w", listenAddr, err)
	}
	portNum, _ := strconv.Atoi(svcPort)

	// 注册到 Consul 的 IP：容器环境常设 CONSUL_SERVICE_ADDRESS 或 POD_IP。
	advertiseIP := firstNonEmpty(
		os.Getenv("CONSUL_SERVICE_ADDRESS"),
		os.Getenv("POD_IP"),
		v.GetString("consul_service_address"),
	)
	if advertiseIP == "" {
		advertiseIP, _ = outboundIP()
	}
	if advertiseIP == "" {
		advertiseIP = "127.0.0.1"
	}

	serviceName := v.GetString("consul_service_name")
	if serviceName == "" {
		serviceName = "micro-drama-video"
	}
	// 每个实例唯一 ID，避免同机多实例冲突。
	serviceID := fmt.Sprintf("%s-%s-%s", serviceName, advertiseIP, svcPort)
	// Consul 定期请求此 URL 判断实例是否健康（类似 Spring Actuator /actuator/health）。
	checkURL := fmt.Sprintf("http://%s:%s%s", advertiseIP, svcPort, healthPath)

	reg := &api.AgentServiceRegistration{
		ID:      serviceID,
		Name:    serviceName,
		Address: advertiseIP,
		Port:    portNum,
		Tags:    []string{"go", "video", "http"},
		Check: &api.AgentServiceCheck{
			HTTP:                           checkURL,
			Interval:                       v.GetString("consul_health_interval"),
			Timeout:                        "5s",
			DeregisterCriticalServiceAfter: "1m", // 连续失败后自动注销
		},
	}
	if reg.Check.Interval == "" {
		reg.Check.Interval = "10s"
	}

	if err := client.Agent().ServiceRegister(reg); err != nil {
		return nil, fmt.Errorf("service register: %w", err)
	}
	log.Info("consul service registered",
		zap.String("id", serviceID),
		zap.String("name", serviceName),
		zap.String("check", checkURL),
	)

	deregister := func() {
		if err := client.Agent().ServiceDeregister(serviceID); err != nil {
			log.Warn("consul deregister", zap.Error(err))
		}
	}
	return deregister, nil
}

// outboundIP 通过 UDP 拨号探测本机出口网卡 IP（容器内常用）。
// 不真正发送业务数据，仅让内核选择默认路由对应的本地地址。
func outboundIP() (string, error) {
	conn, err := net.Dial("udp", "8.8.8.8:80")
	if err != nil {
		return "", err
	}
	defer conn.Close()
	localAddr := conn.LocalAddr().(*net.UDPAddr)
	return localAddr.IP.String(), nil
}

// firstNonEmpty 返回参数中第一个非空字符串（去首尾空格后）。
func firstNonEmpty(vals ...string) string {
	for _, v := range vals {
		if strings.TrimSpace(v) != "" {
			return strings.TrimSpace(v)
		}
	}
	return ""
}
