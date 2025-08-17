package com.tahraoui.txnet.core.ftp;

import com.tahraoui.txnet.packet.ftp.FileMetaDataTransfer;

public interface TXNetFTPListener {
	void onFileMetaDataReceived(FileMetaDataTransfer metadata);
	void onFileBufferReceived(long bytesTransferred, long expectedFileSize);
	void onFileTransferComplete(String fileName, long fileSize);
}
