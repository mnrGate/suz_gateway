package ru.loodsen.syz_gateway.filters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyRoutingFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Логирует детали маршрутизации каждого запроса после того, как он был маршрутизирован шлюзом.
 * Логирует URI, на который был перенаправлен запрос.
 * Логирует ID маршрута и связанные с ним предикаты и фильтры.
 */

@Component
@Slf4j
@RequiredArgsConstructor
public class RouteLoggingFilter implements GlobalFilter, Ordered {

    private final RouteDefinitionLocator routeDefinitionLocator;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        return chain.filter(exchange).then(
                Mono.defer(() -> {
                    Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);

                    if (route != null) {
                        log.info("Request routed to URI: {}", route.getUri());
                        log.debug("Route ID: {}", route.getId());

                        // Получаем определения маршрутов
                        Flux<RouteDefinition> routeDefinitions = routeDefinitionLocator.getRouteDefinitions();

                        return routeDefinitions
                                .filter(rd -> rd.getId().equals(route.getId()))
                                .singleOrEmpty()
                                .doOnNext(routeDefinition -> {
                                    // Логируем индивидуальные предикаты
                                    log.debug("Route Predicates: {}", routeDefinition.getPredicates());
                                    // Логируем фильтры маршрута
                                    log.debug("Route Filters: {}", routeDefinition.getFilters());
                                })
                                .then();
                    } else {
                        log.warn("No route found for the request.");
                        return Mono.empty();
                    }
                })
        );
    }

     @Override
    public int getOrder() {
        // Выполняется после маршрутизации then(Mono.defer(...))..
         return NettyRoutingFilter.ORDER;
    }
}

