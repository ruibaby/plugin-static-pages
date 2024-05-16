package cc.ryanc.staticpages.service;

import java.nio.file.Path;
import reactor.core.publisher.Mono;

public interface PageFileManager {

    Mono<String> readString(Path path);

    Mono<Void> writeString(Path path, String content);
}
