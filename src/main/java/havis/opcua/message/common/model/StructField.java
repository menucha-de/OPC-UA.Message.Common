package havis.opcua.message.common.model;

import java.nio.ByteBuffer;

public class StructField implements MessagePart {
	private ParamValue name;
	private ParamValue value;
	
	public StructField(String name, ParamValue value) {
		super();
		this.name = new ParamValue(name, true);
		this.value = value;
	}
	
	protected StructField(ByteBuffer bb) {
		this.name = new ParamValue(bb, true);
		this.value = new ParamValue(bb);
	}
	
	public String getName() {
		return this.name == null ? null : (String)this.name.getValue();
	}
	public void setName(String name) {
		this.name = new ParamValue(name);
	}
	public ParamValue getValue() {
		return value;
	}
	public void setValue(ParamValue value) {
		this.value = value;
	}
	
	@Override
	public int getByteCount() {
		return				
			/* byte count of name (ParamValue) */	
			this.name.getByteCount() + 
			
			/* byte count of value (ParamValue) */
			this.value.getByteCount();		
	}

	protected void serialize(ByteBuffer bb) {
		name.serialize(bb);
		value.serialize(bb);
	}
	
	@Override
	public String toString() {
		return "{ " +  
			"name = " + (name == null ? null : name.getValue().toString()) +
			", value = " + value +
			" }";
	}
	
}