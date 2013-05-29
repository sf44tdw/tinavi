package tainavi;

import java.util.EventListener;

public interface VWTimerRiseListener extends EventListener {
	
	public void timerRised(VWTimerRiseEvent e);
	
}
