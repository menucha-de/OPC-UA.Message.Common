package havis.opcua.message.common.model;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Event extends Message {

	private ParamId eventTypeId;
	private ParamId paramId;
	private Date timestamp;
	private int severity;
	private ParamValue message;
	private Map<ParamId, ParamValue> paramMap;
	
	public Event() {
		super(MessageType.EVENT, MessageIdSeed.next());
		this.paramMap = new LinkedHashMap<>();
	}

	public Event(ByteBuffer bb) {
		super(bb);
		
		if (getMessageHeader().getMessageType() != MessageType.EVENT)
			throw new IllegalArgumentException("Invalid message type: " + getMessageHeader().getMessageType());
		
		this.eventTypeId = new ParamId(bb);
		this.paramId = new ParamId(bb);
		this.timestamp = new Date(bb.getLong());
		this.severity = bb.getInt();
		this.message = new ParamValue(bb, true);
		
		short numOfParams = bb.getShort();		
		this.paramMap = new LinkedHashMap<>();
		
		for (short s = 0; s < numOfParams; s++) {
			ParamId key = new ParamId(bb);
			ParamValue value = new ParamValue(bb);			
			this.paramMap.put(key, value);
		}
	}
	
	public Event(MessageHeader mh, ByteBuffer bb) {
		super(mh);
		
		if (getMessageHeader().getMessageType() != MessageType.EVENT)
			throw new IllegalArgumentException("Invalid message type: " + getMessageHeader().getMessageType());
		
		this.eventTypeId = new ParamId(bb);
		this.paramId = new ParamId(bb);
		this.timestamp = new Date(bb.getLong());
		this.severity = bb.getInt();
		this.message = new ParamValue(bb, true);
		
		short numOfParams = bb.getShort();
		
		this.paramMap = new LinkedHashMap<>();
		for (short s = 0; s < numOfParams; s++) {
			ParamId key = new ParamId(bb);
			ParamValue value = new ParamValue(bb);
			this.paramMap.put(key, value);
		}
	}
	
	public Event(String eventTypeId, String paramId, Date timestamp, int severity, String message) {
		this();
		
		this.eventTypeId = new ParamId(eventTypeId, true);
		this.paramId = new ParamId(paramId, true);
		this.timestamp = timestamp;
		this.severity = severity;
		this.message = new ParamValue(message, true);
		
	}

	public ParamId getEventTypeId() {
		return eventTypeId;
	}

	public void setEventTypeId(ParamId eventTypeId) {
		this.eventTypeId = eventTypeId;
	}

	public ParamId getParamId() {
		return paramId;
	}

	public void setParamId(ParamId paramId) {
		this.paramId = paramId;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public int getSeverity() {
		return severity;
	}

	public void setSeverity(int severity) {
		this.severity = severity;
	}

	public String getMessage() {
		return message == null ? null : (String) message.getValue();
	}

	public void setMessage(String message) {
		this.message = new ParamValue(message, true);
	}

	public Map<ParamId, ParamValue> getParamMap() {
		return paramMap;
	}

	public void setParamMap(Map<ParamId, ParamValue> paramMap) {
		this.paramMap = paramMap;
	}
	
	public void setUntypedParamMap(Map<String, Object> paramMap) {
		for (Entry<String, Object> e : paramMap.entrySet())
			this.put(e.getKey(), e.getValue());
	}
	
	public Map<String, Object> getUntypedParamMap() {
		Map<String, Object> untypedMap = new HashMap<>();
		
		for (Entry<ParamId, ParamValue> entry : this.paramMap.entrySet())
			untypedMap.put(entry.getKey().toString(), entry.getValue().asGeneric());
		
		return untypedMap;
	}
	
	public ParamValue put(String key, Object value) throws IllegalArgumentException {		
		ParamId pKey = new ParamId(key, true);
		ParamValue pValue = new ParamValue(value);		
		return this.paramMap.put(pKey, pValue);
	}

	@Override
	public int getByteCount() {
		int ret = 
			super.getByteCount() + 
			this.eventTypeId.getByteCount() + 
			this.paramId.getByteCount() + 
			Long.SIZE / 8 + /* timestamp */
			Integer.SIZE / 8 + /* severity */
			this.message.getByteCount();
		
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
		
		getEventTypeId().serialize(bb);
		getParamId().serialize(bb);
		bb.putLong(getTimestamp().getTime());
		bb.putInt(getSeverity());
		this.message.serialize(bb);
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
		
		return "{ " +  super.toString() + 
				", eventTypeId = " + eventTypeId + 
				", paramId = " + paramId + 
				", timestamp = " + timestamp + 
				", severity = " + severity + 
				", message = " + (message == null ? null : message.getValue()) +				
				", paramMap = " + "{" + mapStr + " }" + " }";		
	}	
}
