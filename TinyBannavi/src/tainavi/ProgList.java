package tainavi;

import java.awt.Color;
import java.util.ArrayList;
import tainavi.ProgDateList;

public class ProgList {
	public String Area;
	public String SubArea;
	public String Type;
	public String Center;
	public String CenterId;
	public String ChId;
	public Color BgColor;
	public boolean enabled;
	public ArrayList<ProgDateList> pdate;
	
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
