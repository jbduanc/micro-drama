// Package response 提供统一 HTTP JSON 响应结构。
//
// 与 Java 微服务 com.series.common.entity.Result 字段对齐（code、msg、data、serverTime），
// 前端和 API 网关可共用同一套解析逻辑。
package response

import "time"

// Result 泛型统一响应包装。T 为业务数据类型，成功时放在 data 字段。
//
// code：0 表示成功，非 0 表示失败（与 Java ResponseCode 约定一致时可再细化）。
// msg：人类可读提示。
// data：业务载荷，失败时通常省略。
// serverTime：服务端时间戳（毫秒），便于排查时区/时钟问题。
type Result[T any] struct {
	Code       int    `json:"code"`
	Msg        string `json:"msg"`
	Data       T      `json:"data,omitempty"`
	ServerTime int64  `json:"serverTime"`
}

// OK 构造成功响应，code=0，msg=成功。
func OK[T any](data T) Result[T] {
	return Result[T]{
		Code:       0,
		Msg:        "成功",
		Data:       data,
		ServerTime: time.Now().UnixMilli(),
	}
}

// Fail 构造失败响应，不含 data。
//
// code：HTTP 层常用 400/500，也可与业务错误码表一致。
// msg：返回给前端的错误说明。
func Fail[T any](code int, msg string) Result[T] {
	return Result[T]{
		Code:       code,
		Msg:        msg,
		ServerTime: time.Now().UnixMilli(),
	}
}
