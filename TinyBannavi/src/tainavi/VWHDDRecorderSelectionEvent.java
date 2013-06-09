package tainavi;

import java.util.EventObject;
import java.util.GregorianCalendar;

public class VWHDDRecorderSelectionEvent extends EventObject {
	
	private static final long serialVersionUID = 1L;
	
	private final String selected;
	private final HDDRecorderList recs;

	public String getSelected() { return selected; }
	
	public HDDRecorderList getSelectedRecorderList() { return recs; }
	
	public VWHDDRecorderSelectionEvent(Object source, String selected, HDDRecorderList recs) {
		super(source);
		this.selected = selected;
		this.recs = recs;
	}

}
