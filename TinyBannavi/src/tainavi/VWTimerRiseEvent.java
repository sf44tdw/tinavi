package tainavi;

import java.util.EventObject;
import java.util.GregorianCalendar;

public class VWTimerRiseEvent extends EventObject {

	private final GregorianCalendar calendar;
	
	public GregorianCalendar getCalendar() { return calendar; }
	
	public VWTimerRiseEvent(Object source) {
		super(source);
		this.calendar = new GregorianCalendar();
	}

}
