package tainavi;

import java.util.ArrayList;

public class TraceKey {
	
	public static final int defaultFazzyThreshold = 35;
	public static final int noFazzyThreshold = 0;
	
	private String label = null;
	private String titlePop = null;
	private String center = null;
	private int fazzyThreshold = 0;
	private String okiniiri = null;
	private ArrayList<String> SearchStrKeys = null;
	private boolean disableRepeat = false;
	private boolean showLatestOnly = false;
	
	public void setLabel(String s) { label = s; }
	public String getLabel() { return label; }
	public void setTitlePop(String s) { titlePop = s; }
	public String getTitlePop() { return titlePop; }
	public void setCenter(String s) { center = s; }
	public String getCenter() { return center; }
	public void setFazzyThreshold(int n) { fazzyThreshold = n; }
	public int getFazzyThreshold() { return fazzyThreshold; };
	public void setOkiniiri(String s) { okiniiri = s; }
	public String getOkiniiri() { return okiniiri; }
	public void setSearchStrKeys(ArrayList<String> sa) { SearchStrKeys = sa; }
	public ArrayList<String> getSearchStrKeys() { return SearchStrKeys; }
	public void setDisableRepeat(boolean b) { disableRepeat = b; }
	public boolean getDisableRepeat() { return disableRepeat; }
	public void setShowLatestOnly(boolean b) { showLatestOnly = b; }
	public boolean getShowLatestOnly() { return showLatestOnly; }
}
