package com.tahraoui.xnet.core.host;

import com.tahraoui.xnet.core.XNetObjectStream;
import com.tahraoui.xnet.core.ftp.XNetFTPHandler;
import com.tahraoui.xnet.packet.ftp.XNetFileTransferPacket;
import com.tahraoui.xnet.packet.request.XNetRequestPacket;
import com.tahraoui.xnet.packet.request.XNetConnectionRequest;
import com.tahraoui.xnet.packet.response.XNetConnectionResponse;
import com.tahraoui.xnet.exception.XNetAuthenticationException;
import com.tahraoui.xnet.exception.XNetException;
import com.tahraoui.xnet.util.EncryptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;

class XNetClientHandler implements Runnable {

	private static final Logger LOGGER = LogManager.getLogger(XNetClientHandler.class);

	private final KeyPair rsaKeyPair;
	private final SecretKey aesKey;
	private final IvParameterSpec iv;

	private final XNetClientHandlerListener listener;
	private final int id;
	private final String username;
	private final Socket socket;
	private final XNetObjectStream stream;
	private final XNetFTPHandler ftp;

	XNetClientHandler(int id, Socket socket, String password, EncryptionUtils.XNetKeySpecs keySpecs, XNetClientHandlerListener listener) throws XNetException, IOException {
		this.ftp = new XNetFTPHandler();

		this.rsaKeyPair = keySpecs.rsaKeyPair();
		this.aesKey = keySpecs.aesKey();
		this.iv = keySpecs.iv();

		this.listener = listener;

		this.socket = socket;
		this.stream = XNetObjectStream.createHostStream(socket.getInputStream(), socket.getOutputStream());
		this.id = id == -1 ? stream.hashCode() : id;

		var request = establishConnection(password);
		this.username = request.getUsername();

		this.listener.onClientConnected(id);
	}

	private XNetConnectionRequest establishConnection(String password) throws XNetException {
		var success = false;
		try {
			var request = (XNetConnectionRequest) this.stream.read();
			if (request == null || !request.getPassword().equals(password)) {
				stream.write(new XNetConnectionResponse(-1,null,null, null));
				throw new XNetAuthenticationException();
			}

			var encryptedKey = EncryptionUtils.encryptRSA(aesKey.getEncoded(), request.getKey());
			var response = new XNetConnectionResponse(id, rsaKeyPair.getPublic(), encryptedKey, iv.getIV());
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
				catch (XNetException e) {
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

	private void handleRequest() throws XNetException, IllegalStateException, NullPointerException {
		if (listener == null || ftp == null) throw new IllegalStateException("XNetClientHandler's listener is null.");
		var packet = stream.read();
		switch (packet) {
			case XNetFileTransferPacket ftpPacket -> ftp.handlePacket(ftpPacket);
			case XNetRequestPacket request -> listener.writeRequestPacket(request);
			case null -> throw new NullPointerException("Received packet is null.");
			default -> { }
		}
	}

	int getId() { return id; }
	String getUsername() { return username; }
	XNetObjectStream getStream() { return stream; }
}
