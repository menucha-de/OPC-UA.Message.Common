package havis.opcua.message.common.model;

import java.nio.ByteBuffer;

public class Write extends Message {

	private ParamId paramId;
	private ParamValue paramValue;
	
	public Write(ParamId paramId, ParamValue paramValue, int messageId) {
		super(MessageType.WRITE, messageId);
		this.paramId = paramId;
		this.paramValue = paramValue;
	}

	public Write(MessageHeader mh, ByteBuffer bb) {
		super(mh);
		
		if (getMessageHeader().getMessageType() != MessageType.WRITE)
			throw new IllegalArgumentException("Invalid message type: " + getMessageHeader().getMessageType());
		
		this.paramId = new ParamId(bb);
		this.paramValue = new ParamValue(bb);
	}
	
	public Write(ByteBuffer bb) {
		super(bb);
		
		if (getMessageHeader().getMessageType() != MessageType.WRITE)
			throw new IllegalArgumentException("Invalid message type: " + getMessageHeader().getMessageType());
		
		this.paramId = new ParamId(bb);
		this.paramValue = new ParamValue(bb);
	}

	public ParamId getParamId() {
		return paramId;
	}

	public void setParamId(ParamId paramId) {
		this.paramId = paramId;
	}

	public ParamValue getParamValue() {
		return paramValue;
	}

	public void setParamValue(ParamValue paramValue) {
		this.paramValue = paramValue;
	}

	@Override
	public int getByteCount() {
		return super.getByteCount() + 
				getParamId().getByteCount() + 
				getParamValue().getByteCount();		
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb) {
		getMessageHeader().serialize(bb);
		getParamId().serialize(bb);
		getParamValue().serialize(bb);		
		return bb;
	}
	
	@Override
	public String toString() {
		return "{ " + super.toString() + ", paramId = " + paramId + ", paramValue = " + paramValue + " }";		
	}
}
