package com.tahraoui.xnet.core.client;

import com.tahraoui.xnet.core.XNetConnection;
import com.tahraoui.xnet.model.Connection;
import com.tahraoui.xnet.packet.request.XNetRequestPacket;
import com.tahraoui.xnet.packet.response.XNetResponsePacket;
import com.tahraoui.xnet.exception.XNetException;
import com.tahraoui.xnet.model.UserCredentials;
import com.tahraoui.xnet.util.XNetConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyPair;
import java.util.List;

public class XNetClient implements XNetConnection {

	private static final Logger LOGGER = LogManager.getLogger(XNetClient.class);

	private XNetClientListener listener;
	public void setListener(XNetClientListener listener) { this.listener = listener; }

	private final XNetClientRequestHandler handler;
	private final Thread thread;

	public XNetClient(int port, UserCredentials credentials) throws XNetException, IOException {
		var socket = new Socket(InetAddress.getByName(XNetConfig.getInstance().getServerURL()), port);
		this.handler = new XNetClientRequestHandler(socket, credentials);
		this.handler.setListener(new XNetClientRequestHandlerListener() {
			@Override
			public void readResponsePacket(XNetResponsePacket packet) { listener.readResponsePacket(packet); }
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

	@Override public void writeRequestPacket(XNetRequestPacket packet) { handler.writeRequestPacket(packet); }

	@Override public int getId() { return handler.getId(); }
	@Override public String getUsername() { return handler.getUsername(); }
	@Override public List<Connection> getConnections() { return null; }

	public KeyPair getRSAKeyPair() { return handler.getRSAKeyPair(); }
	public SecretKey getAESKey() { return handler.getAESKey(); }
	public IvParameterSpec getIV() { return handler.getIV(); }
}
