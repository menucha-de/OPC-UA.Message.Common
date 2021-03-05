package havis.opcua.message.common.model;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Notification extends Message {

	private Map<ParamId, ParamValue> paramMap;

	public Notification() {
		super(MessageType.NOTIFICATION, MessageIdSeed.next());
		this.paramMap = new LinkedHashMap<>();
	}

	public Notification(ByteBuffer bb) {
		super(bb);
		
		if (getMessageHeader().getMessageType() != MessageType.NOTIFICATION)
			throw new IllegalArgumentException("Invalid message type: " + getMessageHeader().getMessageType());
		
		short numOfParams = bb.getShort();		
		this.paramMap = new LinkedHashMap<>();
		
		for (short s = 0; s < numOfParams; s++) {
			ParamId key = new ParamId(bb);
			ParamValue value = new ParamValue(bb);			
			this.paramMap.put(key, value);
		}
	}
	
	public Notification(MessageHeader mh, ByteBuffer bb) {
		super(mh);
		
		if (getMessageHeader().getMessageType() != MessageType.NOTIFICATION)
			throw new IllegalArgumentException("Invalid message type: " + getMessageHeader().getMessageType());
		
		short numOfParams = bb.getShort();
		
		this.paramMap = new LinkedHashMap<>();
		for (short s = 0; s < numOfParams; s++) {
			ParamId key = new ParamId(bb);
			ParamValue value = new ParamValue(bb);
			this.paramMap.put(key, value);
		}
	}

	public Map<ParamId, ParamValue> getParamMap() {
		return paramMap;
	}

	public void setParamMap(Map<ParamId, ParamValue> paramMap) {
		this.paramMap = paramMap;
	}
	
	public void setUntypedParamMap(Map<String, Object> paramMap) {
		for (Entry<String, Object> e : paramMap.entrySet()) {
			ParamId pKey = new ParamId(e.getKey(), true);
			ParamValue pValue = new ParamValue(e.getValue());			
			this.paramMap.put(pKey, pValue);			
		}
	}
	
	@Override
	public int getByteCount() {
		int ret = super.getByteCount();
		
		ret += 2; //num of params (short)
		
		for (Entry<ParamId, ParamValue> e : paramMap.entrySet()) {
			ret += e.getKey().getByteCount();
			ret += e.getValue().getByteCount();
		}
		return ret;
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb) {
		getMessageHeader().serialize(bb);
		
		bb.putShort((short)getParamMap().size());
		
		for (Entry<ParamId, ParamValue> e : paramMap.entrySet()) {
			e.getKey().serialize(bb);
			e.getValue().serialize(bb);
		}
		
		return bb;
	}

	@Override
	public String toString() {
		String mapStr = "";
		
		Iterator<Entry<ParamId, ParamValue>> itEntries = paramMap.entrySet().iterator();
		while (itEntries.hasNext())
		{
			Entry<ParamId, ParamValue> entry = itEntries.next();
			mapStr += " { paramId = " + entry.getKey().toString() + ",";
			mapStr += " paramValue = " + entry.getValue().toString() + " }";
			if (itEntries.hasNext()) mapStr += ",";
		}
		
		return "{ " +  super.toString() + ", paramMap = " + "{" + mapStr + " }" + " }";		
	}

	public Map<String, Object> getUntypedMap() {
		Map<String, Object> untypedMap = new HashMap<>();
		
		for (Entry<ParamId, ParamValue> entry : this.paramMap.entrySet())
			untypedMap.put(entry.getKey().toString(), entry.getValue().asGeneric());
		
		return untypedMap;
	}	
}
