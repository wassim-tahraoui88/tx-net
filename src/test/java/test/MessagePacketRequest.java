package test;

import com.tahraoui.txnet.packet.request.TXNetRequestPacket;

public class MessagePacketRequest extends TXNetRequestPacket {

	private final String content;

	public MessagePacketRequest(String content) {
		this.content = content;

	}

	public String getContent() { return content; }

}
