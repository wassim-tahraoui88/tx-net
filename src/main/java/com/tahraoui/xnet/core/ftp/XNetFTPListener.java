package com.tahraoui.xnet.core.ftp;

import com.tahraoui.xnet.packet.ftp.FileMetaDataTransfer;

public interface XNetFTPListener {
	void onFileMetaDataReceived(FileMetaDataTransfer metadata);
	void onFileBufferReceived(long bytesTransferred, long expectedFileSize);
	void onFileTransferComplete(String fileName, long fileSize);
}
