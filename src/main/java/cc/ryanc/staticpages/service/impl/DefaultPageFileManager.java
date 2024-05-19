package cc.ryanc.staticpages.service.impl;

import cc.ryanc.staticpages.service.PageFileManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class DefaultPageFileManager implements PageFileManager {

    @Override
    public Mono<String> readString(Path path) {
        return Mono.fromCallable(() -> {
                if (!Files.isRegularFile(path)) {
                    throw new ServerWebInputException("仅支持读取文件类型");
                }
                if (!Files.isReadable(path)) {
                    throw new ServerWebInputException("文件不可读");
                }
                try {
                    return Files.readString(path, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new ServerWebInputException("此文件类型不支持读取");
                }
            })
            .subscribeOn(Schedulers.boundedElastic());
    }
}
