package com.tahraoui.xnet.packet.ftp;

public final class FileBufferTransfer extends XNetFileTransferPacket {

	private final byte[] bytes;
	private final int offset;
	private final int length;

	public FileBufferTransfer(byte[] bytes, int offset, int length) {
		this.bytes = bytes;
		this.offset = offset;
		this.length = length;
	}

	public byte[] getBytes() { return bytes; }
	public int getOffset() { return offset; }
	public int getLength() { return length; }
}
