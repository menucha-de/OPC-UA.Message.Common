package havis.opcua.message.common;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import havis.opcua.message.DataProvider;
import havis.opcua.message.MessageHandler;
import havis.opcua.message.common.model.Call;
import havis.opcua.message.common.model.CallResponse;
import havis.opcua.message.common.model.Event;
import havis.opcua.message.common.model.Message;
import havis.opcua.message.common.model.MessageHeader;
import havis.opcua.message.common.model.Notification;
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
import havis.opcua.message.common.server.MessageListener;
import havis.opcua.message.common.server.MessageServer;
import havis.opcua.message.exception.ApplicationException;
import havis.opcua.message.exception.NoSuchParameterException;
import havis.opcua.message.exception.ParameterException;

public class MessageHandlerCommon implements MessageHandler, MessageListener {

	private MessageServer msgServer;
	private DataProvider provider;
	private static final Logger LOG = Logger.getLogger(MessageHandler.class.getName());
	private static final String PROP_PORT = "havis.opcua.message.MessageHandler.port";
	private static final String PROP_RECV_TIMEOUT = "havis.opcua.message.MessageHandler.recvTimeout";

	private final ReentrantLock messageLock = new ReentrantLock();
	private final Logger log = Logger.getLogger(MessageHandlerCommon.class.getName());

	public MessageHandlerCommon() {
		/* set the message server port from system property */
		int port = MessageServer.PORT;
		String portProp = System.getProperty(PROP_PORT);
		if (portProp != null) {
			try {
				port = Integer.parseInt(portProp);
			} catch (Exception ex) {
				log.log(Level.SEVERE, "Invalid port type: expected integer: " + System.getProperty(PROP_PORT), ex);
			}
		}

		/* set the receive timeout from system property */
		int recvTimeout = MessageServer.RECV_TIMEOUT_MS;
		String recvTimeoutProp = System.getProperty(PROP_RECV_TIMEOUT);
		if (recvTimeoutProp != null) {
			try {
				recvTimeout = Integer.parseInt(recvTimeoutProp);
			} catch (Exception ex) {
				log.log(Level.SEVERE,
						"Invalid receive time out type: expected integer: " + System.getProperty(PROP_RECV_TIMEOUT),
						ex);
			}
		}

		this.msgServer = new MessageServer(port, recvTimeout);
		LOG.log(Level.FINER, "Message server port: {0}", port);
		LOG.log(Level.FINER, "Receive timeout: {0} ms", recvTimeout);
		this.msgServer.setMessageListener(this);
	}

	public void startMessageServer() throws Exception {
		this.msgServer.start();
	}

	public void stopMessageServer() throws Exception {
		this.msgServer.stop();
	}

