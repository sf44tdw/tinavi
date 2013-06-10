package tainavi;

import java.io.File;
import java.util.ArrayList;


public class AutoReserveInfoList extends ArrayList<AutoReserveInfo> {

	private static final long serialVersionUID = 1L;
	
	private final String filename;
	
	public AutoReserveInfoList(String envdir, String recid, String ipaddr, String portno) {
		super();
		this.filename = String.format("%s%s%s.%s_%s_%s.txt", "env", File.separator, "autorsv", recid, ipaddr, portno);
	}

}
