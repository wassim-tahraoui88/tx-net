package com.tahraoui.txnet.core.host;

import com.tahraoui.txnet.core.TXNetObjectStream;
import com.tahraoui.txnet.core.ftp.TXNetFTPHandler;
import com.tahraoui.txnet.packet.ftp.TXNetFileTransferPacket;
import com.tahraoui.txnet.packet.request.TXNetRequestPacket;
import com.tahraoui.txnet.packet.request.TXNetConnectionRequest;
import com.tahraoui.txnet.packet.response.TXNetConnectionResponse;
import com.tahraoui.txnet.exception.TXNetAuthenticationException;
import com.tahraoui.txnet.exception.TXNetException;
import com.tahraoui.txnet.util.TXNetEncryptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;

class TXNetClientHandler implements Runnable {

	private static final Logger LOGGER = LogManager.getLogger(TXNetClientHandler.class);

	private final KeyPair rsaKeyPair;
	private final SecretKey aesKey;
	private final IvParameterSpec iv;

	private final TXNetClientHandlerListener listener;
	private final int id;
	private final String username;
	private final Socket socket;
	private final TXNetObjectStream stream;
	private final TXNetFTPHandler ftp;

	TXNetClientHandler(int id, Socket socket, String password, TXNetEncryptionUtils.XNetKeySpecs keySpecs, TXNetClientHandlerListener listener) throws TXNetException, IOException {
		this.ftp = new TXNetFTPHandler();

		this.rsaKeyPair = keySpecs.rsaKeyPair();
		this.aesKey = keySpecs.aesKey();
		this.iv = keySpecs.iv();

		this.listener = listener;

		this.socket = socket;
		this.stream = TXNetObjectStream.createHostStream(socket.getInputStream(), socket.getOutputStream());
		this.id = id == -1 ? stream.hashCode() : id;

		var request = establishConnection(password);
		this.username = request.getUsername();

		this.listener.onClientConnected(id);
	}

	private TXNetConnectionRequest establishConnection(String password) throws TXNetException {
		var success = false;
		try {
			var request = (TXNetConnectionRequest) this.stream.read();
			if (request == null || !request.getPassword().equals(password)) {
				stream.write(new TXNetConnectionResponse(-1,null,null, null));
				throw new TXNetAuthenticationException();
			}

			var encryptedKey = TXNetEncryptionUtils.encryptRSA(aesKey.getEncoded(), request.getKey());
			var response = new TXNetConnectionResponse(id, rsaKeyPair.getPublic(), encryptedKey, iv.getIV());
			stream.write(response);

			success = true;
			return request;
		}
		finally {
			if (!success) {
				closeStream();
				closeSocket();
			}
		}
	}

	@Override public void run() {
		try {
			while (socket.isConnected() && !socket.isClosed()) {
				try {
					handleRequest();
				}
				catch (NullPointerException e) {
					LOGGER.warn("Failed to handle request: {}", e.getMessage());
				}
				catch (TXNetException e) {
					LOGGER.error("Failed to handle request: {}", e.getMessage());
					closeConnection();
				}
			}
			LOGGER.warn("Socket of Client id {} is closed.", id);
			listener.onClientDisconnected(id);
			Thread.currentThread().interrupt();
		}
		catch (IllegalStateException e) {
			LOGGER.fatal("Socket connection interrupted: {}", e.getMessage());
		}
		finally {
			closeConnection();
		}
	}
	void closeConnection() {
		closeStream();
		closeSocket();
	}
	private void closeStream() {
		try {
			stream.close();
		}
		catch (IOException e) {
			LOGGER.fatal("Failed to close stream: {}", e.getMessage());
		}
	}
	private void closeSocket() {
		try {
			socket.close();
		}
		catch (IOException e) {
			LOGGER.fatal("Failed to close socket: {}", e.getMessage());
		}
	}

	private void handleRequest() throws TXNetException, IllegalStateException, NullPointerException {
		if (listener == null || ftp == null) throw new IllegalStateException("XNetClientHandler's listener is null.");
		var packet = stream.read();
		switch (packet) {
			case TXNetFileTransferPacket ftpPacket -> ftp.handlePacket(ftpPacket);
			case TXNetRequestPacket request -> listener.writeRequestPacket(request);
			case null -> throw new NullPointerException("Received packet is null.");
			default -> { }
		}
	}

	int getId() { return id; }
	String getUsername() { return username; }
	TXNetObjectStream getStream() { return stream; }
}
