package auth

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v4"
)

// MiddlewareConfig Gin 鉴权：Kong 模式信任网关头并查 Redis；本地直连时校验 Bearer JWT。
type MiddlewareConfig struct {
	Secret           string
	AllowedAudiences []string
	GatewayMode      string
	SessionStore     SessionStore
	SkipPaths        []string
}

// GinMiddleware 返回 Gin 鉴权中间件。
func GinMiddleware(cfg MiddlewareConfig) gin.HandlerFunc {
	allowed := make(map[string]struct{}, len(cfg.AllowedAudiences))
	for _, a := range cfg.AllowedAudiences {
		allowed[a] = struct{}{}
	}
	kongMode := strings.EqualFold(strings.TrimSpace(cfg.GatewayMode), "kong")

	return func(c *gin.Context) {
		path := c.Request.URL.Path
		for _, skip := range cfg.SkipPaths {
			if path == skip || strings.HasPrefix(path, skip) {
				c.Next()
				return
			}
		}

		if sub, aud, ok := PrincipalFromKong(c); ok {
			if _, ok := allowed[aud]; !ok {
				c.AbortWithStatusJSON(http.StatusForbidden, gin.H{"error": "audience not allowed"})
				return
			}
			if kongMode {
				token := BearerToken(c)
				if token == "" || cfg.SessionStore == nil {
					c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "invalid or revoked token"})
					return
				}
				valid, err := cfg.SessionStore.SessionValid(c.Request.Context(), token, sub, aud)
				if err != nil || !valid {
					c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "invalid or revoked token"})
					return
				}
			}
			ApplyPrincipal(c, sub, aud)
			c.Next()
			return
		}

		if cfg.Secret == "" {
			c.Next()
			return
		}

		tokenStr := BearerToken(c)
		if tokenStr == "" {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "missing bearer token"})
			return
		}
		token, err := jwt.Parse(tokenStr, func(t *jwt.Token) (interface{}, error) {
			if _, ok := t.Method.(*jwt.SigningMethodHMAC); !ok {
				return nil, jwt.ErrSignatureInvalid
			}
			return []byte(cfg.Secret), nil
		})
		if err != nil || !token.Valid {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "invalid token"})
			return
		}
		claims, ok := token.Claims.(jwt.MapClaims)
		if !ok {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "invalid claims"})
			return
		}
		aud, _ := claims["aud"].(string)
		if aud == "" {
			aud = "admin"
		}
		if _, ok := allowed[aud]; !ok {
			c.AbortWithStatusJSON(http.StatusForbidden, gin.H{"error": "audience not allowed"})
			return
		}
		sub, _ := claims["sub"].(string)
		ApplyPrincipal(c, sub, aud)
		c.Next()
	}
}

// BearerToken 从 Authorization 头提取 Bearer token。
func BearerToken(c *gin.Context) string {
	auth := strings.TrimSpace(c.GetHeader("Authorization"))
	if auth == "" {
		return ""
	}
	parts := strings.SplitN(auth, " ", 2)
	if len(parts) != 2 || !strings.EqualFold(parts[0], "Bearer") {
		return ""
	}
	return strings.TrimSpace(parts[1])
}
