package havis.opcua.message.common.serialize;

import java.nio.ByteBuffer;

import havis.opcua.message.common.model.Message;
import havis.opcua.message.common.model.MessageHeader;

public class MessageSerializer {
	
	public static byte[] serialize(MessageHeader mh) {
		ByteBuffer bb = ByteBuffer.allocate(MessageHeader.BYTE_COUNT);
    mh.serialize(bb);
    return bb.array();
  } 

	public static byte[] serialize(Message m) {
		m.getMessageHeader().setMessageLength(m.getByteCount());
		ByteBuffer bb = ByteBuffer.allocate(m.getMessageHeader().getMessageLength());
		return m.serialize(bb).array();		
	}
	
}
