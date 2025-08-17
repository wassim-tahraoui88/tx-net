package test;

import com.tahraoui.xnet.core.XNetServiceListener;
import com.tahraoui.xnet.packet.request.XNetRequestPacket;
import com.tahraoui.xnet.packet.response.XNetResponsePacket;

public class XNetServiceListenerImpl implements XNetServiceListener {
	@Override public void onConnected() {
		System.out.println("Connected");

	}
	@Override public void onDisconnected() {
		System.out.println("Disconnected");

	}
	@Override public void writeRequestPacket(XNetRequestPacket request) {
	}
	@Override public void readResponsePacket(XNetResponsePacket response) {
		System.out.println("Response: " + response);
	}
}
