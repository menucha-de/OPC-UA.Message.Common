package havis.opcua.message.common.model;

import java.nio.ByteBuffer;

public abstract class Message implements MessagePart {
	private MessageHeader messageHeader;

	protected Message(MessageType messageType, int messageId) {
		super();
		this.messageHeader = new MessageHeader(messageType, messageId);
	}

	protected Message(MessageHeader mh) {
		this.messageHeader = mh;
	}
	
	protected Message(ByteBuffer bb) {
		this.messageHeader = new MessageHeader(bb);
	}

	public MessageHeader getMessageHeader() {
		return messageHeader;
	}
	
	public int getByteCount() {
		return getMessageHeader().getByteCount();
	}
	
	@Override
	public String toString() {
		return "messageHeader = " + messageHeader;
	}

	public abstract ByteBuffer serialize(ByteBuffer bb);
		
}
