import com.tahraoui.txnet.core.TXNetService;
import com.tahraoui.txnet.model.FileType;
import com.tahraoui.txnet.model.UserCredentials;
import com.tahraoui.txnet.packet.ftp.FileMetaDataTransfer;
import com.tahraoui.txnet.packet.ftp.TransferMode;
import test.MessagePacketRequest;
import test.TXNetServiceListenerImpl;

void main() {
	TXNetService.init(null, new TXNetServiceListenerImpl());
	TXNetService.join(8080, new UserCredentials("Amine", "123"));

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
				TXNetService.sendFileMetaData(TXNetService.getInstance().getId(), metadata);
			}
			case "download" -> {
				var metadata = new FileMetaDataTransfer("new.txt", FileType.DOC, file.getTotalSpace(), TransferMode.DOWNLOAD);
				TXNetService.sendFileMetaData(TXNetService.getInstance().getId(), metadata);
			}
			default -> TXNetService.sendRequestPacket(new MessagePacketRequest(command));
		}
	}
	TXNetService.disconnect();
}

private static void promptMenu() {
	System.out.print("Type your command [ /upload, /download, /exit ] or a message:");

}
