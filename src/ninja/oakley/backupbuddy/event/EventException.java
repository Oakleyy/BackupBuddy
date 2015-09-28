package ninja.oakley.backupbuddy.event;

public class EventException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4191226067345001394L;

	private String message = "";
	
	public EventException(String message){
		this.message = message;
	}
	
	public String getMessage(){
		return this.message;
	}
	
}
