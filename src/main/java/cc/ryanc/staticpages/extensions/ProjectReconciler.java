package cc.ryanc.staticpages.extensions;

import static cc.ryanc.staticpages.extensions.Project.Phase.FAILED;
import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;

import cc.ryanc.staticpages.service.PageFileManager;
import cc.ryanc.staticpages.service.PageProjectService;
import cc.ryanc.staticpages.service.ProjectRewriteRules;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionStatus;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectReconciler implements Reconciler<Reconciler.Request> {
    private static final String FINALIZER = "project-protection";

    private final ExtensionClient client;
    private final ProjectRewriteRules projectRewriteRules;
    private final PageProjectService pageProjectService;
    private final PageFileManager pageFileManager;

    @Override
    public Result reconcile(Request request) {
        client.fetch(Project.class, request.name())
            .ifPresent(project -> {
                if (ExtensionUtil.isDeleted(project)) {
                    if (removeFinalizers(project.getMetadata(), Set.of(FINALIZER))) {
                        projectRewriteRules.removeRules(project);
                        pageProjectService.deleteProject(project).block();
                        client.update(project);
                        return;
                    }
                }
                addFinalizers(project.getMetadata(), Set.of(FINALIZER));
                projectRewriteRules.updateRules(project);
                handleDirectoryChange(project);

                client.update(project);
            });
        return Result.doNotRetry();
    }

    private void handleDirectoryChange(Project project) {
        var annotations = MetadataUtil.nullSafeAnnotations(project);
        var directory = project.getSpec().getDirectory();
        var oldDir = annotations.get(Project.LAST_DIRECTORY_ANNO);
        var newPath = pageProjectService.determinePath(directory);

        if (shouldMove(oldDir, directory, newPath)) {
            moveTo(project, oldDir, newPath);
        }
        annotations.put(Project.LAST_DIRECTORY_ANNO, directory);
    }

    boolean shouldMove(String oldDir, String newDir, Path newPath) {
        return StringUtils.isNotBlank(oldDir) && !oldDir.equals(newDir)
            || !Files.exists(newPath);
    }

    private void moveTo(Project project, String oldDir, Path target) {
        var oldPath = pageProjectService.determinePath(oldDir);
        try {
            pageFileManager.move(oldPath, target).block();
        } catch (Throwable e) {
            log.error("Failed to move directory from {} to {}", oldPath, target, e);
            project.getStatus().setPhase(FAILED);
            var condition = Condition.builder()
                .type("UnexpectedState")
                .reason("FailedToMoveDirectory")
                .message(e.getMessage())
                .status(ConditionStatus.FALSE)
                .lastTransitionTime(Instant.now())
                .build();
            project.getStatus().getConditions().addAndEvictFIFO(condition);
        }
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Project())
            .build();
    }
}
