package havis.opcua.message.common.client;

import havis.opcua.message.DataProvider;
import havis.opcua.message.MessageHandler;
import havis.opcua.message.common.model.Call;
import havis.opcua.message.common.model.CallResponse;
import havis.opcua.message.common.model.Event;
import havis.opcua.message.common.model.Message;
import havis.opcua.message.common.model.MessageHeader;
import havis.opcua.message.common.model.MessageIdSeed;
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
import havis.opcua.message.exception.ApplicationException;
import havis.opcua.message.exception.InvalidParameterException;
import havis.opcua.message.exception.ParameterException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageClient {
	
	private MessageHandler mHdl;
	private SocketChannel client;
	private Logger log = Logger.getLogger(MessageClient.class.getName());
	private Queue<Message> msgQueue = new ConcurrentLinkedQueue<>();
	
	private static final int OPCUA_PORT = 4810;
	private static final int RECV_INTERVAL = 100;
	private static final int RECV_TIMEOUT = 500;
	private static final int RECV_RETRY_LIMIT = 0; //0 = infinite
	private static final int STOP_TIMEOUT = 2000;
	
	private NotifyThread notifyThread;
	private EventThread eventThread;
	private ReceiverThread receiverThread;
	
	private Lock socketLock = new ReentrantLock(true);
	private Condition messageReceived = socketLock.newCondition();
	private Condition receiverThreadStopped = socketLock.newCondition();
	private Condition notifyThreadStopped = socketLock.newCondition();
	private Condition eventThreadStopped = socketLock.newCondition();
		
	class ClientDataProvider implements DataProvider {
		
		@Override
		public Object[] call(String methodId, String paramId, Object[] params) throws ParameterException {			
			
			int msgId = MessageIdSeed.next();
			
			try {
				Call msg = new Call(new ParamId(methodId, true), new ParamId(-1, paramId), params, msgId);
				
				Message resp = MessageClient.this.sendMessage(msg);				
				if (resp == null) 
					throw new InvalidParameterException("Received null due to InterruptedException while waiting for result.");				
				
				CallResponse callResp = getSuccessfulResponse(resp);
				
				if (callResp.getStatus() == Status.APPLICATION_ERROR)
					throw ApplicationException.forErrorCode((Integer) 
							callResp.getResultList().get(0).getValue());				
				
				List<ParamValue> resultList = callResp.getResultList(); 
				Object[] ret = new Object[resultList.size()];
				int i = 0;
				for( ParamValue pVal : resultList ) 
					ret[i++] = pVal.asGeneric();
				
				return ret;
				
			} catch (ApplicationException e) {
				throw e;
			} catch (IOException e) {
				throw new InvalidParameterException("IOException occurred while sending read message.", e);
			} catch (ClassCastException e) {
				throw new InvalidParameterException("Received response of unexpected type.", e);
			} catch (Exception ex) {
				throw new InvalidParameterException(ex);
			}
		}
		
		@Override
		public Object read(String id) throws ParameterException {
			
			int msgId = MessageIdSeed.next();
			
			try {
				Read msg = new Read(new ParamId(id, true), msgId);
				Message resp = MessageClient.this.sendMessage(msg);				
				if (resp == null) 
					throw new InvalidParameterException("Received null due to InterruptedException while waiting for result.");				

				ReadResponse rdResp = getSuccessfulResponse(resp); 
				return rdResp.getResult().asGeneric();
				
			} catch (IllegalArgumentException | NullPointerException e) {
				throw new InvalidParameterException(e);
			} catch (IOException e) {
				throw new InvalidParameterException("IOException occurred while sending read message.", e);
			} catch (ClassCastException e) {
				throw new InvalidParameterException("Received response of unexpected type.", e);
			} catch (Exception ex) {
				throw new InvalidParameterException(ex);
			}
		}
		
		@Override
		public void write(String id, Object value) throws ParameterException {
			
			int msgId = MessageIdSeed.next();
			
			try {
				Write msg = new Write(new ParamId(id, true), new ParamValue(value), msgId);
				Message resp = MessageClient.this.sendMessage(msg);				
				if (resp == null) 
					throw new InvalidParameterException("Received null due to InterruptedException while waiting for result.");
				
				@SuppressWarnings("unused")
				WriteResponse writeResp = getSuccessfulResponse(resp); 				

			} catch (IllegalArgumentException | NullPointerException e) {
				throw new InvalidParameterException(e);
			} catch (IOException e) {
				throw new InvalidParameterException("IOException occurred while sending read message.", e);
			} catch (ClassCastException e) {
				throw new InvalidParameterException("Received response of unexpected type.", e);
			} catch (Exception ex) {
				throw new InvalidParameterException(ex);
			}				
		}

		@Override
		public void subscribe(String id) throws ParameterException {
			int msgId = MessageIdSeed.next();
			
			try {
				Subscribe msg = new Subscribe(new ParamId(id, true), msgId);
				Message resp = MessageClient.this.sendMessage(msg);				
				if (resp == null) 
					throw new InvalidParameterException("Received null due to InterruptedException while waiting for result.");
				
				@SuppressWarnings("unused")
				SubscribeResponse subscrResp = getSuccessfulResponse(resp); 				

			} catch (IllegalArgumentException | NullPointerException e) {
				throw new InvalidParameterException(e);
			} catch (IOException e) {
				throw new InvalidParameterException("IOException occurred while sending read message.", e);
			} catch (ClassCastException e) {
				throw new InvalidParameterException("Received response of unexpected type.", e);
			} catch (Exception ex) {
				throw new InvalidParameterException(ex);
			}	
		}
		
		@Override
		public void unsubscribe(String id) throws ParameterException {
			int msgId = MessageIdSeed.next();
			
			try {
				Unsubscribe msg = new Unsubscribe(new ParamId(id, true), msgId);
				Message resp = MessageClient.this.sendMessage(msg);				
				if (resp == null) 
					throw new InvalidParameterException("Received a null due to InterruptedException while waiting for result.");
				
				@SuppressWarnings("unused")
				UnsubscribeResponse unsubscrResp = getSuccessfulResponse(resp); 				

			} catch (IllegalArgumentException | NullPointerException e) {
				throw new InvalidParameterException(e);
			} catch (IOException e) {
				throw new InvalidParameterException("IOException occurred while sending read message.", e);
			} catch (ClassCastException e) {
				throw new InvalidParameterException("Received response of unexpected type.", e);
			} catch (Exception ex) {
				throw new InvalidParameterException(ex);
			}	
		}
	}	
	
	@SuppressWarnings("unchecked")
	private <T> T getSuccessfulResponse(Message msg) throws InvalidParameterException {
		
		if (msg instanceof ReadResponse) {
			ReadResponse resp = (ReadResponse)msg;
			if (resp.getStatus() != Status.SUCCESS) 
				throw new InvalidParameterException("" + resp.getStatus());
		}
		
		if (msg instanceof WriteResponse) {
			WriteResponse resp = (WriteResponse)msg;
			if (resp.getStatus() != Status.SUCCESS) 
				throw new InvalidParameterException("" + resp.getStatus());
		}
		
		if (msg instanceof SubscribeResponse) {
			SubscribeResponse resp = (SubscribeResponse)msg;
			if (resp.getStatus() != Status.SUCCESS) 
				throw new InvalidParameterException("" + resp.getStatus());
		}
		
		if (msg instanceof UnsubscribeResponse) {
			UnsubscribeResponse resp = (UnsubscribeResponse)msg;
			if (resp.getStatus() != Status.SUCCESS) 
				throw new InvalidParameterException("" + resp.getStatus());
		}
		
		if (msg instanceof CallResponse) {
			CallResponse resp = (CallResponse)msg;
			if (resp.getStatus() != Status.SUCCESS && resp.getStatus() != Status.APPLICATION_ERROR) 
				throw new InvalidParameterException("" + resp.getStatus());
		}
		
		return (T) msg;
	}
	
	class NotifyThread implements Runnable {

		private boolean running;
		
		@Override
		public void run() {
			
			running = true;
			int logCycle = 0;
			
			while (running) {
				
				try {
					socketLock.lock();
					while (running && (msgQueue.peek() == null || msgQueue.peek().getMessageHeader().getMessageType() != MessageType.NOTIFICATION)) {						
						if (++logCycle % 10 == 0) {					
							log.log(Level.FINEST, "Waiting for notification...");
							logCycle = 0;
						}
						messageReceived.await(RECV_INTERVAL, TimeUnit.MILLISECONDS);
						if (!running) break;
					}
					
					if (!running) {
						notifyThreadStopped.signalAll();
						break;
					}
					
					Notification ntf = (Notification)MessageClient.this.msgQueue.poll();
					
					if (ntf != null) 
						MessageClient.this.mHdl.notify(ntf.getUntypedMap());
									
					
				} catch(NullPointerException npe) { 
					
				} catch (InterruptedException e) {
					e.printStackTrace();
					running = false;
				}
				finally { socketLock.unlock(); }
			}			
		}
		
		public void stop() {
			this.running = false;
		}
		
	}
	
	class EventThread implements Runnable {

		private boolean running;
		
		@Override
		public void run() {
			
			running = true;
			int logCycle = 0;
			
			while (running) {
				
				try {
					socketLock.lock();
					while (running && (msgQueue.peek() == null || msgQueue.peek().getMessageHeader().getMessageType() != MessageType.EVENT)) {						
						if (++logCycle % 10 == 0) {					
							log.log(Level.FINEST, "Waiting for event...");
							logCycle = 0;
						}
						messageReceived.await(RECV_INTERVAL, TimeUnit.MILLISECONDS);
						if (!running) break;
					}
					
					if (!running) {
						eventThreadStopped.signalAll();
						break;
					}
					
					Event evnt = (Event)MessageClient.this.msgQueue.poll();
					
					if (evnt != null)
						MessageClient.this.mHdl.event(evnt.getEventTypeId().toString(), evnt.getParamId().toString(),
								evnt.getTimestamp(), evnt.getSeverity(), evnt.getMessage(), evnt.getUntypedParamMap());					
					
				} catch (NullPointerException npe) { 
					
				} catch (InterruptedException e) {
					e.printStackTrace();
					running = false;
				}
				finally { socketLock.unlock(); }
			}			
		}
		
		public void stop() {
			this.running = false;
		}
		
	}
	
	class ReceiverThread implements Runnable {

		private boolean running;
		private Throwable error;
		
		public ReceiverThread() {
			super();
		}

		@Override
		public void run() {
			running = true;
			int logCycle = 0;
			while (running) {
				try {
					socketLock.lock();
					
					ByteBuffer msgHeaderBuf = ByteBuffer.allocate(10);
					long bytesRead = client.read(msgHeaderBuf);
					if (bytesRead > 0) {
						
						if (log.isLoggable(Level.FINEST)) {
							byte[] bytes = new byte[10];
							msgHeaderBuf.flip();
							msgHeaderBuf.get(bytes);								
							log.log(Level.FINEST, "Read 10 bytes: {0}", bytesToHex(bytes));
						}
						msgHeaderBuf.flip();
						
						MessageHeader mh = new MessageHeader(msgHeaderBuf);
						log.log(Level.FINER, 
								"Received header for message of type {0}. Reading additional {1} bytes...", 
								new Object[] { mh.getMessageType(), mh.getMessageLength()-10 });						
						
						ByteBuffer msgBuf = ByteBuffer.allocate(mh.getMessageLength()-10);
						client.read(msgBuf);
						
						if (log.isLoggable(Level.FINEST)) {
							byte[] bytes = new byte[mh.getMessageLength()-10];
							msgBuf.flip();
							msgBuf.get(bytes);								
							log.log(Level.FINEST,"Read {0} bytes: {1}", new Object[] { bytes.length, bytesToHex(bytes) });
						}
						msgBuf.flip();
						
						Message msg = MessageDeserializer.deserialize(mh, msgBuf);
						log.log(Level.FINER,"Received message: {0}", new Object[] { msg });
						MessageClient.this.msgQueue.add(msg);
						messageReceived.signalAll();											
					}																	
				} 				
				catch (Exception e) {					
					running = false;
					error = e;										
					log.log(Level.FINER, e.getMessage(), e);
					e.printStackTrace();					
					break;
				}
				finally {
					socketLock.unlock();
				}
				
				if (!running) break;
				
				try {
					if (++logCycle % 10 == 0) {					
						log.log(Level.FINEST, "Waiting for 10 bytes...");
						logCycle = 0;
					}
					Thread.sleep(RECV_INTERVAL);
				} catch (InterruptedException e) {
					running = false;
				}
			}
			
			try { socketLock.lock(); receiverThreadStopped.signal(); }
			finally { socketLock.unlock(); }
			
		}

		public Throwable getError() {
			return error;
		}

		public void stop() {
			this.running = false;
		}	
	}
	
	public MessageClient(MessageHandler mHdl) {
		super();
		this.mHdl = mHdl;		
	}

	private Message sendMessage(Message msg) throws IOException, TimeoutException, InterruptedException {
		byte[] msgData = MessageSerializer.serialize(msg);
		try {
			socketLock.lock();
			client.write(ByteBuffer.wrap(msgData));			
		}
		finally {
			socketLock.unlock();
		}
		
		int retry = 0;
		int limit = RECV_RETRY_LIMIT;
		while (msgQueue.peek() == null || msgQueue.peek().getMessageHeader().getMessageId() != msg.getMessageHeader().getMessageId()) {
			try {
				socketLock.lock();				
				if (!messageReceived.await(RECV_TIMEOUT, TimeUnit.MILLISECONDS)) {					
					if (limit == 0) 
						log.log(Level.FINER, "Received no response after {0} ms. Retrying.", new Object[] { RECV_TIMEOUT });
					
					else if (retry++ < limit) 
						log.log(Level.FINER, "Received no response after {0} ms. Retry {1}/{2}.", new Object[] { RECV_TIMEOUT, retry, limit }); 
					
					else throw new TimeoutException("No response received after " + limit + " retries.");					
				}
			}
			finally {
				socketLock.unlock();
			}
		}
		return msgQueue.poll();
	}
	
	public void connect(String host, int port) throws IOException {
		client = SocketChannel.open(); 
		client.configureBlocking(false);
		client.connect(new InetSocketAddress(InetAddress.getByName(host), port > 0 ? port : OPCUA_PORT));
		
		while (!client.finishConnect()) {}
		
		this.notifyThread = new NotifyThread();
		new Thread(this.notifyThread, NotifyThread.class.getSimpleName()).start();
		
		this.eventThread = new EventThread();
		new Thread(this.eventThread, EventThread.class.getSimpleName()).start();
		
		this.receiverThread = new ReceiverThread();		
		new Thread(this.receiverThread, ReceiverThread.class.getSimpleName()).start();
		
		mHdl.open(new ClientDataProvider());
	}
	
	public void disconnect() throws IOException, InterruptedException {
		
		try {
			socketLock.lock();
			this.receiverThread.stop();
						
			if (!receiverThreadStopped.await(STOP_TIMEOUT, TimeUnit.MILLISECONDS))
				throw new InterruptedException("The receiver thread did not stop on time.");
		} finally { socketLock.unlock(); }
		
		try {
			socketLock.lock();
			this.notifyThread.stop();			
			if (!notifyThreadStopped.await(2000, TimeUnit.MILLISECONDS))
				throw new InterruptedException("The notify thread did not stop on time.");			
		} finally {
			socketLock.unlock();
		}
		
		try {
			socketLock.lock();
			this.eventThread.stop();			
			if (!eventThreadStopped.await(2000, TimeUnit.MILLISECONDS))
				throw new InterruptedException("The event thread did not stop on time.");			
		} finally {
			socketLock.unlock();
		}
		
		client.close();
		mHdl.close();
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
}
