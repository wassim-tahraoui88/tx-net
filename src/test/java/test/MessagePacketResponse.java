package test;

import com.tahraoui.txnet.packet.response.TXNetResponsePacket;

public class MessagePacketResponse extends TXNetResponsePacket {

	private final String content;

	public MessagePacketResponse(String content) {
		this.content = content;

	}

	public String getContent() { return content; }

}
