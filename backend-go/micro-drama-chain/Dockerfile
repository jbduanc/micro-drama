FROM golang:1.22-alpine AS build
RUN apk add --no-cache git ca-certificates
WORKDIR /src
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 go build -ldflags="-s -w" -o /out/chain ./cmd/chain

FROM alpine:3.19
RUN apk add --no-cache ca-certificates
COPY --from=build /out/chain /usr/local/bin/chain
EXPOSE 9092
ENTRYPOINT ["/usr/local/bin/chain"]
