package havis.opcua.message.common.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CallerCLI {
	private Caller caller;
	
	public static void main(String[] args) {
		try {			
			Caller caller = new Caller();
			
			String host = "127.0.0.1";
			int port = 4223;
			
			try {
				host = args[0];
				port = Integer.parseInt(args[1]);
				new InetSocketAddress(host, port);
			} 
			catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) { }
			catch (IllegalArgumentException ia) {
				System.err.println("Invalid port number: " + port);
				System.exit(1);
			}
			
			System.out.println("Connecting to socket: " + host + ":" + port);			
			caller.connect(host, port);
			new CallerCLI(caller).start();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(2);
		} 
	}
	
	public CallerCLI(Caller caller) {
		super();
		this.caller = caller;
	}
	
	private void printHelp() {
		System.out.println("Read Syntax:\n\tread: \"[variable]\"");
		System.out.println("\te.g. read: \"rfr310.DeviceStatus\"\n");
		
		System.out.println("Write Syntax:\n\twrite: \"[variable]\", \"[value]\"");
		System.out.println("\te.g. write: \"rfr310.DeviceName\", \"bar\"\n");
		
		System.out.println("Subscribe Syntax:\n\tsubscribe: \"[variable]\"");
		System.out.println("\te.g. subscribe: \"rfr310.LastScanData\"\n");
		
		System.out.println("Unsubscribe Syntax:\n\tunsubscribe: \"[variable]\"");
		System.out.println("\te.g. unsubscribe: \"rfr310.LastScanData\"\n");
		
		System.out.println("Call Syntax:\n\tcall:[method]: [arg1, arg2, arg3...]\n");
		
		System.out.println("Call Scan Syntax:\n\tcall:rfr310.Scan: [cycles], [duration], [dataAvailable]");
		System.out.println("\te.g. call:rfr310.Scan: 50, 10000, true\n");
		
		System.out.println("Call ScanStart Syntax:\n\tcall:rfr310.ScanStart: [cycles], [duration], [dataAvailable]");
		System.out.println("\te.g. call:rfr310.ScanStart: 50, 10000, true\n");
		
		System.out.println("Call ScanStop Syntax:\n\tcall:rfr310.ScanStop");
		System.out.println();
		
		System.out.println("Call ReadTag Syntax:\n\tcall:rfr310.ReadTag: [epcFilter], \"[codeType]\", [bank], [offset], [length], [accessPassword]");
		System.out.println("\te.g. call:rfr310.ReadTag: 300833b2ddd9014000000000, \"EPC\", 3, 0, 4, 00000000\n");
		
		System.out.println("Call WriteTag Syntax:\n\tcall:rfr310.WriteTag: [epcFilter], \"[codeType]\", [bank], [offset], [data], [accessPassword]");
		System.out.println("\te.g. call:rfr310.WriteTag: 300833b2ddd9014000000000, \"EPC\", 3, 0, aabbccdd, 00000000\n");
		
		System.out.println("Call SetTagPassword Syntax:\n\tcall:rfr310.SetTagPassword: [epcFilter], \"[codeType]\", [passwordType], [accessPassword], [newPassword]");
		System.out.println("\tpasswordType: 0=Access, 1=Kill, 2=Read, 3=Write");
		System.out.println("\te.g. call:rfr310.SetTagPassword: 300833b2ddd9014000000000, \"EPC\", 0, 00000000, 11223344\n");
		
		System.out.println("Call LockTag Syntax:\n\tcall:rfr310.LockTag: [epcFilter], \"[codeType]\", [accessPassword], [lockBank], [lockType], [offset], [length]");
		System.out.println("\tlockBank: 0=Kill, 1=Access, 2=EPC, 3=TID, 4=User");
		System.out.println("\tlockType: 0=Lock, 1=Unlock, 2=PermanentLock, 3=PermanentUnlock");
		System.out.println("\te.g. call:rfr310.LockTag: 300833b2ddd9014000000000, \"EPC\", 11223344, 1, 0, 0, 0\n");
		
		System.out.println("Call KillTag Syntax:\n\tcall:rfr310.KillTag: [epcFilter], \"[codeType]\", [killPassword]");
		System.out.println("\te.g. call:rfr310.KillTag: 300833b2ddd9014000000000, \"EPC\", 99887766\n");
	}
	
	public void start() throws IOException, InterruptedException {
		boolean running = true;
		try (Scanner sc = new Scanner(System.in)) {
			System.out.println("Type 'q' to quit. Type '?' for help.");
			
			while (running) {
				System.out.print("client>");
				String cmd = sc.nextLine();				
				cmd = cmd.replaceAll("\\s+", "");
				
				if (0 == cmd.length()) continue;
				
				if (cmd.equals("q")) {
					System.out.println("Disconnecting client...");
					caller.disconnect();
					System.out.println("Client disconnected.");
					running = false;
				}
				
				else if (cmd.equals("?")) {
					printHelp();
				}
				
				else if (cmd.startsWith("read:")) {					
					Pattern regex = Pattern.compile("^read:\"([\\w.]+)\"$"); 
					Matcher matcher = regex.matcher(cmd);					
					if (!matcher.find()) System.err.println("Read: Syntax error.");
					else {
						try {
							System.out.println(caller.read(matcher.group(1)));
						} catch (Exception e) {
							System.err.println(e);
						}
					}
				}
				
				else if (cmd.startsWith("write:")) {					
					Pattern regex = Pattern.compile("^write:\"([\\w.]+)\",\"([\\w.]+)\"$"); 
					Matcher matcher = regex.matcher(cmd);					
					if (!matcher.find()) System.err.println("Write: Syntax error.");
					
					else {
						try {
							caller.write(matcher.group(1), matcher.group(2));
							System.out.println("SUCCESS");
						} catch (Exception e) {
							System.err.println(e);
						}
					}
				}
				
				else if (cmd.startsWith("subscribe:")) {
					Pattern regex = Pattern.compile("^subscribe:\"([\\w.#]+)\"$"); 
					Matcher matcher = regex.matcher(cmd);					
					if (!matcher.find()) System.err.println("Subscribe: Syntax error.");
					else {
						try {
							caller.subscribe(matcher.group(1));
							System.out.println("SUCCESS");
						} catch (Exception e) {
							System.err.println(e);
						}
					}
				}
				
				else if (cmd.startsWith("unsubscribe:")) {
					Pattern regex = Pattern.compile("^unsubscribe:\"([\\w.#]+)\"$"); 
					Matcher matcher = regex.matcher(cmd);					
					if (!matcher.find()) System.err.println("Unsubscribe: Syntax error.");
					else {
						try {
							caller.unsubscribe(matcher.group(1));
							System.out.println("SUCCESS");
						} catch (Exception e) {
							System.err.println(e);
						}
					}
				}
				
				else if (cmd.startsWith("call:rfr310.ScanStart")) {
					Pattern regex = Pattern.compile("^call:rfr310\\.ScanStart:(\\d+),(\\d+),(true|false)$"); 
					Matcher matcher = regex.matcher(cmd);					
					if (!matcher.find()) System.err.println("ScanStart: Syntax error.");
					
					else {
						try {
							Map<String, Object> scanSettings = caller.buildScanSettings(
								/* cycles */ Integer.parseInt(matcher.group(1)), 
								/* duration */ Integer.parseInt(matcher.group(2)), 
								/* dataAvailable */ Boolean.parseBoolean(matcher.group(3)));
						
							Object[] result = caller.callScanStart(scanSettings);
							System.out.println(caller.aimStatusCodeToString((int)result[0]));
													
						} catch (Exception e) {
							System.err.println(e);
						}
					}
				}
				
				else if (cmd.startsWith("call:rfr310.ScanStop")) {
					Pattern regex = Pattern.compile("^call:rfr310\\.ScanStop$"); 
					Matcher matcher = regex.matcher(cmd);					
					if (!matcher.find()) System.err.println("ScanStop: Syntax error.");
					
					else {
						try {
							caller.callScanStop();
							System.out.println("SUCCESS");													
						}
						catch (Exception e) {
							System.err.println(e);
						}
					}
				}
				
				else if (cmd.startsWith("call:rfr310.Scan")) {
					Pattern regex = Pattern.compile("^call:rfr310\\.Scan:(\\d+),(\\d+),(true|false)$"); 
					Matcher matcher = regex.matcher(cmd);					
					if (!matcher.find()) System.err.println("Scan: Syntax error.");
					
					else {
						try {
							Map<String, Object> scanSettings = caller.buildScanSettings(
								/* cycles */ Integer.parseInt(matcher.group(1)), 
								/* duration */ Integer.parseInt(matcher.group(2)), 
								/* dataAvailable */ Boolean.parseBoolean(matcher.group(3)));
						
							Object[] result = caller.callScan(scanSettings);
							
							int status = (int)result[1];
							if (status == 0) {
									System.out.println("SUCCESS");
									
									Object[] tags = (Object[])result[0];
									for (Object tagObj : tags) {
										try {
											@SuppressWarnings("unchecked")
											Map<String, Object> tagMap = (Map<String, Object>) tagObj;
											@SuppressWarnings("unchecked")
											Map<String, Object> scDataMap = (Map<String, Object>) tagMap.get("ScanData");
											@SuppressWarnings("unchecked")
											Map<String, Object> epcMap = (Map<String, Object>) scDataMap.get("Epc");
											Byte[] epc = (Byte[]) epcMap.get("UId");
											System.out.println(bytesToHex(epc));
										} catch (Exception e) { System.out.println(tagObj); }
									}
							} 
							else System.out.println(caller.aimStatusCodeToString(status));

													
						} catch (Exception e) {
							System.err.println(e);
						}
					}					
				}
				
				else if (cmd.startsWith("call:rfr310.ReadTag")) {					
					Pattern regex = Pattern.compile("^call:rfr310\\.ReadTag:(([a-f0-9]{2})+),\"(\\w+)\",(\\d+),(\\d+),(\\d+),(([a-f0-9]{2})+)$"); 
					Matcher matcher = regex.matcher(cmd);
					
					if (!matcher.find()) System.err.println("ReadTag: Syntax error.");
					else {
						try {
							Map<String, Object> scanData = caller.buildScanData(hexToBytes(matcher.group(1)));
						
							Object[] readResult = 							
								caller.callReadTag(
									scanData, 
									/* codeType */ matcher.group(3), 
									/* region */ Integer.parseInt(matcher.group(4)), 
									/* offset */ Long.parseLong(matcher.group(5)), 
									/* length */ Long.parseLong(matcher.group(6)), 
									/* password */ hexToBytes(matcher.group(7)));
							if (readResult.length == 2) {
								System.out.println(caller.aimStatusCodeToString((int)readResult[1]));
								System.out.println(bytesToHex((Byte[])readResult[0]));								
							}
							else System.out.println(caller.aimStatusCodeToString((int)readResult[0]));
						
						}
						catch (Exception e) {
							System.err.println(e);
						}
					};									
				}
				
				else if (cmd.startsWith("call:rfr310.WriteTag")) {					
					Pattern regex = Pattern.compile("^call:rfr310\\.WriteTag:(([a-f0-9]{2})+),\"(\\w+)\",(\\d+),(\\d+),(([a-f0-9]{2})+),(([a-f0-9]{2})+)$"); 
					Matcher matcher = regex.matcher(cmd);
					
					if (!matcher.find()) System.err.println("WriteTag: Syntax error.");
					else {
						try {
							Map<String, Object> scanData = caller.buildScanData(hexToBytes(matcher.group(1)));
						
							Object writeResult = 							
								caller.callWriteTag(
									scanData, 
									/* codeType */ matcher.group(3), 
									/* region */ Integer.parseInt(matcher.group(4)), 
									/* offset */ Long.parseLong(matcher.group(5)), 
									/* data */ hexToBytes(matcher.group(6)), 
									/* password */ hexToBytes(matcher.group(8)));
							System.out.println(caller.aimStatusCodeToString((int)writeResult));
						
						}
						catch (Exception e) {
							System.err.println(e);
						}
					};									
				}
				
				else if (cmd.startsWith("call:rfr310.SetTagPassword")) {
					Pattern regex = Pattern.compile("^call:rfr310\\.SetTagPassword:(([a-f0-9]{2})+),\"(\\w+)\",(\\d+),(([a-f0-9]{2})+),(([a-f0-9]{2})+)$"); 
					Matcher matcher = regex.matcher(cmd);
					
					if (!matcher.find()) System.err.println("SetTagPassword: Syntax error.");
					else {
						try {
							Map<String, Object> scanData = caller.buildScanData(hexToBytes(matcher.group(1)));
						
							Object setPswResult = 							
								caller.callSetTagPassword(
									scanData, 
									/* codeType */ matcher.group(3), 
									/* passwordType */ Integer.parseInt(matcher.group(4)), 
									/* accessPassword */hexToBytes(matcher.group(5)), 
									/* newPasword */ hexToBytes(matcher.group(7)));
							System.out.println(caller.aimStatusCodeToString((int)setPswResult));
						
						}
						catch (Exception e) {
							System.err.println(e);
						}
					};									
				}
				
				else if (cmd.startsWith("call:rfr310.LockTag")) {
					Pattern regex = Pattern.compile("^call:rfr310\\.LockTag:(([a-f0-9]{2})+),\"(\\w+)\",(([a-f0-9]{2})+),(\\d+),(\\d+),(\\d+),(\\d+)$"); 
					Matcher matcher = regex.matcher(cmd);
					
					if (!matcher.find()) System.err.println("LockTag: Syntax error.");
					else {
						try {
							Map<String, Object> scanData = caller.buildScanData(hexToBytes(matcher.group(1)));
						
							Object lockResult = 							
								caller.callLockTag (
									scanData, 
									/* codeType */ matcher.group(3), 
									/* accessPassword */ hexToBytes(matcher.group(4)),
									/* lockRegion */ Integer.parseInt(matcher.group(6)), 
									/* lockOperation */ Integer.parseInt(matcher.group(7)),
									/* offset */ Long.parseLong(matcher.group(8)),
									/* length */ Long.parseLong(matcher.group(9)));
							System.out.println(caller.aimStatusCodeToString((int)lockResult));
						
						}
						catch (Exception e) {
							System.err.println(e);
						}
					};									
				}
				
				else if (cmd.startsWith("call:rfr310.KillTag")) {
					Pattern regex = Pattern.compile("^call:rfr310\\.KillTag:(([a-f0-9]{2})+),\"(\\w+)\",(([a-f0-9]{2})+)$"); 
					Matcher matcher = regex.matcher(cmd);
					
					if (!matcher.find()) System.err.println("KillTag: Syntax error.");
					else {
						try {
							Map<String, Object> scanData = caller.buildScanData(hexToBytes(matcher.group(1)));
						
							Object killResult = 							
								caller.callKillTag (
									scanData, 
									/* codeType */ matcher.group(3), 
									/* killPassword */ hexToBytes(matcher.group(4)));
							System.out.println(caller.aimStatusCodeToString((int)killResult));
						
						}
						catch (Exception e) {
							System.err.println(e);
						}
					};									
				}
				
				else System.err.println("Unknown command: " + cmd);
			}
		}
	}

	private static String bytesToHex(Byte[] bytes) {
		if (bytes == null)
			return null;

		char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		
		StringBuffer sb = new StringBuffer();
		
		for (int iByte = 0; iByte < bytes.length; iByte++) {
			byte b = bytes[iByte];
			int b0 = (b & 0xf0) >> 4;
			int b1 = b & 0x0f;

			sb.append(hexChars[b0]);
			sb.append(hexChars[b1]);						
			//sb.append(" ");
		}
				
		return sb.toString();
	}
	
	private static Byte[] hexToBytes(String hexStr) throws IllegalArgumentException {
		hexStr = hexStr.replaceAll("\\s|_", "");
		if (hexStr.length() % 2 != 0)
			throw new IllegalArgumentException(
					"Hex string must have an even number of characters.");

		Byte[] result = new Byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length(); i += 2)
			result[i / 2] = Integer.decode(
					"0x" + hexStr.charAt(i) + hexStr.charAt(i + 1)).byteValue();

		return result;
	}
	
}
