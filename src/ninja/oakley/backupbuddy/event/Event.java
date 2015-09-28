package ninja.oakley.backupbuddy.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class Event {

	private String name = "";
	private List<Method> handles;

	public Event(){
		handles = new ArrayList<Method>();
	}
	
	public String getEventName() {
		if (name.isEmpty()) {
			name = getClass().getSimpleName();
		}
		return name;
	}

	public abstract void execute();
	
	
}
