package com.tahraoui.txnet.core.client;

import com.tahraoui.txnet.packet.request.TXNetRequestPacketWriter;
import com.tahraoui.txnet.packet.response.TXNetResponsePacketReader;

public interface TXNetClientListener extends TXNetRequestPacketWriter, TXNetResponsePacketReader {

}
