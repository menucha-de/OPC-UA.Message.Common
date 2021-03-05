package havis.opcua.message.common.serialize;

import java.nio.ByteBuffer;

import havis.opcua.message.common.model.Call;
import havis.opcua.message.common.model.CallResponse;
import havis.opcua.message.common.model.Event;
import havis.opcua.message.common.model.Message;
import havis.opcua.message.common.model.MessageHeader;
import havis.opcua.message.common.model.MessageType;
import havis.opcua.message.common.model.Notification;
import havis.opcua.message.common.model.Read;
import havis.opcua.message.common.model.ReadResponse;
import havis.opcua.message.common.model.Subscribe;
import havis.opcua.message.common.model.SubscribeResponse;
import havis.opcua.message.common.model.Unsubscribe;
import havis.opcua.message.common.model.UnsubscribeResponse;
import havis.opcua.message.common.model.Write;
import havis.opcua.message.common.model.WriteResponse;

public class MessageDeserializer {
	
	public static <T extends Message> T deserialize(byte[] data) {		
		return deserialize(ByteBuffer.wrap(data));
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Message> T deserialize(ByteBuffer bb) {		
		int pos = bb.position();
		MessageType msgType = MessageType.forValue(bb.getShort());		
		bb.position(pos);
		
		switch (msgType) {
			case READ: return (T) new Read(bb); 
			case READ_RESPONSE: return (T) new ReadResponse(bb); 
			case WRITE: return (T) new Write(bb);
			case WRITE_RESPONSE: return (T) new WriteResponse(bb);
			case SUBSCRIBE: return (T) new Subscribe(bb);
			case SUBSCRIBE_RESPONSE: return (T) new SubscribeResponse(bb);
			case UNSUBSCRIBE: return (T) new Unsubscribe(bb);
			case UNSUBSCRIBE_RESPONSE: return (T) new UnsubscribeResponse(bb);
			case NOTIFICATION: return (T) new Notification(bb);	
			case CALL: return (T) new Call(bb);
			case CALL_RESPONSE: return (T) new CallResponse(bb);
			case EVENT: return (T) new Event(bb);
		}	
		return null;
	}
	
	public static Message deserialize(MessageHeader mh, ByteBuffer bb) {		
		MessageType msgType = mh.getMessageType();
		
		switch (msgType) {
			case READ: return new Read(mh, bb); 
			case READ_RESPONSE: return new ReadResponse(mh, bb); 
			case WRITE: return new Write(mh, bb);
			case WRITE_RESPONSE: return new WriteResponse(mh, bb);
			case SUBSCRIBE: return new Subscribe(mh, bb);
			case SUBSCRIBE_RESPONSE: return new SubscribeResponse(mh, bb);
			case UNSUBSCRIBE: return new Unsubscribe(mh, bb);
			case UNSUBSCRIBE_RESPONSE: return new UnsubscribeResponse(mh, bb);
			case NOTIFICATION: return new Notification(mh, bb);
			case CALL: return new Call(mh, bb);
			case CALL_RESPONSE: return new CallResponse(mh, bb);
			case EVENT: return new Event(mh, bb);
		}	
		return null;
	}
}
