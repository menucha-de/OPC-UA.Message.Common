package havis.opcua.message.common.model;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class MessageHeader implements MessagePart {
	private MessageType messageType;
	private int messageId;
	private int messageLength;
	
	public static final int BYTE_COUNT = MessageType.BYTE_COUNT + 8;
	
	public MessageHeader(MessageType messageType, int messageId) {
		super();
		this.messageType = messageType;
		this.messageId = messageId;
	}

	public MessageHeader(ByteBuffer bb) {
		
		super();
		
		try {
			this.messageType = MessageType.forValue(bb.getShort());
			this.messageLength = bb.getInt();
			this.messageId = bb.getInt();
		} catch (BufferUnderflowException e) {
			throw new IllegalArgumentException("Invalid message header.");
		}
		
		if (messageType == null) throw new IllegalArgumentException("Unrecognized message type.");		
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public int getMessageId() {
		return messageId;
	}

	public int getMessageLength() {
		return messageLength;
	}

	public void setMessageLength(int messageLength) {
		this.messageLength = messageLength;
	}

	@Override
	public int getByteCount() {
		/*
		 * MessageType.BYTE_COUNT + length(4 bytes) + id(4 bytes) = 10 bytes
		 */
		return BYTE_COUNT;
	}
	
	public void serialize(ByteBuffer bb) {
		this.messageType.serialize(bb);
		bb.putInt(getMessageLength()); 		
		bb.putInt(getMessageId()); //4 bytes
	}

	@Override
	public String toString() {
		return String.format("{ messageType = %s, messageId = %d, messageLength = %d }", messageType.toString(), messageId, messageLength);   
	}
	
	
}
