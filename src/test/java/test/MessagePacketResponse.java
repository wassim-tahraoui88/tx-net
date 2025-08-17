package test;

import com.tahraoui.xnet.packet.response.XNetResponsePacket;

public class MessagePacketResponse extends XNetResponsePacket {

	private final String content;

	public MessagePacketResponse(String content) {
		this.content = content;

	}

	public String getContent() { return content; }

}
