package com.tahraoui.txnet.model;

import com.tahraoui.core.io.TXSerializable;

public record Connection(int id, String username) implements TXSerializable {}
