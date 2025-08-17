package com.tahraoui.txnet.core.host;

import com.tahraoui.txnet.core.TXNetConnection;
import com.tahraoui.txnet.model.Connection;
import com.tahraoui.txnet.packet.TXNetPacketRequestHandler;
import com.tahraoui.txnet.packet.request.TXNetRequestPacket;
import com.tahraoui.txnet.packet.response.TXNetResponsePacket;
import com.tahraoui.txnet.exception.TXNetException;
import com.tahraoui.txnet.model.UserCredentials;
import com.tahraoui.txnet.util.TXNetEncryptionUtils;
import com.tahraoui.txnet.util.TXNetConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.util.List;

public class TXNetHost implements Runnable, TXNetConnection {

	private static final Logger LOGGER = LogManager.getLogger(TXNetHost.class);

	private TXNetHostListener listener;
	public void setListener(TXNetHostListener listener) { this.listener = listener; }

	private ServerSocket serverSocket;
	private final KeyPair rsaKeyPair;
	private final SecretKey aesKey;
	private final IvParameterSpec iv;

	private final int port;
	private final String username, password;

	private final TXNetClientManager clientManager;

	public TXNetHost(int port, UserCredentials credentials, TXNetPacketRequestHandler packetHandler) throws TXNetException {
		this.aesKey = TXNetEncryptionUtils.generateKey();
		this.rsaKeyPair = TXNetEncryptionUtils.generateKeyPair();
		this.iv = TXNetEncryptionUtils.generateIV();

		this.port = port;
		this.username = credentials.username();
		this.password = credentials.password();

		this.clientManager = new TXNetClientManager(packetHandler, response -> listener.readResponsePacket(response));
	}
	private TXNetClientHandler createClientHandler(Socket socket) throws TXNetException, IOException {
		var handlerListener = new TXNetClientHandlerListener() {
			@Override public void onClientConnected(int id) { listener.onClientConnectionEstablished(id); }
			@Override public void onClientDisconnected(int id) {
				clientManager.remove(id);
				listener.onClientConnectionTerminated(id);
			}
			@Override public void writeRequestPacket(TXNetRequestPacket request) { clientManager.receivePacket(request); }
			@Override public void readResponsePacket(TXNetResponsePacket packet) {}
		};
		int id = -1;
		var generatedId = TXNetConfig.getInstance().getGeneratedId();
		if (generatedId == TXNetConfig.GeneratedId.SEQ) id = clientManager.getNextId();
		var handler = new TXNetClientHandler(id, socket, password, new TXNetEncryptionUtils.XNetKeySpecs(rsaKeyPair, aesKey, iv), handlerListener);
		clientManager.add(handler.getId(), handler);
		return handler;
	}

	@Override public void run() {
		try (var serverSocket = new ServerSocket(port)) {
			this.serverSocket = serverSocket;
			while (!serverSocket.isClosed()) {
				try {
					var socket = serverSocket.accept();
					LOGGER.info("Client {} has connected.", createClientHandler(socket).getId());
				}
				catch (TXNetException e) {
					LOGGER.error(e.getMessage());
				}
				catch (IOException e) {
					LOGGER.fatal("An internal error has occurred while establishing connection: {}.", e.getMessage());
				}
			}
		}
		catch (IOException _) {
			LOGGER.error("Server is shutdown.");
		}
	}

	@Override public void disconnect() {
		clientManager.close();
		try {
			serverSocket.close();
		}
		catch (IOException _) {}
	}
	@Override public void disconnect(int id) {
		clientManager.disconnect(id);
		listener.onClientConnectionTerminated(id);
	}
	@Override public void writeRequestPacket(TXNetRequestPacket request) {
		LOGGER.debug("Sending packet {}.", request);
		clientManager.receivePacket(request);
	}

	@Override public int getId() { return 0; }
	@Override public String getUsername() { return username; }
	@Override public List<Connection> getConnections() { return clientManager.getConnections(); }
}
