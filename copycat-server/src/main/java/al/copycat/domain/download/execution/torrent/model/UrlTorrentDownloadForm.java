package al.copycat.domain.download.execution.torrent.model;

import al.copycat.domain.download.source.torrent.model.UrlTorrentSource;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.file.Path;

@Getter
@AllArgsConstructor(staticName = "of")
public class UrlTorrentDownloadForm implements TorrentDownloadForm<UrlTorrentSource> {

	private UrlTorrentSource from;
	private Path torrentFileDownloadTo;
	private Path torrentContentDownloadTo;

}
