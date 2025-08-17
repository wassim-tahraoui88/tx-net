package com.tahraoui.txnet.core;

import com.tahraoui.txnet.packet.response.TXNetResponsePacket;

import java.util.function.Predicate;

public interface TXNetResponsePacketDispatcher {
	void unicastPacket(int id, TXNetResponsePacket packet);
	void broadcastPacket(TXNetResponsePacket packet, Predicate<Integer> filter);
}
