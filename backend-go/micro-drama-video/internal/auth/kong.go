package auth

import (
	"strings"

	"github.com/gin-gonic/gin"
)

// Kong 网关注入头（须同时存在 X-Kong-Request-Id，防止客户端伪造）。
const (
	HeaderKongRequestID = "X-Kong-Request-Id"
	HeaderAuthSubject   = "X-Auth-Subject"
	HeaderAuthAudience  = "X-Auth-Audience"
	HeaderUserID        = "X-User-Id"
)

// Gin context keys（与历史 jwt_sub / jwt_aud 兼容）。
const (
	CtxJWTSub = "jwt_sub"
	CtxJWTAud = "jwt_aud"
)

// IsKongProxied 请求是否经 Kong 转发。
func IsKongProxied(c *gin.Context) bool {
	return strings.TrimSpace(c.GetHeader(HeaderKongRequestID)) != ""
}

// PrincipalFromKong 从 Kong 头解析登录身份；第二个返回值为 aud（admin/user）。
func PrincipalFromKong(c *gin.Context) (subject string, aud string, ok bool) {
	if !IsKongProxied(c) {
		return "", "", false
	}
	subject = strings.TrimSpace(c.GetHeader(HeaderAuthSubject))
	if subject == "" {
		return "", "", false
	}
	aud = strings.TrimSpace(c.GetHeader(HeaderAuthAudience))
	if aud == "" {
		aud = "user"
	}
	return subject, aud, true
}

// ApplyPrincipal 将身份写入 Gin 上下文，并同步 X-User-Id（C 端）。
func ApplyPrincipal(c *gin.Context, subject, aud string) {
	c.Set(CtxJWTSub, subject)
	c.Set(CtxJWTAud, aud)
	if aud == "user" {
		c.Request.Header.Set(HeaderUserID, subject)
	}
}

// GetSubject 当前登录主体（优先 Kong / JWT 中间件写入的上下文）。
func GetSubject(c *gin.Context) string {
	if v, ok := c.Get(CtxJWTSub); ok {
		if s, ok := v.(string); ok && s != "" {
			return s
		}
	}
	if sub, _, ok := PrincipalFromKong(c); ok {
		return sub
	}
	return ""
}

// GetAudience 当前受众 admin / user。
func GetAudience(c *gin.Context) string {
	if v, ok := c.Get(CtxJWTAud); ok {
		if s, ok := v.(string); ok && s != "" {
			return s
		}
	}
	if _, aud, ok := PrincipalFromKong(c); ok {
		return aud
	}
	return ""
}

// GetUserID C 端用户 UUID（aud=user 时与 subject 相同）。
func GetUserID(c *gin.Context) string {
	if GetAudience(c) == "user" {
		if id := GetSubject(c); id != "" {
			return id
		}
	}
	if id := strings.TrimSpace(c.GetHeader(HeaderUserID)); id != "" {
		return id
	}
	return ""
}

// GetAdminEmail 管理端邮箱（aud=admin 时的 subject）。
func GetAdminEmail(c *gin.Context) string {
	if GetAudience(c) == "admin" {
		return GetSubject(c)
	}
	return ""
}
