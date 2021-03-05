package havis.opcua.message.common.model;

import java.nio.ByteBuffer;

public class WriteResponse extends Message {

	private Status status;
	
	public WriteResponse(int messageId, Status status) {
		super(MessageType.WRITE_RESPONSE, messageId);
		this.status = status;
	}
	
	public WriteResponse(Write writeMessage, Status status) {
		super(MessageType.WRITE_RESPONSE, writeMessage.getMessageHeader().getMessageId());
		this.status = status;
	}

	public WriteResponse(MessageHeader mh, ByteBuffer bb) {
		super(mh);

		if (getMessageHeader().getMessageType() != MessageType.WRITE_RESPONSE)
			throw new IllegalArgumentException("Invalid message type: " + getMessageHeader().getMessageType());
		
		this.status = Status.forValue(bb.getShort());		
		if (status == null) throw new IllegalArgumentException("Invalid message status.");
	}
	
	public WriteResponse(ByteBuffer bb) {
		super(bb);

		if (getMessageHeader().getMessageType() != MessageType.WRITE_RESPONSE)
			throw new IllegalArgumentException("Invalid message type: " + getMessageHeader().getMessageType());
		
		this.status = Status.forValue(bb.getShort());		
		if (status == null) throw new IllegalArgumentException("Invalid message status.");
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public int getByteCount() {
		return Status.BYTE_COUNT + 
		super.getByteCount();
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb) {
		getMessageHeader().serialize(bb);
		bb.putShort(getStatus().getValue());
		return bb;
	}
	
	@Override
	public String toString() {
		return "{ " + super.toString() + ", status = " + status + " }";
	}
}




