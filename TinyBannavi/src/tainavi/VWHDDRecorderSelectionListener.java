package tainavi;

import java.util.EventListener;

public interface VWHDDRecorderSelectionListener extends EventListener {
	
	// 定数
	public static final String SELECTED_ALL = null;
	public static final String SELECTED_PICKUP = "";
	
	//
	public void recorderSelected(VWHDDRecorderSelectionEvent e);
	
	// 多分↓はよくないコード
	
	public String getSelectedRecorder();
	
	public HDDRecorderList getSelectedRecorderList();
	
}
