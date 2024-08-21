package cc.ryanc.staticpages.service.impl;

import cc.ryanc.staticpages.service.PageFileManager;
import cc.ryanc.staticpages.utils.FileUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
public class DefaultPageFileManager implements PageFileManager {

    private static void validateRegularFile(Path path) {
        if (!Files.isRegularFile(path)) {
            throw new ServerWebInputException("仅支持操作文件类型");
        }
    }

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
                    log.error("Failed to read file", e);
                    throw new ServerWebInputException("此文件类型不支持读取", null, e);
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
                    log.error("Failed to write file", e);
                    throw new ServerWebInputException("写入文件失败, 请稍后重试", null, e);
                }
            })
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }

    @Override
    public Mono<Void> createFile(Path filePath, boolean dir) {
        return Mono.fromRunnable(() -> {
                if (Files.exists(filePath)) {
                    throw new ServerWebInputException("文件已存在");
                }
                try {
                    if (!Files.exists(filePath.getParent())) {
                        Files.createDirectories(filePath.getParent());
                    }
                    if (dir) {
                        Files.createDirectory(filePath);
                    } else {
                        Files.createFile(filePath);
                    }
                } catch (IOException e) {
                    log.error("Failed to create file", e);
                    throw new ServerWebInputException("创建文件失败, 请稍后重试", null, e);
                }
            })
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }

    @Override
    public Mono<Void> move(Path source, Path target) {
        return Mono.fromRunnable(() -> {
                try {
                    FileUtils.ensureEmpty(target);
                } catch (IOException e) {
                    throw new UnsupportedOperationException("目标文件夹不为空, 无法移动文件");
                }
                try {
                    if (!Files.exists(target.getParent())) {
                        Files.createDirectories(target.getParent());
                    }
                    if (Files.isDirectory(source) && !Files.exists(target)) {
                        Files.createDirectories(target);
                    }
                    FileSystemUtils.copyRecursively(source, target);
                    FileSystemUtils.deleteRecursively(source);
                } catch (IOException e) {
                    log.error("Failed to move file", e);
                    throw new IllegalStateException("移动文件或文件夹失败, 请稍后重试", e);
                }
            })
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }
}
