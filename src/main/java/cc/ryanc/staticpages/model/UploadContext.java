package cc.ryanc.staticpages.model;

import lombok.Builder;
import lombok.Value;
import org.springframework.http.codec.multipart.FilePart;

@Value
@Builder
public class UploadContext {
    String name;
    FilePart filePart;
    boolean unzip;
    String dir;
}
