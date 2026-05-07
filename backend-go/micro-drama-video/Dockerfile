FROM golang:1.22-alpine AS build
RUN apk add --no-cache git ca-certificates
WORKDIR /src
COPY go.mod ./
COPY . .
RUN go mod download && CGO_ENABLED=0 go build -ldflags="-s -w" -o /out/video-worker ./cmd/video-worker

FROM alpine:3.19
RUN apk add --no-cache ca-certificates ffmpeg
COPY --from=build /out/video-worker /usr/local/bin/video-worker
ENTRYPOINT ["/usr/local/bin/video-worker"]
