package com.tahraoui.txnet.core;

import com.tahraoui.txnet.core.client.TXNetClient;
import com.tahraoui.txnet.core.client.TXNetClientListener;
import com.tahraoui.txnet.core.ftp.TXNetFTPDispatcher;
import com.tahraoui.txnet.core.host.TXNetHost;
import com.tahraoui.txnet.core.host.TXNetHostListener;
import com.tahraoui.txnet.model.Connection;
import com.tahraoui.txnet.model.UserCredentials;
import com.tahraoui.txnet.packet.TXNetPacketRequestHandler;
import com.tahraoui.txnet.packet.ftp.EOFBufferTransfer;
import com.tahraoui.txnet.packet.ftp.FileMetaDataTransfer;
import com.tahraoui.txnet.packet.request.TXNetRequestPacket;
import com.tahraoui.txnet.packet.response.TXNetResponsePacket;
import com.tahraoui.txnet.exception.TXNetException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class TXNetService {
	private static final Logger LOGGER = LogManager.getLogger(TXNetService.class);

	private static TXNetService instance;
	public static TXNetService getInstance() {
		if (instance == null) instance = new TXNetService();
		return instance;
	}

	public boolean isHost;
	public boolean isClient;
	private TXNetConnection connection;
	private TXNetServiceListener listener;
	private TXNetPacketRequestHandler handler;
	private TXNetFTPDispatcher ftpDispatcher;

	private TXNetService() {}

	/**
	 * Initializes the XNetService with the given packet handler and listener.
	 * @param handler the server-side packet handler to handle incoming request packets.
	 * @param listener the service listener implementation to handle returned response packets.
	 */
	public static void init(TXNetPacketRequestHandler handler, TXNetServiceListener listener) {
		getInstance().handler = handler;
		getInstance().listener = listener;
	}

	//region Host/Client Connection
	public static void host(int port, UserCredentials credentials) { getInstance().hostImpl(port, credentials); }
	public static void join(int port, UserCredentials credentials) { getInstance().joinImpl(port, credentials); }
	public static void disconnect() { getInstance().disconnectImpl(); }
	public static void disconnect(int clientId) { getInstance().disconnectImpl(clientId); }

	private void hostImpl(int port, UserCredentials credentials) {
		if (isConnected()) {
			LOGGER.warn("Host already started.");
			return;
		}

		try {
			var host = createHost(port, credentials);
			this.connection = host;
			new Thread(host,"Thread - Host").start();
		}
		catch (TXNetException e) {
			disconnectImpl();
			LOGGER.error(e.getMessage());
			return;
		}
		catch (Exception e) {
			disconnectImpl();
			LOGGER.fatal(e.getMessage());
			return;
		}

		this.isHost = true;
		this.listener.onConnected();
		LOGGER.info("Host started on port {}.", port);
	}
	private void joinImpl(int port, UserCredentials credentials) {
		if (isConnected()) {
			LOGGER.warn("Client already connected.");
			return;
		}

		try {
			this.connection = createClient(port, credentials);
		}
		catch (TXNetException e) {
			disconnectImpl();
			LOGGER.error(e.getMessage());
			return;
		}
		catch (IOException _) {
			disconnectImpl();
			LOGGER.fatal("An error has occurred while connecting.");
			return;
		}

		this.isClient = true;
		this.listener.onConnected();
		LOGGER.debug("Connected to server on port {} with id {}.", port, getId());
	}
	private void disconnectImpl() {
		this.isHost = false;
		this.isClient = false;
		listener.onDisconnected();
		if (connection != null) connection.disconnect();
		connection = null;
	}
	private void disconnectImpl(int clientId) {
		if (!isHost) {
			LOGGER.error("Cannot disconnect client, not a host.");
			return;
		}
		connection.disconnect(clientId);
		LOGGER.debug("Disconnected client with id {}.", clientId);
	}
	//endregion

	//region Request/FTP Handlers
	public static void sendRequestPacket(TXNetRequestPacket request) { instance.connection.writeRequestPacket(request); }
	public static void sendFileMetaData(int id, FileMetaDataTransfer metadata) { instance.ftpDispatcher.onFileMetaDataSent(id, metadata); }
	public static void sendFileTransfer(int id, EOFBufferTransfer eof) { instance.ftpDispatcher.onFileTransferComplete(id, eof); }
	//endregion

	//region Aux Methods
	private TXNetHost createHost(int port, UserCredentials credentials) {
		var host = new TXNetHost(port, credentials, handler);
		host.setListener(new TXNetHostListener() {
			@Override public void onClientConnectionEstablished(int clientId) {
				LOGGER.debug("Client connected with id {}.", clientId);
			}
			@Override public void onClientConnectionTerminated(int clientId) {
				LOGGER.debug("Client disconnected with id {}.", clientId);

			}
			@Override public void writeRequestPacket(TXNetRequestPacket packet) { listener.writeRequestPacket(packet); }
			@Override public void readResponsePacket(TXNetResponsePacket packet) { listener.readResponsePacket(packet); }
		});
		return host;
	}
	private TXNetClient createClient(int port, UserCredentials credentials) throws IOException {
		var client = new TXNetClient(port, credentials);
		client.setListener(new TXNetClientListener() {
			@Override public void writeRequestPacket(TXNetRequestPacket request) { listener.writeRequestPacket(request); }
			@Override public void readResponsePacket(TXNetResponsePacket packet) { listener.readResponsePacket(packet); }
		});
		return client;
	}
	//endregion

	//region Getters
	public List<Connection> getConnections() {
		if (!isHost) LOGGER.error("Cannot get connections, not a host.");
		return connection.getConnections();
	}
	public int getId() { return connection.getId(); }
	public String getUsername() { return connection.getUsername(); }
	public boolean isConnected() { return isHost || isClient; }
	//endregion
}
