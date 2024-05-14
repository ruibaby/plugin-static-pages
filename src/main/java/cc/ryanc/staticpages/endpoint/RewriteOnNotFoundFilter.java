package cc.ryanc.staticpages.endpoint;

import cc.ryanc.staticpages.service.ProjectRewriteRules;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.PathContainer;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.security.AdditionalWebFilter;

@Component
@RequiredArgsConstructor
public class RewriteOnNotFoundFilter implements AdditionalWebFilter {

    private final ProjectRewriteRules rewriteRules;

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return chain.filter(exchange)
            .onErrorResume(NoResourceFoundException.class, e -> {
                if (!isMatchRequest(exchange)) {
                    return Mono.error(e);
                }
                // Check rewrite rules
                var requestPathContainer =
                    exchange.getRequest().getPath().pathWithinApplication();
                var requestPath = normalizePath(requestPathContainer);
                for (var entry : rewriteRules.getRewriteRules().entrySet()) {
                    if (entry.getKey().matches(requestPath)) {
                        // Rewrite the request here
                        String rewrittenPath = entry.getValue();
                        var mutatedRequest = exchange.getRequest().mutate()
                            .path(rewrittenPath).build();
                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    }
                }
                return Mono.error(e);
            });
    }

    private boolean isMatchRequest(ServerWebExchange exchange) {
        var requestPathContainer = exchange.getRequest().getPath().pathWithinApplication();
        var projectRootPaths = rewriteRules.getProjectRootPaths();
        for (String projectRootPath : projectRootPaths) {
            if (requestPathContainer.value().startsWith(ensureLeadingSlash(projectRootPath))) {
                return true;
            }
        }
        return false;
    }

    String ensureLeadingSlash(String path) {
        return path.startsWith("/") ? path : "/" + path;
    }

    private PathContainer normalizePath(PathContainer pathContainer) {
        if (pathContainer.value().endsWith("/")) {
            // Remove trailing slash
            return pathContainer.subPath(0, pathContainer.elements().size() - 1);
        }
        return pathContainer;
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
