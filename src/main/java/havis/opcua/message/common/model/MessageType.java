package havis.opcua.message.common.model;

import java.nio.ByteBuffer;

public enum MessageType {
	READ(0x00),
	READ_RESPONSE(0x01),
	WRITE(0x02),
	WRITE_RESPONSE(0x03),
	SUBSCRIBE(0x04),
	SUBSCRIBE_RESPONSE(0x05),
	UNSUBSCRIBE(0x06),
	UNSUBSCRIBE_RESPONSE(0x07),
	NOTIFICATION(0x08),
	EVENT(0x09),
	CALL(0x0A),
	CALL_RESPONSE(0x0B);
	
	public static final short BYTE_COUNT = 2;
	
	private final short value;	
	
	private MessageType(int value) {
		this.value = (short)value;
	}

	public short getValue() {
		return value;
	}
	
	protected void serialize(ByteBuffer bb) {
		bb.putShort(this.value);
	}

	public static MessageType deserialize(byte[] bytes) {
		short msgTypeVal = (short) (((((bytes[0] & 0xff) << 8) | (bytes[1] & 0xff))) & 0xffff);
		return forValue(msgTypeVal);
	}
	
	public static MessageType forValue(short value) {
		switch (value) {
			case 0x00 : return READ;
			case 0x01 : return READ_RESPONSE;
			case 0x02 : return WRITE;
			case 0x03 : return WRITE_RESPONSE;
			case 0x04 : return SUBSCRIBE;
			case 0x05 : return SUBSCRIBE_RESPONSE;
			case 0x06 : return UNSUBSCRIBE;
			case 0x07 : return UNSUBSCRIBE_RESPONSE;
			case 0x08 : return NOTIFICATION;
			case 0x09 : return EVENT;
			case 0x0A : return CALL;
			case 0x0B : return CALL_RESPONSE;
			default: return null;
		}
	}

	public static MessageType getResponseType(MessageType requestType) {
		switch (requestType) {
			case READ : return READ_RESPONSE;
			case WRITE : return WRITE_RESPONSE;
			case SUBSCRIBE : return SUBSCRIBE_RESPONSE;
			case UNSUBSCRIBE : return UNSUBSCRIBE_RESPONSE;
			case CALL : return CALL_RESPONSE;
			default: return null;
		}
	}
}

