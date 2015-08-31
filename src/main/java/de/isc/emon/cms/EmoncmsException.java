package de.isc.emon.cms;


public class EmoncmsException extends Exception {
	private static final long serialVersionUID = 3628437143951931739L;

	public EmoncmsException() {
		super();
	}

	public EmoncmsException(String s) {
		super(s);
	}

	public EmoncmsException(Throwable cause) {
		super(cause);
	}

	public EmoncmsException(String s, Throwable cause) {
		super(s, cause);
	}

}
