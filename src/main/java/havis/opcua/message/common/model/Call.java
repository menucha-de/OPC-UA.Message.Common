package havis.opcua.message.common.model;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Call extends Message {

	private ParamId methodId;
	private ParamId paramId;
	private List<ParamValue> paramList;
	
	public Call(ParamId methodId, ParamId paramId, Object[] params, int messageId) {
		super(MessageType.CALL, messageId);
		
		this.methodId = methodId;
		this.paramId = paramId;
		
		this.paramList = new ArrayList<>();
		
		for (Object param : params) 
			paramList.add(new ParamValue(param));
	}

	public Call(MessageHeader mh, ByteBuffer bb) {
		super(mh);

		if (getMessageHeader().getMessageType() != MessageType.CALL)
			throw new IllegalArgumentException("Invalid message type: " + getMessageHeader().getMessageType());
		
		this.methodId = new ParamId(bb);
		this.paramId = new ParamId(bb);
		
		short numOfParams = bb.getShort();
		this.paramList = new ArrayList<>();
		for (short s = 0; s < numOfParams; s++)
			this.paramList.add(new ParamValue(bb));
	}
	
	public Call(ByteBuffer bb) {
		super(bb);

		if (getMessageHeader().getMessageType() != MessageType.CALL)
			throw new IllegalArgumentException("Invalid message type: " + getMessageHeader().getMessageType());
		
		this.methodId = new ParamId(bb);
		this.paramId = new ParamId(bb);
		
		short numOfParams = bb.getShort();
		this.paramList = new ArrayList<>();
		for (short s = 0; s < numOfParams; s++)
			this.paramList.add(new ParamValue(bb));				
	}
	
	public ParamId getMethodId() {
		return methodId;
	}

	public void setMethodId(ParamId methodId) {
		this.methodId = methodId;
	}

	public ParamId getParamId() {
		return paramId;
	}

	public void setParamId(ParamId paramId) {
		this.paramId = paramId;
	}

	public List<ParamValue> getParamList() {
		return paramList;
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb) {
		getMessageHeader().serialize(bb);
		getMethodId().serialize(bb);
		getParamId().serialize(bb);
		bb.putShort((short) getParamList().size());
		for (ParamValue pv : getParamList())
			pv.serialize(bb);
		return bb;
	}

	@Override
	public int getByteCount() {
		
		int byteCount = 
			super.getByteCount() + 
			getMethodId().getByteCount() +
			getParamId().getByteCount() + 
			Short.SIZE / 8;
		
		
		
		for (ParamValue pv : this.getParamList())
			byteCount += pv.getByteCount();
		
		return byteCount;
	}	
	
	@Override
	public String toString() {		
		String paramListStr = "";
		
		Iterator<ParamValue> itEntries = paramList.iterator();
		while (itEntries.hasNext()) {
			paramListStr += itEntries.next().toString();
			if (itEntries.hasNext()) paramListStr += ",";
		}
		
		return "{ " +  super.toString() +				
		", methodId = " + methodId +
		", paramId = " + paramId +
		", paramList = " + "[" + paramListStr + "]" 
		+ " }"; 
				
	}
}
