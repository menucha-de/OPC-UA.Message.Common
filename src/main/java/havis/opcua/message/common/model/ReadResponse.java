package havis.opcua.message.common.model;

import java.nio.ByteBuffer;

public class ReadResponse extends Message {
	private Status status;
	private ParamValue result;
	private ParamId paramId;

	public ReadResponse(int messageId, Status status) {
		super(MessageType.READ_RESPONSE, messageId);
		this.status = status;
	}
	
	public ReadResponse(Read readMessage, ParamValue paramValue, Status status) {
		super(MessageType.READ_RESPONSE, readMessage.getMessageHeader().getMessageId());
		this.paramId = readMessage.getParamId();
		this.result = paramValue;
		this.status = status;
	}

	public ReadResponse(MessageHeader mh, ByteBuffer bb) throws IllegalArgumentException {
		super(mh);

		if (getMessageHeader().getMessageType() != MessageType.READ_RESPONSE)
			throw new IllegalArgumentException("Invalid message type: " + getMessageHeader().getMessageType());

		this.status = Status.forValue(bb.getShort());
		if (status == null)
			throw new IllegalArgumentException("Invalid message status.");

		if (status == Status.SUCCESS) {
			this.paramId = new ParamId(bb);
			this.result = new ParamValue(bb);
		}
	}

	public ReadResponse(ByteBuffer bb) throws IllegalArgumentException {
		super(bb);

		if (getMessageHeader().getMessageType() != MessageType.READ_RESPONSE)
			throw new IllegalArgumentException("Invalid message type: " + getMessageHeader().getMessageType());

		this.status = Status.forValue(bb.getShort());
		if (status == null)
			throw new IllegalArgumentException("Invalid message status.");

		if (status == Status.SUCCESS) {
			this.paramId = new ParamId(bb);
			this.result = new ParamValue(bb);
		}
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public ParamValue getResult() {
		return result;
	}

	public void setResult(ParamValue result) {
		this.result = result;
	}

	public ParamId getParamId() {
		return paramId;
	}

	public void setParamId(ParamId paramId) {
		this.paramId = paramId;
	}

	@Override
	public int getByteCount() {
		return super.getByteCount() + Status.BYTE_COUNT
			+ (getStatus() == Status.SUCCESS ? (getParamId().getByteCount() 
			+ getResult().getByteCount()) : 0);
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb) {
		getMessageHeader().serialize(bb);
		getStatus().serialize(bb);
		if (getStatus() == Status.SUCCESS) {
			getParamId().serialize(bb);
			getResult().serialize(bb);
		}

		return bb;
	}
	
	@Override
	public String toString() {		
		if (status == Status.SUCCESS) 
			return "{ " + super.toString() + ", status = " + status + ", paramId = " + paramId + ", paramValue = " + result + " }";
		
		return "{ " + super.toString() + ", status = " + status + " }";
	}
}
