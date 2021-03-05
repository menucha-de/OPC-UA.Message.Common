package havis.opcua.message.common.model;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Struct implements MessagePart {
	
	private ParamId structId;
	private List<StructField> fields;
	
	protected Struct(ByteBuffer bb) {
		this.structId = new ParamId(bb);
		this.fields = new ArrayList<>();
		
		short fieldCount = bb.getShort();
		for (short s = 0; s < fieldCount; s++)
			fields.add(new StructField(bb));
	}
	
	public Struct(ParamId structId) {
		super();
		
		this.structId = structId;
		this.fields = new ArrayList<>();		
	}

	public Struct(Map<String, Object> map) {
		super();
		this.fields = new ArrayList<>();
		Iterator<Entry<String, Object>> itEntries = map.entrySet().iterator();
		
		while (itEntries.hasNext()) {
			Entry<String, Object> entry = itEntries.next();
			if (entry.getKey().trim().equals("@id"))
				this.structId = new ParamId(entry.getValue().toString(), true);
			else 
				this.fields.add(new StructField(entry.getKey().trim(), 
								new ParamValue(entry.getValue())));
		}
	}

	public short getFieldCount() {
		return (short) this.fields.size();
	}
	
	public List<StructField> getFields() {
		return fields;
	}

	@Override
	public int getByteCount() {
		int byteCount = this.structId.getByteCount() + /* field count */ Short.SIZE / 8;
		for (StructField sf : fields) byteCount += sf.getByteCount();		
		return byteCount;
	}
	
	protected void serialize(ByteBuffer bb) {
		structId.serialize(bb);
		bb.putShort(getFieldCount());
		for(StructField sf : fields) sf.serialize(bb);
	}

	@Override
	public String toString() {
		String fieldStr = "";
		Iterator<StructField> itFields = fields.iterator();
		
		while (itFields.hasNext()) {
			fieldStr += itFields.next().toString();			
			if (itFields.hasNext()) fieldStr += ",";
		}
		
		return "{ structId = " + structId + ", fields = [ " + fieldStr + " ] }";
	}
	
	@SuppressWarnings("rawtypes")
	public Map<String, Object> asMap() {		
		Map<String, Object> map = new LinkedHashMap<>();
		
		if (this.structId != null) map.put("@id", this.structId.toString());
		
		for (StructField sf : this.fields) {
			if (sf.getValue().getValueType() == Struct.class)
				map.put(sf.getName(), ((Struct)sf.getValue().getValue()).asMap());
			
			else if (sf.getValue().getValueType() == Struct[].class) {
				Struct[] structs = (Struct[]) sf.getValue().getValue();
				Map[] maps = new Map[structs.length];
				
				for (int i = 0; i < maps.length; i++)
					maps[i] = structs[i].asMap();
				
				map.put(sf.getName(), maps);
			}
			
			else map.put(sf.getName(), sf.getValue().getValue());
		}

		return map;
	}

	@SuppressWarnings("rawtypes")
	public static Map[] asMapArray(Struct[] structs) {		
		Map[] maps = new Map[structs.length];
		for (int i = 0; i < maps.length; i++)
			maps[i] = structs[i].asMap();		
		return maps;
	}
}
