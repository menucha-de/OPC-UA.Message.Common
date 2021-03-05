package havis.opcua.message.common.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public class ParamValueTest {

	@Test
	public void testScalarSerialize() {

		ParamValue pv;
		ByteBuffer bb;
		byte[] bytes;

		pv = new ParamValue(false);
		bb = ByteBuffer.allocate(pv.getByteCount());
		bytes = pv.serialize(bb).array();
		assertArrayEquals(new byte[] { 0x00, 0x00, 0x00 }, bytes);

		pv = new ParamValue(true);
		bb = ByteBuffer.allocate(pv.getByteCount());
		bytes = pv.serialize(bb).array();
		assertArrayEquals(new byte[] { 0x00, 0x00, 0x01 }, bytes);

		pv = new ParamValue((byte) 0xaf);
		bb = ByteBuffer.allocate(pv.getByteCount());
		bytes = pv.serialize(bb).array();
		assertArrayEquals(new byte[] { 0x00, 0x02, (byte) 0xaf }, bytes);

		pv = new ParamValue((short) 0xffff);
		bb = ByteBuffer.allocate(pv.getByteCount());
		bytes = pv.serialize(bb).array();
		assertArrayEquals(new byte[] { 0x00, 0x03, (byte) 0xff, (byte) 0xff }, bytes);

		pv = new ParamValue((int) 0xffffffff);
		bb = ByteBuffer.allocate(pv.getByteCount());
		bytes = pv.serialize(bb).array();
		assertArrayEquals(new byte[] { 0x00, 0x04, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff }, bytes);

		pv = new ParamValue((long) 0xffffffffffffffffl);
		bb = ByteBuffer.allocate(pv.getByteCount());
		bytes = pv.serialize(bb).array();
		assertArrayEquals(new byte[] { 0x00, 0x05, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff },
				bytes);

		pv = new ParamValue(Float.MIN_VALUE);
		bb = ByteBuffer.allocate(pv.getByteCount());
		bytes = pv.serialize(bb).array();
		assertArrayEquals(new byte[] { 0x00, 0x06, 0x00, 0x00, 0x00, 0x01 }, bytes);

		pv = new ParamValue(Float.MAX_VALUE);
		bb = ByteBuffer.allocate(pv.getByteCount());
		bytes = pv.serialize(bb).array();
		assertArrayEquals(new byte[] { 0x00, 0x06, (byte) 0x7f, (byte) 0x7f, (byte) 0xff, (byte) 0xff }, bytes);

		pv = new ParamValue(Double.MIN_VALUE);
		bb = ByteBuffer.allocate(pv.getByteCount());
		bytes = pv.serialize(bb).array();
		assertArrayEquals(new byte[] { 0x00, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01 }, bytes);

		pv = new ParamValue(Double.MAX_VALUE);
		bb = ByteBuffer.allocate(pv.getByteCount());
		bytes = pv.serialize(bb).array();
		assertArrayEquals(new byte[] { 0x00, 0x07, 0x7F, (byte) 0xEF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }, bytes);
	}

	@Test
	public void testArraySerialize() {
		ParamValue pv;
		ByteBuffer bb;
		byte[] bytes;

		pv = new ParamValue(new Boolean[] { true, false });
		bb = ByteBuffer.allocate(pv.getByteCount());
		bytes = pv.serialize(bb).array();
		assertArrayEquals(new byte[] {
		/* param type: array */0x00, 0x08,
		/* array type: bool */0x00, 0x00,
		/* num of elems: 2 */0x00, 0x02,
		/* data: */
		0x01, 0x00 }, bytes);

		pv = new ParamValue(new Byte[] { (byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe });
		bb = ByteBuffer.allocate(pv.getByteCount());
		bytes = pv.serialize(bb).array();
		assertArrayEquals(new byte[] {
		/* param type: array */0x00, 0x08,
		/* array type: byte */0x00, 0x02,
		/* num of elems: 4 */0x00, 0x04,
		/* data: */
		(byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe }, bytes);

		pv = new ParamValue(new Short[] { (short) 0xcafe, (short) 0xbabe });
		bb = ByteBuffer.allocate(pv.getByteCount());
		bytes = pv.serialize(bb).array();
		assertArrayEquals(new byte[] {
		/* param type: array */0x00, 0x08,
		/* array type: short */0x00, 0x03,
		/* num of elems: 2 */0x00, 0x02,
		/* data: */
		(byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe }, bytes);

		pv = new ParamValue(new Integer[] { 0xcafebabe, 0xdeadbeef });
		bb = ByteBuffer.allocate(pv.getByteCount());
		bytes = pv.serialize(bb).array();
		assertArrayEquals(new byte[] {
		/* param type: array */0x00, 0x08,
		/* array type: int */0x00, 0x04,
		/* num of elems: 2 */0x00, 0x02,
		/* data: */
		(byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe, (byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef }, bytes);

		pv = new ParamValue(new Long[] { 0xcafebabedeadbeefL, 0xaaaabbbbccccddddL });
		bb = ByteBuffer.allocate(pv.getByteCount());
		bytes = pv.serialize(bb).array();
		assertArrayEquals(new byte[] {
		/* param type: array */0x00, 0x08,
		/* array type: long */0x00, 0x05,
		/* num of elems: 2 */0x00, 0x02,
		/* data: */
		(byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe, (byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef, (byte) 0xaa, (byte) 0xaa, (byte) 0xbb,
				(byte) 0xbb, (byte) 0xcc, (byte) 0xcc, (byte) 0xdd, (byte) 0xdd }, bytes);

		pv = new ParamValue(new Float[] { Float.MIN_VALUE, Float.MAX_VALUE });
		bb = ByteBuffer.allocate(pv.getByteCount());
		bytes = pv.serialize(bb).array();
		assertArrayEquals(new byte[] {
		/* param type: array */0x00, 0x08,
		/* array type: float */0x00, 0x06,
		/* num of elems: 2 */0x00, 0x02,
		/* data: */
		0x00, 0x00, 0x00, 0x01, (byte) 0x7f, (byte) 0x7f, (byte) 0xff, (byte) 0xff }, bytes);

		pv = new ParamValue(new Double[] { Double.MIN_VALUE, Double.MAX_VALUE });
		bb = ByteBuffer.allocate(pv.getByteCount());
		bytes = pv.serialize(bb).array();
		assertArrayEquals(new byte[] {
		/* param type: array */0x00, 0x08,
		/* array type: double */0x00, 0x07,
		/* num of elems: 2 */0x00, 0x02,
		/* data: */
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x7F, (byte) 0xEF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF },
				bytes);

		// String[]
		pv = new ParamValue(new String[0]);
		bb = ByteBuffer.allocate(pv.getByteCount());
		bytes = pv.serialize(bb).array();
		assertArrayEquals(new byte[] {
		/* param type: array */0x00, 0x08,
		/* array type: array */0x00, 0x08,
		/* array length: */0x00, 0x00
		/* array values: */
		}, bytes);

		pv = new ParamValue(new String[] { "a", "bcd", "" });
		bb = ByteBuffer.allocate(pv.getByteCount());
		bytes = pv.serialize(bb).array();
		assertArrayEquals(new byte[] {
		/* param type: array */0x00, 0x08,
		/* array type: array */0x00, 0x08,
		/* array length: */0x00, 0x03,
		/* array type: char */0x00, 0x01,
		/* array length: */0x00, 0x01,
		/* array values: */0x61,
		/* array type: char */0x00, 0x01,
		/* array length: */0x00, 0x03,
		/* array values: */0x62, 0x63, 0x64,
		/* array type: char */0x00, 0x01,
		/* array length: */0x00, 0x00
		/* array values: */
		}, bytes);

		pv = new ParamValue("\u0080\u0081\u0082\u0083\u0084\u0085");
		bb = ByteBuffer.allocate(pv.getByteCount());
		bytes = pv.serialize(bb).array();
		assertArrayEquals(new byte[] {
		/* param type: array */0x00, 0x08,
		/* array type: char */0x00, 0x01,
		/* num of elems: 2 */0x00, 0x06,
		/* data: */
		(byte) 0x80, (byte) 0x81, (byte) 0x82, (byte) 0x83, (byte) 0x84, (byte) 0x85, }, bytes);

		pv = new ParamValue("\u0080\u0081\u0082\u0083\u0084\u0085", true);
		bb = ByteBuffer.allocate(pv.getByteCount());
		bytes = pv.serialize(bb).array();
		assertArrayEquals(new byte[] {
		/* num of elems: 2 */0x00, 0x06,
		/* data: */
		(byte) 0x80, (byte) 0x81, (byte) 0x82, (byte) 0x83, (byte) 0x84, (byte) 0x85, }, bytes);
	}

	@Test
	public void testScalarDeserialize() {
		byte[] data;
		ParamValue pv;

		/* bool: true */
		data = new byte[] { 0x00, 0x00, 0x01 };
		pv = new ParamValue(ByteBuffer.wrap(data));
		assertEquals((boolean) pv.getValue(), true);

		/* bool: false */
		data = new byte[] { 0x00, 0x00, 0x00 };
		pv = new ParamValue(ByteBuffer.wrap(data));
		assertEquals((boolean) pv.getValue(), false);

		/* char: exception */
		data = new byte[] { 0x00, 0x01, 0x42 };
		try {
			pv = new ParamValue(ByteBuffer.wrap(data));
			fail("Expected exception but none thrown.");
		} catch (IllegalArgumentException ex) {
		}

		/* byte */
		data = new byte[] { 0x00, 0x02, (byte) 0xaa };
		pv = new ParamValue(ByteBuffer.wrap(data));
		assertEquals((byte) pv.getValue(), (byte) 0xaa);

		/* short: min */
		data = new byte[] { 0x00, 0x03, (byte) 0x80, 0x00 };
		pv = new ParamValue(ByteBuffer.wrap(data));
		assertEquals((short) pv.getValue(), Short.MIN_VALUE);

		/* short: max */
		data = new byte[] { 0x00, 0x03, (byte) 0x7f, (byte) 0xff };
		pv = new ParamValue(ByteBuffer.wrap(data));
		assertEquals((short) pv.getValue(), Short.MAX_VALUE);

		/* int: min */
		data = new byte[] { 0x00, 0x04, (byte) 0x80, 0x00, 0x00, 0x00 };
		pv = new ParamValue(ByteBuffer.wrap(data));
		assertEquals((int) pv.getValue(), Integer.MIN_VALUE);

		/* int: max */
		data = new byte[] { 0x00, 0x04, (byte) 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff };
		pv = new ParamValue(ByteBuffer.wrap(data));
		assertEquals((int) pv.getValue(), Integer.MAX_VALUE);

		/* long: min */
		data = new byte[] { 0x00, 0x05, (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		pv = new ParamValue(ByteBuffer.wrap(data));
		assertEquals((long) pv.getValue(), Long.MIN_VALUE);

		/* long: max */
		data = new byte[] { 0x00, 0x05, (byte) 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
		pv = new ParamValue(ByteBuffer.wrap(data));
		assertEquals((long) pv.getValue(), Long.MAX_VALUE);

		/* float: min */
		data = new byte[] { 0x00, 0x06, 0x00, 0x00, 0x00, 0x01 };
		pv = new ParamValue(ByteBuffer.wrap(data));
		assert ((float) pv.getValue() == Float.MIN_VALUE);

		/* float: max */
		data = new byte[] { 0x00, 0x06, (byte) 0x7f, (byte) 0x7f, (byte) 0xff, (byte) 0xff };
		pv = new ParamValue(ByteBuffer.wrap(data));
		assert ((float) pv.getValue() == Float.MAX_VALUE);

		/* double: min */
		data = new byte[] { 0x00, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01 };
		pv = new ParamValue(ByteBuffer.wrap(data));
		assert ((double) pv.getValue() == Double.MIN_VALUE);

		/* double: max */
		data = new byte[] { 0x00, 0x07, 0x7F, (byte) 0xEF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
		pv = new ParamValue(ByteBuffer.wrap(data));
		assert ((double) pv.getValue() == Double.MAX_VALUE);
	}

	@Test
	public void testArrayDeserialize() {
		byte[] data;
		ParamValue pv;

		data = new byte[] {
		/* param type: array */0x00, 0x08,
		/* array type: bool */0x00, 0x00,
		/* num of elems: 2 */0x00, 0x02,
		/* data: */
		0x01, 0x00 };
		pv = new ParamValue(ByteBuffer.wrap(data));
		Boolean[] bools = pv.getValue();
		assertArrayEquals(bools, new Boolean[] { true, false });

		data = new byte[] {
		/* param type: array */0x00, 0x08,
		/* array type: byte */0x00, 0x02,
		/* num of elems: 4 */0x00, 0x04,
		/* data: */
		(byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe };
		pv = new ParamValue(ByteBuffer.wrap(data));
		Byte[] bytes = pv.getValue();
		assertArrayEquals(bytes, new Byte[] { (byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe });

		data = new byte[] {
		/* param type: array */0x00, 0x08,
		/* array type: short */0x00, 0x03,
		/* num of elems: 2 */0x00, 0x02,
		/* data: */
		(byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe };
		pv = new ParamValue(ByteBuffer.wrap(data));
		Short[] shorts = pv.getValue();
		assertArrayEquals(shorts, new Short[] { (short) 0xcafe, (short) 0xbabe });

		data = new byte[] {
		/* param type: array */0x00, 0x08,
		/* array type: int */0x00, 0x04,
		/* num of elems: 2 */0x00, 0x02,
		/* data: */
		(byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe, (byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef };
		pv = new ParamValue(ByteBuffer.wrap(data));
		Integer[] ints = pv.getValue();
		assertArrayEquals(ints, new Integer[] { (int) 0xcafebabe, (int) 0xdeadbeef });

		data = new byte[] {
		/* param type: array */0x00, 0x08,
		/* array type: long */0x00, 0x05,
		/* num of elems: 2 */0x00, 0x02,
		/* data: */
		(byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe, (byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef, (byte) 0xaa, (byte) 0xaa, (byte) 0xbb,
				(byte) 0xbb, (byte) 0xcc, (byte) 0xcc, (byte) 0xdd, (byte) 0xdd };
		pv = new ParamValue(ByteBuffer.wrap(data));
		Long[] longs = pv.getValue();
		assertArrayEquals(longs, new Long[] { 0xcafebabedeadbeefL, 0xaaaabbbbccccddddL });

		data = new byte[] {
		/* param type: array */0x00, 0x08,
		/* array type: float */0x00, 0x06,
		/* num of elems: 2 */0x00, 0x02,
		/* data: */
		0x00, 0x00, 0x00, 0x01, (byte) 0x7f, (byte) 0x7f, (byte) 0xff, (byte) 0xff };

		pv = new ParamValue(ByteBuffer.wrap(data));
		Float[] floats = pv.getValue();
		assertArrayEquals(floats, new Float[] { Float.MIN_VALUE, Float.MAX_VALUE });

		data = new byte[] {
		/* param type: array */0x00, 0x08,
		/* array type: double */0x00, 0x07,
		/* num of elems: 2 */0x00, 0x02,
		/* data: */
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x7F, (byte) 0xEF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };

		pv = new ParamValue(ByteBuffer.wrap(data));
		Double[] doubles = pv.getValue();
		assertArrayEquals(doubles, new Double[] { Double.MIN_VALUE, Double.MAX_VALUE });

		data = new byte[] {
		/* param type: array */0x00, 0x08,
		/* array type: char */0x00, 0x01,
		/* num of elems: 2 */0x00, 0x06,
		/* data: */
		(byte) 0x80, (byte) 0x81, (byte) 0x82, (byte) 0x83, (byte) 0x84, (byte) 0x85 };
		pv = new ParamValue(ByteBuffer.wrap(data));
		String str = pv.getValue();
		assertEquals(str, "\u0080\u0081\u0082\u0083\u0084\u0085");

		data = new byte[] {
		/* num of elems: 2 */0x00, 0x06,
		/* data: */
		(byte) 0x80, (byte) 0x81, (byte) 0x82, (byte) 0x83, (byte) 0x84, (byte) 0x85 };

		pv = new ParamValue(ByteBuffer.wrap(data), true);
		str = pv.getValue();
		assertEquals(str, "\u0080\u0081\u0082\u0083\u0084\u0085");

		// String[]
		data = new byte[] {
		/* param type: array */0x00, 0x08,
		/* array type: array */0x00, 0x08,
		/* array length: */0x00, 0x00
		/* array values: */
		};
		pv = new ParamValue(ByteBuffer.wrap(data));
		String[] strings = pv.getValue();
		assertArrayEquals(strings, new String[0]);

		data = new byte[] {
		/* param type: array */0x00, 0x08,
		/* array type: array */0x00, 0x08,
		/* array length: */0x00, 0x03,
		/* array type: char */0x00, 0x01,
		/* array length: */0x00, 0x01,
		/* array values: */0x61,
		/* array type: char */0x00, 0x01,
		/* array length: */0x00, 0x03,
		/* array values: */0x62, 0x63, 0x64,
		/* array type: char */0x00, 0x01,
		/* array length: */0x00, 0x00,
		/* array values: */
		};
		pv = new ParamValue(ByteBuffer.wrap(data));
		strings = pv.getValue();
		assertArrayEquals(strings, new String[] { "a", "bcd", "" });
	}

	private byte[] structTestData = { 0x00, 0x09, // type: struct

			// struct id:
			0x00, 0x00,
			0x01, // id type: str
			0x00, 0x07, // str len: 7 chars
			0x61, 0x63, 0x63, 0x6F, 0x75, 0x6E, 0x74, // "account"

			0x00, 0x04, // field count: 4

			// field 0
			0x00, 0x07, // char count: 7
			0x75, 0x73, 0x65, 0x72, 0x5F, 0x69, 0x64, // "user_id"

			0x00, 0x04, // field value type: int
			0x00, 0x00, 0x00, 0x2A, // field value: 42

			// field 1
			0x00, 0x0A, // char count: 10
			0x66, 0x69, 0x72, 0x73, 0x74, 0x5F, 0x6E, 0x61, 0x6D, 0x65, // "first_name"

			0x00, 0x08, // field value type: array
			0x00, 0x01, // field value array type: char
			0x00, 0x09, // char count: 9
			0x46, 0x69, 0x72, 0x73, 0x74, 0x6e, 0x61, 0x6d, 0x65, // "Firstname"

			// field 2
			0x00, 0x09, // char count: 9
			0x6C, 0x61, 0x73, 0x74, 0x5F, 0x6E, 0x61, 0x6D, 0x65, // "last_name"

			0x00, 0x08, // field value type: array
			0x00, 0x01, // field value array type: char
			0x00, 0x08, // char count: 5
			0x4c, 0x61, 0x73, 0x74, 0x6e, 0x61, 0x6d, 0x65, // "Lastname"

			// field 3
			0x00, 0x06, // char count: 6
			0x67, 0x72, 0x6F, 0x75, 0x70, 0x73, // "groups"

			0x00, 0x08, // field value type: array
			0x00, 0x09, // field value array type: struct
			0x00, 0x03, // item count: 3

			// item 0
			0x00, 0x00,
			0x01, // id type: str
			0x00, 0x0B, // str len: 11,
			0x72, 0x65, 0x61, 0x64, 0x65, 0x72, 0x47, 0x72, 0x6F, 0x75, 0x70, // "readerGroup"

			0x00, 0x02, // field count: 2

			// field 0:
			0x00, 0x04, // char count: 4
			0x6E, 0x61, 0x6D, 0x65, // "name"

			0x00, 0x08, // field value type: array
			0x00, 0x01, // field value type: char
			0x00, 0x06, // char count
			0x72, 0x65, 0x61, 0x64, 0x65, 0x72, // "reader"

			// field 1:
			0x00, 0x0B, // char count: 0x11,
			0x70, 0x65, 0x72, 0x6D, 0x69, 0x73, 0x73, 0x69, 0x6F, 0x6E, 0x73, // "permissions"

			0x00, 0x02, // field value type: byte
			0x04, // field value 0b100

			// item 1:
			0x00, 0x00,
			0x01, // id type: str
			0x00, 0x0B, // char count: 0x11,
			0x77, 0x72, 0x69, 0x74, 0x65, 0x72, 0x47, 0x72, 0x6F, 0x75, 0x70, // "writerGroup"

			0x00, 0x02, // field count: 2

			// field 0:
			0x00, 0x04, // char count: 4
			0x6E, 0x61, 0x6D, 0x65, // "name"

			0x00, 0x08, // field value type: array
			0x00, 0x01, // field value array type: char
			0x00, 0x06, // char count: 6
			0x77, 0x72, 0x69, 0x74, 0x65, 0x72, // "writer"

			// field 1:
			0x00, 0x0B, // char count: 0x11,
			0x70, 0x65, 0x72, 0x6D, 0x69, 0x73, 0x73, 0x69, 0x6F, 0x6E, 0x73, // "permissions"

			0x00, 0x02, // field value type: byte
			0x01, // field value: 0b0x00,1

			// item 2:
			0x00, 0x00,
			0x01, // id type: str
			0x00, 0x09, // str len: 9
			0x65, 0x78, 0x65, 0x63, 0x47, 0x72, 0x6F, 0x75, 0x70, // "execGroup"

			0x00, 0x02, // field count: 2

			// field 0:
			0x00, 0x04, // char count
			0x6E, 0x61, 0x6D, 0x65, // "name"

			0x00, 0x08, // field value type: array
			0x00, 0x01, // field value array type: char
			0x00, 0x08, // char count
			0x65, 0x78, 0x65, 0x63, 0x75, 0x74, 0x6F, 0x72, // "executor"

			// field 1:
			0x00, 0x0B, // char count: 0x11,
			0x70, 0x65, 0x72, 0x6D, 0x69, 0x73, 0x73, 0x69, 0x6F, 0x6E, 0x73, // "persmissions"

			0x00, 0x02, // field value type: byte
			0x02 // field value: 0b010
	};

	@Test
	public void testStructSerialize() {

		int byteCount = 0;

		/* 3 bytes + 9 bytes (param id) + 2 bytes (field cnt) = 14 byte */
		Struct account = new Struct(new ParamId(0, "account"));
		byteCount += 14;

		/*
		 * 2+7 byte (name) + 2 byte (param type) + 4 byte (param value) = 15
		 * byte
		 */
		account.getFields().add(new StructField("user_id", new ParamValue(42)));
		byteCount += 15;

		/* 2+10 byte (name) + 2+2+2+9 byte (param value) = 27 byte */
		account.getFields().add(new StructField("first_name", new ParamValue("Firstname")));
		byteCount += 27;

		/* 2+9 byte (name) + 2+2+2+8 byte (param value) = 25 byte */
		account.getFields().add(new StructField("last_name", new ParamValue("Lastname")));
		byteCount += 25;

		/* 3 bytes + 13 bytes (param id) + 2 bytes (field cnt) = 16 byte */
		Struct readerGroup = new Struct(new ParamId(0, "readerGroup"));
		byteCount += 18;

		/* 2+4 byte (name) + 2+2+2+6 byte (param value) = 18 byte */
		readerGroup.getFields().add(new StructField("name", new ParamValue("reader")));
		byteCount += 18;

		/* 2+11 byte (name) + 2+1 byte (param value) = 16 byte */
		readerGroup.getFields().add(new StructField("permissions", new ParamValue((byte) 4)));
		byteCount += 16;

		/* 3 bytes + 12 bytes (param id) + 2 byte (field cnt) = 16 bytes */
		Struct writerGroup = new Struct(new ParamId(0, "writerGroup"));
		byteCount += 18;

		/* 2+4 byte (name) + 2+2+2+6 byte (param value) = 18 byte */
		writerGroup.getFields().add(new StructField("name", new ParamValue("writer")));
		byteCount += 18;

		/* 2+11 byte (name) + 2+1 byte (param value) = 16 byte */
		writerGroup.getFields().add(new StructField("permissions", new ParamValue((byte) 1)));
		byteCount += 16;

		/* 3 bytes + 11 bytes (param id) + 2 byte (field cnt) = 14 bytes */
		Struct execGroup = new Struct(new ParamId(0, "execGroup"));
		byteCount += 16;

		/* 2+4 byte (name) + 2+2+2+8 byte (param value) = 20 byte */
		execGroup.getFields().add(new StructField("name", new ParamValue("executor")));
		byteCount += 20;

		/* 2+11 byte (name) + 2+1 byte (param value) = 16 byte */
		execGroup.getFields().add(new StructField("permissions", new ParamValue((byte) 2)));
		byteCount += 16;

		/* 2+6 byte (name) + 2+2+2 byte (array) = 14 byte */
		account.getFields().add(new StructField("groups", new ParamValue(new Struct[] { readerGroup, writerGroup, execGroup })));
		byteCount += 14;

		/* 2 byte (param type) */
		ParamValue pv = new ParamValue(account);
		byteCount += 2;

		assertEquals(byteCount, pv.getByteCount());

		ByteBuffer bb = ByteBuffer.allocate(pv.getByteCount());
		byte[] data = pv.serialize(bb).array();

		assertArrayEquals(structTestData, data);
	}

	@Test
	public void testStructDeserialize() {

		ByteBuffer bb = ByteBuffer.wrap(structTestData);
		ParamValue pv = new ParamValue(bb);
		// System.out.println(pv);

		assertEquals(pv.getByteCount(), structTestData.length);

		Struct account = pv.getValue();

		assertEquals(4, account.getFieldCount());
		StructField sf0 = account.getFields().get(0);
		assertEquals("user_id", sf0.getName());
		assertEquals(42, sf0.getValue().<Integer> getValue().intValue());

		StructField sf1 = account.getFields().get(1);
		assertEquals("first_name", sf1.getName());
		assertEquals("Firstname", sf1.getValue().getValue());

		StructField sf2 = account.getFields().get(2);
		assertEquals("last_name", sf2.getName());
		assertEquals("Lastname", sf2.getValue().getValue());

		StructField sf3 = account.getFields().get(3);
		assertEquals("groups", sf3.getName());
		assertEquals(Struct[].class, sf3.getValue().getValue().getClass());

		Struct[] groups = sf3.getValue().getValue();

		assertEquals(3, groups.length);

		Struct group0 = groups[0];
		assertEquals("name", group0.getFields().get(0).getName());
		assertEquals("reader", group0.getFields().get(0).getValue().getValue());
		assertEquals("permissions", group0.getFields().get(1).getName());
		assertEquals((byte) 0x04, group0.getFields().get(1).getValue().<Byte> getValue().byteValue());

		Struct group1 = groups[1];
		assertEquals("name", group1.getFields().get(0).getName());
		assertEquals("writer", group1.getFields().get(0).getValue().getValue());
		assertEquals("permissions", group1.getFields().get(1).getName());
		assertEquals((byte) 0x01, group1.getFields().get(1).getValue().<Byte> getValue().byteValue());

		Struct group2 = groups[2];
		assertEquals("name", group2.getFields().get(0).getName());
		assertEquals("executor", group2.getFields().get(0).getValue().getValue());
		assertEquals("permissions", group2.getFields().get(1).getName());
		assertEquals((byte) 0x02, group2.getFields().get(1).getValue().<Byte> getValue().byteValue());

		ByteBuffer bb2 = ByteBuffer.allocate(pv.getByteCount());
		byte[] data = pv.serialize(bb2).array();
		assertArrayEquals(structTestData, data);
	}

	@Test
	public void testStructFromHashMap() {

		Map<String, Object> account = new LinkedHashMap<>();

		account.put("@id", "ns=0;s=account");
		account.put("user_id", 42);
		account.put("first_name", "Firstname");
		account.put("last_name", "Lastname");

		Map<String, Object> readerGroup = new LinkedHashMap<>();
		readerGroup.put("@id", "ns=0;s=readerGroup");
		readerGroup.put("name", "reader");
		readerGroup.put("permissions", (byte) 0x04);

		Map<String, Object> writerGroup = new LinkedHashMap<>();
		writerGroup.put("@id", "ns=0;s=writerGroup");
		writerGroup.put("name", "writer");
		writerGroup.put("permissions", (byte) 0x01);

		Map<String, Object> execGroup = new LinkedHashMap<>();
		execGroup.put("@id", "ns=0;s=execGroup");
		execGroup.put("name", "executor");
		execGroup.put("permissions", (byte) 0x02);

		account.put("groups", new Map[] { readerGroup, writerGroup, execGroup });

		ParamValue pv = new ParamValue(account);

		ByteBuffer bb = ByteBuffer.allocate(pv.getByteCount());
		byte[] data = pv.serialize(bb).array();
		assertArrayEquals(structTestData, data);

	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testStructToHashMap() {
		ByteBuffer bb = ByteBuffer.wrap(structTestData);
		ParamValue pv = new ParamValue(bb);

		Map m = ((Struct) pv.getValue()).asMap();
		ParamValue pv2 = new ParamValue(new Struct(m));

		ByteBuffer bb2 = ByteBuffer.allocate(pv.getByteCount());
		byte[] data = pv2.serialize(bb2).array();

		assertArrayEquals(structTestData, data);

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
