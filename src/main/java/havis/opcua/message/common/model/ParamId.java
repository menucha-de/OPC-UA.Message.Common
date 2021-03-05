package havis.opcua.message.common.model;

import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParamId implements MessagePart {

	private static final String NAMESPACE_INDEX_GROUP = "ns";
	private static final String IDENTIFIER_TYPE_GROUP = "type";
	private static final String IDENTIFIER_GROUP = "id";
	private static final Pattern ID_PATTERN = Pattern.compile("^(ns=(?<" + NAMESPACE_INDEX_GROUP + ">-?\\d+);)?(?<" + IDENTIFIER_TYPE_GROUP + ">[isgb])=(?<"
			+ IDENTIFIER_GROUP + ">.+)$");

	private int namespaceIndex = -1;
	private Object value;

	public ParamId(int namespaceIndex, String value) {
		this(value, false);
		this.namespaceIndex = namespaceIndex;
	}

	public ParamId(int namespaceIndex, int value) {
		this.namespaceIndex = namespaceIndex;
		this.value = value;
	}

	public ParamId(String value, boolean tryParse) {
		if (tryParse) {
			Matcher m;
			if ((m = ID_PATTERN.matcher(value)).matches()) {
				String ns = m.group(NAMESPACE_INDEX_GROUP);
				this.namespaceIndex = ns != null && ns.length() > 0 ? Integer.parseInt(m.group(NAMESPACE_INDEX_GROUP)) : 0;
				char type = m.group(IDENTIFIER_TYPE_GROUP).charAt(0);
				String id = m.group(IDENTIFIER_GROUP);
				switch (type) {
				case 'i':
					try {
						this.value = Integer.parseInt(id);
					} catch (NumberFormatException nfe) {
						throw new IllegalArgumentException("Invalid numeric param id " + id);
					}
					break;
				case 's':
					this.value = id;
					break;
				default:
					throw new UnsupportedOperationException("Unsupported identifier type " + type);
				}
			} else {
				// likely the old format
				if (value.startsWith("#")) {
					try {
						this.value = Integer.parseInt(value.substring(1));
					} catch (NumberFormatException nfe) {
						this.value = value;
					}
				} else {
					this.value = value;
				}
			}
		} else {
			this.value = value;
		}
	}
	
	protected ParamId(ByteBuffer bb) throws IllegalArgumentException {
		
		this.namespaceIndex = bb.getShort() & 0xFFFF;
		byte type = bb.get();
		if (type == 0x00) { //numeric
			this.value = bb.getInt();
		}
		else if (type == 0x01) { //alphanumeric
			final short idLen = bb.getShort();
			
			try {
				byte[] idData = new byte[idLen];
				bb.get(idData);
				
				char[] chars = new char[idData.length];
				for(int i = 0; i < idData.length; i++) 
					chars[i] = (char)(idData[i] & 0xff);
				
				this.value = new String(chars);
				
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid param id.", e);
			}
		}
		else { //something weird
			throw new IllegalArgumentException("Invalid param id type: " + type);
		}
		
	}

	public int getNamespaceIndex() { return namespaceIndex; }
	public void setNamespaceIndex(int index) { namespaceIndex = index; }

	@SuppressWarnings("unchecked")
	public <T> T getValue() { return (T)value; }
	
	public boolean isNumeric() { return value instanceof Integer; }
	public boolean isAlphanumeric() { return !isNumeric(); }

	@Override
	public int getByteCount() {
		
		/* numeric: 
		 * namespaceIndex: 2 byte
		 * type: 1 byte
		 * value: 4 bytes
		 * = 7 byte */
		
		/* alphanumeric: 
		 * namespaceIndex: 2 byte 
		 * type: 1 bytes
		 * length: 2 bytes 
		 * value.len */
		
		return isNumeric() ? 7 : (5 + ((String)value).length()); 				
	} 

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + namespaceIndex;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ParamId other = (ParamId) obj;
		if (namespaceIndex != other.namespaceIndex)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	protected void serialize(ByteBuffer bb) {		
		bb.putShort((short)getNamespaceIndex());
		if (isNumeric()) { 
			bb.put((byte)0x0);
			bb.putInt((int)getValue());
		}
		else {
			bb.put((byte)0x1);
			String sVal = getValue();
			short sLen = (short)(sVal.length() & 0xffff); 
			bb.putShort(sLen);
			
			for (int i = 0; i < sVal.length(); i++)
				bb.put((byte)sVal.charAt(i));		
		}
	}

	@Override
	public String toString() {		
		return "ns=" + namespaceIndex + ';' + (isNumeric() ? "i=" : "s=") + value;
	}
}