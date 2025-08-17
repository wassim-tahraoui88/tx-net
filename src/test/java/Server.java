import com.tahraoui.txnet.core.TXNetService;
import com.tahraoui.txnet.model.UserCredentials;
import test.MessagePacketRequest;
import test.PacketHandler;
import test.TXNetServiceListenerImpl;

void main() {
	TXNetService.init(new PacketHandler(), new TXNetServiceListenerImpl());
	TXNetService.host(8080, new UserCredentials("Wassim", "123"));

	var scanner = new Scanner(System.in);

	mainLoop: while (true) {
		promptMenu();
		var command = scanner.nextLine();
		switch (command) {
			case "exit" -> {
				break mainLoop;
			}
			default -> TXNetService.sendRequestPacket(new MessagePacketRequest(command));
		}
	}
	TXNetService.disconnect();
}

private static void promptMenu() {
	System.out.print("Type a command [ /exit ] or a message:");

}