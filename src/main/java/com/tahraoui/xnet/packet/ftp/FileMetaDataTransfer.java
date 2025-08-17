package com.tahraoui.xnet.packet.ftp;

import com.tahraoui.xnet.model.FileType;

public final class FileMetaDataTransfer extends XNetFileTransferPacket {
	private final String filename;
	private final FileType fileType;
	private final long expectedSize;
	private final TransferMode transferMode;

	public FileMetaDataTransfer(String filename, FileType fileType, long expectedSize, TransferMode transferMode) {
		this.filename = filename;
		this.fileType = fileType;
		this.expectedSize = expectedSize;
		this.transferMode = transferMode;
	}

	public String getFilename() { return filename; }
	public FileType getFileType() { return fileType; }
	public long getExpectedSize() { return expectedSize; }
	public TransferMode getTransferMode() { return transferMode; }

	@Override
	public String toString() {
		return "FileMetaDataTransfer[" +
				"filename=" + filename + ", " +
				"fileType=" + fileType + ", " +
				"expectedSize=" + expectedSize + ']';
	}
}