package havis.opcua.message.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import havis.opcua.message.DataProvider;
import havis.opcua.message.MessageHandler;
import havis.opcua.message.common.MessageHandlerCommon;
import havis.opcua.message.exception.NoSuchParameterException;
import havis.opcua.message.exception.ParameterException;

public class MessageHandlerTest {
	
	static Map<String, Object> map =  new LinkedHashMap<>();	
	static {
		map.put("bool_true", true);
		map.put("bool_false", false);
		map.put("string", "foo bar");
		map.put("byte", (byte)0xaa);
		map.put("min_short", Short.MIN_VALUE);
		map.put("max_short", Short.MAX_VALUE);
		map.put("min_int", Integer.MIN_VALUE);
		map.put("max_int", Integer.MAX_VALUE);						
		map.put("min_long", Long.MIN_VALUE);
		map.put("max_long", Long.MAX_VALUE);
		map.put("min_float", Float.MIN_VALUE);
		map.put("max_float", Float.MAX_VALUE);
		map.put("min_double", Double.MIN_VALUE);
		map.put("max_double", Double.MAX_VALUE);
		
		map.put("booleans", new Boolean[] { true, false });
		map.put("bytes", new Byte[] { (byte)0xaa, (byte)0xbb });
		map.put("shorts", new Short[] { Short.MIN_VALUE, Short.MAX_VALUE });
		map.put("integers", new Integer[] { Integer.MIN_VALUE, Integer.MAX_VALUE });
		map.put("longs", new Long[] { Long.MIN_VALUE, Long.MAX_VALUE });
		map.put("floats", new Float[] { Float.MIN_VALUE, Float.MAX_VALUE });
		map.put("doubles", new Double[] { Double.MIN_VALUE, Double.MAX_VALUE });	
	}
	
	public static void main(String[] args) {
		
		try {
			MessageHandler mHdl = new MessageHandlerCommon();
			
			DataProvider dp = new TestDataProvider(mHdl);
			mHdl.open(dp);
			
			try(Scanner sc = new Scanner(System.in)) {
				System.out.println("Type 'q' to quit.");
				while (true) {
					
					System.out.print("msg> ");
					
					String line = sc.nextLine();				
					if (line.equals("q")) { 
						System.out.println("Stopping message handler...");
						break;
					}
					else if (line.equals("n"))
						mHdl.notify(map);
					
					else if (line.equals("e"))
						mHdl.event("#23", "#42", new Date(), 5, "This is a test", map);
				}
			}
			
			mHdl.close();			
			System.out.println("Message handler stopped.");

			
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private static class TestDataProvider implements DataProvider {

		private MessageHandler mHdl;
		private Map<String, Object> data;
		private Set<String> subscriptions;

		public TestDataProvider(MessageHandler mHdl) {
			super();
			this.mHdl = mHdl;
			this.data = new HashMap<>();
			this.subscriptions = new HashSet<>();
		}

		@Override
		public Object read(String param) throws ParameterException {
			
			if (param.startsWith("#"))
				param = param.substring(1);

			if (data.containsKey(param))
				return data.get(param);

			throw new NoSuchParameterException();
		}

		@Override
		public void subscribe(String param) throws ParameterException {
			if (param.startsWith("#"))
				param = param.substring(1);
			if (data.containsKey(param))
				subscriptions.add(param);
			else
				throw new NoSuchParameterException();

			System.out.println("Subscribed, subscriptions are: " + subscriptions);
		}

		@Override
		public void unsubscribe(String param) throws ParameterException {
			if (param.startsWith("#"))
				param = param.substring(1);
			if (subscriptions.contains(param))
				subscriptions.remove(param);
			else
				throw new NoSuchParameterException();

			System.out.println("Unsubscribed, subscriptions are: " + subscriptions);
		}

		@Override
		public void write(String param, Object value) throws ParameterException {
			if (param.startsWith("#"))
				param = param.substring(1);
			this.data.put(param, value);
			System.out.println("Written, data is now: " + data);

			if (this.subscriptions.contains(param) && data.containsKey(param)) {
				HashMap<String, Object> notifyMap = new HashMap<>();
				notifyMap.put(param, data.get(param));
				mHdl.notify(notifyMap);
			}
		}

		@Override
		public Object[] call(String methodId, String paramId, Object[] params) throws ParameterException {
			
			System.out.println("Method called. Method ID: " + methodId + ", Param ID: " + paramId);
			
			switch (methodId) {
				case "userAccount.Login":
					return new Object[] { true, "Welcome back!", 5 };
				
				case "userAccount.Logout":					
					return new Object[] { false, "So long!" };
					
				case "userAccount.SetEnabled":
					if ((boolean)params[0]) return new Object[] { true, "Account enabled." };
					else return new Object[] { false, "Account disabled." };
					
				case "userAccount.Add":					
					return params;
					
				case "rfidDevice.Scan":					
					HashMap<String, Object> scanResult = new HashMap<>();
					scanResult.put("@id", "scanResult");
					
					HashMap<String, Object> tag1 = new HashMap<>();
					tag1.put("@id", "tag1");
					tag1.put("epc","11111111");
					tag1.put("tid","11111111");
					tag1.put("killpwd",11111111);
					tag1.put("lockpwd",11111111);
					tag1.put("locked",false);
					tag1.put("killed",false);
					tag1.put("userdata", "11111111");
					tag1.put("antenna", (short)1);
					
					HashMap<String, Object> tag2 = new HashMap<>();
					tag2.put("@id", "tag2");
					tag2.put("epc","2222222");
					tag2.put("tid","2222222");
					tag2.put("killpwd",2222222);
					tag2.put("lockpwd",2222222);
					tag2.put("locked",true);
					tag2.put("killed",true);
					tag2.put("userdata", "2222222");
					tag2.put("antenna", (short)2);
					
					HashMap<String, Object> tag3 = new HashMap<>();
					tag3.put("@id", "tag3");
					tag3.put("epc","33333333");
					tag3.put("tid","33333333");
					tag3.put("killpwd",33333333);
					tag3.put("lockpwd",33333333);
					tag3.put("locked",true);
					tag3.put("killed",true);
					tag3.put("userdata", "33333333");
					tag3.put("antenna", (short)3);
					
					HashMap<String, Object> tag4 = new HashMap<>();
					tag4.put("@id", "tag4");
					tag4.put("epc","44444444");
					tag4.put("tid","44444444");
					tag4.put("killpwd",44444444);
					tag4.put("lockpwd",44444444);
					tag4.put("locked",true);
					tag4.put("killed",true);
					tag4.put("userdata", "44444444");
					tag4.put("antenna", (short)4);
					
					List<HashMap<String, Object>> tags = new ArrayList<>(); 
					short antMask = (short)params[0];
					
					if ( (antMask & 1) > 0 ) tags.add(tag1); 
					if ( (antMask & 2) > 0 ) tags.add(tag2);
					if ( (antMask & 4) > 0 ) tags.add(tag3);
					if ( (antMask & 8) > 0 ) tags.add(tag4);
					
					scanResult.put("TransponderList", tags.toArray(new HashMap[]{}));										
					return new Object[] { scanResult };
				
				case "rfidDevice.Kill":					
					@SuppressWarnings("unchecked")
					HashMap<String, Object> tag = (HashMap<String, Object>)params[0]; 					
					tag.put("killed", true);					
					return new Object[]{ tag };			
			}			
			return new Object[]{ };
		}
	}	
}
