package havis.opcua.message.common.client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import havis.opcua.message.DataProvider;
import havis.opcua.message.MessageHandler;
import havis.opcua.message.common.client.MessageClient;
import havis.opcua.message.exception.ParameterException;

public class Caller implements MessageHandler {
	private static final Logger log = Logger.getLogger(Caller.class.getName());
	
	private DataProvider provider;
	private MessageClient msgClient;
	private List<Map<String, Object>> notifications;
	
	public static final int PASSWORD_TYPE_ACCESS = 0;
	public static final int PASSWORD_TYPE_KILL = 1;
	public static final int PASSWORD_TYPE_READ = 2;
	public static final int PASSWORD_TYPE_WRITE = 3;

	public static final int LOCK_REGION_KILL = 0;
	public static final int LOCK_REGION_ACCESS = 1;
	public static final int LOCK_REGION_EPC = 2;
	public static final int LOCK_REGION_TID = 3;
	public static final int LOCK_REGION_USER = 4;
	
	public static final int LOCK_OPERATION_LOCK = 0;
	public static final int LOCK_OPERATION_UNLOCK = 1;
	public static final int LOCK_OPERATION_PERMANENTLOCK = 2;
	public static final int LOCK_OPERATION_PERMANENTUNLOCK = 3;
	
	public Caller() throws IOException, InterruptedException {
		this.msgClient = new MessageClient(this);		
	}
	
	public Object read(String id) throws ParameterException {
		return this.provider.read(id);
	}
	
	public void write (String id, Object value) throws ParameterException {
		this.provider.write(id, value);
	}
	
	public void subscribe(String id) throws ParameterException {
		this.provider.subscribe(id);
	}
	
	public void unsubscribe(String id) throws ParameterException {
		this.provider.unsubscribe(id);
	}
	
	public Object[] callScanStart(Map<String, Object> scanSettings) throws ParameterException {
		return provider.call("rfr310.ScanStart", "rfr310", new Object[] { scanSettings });
	}
	
	public Object[] callScanStop() throws ParameterException {
		return provider.call("rfr310.ScanStop", "rfr310", new Object[] { });
	}
	
	public Object[] callScan(Map<String, Object> scanSettings) throws ParameterException {
		return provider.call("rfr310.Scan", "rfr310", new Object[] { scanSettings });
	}	
	
	public Object[] callReadTag(Map<String, Object> scanData, String codeType, int region, long offset, long length, Byte[] password) throws ParameterException {
		Object res = provider.call("rfr310.ReadTag", "rfr310", new Object[] { scanData, codeType, region, offset, length, password });		
		if (res instanceof Object[]) return (Object[]) res;
		else return new Object[] { res };
	}	
	
	public Object callWriteTag(Map<String, Object> scanData, String codeType, int region, long offset, Byte[] data, Byte[] password) throws ParameterException {
		return provider.call("rfr310.WriteTag", "rfr310", new Object[] { scanData, codeType, region, offset, data, password })[0];
	}
	
	public Object callSetTagPassword(Map<String, Object> scanData, String codeType, int passwordType, Byte[] accessPassword, Byte[] newPassword) throws ParameterException {
		return provider.call("rfr310.SetTagPassword", "rfr310", new Object[] { scanData, codeType, passwordType, accessPassword, newPassword })[0];
	}

	public Object callLockTag(Map<String, Object> scanData, String codeType, Byte[] password, int lockRegion, int lockOperation, long offset, long length) throws ParameterException {
		return provider.call("rfr310.LockTag", "rfr310", new Object[] { scanData, codeType, password, lockRegion, lockOperation, offset, length })[0];
	}
	
	public Object callKillTag(Map<String, Object> scanData, String codeType, Byte[] killPassword) throws ParameterException {
		return provider.call("rfr310.KillTag", "rfr310", new Object[] { scanData, codeType, killPassword })[0];
	}
	
	public DataProvider getProvider() {
		return provider;
	}
	
	public Map<String, Object> buildScanSettings(int cyclces, int duration, boolean dataAvailable) {
		Map<String, Object> scanSettings = new HashMap<>();
		scanSettings.put("@id", "ScanSettings");
		scanSettings.put("Cycles",new Integer(cyclces));
		scanSettings.put("Duration",new Double(duration));
		scanSettings.put("DataAvailable",new Boolean(dataAvailable));
		return scanSettings;		
	}
	
	public Map<String, Object> buildScanData(Byte[] epc) {
		Map<String, Object> scanDataMap = new HashMap<>();
		Map<String, Object> scanDataMapEpc = new HashMap<>();
		
		scanDataMapEpc.put("@id", "ScanDataMapEpc");
		scanDataMapEpc.put("PC", 0);
		scanDataMapEpc.put("UId", epc);
		
		scanDataMap.put("@id", "ScanDataMap");
		scanDataMap.put("Epc", scanDataMapEpc);

		return scanDataMap;
	}
	
	@Override
	public void open(DataProvider provider) {		
		this.provider = provider;
		this.notifications = new ArrayList<>();
		log.log(Level.FINER, "MessageHandler opened with data provider: {0}", new Object[] { provider });
			
	}

	@Override
	public void close() {
		this.provider = null;
		this.notifications = null;
		log.log(Level.FINER, "MessageHandler closed.");
	}
	
	@Override
	public void notify(Map<String, Object> map) {
		this.notifications.add(map);
		log.log(Level.FINE, "Notification received: {0}", new Object[] { map });
	}
	
	@Override
	public void event(String eventId, String paramId, Date timestamp, 
			int severity, String message, Map<String, Object> map) {		
		log.log(Level.FINE,
				"Event received: '{' eventId={0}, paramId={1}, timestamp={2}, severity={3}, message={4}, paramMap={5} '}' ",
				new Object[] { eventId, paramId, timestamp.getTime(), severity, message, map });		
	}
	
	public List<Map<String, Object>> getNotifications() {
		return notifications;
	}
	
	public void clearNotifications() {
		this.notifications.clear();
	}

	public void connect(String host, int port) throws UnknownHostException, IOException {
		this.msgClient.connect(host, port);
	}
	
	public void disconnect() throws IOException, InterruptedException {
		msgClient.disconnect();		
	}
	
	public String aimStatusCodeToString(int code) {
		switch (code) {
			case 0: return "SUCCESS";
			case 1: return "MISC_ERROR_TOTAL";
			case 2: return "MISC_ERROR_PARTIAL";
			case 3: return "PERMISSON_ERROR";
			case 4: return "PASSWORD_ERROR";
			case 5: return "REGION_NOT_FOUND_ERROR";
			case 6: return "OP_NOT_POSSIBLE_ERROR"; 
			case 7: return "OUT_OF_RANGE_ERROR"; 
			case 8: return "NO_IDENTIFIER"; 
			case 9: return "MULTIPLE_IDENTIFIERS"; 
			case 10: return "READ_ERROR"; 
			case 11: return "DECODING_ERROR"; 
			case 12: return "MATCH_ERROR"; 
			case 13: return "CODE_NOT_SUPPORTED"; 
			case 14: return "WRITE_ERROR"; 
			case 15: return "NOT_SUPPORTED_BY_DEVICE"; 
			case 16: return "NOT_SUPPORTED_BY_TAG"; 
			case 17: return "DEVICE_NOT_READY"; 
			case 18: return "INVALID_CONFIGURATION"; 
			case 19: return "RF_COMMUNICATION_ERROR"; 
			case 20: return "DEVICE_FAULT"; 
			case 21: return "TAG_HAS_LOW_BATTERY";
			default: return "-UNKNOWN-";
		}
	}
}
