package com.tahraoui.xnet.core.client;

import com.tahraoui.xnet.packet.request.XNetRequestPacketWriter;
import com.tahraoui.xnet.packet.response.XNetResponsePacketReader;

public interface XNetClientListener extends XNetRequestPacketWriter, XNetResponsePacketReader {

}
