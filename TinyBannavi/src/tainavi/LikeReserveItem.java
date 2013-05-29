package tainavi;

public class LikeReserveItem {

	private HDDRecorder rec = null;
	private ReserveList rsv = null;
	
	public LikeReserveItem(HDDRecorder rec, ReserveList rsv) {
		this.rec = rec;
		this.rsv = rsv;
	}
	
	public HDDRecorder getRec() { return rec; }
	
	public ReserveList getRsv() { return rsv; }
	
	@Override
	public String toString() {
		return rsv.getTitle()+", "+rsv.getRec_pattern()+", "+rsv.getAhh()+":"+rsv.getAmm()+", "+rec.Myself()+", "+rsv.getTuner();
	}
	
}
