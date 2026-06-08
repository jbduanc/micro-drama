package auth

import (
	"strings"

	"github.com/gin-gonic/gin"
)

// Traefik ForwardAuth（gateway-auth）注入头；须同时存在 X-Gateway-Request-Id。
const (
	HeaderGatewayRequestID = "X-Gateway-Request-Id"
	HeaderAuthSubject      = "X-Auth-Subject"
	HeaderAuthAudience     = "X-Auth-Audience"
	HeaderUserID           = "X-User-Id"
)

const (
	CtxJWTSub = "jwt_sub"
	CtxJWTAud = "jwt_aud"
)

func IsGatewayProxied(c *gin.Context) bool {
	return strings.TrimSpace(c.GetHeader(HeaderGatewayRequestID)) != ""
}

func PrincipalFromGateway(c *gin.Context) (subject string, aud string, ok bool) {
	if !IsGatewayProxied(c) {
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

func ApplyPrincipal(c *gin.Context, subject, aud string) {
	c.Set(CtxJWTSub, subject)
	c.Set(CtxJWTAud, aud)
	if aud == "user" {
		c.Request.Header.Set(HeaderUserID, subject)
	}
}

func GetSubject(c *gin.Context) string {
	if v, ok := c.Get(CtxJWTSub); ok {
		if s, ok := v.(string); ok && s != "" {
			return s
		}
	}
	if sub, _, ok := PrincipalFromGateway(c); ok {
		return sub
	}
	return ""
}

func GetAudience(c *gin.Context) string {
	if v, ok := c.Get(CtxJWTAud); ok {
		if s, ok := v.(string); ok && s != "" {
			return s
		}
	}
	if _, aud, ok := PrincipalFromGateway(c); ok {
		return aud
	}
	return ""
}

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

func GetAdminEmail(c *gin.Context) string {
	if GetAudience(c) == "admin" {
		return GetSubject(c)
	}
	return ""
}
