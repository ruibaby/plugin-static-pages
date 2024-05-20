package cc.ryanc.staticpages.endpoint;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.BooleanUtils.toBooleanObject;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

import cc.ryanc.staticpages.model.ProjectFile;
import cc.ryanc.staticpages.model.UploadContext;
import cc.ryanc.staticpages.service.PageProjectService;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

@RequiredArgsConstructor
@Component
public class PageProjectEndpoint implements CustomEndpoint {
    private final PageProjectService pageProjectService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "console.api.staticpage.halo.run/v1alpha1/Project";
        return SpringdocRouteBuilder.route()
            .POST("/projects/{name}/upload", contentType(MediaType.MULTIPART_FORM_DATA),
                request -> request.body(BodyExtractors.toMultipartData())
                    .map(UploadRequest::new)
                    .flatMap(uploadReq -> {
                        var context = UploadContext.builder()
                            .name(request.pathVariable("name"))
                            .unzip(uploadReq.getUnzip())
                            .filePart(uploadReq.getFile())
                            .dir(uploadReq.getDir())
                            .build();
                        return pageProjectService.upload(context);
                    })
                    .flatMap(path -> ServerResponse.ok().bodyValue(path)),
                builder -> builder
                    .operationId("UploadFileToProject")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("name")
                        .required(true)
                    )
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .schema(schemaBuilder().implementation(UploadRequest.class))
                        ))
                    .response(responseBuilder().implementation(Path.class))
                    .build()
            )
            .GET("/projects/{name}/files", this::listFiles, builder -> builder
                .operationId("ListFilesInProject")
                .tag(tag)
                .parameter(parameterBuilder()
                    .in(ParameterIn.PATH)
                    .name("name")
                    .required(true)
                )
                .parameter(parameterBuilder()
                    .in(ParameterIn.QUERY)
                    .required(false)
                    .name("path")
                )
                .response(responseBuilder().implementation(String.class))
            )
            .DELETE("/projects/{name}/files", request -> {
                    var projectName = request.pathVariable("name");
                    var path = request.queryParam("path").orElse(null);
                    return pageProjectService.deleteFile(projectName, path)
                        .flatMap(result -> ServerResponse.ok().bodyValue(result));
                }, builder -> builder
                    .operationId("DeleteFileInProject")
                    .description("Delete file or directory in project by given path")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("name")
                        .required(true)
                    )
                    .parameter(parameterBuilder()
                        .in(ParameterIn.QUERY)
                        .required(false)
                        .name("path")
                    )
                    .response(responseBuilder().implementation(Boolean.class))
            )
            .GET("/projects/{name}/file-content", this::getFileContent, builder -> builder
                .operationId("GetFileContent")
                .tag(tag)
                .parameter(parameterBuilder()
                    .in(ParameterIn.PATH)
                    .name("name")
                    .required(true)
                )
                .parameter(parameterBuilder()
                    .in(ParameterIn.QUERY)
                    .required(false)
                    .name("path")
                )
                .response(responseBuilder().implementation(String.class))
            )
            .PUT("/projects/{name}/file-content", this::writeContentToFile, builder -> builder
                .operationId("WriteContentToFile")
                .tag(tag)
                .parameter(parameterBuilder()
                    .in(ParameterIn.PATH)
                    .name("name")
                    .required(true)
                )
                .parameter(parameterBuilder()
                    .in(ParameterIn.QUERY)
                    .required(false)
                    .name("path")
                )
                .requestBody(requestBodyBuilder()
                    .implementation(WriteContentRequest.class)
                )
                .response(responseBuilder()
                    .responseCode(String.valueOf(HttpStatus.NO_CONTENT.value())))
            )
            .build();
    }

    private Mono<ServerResponse> writeContentToFile(ServerRequest request) {
        final var projectName = request.pathVariable("name");
        final var path = request.queryParam("path").orElse("/");
        return request.bodyToMono(WriteContentRequest.class)
            .switchIfEmpty(Mono.error(new ServerWebInputException("Required body is missing.")))
            .flatMap(req -> pageProjectService.writeContent(projectName, path, req.content()))
            .then(ServerResponse.noContent().build());
    }

    private Mono<ServerResponse> getFileContent(ServerRequest request) {
        final var projectName = request.pathVariable("name");
        final var path = request.queryParam("path").orElse("/");
        return pageProjectService.readFileContent(projectName, path)
            .flatMap(content -> ServerResponse.ok().bodyValue(content));
    }

    private Mono<ServerResponse> listFiles(ServerRequest serverRequest) {
        final var projectName = serverRequest.pathVariable("name");
        final var path = serverRequest.queryParam("path").orElse(null);
        var files = pageProjectService.listFiles(projectName, path);
        return ServerResponse.ok().body(files, ProjectFile.class);
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("console.api.staticpage.halo.run/v1alpha1");
    }

    public record WriteContentRequest(@Schema(requiredMode = REQUIRED) String content) {
    }

    public record UploadRequest(MultiValueMap<String, Part> formData) {
        @Schema(requiredMode = REQUIRED)
        public FilePart getFile() {
            var part = formData.getFirst("file");
            if (part instanceof FilePart filePart) {
                return filePart;
            }
            throw new ServerWebInputException("Required 'file' param is missing.");
        }

        @Schema(requiredMode = NOT_REQUIRED, pattern = "^(?:/[\\w\\-.~!$&'()*+,;=:@%]+)*$",
            description = "Segments of the path, relative to the current static page, are divided"
                + " by slashes to form multiple subpaths")
        public String getDir() {
            if (formData.getFirst("dir") instanceof FormFieldPart form) {
                return form.value();
            }
            return null;
        }

        public boolean getUnzip() {
            if (formData.getFirst("unzip") instanceof FormFieldPart form) {
                return isTrue(toBooleanObject(form.value()));
            }
            return false;
        }
    }
}
