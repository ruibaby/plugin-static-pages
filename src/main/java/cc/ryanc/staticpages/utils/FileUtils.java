package cc.ryanc.staticpages.utils;

import static cc.ryanc.staticpages.utils.DataBufferUtils.toInputStream;
import static org.springframework.util.FileSystemUtils.copyRecursively;
import static org.springframework.util.FileSystemUtils.deleteRecursively;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@UtilityClass
public class FileUtils {

    public static Mono<Void> unzipTo(Publisher<DataBuffer> content, Path storePath) {
        return Mono.usingWhen(createTempDir("halo-staticpage"),
                tempDir -> unzip(content, tempDir)
                    .then(Mono.defer(() -> {
                        try {
                            copyRecursively(tempDir, storePath);
                        } catch (IOException e) {
                            deleteRecursivelyAndSilently(tempDir);
                            return Mono.error(e);
                        }
                        return Mono.empty();
                    }))
                    .subscribeOn(Schedulers.boundedElastic()),
                tempDir -> Mono.fromSupplier(() -> {
                    try {
                        return FileSystemUtils.deleteRecursively(tempDir);
                    } catch (IOException ignored) {
                        return false;
                    }
                }).subscribeOn(Schedulers.boundedElastic())
            )
            .then();
    }

    public static Mono<Path> createTempDir(String prefix) {
        return Mono.fromCallable(() -> Files.createTempDirectory(prefix))
            .subscribeOn(Schedulers.boundedElastic());
    }

    public static Mono<Void> unzip(Publisher<DataBuffer> content, @NonNull Path targetPath) {
        return Mono.usingWhen(
            toInputStream(content),
            is -> {
                try (var zis = new ZipInputStream(is)) {
                    FileUtils.unzip(zis, targetPath);
                    return Mono.empty();
                } catch (IOException e) {
                    return Mono.error(e);
                }
            },
            is -> Mono.fromRunnable(() -> FileUtils.closeQuietly(is))
        );
    }

    public static void unzip(@NonNull ZipInputStream zis, @NonNull Path targetPath)
        throws IOException {
        // 1. unzip file to folder
        // 2. return the folder path
        Assert.notNull(zis, "Zip input stream must not be null");
        Assert.notNull(targetPath, "Target path must not be null");

        // Create a path if absent
        createIfAbsent(targetPath);

        // The Folder must be empty
        ensureEmpty(targetPath);

        ZipEntry zipEntry = zis.getNextEntry();

        while (zipEntry != null) {
            // Resolve the entry path
            Path entryPath = targetPath.resolve(zipEntry.getName());

            checkDirectoryTraversal(targetPath, entryPath);

            if (Files.notExists(entryPath.getParent())) {
                Files.createDirectories(entryPath.getParent());
            }

            if (zipEntry.isDirectory()) {
                // Create directory
                Files.createDirectory(entryPath);
            } else {
                // Copy file
                Files.copy(zis, entryPath);
            }

            zipEntry = zis.getNextEntry();
        }
    }

    /**
     * Creates directories if absent.
     *
     * @param path path must not be null
     * @throws IOException io exception
     */
    public static void createIfAbsent(@NonNull Path path) throws IOException {
        Assert.notNull(path, "Path must not be null");

        if (Files.notExists(path)) {
            // Create directories
            Files.createDirectories(path);

            log.debug("Created directory: [{}]", path);
        }
    }

    /**
     * The given path must be empty.
     *
     * @param path path must not be null
     * @throws IOException io exception
     */
    public static void ensureEmpty(@NonNull Path path) throws IOException {
        if (!isEmpty(path)) {
            throw new DirectoryNotEmptyException("Target directory: " + path + " was not empty");
        }
    }

    /**
     * Checks if the given path is empty.
     *
     * @param path path must not be null
     * @return true if the given path is empty; false otherwise
     * @throws IOException io exception
     */
    public static boolean isEmpty(@NonNull Path path) throws IOException {
        Assert.notNull(path, "Path must not be null");

        if (!Files.isDirectory(path) || Files.notExists(path)) {
            return true;
        }

        try (Stream<Path> pathStream = Files.list(path)) {
            return pathStream.findAny().isEmpty();
        }
    }

    public static void deleteRecursivelyAndSilently(Path root) {
        try {
            var deleted = deleteRecursively(root);
            if (log.isDebugEnabled()) {
                log.debug("Delete {} result: {}", root, deleted);
            }
        } catch (IOException ignored) {
            // Ignore this error
        }
    }

    public static void checkDirectoryTraversal(@NonNull Path parentPath,
        @NonNull Path pathToCheck) {
        Assert.notNull(parentPath, "Parent path must not be null");
        Assert.notNull(pathToCheck, "Path to check must not be null");

        if (pathToCheck.normalize().startsWith(parentPath)) {
            return;
        }

        throw new ServerWebInputException("Directory traversal detected: " + pathToCheck);
    }

    public static Mono<Boolean> deleteFileSilently(Path file) {
        return Mono.fromSupplier(
                () -> {
                    if (file == null || !Files.isRegularFile(file)) {
                        return false;
                    }
                    try {
                        return Files.deleteIfExists(file);
                    } catch (IOException ignored) {
                        return false;
                    }
                })
            .subscribeOn(Schedulers.boundedElastic());
    }

    public static void closeQuietly(final Closeable closeable) {
        closeQuietly(closeable, null);
    }

    public static void closeQuietly(final Closeable closeable,
        final Consumer<IOException> consumer) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                if (consumer != null) {
                    consumer.accept(e);
                }
            }
        }
    }
}
