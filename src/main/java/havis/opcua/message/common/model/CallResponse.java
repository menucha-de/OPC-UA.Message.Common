package havis.opcua.message.common.model;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CallResponse extends Message {

	private Status status;
	private List<ParamValue> resultList;
	private ParamId methodId;
	private ParamId paramId;
	
	
	public CallResponse(int messageId, Status status) {
		super(MessageType.CALL_RESPONSE, messageId);
		this.status = status;
	}

	public CallResponse(Call callMessage, List<ParamValue> resultList, Status status) {
		super(MessageType.CALL_RESPONSE, callMessage.getMessageHeader().getMessageId());
		this.methodId = callMessage.getMethodId();
		this.paramId = callMessage.getParamId();		
		this.resultList = resultList; 
		this.status = status;
	}

	public CallResponse(MessageHeader mh, ByteBuffer bb) throws IllegalArgumentException {
		super(mh);

		if (getMessageHeader().getMessageType() != MessageType.CALL_RESPONSE)
			throw new IllegalArgumentException("Invalid message type: " + getMessageHeader().getMessageType());

		this.status = Status.forValue(bb.getShort());
		if (status == null)
			throw new IllegalArgumentException("Invalid message status.");

		if (status == Status.SUCCESS || status == Status.APPLICATION_ERROR) {
			this.methodId = new ParamId(bb);
			this.paramId = new ParamId(bb);
			short numOfParams = bb.getShort();
			
			this.resultList = new ArrayList<>();
			for (short s = 0; s < numOfParams; s++)
				this.resultList.add(new ParamValue(bb));
			
		}
	}

	public CallResponse(ByteBuffer bb) throws IllegalArgumentException {
		super(bb);

		if (getMessageHeader().getMessageType() != MessageType.CALL_RESPONSE)
			throw new IllegalArgumentException("Invalid message type: " + getMessageHeader().getMessageType());

		this.status = Status.forValue(bb.getShort());
		if (status == null)
			throw new IllegalArgumentException("Invalid message status.");

		if (status == Status.SUCCESS || status == Status.APPLICATION_ERROR) {
			this.methodId = new ParamId(bb);
			this.paramId = new ParamId(bb);
			short numOfParams = bb.getShort();
			
			this.resultList = new ArrayList<>();
			for (short s = 0; s < numOfParams; s++)
				this.resultList.add(new ParamValue(bb));
			
		}
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public ParamId getParamId() {
		return paramId;
	}

	public void setParamId(ParamId paramId) {
		this.paramId = paramId;
	}
	
	public ParamId getMethodId() {
		return methodId;
	}

	public void setMethodId(ParamId methodId) {
		this.methodId = methodId;
	}

	public List<ParamValue> getResultList() {
		return resultList;
	}

	@Override
	public int getByteCount() {
		
		int byteCount = 
			super.getByteCount() + 
			Status.BYTE_COUNT;
		
		if (getStatus() != Status.SUCCESS && getStatus() != Status.APPLICATION_ERROR)
			return byteCount;
		
		byteCount += 
			getMethodId().getByteCount() + 
			getParamId().getByteCount() + 
			Short.SIZE / 8;
			
		for (ParamValue pv : getResultList())
			byteCount += pv.getByteCount();
		
		return byteCount;
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb) {		
		getMessageHeader().serialize(bb);
		getStatus().serialize(bb);
		
		if (getStatus() != Status.SUCCESS && getStatus() != Status.APPLICATION_ERROR)
			return bb;
		
		getMethodId().serialize(bb);
		getParamId().serialize(bb);
		bb.putShort((short) getResultList().size());
		for (ParamValue pv : getResultList())
			pv.serialize(bb);
		return bb;
	}
	
	@Override
	public String toString() {	
		if (status == Status.SUCCESS || status == Status.APPLICATION_ERROR) {			
			String resultListStr = "";
			Iterator<ParamValue> itEntries = resultList.iterator();
			while (itEntries.hasNext()) {
				resultListStr += itEntries.next().toString();
				if (itEntries.hasNext()) resultListStr += ",";
			}
			return "{ " +  super.toString() +				
				", status = " + status +
				", methodId = " + methodId +
				", paramId = " + paramId +
				", paramList = " + "[" + resultListStr + "]" 
				+ " }"; 			
		}
		else return "{ " +  super.toString() + ", status = " + status + " }";		
	}
}
