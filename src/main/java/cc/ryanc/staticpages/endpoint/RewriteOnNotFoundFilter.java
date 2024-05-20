package cc.ryanc.staticpages.endpoint;

import cc.ryanc.staticpages.service.ProjectRewriteRules;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.PathContainer;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;
import run.halo.app.security.AdditionalWebFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class RewriteOnNotFoundFilter implements AdditionalWebFilter {

    private final ProjectRewriteRules rewriteRules;

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return chain.filter(exchange)
            .onErrorResume(NoResourceFoundException.class,
                e -> tryRewritesSequentially(exchange, chain, e)
            );
    }

    private Mono<Void> tryRewritesSequentially(ServerWebExchange exchange, WebFilterChain chain,
        Throwable e) {
        if (!hasMatchedRewriteRule(exchange)) {
            return Mono.error(e);
        }
        var requestPath = normalizePath(exchange.getRequest().getPath().pathWithinApplication());

        // Collect all possible rewrites into a list
        var rewrites = getRulesWithDefault(exchange).entrySet().stream()
            .filter(entry -> entry.getKey().matches(requestPath))
            .map(Map.Entry::getValue)
            .toList();

        // Attempt to apply each rewrite one by one until one succeeds
        return tryRewrites(exchange, chain, rewrites.iterator(), e);
    }

    private Mono<Void> tryRewrites(ServerWebExchange exchange, WebFilterChain chain,
        Iterator<String> rewrites, Throwable e) {
        if (!rewrites.hasNext()) {
            return Mono.error(e);
        }

        String rewrittenPath = rewrites.next();
        log.debug("No static resource found for path {} and trying rewrite to {}",
            exchange.getRequest().getPath(), rewrittenPath);
        var mutatedRequest = exchange.getRequest().mutate().path(rewrittenPath).build();
        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        // Try the next rewrite rule if this one fails
        return chain.filter(mutatedExchange)
            .onErrorResume(NoResourceFoundException.class,
                unusedEx -> tryRewrites(mutatedExchange, chain, rewrites, e));
    }

    private boolean hasMatchedRewriteRule(ServerWebExchange exchange) {
        var requestPathContainer = exchange.getRequest().getPath().pathWithinApplication();
        var projectRootPaths = rewriteRules.getProjectRootPaths();
        for (String projectRootPath : projectRootPaths) {
            if (requestPathContainer.value().startsWith(ensureLeadingSlash(projectRootPath))) {
                return true;
            }
        }
        return false;
    }

    Map<PathPattern, String> getRulesWithDefault(ServerWebExchange exchange) {
        var requestPath = exchange.getRequest().getPath().pathWithinApplication();
        var rules = new TreeMap<>(rewriteRules.getRewriteRules());
        var parser = PathPatternParser.defaultInstance;
        rules.put(parser.parse(requestPath.value()), requestPath + "/index.html");
        rules.put(parser.parse(requestPath.value() + "/"), requestPath + "/index.html");
        return rules;
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
