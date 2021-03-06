package al.copycat.domain.download.source.simple.model;

import al.copycat.domain.download.source.common.model.Source;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor(staticName = "of")
public class MultipartFileSource implements Source<MultipartFile> {

	private MultipartFile source;

}
