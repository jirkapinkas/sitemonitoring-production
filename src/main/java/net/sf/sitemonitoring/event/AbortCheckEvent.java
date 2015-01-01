package net.sf.sitemonitoring.event;

public class AbortCheckEvent {

	private int checkId;
	private String reason;

	public AbortCheckEvent(int checkId, String reason) {
		this.checkId = checkId;
		this.reason = reason;
	}

	public int getCheckId() {
		return checkId;
	}

	public String getReason() {
		return reason;
	}
}
