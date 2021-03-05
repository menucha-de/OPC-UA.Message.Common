package havis.opcua.message.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import havis.opcua.message.common.serialize.MessageDeserializer;

import java.nio.ByteBuffer;

import org.junit.Test;

public class MessageDeserializeTest {

	@Test
	public void testDeserializeMessageType() {

		assertEquals(MessageType.deserialize(new byte[] { 0x00, 0x00 }), MessageType.READ);
		assertEquals(MessageType.deserialize(new byte[] { 0x00, 0x01 }), MessageType.READ_RESPONSE);
		assertEquals(MessageType.deserialize(new byte[] { 0x00, 0x02 }), MessageType.WRITE);
		assertEquals(MessageType.deserialize(new byte[] { 0x00, 0x03 }), MessageType.WRITE_RESPONSE);
		assertEquals(MessageType.deserialize(new byte[] { 0x00, 0x04 }), MessageType.SUBSCRIBE);
		assertEquals(MessageType.deserialize(new byte[] { 0x00, 0x05 }), MessageType.SUBSCRIBE_RESPONSE);
		assertEquals(MessageType.deserialize(new byte[] { 0x00, 0x06 }), MessageType.UNSUBSCRIBE);
		assertEquals(MessageType.deserialize(new byte[] { 0x00, 0x07 }), MessageType.UNSUBSCRIBE_RESPONSE);
		assertEquals(MessageType.deserialize(new byte[] { 0x00, 0x08 }), MessageType.NOTIFICATION);
		assertNull(MessageType.deserialize(new byte[] { (byte) 0xff, (byte) 0xff }));
	}

