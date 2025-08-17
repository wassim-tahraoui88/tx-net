package test;

import com.tahraoui.xnet.packet.request.XNetRequestPacket;

public class MessagePacketRequest extends XNetRequestPacket {

	private final String content;

	public MessagePacketRequest(String content) {
		this.content = content;

	}

	public String getContent() { return content; }

}
