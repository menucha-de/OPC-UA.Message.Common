package havis.opcua.message.common.model;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Map;

public class ParamValue implements MessagePart {
	private Object value;

	private boolean omitStringTypeBytes;
	
	public ParamValue(Boolean value) {
		this.value = value;
	}

	public ParamValue(Byte value) {
		this.value = value;
	}

	public ParamValue(Short value) {
		this.value = value;
	}

	public ParamValue(Integer value) {
		this.value = value;
	}

	public ParamValue(Long value) {
		this.value = value;
	}

	public ParamValue(Float value) {
		this.value = value;
	}

	public ParamValue(Double value) {
		this.value = value;
	}

	public ParamValue(String value, boolean omitStringTypeBytes) {
		this.omitStringTypeBytes = omitStringTypeBytes;
		this.value = value;
	}
	
	public ParamValue(String value) {
		this(value, false);
	}
	
	public ParamValue(Struct value) {
		this.value = value;
	}

	public ParamValue(Boolean[] value) {
		this.value = value;
	}

	public ParamValue(Byte[] value) {
		this.value = value;
	}

	public ParamValue(Short[] value) {
		this.value = value;
	}

	public ParamValue(Integer[] value) {
		this.value = value;
	}

	public ParamValue(Long[] value) {
		this.value = value;
	}

	public ParamValue(Float[] value) {
		this.value = value;
	}

	public ParamValue(Double[] value) {
		this.value = value;
	}
	