	@Test
	public void testDeserializeMessageHeader() {
		byte[] data = new byte[] { 0x00, 0x01, /* message type: read_resp */
		0x00, 0x00, 0x00, 0x1B, /* message len: 27 bytes */
		(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, /*
															 * message id:
															 * 0xffffffff
															 */
		};

		MessageHeader mh = new MessageHeader(ByteBuffer.wrap(data));

		assertEquals(mh.getMessageId(), 0xffffffff);
		assertEquals(mh.getMessageLength(), 27);
		assertEquals(mh.getMessageType(), MessageType.READ_RESPONSE);
	}

	@Test
	public void testDeserializeRead() {
		byte[] data = new byte[] { 0x00, 0x00, /* message type: read */
		0x00, 0x00, 0x00, 0x0F, /* message len: 15 bytes */
		(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, /*
															 * message id:
															 * 0xffffffff
															 */
		0x00, 0x00,
		0x00, /* param id type: num */
		(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF /*
														    * param id val:
														    * 0xffffffff
														    */
		};

		Read r = MessageDeserializer.deserialize(data);
		assertEquals(r.getMessageHeader().getMessageType(), MessageType.READ);
		assertEquals(r.getMessageHeader().getMessageLength(), 15);
		assertEquals(r.getMessageHeader().getMessageId(), 0xffffffff);
		assertEquals(r.getParamId().<Integer> getValue().intValue(), 0xffffffff);

		data = new byte[] { 0x00, 0x00, /* message type: read */
		0x00, 0x00, 0x00, 0x10, /* message len: 16 bytes */
		(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, /*
															 * message id:
															 * 0xffffffff
															 */
		0x00, 0x00,
		0x01, /* param id type: alphanum */
		0x00, 0x03, /* param id len: 3 */
		0x66, 0x6F, 0x6F, /* param id str: "foo" */
		};

		r = MessageDeserializer.deserialize(data);
		assertEquals(r.getMessageHeader().getMessageType(), MessageType.READ);
		assertEquals(r.getMessageHeader().getMessageLength(), 16);
		assertEquals(r.getMessageHeader().getMessageId(), 0xffffffff);
		assertEquals(r.getParamId().getValue(), "foo");
	}

	@Test
	public void testDeserializeReadResponse() {
		byte[] data = new byte[] { 0x00, 0x01, /* message type: read_resp */
		0x00, 0x00, 0x00, 0x1B, /* message len: 27 bytes */
		(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, /*
															 * message id:
															 * 0xffffffff
															 */
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

		ReadResponse rr = MessageDeserializer.deserialize(data);
		assertEquals(rr.getMessageHeader().getMessageType(), MessageType.READ_RESPONSE);
		assertEquals(rr.getMessageHeader().getMessageLength(), 27);
		assertEquals(rr.getMessageHeader().getMessageId(), 0xffffffff);
		assertEquals(rr.getStatus(), Status.SUCCESS);
		assertEquals(rr.getParamId().getValue(), "foo");
		assertEquals(rr.getResult().getValue(), "bar");
	}

	@Test
	public void testDeserializeWrite() {
		byte[] data = new byte[] { 0x00, 0x02, /* message type: write */
		0x00, 0x00, 0x00, 0x19, /* message len: 25 bytes */
		(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, /*
															 * message id:
															 * 0xffffffff
															 */
		0x00, 0x00,
		0x01, /* param id type: alphanum */
		0x00, 0x03, /* param id len: 3 */
		0x66, 0x6F, 0x6F, /* param id str: "foo" */
		0x00, 0x08, /* param type: array */
		0x00, 0x01, /* array type: char */
		0x00, 0x03, /* elem count: 3 */
		0x62, 0x61, 0x72 /* payload: "bar" */
		};

		Write w = MessageDeserializer.deserialize(data);

		assertEquals(w.getMessageHeader().getMessageType(), MessageType.WRITE);
		assertEquals(w.getMessageHeader().getMessageLength(), 25);
		assertEquals(w.getMessageHeader().getMessageId(), 0xffffffff);
		assertEquals(w.getParamId().getValue(), "foo");
		assertEquals(w.getParamValue().getValue(), "bar");
	}

	@Test
	public void testDeserializeWriteResponse() {
		byte[] data = new byte[] { 0x00, 0x03, /* message type: write_resp */
		0x00, 0x00, 0x00, 0x0c, /* message len: 12 bytes */
		(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, /*
															 * message id:
															 * 0xffffffff
															 */
		0x00, 0x00 /* status: SUCCESS */
		};

		WriteResponse wr = MessageDeserializer.deserialize(data);
		assertEquals(wr.getMessageHeader().getMessageType(), MessageType.WRITE_RESPONSE);
		assertEquals(wr.getMessageHeader().getMessageLength(), 12);
		assertEquals(wr.getMessageHeader().getMessageId(), 0xffffffff);
		assertEquals(wr.getStatus(), Status.SUCCESS);
	}

	@Test
	public void testDeserializeSubscribe() {
		byte[] data = new byte[] { 0x00, 0x04, /* message type: subscribe */
		0x00, 0x00, 0x00, 0x0F, /* message len: 15 bytes */
		(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, /*
															 * message id:
															 * 0xffffffff
															 */
		0x00, 0x00,
		0x00, /* param id type: num */
		(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF /*
														    * param id val:
														    * 0xffffffff
														    */
		};

		Subscribe s = MessageDeserializer.deserialize(data);
		assertEquals(s.getMessageHeader().getMessageType(), MessageType.SUBSCRIBE);
		assertEquals(s.getMessageHeader().getMessageLength(), 15);
		assertEquals(s.getMessageHeader().getMessageId(), 0xffffffff);
		assertEquals(s.getParamId().<Integer> getValue().intValue(), 0xffffffff);

		data = new byte[] { 0x00, 0x04, /* message type: subscribe */
		0x00, 0x00, 0x00, 0x10, /* message len: 16 bytes */
		(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, /*
															 * message id:
															 * 0xffffffff
															 */
		0x00, 0x00,
		0x01, /* param id type: alphanum */
		0x00, 0x03, /* param id len: 3 */
		0x66, 0x6F, 0x6F, /* param id str: "foo" */
		};

		s = MessageDeserializer.deserialize(data);
		assertEquals(s.getMessageHeader().getMessageType(), MessageType.SUBSCRIBE);
		assertEquals(s.getMessageHeader().getMessageLength(), 16);
		assertEquals(s.getMessageHeader().getMessageId(), 0xffffffff);
		assertEquals(s.getParamId().getValue(), "foo");
	}

	@Test
	public void testDeserializeSubscribeResponse() {
		byte[] data = new byte[] { 0x00, 0x05, /* message type: subsc_resp */
		0x00, 0x00, 0x00, 0x0c, /* message len: 12 bytes */
		(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, /*
															 * message id:
															 * 0xffffffff
															 */
		0x00, 0x00 /* status: SUCCESS */
		};
		SubscribeResponse sr = MessageDeserializer.deserialize(data);
		assertEquals(sr.getMessageHeader().getMessageType(), MessageType.SUBSCRIBE_RESPONSE);
		assertEquals(sr.getMessageHeader().getMessageLength(), 12);
		assertEquals(sr.getMessageHeader().getMessageId(), 0xffffffff);
		assertEquals(sr.getStatus(), Status.SUCCESS);
	}

	@Test
	public void testDeserializeUnsubscribe() {
		byte[] data = new byte[] { 0x00, 0x06, /* message type: unsubscribe */
		0x00, 0x00, 0x00, 0x0F, /* message len: 15 bytes */
		(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, /*
															 * message id:
															 * 0xffffffff
															 */
		0x00, 0x00,
		0x00, /* param id type: num */
		(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF /*
														    * param id val:
														    * 0xffffffff
														    */
		};

		Unsubscribe u = MessageDeserializer.deserialize(data);
		assertEquals(u.getMessageHeader().getMessageType(), MessageType.UNSUBSCRIBE);
		assertEquals(u.getMessageHeader().getMessageLength(), 15);
		assertEquals(u.getMessageHeader().getMessageId(), 0xffffffff);
		assertEquals(u.getParamId().<Integer> getValue().intValue(), 0xffffffff);

		data = new byte[] { 0x00, 0x06, /* message type: subscribe */
		0x00, 0x00, 0x00, 0x10, /* message len: 16 bytes */
		(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, /*
															 * message id:
															 * 0xffffffff
															 */
		0x00, 0x00,
		0x01, /* param id type: alphanum */
		0x00, 0x03, /* param id len: 3 */
		0x66, 0x6F, 0x6F, /* param id str: "foo" */
		};

		u = MessageDeserializer.deserialize(data);
		assertEquals(u.getMessageHeader().getMessageType(), MessageType.UNSUBSCRIBE);
		assertEquals(u.getMessageHeader().getMessageLength(), 16);
		assertEquals(u.getMessageHeader().getMessageId(), 0xffffffff);
		assertEquals(u.getParamId().getValue(), "foo");
	}

	@Test
	public void testDeserializeUnsubscribeResponse() {
		byte[] data = new byte[] { 0x00, 0x07, /* message type: subsc_resp */
		0x00, 0x00, 0x00, 0x0c, /* message len: 12 bytes */
		(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, /*
															 * message id:
															 * 0xffffffff
															 */
		0x00, 0x00 /* status: SUCCESS */
		};
		UnsubscribeResponse ur = MessageDeserializer.deserialize(data);
		assertEquals(ur.getMessageHeader().getMessageType(), MessageType.UNSUBSCRIBE_RESPONSE);
		assertEquals(ur.getMessageHeader().getMessageLength(), 12);
		assertEquals(ur.getMessageHeader().getMessageId(), 0xffffffff);
		assertEquals(ur.getStatus(), Status.SUCCESS);
	}

	@Test
	public void testDeserializeNotification() {

		byte[] data = new byte[] { 0x00, 0x08, // msg type: notification
				0x00, 0x00, 0x00, 0x33, // msg len: 51 bytes
				(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, // msg id:
																	// 0xffffffff

				0x00, 0x03, // num of params: 3

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
				0x00, 0x00, 0x00, 0x17, // val: 23

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
				0x40, 0x09, 0x21, (byte) 0xFB, 0x54, 0x44, 0x2D, 0x18 // val:
																	  // 3.14....
		};

		Notification n = MessageDeserializer.deserialize(data);

		assertEquals(n.getMessageHeader().getMessageType(), MessageType.NOTIFICATION);
		assertEquals(n.getMessageHeader().getMessageLength(), 51);
		assertEquals(n.getMessageHeader().getMessageId(), 0xffffffff);
		assertEquals(n.getParamMap().size(), 3);

		ParamValue pv = n.getParamMap().get(new ParamId(0, "foo"));
		assertNotNull(pv);
		assertEquals(pv.getValue(), "bar");

		pv = n.getParamMap().get(new ParamId(0, 23));
		assertNotNull(pv);
		assertEquals(pv.<Short>getValue().shortValue(), (short) 42);

		pv = n.getParamMap().get(new ParamId(0, "pi"));
		assertNotNull(pv);
		assertEquals(pv.<Double>getValue().doubleValue(), Math.PI, 0D);
	}
}
