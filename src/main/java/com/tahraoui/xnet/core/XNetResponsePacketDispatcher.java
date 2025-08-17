package com.tahraoui.xnet.core;

import com.tahraoui.xnet.packet.response.XNetResponsePacket;

import java.util.function.Predicate;

public interface XNetResponsePacketDispatcher {
	void unicastPacket(int id, XNetResponsePacket packet);
	void broadcastPacket(XNetResponsePacket packet, Predicate<Integer> filter);
}
