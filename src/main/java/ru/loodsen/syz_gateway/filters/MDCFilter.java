package ru.loodsen.syz_gateway.filters;

import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


/**
 * Добавляет уникальный traceId для каждого запроса,
 * чтобы обеспечить трассировку между логами.
 */
@Component
@Slf4j
public class MDCFilter implements GlobalFilter, Ordered {

    private final Tracer tracer;

    public MDCFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String traceId = tracer.currentSpan() != null ? tracer.currentSpan().context().traceId() : "unknown";
        MDC.put("traceId", traceId);
        log.debug("MDCFilter - traceId set to: {}", traceId);

        return chain.filter(exchange).doFinally(signalType -> {
            MDC.clear();
            log.debug("MDCFilter - MDC cleared");
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
