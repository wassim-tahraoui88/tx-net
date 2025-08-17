package com.tahraoui.txnet.core.ftp;

import com.tahraoui.txnet.packet.ftp.EOFBufferTransfer;
import com.tahraoui.txnet.packet.ftp.FileBufferTransfer;
import com.tahraoui.txnet.packet.ftp.FileMetaDataTransfer;

public interface TXNetFTPDispatcher {
	void onFileMetaDataSent(int id, FileMetaDataTransfer metadata);
	void onFileBufferSent(int id, FileBufferTransfer buffer);
	void onFileTransferComplete(int id, EOFBufferTransfer eof);
}
