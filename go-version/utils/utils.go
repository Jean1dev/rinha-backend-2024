package utils

import (
	"fmt"
	"strconv"
	"strings"
)

func ParseIDFromURL(url string) (int, error) {
	segments := strings.Split(url, "/")
	if len(segments) < 3 {
		return 0, fmt.Errorf("URL inválida")
	}

	id, err := strconv.Atoi(segments[2])
	if err != nil {
		return 0, err
	}

	return id, nil
}
