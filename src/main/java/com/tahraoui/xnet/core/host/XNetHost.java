package com.tahraoui.xnet.core.host;

import com.tahraoui.xnet.core.XNetConnection;
import com.tahraoui.xnet.model.Connection;
import com.tahraoui.xnet.packet.XNetPacketRequestHandler;
import com.tahraoui.xnet.packet.request.XNetRequestPacket;
import com.tahraoui.xnet.packet.response.XNetResponsePacket;
import com.tahraoui.xnet.exception.XNetException;
import com.tahraoui.xnet.model.UserCredentials;
import com.tahraoui.xnet.util.EncryptionUtils;
import com.tahraoui.xnet.util.XNetConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.util.List;

public class XNetHost implements Runnable, XNetConnection {

	private static final Logger LOGGER = LogManager.getLogger(XNetHost.class);

	private XNetHostListener listener;
	public void setListener(XNetHostListener listener) { this.listener = listener; }

	private ServerSocket serverSocket;
	private final KeyPair rsaKeyPair;
	private final SecretKey aesKey;
	private final IvParameterSpec iv;

	private final int port;
	private final String username, password;

	private final XNetClientManager clientManager;

	public XNetHost(int port, UserCredentials credentials, XNetPacketRequestHandler packetHandler) throws XNetException {
		this.aesKey = EncryptionUtils.generateKey();
		this.rsaKeyPair = EncryptionUtils.generateKeyPair();
		this.iv = EncryptionUtils.generateIV();

		this.port = port;
		this.username = credentials.username();
		this.password = credentials.password();

		this.clientManager = new XNetClientManager(packetHandler, response -> listener.readResponsePacket(response));
	}
	private XNetClientHandler createClientHandler(Socket socket) throws XNetException, IOException {
		var handlerListener = new XNetClientHandlerListener() {
			@Override public void onClientConnected(int id) { listener.onClientConnectionEstablished(id); }
			@Override public void onClientDisconnected(int id) {
				clientManager.remove(id);
				listener.onClientConnectionTerminated(id);
			}
			@Override public void writeRequestPacket(XNetRequestPacket request) { clientManager.receivePacket(request); }
			@Override public void readResponsePacket(XNetResponsePacket packet) {}
		};
		int id = -1;
		var generatedId = XNetConfig.getInstance().getGeneratedId();
		if (generatedId == XNetConfig.GeneratedId.SEQ) id = clientManager.getNextId();
		var handler = new XNetClientHandler(id, socket, password, new EncryptionUtils.XNetKeySpecs(rsaKeyPair, aesKey, iv), handlerListener);
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
				catch (XNetException e) {
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
	@Override public void writeRequestPacket(XNetRequestPacket request) {
		LOGGER.debug("Sending packet {}.", request);
		clientManager.receivePacket(request);
	}

	@Override public int getId() { return 0; }
	@Override public String getUsername() { return username; }
	@Override public List<Connection> getConnections() { return clientManager.getConnections(); }
}
