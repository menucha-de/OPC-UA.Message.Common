package havis.opcua.message.common.model;

import static mockit.Deencapsulation.setField;
import static org.junit.Assert.assertArrayEquals;
import havis.opcua.message.common.serialize.MessageSerializer;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public class MessageSerializeTest {
	
	@Test
	public void testSerializeRead() {
		Read rd = new Read(new ParamId(0, 0xffffffff), 0xffffffff);
		
		byte[] act = MessageSerializer.serialize(rd); 		
		byte[] exp = new byte[] { 
				0x00, 0x00, /* message type: read */ 			 
				0x00, 0x00, 0x00, 0x11, /* message len: 17 bytes */
				(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, /* message id: 0xffffffff */
				0x00, 0x00,
				0x00, /* param id type: num */  
				(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF /* param id type: 0xffffffff */  
			};
			
		assertArrayEquals(exp, act);		
	}

	@Test
	public void testSerializeReadResponse() {
		ParamId paramId = new ParamId(0, "foo");
		Read r = new Read(paramId, 0xffffffff);
		ParamValue pv = new ParamValue("bar");
		ReadResponse rr = new ReadResponse(r, pv, Status.SUCCESS);
		byte[] act = MessageSerializer.serialize(rr);
		
		byte[] exp = 
			new byte[] {
				0x00, 0x01, /* message type: read_resp */ 			
				0x00, 0x00, 0x00, 0x1D, /* message len: 29 bytes */  			
				(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, /* message id: 0xffffffff */  
				0x00, 0x00, /* status: SUCCESS */
				0x00, 0x00,
				0x01, /* param id type: alphanum */  
				0x00, 0x03, /* param id len: 3 */  
				0x66, 0x6F, 0x6F, /* param id str: "foo" */  
				0x00, 0x08, /* param type: array */  
				0x00, 0x01, /* array type: char */  
				0x00, 0x03, /* elem count: 3 */  
				0x62, 0x61, 0x72 /* payload: "bar" */ 
			};
		
		assertArrayEquals(exp, act);
	}
	
	@Test
	public void testSerializeWrite() {
		ParamId paramId = new ParamId(0, "foo");
		ParamValue paramValue = new ParamValue("bar");
		Write w = new Write(paramId, paramValue, 0xffffffff);			
		byte[] act = MessageSerializer.serialize(w);				
		
		byte[] exp = 
				new byte[] {
					0x00, 0x02, /* message type: write */ 			
					0x00, 0x00, 0x00, 0x1B, /* message len: 27 bytes */  			
					(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, /* message id: 0xffffffffL */  
					0x00, 0x00,
					0x01, /* param id type: alphanum */  
					0x00, 0x03, /* param id len: 3 */  
					0x66, 0x6F, 0x6F, /* param id str: "foo" */  
					0x00, 0x08, /* param type: array */  
					0x00, 0x01, /* array type: char */  
					0x00, 0x03, /* elem count: 3 */  
					0x62, 0x61, 0x72 /* payload: "bar" */ 
				};
		
		assertArrayEquals(exp, act);
	}
	
	@Test
	public void testSerializeWriteResponse() {
		Write w = new Write(new ParamId(0, 0xffffffff), new ParamValue(0xffffffff), 0xffffffaa);		
		WriteResponse wr = new WriteResponse(w, Status.SUCCESS);
		
		byte[] act = MessageSerializer.serialize(wr);
		
		byte[] exp = new byte[] {
				0x00, 0x03, /* message type: write_resp */
				0x00, 0x00, 0x00, 0x0C, /* message len: 12 bytes */
				(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xAA, /* message id: 0xffffffaa */
				0x00, 0x00 /* status: SUCCESS */
			};
		
		assertArrayEquals(exp, act);
	}
	
	@Test
	public void testSerializeSubscribe() {
		Subscribe s = new Subscribe(new ParamId(0, 0xffffffff), 0xffffffff);
		byte[] act = MessageSerializer.serialize(s);
		
		byte[] exp = new byte[] { 
				0x00, 0x04, /* message type: subscribe */ 			 
				0x00, 0x00, 0x00, 0x11, /* message len: 17 bytes */
				(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, /* message id: 0xffffffff */
				0x00, 0x00,
				0x00, /* param id type: num */  
				(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF /* param id type: 0xffffffff */  
			};
		assertArrayEquals(exp, act);
	}
	
	@Test
	public void testSerializeSubscribeResponse() {
		Subscribe s = new Subscribe(new ParamId(0, 0xffffffff), 0xffffaaff);		
		SubscribeResponse sr = new SubscribeResponse(s, Status.INVALID_MESSAGE);
		
		byte[] act = MessageSerializer.serialize(sr);
		byte[] exp = new byte[] {
				0x00, 0x05, /* message type: subscribe_resp */
				0x00, 0x00, 0x00, 0x0C, /* message len: 12 bytes */
				(byte)0xFF, (byte)0xFF, (byte)0xAA, (byte)0xFF, /* message id: 0xffffaaff */
				0x00, 0x64 /* status: INVALID_MESSAGE */
			};
		
		assertArrayEquals(exp, act);
	}

	@Test
	public void testSerializeUnsubscribe() {
		Unsubscribe s = new Unsubscribe(new ParamId(0, 0xffffffff), 0xffffffff);
		byte[] act = MessageSerializer.serialize(s);
		
		byte[] exp = new byte[] { 
				0x00, 0x06, /* message type: subscribe */ 			 
				0x00, 0x00, 0x00, 0x11, /* message len: 15 bytes */
				(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, /* message id: 0xffffffff */  
				0x00, 0x00,
				0x00, /* param id type: num */  
				(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF /* param id type: 0xffffffff */  
			};
		assertArrayEquals(exp, act);
	}
	
	@Test
	public void testSerializeUnsubscribeResponse() {
		Unsubscribe s = new Unsubscribe(new ParamId(0, 0xffffffff), 0xffaaffff);		
		UnsubscribeResponse sr = new UnsubscribeResponse(s, Status.UNSUPPORTED_MESSAGE);
		byte[] act = MessageSerializer.serialize(sr);
		
		byte[] exp = new byte[] {
				0x00, 0x07, /* message type: unsubscribe_resp */
				0x00, 0x00, 0x00, 0x0C, /* message len: 12 bytes */
				(byte)0xFF, (byte)0xAA, (byte)0xFF, (byte)0xFF, /* message id: 0xffffaaff */
				0x00, 0x65 /* status: UNSUPPORTED_MESSAGE */
			};
		assertArrayEquals(exp, act);
	}
	
	@Test
	public void testSerializeNotification() {
		Notification n = new Notification();
		
		n.getParamMap().put(new ParamId(0, "foo"), new ParamValue("bar"));
		n.getParamMap().put(new ParamId(0, 23), new ParamValue((short)42));
		n.getParamMap().put(new ParamId(0, "pi"), new ParamValue(Math.PI));		
		n.getMessageHeader().setMessageLength(n.getByteCount());		
		setField(n.getMessageHeader(), "messageId", 1);
		
		byte[] act = MessageSerializer.serialize(n);
		
		byte[] exp = new byte[] {
			0x00, 0x08, //msg type: notification
			0x00, 0x00, 0x00, 0x39, //msg len: 57 bytes 
			0x00, 0x00, 0x00, 0x01, //msg id: 1
			
			0x00, 0x03, //num of params: 3
			
			/* 1st paramId */ 
			0x00, 0x00,
			0x01, // type: alphanum
			0x00, 0x03, // len: 3
			0x66, 0x6F, 0x6F, // val: "foo"
			
			/* 1st paramVal */
			0x00, 0x08, // data type: array
			0x00, 0x01, // array type: char
			0x00, 0x03, // elem count: 3
			0x62, 0x61, 0x72, // val: "bar" 
			
			/* 2nd paramId */
			0x00, 0x00,
			0x00, // type: num
			0x00, 0x00, 0x00, 0x17, //val: 23
			
			/* 2nd paramVal */
			0x00, 0x03, // type: short
			0x00, 0x2A, // val: 42
			
			/* 3rd paramId */
			0x00, 0x00,
			0x01, // type: alphanum
			0x00, 0x02, // len: 2
			0x70, 0x69, // val: "pi"
			
			/* 3rd paramVal */
			0x00, 0x07, // type: double
			0x40, 0x09, 0x21, (byte)0xFB, 0x54, 0x44, 0x2D, 0x18 // val: 3.14....
		};
		
		assertArrayEquals(exp, act);
		
		n = new Notification();		
		n.getParamMap().put(new ParamId(0, "foo"), new ParamValue("bar"));
		n.getParamMap().put(new ParamId(0, 23), new ParamValue((short)42));
		n.getParamMap().put(new ParamId(0, "pi"), new ParamValue(Math.PI));		
		n.getMessageHeader().setMessageLength(n.getByteCount());
		setField(n.getMessageHeader(), "messageId", 2);
		act = MessageSerializer.serialize(n);		
		
		//set correct message id		
		exp[9] = 0x02; 
		assertArrayEquals(exp, act);
	}
	
	byte[] eventData = new byte[] {
		0x00, 0x09, /* msg type */
		0x00, 0x00, 0x00, (byte)0xBC, /* msg len: 188 */ 
		0x00, 0x00, 0x00, 0x00, /* msg id */

		0x01, /* event id type: alphanum */
		0x00, 0x03, /*  event id len: 3 */
		0x66, 0x6F, 0x6F, /* event id: foo  */

		0x00, 0x00,
		0x00, /* param id type: num */
		0x00, 0x00, 0x00, 0x2A, /* param id: 42 */ 

		0x00, 0x00, 0x01, 0x54, (byte)0xA4, (byte)0x8F, 0x6D, (byte)0xEF, /* timestamp */ 

		0x00, 0x00, 0x00, 0x05, /* severity */
		
		0x00, 0x03, /* char count: 3 */
		0x62, 0x61, 0x72, /* "bar" */ 

		0x00, 0x04, /* num of params */ 

		0x00, 0x00,
		0x01, /* param id type: alphanum */
		0x00, 0x0C, /* char count: 12 */
		0x61, 0x63, 0x63, 0x6F, 0x75, 0x6E, 0x74, 0x50, 0x61, 0x72, 0x61, 0x6D, /* "accountParam" */ 

		0x00, 0x09, /* param type: struct */
		
		0x01, /* struct id type: alphanum */
		0x00, 0x07, /* char count: 7 */
		0x61, 0x63, 0x63, 0x6F, 0x75, 0x6E, 0x74, /* "account" */ 

		0x00, 0x03, /* field count: 3 */

		0x00, 0x08, /* field name type: array */
		0x00, 0x01, /* field name array type: char */
		0x00, 0x07, /* char count: 7 */
		0x75, 0x73, 0x65, 0x72, 0x5F, 0x69, 0x64, /* "user_id" */ 

		0x00, 0x04, /* field value type: int */
		0x00, 0x00, 0x00, 0x2A, /* field value: 42 */ 

		0x00, 0x08, /* field name type: array */ 
		0x00, 0x01, /* field name array type: char */
		0x00, 0x0A, /* char count: 10 */
		0x66, 0x69, 0x72, 0x73, 0x74, 0x5F, 0x6E, 0x61, 0x6D, 0x65, /* "first_name" */ 

		0x00, 0x08, /* field name type: array */
		0x00, 0x01, /* field name array type: char */
		0x00, 0x09, /* char count: 9 */
		0x46, 0x69, 0x72, 0x73, 0x74, 0x6e, 0x61, 0x6d, 0x65, /* "Firstname" */

		0x00, 0x08, /* field name type: array */
		0x00, 0x01, /* field name array type: char*/
		0x00, 0x09, /* char count: 9 */
		0x6C, 0x61, 0x73, 0x74, 0x5F, 0x6E, 0x61, 0x6D, 0x65, /* "last_name" */ 

		0x00, 0x08, /* field name type: array */
		0x00, 0x01, /* field name array type: char*/
		0x00, 0x05, /* char count: 4 */
		0x4c, 0x61, 0x73, 0x74, 0x6e, 0x61, 0x6d, 0x65, /* "Lastname" */

		0x00, 0x00,
		0x01, /*  param id type: alphanum */
		0x00, 0x03, /* char count: 3 */
		0x66, 0x6F, 0x6F, /* "foo" */

		0x00, 0x08, /* param value type: array */
		0x00, 0x01, /* param value array type: char */
		0x00, 0x03, /* char count: 3 */
		0x62, 0x61, 0x72, /* "bar" */ 

		0x00, 0x00,
		0x00, /* parm id type: numeric*/
		0x00, 0x00, 0x00, 0x17, /* value: 23 */ 

		0x00, 0x03, /* value type: short */
		0x00, 0x2A, /* value: 42 */

		0x00, 0x00,
		0x01, /* param id type: alphanum */ 
		0x00, 0x02, /* char count: 2 */
		0x70, 0x69, /* value: pi */

		0x00, 0x07, /*  */
		0x40, 0x09, 0x21, (byte)0xFB, 0x54, 0x44, 0x2D, 0x18 /* */
	};

	
	@Test
	public void testSerializeEvent() {		
		Event e = new Event();
		e.setEventTypeId(new ParamId(-1, "foo"));		
		e.setParamId(new ParamId("#42", true));
		Calendar cal = new GregorianCalendar();
		cal.set(2016, 04, 12, 12, 42, 23);
		e.setTimestamp(cal.getTime());
		e.setSeverity(5);
		e.setMessage("bar");
		
		Map<String, Object> account = new LinkedHashMap<>();
		
		account.put("@id", "account");
		account.put("user_id", 42);
		account.put("first_name", "Firstname");
		account.put("last_name", "Lastname");
		
		Map<String, Object> paramMap = new LinkedHashMap<>();
		
		paramMap.put("accountParam", account);		
		paramMap.put("foo", "bar");
		paramMap.put("#23", (short)42);
		paramMap.put("pi", Math.PI);
		
		e.setUntypedParamMap(paramMap);
		
		e.getMessageHeader().setMessageLength(e.getByteCount());
		ByteBuffer bb = ByteBuffer.allocate(e.getByteCount());
		byte[] data = e.serialize(bb).array();
		
		System.out.println(bytesToHex(data));		
	}
	
	public static String bytesToHex(byte[] bytes) {
		if (bytes == null)
			return null;

		char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };
		char[] resChars = new char[bytes.length * 2];

		for (int iByte = 0; iByte < bytes.length; iByte++) {
			byte b = bytes[iByte];
			int b0 = (b & 0xf0) >> 4;
			int b1 = b & 0x0f;

			resChars[2 * iByte] = hexChars[b0];
			resChars[2 * iByte + 1] = hexChars[b1];
		}
		return new String(resChars);
	}
}
