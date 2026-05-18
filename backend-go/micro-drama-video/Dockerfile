FROM golang:1.22-alpine AS build
RUN apk add --no-cache git ca-certificates
WORKDIR /src
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 go build -ldflags="-s -w" -o /out/video-api ./cmd/video-api

FROM alpine:3.19
RUN apk add --no-cache ca-certificates
COPY --from=build /out/video-api /usr/local/bin/video-api
EXPOSE 8080
ENTRYPOINT ["/usr/local/bin/video-api"]
