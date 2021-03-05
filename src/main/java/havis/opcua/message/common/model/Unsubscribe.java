package havis.opcua.message.common.model;

import java.nio.ByteBuffer;

public class Unsubscribe extends Message {

	private ParamId paramId;
	
	public Unsubscribe(ParamId paramId, int messageId) {		
		super(MessageType.UNSUBSCRIBE, messageId);
		this.paramId = paramId;
	}

	public Unsubscribe(MessageHeader mh, ByteBuffer bb) {
		super(mh);
		
		if (getMessageHeader().getMessageType() != MessageType.UNSUBSCRIBE)
			throw new IllegalArgumentException("Invalid message type: " + getMessageHeader().getMessageType());

		this.paramId = new ParamId(bb);
	}
	
	public Unsubscribe(ByteBuffer bb) {
		super(bb);
		
		if (getMessageHeader().getMessageType() != MessageType.UNSUBSCRIBE)
			throw new IllegalArgumentException("Invalid message type: " + getMessageHeader().getMessageType());

		this.paramId = new ParamId(bb);
	}

	public ParamId getParamId() {
		return paramId;
	}

	public void setParamId(ParamId paramId) {
		this.paramId = paramId;
	}

	@Override
	public int getByteCount() {
		return super.getByteCount() + paramId.getByteCount(); 
	}

	public ByteBuffer serialize(ByteBuffer bb) {
		getMessageHeader().serialize(bb);
		getParamId().serialize(bb);
		return bb;
	}
	
	@Override
	public String toString() {
		return "{ " + super.toString() + ", paramId = " + paramId + " }"; 
	}
}
