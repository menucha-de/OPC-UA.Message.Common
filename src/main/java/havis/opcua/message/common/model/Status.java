package havis.opcua.message.common.model;

import java.nio.ByteBuffer;

public enum Status {
	
	SUCCESS(0x0000), 			//0
	INVALID_MESSAGE(0x0064),	//100
	UNSUPPORTED_MESSAGE(0x0065),//101
	UNEXPECTED_MESSAGE(0x0066), //102
	MISSING_FIELD(0x0067), 		//103
	UNEXPECTED_FIELD(0x0068), 	//104
	UNKOWN_ID(0x00c8), 			//200
	INVALID_PARAMETER(0x012c), 	//300
	INVALID_PARAM_TYPE(0x012d), //301
	INVALID_PARAM_VALUE(0x012e),//302
	APPLICATION_ERROR(0x01f4);  //500
	
	public static final int BYTE_COUNT = 2;
	
	private final short value;
	
	
	private Status(int value) {
		this.value = (short)value;
	}
	
	public short getValue() {
		return this.value;
	}
	
	public static Status forValue(short value) {
		switch(value) {
			case 0x0000 : return SUCCESS;
			case 0x0064 : return INVALID_MESSAGE;
			case 0x0065 : return UNSUPPORTED_MESSAGE;
			case 0x0066 : return UNEXPECTED_MESSAGE;
			case 0x0067 : return MISSING_FIELD;
			case 0x0068 : return UNEXPECTED_FIELD;
			case 0x00c8 : return UNKOWN_ID;
			case 0x012c : return INVALID_PARAMETER;
			case 0x012d : return INVALID_PARAM_TYPE;
			case 0x012e : return INVALID_PARAM_VALUE;
			case 0x01f4 : return APPLICATION_ERROR;
			default : return null;
		}
	}
	
	protected void serialize(ByteBuffer bb) {
		bb.putShort(this.value);
	}
} 