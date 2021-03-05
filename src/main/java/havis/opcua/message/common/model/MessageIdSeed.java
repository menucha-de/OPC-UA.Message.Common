package havis.opcua.message.common.model;

import java.util.UUID;

public class MessageIdSeed {
	public static synchronized int next() {
		return UUID.randomUUID().hashCode();
	}
}
