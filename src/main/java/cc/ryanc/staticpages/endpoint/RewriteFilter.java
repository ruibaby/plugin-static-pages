package cc.ryanc.staticpages.endpoint;

import cc.ryanc.staticpages.service.ProjectRewriteRules;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.PathContainer;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.security.AdditionalWebFilter;

@Component
@RequiredArgsConstructor
public class RewriteFilter implements AdditionalWebFilter {
    private final ProjectRewriteRules rewriteRules;

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        var request = exchange.getRequest();
        var requestPath = normalizePath(request.getPath().pathWithinApplication());

        for (var entry : rewriteRules.getRewriteRules().entrySet()) {
            if (entry.getKey().matches(requestPath)) {
                String rewrittenPath = entry.getValue();
                var newExchange = exchange.mutate()
                    .request(request.mutate().path(rewrittenPath).build())
                    .build();
                return chain.filter(newExchange);
            }
        }
        return chain.filter(exchange);
    }

    private PathContainer normalizePath(PathContainer pathContainer) {
        if (pathContainer.value().endsWith("/")) {
            return pathContainer.subPath(0, pathContainer.elements().size() - 1);
        }
        return pathContainer;
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