	public ParamValue(Struct[] value) {
		this.value = value;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getValue() {
		return (T) value;
	}

	public Class<?> getValueType() {
		return value.getClass();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ParamValue(Object value) throws IllegalArgumentException {
		
		if (value == null) throw new IllegalArgumentException("Null is not a supported param value.");
		
		if (value instanceof Boolean || 
			value instanceof Byte || 
			value instanceof Short || 
			value instanceof Integer || 
			value instanceof Long || 
			value instanceof Float || 
			value instanceof Double || 
			value instanceof String ||
			value instanceof Struct ||
			value instanceof Boolean[] ||  
			value instanceof Byte[] ||   
			value instanceof Short[] ||   
			value instanceof Integer[] ||  
			value instanceof Long[] ||  
			value instanceof Float[] ||  
			value instanceof Double[] || 
			value instanceof String[] ||
			value instanceof Struct[])
			this.value = value;
		
		else if (value instanceof Map)
			this.value = new Struct((Map)value);
		
		else if (value instanceof Map[]) {
			Map[] maps = (Map[])value;			
			Struct[] structs = new Struct[maps.length];
			for (int i = 0; i < maps.length; i++)
				structs[i] = new Struct(maps[i]);
			
			this.value = structs;
		}
		else
			throw new IllegalArgumentException("Unsupported data type: " + value.getClass().getName());
	}

	protected ParamValue(ByteBuffer bb) throws IllegalArgumentException {
		this(bb, false);
	}
	
	protected ParamValue(ByteBuffer bb, boolean omitStringTypeBytes) throws IllegalArgumentException {
		
		this.omitStringTypeBytes = omitStringTypeBytes;
		
		short paramTypeVal = omitStringTypeBytes ? ParamType.ARRAY.getValue() : bb.getShort();		
		ParamType paramType = ParamType.forValue(paramTypeVal);
		
		if (paramType == null || paramType == ParamType.CHAR)
			throw new IllegalArgumentException("Invalid param type: " + paramTypeVal);

		if (paramType == ParamType.BOOLEAN)
			this.value = bb.get() != 0x00;

		else if (paramType == ParamType.BYTE)
			this.value = bb.get();

		else if (paramType == ParamType.SHORT)
			this.value = bb.getShort();

		else if (paramType == ParamType.INT)
			this.value = bb.getInt();

		else if (paramType == ParamType.LONG)
			this.value = bb.getLong();

		else if (paramType == ParamType.FLOAT)
			this.value = bb.getFloat();

		else if (paramType == ParamType.DOUBLE)
			this.value = bb.getDouble();

		else if (paramType == ParamType.STRUCT)
			this.value = new Struct(bb);
		
		else if (paramType == ParamType.ARRAY) {
			short arrayTypeVal = omitStringTypeBytes ? ParamType.CHAR.getValue() : bb.getShort();

			ParamType arrayType = ParamType.forValue(arrayTypeVal);
			if (arrayType == null)
				throw new IllegalArgumentException("Invalid array type: " + arrayTypeVal);

			short elementCount = bb.getShort();
			this.value = deserializeArray(bb, elementCount, arrayType);
		}
	}

	private Object deserializeArray(ByteBuffer bb, short elementCount, ParamType arrayType) {
		try {

			if (arrayType == ParamType.BOOLEAN)
				return deserializeBooleanArray(bb, elementCount);
			else if (arrayType == ParamType.CHAR)
				return deserializeString(bb, elementCount);
			else if (arrayType == ParamType.BYTE)
				return deserializeByteArray(bb, elementCount);
			else if (arrayType == ParamType.SHORT)
				return deserializeShortArray(bb, elementCount);
			else if (arrayType == ParamType.INT)
				return deserializeIntArray(bb, elementCount);
			else if (arrayType == ParamType.LONG)
				return deserializeLongArray(bb, elementCount);
			else if (arrayType == ParamType.FLOAT)
				return deserializeFloatArray(bb, elementCount);
			else if (arrayType == ParamType.DOUBLE)
				return deserializeDoubleArray(bb, elementCount);
			else if (arrayType == ParamType.ARRAY)
				// only arrays of char arrays are supported
				return deserializeStringArray(bb, elementCount);
			else if (arrayType == ParamType.STRUCT)
				return deserializeStructArray(bb, elementCount);
			
			return null;

		} catch (BufferUnderflowException e) {
			throw new IllegalArgumentException(
					"Failed to deserialize array of type: " + (arrayType == null ? "null" : arrayType.toString()));
		}
	}

	private Boolean[] deserializeBooleanArray(ByteBuffer bb, short elementCount) throws BufferUnderflowException {
		Boolean[] array = new Boolean[elementCount];
		for (int i = 0; i < elementCount; i++)
			array[i] = bb.get() != 0x00;
		return array;
	}

	private String deserializeString(ByteBuffer bb, short charCount) throws BufferUnderflowException {
		byte[] bytes = new byte[charCount];
		char[] chars = new char[charCount];
		bb.get(bytes);
		
		for (int i = 0; i < bytes.length; i++)
			chars[i] = (char)(bytes[i] & 0xff);
		
		return new String(chars);
	}

	private Byte[] deserializeByteArray(ByteBuffer bb, short elementCount) throws BufferUnderflowException {
		Byte[] array = new Byte[elementCount];
		for (int i = 0; i < elementCount; i++)
			array[i] = bb.get();		
		return array;
	}

	private Short[] deserializeShortArray(ByteBuffer bb, short elementCount) throws BufferUnderflowException {
		Short[] array = new Short[elementCount];
		for (int i = 0; i < elementCount; i++)
			array[i] = bb.getShort();
		return array;
	}

	private Integer[] deserializeIntArray(ByteBuffer bb, short elementCount) throws BufferUnderflowException {
		Integer[] array = new Integer[elementCount];
		for (int i = 0; i < elementCount; i++)
			array[i] = bb.getInt();
		return array;
	}

	private Long[] deserializeLongArray(ByteBuffer bb, short elementCount) throws BufferUnderflowException {
		Long[] array = new Long[elementCount];
		for (int i = 0; i < elementCount; i++)
			array[i] = bb.getLong();
		return array;
	}

	private Float[] deserializeFloatArray(ByteBuffer bb, short elementCount) throws BufferUnderflowException {
		Float[] array = new Float[elementCount];
		for (int i = 0; i < elementCount; i++)
			array[i] = bb.getFloat();
		return array;
	}

	private Double[] deserializeDoubleArray(ByteBuffer bb, short elementCount) throws BufferUnderflowException {
		Double[] array = new Double[elementCount];
		for (int i = 0; i < elementCount; i++)
			array[i] = bb.getDouble();
		return array;
	}
	
	private String[] deserializeStringArray(ByteBuffer bb, short elementCount) throws BufferUnderflowException {
		String[] array = new String[elementCount];
		for (int i = 0; i < elementCount; i++) {
			// array type
			short arrayTypeVal = bb.getShort();
			ParamType arrayType = ParamType.forValue(arrayTypeVal);
			if (arrayType == null || arrayType != ParamType.CHAR) {
				throw new IllegalArgumentException("Invalid array type: " + arrayTypeVal + " (expected 1/CHAR)");
			}
			// array length
			short arrayLength = bb.getShort();
			// array value
			array[i] = deserializeString(bb, arrayLength);	
		}
		return array;
	}
	
	private Struct[] deserializeStructArray(ByteBuffer bb, short elementCount) throws BufferUnderflowException {
		Struct[] array = new Struct[elementCount];
		for (int i = 0; i < elementCount; i++)
			array[i] = new Struct(bb);
		return array;
	}

	@Override
	public int getByteCount() {
		/*
		 * Scalar types: paramType(2 bytes) + paramContent(variable)
		 */
		if (value instanceof Boolean)
			return ParamType.BYTE_COUNT + Byte.SIZE / 8; // yes, it's 8 bit.

		if (value instanceof Byte)
			return ParamType.BYTE_COUNT + Byte.SIZE / 8;

		if (value instanceof Short)
			return ParamType.BYTE_COUNT + Short.SIZE / 8;

		if (value instanceof Integer)
			return ParamType.BYTE_COUNT + Integer.SIZE / 8;

		if (value instanceof Long)
			return ParamType.BYTE_COUNT + Long.SIZE / 8;

		if (value instanceof Double)
			return ParamType.BYTE_COUNT + Double.SIZE / 8;

		if (value instanceof Float)
			return ParamType.BYTE_COUNT + Float.SIZE / 8;

		if (value instanceof Struct)
			return ParamType.BYTE_COUNT + ((Struct)value).getByteCount(); 
		
		/*
		 * Array types: paramType(2 bytes) + arrayType(2 bytes) + itemCount(2
		 * bytes) + arrayContent(variable)
		 */
		if (value instanceof String) { // UTF-8: 1 byte per char.
			
			if (omitStringTypeBytes)
				return 2 + ((String) value).length();
			else
				return ParamType.BYTE_COUNT + ParamType.BYTE_COUNT + 2 + ((String) value).length();
		}

		if (value instanceof Boolean[])
			return ParamType.BYTE_COUNT + ParamType.BYTE_COUNT + 2 + Byte.SIZE / 8 * ((Boolean[]) value).length;

		if (value instanceof Byte[])
			return ParamType.BYTE_COUNT + ParamType.BYTE_COUNT + 2 + Byte.SIZE / 8 * ((Byte[]) value).length;

		if (value instanceof Short[])
			return ParamType.BYTE_COUNT + ParamType.BYTE_COUNT + 2 + Short.SIZE / 8 * ((Short[]) value).length;

		if (value instanceof Integer[])
			return ParamType.BYTE_COUNT + ParamType.BYTE_COUNT + 2 + Integer.SIZE / 8 * ((Integer[]) value).length;

		if (value instanceof Long[])
			return ParamType.BYTE_COUNT + ParamType.BYTE_COUNT + 2 + Long.SIZE / 8 * ((Long[]) value).length;

		if (value instanceof Float[])
			return ParamType.BYTE_COUNT + ParamType.BYTE_COUNT + 2 + Float.SIZE / 8 * ((Float[]) value).length;

		if (value instanceof Double[])
			return ParamType.BYTE_COUNT + ParamType.BYTE_COUNT + 2 + Double.SIZE / 8 * ((Double[]) value).length;

		if (value instanceof String[]) {
			int count = ParamType.BYTE_COUNT + ParamType.BYTE_COUNT + 2;
			for (String v: (String[]) value) {
				count += 4 /* array type + length */ + v.length() /* array value */;
			}
			return count;
		}
		
		if (value instanceof Struct[]) {
			int ret = ParamType.BYTE_COUNT + ParamType.BYTE_COUNT + 2;
			for (Struct s : (Struct[])value) ret += s.getByteCount();			
			return ret;		
		}		
		return 0;
	}

	protected ByteBuffer serialize(ByteBuffer bb) {
		if (value instanceof Boolean) {
			bb.putShort(ParamType.BOOLEAN.getValue());
			bb.put((Boolean) value ? (byte) 0x01 : (byte) 0x00);
		} else if (value instanceof Byte) {
			bb.putShort(ParamType.BYTE.getValue());
			bb.put((Byte) value);
		} else if (value instanceof Short) {
			bb.putShort(ParamType.SHORT.getValue());
			bb.putShort((Short) value);
		} else if (value instanceof Integer) {
			bb.putShort(ParamType.INT.getValue());
			bb.putInt((Integer) value);
		} else if (value instanceof Long) {
			bb.putShort(ParamType.LONG.getValue());
			bb.putLong((Long) value);
		} else if (value instanceof Float) {
			bb.putShort(ParamType.FLOAT.getValue());
			bb.putFloat((Float) value);
		} else if (value instanceof Double) {
			bb.putShort(ParamType.DOUBLE.getValue());
			bb.putDouble((Double) value);
		} else if (value instanceof Struct) {
			bb.putShort(ParamType.STRUCT.getValue());
			((Struct)getValue()).serialize(bb);			
		} else if (value instanceof Boolean[]) {
			bb.putShort(ParamType.ARRAY.getValue());
			bb.putShort(ParamType.BOOLEAN.getValue());
			serialize((Boolean[]) value, bb);
		} else if (value instanceof Byte[]) {
			bb.putShort(ParamType.ARRAY.getValue());
			bb.putShort(ParamType.BYTE.getValue());
			serialize((Byte[]) value, bb);
		} else if (value instanceof Short[]) {
			bb.putShort(ParamType.ARRAY.getValue());
			bb.putShort(ParamType.SHORT.getValue());
			serialize((Short[]) value, bb);
		} else if (value instanceof Integer[]) {
			bb.putShort(ParamType.ARRAY.getValue());
			bb.putShort(ParamType.INT.getValue());
			serialize((Integer[]) value, bb);
		} else if (value instanceof Long[]) {
			bb.putShort(ParamType.ARRAY.getValue());
			bb.putShort(ParamType.LONG.getValue());
			serialize((Long[]) value, bb);
		} else if (value instanceof Float[]) {
			bb.putShort(ParamType.ARRAY.getValue());
			bb.putShort(ParamType.FLOAT.getValue());
			serialize((Float[]) value, bb);
		} else if (value instanceof Double[]) {
			bb.putShort(ParamType.ARRAY.getValue());
			bb.putShort(ParamType.DOUBLE.getValue());
			serialize((Double[]) value, bb);
		} else if (value instanceof String[]) {
			// param type
			bb.putShort(ParamType.ARRAY.getValue());
			// array type
			bb.putShort(ParamType.ARRAY.getValue());
			// array length + values
			serialize((String[]) value, bb);
		} else if (value instanceof String) {
			if (!omitStringTypeBytes) {
				bb.putShort(ParamType.ARRAY.getValue());
				bb.putShort(ParamType.CHAR.getValue());
			}
			serialize((String) value, bb);
		} else if (value instanceof Struct[]) {
			bb.putShort(ParamType.ARRAY.getValue());
			bb.putShort(ParamType.STRUCT.getValue());
			serialize((Struct[]) value, bb);
		}
		return bb;
	}

	private void serialize(Boolean[] value, ByteBuffer bb) {
		bb.putShort((short) value.length);
		for (boolean v : value)
			bb.put(v ? (byte) 0x01 : (byte) 0x00);
	}

	private void serialize(Byte[] value, ByteBuffer bb) {
		bb.putShort((short) value.length);
		for (byte v : value)
			bb.put(v);
	}

	private void serialize(Short[] value, ByteBuffer bb) {
		bb.putShort((short) value.length);
		for (short v : value)
			bb.putShort(v);
	}

	private void serialize(Integer[] value, ByteBuffer bb) {
		bb.putShort((short) value.length);
		for (int v : value)
			bb.putInt(v);
	}

	private void serialize(Long[] value, ByteBuffer bb) {
		bb.putShort((short) value.length);
		for (long v : value)
			bb.putLong(v);
	}

	private void serialize(Float[] value, ByteBuffer bb) {
		bb.putShort((short) value.length);
		for (float v : value)
			bb.putFloat(v);
	}

	private void serialize(Double[] value, ByteBuffer bb) {
		bb.putShort((short) value.length);
		for (double v : value)
			bb.putDouble(v);
	}
	
	private void serialize(String[] value, ByteBuffer bb) {
		// array length
		bb.putShort((short) ((String[])value).length);
		for (String v : value) {
			// array type
			bb.putShort(ParamType.CHAR.getValue());
			// array length + values
			serialize(v, bb);
		}
	}

	private void serialize(String value, ByteBuffer bb) {
		// array length
		bb.putShort((short) value.length());		
		for (int i = 0; i < value.length(); i++)
			// array value
			bb.put((byte)value.charAt(i));
	}
	
	private void serialize(Struct[] value, ByteBuffer bb) {
		bb.putShort((short) value.length);
		for (Struct s : value)
			s.serialize(bb);			
	}

	@Override
	public String toString(){
		
		if (value == null) 
			return null;
		
		if (value instanceof Boolean || 
			value instanceof Short || 
			value instanceof Integer || 
			value instanceof Long || 
			value instanceof Float || 
			value instanceof Double || 
			value instanceof String ||
			value instanceof Struct)
			return value.toString();
		
		if (value instanceof Byte)
			return byteToHex((byte)value);
		
		String ret = "";
		if (value instanceof Boolean[]) return getArrayContent((Boolean[])getValue());
		if (value instanceof Byte[]) return getArrayContent((Byte[])getValue());
		if (value instanceof Short[]) return getArrayContent((Short[])getValue());
		if (value instanceof Integer[]) return getArrayContent((Integer[])getValue());
		if (value instanceof Long[]) return getArrayContent((Long[])getValue());
		if (value instanceof Float[]) return getArrayContent((Float[])getValue());
		if (value instanceof Double[]) return getArrayContent((Double[])getValue());	
		if (value instanceof String[]) return getArrayContent((String[])getValue());
		if (value instanceof Struct[]) return getArrayContent((Struct[])getValue());
		
		return ret;
	}
	
	private <T> String getArrayContent( T[] array ) {
		String ret = "[ ";
		for (int i = 0; i < array.length; i++)
			ret += (array[i] instanceof Byte ? byteToHex((Byte)array[i]) : array[i]) + (i+1 < array.length ? ", " : " ");		
		return ret + "]";
	}
	
	private static String byteToHex(byte b) {
		char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		int b0 = (b & 0xf0) >> 4;
		int b1 = b & 0x0f;
		return "0x" + hexChars[b0] + hexChars[b1];
	}
	
	public Object asGeneric() {
		if (getValueType() == Struct.class)
			return ((Struct) getValue()).asMap();

		if (getValueType() == Struct[].class)
			return Struct.asMapArray((Struct[]) getValue());
		
		return getValue();
	}
	
}
