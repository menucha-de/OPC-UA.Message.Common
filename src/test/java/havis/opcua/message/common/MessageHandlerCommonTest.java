package havis.opcua.message.common;

import static mockit.Deencapsulation.getField;
import static mockit.Deencapsulation.setField;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import havis.opcua.message.DataProvider;
import havis.opcua.message.common.model.Call;
import havis.opcua.message.common.model.CallResponse;
import havis.opcua.message.common.model.Event;
import havis.opcua.message.common.model.Message;
import havis.opcua.message.common.model.MessageHeader;
import havis.opcua.message.common.model.MessageType;
import havis.opcua.message.common.model.Notification;
import havis.opcua.message.common.model.ParamId;
import havis.opcua.message.common.model.ParamValue;
import havis.opcua.message.common.model.Read;
import havis.opcua.message.common.model.ReadResponse;
import havis.opcua.message.common.model.Status;
import havis.opcua.message.common.model.Subscribe;
import havis.opcua.message.common.model.SubscribeResponse;
import havis.opcua.message.common.model.Unsubscribe;
import havis.opcua.message.common.model.UnsubscribeResponse;
import havis.opcua.message.common.model.Write;
import havis.opcua.message.common.model.WriteResponse;
import havis.opcua.message.common.serialize.MessageDeserializer;
import havis.opcua.message.common.serialize.MessageSerializer;
import havis.opcua.message.common.server.MessageServer;
import havis.opcua.message.exception.NoSuchParameterException;
import havis.opcua.message.exception.ParameterException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

import org.junit.Test;

@SuppressWarnings("unused")
public class MessageHandlerCommonTest {

	@Test
	public void testMessageHandlerCommon(@Mocked final MessageServer msgServer) throws Exception {
		
		/*
		 * Test: 
		 * - Constructor call
		 * 
		 * Expected: 
		 * - MessageServer constructor with port and recvTimeout
		 * - instance var msgServer is set 
		 * - method setMessageListener of MessageServer instance is called 
		 *   with MessageHandlerCommon instance
		 */
		
		final MessageHandlerCommon mhc = new MessageHandlerCommon();
		
		new Verifications() {{			
			int port, recvTimeout;			
			new MessageServer(port = withCapture(), recvTimeout = withCapture());
			times = 1;
			
			assertEquals(MessageServer.PORT, port);
			assertEquals(MessageServer.RECV_TIMEOUT_MS, recvTimeout);
			
			MessageServer msgServer2 = getField(mhc,"msgServer");
			assertNotNull(msgServer2);
			
			MessageHandlerCommon mhc2;
			msgServer.setMessageListener(mhc2 = withCapture());
			assertEquals(mhc, mhc2);						
		}};
	}

