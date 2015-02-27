package com.qingyou.http;

public class ProtocolException extends Exception {

	private int status;
	private static final long serialVersionUID = -6139444426219086633L;

	public ProtocolException() {
	}

	public ProtocolException(String detailMessage) {
		super(detailMessage);
	}

	public ProtocolException(Throwable throwable) {
		super(throwable);
	}

	public ProtocolException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
