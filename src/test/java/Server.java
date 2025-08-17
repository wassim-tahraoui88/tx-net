import com.tahraoui.xnet.core.XNetService;
import com.tahraoui.xnet.model.UserCredentials;
import test.MessagePacketRequest;
import test.PacketHandler;
import test.XNetServiceListenerImpl;

void main() {
	XNetService.init(new PacketHandler(), new XNetServiceListenerImpl());
	XNetService.host(8080, new UserCredentials("Wassim", "123"));

	var scanner = new Scanner(System.in);

	mainLoop: while (true) {
		promptMenu();
		var command = scanner.nextLine();
		switch (command) {
			case "exit" -> {
				break mainLoop;
			}
			default -> XNetService.sendRequestPacket(new MessagePacketRequest(command));
		}
	}
	XNetService.disconnect();
}

private static void promptMenu() {
	System.out.print("Type a command [ /exit ] or a message:");

}