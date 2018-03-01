package al.copycat.domain.download.source.torrent.service;

import al.copycat.domain.download.source.torrent.exception.TorrentException;
import al.copycat.domain.download.source.torrent.model.FileTorrentSource;
import al.copycat.domain.download.source.torrent.model.MagnetTorrentSource;
import bt.Bt;
import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.dht.DHTConfig;
import bt.dht.DHTModule;
import bt.runtime.BtClient;
import com.google.inject.Module;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.net.MalformedURLException;
import java.util.Optional;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TorrentClient implements Closeable {

	private BtClient client;
	private int interval = 1000; // 1 sec
	private String path;

	private TorrentClient(BtClient client, String path) {
		this.client = client;
		this.path = path;
	}

	public static TorrentClient fromMagnet(MagnetTorrentSource source) {
		Storage storage = new FileSystemStorage(source.getDestination());
		Module dhtModule = new DHTModule(new DHTConfig() {
			@Override public boolean shouldUseRouterBootstrap() {
				return true;
			}
		});
		BtClient client = Bt.client()
			.magnet(source.getSource())
			.storage(storage)
			.autoLoadModules()
			.module(dhtModule)
			.build();
		return new TorrentClient(client, source.getSource());
	}

	public static TorrentClient fromFile(FileTorrentSource source) {
		Storage storage = new FileSystemStorage(source.getDestination());
		Module dhtModule = new DHTModule(new DHTConfig() {
			@Override public boolean shouldUseRouterBootstrap() {
				return true;
			}
		});
		try {
			BtClient client = Bt.client()
				.torrent(source.getSource().toURI().toURL())
				.storage(storage)
				.autoLoadModules()
				.module(dhtModule)
				.stopWhenDownloaded()
				.build();
			return new TorrentClient(client, source.getSource().getAbsolutePath());
		} catch (MalformedURLException e) {
			log.error("Fail to build torrent client: invalid file, {}", source.getSource().getAbsolutePath(), e);
			throw new TorrentException("Fail to build torrent client: invalid file url", e);
		}
	}

	public void startDownload() {
		//FIXME: db에 로그 저장
		Mono.fromFuture(client.startAsync(state -> {
			log.debug("Download torrent({}), remain: {}", path, state.getPiecesRemaining());
			if (state.getPiecesRemaining() == 0) {
				log.debug("Download completed, close client: {}", path);
				close();
			}
		}, interval))
			.subscribe();
	}

	@Override
	public void close() {
		log.info("Close torrent client: {}", path);
		Optional.ofNullable(client)
			.filter(BtClient::isStarted)
			.ifPresent(BtClient::stop);
	}

}
