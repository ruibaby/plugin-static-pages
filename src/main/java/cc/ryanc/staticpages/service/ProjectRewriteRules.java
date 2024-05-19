package cc.ryanc.staticpages.service;

import cc.ryanc.staticpages.extensions.Project;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import run.halo.app.infra.utils.PathUtils;

@Component
public class ProjectRewriteRules {
    private final Map<SimpleProject, List<PathPattern>> projectPatterns = new ConcurrentHashMap<>();
    @Getter
    private final Map<PathPattern, String> rewriteRules = new ConcurrentHashMap<>();
    private final PathPatternParser patternParser = PathPatternParser.defaultInstance;

    private static List<Project.Rewrite> getRulesWithDefault(Project project) {
        var rules = new ArrayList<Project.Rewrite>();
        if (project.getSpec().getRewrites() != null) {
            rules.addAll(project.getSpec().getRewrites());
        }

        var defaultRewrite = new Project.Rewrite();
        defaultRewrite.setSource("/");
        defaultRewrite.setTarget("/index.html");

        if (!rules.contains(defaultRewrite)) {
            rules.add(defaultRewrite);
        }

        return rules;
    }

    public void updateRules(Project project) {
        var rules = getRulesWithDefault(project);

        var simpleProject = SimpleProject.builder()
            .name(project.getMetadata().getName())
            .rootPath(project.getSpec().getDirectory())
            .build();
        removeRule(simpleProject);
        for (Project.Rewrite rule : rules) {
            var source = sourceInProject(project, rule.getSource());
            var targetPath = sourceInProject(project, rule.getTarget());
            addRule(simpleProject, source, targetPath);
        }
    }

    public void removeRules(Project project) {
        var simpleProject = SimpleProject.builder()
            .name(project.getMetadata().getName())
            .rootPath(project.getSpec().getDirectory())
            .build();
        removeRule(simpleProject);
    }

    public Set<String> getProjectRootPaths() {
        return projectPatterns.keySet()
            .stream()
            .map(SimpleProject::rootPath)
            .collect(Collectors.toSet());
    }

    void addRule(SimpleProject project, String key, String value) {
        PathPattern pattern = patternParser.parse(key);
        projectPatterns.compute(project, (k, v) -> {
            if (v == null) {
                v = new ArrayList<>();
            }
            v.add(pattern);
            return v;
        });
        rewriteRules.put(pattern, value);
    }

    void removeRule(SimpleProject project) {
        var patterns = projectPatterns.remove(project);
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

    @Builder
    record SimpleProject(String name, String rootPath) {
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SimpleProject that = (SimpleProject) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}
