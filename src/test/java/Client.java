import com.tahraoui.xnet.core.XNetService;
import com.tahraoui.xnet.model.FileType;
import com.tahraoui.xnet.model.UserCredentials;
import com.tahraoui.xnet.packet.ftp.FileMetaDataTransfer;
import com.tahraoui.xnet.packet.ftp.TransferMode;
import test.MessagePacketRequest;
import test.XNetServiceListenerImpl;

void main() {
	XNetService.init(null, new XNetServiceListenerImpl());
	XNetService.join(8080, new UserCredentials("Amine", "123"));

	var fileName = "test.txt";
	var resource = ClassLoader.getSystemClassLoader().getResource(fileName);
	if (resource == null) {
		System.err.println("Resource not found: test.txt");
		return;
	}
	var file = new File(resource.getFile());

	var scanner = new Scanner(System.in);

	mainLoop: while (true) {
		promptMenu();
		var command = scanner.nextLine();
		switch (command) {
			case "exit" -> {
				break mainLoop;
			}
			case "upload" -> {
				var metadata = new FileMetaDataTransfer(fileName, FileType.DOC, file.getTotalSpace(), TransferMode.UPLOAD);
				XNetService.sendFileMetaData(XNetService.getInstance().getId(), metadata);
			}
			case "download" -> {
				var metadata = new FileMetaDataTransfer("new.txt", FileType.DOC, file.getTotalSpace(), TransferMode.DOWNLOAD);
				XNetService.sendFileMetaData(XNetService.getInstance().getId(), metadata);
			}
			default -> XNetService.sendRequestPacket(new MessagePacketRequest(command));
		}
	}
	XNetService.disconnect();
}

private static void promptMenu() {
	System.out.print("Type your command [ /upload, /download, /exit ] or a message:");

}
