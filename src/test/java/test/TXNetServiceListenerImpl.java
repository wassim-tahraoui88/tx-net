package test;

import com.tahraoui.txnet.core.TXNetServiceListener;
import com.tahraoui.txnet.packet.request.TXNetRequestPacket;
import com.tahraoui.txnet.packet.response.TXNetResponsePacket;

public class TXNetServiceListenerImpl implements TXNetServiceListener {
	@Override public void onConnected() {
		System.out.println("Connected");

	}
	@Override public void onDisconnected() {
		System.out.println("Disconnected");

	}
	@Override public void writeRequestPacket(TXNetRequestPacket request) {
	}
	@Override public void readResponsePacket(TXNetResponsePacket response) {
		System.out.println("Response: " + response);
	}
}
