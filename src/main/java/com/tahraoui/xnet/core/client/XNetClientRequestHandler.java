package com.tahraoui.xnet.core.client;

import com.tahraoui.xnet.core.XNetConnection;
import com.tahraoui.xnet.core.XNetObjectStream;
import com.tahraoui.xnet.core.XNetService;
import com.tahraoui.xnet.exception.XNetEncryptionException;
import com.tahraoui.xnet.exception.XNetReadingException;
import com.tahraoui.xnet.model.Connection;
import com.tahraoui.xnet.model.UserCredentials;
import com.tahraoui.xnet.packet.request.XNetPacketEncrypted;
import com.tahraoui.xnet.packet.request.XNetRequestPacket;
import com.tahraoui.xnet.packet.response.XNetResponsePacket;
import com.tahraoui.xnet.packet.request.XNetConnectionRequest;
import com.tahraoui.xnet.packet.response.XNetConnectionResponse;
import com.tahraoui.xnet.exception.XNetConnectionFailedException;
import com.tahraoui.xnet.exception.XNetException;
import com.tahraoui.xnet.exception.XNetAuthenticationException;
import com.tahraoui.xnet.util.EncryptionUtils;
import com.tahraoui.xnet.util.XNetConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;
import java.util.List;

class XNetClientRequestHandler implements Runnable, XNetConnection {

	private static final Logger LOGGER = LogManager.getLogger(XNetClientRequestHandler.class);

	protected XNetClientRequestHandlerListener listener;
	public void setListener(XNetClientRequestHandlerListener listener) { this.listener = listener; }

	private final int id;
	private final String username;

	private final KeyPair rsaKeyPair;
	private final SecretKey aesKey;
	private final IvParameterSpec iv;

	private final Socket socket;
	private final XNetObjectStream stream;

	XNetClientRequestHandler(Socket socket, UserCredentials credentials) throws XNetException, IOException {
		this.rsaKeyPair = EncryptionUtils.generateKeyPair();

		this.socket = socket;
		this.stream = XNetObjectStream.createClientStream(socket.getOutputStream(), socket.getInputStream());

		var response = (XNetConnectionResponse) null;
		try {
			response = establishConnection(credentials);
		}
		catch (XNetException e) {
			closeConnection();
			throw new XNetConnectionFailedException();
		}

		this.id = response.getId();
		this.username = credentials.username();
		this.aesKey = EncryptionUtils.decryptRSA(response.getSecret(), rsaKeyPair.getPrivate());
		this.iv = new IvParameterSpec(response.getIv());
	}

	private XNetConnectionResponse establishConnection(UserCredentials credentials) throws XNetException {

		stream.write(new XNetConnectionRequest(credentials.username(), credentials.password(), rsaKeyPair.getPublic()));

		var response = (XNetConnectionResponse) this.stream.read();
		if (response == null) throw new XNetConnectionFailedException();
		else if (!response.isSuccessful()) throw new XNetAuthenticationException();

		LOGGER.info("Connection established.");
		return response;
	}

	@Override public void run() {
		try {
			while (socket.isConnected() && !socket.isClosed()) handleResponse();
			XNetService.disconnect();
		}
		finally {
			closeConnection();
		}
	}

	private void handleResponse() {
		try {
			var packet = stream.read();
			if (!socket.isConnected() || socket.isClosed()) return;
			if (packet == null) throw new XNetReadingException();
			if (XNetConfig.getInstance().isSecurityEnabled()) {
				if (packet instanceof XNetPacketEncrypted encrypted) {
					var decryptedData = encrypted.decrypt(aesKey, iv);
					if (decryptedData instanceof XNetResponsePacket response) listener.readResponsePacket(response);
					else throw new XNetEncryptionException("Failed to decrypt packet.");
				}
				else throw new XNetEncryptionException("Security is enabled but the received packet is not encrypted.");
			}
			else {
				if (packet instanceof XNetResponsePacket response) listener.readResponsePacket(response);
				else throw new XNetReadingException();
			}
		}
		catch (XNetEncryptionException e) {
			LOGGER.error(e.getMessage(), e);
		}
		catch (XNetReadingException e) {
			LOGGER.fatal("Failed to handle response: {}", e.getMessage());
			closeConnection();
		}
	}

	void closeConnection() {
		closeStream();
		closeSocket();
	}
	void closeStream() {
		try {
			stream.close();
			LOGGER.debug("Stream of client {} closed.", id);
		}
		catch (IOException e) {
			LOGGER.fatal("Failed to close stream: {}", e.getMessage());
		}
	}
	void closeSocket() {
		try {
			socket.close();
			LOGGER.debug("Socket of client {} closed.", id);
		}
		catch (IOException e) {
			LOGGER.fatal("Failed to close socket: {}", e.getMessage());
		}
	}

	@Override public void writeRequestPacket(XNetRequestPacket packet) {
		var data = XNetConfig.getInstance().isSecurityEnabled() ? packet.encrypt(aesKey, iv) : packet;
		stream.write(data);
	}
	@Override public void disconnect() {}
	@Override public void disconnect(int id) {}


	@Override public int getId() { return id; }
	@Override public String getUsername() { return username; }
	@Override public List<Connection> getConnections() { return null; }

	KeyPair getRSAKeyPair() { return rsaKeyPair; }
	SecretKey getAESKey() { return aesKey; }
	IvParameterSpec getIV() { return iv; }
}
