package test;

import com.tahraoui.txnet.core.TXNetResponsePacketDispatcher;
import com.tahraoui.txnet.packet.TXNetPacketRequestHandler;
import com.tahraoui.txnet.packet.request.TXNetRequestPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PacketHandler extends TXNetPacketRequestHandler {

	private static final Logger LOGGER = LogManager.getLogger(PacketHandler.class);

	@Override
	public void handle(TXNetRequestPacket packet, TXNetResponsePacketDispatcher dispatcher) {
		if (packet instanceof MessagePacketRequest request) dispatcher.broadcastPacket(new MessagePacketResponse(request.getContent()),null);
	}
}
