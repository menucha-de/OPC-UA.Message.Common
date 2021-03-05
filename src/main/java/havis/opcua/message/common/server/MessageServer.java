package havis.opcua.message.common.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import havis.opcua.message.common.model.MessageHeader;

public class MessageServer implements Callable<Object> {

	public static final int PORT = 4223;
	public static final int RECV_TIMEOUT_MS = 500;
	private int port;
	private Selector selector;
	private SocketChannel csc;
	private boolean serverRunning;
	private ServerSocketChannel ssc;
	private BlockingQueue<ByteBuffer> pendingWriteBuffers;
	private BlockingQueue<ByteBuffer> pendingReadBuffers;
	private MessageListener messageListener;
	private ExecutorService threadPool;
	private IncomingMessageProcessor msgProcessor;
	
	public MessageServer() {
		this(PORT, RECV_TIMEOUT_MS);
	}
	
	public MessageServer(int port) {
		this(port, RECV_TIMEOUT_MS);
	}
	
	public MessageServer(int port, int recvTimeout) {
		this.port = port;
		this.pendingWriteBuffers = new LinkedBlockingQueue<>();
		this.pendingReadBuffers = new LinkedBlockingQueue<>();
	}

	public MessageListener getMessageListener() {
		return messageListener;
	}

	public void setMessageListener(MessageListener messageListener) {
		this.messageListener = messageListener;
	}
	
	@Override
	public Object call() throws Exception {
		if (this.ssc == null)
			throw new IllegalStateException("No server socket connection found. Please call connect() first.");

		/*
		 * set server to be running (i.e. loop termination condition to false)
		 */
		this.serverRunning = true;

		/* as long as server is running... */
		while (serverRunning) {
			if (!pendingWriteBuffers.isEmpty()) {
 				SelectionKey selKey = csc.keyFor(selector);
 				selKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
 			}
			
			/* select the channel keys (this will wait for new connections) */
 			selector.select();

			/* get the key iterator */
			Iterator<SelectionKey> iSelKey = selector.selectedKeys().iterator();

			/* iterate through selection keys */
			while (iSelKey.hasNext()) {

				/* get next selection key and remove it from iterator */
				SelectionKey selKey = iSelKey.next();
				iSelKey.remove();

				/*
				 * if key's channel is ready to accept connection establish
				 * connection and get client channel
				 */
				if (selKey.isAcceptable()) {
					/* accept connection if no connection exists yet */
					if (csc == null || !csc.isConnected()) 
						this.csc = accept();
					
					/* reject connection if a client socket already exists */
					else {
						ssc.accept().close();
						
						if (this.messageListener != null) 
							this.messageListener.rejected(this.csc.socket().getRemoteSocketAddress());
						continue;
					}
				}
				
				/* if client channel is readable, receive from client */
				if (selKey.isValid() && selKey.isReadable()) {
					ByteBuffer buffer = ByteBuffer.allocate(1024);
					
					if (csc.read(buffer) < 0) {
						disconnectClientSocket();
						break;
					}
					
					buffer.flip();
					pendingReadBuffers.add(buffer);
				}
				
				/* if client channel is writable, send pending write buffers */
				if (selKey.isValid() && selKey.isWritable()) {
					ByteBuffer buffer = pendingWriteBuffers.peek();
					if (buffer != null) {
						csc.write(buffer);
						if(!buffer.hasRemaining())
							pendingWriteBuffers.remove();
					}										
					selKey.interestOps(SelectionKey.OP_READ);					
				}
			}
		}
		
		return null;
	}
	
	public void submit(ByteBuffer bb) throws IOException {
		if (csc == null || !this.serverRunning) return;		
		this.pendingWriteBuffers.add(bb);
		selector.wakeup();
	}
	
	private SocketChannel accept() throws IOException {

		/* get the client socket channel CSC */
		SocketChannel csc = ssc.accept();

		/* set CSC to be unblocking */
		csc.configureBlocking(false);

		/* register CSC for reading */
		csc.register( selector, SelectionKey.OP_READ);

		if (this.messageListener != null)
			this.messageListener.accepted(csc.socket().getRemoteSocketAddress());

		this.msgProcessor = new IncomingMessageProcessor();
		this.msgProcessor.start();
		
		return csc;
	}
	
