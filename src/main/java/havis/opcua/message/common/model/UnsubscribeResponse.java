package havis.opcua.message.common.model;

import java.nio.ByteBuffer;

public class UnsubscribeResponse extends Message {
	private Status status;
	
	public UnsubscribeResponse(int messageId, Status status) {
		super(MessageType.UNSUBSCRIBE_RESPONSE, messageId);
		this.status = status;
	}
	
	public UnsubscribeResponse(Unsubscribe unsubscribeMessage, Status status) {
		super(MessageType.UNSUBSCRIBE_RESPONSE, unsubscribeMessage.getMessageHeader().getMessageId());
		this.status = status;
	}

	public UnsubscribeResponse(MessageHeader mh, ByteBuffer bb) {
		super(mh);

		if (getMessageHeader().getMessageType() != MessageType.UNSUBSCRIBE_RESPONSE)
			throw new IllegalArgumentException("Invalid message type: " + getMessageHeader().getMessageType());
		
		this.status = Status.forValue(bb.getShort());		
		if (status == null) throw new IllegalArgumentException("Invalid message status.");
	}
	
	public UnsubscribeResponse(ByteBuffer bb) {
		super(bb);

		if (getMessageHeader().getMessageType() != MessageType.UNSUBSCRIBE_RESPONSE)
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
		return Status.BYTE_COUNT + super.getByteCount();
	}

	public ByteBuffer serialize(ByteBuffer bb) {
		getMessageHeader().serialize(bb);
		getStatus().serialize(bb);
		return bb;
	}
	
	@Override
	public String toString() {
		return "{ " + super.toString() + ", status = " + status + " }";
	}
}
