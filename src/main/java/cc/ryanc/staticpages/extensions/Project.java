package cc.ryanc.staticpages.extensions;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.lang.NonNull;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.infra.ConditionList;

@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "staticpage.halo.run", version = "v1alpha1", kind = "Project",
    plural = "projects", singular = "project")
public class Project extends AbstractExtension {
    public static String LAST_DIRECTORY_ANNO = "staticpages.halo.run/last-directory";

    @Schema(requiredMode = REQUIRED)
    private Spec spec;

    @Getter(onMethod_ = @NonNull)
    @Schema(requiredMode = NOT_REQUIRED)
    private Status status = new Status();

    public void setStatus(Status status) {
        this.status = (status == null ? new Status() : status);
    }

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

    @Data
    @Schema(name = "ProjectStatus")
    public static class Status {
        private Phase phase = Phase.READY;

        @Getter(onMethod_ = @NonNull)
        private ConditionList conditions = new ConditionList();

        public void setConditions(ConditionList conditions) {
            this.conditions = (conditions == null ? new ConditionList() : conditions);
        }
    }

    public enum Phase {
        READY,
        FAILED
    }
}
