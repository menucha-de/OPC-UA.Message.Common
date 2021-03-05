package havis.opcua.message.common.server;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import havis.opcua.message.common.model.MessageHeader;

public interface MessageListener {
	
	void received(MessageHeader msgHdr, ByteBuffer msgBodyBytes);
	
	void sent(ByteBuffer bb);
	
	void accepted(SocketAddress remoteHost);
	
	void rejected(SocketAddress socketAddress);
	
	void disconnected(SocketAddress remoteHost);
	
	void errorOccurred(Throwable error);
	
}