	@Override
	public void open(DataProvider provider) {
		try {
			this.startMessageServer();
			this.provider = provider;
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Cannot open message handler", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see havis.opcua.message.MessageHandler#close()
	 */
	@Override
	public void close() {
		try {
			this.stopMessageServer();
			this.provider = null;
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Cannot close message handler", e);
		}
	}

	// Key: string or #int
	/*
	 * (non-Javadoc)
	 * 
	 * @see havis.opcua.message.MessageHandler#notify(java.util.Map)
	 */
	@Override
	public void notify(Map<String, Object> map) {
		try {
			Notification n = new Notification();
			n.setUntypedParamMap(map);
			this.msgServer.submit(ByteBuffer.wrap(MessageSerializer.serialize(n)));
			LOG.log(Level.FINEST, "SENT: {0}", n);
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to send notify: {0}", e);
		}
	}

	@Override
	public void event(String eventId, String paramId, Date timestamp, int severity, String message,
			Map<String, Object> map) {
		try {
			Event e = new Event(eventId, paramId, timestamp, severity, message);
			e.setUntypedParamMap(map);
			this.msgServer.submit(ByteBuffer.wrap(MessageSerializer.serialize(e)));
			LOG.log(Level.FINEST, "SENT: {0}", e);
		} catch (Exception ex) {
			LOG.log(Level.SEVERE, "Failed to send event: {0}", ex);
		}
	}

	@Override
	public void received(MessageHeader msgHdr, ByteBuffer msgBodyBytes) {
		Message msg = null;

		try {
			msg = MessageDeserializer.deserialize(msgHdr, msgBodyBytes);
		} catch (Exception ex) {
			LOG.log(Level.SEVERE, "Failed to deserialize message: type={0}, size={1}, id={2}, body={3}",
					new Object[] { msgHdr.getMessageType(), msgHdr.getMessageLength(), msgHdr.getMessageId(),
							bytesToHex(msgBodyBytes.array()) });
		}

		if (LOG.isLoggable(Level.FINEST))
			LOG.log(Level.FINEST, "Received: {0}",
					bytesToHex(msg.serialize(ByteBuffer.allocate(msg.getByteCount())).array()));

		if (this.provider == null) {
			LOG.log(Level.FINE, "No data provider instance available. All requests will be "
					+ "responded with status 'INVALID_MESSAGE'.");
		}

		Status status = null;
		ParamValue result = null;

		switch (msgHdr.getMessageType()) {

		case READ:
			Read r = (Read) msg;
			LOG.log(Level.FINEST, "RECV: {0}", r);

			try {
				Object obj = this.provider.read((r.getParamId().isNumeric() ? "#" : "") + r.getParamId().getValue());

				result = new ParamValue(obj);
				status = Status.SUCCESS;
			} catch (IllegalArgumentException ex) {
				LOG.log(Level.SEVERE, "Cannot read data: " + Status.INVALID_PARAM_TYPE, ex);
				status = Status.INVALID_PARAM_TYPE;
			} catch (NoSuchParameterException ex) {
				// level FINE instead of ERROR due to unused variables
				LOG.log(Level.FINE, "Cannot read data: " + Status.INVALID_PARAMETER, ex);
				status = Status.INVALID_PARAMETER;
			} catch (ParameterException ex) {
				LOG.log(Level.SEVERE, "Cannot read data: " + Status.INVALID_PARAM_VALUE, ex);
				status = Status.INVALID_PARAM_VALUE;
			} catch (Exception ex) {
				LOG.log(Level.SEVERE, "Cannot read data: " + Status.INVALID_PARAM_VALUE, ex);
				status = Status.INVALID_MESSAGE;
			}

			ReadResponse rr = status == Status.SUCCESS ? new ReadResponse(r, result, status)
					: new ReadResponse(msgHdr.getMessageId(), status);

			try {
				this.msgServer.submit(ByteBuffer.wrap(MessageSerializer.serialize(rr)));
				LOG.log(Level.FINEST, "RESP: {0}", rr);

			} catch (IOException e) {
				LOG.log(Level.SEVERE, "Failed to send READ_RESP. ", e);
			}

			break;

		case WRITE:
			Write w = (Write) msg;
			LOG.log(Level.FINEST, "RESV: {0}", w);

			try {
				this.provider.write((w.getParamId().isNumeric() ? "#" : "") + w.getParamId().getValue(),
						w.getParamValue().asGeneric());
				status = Status.SUCCESS;
			} catch (NullPointerException npe) {
				LOG.log(Level.SEVERE, "Cannot write data: " + Status.INVALID_MESSAGE, npe);
				status = Status.INVALID_MESSAGE;
			} catch (NoSuchParameterException ex) {
				// level FINE instead of ERROR due to unused variables
				LOG.log(Level.FINE, "Cannot write data: " + Status.INVALID_PARAMETER, ex);
				status = Status.INVALID_PARAMETER;
			} catch (ParameterException ex) {
				LOG.log(Level.SEVERE, "Cannot write data: " + Status.INVALID_PARAM_VALUE, ex);
				status = Status.INVALID_PARAM_VALUE;
			}

			WriteResponse wr = status == Status.SUCCESS ? new WriteResponse(w, status)
					: new WriteResponse(msgHdr.getMessageId(), status);

			try {
				this.msgServer.submit(ByteBuffer.wrap(MessageSerializer.serialize(wr)));
				LOG.log(Level.FINEST, "RESP: {0}", wr);
			} catch (IOException e) {
				LOG.log(Level.SEVERE, "Failed to send WRITE_RESP. ", e);
			}

			break;

		case SUBSCRIBE:
			Subscribe s = (Subscribe) msg;
			LOG.log(Level.FINEST, "RESV: {0}", s);

			try {
				this.provider.subscribe((s.getParamId().isNumeric() ? "#" : "") + s.getParamId().getValue());
				status = Status.SUCCESS;
			} catch (NullPointerException npe) {
				LOG.log(Level.SEVERE, "Cannot subscribe: " + Status.INVALID_MESSAGE, npe);
				status = Status.INVALID_MESSAGE;
			} catch (NoSuchParameterException ex) {
				// level FINE instead of ERROR due to unused variables
				LOG.log(Level.FINE, "Cannot subscribe: " + Status.INVALID_PARAMETER, ex);
				status = Status.INVALID_PARAMETER;
			} catch (ParameterException ex) {
				LOG.log(Level.SEVERE, "Cannot subscribe: " + Status.INVALID_PARAM_VALUE, ex);
				status = Status.INVALID_PARAM_VALUE;
			}

			SubscribeResponse sr = status == Status.SUCCESS ? new SubscribeResponse(s, status)
					: new SubscribeResponse(msgHdr.getMessageId(), status);

			try {
				this.msgServer.submit(ByteBuffer.wrap(MessageSerializer.serialize(sr)));
				LOG.log(Level.FINE, "RESP: {0}", sr);

			} catch (IOException e) {
				LOG.log(Level.SEVERE, "Failed to send SUBSCRIBE_RESP.", e);
			}

			break;

		case UNSUBSCRIBE:
			Unsubscribe u = (Unsubscribe) msg;
			LOG.log(Level.FINEST, "RECV: {0}", u);

			try {
				this.provider.unsubscribe((u.getParamId().isNumeric() ? "#" : "") + u.getParamId().getValue());
				status = Status.SUCCESS;
			} catch (NullPointerException npe) {
				LOG.log(Level.SEVERE, "Cannot unsubscribe: " + Status.INVALID_MESSAGE, npe);
				status = Status.INVALID_MESSAGE;
			} catch (NoSuchParameterException ex) {
				// level FINE instead of ERROR due to unused variables
				LOG.log(Level.FINE, "Cannot unsubscribe: " + Status.INVALID_PARAMETER, ex);
				status = Status.INVALID_PARAMETER;
			} catch (ParameterException ex) {
				LOG.log(Level.SEVERE, "Cannot unsubscribe: " + Status.INVALID_PARAM_VALUE, ex);
				status = Status.INVALID_PARAM_VALUE;
			}

			UnsubscribeResponse ur = status == Status.SUCCESS ? new UnsubscribeResponse(u, status)
					: new UnsubscribeResponse(msgHdr.getMessageId(), status);

			try {
				this.msgServer.submit(ByteBuffer.wrap(MessageSerializer.serialize(ur)));
				LOG.log(Level.FINEST, "RESP: {0}", ur);

			} catch (IOException e) {
				LOG.log(Level.SEVERE, "Failed to send UNSUBSCRIBE_RESP.", e);
			}

			break;

		case CALL:

			final Call c = (Call) msg;
			LOG.log(Level.FINEST, "RESV: {0}", c);
			new Thread(new CallThread(msgHdr, c), CallThread.class.getSimpleName()).start();
			break;

		case NOTIFICATION:
		case READ_RESPONSE:
		case WRITE_RESPONSE:
		case SUBSCRIBE_RESPONSE:
		case UNSUBSCRIBE_RESPONSE:
		case CALL_RESPONSE:
		case EVENT:
			LOG.log(Level.FINE, "Unsupported message received: {0}", msg.getMessageHeader().getMessageType());
			break;
		}

	}

	@Override
	public void sent(ByteBuffer bb) {
		if (LOG.isLoggable(Level.FINEST))
			LOG.log(Level.FINEST, "Sent: {0}", bytesToHex(bb.array()));
	}

	@Override
	public void accepted(SocketAddress remoteHost) {
		LOG.log(Level.FINEST, "Connected: {0}", remoteHost);
	}

	@Override
	public void disconnected(SocketAddress remoteHost) {
		LOG.log(Level.FINEST, "Disconnected: {0}", remoteHost);

		try {
			LOG.log(Level.FINEST, "Resetting subscriptions.");
			this.provider.unsubscribe("*");
		} catch (ParameterException e) {
			LOG.log(Level.SEVERE, "Failed to reset subscriptions.", e);
		}

		try {
			LOG.log(Level.FINEST, "Stopping scan.");
			this.provider.call("rfr310.ScanStop", "rfr310", new Object[] {});
		} catch (ApplicationException aex) {
			/* no matter if no scan is running */
		} catch (ParameterException e) {
			LOG.log(Level.SEVERE, "Failed to stop scan.", e);
		}
	}

	@Override
	public void rejected(SocketAddress remoteHost) {
		LOG.log(Level.FINEST, "Connection attempt rejected because of a connection already exists to host: {0}",
				remoteHost);
	}

	@Override
	public void errorOccurred(Throwable error) {
		LOG.log(Level.FINER, "An error occurred.", error);
	}

	public static String bytesToHex(byte[] bytes) {
		if (bytes == null)
			return null;

		char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

		StringBuffer sb = new StringBuffer();

		for (int iByte = 0; iByte < bytes.length; iByte++) {
			byte b = bytes[iByte];
			int b0 = (b & 0xf0) >> 4;
			int b1 = b & 0x0f;

			sb.append(hexChars[b0]);
			sb.append(hexChars[b1]);
			sb.append(" ");
		}

		return sb.toString();
	}

	class CallThread implements Runnable {
		private Call c;
		private MessageHeader msgHeader;

		public CallThread(MessageHeader msgHeader, Call c) {
			this.c = c;
			this.msgHeader = msgHeader;
		}

		@Override
		public void run() {

			MessageHandlerCommon.this.messageLock.lock();

			try {

				Status status = null;
				List<ParamValue> resultList = null;

				try {

					Object[] params = new Object[c.getParamList().size()];
					for (int i = 0; i < params.length; i++)
						params[i] = c.getParamList().get(i).asGeneric();

					Object[] results = MessageHandlerCommon.this.provider.call(
							(c.getMethodId().isNumeric() ? "#" : "") + c.getMethodId().getValue(),
							(c.getParamId().isNumeric() ? "#" : "") + c.getParamId().getValue(), params);

					resultList = new ArrayList<>();
					for (Object obj : results)
						resultList.add(new ParamValue(obj));

					status = Status.SUCCESS;

				} catch (NoSuchParameterException ex) {
					// level FINE instead of ERROR due to unused variables
					log.log(Level.FINE, "Cannot call method", ex);
					status = Status.INVALID_PARAMETER;
				} catch (ApplicationException ex) {
					status = Status.APPLICATION_ERROR;
					resultList = new ArrayList<>();
					resultList.add(new ParamValue(ex.getErrorCode()));
					resultList.add(new ParamValue(ex.getMessage()));

					/* DIAG */
					log.log(Level.SEVERE, "Sending application error {0} {1}",
							new Object[] { Integer.toHexString(ex.getErrorCode()), ex.getMessage() });
					/* DIAG */
				} catch (ParameterException ex) {
					log.log(Level.SEVERE, "Cannot call method", ex);
					status = Status.INVALID_PARAM_VALUE;
				} catch (Exception ex) {
					log.log(Level.SEVERE, "Cannot call method", ex);
					status = Status.INVALID_MESSAGE;
				}

				CallResponse cr = status == Status.SUCCESS || status == Status.APPLICATION_ERROR
						? new CallResponse(c, resultList, status) : new CallResponse(msgHeader.getMessageId(), status);
				try {
					MessageHandlerCommon.this.msgServer.submit(ByteBuffer.wrap(MessageSerializer.serialize(cr)));
					LOG.log(Level.FINEST, "RESP: {0}", cr);

				} catch (IOException e) {
					LOG.log(Level.SEVERE, "Failed to send CALL_RESP.", e);
				}

			} finally {
				MessageHandlerCommon.this.messageLock.unlock();
			}
		}
	}
}
