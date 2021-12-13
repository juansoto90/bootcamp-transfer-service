package com.nttdata.transfer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

public class TransferException {

    public static Mono<ServerResponse> errorHandler(Throwable error) {
            WebClientResponseException errorResponse = (WebClientResponseException) error;
            if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                Map<String, Object> body = new HashMap<>();
                body.put("error", errorResponse.getStatusText());
                body.put("status", errorResponse.getStatusCode().value());
                return ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(body);
            } else if (errorResponse.getStatusCode() == HttpStatus.BAD_REQUEST){
                Map<String, Object> body = new HashMap<>();
                body.put("error", errorResponse.getStatusText());
                body.put("status", errorResponse.getStatusCode().value());
                return ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(body);
            }
            return Mono.error(errorResponse);
    }
}
