package cc.ryanc.staticpages.extensions;

import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;

import cc.ryanc.staticpages.service.PageProjectService;
import cc.ryanc.staticpages.service.ProjectRewriteRules;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;

@Component
@RequiredArgsConstructor
public class ProjectReconciler implements Reconciler<Reconciler.Request> {
    private static final String FINALIZER = "project-protection";

    private final ExtensionClient client;
    private final ProjectRewriteRules projectRewriteRules;
    private final PageProjectService pageProjectService;

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
                client.update(project);
            });
        return Result.doNotRetry();
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Project())
            .build();
    }
}
