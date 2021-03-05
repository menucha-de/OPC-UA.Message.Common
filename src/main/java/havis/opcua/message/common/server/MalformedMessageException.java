package havis.opcua.message.common.server;

public class MalformedMessageException extends Exception {
	private static final long serialVersionUID = 7383750067793826167L;

	public MalformedMessageException() {
		super();
	}

	public MalformedMessageException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MalformedMessageException(String message, Throwable cause) {
		super(message, cause);
	}

	public MalformedMessageException(String message) {
		super(message);
	}

	public MalformedMessageException(Throwable cause) {
		super(cause);
	}
	
	

}
