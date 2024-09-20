package ru.loodsen.syz_gateway.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Логирует входящие HTTP-запросы и исходящие HTTP-ответы.
 * Логирует HTTP-метод, путь URI, удаленный адрес и заголовки входящих запросов.
 * Логирует статусный код и заголовки исходящих ответов.
 */

@Component
@Slf4j
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        HttpHeaders headers = request.getHeaders();
        String remoteAddress = request.getRemoteAddress() != null ? request.getRemoteAddress().toString() : "unknown";

        log.debug("Incoming request: method={}, path={}, remoteAddress={}", method, path, remoteAddress);
        log.debug("Request headers: {}", headers);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            HttpHeaders responseHeaders = exchange.getResponse().getHeaders();
            int statusCode = exchange.getResponse().getStatusCode() != null ? exchange.getResponse().getStatusCode().value() : 0;
            log.info("Response status code: {}", statusCode);
            log.debug("Response headers: {}", responseHeaders);
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}

