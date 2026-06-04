package auth

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v4"
)

// GinMiddleware 校验 Bearer JWT（HS256），允许 admin / user 访问 video-api。
func GinMiddleware(secret string, allowedAudiences []string, skipPaths ...string) gin.HandlerFunc {
	allowed := make(map[string]struct{}, len(allowedAudiences))
	for _, a := range allowedAudiences {
		allowed[a] = struct{}{}
	}
	return func(c *gin.Context) {
		if secret == "" {
			c.Next()
			return
		}
		path := c.Request.URL.Path
		for _, skip := range skipPaths {
			if path == skip || strings.HasPrefix(path, skip) {
				c.Next()
				return
			}
		}
		auth := c.GetHeader("Authorization")
		if !strings.HasPrefix(auth, "Bearer ") {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "missing bearer token"})
			return
		}
		tokenStr := strings.TrimSpace(strings.TrimPrefix(auth, "Bearer "))
		token, err := jwt.Parse(tokenStr, func(t *jwt.Token) (interface{}, error) {
			if _, ok := t.Method.(*jwt.SigningMethodHMAC); !ok {
				return nil, jwt.ErrSignatureInvalid
			}
			return []byte(secret), nil
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
		c.Set("jwt_sub", sub)
		c.Set("jwt_aud", aud)
		if c.GetHeader("X-User-Id") == "" && aud == "user" {
			c.Request.Header.Set("X-User-Id", sub)
		}
		c.Next()
	}
}
