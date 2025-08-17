package com.tahraoui.xnet.model;

import java.io.Serializable;

public record Connection(int id, String username) implements Serializable {}