	@Test
	public void testStartMessageServer(@Mocked final MessageServer msgServer) throws Exception {
		/* 
		 * Test: 
		 * - start method of msgServer is called without exception
		 * 
		 * Expected: 
		 * - no exception is thrown
		 */
		
		final MessageHandlerCommon mhc = new MessageHandlerCommon();		
		
		try {
			mhc.startMessageServer();
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		
		/* 
		 * Test:
		 * 	- start method of msgServer is called with exception
		 * 
		 * Expected: 
		 * 	- exception is thrown 
		 */
		
		new NonStrictExpectations() {{
			msgServer.start();
			result = new Exception();
		}};

		try {
			mhc.startMessageServer();
			fail("Expected exception");
		} catch (Exception e) {
			
		}		
	}

	@Test
	public void testStopMessageServer(@Mocked final MessageServer msgServer) throws Exception {
		/* 
		 * Test:
		 * - stop method of msgServer is called without exception
		 * 
		 * Expected: 
		 * - no exception is thrown
		 */
		
		final MessageHandlerCommon mhc = new MessageHandlerCommon();		
		
		try {
			mhc.stopMessageServer();
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		
		/* 
		 * Test
		 * - stop method of msgServer is called with exception
		 * 
		 * Expected: 
		 * - exception is thrown 
		 */
		
		new NonStrictExpectations() {{
			msgServer.stop();
			result = new Exception();
		}};

		try {
			mhc.stopMessageServer();
			fail("Expected exception");
		} catch (Exception e) {
			
		}	
	}

	@Test
	public void testOpen(@Mocked final DataProvider provider) throws Exception {
		
		/* 
		 * Test:
		 * - startMessageServer throws no exception
		 * Expected:
		 * - provider instance var is not null  
		 */
		
		final MessageHandlerCommon mhc = new  MessageHandlerCommon();
		
		new NonStrictExpectations(mhc) {{			
			
		}};
		
		mhc.open(provider);
		
		new Verifications(){{
			mhc.startMessageServer();
			times = 1;			
			
			assertNotNull(getField(mhc, "provider"));
		}};
		
		/* 
		 * Test:
		 * - startMessageServer throws exception
		 * Expected:
		 * - provider instance var is null  
		 */
		
		new NonStrictExpectations() {{			
			mhc.startMessageServer();
			result = new Exception();				
		}};
		
		setField(mhc, "provider", null);
		mhc.open(provider);
		
		new Verifications(){{
			mhc.startMessageServer();
			times = 1;			
			
			assertNull(getField(mhc, "provider"));
		}};
		
	}

	@Test
	public void testClose(@Mocked final DataProvider provider) throws Exception {
		/* 
		 * Test:
		 * - stopMessageServer throws no exception
		 * Expected:
		 * - provider instance var is null  
		 */
		
		final MessageHandlerCommon mhc = new  MessageHandlerCommon();
		
		setField(mhc, "provider", provider);
		new NonStrictExpectations(mhc) {{			
			
		}};
		
		mhc.close();
		
		new Verifications(){{
			mhc.stopMessageServer();
			times = 1;			
			
			assertNull(getField(mhc, "provider"));
		}};
		
		/* 
		 * Test:
		 * - stopMessageServer throws exception
		 * Expected:
		 * - provider instance var is not null  
		 */
		
		new NonStrictExpectations() {{			
			mhc.stopMessageServer();
			result = new IOException();				
		}};
		
		setField(mhc, "provider", provider);
		mhc.close();
		
		new Verifications(){{
			mhc.stopMessageServer();
			times = 1;			
			
			assertNotNull(getField(mhc, "provider"));
		}};
	}

	
	@Test
	@SuppressWarnings("unchecked")
	public void testNotify(@Mocked final Notification notification, 
			@Mocked final MessageServer msgServer, 
			@Mocked MessageSerializer msgSer) throws IOException {
		
		/*
		 * Test: 
		 * - notify call with no exception
		 * Expected:
		 * - setUntypedParamMap called with specified map
		 * - submit called with specified byte buffer  
		 */
		
		final MessageHandlerCommon mhc = new MessageHandlerCommon();
		
		final Map<String, Object> map = new HashMap<>();
		map.put("foo", "bar");
		
		new NonStrictExpectations() {{
			MessageSerializer.serialize((Message)any);
			result = new byte[] { (byte)0xaa, (byte)0xbb, (byte)0xcc, (byte)0xdd };		
		}};
		
		final ByteBuffer bb = 
			ByteBuffer.wrap(MessageSerializer.serialize(notification));
		
		mhc.notify(map);
		
		new Verifications() {{
			final Map<String, Object> map2;
			notification.setUntypedParamMap(map2 = withCapture());
			assertEquals(map, map2);
			
			final ByteBuffer bb2; 					
			msgServer.submit(bb2 = withCapture());
			
			assertArrayEquals(bb.array(), bb2.array());			
		}}; 
		
		
		/*
		 * Test: 
		 * - notify call with exception during serialize call
		 * Expected:
		 * - no exception is thrown
		 * - submit is not called   
		 */
		
		new NonStrictExpectations() {{
			MessageSerializer.serialize((Message)any);
			result = new Exception();
		}};
		
		try {  mhc.notify(map);	}
		catch (Exception ex) { fail("Unexpected exception"); }
		
		new Verifications() {{
			msgServer.submit((ByteBuffer)any);
			times = 0;
		}};
		
		/*
		 * Test: 
		 * - notify call with exception during submit call
		 * Expected:
		 * - no exception is thrown   
		 */
		
		new NonStrictExpectations() {{
			MessageSerializer.serialize((Message)any);
			result = new byte[] { (byte)0xaa, (byte)0xbb, (byte)0xcc, (byte)0xdd };
			
			msgServer.submit((ByteBuffer)any);
			result = new Exception();
		}};
		
		try {  mhc.notify(map);	}
		catch (Exception ex) { fail("Unexpected exception"); }
		
		/*
		 * Test: 
		 * - notify call with exception during setUntypedParamMap call
		 * Expected:
		 * - no exception is thrown
		 * - submit is not called  
		 */		
		new NonStrictExpectations() {{
			notification.setUntypedParamMap((Map<String, Object>)any);
			result = new Exception();
		}};
		
		try { mhc.notify(map);	}
		catch (Exception ex) { fail("Unexpected exception"); }
		
		new Verifications() {{
			msgServer.submit((ByteBuffer)any);
			times = 0;
		}};
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testEvent(@Mocked final Event event, 
			@Mocked final MessageServer msgServer, 
			@Mocked MessageSerializer msgSer) throws IOException {		
		/*
		 * Test: 
		 * - event call with no exception
		 * Expected:
		 * - Event constructor is called with specified params (eventId, paramId etc.)
		 * - setUntypedParamMap called with specified map
		 * - submit called with specified byte buffer  
		 */
		
		final MessageHandlerCommon mhc = new MessageHandlerCommon();
		
		final String eventId = "eventId";
		final String paramId = "paramId";
		final Date timestamp = new Date();
		final int severity = 500;
		final String message = "message";
		final Map<String, Object> map = new HashMap<>();
		map.put("foo", "bar");
		
		new NonStrictExpectations() {{
			MessageSerializer.serialize((Message)any);
			result = new byte[] { (byte)0xaa, (byte)0xbb, (byte)0xcc, (byte)0xdd };		
		}};
		
		final ByteBuffer bb = 
			ByteBuffer.wrap(MessageSerializer.serialize(event));
		
		mhc.event(eventId, paramId, timestamp, severity, message, map);
		
		new Verifications() {{
			final String eventId2;
			final String paramId2;
			final Date timestamp2;
			final int severity2;
			final String message2;
			final Map<String, Object> map2;
			
			new Event(eventId2 = withCapture(), paramId2 = withCapture(), timestamp2 = withCapture(), 
					severity2 = withCapture(), message2 = withCapture());
			
			assertEquals(eventId, eventId2);
			assertEquals(paramId, paramId2);
			assertEquals(timestamp, timestamp2);
			assertEquals(severity, severity2);
			assertEquals(message, message2);
			
			event.setUntypedParamMap(map2 = withCapture());
			assertEquals(map, map2);
			
			final ByteBuffer bb2; 					
			msgServer.submit(bb2 = withCapture());
			
			assertArrayEquals(bb.array(), bb2.array());			
		}}; 
		
		/*
		 * Test: 
		 * - event call with exception during serialize call 
		 * Expected:
		 * - no exception is thrown
		 * - submit is not called   
		 */
		
		new NonStrictExpectations() {{
			MessageSerializer.serialize((Message)any);
			result = new Exception();
		}};
		
		try {  mhc.event(eventId, paramId, timestamp, severity, message, map); }
		catch (Exception ex) { fail("Unexpected exception"); }
		
		new Verifications() {{
			msgServer.submit((ByteBuffer)any);
			times = 0;
		}};
		
		/*
		 * Test: 
		 * - event call with exception during submit call
		 * Expected:
		 * - no exception is thrown   
		 */
		
		new NonStrictExpectations() {{
			MessageSerializer.serialize((Message)any);
			result = new byte[] { (byte)0xaa, (byte)0xbb, (byte)0xcc, (byte)0xdd };
			
			msgServer.submit((ByteBuffer)any);
			result = new Exception();
		}};
		
		try {  mhc.event(eventId, paramId, timestamp, severity, message, map); }
		catch (Exception ex) { fail("Unexpected exception"); }
		
		/*
		 * Test: 
		 * - event call with exception during setUntypedParamMap call
		 * Expected:
		 * - no exception is thrown
		 * - submit is not called  
		 */		
		new NonStrictExpectations() {{
			event.setUntypedParamMap((Map<String, Object>)any);
			result = new Exception();
		}};
		
		try {  mhc.event(eventId, paramId, timestamp, severity, message, map); }
		catch (Exception ex) { fail("Unexpected exception"); }
		
		new Verifications() {{
			msgServer.submit((ByteBuffer)any);
			times = 0;
		}};
	}

	@Test
	public void testReceived(@Mocked final DataProvider provider, 
			@Mocked final MessageHeader mHdr, 
			@Mocked final MessageSerializer msgSer,
			@Mocked final MessageDeserializer msgDeSer, 
			@Mocked final MessageServer msgServer, 
			@Mocked final Read read, 
			@Mocked final Write write, 
			@Mocked final Subscribe subscribe, 
			@Mocked final Unsubscribe unsubscribe,
			@Mocked final Call call, 
			@Mocked final ParameterException paramException) throws ParameterException {

		MessageHandlerCommon mhc = new MessageHandlerCommon();
		
		/*
		 * Test: 
		 * - exception occurs during deserialization of READ message 
		 * 
		 * Expected:
		 * - Message msg remains null yielding an NPE 
		 *   causing a read response with status INVALID_MESSAGE. 
		 */
		
		new NonStrictExpectations() {{
			MessageDeserializer.deserialize((MessageHeader)any, (ByteBuffer)any);
			result = new Exception();
			
			mHdr.getMessageId(); 
			result = 42;
			
			mHdr.getMessageLength();
			result = 23;
			
			mHdr.getMessageType();
			result = MessageType.READ;
			
			read.getParamId();
			result = new ParamId(-1, "rfr310.test");
			
			write.getParamId();
			result = new ParamId(-1, "rfr310.test");
			
			write.getParamValue();
			result = new ParamValue(42);
			
			subscribe.getParamId();
			result = new ParamId(-1, "rfr310.test");
			
			unsubscribe.getParamId();
			result = new ParamId(-1, "rfr310.test");
			
			call.getParamId();
			result = new ParamId(-1, "rfr310.test");
			
		}};
		
		mhc.received(mHdr, ByteBuffer.wrap(new byte[] { 0x11, 0x22, 0x33, 0x44 }));
		
		new Verifications() {{ 			
			Message msg;			
			MessageSerializer.serialize(msg = withCapture());			
			
			assertTrue(msg instanceof ReadResponse);
			assertEquals(Status.INVALID_MESSAGE, ((ReadResponse)msg).getStatus());
		}};
		
		/*
		 * Test: 
		 * - exception occurs during deserialization of WRITE message 
		 * 
		 * Expected:
		 * - Message msg remains null yielding an NPE 
		 *   causing a write response with status INVALID_MESSAGE. 
		 */
		
		new NonStrictExpectations() {{
			mHdr.getMessageType();
			result = MessageType.WRITE;						
		}};
		
		mhc.received(mHdr, ByteBuffer.wrap(new byte[] { 0x11, 0x22, 0x33, 0x44 }));
		
		new Verifications() {{ 			
			Message msg;			
			MessageSerializer.serialize(msg = withCapture());			
			
			assertTrue(msg instanceof WriteResponse);
			assertEquals(Status.INVALID_MESSAGE, ((WriteResponse)msg).getStatus());
		}};
		
		/*
		 * Test: 
		 * - exception occurs during deserialization of SUBSCRIBE message 
		 * 
		 * Expected:
		 * - Message msg remains null yielding an NPE 
		 *   causing a subscribe response with status INVALID_MESSAGE. 
		 */
		
		new NonStrictExpectations() {{
			mHdr.getMessageType();
			result = MessageType.SUBSCRIBE;						
		}};
		
		mhc.received(mHdr, ByteBuffer.wrap(new byte[] { 0x11, 0x22, 0x33, 0x44 }));
		
		new Verifications() {{ 			
			Message msg;			
			MessageSerializer.serialize(msg = withCapture());			
			
			assertTrue(msg instanceof SubscribeResponse);
			assertEquals(Status.INVALID_MESSAGE, ((SubscribeResponse)msg).getStatus());
		}};
		
		/*
		 * Test: 
		 * - exception occurs during deserialization of UNSUBSCRIBE message 
		 * 
		 * Expected:
		 * - Message msg remains null yielding an NPE 
		 *   causing an unsubscribe response with status INVALID_MESSAGE. 
		 */
		
		new NonStrictExpectations() {{
			mHdr.getMessageType();
			result = MessageType.UNSUBSCRIBE;						
		}};
		
		mhc.received(mHdr, ByteBuffer.wrap(new byte[] { 0x11, 0x22, 0x33, 0x44 }));
		
		new Verifications() {{ 			
			Message msg;			
			MessageSerializer.serialize(msg = withCapture());			
			
			assertTrue(msg instanceof UnsubscribeResponse);
			assertEquals(Status.INVALID_MESSAGE, ((UnsubscribeResponse)msg).getStatus());
		}};
		
		/*
		 * Test: 
		 * - exception occurs during deserialization of CALL message 
		 * 
		 * Expected:
		 * - Message msg remains null yielding an NPE 
		 *   causing a call response with status INVALID_MESSAGE. 
		 */
		
		new NonStrictExpectations() {{
			mHdr.getMessageType();
			result = MessageType.CALL;						
		}};
		
		mhc.received(mHdr, ByteBuffer.wrap(new byte[] { 0x11, 0x22, 0x33, 0x44 }));
		
		new Verifications() {{ 			
			Message msg;			
			MessageSerializer.serialize(msg = withCapture());			
			
			assertTrue(msg instanceof CallResponse);
			assertEquals(Status.INVALID_MESSAGE, ((CallResponse)msg).getStatus());
		}};
		
		/*
		 * Test: 
		 * - provider is null when performing a READ 
		 * 
		 * Expected:
		 * - provider being null yielding an NPE 
		 *   causing a read response with status INVALID_MESSAGE. 
		 */
		
		new NonStrictExpectations() {{
			MessageDeserializer.deserialize((MessageHeader)any, (ByteBuffer)any);
			result = read;
			
			mHdr.getMessageType();
			result = MessageType.READ;						
		}};
		
		mhc.received(mHdr, ByteBuffer.wrap(new byte[] { 0x11, 0x22, 0x33, 0x44 }));
		
		new Verifications() {{ 			
			Message msg;			
			MessageSerializer.serialize(msg = withCapture());			
			
			assertTrue(msg instanceof ReadResponse);
			assertEquals(Status.INVALID_MESSAGE, ((ReadResponse)msg).getStatus());
		}};
		
		/*
		 * Test: 
		 * - provider is null when performing a WRITE 
		 * 
		 * Expected:
		 * - provider being null yielding an NPE 
		 *   causing a write response with status INVALID_MESSAGE. 
		 */
		
		new NonStrictExpectations() {{
			MessageDeserializer.deserialize((MessageHeader)any, (ByteBuffer)any);
			result = write;
			
			mHdr.getMessageType();
			result = MessageType.WRITE;						
		}};
		
		mhc.received(mHdr, ByteBuffer.wrap(new byte[] { 0x11, 0x22, 0x33, 0x44 }));
		
		new Verifications() {{ 			
			Message msg;			
			MessageSerializer.serialize(msg = withCapture());			
			
			assertTrue(msg instanceof WriteResponse);
			assertEquals(Status.INVALID_MESSAGE, ((WriteResponse)msg).getStatus());
		}};
		
		/*
		 * Test: 
		 * - provider is null when performing a SUBSRIBE 
		 * 
		 * Expected:
		 * - provider being null yielding an NPE 
		 *   causing a subscribe response with status INVALID_MESSAGE. 
		 */
		
		new NonStrictExpectations() {{
			MessageDeserializer.deserialize((MessageHeader)any, (ByteBuffer)any);
			result = subscribe;
			
			mHdr.getMessageType();
			result = MessageType.SUBSCRIBE;						
		}};
		
		mhc.received(mHdr, ByteBuffer.wrap(new byte[] { 0x11, 0x22, 0x33, 0x44 }));
		
		new Verifications() {{ 			
			Message msg;			
			MessageSerializer.serialize(msg = withCapture());			
			
			assertTrue(msg instanceof SubscribeResponse);
			assertEquals(Status.INVALID_MESSAGE, ((SubscribeResponse)msg).getStatus());
		}};
		
		/*
		 * Test: 
		 * - provider is null when performing a UNSUBSCRIBE 
		 * 
		 * Expected:
		 * - provider being null yielding an NPE 
		 *   causing a unsubscribe response with status INVALID_MESSAGE. 
		 */
		
		new NonStrictExpectations() {{
			MessageDeserializer.deserialize((MessageHeader)any, (ByteBuffer)any);
			result = unsubscribe;
			
			mHdr.getMessageType();
			result = MessageType.UNSUBSCRIBE;						
		}};
		
		mhc.received(mHdr, ByteBuffer.wrap(new byte[] { 0x11, 0x22, 0x33, 0x44 }));
		
		new Verifications() {{ 			
			Message msg;			
			MessageSerializer.serialize(msg = withCapture());			
			
			assertTrue(msg instanceof UnsubscribeResponse);
			assertEquals(Status.INVALID_MESSAGE, ((UnsubscribeResponse)msg).getStatus());
		}};
		
		/*
		 * Test: 
		 * - provider is null when performing a CALL 
		 * 
		 * Expected:
		 * - provider being null yielding an NPE 
		 *   causing a call response with status INVALID_MESSAGE. 
		 */
		
		new NonStrictExpectations() {{
			MessageDeserializer.deserialize((MessageHeader)any, (ByteBuffer)any);
			result = call;
			
			mHdr.getMessageType();
			result = MessageType.CALL;						
		}};
		
		mhc.received(mHdr, ByteBuffer.wrap(new byte[] { 0x11, 0x22, 0x33, 0x44 }));
		
		try { Thread.sleep(20); } 
		catch (InterruptedException e) { }
		
		new Verifications() {{ 			
			Message msg;			
			MessageSerializer.serialize(msg = withCapture());			
			
			assertTrue(msg instanceof CallResponse);
			assertEquals(Status.INVALID_MESSAGE, ((CallResponse)msg).getStatus());
		}};
		
		setField(mhc, "provider", provider);
		
		/*
		 * Test: 
		 * - IllegalArgumentException is thrown by read method 
		 * 
		 * Expected:
		 * - read response with status INVALID_PARAM_TYPE. 
		 */
		
		new NonStrictExpectations() {{

			MessageDeserializer.deserialize((MessageHeader)any, (ByteBuffer)any);
			result = read;
			
			mHdr.getMessageType();
			result = MessageType.READ;
			
			provider.read(anyString);
			result = new IllegalArgumentException();
		}};
		
		mhc.received(mHdr, ByteBuffer.wrap(new byte[] { 0x11, 0x22, 0x33, 0x44 }));
		
		new Verifications() {{ 			
			Message msg;			
			MessageSerializer.serialize(msg = withCapture());			
			
			assertTrue(msg instanceof ReadResponse);
			assertEquals(Status.INVALID_PARAM_TYPE, ((ReadResponse)msg).getStatus());
		}};
		
		/*
		 * Test: 
		 * - NoSuchParameterException is thrown by read method 
		 * 
		 * Expected:
		 * - read response with status INVALID_PARAMETER. 
		 */
		
		new NonStrictExpectations() {{
			MessageDeserializer.deserialize((MessageHeader)any, (ByteBuffer)any);
			result = read;
			
			mHdr.getMessageType();
			result = MessageType.READ;
			
			provider.read(anyString);
			result = new NoSuchParameterException();
		}};
		
		mhc.received(mHdr, ByteBuffer.wrap(new byte[] { 0x11, 0x22, 0x33, 0x44 }));
		
		new Verifications() {{ 			
			Message msg;			
			MessageSerializer.serialize(msg = withCapture());			
			
			assertTrue(msg instanceof ReadResponse);
			assertEquals(Status.INVALID_PARAMETER, ((ReadResponse)msg).getStatus());
		}};
		
		/*
		 * Test: 
		 * - ParameterException is thrown by read method 
		 * 
		 * Expected:
		 * - read response with status INVALID_PARAM_VALUE. 
		 */
		
		new NonStrictExpectations() {{
			MessageDeserializer.deserialize((MessageHeader)any, (ByteBuffer)any);
			result = read;
			
			mHdr.getMessageType();
			result = MessageType.READ;
			
			provider.read(anyString);
			result = paramException;
		}};
		
		mhc.received(mHdr, ByteBuffer.wrap(new byte[] { 0x11, 0x22, 0x33, 0x44 }));
		
		new Verifications() {{ 			
			Message msg;			
			MessageSerializer.serialize(msg = withCapture());			
			
			assertTrue(msg instanceof ReadResponse);
			assertEquals(Status.INVALID_PARAM_VALUE, ((ReadResponse)msg).getStatus());
		}};
		
		/*
		 * Test: 
		 * - NoSuchParameterException is thrown by write method 
		 * 
		 * Expected:
		 * - write response with status INVALID_PARAMETER. 
		 */
		
		new NonStrictExpectations() {{
			MessageDeserializer.deserialize((MessageHeader)any, (ByteBuffer)any);
			result = write;
			
			mHdr.getMessageType();
			result = MessageType.WRITE;
			
			provider.write(anyString,any);
			result = new NoSuchParameterException();
		}};
		
		mhc.received(mHdr, ByteBuffer.wrap(new byte[] { 0x11, 0x22, 0x33, 0x44 }));
		
		new Verifications() {{ 			
			Message msg;			
			MessageSerializer.serialize(msg = withCapture());			
			
			assertTrue(msg instanceof WriteResponse);
			assertEquals(Status.INVALID_PARAMETER, ((WriteResponse)msg).getStatus());
		}};
		
		/*
		 * Test: 
		 * - ParameterException is thrown by write method 
		 * 
		 * Expected:
		 * - write response with status INVALID_PARAM_VALUE. 
		 */
		
		new NonStrictExpectations() {{
			MessageDeserializer.deserialize((MessageHeader)any, (ByteBuffer)any);
			result = write;
			
			mHdr.getMessageType();
			result = MessageType.WRITE;
			
			provider.write(anyString,any);
			result = paramException;
		}};
		
		mhc.received(mHdr, ByteBuffer.wrap(new byte[] { 0x11, 0x22, 0x33, 0x44 }));
		
		new Verifications() {{ 			
			Message msg;			
			MessageSerializer.serialize(msg = withCapture());			
			
			assertTrue(msg instanceof WriteResponse);
			assertEquals(Status.INVALID_PARAM_VALUE, ((WriteResponse)msg).getStatus());
		}};
		
		/*
		 * Test: 
		 * - NoSuchParameterException is thrown by subscribe method 
		 * 
		 * Expected:
		 * - subscribe response with status INVALID_PARAMETER. 
		 */
		
		new NonStrictExpectations() {{
			MessageDeserializer.deserialize((MessageHeader)any, (ByteBuffer)any);
			result = subscribe;
			
			mHdr.getMessageType();
			result = MessageType.SUBSCRIBE;
			
			provider.subscribe(anyString);
			result = new NoSuchParameterException();
		}};
		
		mhc.received(mHdr, ByteBuffer.wrap(new byte[] { 0x11, 0x22, 0x33, 0x44 }));
		
		new Verifications() {{ 			
			Message msg;			
			MessageSerializer.serialize(msg = withCapture());			
			
			assertTrue(msg instanceof SubscribeResponse);
			assertEquals(Status.INVALID_PARAMETER, ((SubscribeResponse)msg).getStatus());
		}};
		
		/*
		 * Test: 
		 * - ParameterException is thrown by subscribe method 
		 * 
		 * Expected:
		 * - subscribe response with status INVALID_PARAM_VALUE. 
		 */
		
		new NonStrictExpectations() {{
			MessageDeserializer.deserialize((MessageHeader)any, (ByteBuffer)any);
			result = subscribe;
			
			mHdr.getMessageType();
			result = MessageType.SUBSCRIBE;
			
			provider.subscribe(anyString);
			result = paramException;
		}};
		
		mhc.received(mHdr, ByteBuffer.wrap(new byte[] { 0x11, 0x22, 0x33, 0x44 }));
		
		new Verifications() {{ 			
			Message msg;			
			MessageSerializer.serialize(msg = withCapture());			
			
			assertTrue(msg instanceof SubscribeResponse);
			assertEquals(Status.INVALID_PARAM_VALUE, ((SubscribeResponse)msg).getStatus());
		}};
		
		/*
		 * Test: 
		 * - NoSuchParameterException is thrown by unsubscribe method 
		 * 
		 * Expected:
		 * - unsubscribe response with status INVALID_PARAMETER. 
		 */
		
		new NonStrictExpectations() {{
			MessageDeserializer.deserialize((MessageHeader)any, (ByteBuffer)any);
			result = unsubscribe;
			
			mHdr.getMessageType();
			result = MessageType.UNSUBSCRIBE;
			
			provider.unsubscribe(anyString);
			result = new NoSuchParameterException();
		}};
		
		mhc.received(mHdr, ByteBuffer.wrap(new byte[] { 0x11, 0x22, 0x33, 0x44 }));
		
		new Verifications() {{ 			
			Message msg;			
			MessageSerializer.serialize(msg = withCapture());			
			
			assertTrue(msg instanceof UnsubscribeResponse);
			assertEquals(Status.INVALID_PARAMETER, ((UnsubscribeResponse)msg).getStatus());
		}};
		
		/*
		 * Test: 
		 * - ParameterException is thrown by unsubscribe method 
		 * 
		 * Expected:
		 * - unsubscribe response with status INVALID_PARAM_VALUE. 
		 */
		
		new NonStrictExpectations() {{
			MessageDeserializer.deserialize((MessageHeader)any, (ByteBuffer)any);
			result = unsubscribe;
			
			mHdr.getMessageType();
			result = MessageType.UNSUBSCRIBE;
			
			provider.unsubscribe(anyString);
			result = paramException;
		}};
		
		mhc.received(mHdr, ByteBuffer.wrap(new byte[] { 0x11, 0x22, 0x33, 0x44 }));
		
		new Verifications() {{ 			
			Message msg;			
			MessageSerializer.serialize(msg = withCapture());			
			
			assertTrue(msg instanceof UnsubscribeResponse);
			assertEquals(Status.INVALID_PARAM_VALUE, ((UnsubscribeResponse)msg).getStatus());
		}};
		
		/*
		 * Test: 
		 * - NoSuchParameterException is thrown by call method 
		 * 
		 * Expected:
		 * - unsubscribe response with status INVALID_PARAMETER. 
		 */
		
		new NonStrictExpectations() {{
			MessageDeserializer.deserialize((MessageHeader)any, (ByteBuffer)any);
			result = call;
			
			mHdr.getMessageType();
			result = MessageType.CALL;
			
			provider.call(anyString,anyString,(Object[])any);
			result = new NoSuchParameterException();
		}};
		
		mhc.received(mHdr, ByteBuffer.wrap(new byte[] { 0x11, 0x22, 0x33, 0x44 }));
		try { Thread.sleep(20); } catch(Exception ex) {}
		
		new Verifications() {{ 			
			Message msg;			
			MessageSerializer.serialize(msg = withCapture());			
			
			assertTrue(msg instanceof CallResponse);
			assertEquals(Status.INVALID_PARAMETER, ((CallResponse)msg).getStatus());
		}};
		
		
		/*
		 * Test: 
		 * - ParameterException is thrown by call method 
		 * 
		 * Expected:
		 * - call response with status INVALID_PARAM_VALUE. 
		 */
		
		new NonStrictExpectations() {{
			MessageDeserializer.deserialize((MessageHeader)any, (ByteBuffer)any);
			result = call;
			
			mHdr.getMessageType();
			result = MessageType.CALL;
			
			provider.call(anyString,anyString,(Object[])any);
			result = paramException;
		}};
		
		mhc.received(mHdr, ByteBuffer.wrap(new byte[] { 0x11, 0x22, 0x33, 0x44 }));
		try { Thread.sleep(20); } catch(Exception ex) {}
		
		new Verifications() {{ 			
			Message msg;
			MessageSerializer.serialize(msg = withCapture());			
			
			assertTrue(msg instanceof CallResponse);
			assertEquals(Status.INVALID_PARAM_VALUE, ((CallResponse)msg).getStatus());
		}};
		
		//TODO: new ParamValue in READ case throws IllegalArgumentException
		//TODO: success tests 
		
	}

//	@Test
//	public void testSent() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testAccepted() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testDisconnected() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testRejected() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testErrorOccurred() {
//		fail("Not yet implemented");
//	}

}
