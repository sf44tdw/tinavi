package tainavi;

import java.awt.Color;
import java.util.ArrayList;
import tainavi.ProgDateList;

public class ProgList {
	String Area;
	String SubArea;
	String Type;
	String Center;
	String CenterId;
	String ChId;
	Color BgColor;
	boolean enabled;
	ArrayList<ProgDateList> pdate;
	
	public ProgList() {
		Area = "";
		SubArea = "";
		Type = "";
		Center = "";
		CenterId = "";
		ChId = "";
		BgColor = new Color(180,180,180);
		enabled = true;
		pdate = new ArrayList<ProgDateList>();
	}
}
