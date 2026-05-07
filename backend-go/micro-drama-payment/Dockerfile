FROM golang:1.22-alpine AS build
RUN apk add --no-cache git ca-certificates
WORKDIR /src
COPY go.mod ./
COPY . .
RUN go mod download && CGO_ENABLED=0 go build -ldflags="-s -w" -o /out/payment ./cmd/payment

FROM alpine:3.19
RUN apk add --no-cache ca-certificates
COPY --from=build /out/payment /usr/local/bin/payment
EXPOSE 8080
ENTRYPOINT ["/usr/local/bin/payment"]
