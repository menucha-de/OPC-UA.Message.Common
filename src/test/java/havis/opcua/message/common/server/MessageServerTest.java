package havis.opcua.message.common.server;

import havis.opcua.message.common.server.MessageServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.junit.Test;

public class MessageServerTest {
	
	byte[][] messages = new byte[][] {
    	new byte[] {
			0x00, 0x00, /* message type: read */
			0x00, 0x00, 0x00, 0x0F, /* message len: 15 bytes */
    		(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, /* message id: 0xffffffff */
			0x00, /* param id type: num */
			(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF /* param id val: 0xffffffff */
		}, 
    	new byte[] {
			0x00, 0x01, /* message type: read_resp */ 			
			0x00, 0x00, 0x00, 0x1B, /* message len: 27 bytes */  			
			(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, /* message id: 0xffffffff */  
			0x00, 0x00, /* status: SUCCESS */  
			0x01, /* param id type: alphanum */  
			0x00, 0x03, /* param id len: 3 */  
			0x66, 0x6F, 0x6F, /* param id str: "foo" */  
			0x00, 0x08, /* param type: array */  
			0x00, 0x01, /* array type: char */  
			0x00, 0x03, /* elem count: 3 */  
			0x62, 0x61, 0x72 /* payload: "bar" */ 
		},
		new byte[] {
			0x00, 0x02, /* message type: write */ 			
			0x00, 0x00, 0x00, 0x19, /* message len: 25 bytes */  			
			(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, /* message id: 0xffffffff */  
			0x01, /* param id type: alphanum */  
			0x00, 0x03, /* param id len: 3 */  
			0x66, 0x6F, 0x6F, /* param id str: "foo" */  
			0x00, 0x08, /* param type: array */  
			0x00, 0x01, /* array type: char */  
			0x00, 0x03, /* elem count: 3 */  
			0x62, 0x61, 0x72 /* payload: "bar" */ 
		},
		new byte[] {
			0x00, 0x03, /* message type: write_resp */ 			
			0x00, 0x00, 0x00, 0x0c, /* message len: 12 bytes */  			
			(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, /* message id: 0xffffffff */  
			0x00, 0x00 /* status: SUCCESS */  					
		},
		new byte[] {					
			0x00, 0x04, /* message type: subscribe */
			0x00, 0x00, 0x00, 0x0F, /* message len: 15 bytes */ 
			(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, /* message id: 0xffffffff */
			0x00, /* param id type: num */
			(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF /* param id val: 0xffffffff */
		},
		new byte[] {
			0x00, 0x05, /* message type: subsc_resp */ 			
			0x00, 0x00, 0x00, 0x0c, /* message len: 12 bytes */  			
			(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, /* message id: 0xffffffff */  
			0x00, 0x00 /* status: SUCCESS */  					
		},
		new byte[] {					
			0x00, 0x06, /* message type: unsubscribe */
			0x00, 0x00, 0x00, 0x0F, /* message len: 15 bytes */ 
			(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, /* message id: 0xffffffff */
			0x00, /* param id type: num */
			(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF /* param id val: 0xffffffff */
		},
		new byte[] {
			0x00, 0x07, /* message type: subsc_resp */ 			
			0x00, 0x00, 0x00, 0x0c, /* message len: 12 bytes */  			
			(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, /* message id: 0xffffffff */  
			0x00, 0x00 /* status: SUCCESS */  					
		},
		new byte[] {
			0x00, 0x08, //msg type: notification
			0x00, 0x00, 0x00, 0x33, //msg len: 51 bytes 
			(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, //msg id: 0xffffffff
			
			0x00, 0x03, //num of params: 3
			
			/* 1st paramId */ 
			0x01, // type: alphanum
			0x00, 0x03, // len: 3
			0x66, 0x6F, 0x6F, // val: "foo"
			
			/* 1st paramVal */
			0x00, 0x08, // data type: array
			0x00, 0x01, // array type: char
			0x00, 0x03, // elem count: 3
			0x62, 0x61, 0x72, // val: "bar" 
			
			/* 2nd paramId */
			0x00, // type: num
			0x00, 0x00, 0x00, 0x17, //val: 23
			
			/* 2nd paramVal */
			0x00, 0x03, // type: short
			0x00, 0x2A, // val: 42
			
			/* 3rd paramId */
			0x01, // type: alphanum
			0x00, 0x02, // len: 2
			0x70, 0x69, // val: "pi"
			
			/* 3rd paramVal */
			0x00, 0x07, // type: double
			0x40, 0x09, 0x21, (byte)0xFB, 0x54, 0x44, 0x2D, 0x18 // val: 3.14....
		}
    };
	
	@Test
	public void testRecv() throws Throwable {
		
		/* Client socket */
		SocketChannel client = SocketChannel.open();
		client.configureBlocking(true);
        client.connect(new InetSocketAddress("localhost", MessageServer.PORT));
        
        while (!client.finishConnect()) {
        	System.out.println("still connecting");
        }
        
		for (int i = 0; i < messages.length; i++)
		{			
	        byte[] message = messages[i];					        
	        ByteBuffer bb = ByteBuffer.wrap(message);
	        while (bb.hasRemaining()) client.write(bb);	        
		}
		
		client.close();
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