	private void disconnectClientSocket() {

		try {
			this.msgProcessor.running = false;
			this.msgProcessor.join();
			this.pendingReadBuffers.clear();
			this.pendingWriteBuffers.clear();
			csc.close();

			if (messageListener != null)
				this.messageListener.disconnected(csc.socket().getRemoteSocketAddress());

			csc = null;
		} catch (Exception e) {

		}
	}

	private void disconnectServerSocket() {
		try {
			this.ssc.close();
		} catch (Exception e) {
		}
		this.ssc = null;

		try {
			this.selector.close();
		} catch (Exception e) {
		}
		this.selector = null;
	}
	
	public void start() throws Exception {
		try {
			/* get the selector */
			selector = SelectorProvider.provider().openSelector();

			/* open the server socket channel (SSC) */
			this.ssc = ServerSocketChannel.open();

			/* set SSC to be unblocking */
			this.ssc.configureBlocking(false);

			/*
			 * bind the SSC to the given port (an IOException will be thrown here,
			 * if unsuccessful)
			 */
			this.ssc.socket().bind(new InetSocketAddress("0.0.0.0", this.port));

			/* register SSC for accepting connections */
			this.ssc.register(selector, SelectionKey.OP_ACCEPT);
			
			threadPool = Executors.newSingleThreadExecutor();
			Future<Object> result = threadPool.submit(this);

			try {
				result.get(100, TimeUnit.MILLISECONDS);
			} catch (TimeoutException e) {
				/* OK, not error after 100ms, server seems to be running */
			} catch (Exception e) { // exception thrown during call()
				throw e;
			}
			
		} catch (Exception e) {
			if (threadPool != null) {
				threadPool.shutdown();
				threadPool = null;
			}

			throw e;
		}
	}

	public void stop() throws Exception {
		try {
			
			disconnectClientSocket();
			disconnectServerSocket();

			this.serverRunning = false;
			
		} finally {
			if (threadPool != null) {
				threadPool.shutdown();
				threadPool = null;
			}
		}
	}

	private class IncomingMessageProcessor extends Thread {

		private ByteBuffer msgHeaderBuffer;
		private ByteBuffer msgBodyBuffer;
		
		private boolean running;
		
		IncomingMessageProcessor() {
			super(IncomingMessageProcessor.class.getCanonicalName());
		}
		
		@Override
		public void run() {			
			
			running = true;
			
			msgHeaderBuffer = ByteBuffer.allocate(MessageHeader.BYTE_COUNT);
			ByteBuffer src = null;
			ByteBuffer dst = msgHeaderBuffer;
			int limit = MessageHeader.BYTE_COUNT;
			
			MessageHeader msgHeader = null;
			
			while (running) {
				try {
					if (dst.position() < limit) {					
						if (src != null && src.hasRemaining())
							dst.put(src.get());
						else {
							ByteBuffer bb = null;
							/* while running and no new byte buffer becomes available in 500 ms */
							while ( running && (bb = MessageServer.this.pendingReadBuffers.poll(500, TimeUnit.MILLISECONDS)) == null );							
							
							if (!running) break;
							src = bb; 
						}
					} 
					else {
						if (dst == msgHeaderBuffer) {
							dst.flip();							
							msgHeader = new MessageHeader(dst);												
							limit = msgHeader.getMessageLength() - MessageHeader.BYTE_COUNT;
							dst = msgBodyBuffer = ByteBuffer.allocate(limit);						
						}
						else {						
							msgBodyBuffer.flip();						
							MessageServer.this.messageListener.received(msgHeader, msgBodyBuffer);
							
							msgHeaderBuffer.clear();
							dst = msgHeaderBuffer;
							limit = MessageHeader.BYTE_COUNT;						
						}
					}				
				} catch (IllegalArgumentException | InterruptedException e) {
					msgHeaderBuffer.clear();
					dst = msgHeaderBuffer;
					limit = MessageHeader.BYTE_COUNT;					
					MessageServer.this.messageListener.errorOccurred(e);
				}
			}
		}
	}
}
