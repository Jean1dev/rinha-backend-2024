FROM golang:alpine AS build

WORKDIR /app

COPY go.mod go.sum ./
RUN go mod download

COPY . ./

RUN go build -o /app/bin/ ./main.go

FROM scratch AS run

WORKDIR /app

COPY --from=build /app/bin ./

CMD ["./main"]