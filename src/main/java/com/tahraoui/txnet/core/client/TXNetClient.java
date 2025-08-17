package com.tahraoui.txnet.core.client;

import com.tahraoui.txnet.core.TXNetConnection;
import com.tahraoui.txnet.model.Connection;
import com.tahraoui.txnet.packet.request.TXNetRequestPacket;
import com.tahraoui.txnet.packet.response.TXNetResponsePacket;
import com.tahraoui.txnet.exception.TXNetException;
import com.tahraoui.txnet.model.UserCredentials;
import com.tahraoui.txnet.util.TXNetConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyPair;
import java.util.List;

public class TXNetClient implements TXNetConnection {

	private static final Logger LOGGER = LogManager.getLogger(TXNetClient.class);

	private TXNetClientListener listener;
	public void setListener(TXNetClientListener listener) { this.listener = listener; }

	private final TXNetClientRequestHandler handler;
	private final Thread thread;

	public TXNetClient(int port, UserCredentials credentials) throws TXNetException, IOException {
		var socket = new Socket(InetAddress.getByName(TXNetConfig.getInstance().getServerURL()), port);
		this.handler = new TXNetClientRequestHandler(socket, credentials);
		this.handler.setListener(new TXNetClientRequestHandlerListener() {
			@Override
			public void readResponsePacket(TXNetResponsePacket packet) { listener.readResponsePacket(packet); }
		});
		var threadName = "Client Thread - [%d]".formatted(this.handler.getId());
		this.thread = new Thread(handler, threadName);
		this.thread.start();
	}

	@Override public void disconnect() {
		handler.closeConnection();
		thread.interrupt();
	}
	@Override public void disconnect(int id) {}

	@Override public void writeRequestPacket(TXNetRequestPacket packet) { handler.writeRequestPacket(packet); }

	@Override public int getId() { return handler.getId(); }
	@Override public String getUsername() { return handler.getUsername(); }
	@Override public List<Connection> getConnections() { return null; }

	public KeyPair getRSAKeyPair() { return handler.getRSAKeyPair(); }
	public SecretKey getAESKey() { return handler.getAESKey(); }
	public IvParameterSpec getIV() { return handler.getIV(); }
}
