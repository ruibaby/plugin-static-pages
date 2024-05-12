package cc.ryanc.staticpages.service;

import cc.ryanc.staticpages.model.UploadContext;
import java.nio.file.Path;
import reactor.core.publisher.Mono;

public interface PageProjectService {

    Mono<Path> upload(UploadContext uploadContext);
}
