// Package consul 封装与 HashiCorp Consul 的交互（配置中心 + 服务注册）。
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

func MergeRemoteConfig(v *viper.Viper, log *zap.Logger) error {
	if !v.GetBool("consul_enabled") {
		log.Info("consul config disabled")
		return nil
	}

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

	cfg := api.DefaultConfig()
	cfg.Address = net.JoinHostPort(host, port)
	if token != "" {
		cfg.Token = token
	}
	client, err := api.NewClient(cfg)
	if err != nil {
		return fmt.Errorf("consul client: %w", err)
	}

	prefix := strings.Trim(v.GetString("consul_config_prefix"), "/")
	name := v.GetString("consul_config_name")
	dataKey := v.GetString("consul_config_data_key")
	if dataKey == "" {
		dataKey = "data"
	}
	kvKey := fmt.Sprintf("%s/%s/%s", prefix, name, dataKey)

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

	var nested map[string]any
	if err := yaml.Unmarshal(pair.Value, &nested); err != nil {
		return fmt.Errorf("parse consul yaml: %w", err)
	}
	flattenIntoViper("", nested, v)
	log.Info("consul config loaded", zap.String("key", kvKey))
	return nil
}

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

func RegisterService(v *viper.Viper, log *zap.Logger, listenAddr, healthPath string) (func(), error) {
	if !v.GetBool("consul_discovery_enabled") {
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

	_, svcPort, err := net.SplitHostPort(listenAddr)
	if err != nil {
		return nil, fmt.Errorf("parse listen addr %q: %w", listenAddr, err)
	}
	portNum, _ := strconv.Atoi(svcPort)

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
		serviceName = "micro-drama-transcoder"
	}
	serviceID := fmt.Sprintf("%s-%s-%s", serviceName, advertiseIP, svcPort)
	checkURL := fmt.Sprintf("http://%s:%s%s", advertiseIP, svcPort, healthPath)

	reg := &api.AgentServiceRegistration{
		ID:      serviceID,
		Name:    serviceName,
		Address: advertiseIP,
		Port:    portNum,
		Tags:    []string{"go", "video", "transcoder"},
		Check: &api.AgentServiceCheck{
			HTTP:                           checkURL,
			Interval:                       v.GetString("consul_health_interval"),
			Timeout:                        "5s",
			DeregisterCriticalServiceAfter: "1m",
		},
	}
	if reg.Check.Interval == "" {
		reg.Check.Interval = "10s"
	}

	if err := client.Agent().ServiceRegister(reg); err != nil {
		return nil, fmt.Errorf("service register: %w", err)
	}
	log.Info("consul service registered", zap.String("id", serviceID), zap.String("name", serviceName), zap.String("check", checkURL))

	return func() {
		if err := client.Agent().ServiceDeregister(serviceID); err != nil {
			log.Warn("consul deregister", zap.Error(err))
		}
	}, nil
}

func outboundIP() (string, error) {
	conn, err := net.Dial("udp", "8.8.8.8:80")
	if err != nil {
		return "", err
	}
	defer conn.Close()
	localAddr := conn.LocalAddr().(*net.UDPAddr)
	return localAddr.IP.String(), nil
}

func firstNonEmpty(vals ...string) string {
	for _, v := range vals {
		if strings.TrimSpace(v) != "" {
			return strings.TrimSpace(v)
		}
	}
	return ""
}
