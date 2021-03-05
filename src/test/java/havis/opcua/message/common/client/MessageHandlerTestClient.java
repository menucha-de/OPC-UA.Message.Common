package havis.opcua.message.common.client;

import havis.opcua.message.common.model.Message;
import havis.opcua.message.common.model.MessageIdSeed;
import havis.opcua.message.common.model.ParamId;
import havis.opcua.message.common.model.ParamValue;
import havis.opcua.message.common.model.Read;
import havis.opcua.message.common.model.Subscribe;
import havis.opcua.message.common.model.Unsubscribe;
import havis.opcua.message.common.model.Write;
import havis.opcua.message.common.serialize.MessageDeserializer;
import havis.opcua.message.common.serialize.MessageSerializer;
import havis.opcua.message.common.server.MessageServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MessageHandlerTestClient {
	
	private static Map<String, byte[]> msgMap;
	
	static {
		msgMap = new HashMap<>();
		
		Write w1 = new Write(new ParamId(-1, "é"), new ParamValue("é"), MessageIdSeed.next());
		Write w2 = new Write(new ParamId(-1, 42), new ParamValue(23), MessageIdSeed.next());
		Write w3 = new Write(new ParamId(-1, "foo"), new ParamValue("bas"), MessageIdSeed.next());
		
		msgMap.put("w1", MessageSerializer.serialize(w1));
		msgMap.put("w2", MessageSerializer.serialize(w2));
		msgMap.put("w3", MessageSerializer.serialize(w3));
		
		Read r1 = new Read(new ParamId(-1, "foo"), MessageIdSeed.next()); 
		Read r2 = new Read(new ParamId(-1, 42), MessageIdSeed.next());
		
		msgMap.put("r1", MessageSerializer.serialize(r1));
		msgMap.put("r2", MessageSerializer.serialize(r2));
		
		Subscribe s1 = new Subscribe(new ParamId(-1, "foo"), MessageIdSeed.next());
		Subscribe s2 = new Subscribe(new ParamId(-1, 42), MessageIdSeed.next());
		
		msgMap.put("s1", MessageSerializer.serialize(s1));
		msgMap.put("s2", MessageSerializer.serialize(s2));
		
		Unsubscribe u1 = new Unsubscribe(new ParamId(-1, "foo"), MessageIdSeed.next());
		Unsubscribe u2 = new Unsubscribe(new ParamId(-1, 42), MessageIdSeed.next());
		
		msgMap.put("u1", MessageSerializer.serialize(u1));
		msgMap.put("u2", MessageSerializer.serialize(u2));
		
		/* incomplete msg header */
		msgMap.put("e1", new byte[] {
				0x00, 0x02, /* message type: write */ 			
				0x00, 0x00, 0x00, 0x19, /* message len: 25 bytes */  			
				0x00, 0x00, 0x00//, 0x01, /* message id: 0x1 */ 				
		});
		
		/* incomplete msg body */
		msgMap.put("e2", new byte[] {
				0x00, 0x02, /* message type: write */ 			
				0x00, 0x00, 0x00, 0x19, /* message len: 25 bytes */  			
				0x00, 0x00, 0x00, 0x01, /* message id: 0x1 */				
				0x01 /* param id type: alphanum */  
			});		
	}
		
	public static void main(String[] args) throws IOException {
		/* Client socket */
		SocketChannel client = SocketChannel.open();
		client.configureBlocking(false);
        client.connect(new InetSocketAddress("127.0.0.1", MessageServer.PORT));
        
        while (!client.finishConnect()) {}
        
        try(Scanner sc = new Scanner(System.in)) {
			System.out.println("Type 'q' to quit.");
			while (true) {
				System.out.print("client>");
				String line = sc.nextLine();				
				if (line.equals("q")) { 
					System.out.println("Disconnecting client...");
					client.close();
					System.out.println("Client disconnected.");
					break;
				} 
				else if (line.equals("<")) {
					ByteBuffer dst = ByteBuffer.allocate(1024);
					int bytesRead = 0;
					String msg = "";
					
					if((bytesRead = client.read(dst)) > 0) {
						System.out.println("RECV: " + bytesRead + " bytes");
						
						byte[] bytes = new byte[bytesRead];
						dst.flip();
						dst.get(bytes);		
						msg = bytesToHex(bytes);
						System.out.println("RECV: " + msg);
						
						dst.flip();
						
						while (dst.hasRemaining()) {
							Message m = MessageDeserializer.deserialize(dst);
							System.out.println("RECV: " + m);
						}
						
						dst.clear();						
					}
					
				}
				else { 
					byte[] data = msgMap.get(line);
					if (data == null) {
						System.out.println("No message with key '" + line + "' found.");
						continue;
					}
					System.out.println("SEND: " + bytesToHex(data));
					client.write(ByteBuffer.wrap(data));
				}
			}
		}	
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
