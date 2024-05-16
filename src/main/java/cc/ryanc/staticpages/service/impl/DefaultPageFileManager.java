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
                validateRegularFile(path);
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

    @Override
    public Mono<Void> writeString(Path path, String content) {
        return Mono.fromRunnable(() -> {
                validateRegularFile(path);
                if (!Files.isWritable(path)) {
                    throw new ServerWebInputException("文件不可写");
                }
                try {
                    Files.writeString(path, content, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new ServerWebInputException("写入文件失败, 请稍后重试");
                }
            })
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }

    private static void validateRegularFile(Path path) {
        if (!Files.isRegularFile(path)) {
            throw new ServerWebInputException("仅支持操作文件类型");
        }
    }
}
