package tainavi;

import java.util.ArrayList;

public class LikeReserveList extends ArrayList<LikeReserveItem> {

	private static final long serialVersionUID = 1L;

	public ReserveList getRsv(int i) { return this.get(i).getRsv(); }
	
	public HDDRecorder getRec(int i) { return this.get(i).getRec(); }
	
}
