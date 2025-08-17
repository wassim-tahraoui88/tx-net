package com.tahraoui.txnet.core.host;

import com.tahraoui.txnet.core.TXNetObjectStream;
import com.tahraoui.txnet.core.TXNetResponsePacketDispatcher;
import com.tahraoui.txnet.model.Connection;
import com.tahraoui.txnet.packet.TXNetPacketRequestHandler;
import com.tahraoui.txnet.exception.TXNetWritingException;
import com.tahraoui.txnet.packet.request.TXNetRequestPacket;
import com.tahraoui.txnet.packet.response.TXNetResponsePacket;
import com.tahraoui.txnet.packet.response.TXNetResponsePacketReader;
import com.tahraoui.txnet.util.TXNetConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

class TXNetClientManager implements TXNetResponsePacketDispatcher {

	private static final Logger LOGGER = LogManager.getLogger(TXNetClientManager.class);
	private int currentId = 1;

	private final Map<Integer, TXNetClientHandler> handlers;
	private final ExecutorService threadPool;
	private final TXNetResponsePacketReader listener;
	private final TXNetPacketRequestHandler handler;

	TXNetClientManager(TXNetPacketRequestHandler handler, TXNetResponsePacketReader listener) {
		this.handlers = new ConcurrentHashMap<>(10);
		this.threadPool = Executors.newFixedThreadPool(TXNetConfig.getInstance().getServerSize());
		this.handler = handler;
		this.listener = listener;
	}

	void add(int id, TXNetClientHandler handler) {
		handlers.put(id, handler);
		threadPool.execute(handler);
	}
	void remove(int id) { handlers.remove(id); }
	List<Connection> getConnections() {
		return handlers.values().stream()
			.map(handler -> new Connection(handler.getId(), handler.getUsername()))
			.toList();
	}

	void receivePacket(TXNetRequestPacket packet) { handler.handle(packet,this); }
	private void unicastPacket(TXNetObjectStream stream, TXNetResponsePacket response) {
		try {
			stream.write(response);
		}
		catch (TXNetWritingException _) {
			LOGGER.error("Failed to send response to client {}.", stream.hashCode());
		}
	}
	@Override public void unicastPacket(int id, TXNetResponsePacket packet) {
		var handler = handlers.get(id);

		if (handler == null) {
			LOGGER.error("No client with id {} found.", id);
			return;
		}

		var stream = handler.getStream();
		unicastPacket(stream, packet);
	}
	@Override public void broadcastPacket(TXNetResponsePacket packet, Predicate<Integer> filter) {
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
