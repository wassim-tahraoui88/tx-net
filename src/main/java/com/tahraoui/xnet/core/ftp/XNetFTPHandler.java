package com.tahraoui.xnet.core.ftp;

import com.tahraoui.xnet.packet.ftp.EOFBufferTransfer;
import com.tahraoui.xnet.packet.ftp.FileBufferTransfer;
import com.tahraoui.xnet.packet.ftp.FileMetaDataTransfer;
import com.tahraoui.xnet.packet.ftp.XNetFileTransferPacket;
import com.tahraoui.xnet.util.XNetConfig;

public class XNetFTPHandler {

	public enum FTPState {
		IDLE, SENDING, RECEIVING
	}

	protected FTPState state;
	protected String expectedFileName;
	protected long expectedFileSize, bytesTransferred;
	protected byte[] data;
	protected XNetFTPListener listener;

	public XNetFTPHandler() {
		initialize();

	}

	protected void initialize() {
		this.state = FTPState.IDLE;
		this.expectedFileName = null;
		this.expectedFileSize = -1;
		this.bytesTransferred = -1;
		this.data = null;
	}

	public void handlePacket(XNetFileTransferPacket packet) {
		if (packet == null) throw new NullPointerException("Received file transfer packet is null.");
		switch (packet) {
			case FileMetaDataTransfer metadata -> onFileMetaDataReceived(metadata);
			case FileBufferTransfer buffer -> onFileBufferReceived(buffer);
			case EOFBufferTransfer _ -> onFileTransferComplete();
			default -> throw new RuntimeException("Unsupported XNetFileTransferPacket type: " + packet);
		}
	}

	//region RECEIVING MODE
	private void onFileMetaDataReceived(FileMetaDataTransfer metadata) {
		if (!verifyInitialization(metadata)) return;
		initFileTransfer(metadata, FTPState.RECEIVING);
		listener.onFileMetaDataReceived(metadata);
	}
	private void onFileBufferReceived(FileBufferTransfer buffer) {
		if (!verifyTransfer(buffer)) return;

		var bytes = buffer.getBytes();
		var bytesSize = buffer.getLength();
		var offset = buffer.getOffset();

		if (bytesTransferred + bytesSize > expectedFileSize) throw new RuntimeException("Received buffer exceeds expected file size.");

		System.arraycopy(bytes,0, data, offset, bytesSize);

		bytesTransferred += bytesSize;
		listener.onFileBufferReceived(bytesTransferred, expectedFileSize);
	}
	private void onFileTransferComplete() {
		initialize();
		listener.onFileTransferComplete(expectedFileName, expectedFileSize);
	}
	//endregion

	//region SENDING MODE
	public void onFileMetaDataSent(FileMetaDataTransfer metadata) {
		if (!verifyInitialization(metadata)) return;
		initFileTransfer(metadata, FTPState.SENDING);
	}
	public void onFileBufferSent(FileBufferTransfer buffer) {
		if (!verifyTransfer(buffer)) return;
		bytesTransferred += buffer.getLength();
	}
	//endregion

	private boolean verifyTransfer(FileBufferTransfer buffer) {
		if (buffer == null) throw new NullPointerException("File buffer is null.");
		if (state == FTPState.IDLE) throw new RuntimeException("File transfer is not in progress.");
		if (expectedFileName == null || expectedFileSize <= 0 || bytesTransferred < 0) throw new RuntimeException("File metadata is not initialized.");
		return true;
	}
	private boolean verifyInitialization(FileMetaDataTransfer metadata) {
		if (this.state != FTPState.IDLE) throw new RuntimeException("File transfer is already in progress.");

		if (metadata == null) throw new NullPointerException("Received file metadata is null.");

		var filename = metadata.getFilename();
		if (filename == null || filename.isEmpty()) throw new RuntimeException("File name cannot be null or empty.");

		var expectedSize = metadata.getExpectedSize();
		var maxFileSize = XNetConfig.getInstance().getMaxFileSize();
		if (expectedSize <= 0) throw new RuntimeException("File size must be greater than zero.");
		if (expectedSize > maxFileSize) throw new RuntimeException("File size exceeds max allowed size: %sMB".formatted(maxFileSize));
		return true;
	}
	private void initFileTransfer(FileMetaDataTransfer metadata, FTPState state) {
		var expectedFileSize = metadata.getExpectedSize();
		try {
			data = new byte[Math.toIntExact(expectedFileSize)];
		}
		catch (ArithmeticException e) {
			throw new RuntimeException("File size exceeds max allowed size");
		}
		expectedFileName = metadata.getFilename();
		this.expectedFileSize = expectedFileSize;
		bytesTransferred = 0;

		this.state = state;
	}
}
