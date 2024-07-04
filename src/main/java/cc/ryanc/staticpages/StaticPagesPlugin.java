package cc.ryanc.staticpages;

import static run.halo.app.extension.index.IndexAttributeFactory.simpleAttribute;

import cc.ryanc.staticpages.extensions.Project;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.index.IndexSpec;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

@Component
public class StaticPagesPlugin extends BasePlugin {
    private final SchemeManager schemeManager;

    public StaticPagesPlugin(PluginContext pluginContext, SchemeManager schemeManager) {
        super(pluginContext);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {
        schemeManager.register(Project.class, indexSpecs -> {
            indexSpecs.add(new IndexSpec()
                .setName(Project.SYNC_ON_STARTUP_INDEX)
                .setIndexFunc(simpleAttribute(Project.class, project -> {
                    var version = project.getMetadata().getVersion();
                    var observedVersion = project.getStatus().getObservedVersion();
                    if (observedVersion == null || observedVersion < version) {
                        return BooleanUtils.TRUE;
                    }
                    // do not care about the false case so return null to avoid indexing
                    return null;
                })));
        });
    }

    @Override
    public void stop() {
        schemeManager.unregister(Scheme.buildFromType(Project.class));
    }
}
