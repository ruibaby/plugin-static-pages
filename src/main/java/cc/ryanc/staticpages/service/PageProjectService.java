package cc.ryanc.staticpages.service;

import cc.ryanc.staticpages.model.ProjectFile;
import cc.ryanc.staticpages.model.UploadContext;
import java.nio.file.Path;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PageProjectService {

    Mono<Path> upload(UploadContext uploadContext);

    Flux<ProjectFile> listFiles(String projectName, String directoryPath);

    Mono<Boolean> deleteFile(String projectName, String path);

    Mono<String> readFileContent(String projectName, String path);

    Mono<Void> writeContent(String projectName, String path, String content);
}
