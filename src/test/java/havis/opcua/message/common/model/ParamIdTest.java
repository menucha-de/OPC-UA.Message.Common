package havis.opcua.message.common.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;

import org.junit.Test;

public class ParamIdTest {

	@Test
	public void testParse() {
		// legacy format
		ParamId pId = new ParamId("test", true);
		assertEquals(pId.getNamespaceIndex(), -1);
		assertEquals(pId.getValue(), "test");
		assertTrue(pId.isAlphanumeric());
		assertFalse(pId.isNumeric());

		pId = new ParamId("#1234", true);
		assertEquals(pId.getNamespaceIndex(), -1);
		assertEquals(pId.getValue(), 1234);
		assertTrue(pId.isNumeric());
		assertFalse(pId.isAlphanumeric());

		// current format
		pId = new ParamId("i=12345", true);
		assertEquals(pId.getNamespaceIndex(), 0);
		assertEquals(pId.getValue(), 12345);
		assertTrue(pId.isNumeric());
		assertFalse(pId.isAlphanumeric());

		pId = new ParamId("s=test", true);
		assertEquals(pId.getNamespaceIndex(), 0);
		assertEquals(pId.getValue(), "test");
		assertTrue(pId.isAlphanumeric());
		assertFalse(pId.isNumeric());

		pId = new ParamId("ns=2;i=12345", true);
		assertEquals(pId.getNamespaceIndex(), 2);
		assertEquals(pId.getValue(), 12345);
		assertTrue(pId.isNumeric());
		assertFalse(pId.isAlphanumeric());

		pId = new ParamId("ns=1;s=test", true);
		assertEquals(pId.getNamespaceIndex(), 1);
		assertEquals(pId.getValue(), "test");
		assertTrue(pId.isAlphanumeric());
		assertFalse(pId.isNumeric());
	}

	@Test
	public void testSerializeParamId() {
		ParamId pId = new ParamId(2, "\u0080\u0081\u0082");
		ByteBuffer bb = ByteBuffer.allocate(pId.getByteCount());
		pId.serialize(bb);
		byte[] bytes = bb.array();
		assertArrayEquals(bytes, new byte[] { 0x00, 0x02, 0x01, 0x00, 0x03, (byte)0x80, (byte)0x81, (byte)0x82 });

		ParamId pId2 = new ParamId(0, 0xffffffff);
		ByteBuffer bb2 = ByteBuffer.allocate(pId2.getByteCount());
		pId2.serialize(bb2);
		byte[] bytes2 = bb2.array();
		assertArrayEquals(bytes2, new byte[] { 0x00, 0x00, 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff });
	}

	@Test
	public void testDeserializeParamId() {
		byte[] data = 
			new byte[] {
				0x00, 0x02, /* namespace index */
				0x01, /* param id type: alphanum */  
				0x00, 0x03, /* param id len: 3 */  
				(byte)0x80, (byte)0x81, (byte)0x82, /* param id str: "\u0080..\0082" */
		};
		
		ParamId pId1 = new ParamId(ByteBuffer.wrap(data));
		assertEquals(pId1.getNamespaceIndex(), 2);
		assertEquals(pId1.isAlphanumeric(), true);
		assert(pId1.getValue() instanceof String);
		assertEquals((String)pId1.getValue(), "\u0080\u0081\u0082");
		
		data = 
			new byte[] {
				0x00, 0x00, /* namespace index */
				0x00, /* param id type: num */  
				(byte)0xaa, (byte)0xbb, (byte)0xcc, (byte)0xdd /* param id val: 0xaabbccdd */  				
			};
		ParamId pId2 = new ParamId(ByteBuffer.wrap(data));
		assertEquals(pId2.getNamespaceIndex(), 0);
		assertEquals(pId2.isNumeric(), true);
		assert(pId2.getValue() instanceof Integer);
		assertEquals((int)pId2.getValue(), (int)0xaabbccdd);
	}
	
	@Test
	public void testEquals() {
		ParamId pId1 = new ParamId(-1, "foo");
		ParamId pId2 = new ParamId(-1, "foo");
		assert( pId1.equals(pId2) );
		
		pId1 = new ParamId(-1, "foo");
		pId2 = new ParamId(-1, "foo".hashCode());
		assert( ! pId1.equals(pId2) );

		pId1 = new ParamId(-1, 42);
		pId2 = new ParamId(-1, 42);
		assert(pId1.equals(pId2));
		
		pId1 = new ParamId(2, 42);
		pId2 = new ParamId(2, 42);
		assert(pId1.equals(pId2));
		
		pId1 = new ParamId(1, 42);
		pId2 = new ParamId(0, 42);
		assert( ! pId1.equals(pId2));
		
		pId1 = new ParamId(-1, "foo");
		assert(pId1.equals("foo"));
		
		pId2 = new ParamId(-1, 42);
		assert(pId2.equals(42));
	}
	
	
	public static String bytesToHex(byte[] bytes) {
		if (bytes == null)
			return null;

		char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
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
