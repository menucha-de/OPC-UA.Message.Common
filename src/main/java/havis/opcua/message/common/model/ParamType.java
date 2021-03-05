package havis.opcua.message.common.model;

public enum ParamType {
	BOOLEAN(0x00),
	CHAR(0x01),
	BYTE(0x02),
	SHORT(0x03),
	INT(0x04),
	LONG(0x05),
	FLOAT(0x06),
	DOUBLE(0x07),
	ARRAY(0x08),
	STRUCT(0x09);

	public static final int BYTE_COUNT = 2;
	
	private short value;
	
	private ParamType(int value) {
		this.value = (short)value;
	}
	
	public short getValue() {
		return value;
	}
	
	public static ParamType forValue(short value) {
		for (ParamType v : values()) {
			if (v.value == value) {
				return v;
			}
		}
		return null;
	}
}
