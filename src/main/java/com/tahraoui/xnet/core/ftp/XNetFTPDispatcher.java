package com.tahraoui.xnet.core.ftp;

import com.tahraoui.xnet.packet.ftp.EOFBufferTransfer;
import com.tahraoui.xnet.packet.ftp.FileBufferTransfer;
import com.tahraoui.xnet.packet.ftp.FileMetaDataTransfer;

public interface XNetFTPDispatcher {
	void onFileMetaDataSent(int id, FileMetaDataTransfer metadata);
	void onFileBufferSent(int id, FileBufferTransfer buffer);
	void onFileTransferComplete(int id, EOFBufferTransfer eof);
}
