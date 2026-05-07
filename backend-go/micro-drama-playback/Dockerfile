FROM golang:1.22-alpine AS build
RUN apk add --no-cache git ca-certificates
WORKDIR /src
COPY go.mod ./
COPY . .
RUN go mod download && CGO_ENABLED=0 go build -ldflags="-s -w" -o /out/playback ./cmd/playback

FROM alpine:3.19
RUN apk add --no-cache ca-certificates
COPY --from=build /out/playback /usr/local/bin/playback
EXPOSE 8080
ENTRYPOINT ["/usr/local/bin/playback"]
