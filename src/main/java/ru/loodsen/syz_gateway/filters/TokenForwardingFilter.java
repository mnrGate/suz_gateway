package ru.loodsen.syz_gateway.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;


/**
 * Пересылает аутентификационные токены (Access Token и Refresh Token) на нижестоящие сервисы.
 * Проверяет наличие заголовков Authorization (Access Token) и X-Refresh-Token (Refresh Token).
 * Пересылает токены в запросе к целевым сервисам.
 * Пропускает OPTIONS-запросы (preflight CORS запросы).
 */
@Component
@Slf4j
public class TokenForwardingFilter extends AbstractGatewayFilterFactory<TokenForwardingFilter.Config> {

    public static final String X_REFRESH_TOKEN = "X-Refresh-Token";

    public static class Config {
    }

    public TokenForwardingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            log.debug("TokenForwardingFilter started for request: {}", request.getURI());
            if (HttpMethod.OPTIONS.matches(request.getMethod().name())) {
                log.debug("Skipping TokenForwardingFilter for OPTIONS request: {}", request.getURI());
                return chain.filter(exchange);
            }

            String accessToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String refreshToken = request.getHeaders().getFirst(X_REFRESH_TOKEN);
            log.debug("Authorization header (Access Token): {}", accessToken);
            log.debug("X-Refresh-Token header: {}", refreshToken);

            if (accessToken != null && !accessToken.isEmpty()) {
                log.debug("Access token found, forwarding with Authorization header: {}", accessToken);
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .build();
                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            }
            if (refreshToken != null && !refreshToken.isEmpty()) {
                log.debug("Access token missing, using Refresh token: {}", refreshToken);
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header(X_REFRESH_TOKEN, refreshToken)
                        .build();
                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            }
            log.debug("No valid token found, proceeding without Authorization or Refresh tokens.");
            return chain.filter(exchange);
        };
    }
}
