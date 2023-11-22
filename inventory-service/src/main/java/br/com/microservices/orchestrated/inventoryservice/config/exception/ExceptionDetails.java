package br.com.microservices.orchestrated.inventoryservice.config.exception;

import org.springframework.http.HttpStatus;

public record ExceptionDetails(HttpStatus status, String message) {
}
