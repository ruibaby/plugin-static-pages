package cc.ryanc.staticpages.service;

import cc.ryanc.staticpages.extensions.Project;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import run.halo.app.infra.utils.PathUtils;

@Component
public class ProjectRewriteRules {
    private final Map<String, List<PathPattern>> projectPatterns = new ConcurrentHashMap<>();
    @Getter
    private final Map<PathPattern, String> rewriteRules = new ConcurrentHashMap<>();
    private final PathPatternParser patternParser = PathPatternParser.defaultInstance;

    public void updateRules(Project project) {
        var rules = project.getSpec().getRewrites();
        if (rules == null) {
            return;
        }
        removeRule(project.getMetadata().getName());
        for (Project.Rewrite rule : rules) {
            var source = sourceInProject(project, rule.getSource());
            var targetPath = sourceInProject(project, rule.getTarget());
            addRule(project.getMetadata().getName(), source, targetPath);
        }
    }

    public void removeRules(Project project) {
        var projectName = project.getMetadata().getName();
        removeRule(projectName);
    }

    void addRule(String projectName, String key, String value) {
        PathPattern pattern = patternParser.parse(key);
        projectPatterns.compute(projectName, (k, v) -> {
            if (v == null) {
                v = new ArrayList<>();
            }
            v.add(pattern);
            return v;
        });
        rewriteRules.put(pattern, value);
    }

    void removeRule(String projectName) {
        var patterns = projectPatterns.remove(projectName);
        if (patterns == null) {
            return;
        }
        for (PathPattern pattern : patterns) {
            rewriteRules.remove(pattern);
        }
    }

    String sourceInProject(Project project, String source) {
        return PathUtils.combinePath(project.getSpec().getDirectory(), source);
    }
}
