package tainavi;

public class LikeReserveItem {

	private HDDRecorder rec = null;
	private ReserveList rsv = null;
	private long dist = 0;
	
	public LikeReserveItem(HDDRecorder rec, ReserveList rsv, long dist) {
		this.rec = rec;
		this.rsv = rsv;
		this.dist = dist;
	}
	
	public HDDRecorder getRec() { return rec; }
	
	public ReserveList getRsv() { return rsv; }
	
	public long getDist() { return dist; }
	
	@Override
	public String toString() {
		return rsv.getTitle()+", "+rsv.getRec_pattern()+", "+rsv.getAhh()+":"+rsv.getAmm()+", "+rec.Myself()+", "+rsv.getTuner();
	}
	
}
