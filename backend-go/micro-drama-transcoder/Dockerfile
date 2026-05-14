FROM golang:1.22-alpine AS build
RUN apk add --no-cache git ca-certificates
WORKDIR /src
COPY go.mod ./
COPY . .
RUN go mod download && CGO_ENABLED=0 go build -ldflags="-s -w" -o /out/transcoder ./cmd/transcoder

FROM alpine:3.19
RUN apk add --no-cache ca-certificates
COPY --from=build /out/transcoder /usr/local/bin/transcoder
EXPOSE 8080
ENTRYPOINT ["/usr/local/bin/transcoder"]
