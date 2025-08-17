package test;

import com.tahraoui.xnet.core.XNetResponsePacketDispatcher;
import com.tahraoui.xnet.packet.XNetPacketRequestHandler;
import com.tahraoui.xnet.packet.request.XNetRequestPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PacketHandler extends XNetPacketRequestHandler {

	private static final Logger LOGGER = LogManager.getLogger(PacketHandler.class);

	@Override
	public void handle(XNetRequestPacket packet, XNetResponsePacketDispatcher dispatcher) {
		if (packet instanceof MessagePacketRequest request) dispatcher.broadcastPacket(new MessagePacketResponse(request.getContent()),null);
	}
}
