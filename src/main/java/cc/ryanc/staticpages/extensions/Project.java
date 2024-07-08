package cc.ryanc.staticpages.extensions;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "staticpage.halo.run", version = "v1alpha1", kind = "Project",
    plural = "projects", singular = "project")
public class Project extends AbstractExtension {

    @Schema(requiredMode = REQUIRED)
    private Spec spec;

    @Data
    @Schema(name = "ProjectSpec")
    public static class Spec {
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String title;

        private String icon;

        private String description;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String directory;

        @Schema(requiredMode = NOT_REQUIRED)
        private List<Rewrite> rewrites;
    }

    @Data
    @Schema(name = "ProjectRewrite")
    public static class Rewrite {
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String source;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String target;
    }
}
