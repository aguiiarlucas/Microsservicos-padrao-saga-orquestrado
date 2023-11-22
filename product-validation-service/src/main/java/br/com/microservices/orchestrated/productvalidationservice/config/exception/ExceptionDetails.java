package br.com.microservices.orchestrated.productvalidationservice.config.exception;

import org.springframework.http.HttpStatus;

public record ExceptionDetails(HttpStatus status, String message) {
}
