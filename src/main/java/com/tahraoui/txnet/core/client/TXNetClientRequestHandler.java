package com.tahraoui.txnet.core.client;

import com.tahraoui.txnet.core.TXNetConnection;
import com.tahraoui.txnet.core.TXNetObjectStream;
import com.tahraoui.txnet.core.TXNetService;
import com.tahraoui.txnet.exception.TXNetEncryptionException;
import com.tahraoui.txnet.exception.TXNetReadingException;
import com.tahraoui.txnet.model.Connection;
import com.tahraoui.txnet.model.UserCredentials;
import com.tahraoui.txnet.packet.request.TXNetPacketEncrypted;
import com.tahraoui.txnet.packet.request.TXNetRequestPacket;
import com.tahraoui.txnet.packet.response.TXNetResponsePacket;
import com.tahraoui.txnet.packet.request.TXNetConnectionRequest;
import com.tahraoui.txnet.packet.response.TXNetConnectionResponse;
import com.tahraoui.txnet.exception.TXNetConnectionFailedException;
import com.tahraoui.txnet.exception.TXNetException;
import com.tahraoui.txnet.exception.TXNetAuthenticationException;
import com.tahraoui.txnet.util.TXNetEncryptionUtils;
import com.tahraoui.txnet.util.TXNetConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;
import java.util.List;

class TXNetClientRequestHandler implements Runnable, TXNetConnection {

	private static final Logger LOGGER = LogManager.getLogger(TXNetClientRequestHandler.class);

	protected TXNetClientRequestHandlerListener listener;
	public void setListener(TXNetClientRequestHandlerListener listener) { this.listener = listener; }

	private final int id;
	private final String username;

	private final KeyPair rsaKeyPair;
	private final SecretKey aesKey;
	private final IvParameterSpec iv;

	private final Socket socket;
	private final TXNetObjectStream stream;

	TXNetClientRequestHandler(Socket socket, UserCredentials credentials) throws TXNetException, IOException {
		this.rsaKeyPair = TXNetEncryptionUtils.generateKeyPair();

		this.socket = socket;
		this.stream = TXNetObjectStream.createClientStream(socket.getOutputStream(), socket.getInputStream());

		var response = (TXNetConnectionResponse) null;
		try {
			response = establishConnection(credentials);
		}
		catch (TXNetException e) {
			closeConnection();
			throw new TXNetConnectionFailedException();
		}

		this.id = response.getId();
		this.username = credentials.username();
		this.aesKey = TXNetEncryptionUtils.decryptRSA(response.getSecret(), rsaKeyPair.getPrivate());
		this.iv = new IvParameterSpec(response.getIv());
	}

	private TXNetConnectionResponse establishConnection(UserCredentials credentials) throws TXNetException {

		stream.write(new TXNetConnectionRequest(credentials.username(), credentials.password(), rsaKeyPair.getPublic()));

		var response = (TXNetConnectionResponse) this.stream.read();
		if (response == null) throw new TXNetConnectionFailedException();
		else if (!response.isSuccessful()) throw new TXNetAuthenticationException();

		LOGGER.info("Connection established.");
		return response;
	}

	@Override public void run() {
		try {
			while (socket.isConnected() && !socket.isClosed()) handleResponse();
			TXNetService.disconnect();
		}
		finally {
			closeConnection();
		}
	}

	private void handleResponse() {
		try {
			var packet = stream.read();
			if (!socket.isConnected() || socket.isClosed()) return;
			if (packet == null) throw new TXNetReadingException();
			if (TXNetConfig.getInstance().isSecurityEnabled()) {
				if (packet instanceof TXNetPacketEncrypted encrypted) {
					var decryptedData = encrypted.decrypt(aesKey, iv);
					if (decryptedData instanceof TXNetResponsePacket response) listener.readResponsePacket(response);
					else throw new TXNetEncryptionException("Failed to decrypt packet.");
				}
				else throw new TXNetEncryptionException("Security is enabled but the received packet is not encrypted.");
			}
			else {
				if (packet instanceof TXNetResponsePacket response) listener.readResponsePacket(response);
				else throw new TXNetReadingException();
			}
		}
		catch (TXNetEncryptionException e) {
			LOGGER.error(e.getMessage(), e);
		}
		catch (TXNetReadingException e) {
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

	@Override public void writeRequestPacket(TXNetRequestPacket packet) {
		var data = TXNetConfig.getInstance().isSecurityEnabled() ? packet.encrypt(aesKey, iv) : packet;
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
