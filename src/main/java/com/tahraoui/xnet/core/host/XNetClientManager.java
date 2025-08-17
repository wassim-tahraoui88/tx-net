package com.tahraoui.xnet.core.host;

import com.tahraoui.xnet.core.XNetObjectStream;
import com.tahraoui.xnet.core.XNetResponsePacketDispatcher;
import com.tahraoui.xnet.model.Connection;
import com.tahraoui.xnet.packet.XNetPacketRequestHandler;
import com.tahraoui.xnet.exception.XNetWritingException;
import com.tahraoui.xnet.packet.request.XNetRequestPacket;
import com.tahraoui.xnet.packet.response.XNetResponsePacket;
import com.tahraoui.xnet.packet.response.XNetResponsePacketReader;
import com.tahraoui.xnet.util.XNetConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

class XNetClientManager implements XNetResponsePacketDispatcher {

	private static final Logger LOGGER = LogManager.getLogger(XNetClientManager.class);
	private int currentId = 1;

	private final Map<Integer, XNetClientHandler> handlers;
	private final ExecutorService threadPool;
	private final XNetResponsePacketReader listener;
	private final XNetPacketRequestHandler handler;

	XNetClientManager(XNetPacketRequestHandler handler, XNetResponsePacketReader listener) {
		this.handlers = new ConcurrentHashMap<>(10);
		this.threadPool = Executors.newFixedThreadPool(XNetConfig.getInstance().getServerSize());
		this.handler = handler;
		this.listener = listener;
	}

	void add(int id, XNetClientHandler handler) {
		handlers.put(id, handler);
		threadPool.execute(handler);
	}
	void remove(int id) { handlers.remove(id); }
	List<Connection> getConnections() {
		return handlers.values().stream()
			.map(handler -> new Connection(handler.getId(), handler.getUsername()))
			.toList();
	}

	void receivePacket(XNetRequestPacket packet) { handler.handle(packet,this); }
	private void unicastPacket(XNetObjectStream stream, XNetResponsePacket response) {
		try {
			stream.write(response);
		}
		catch (XNetWritingException _) {
			LOGGER.error("Failed to send response to client {}.", stream.hashCode());
		}
	}
	@Override public void unicastPacket(int id, XNetResponsePacket packet) {
		var handler = handlers.get(id);

		if (handler == null) {
			LOGGER.error("No client with id {} found.", id);
			return;
		}

		var stream = handler.getStream();
		unicastPacket(stream, packet);
	}
	@Override public void broadcastPacket(XNetResponsePacket packet, Predicate<Integer> filter) {
		for (var handler : handlers.values()) {
			if (filter != null && !filter.test(handler.getId())) continue;
			unicastPacket(handler.getStream(), packet);
		}
		listener.readResponsePacket(packet);
	}

	void disconnect(int id) {
		var handler = handlers.get(id);
		if (handler != null) {
			handler.closeConnection();
			remove(id);
		}
		else LOGGER.warn("Attempted to disconnect non-existent client with id {}.", id);
	}
	void close() {
		threadPool.shutdown();
		handlers.clear();
	}
	int getNextId() { return currentId++; }
}
