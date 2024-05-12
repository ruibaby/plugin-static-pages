package cc.ryanc.staticpages.service.impl;

import static cc.ryanc.staticpages.utils.FileUtils.checkDirectoryTraversal;
import static cc.ryanc.staticpages.utils.FileUtils.deleteFileSilently;
import static java.nio.file.StandardOpenOption.CREATE_NEW;

import cc.ryanc.staticpages.extensions.Project;
import cc.ryanc.staticpages.model.UploadContext;
import cc.ryanc.staticpages.service.PageProjectService;
import cc.ryanc.staticpages.utils.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.BackupRootGetter;

@Component
@RequiredArgsConstructor
public class PageProjectServiceImpl implements PageProjectService {
    private final ReactiveExtensionClient client;
    private final BackupRootGetter backupRootGetter;

    @Override
    public Mono<Path> upload(UploadContext uploadContext) {
        return client.get(Project.class, uploadContext.getName())
            .map(staticPage -> {
                var dist = staticPage.getSpec().getDirectory();
                return determineStorePath(dist, uploadContext.getDir());
            })
            .flatMap(storePath -> writeToFile(storePath, uploadContext));
    }

    Mono<Path> writeToFile(Path storePath, UploadContext uploadContext) {
        return Mono.fromCallable(() -> {
                try {
                    Files.createDirectories(storePath);
                    return storePath;
                } catch (IOException e) {
                    throw Exceptions.propagate(e);
                }
            })
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(rootPath -> {
                if (uploadContext.isUnzip()) {
                    return FileUtils.unzipTo(uploadContext.getFilePart().content(), rootPath)
                        .thenReturn(rootPath);
                }
                var filePath = rootPath.resolve(uploadContext.getFilePart().filename());
                return writeToFile(uploadContext.getFilePart().content(), filePath);
            });
    }

    private Mono<Path> writeToFile(Flux<DataBuffer> content, Path targetPath) {
        return Mono.defer(// we have to use defer method to obtain a fresh path
                () -> DataBufferUtils.write(content, targetPath, CREATE_NEW))
            // Delete file already wrote partially into attachment folder
            // in case of content is terminated with an error
            .onErrorResume(t -> deleteFileSilently(targetPath).then(Mono.error(t)))
            .thenReturn(targetPath);
    }

    Path determineStorePath(@NonNull String dist, String dir) {
        Assert.notNull(dist, "The dist must not be null.");

        var uploadRoot = getStaticRootPath();
        var storePath = StringUtils.isBlank(dir) ? uploadRoot.resolve(dist)
            : uploadRoot.resolve(dir);

        if (StringUtils.isNotBlank(dir)) {
            var segments = StringUtils.split(dir, "/");
            for (String segment : segments) {
                storePath = storePath.resolve(segment);
            }
        }

        checkDirectoryTraversal(uploadRoot, storePath);
        return storePath;
    }

    Path getStaticRootPath() {
        return backupRootGetter.get().getParent().resolve("static");
    }
}
