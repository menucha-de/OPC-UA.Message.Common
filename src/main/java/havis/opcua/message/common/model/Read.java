package havis.opcua.message.common.model;

import java.nio.ByteBuffer;

public class Read extends Message {
	private ParamId paramId;
	
	public Read(ParamId paramId, int messageId) {
		super(MessageType.READ, messageId);		
		this.paramId = paramId;
	}

	public Read(MessageHeader mh, ByteBuffer bb) {
		super(mh);

		if (getMessageHeader().getMessageType() != MessageType.READ)
			throw new IllegalArgumentException("Invalid message type: " + getMessageHeader().getMessageType());
		
		this.paramId = new ParamId(bb);				
	}
	
	public Read(ByteBuffer bb) {
		super(bb);

		if (getMessageHeader().getMessageType() != MessageType.READ)
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
		return super.getByteCount() + getParamId().getByteCount();
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
