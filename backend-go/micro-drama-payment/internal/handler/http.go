package handler

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"

	"micro-drama-payment/internal/service"
)

func Register(r *gin.Engine, log *zap.Logger, svc *service.PaymentService) {
	r.GET("/healthz", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"ok": true})
	})

	r.POST("/v1/orders", func(c *gin.Context) {
		var body struct {
			ID string `json:"id"`
		}
		if err := c.ShouldBindJSON(&body); err != nil || body.ID == "" {
			c.JSON(http.StatusBadRequest, gin.H{"error": "id required"})
			return
		}
		o, err := svc.CreateOrder(c.Request.Context(), body.ID)
		if err != nil {
			log.Error("create order", zap.Error(err))
			c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
			return
		}
		c.JSON(http.StatusOK, o)
	})

	r.POST("/v1/tx/raw", func(c *gin.Context) {
		var body struct {
			SignedHex string `json:"signedHex"`
		}
		if err := c.ShouldBindJSON(&body); err != nil || body.SignedHex == "" {
			c.JSON(http.StatusBadRequest, gin.H{"error": "signedHex required"})
			return
		}
		txHash, err := svc.SendRawTransaction(c.Request.Context(), body.SignedHex)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}
		c.JSON(http.StatusOK, gin.H{"txHash": txHash})
	})
}
