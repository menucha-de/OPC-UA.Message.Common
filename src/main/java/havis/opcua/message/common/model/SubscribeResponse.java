package havis.opcua.message.common.model;

import java.nio.ByteBuffer;

public class SubscribeResponse extends Message {
	private Status status;
	
	public SubscribeResponse(int messageId, Status status) {
		super(MessageType.SUBSCRIBE_RESPONSE, messageId);
		this.status = status;
	}
	
	public SubscribeResponse(Subscribe subscribeMessage, Status status) {
		super(MessageType.SUBSCRIBE_RESPONSE, subscribeMessage.getMessageHeader().getMessageId());
		this.status = status;
	}

	public SubscribeResponse(MessageHeader mh, ByteBuffer bb) {
		super(mh);

		if (getMessageHeader().getMessageType() != MessageType.SUBSCRIBE_RESPONSE)
			throw new IllegalArgumentException("Invalid message type: " + getMessageHeader().getMessageType());
		
		this.status = Status.forValue(bb.getShort());		
		if (status == null) throw new IllegalArgumentException("Invalid message status.");
	}
	
	public SubscribeResponse(ByteBuffer bb) {
		super(bb);

		if (getMessageHeader().getMessageType() != MessageType.SUBSCRIBE_RESPONSE)
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
		getStatus().serialize(bb);		
		return bb;
	}
	
	@Override
	public String toString() {
		return "{ " + super.toString() + ", status = " + status + " }";
	}
}
