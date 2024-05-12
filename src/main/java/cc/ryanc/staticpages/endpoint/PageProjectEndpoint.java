package cc.ryanc.staticpages.endpoint;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.BooleanUtils.toBooleanObject;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

import cc.ryanc.staticpages.model.UploadContext;
import cc.ryanc.staticpages.service.PageProjectService;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.fn.builders.parameter.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
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
                    .parameter(Builder.parameterBuilder()
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
            .build();
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("console.api.staticpage.halo.run/v1alpha1");
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
