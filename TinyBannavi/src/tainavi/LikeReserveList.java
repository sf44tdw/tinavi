package tainavi;

import java.util.ArrayList;


public class LikeReserveList extends ArrayList<LikeReserveItem> {

	private static final long serialVersionUID = 1L;


	//
	private LikeReserveItem closest = null;
	
	public ReserveList getRsv(int i) { return this.get(i).getRsv(); }
	public ReserveList getRsv() { return ((closest!=null)?closest.getRsv():null); }
	
	public HDDRecorder getRec(int i) { return this.get(i).getRec(); }
	public HDDRecorder getRec() { return ((closest!=null)?closest.getRec():null); }
	
	public LikeReserveItem getClosest(String myself) {
		
		closest = null;
		
		for ( LikeReserveItem lr : this ) {
			if ( (myself!=HDDRecorder.SELECTED_ALL && myself!=HDDRecorder.SELECTED_PICKUP) && ! lr.getRec().Myself().equals(myself) ) {
				// レコーダの個別指定があれば
				continue;
			}
			if ( closest == null || Math.abs(closest.getDist()) > lr.getDist() ) {
				closest = lr;
			}
		}
		
		return closest;
	}

	/**
	 * 時刻昇順で並べる
	 */
	@Override
	public boolean add(LikeReserveItem element) {
		
		if ( size() < 0 ) {
			return super.add(element);
		}
		
		for ( int i=0; i<size(); i++ ) {
			LikeReserveItem lr = get(i);
			if ( lr.getDist() > element.getDist() ) {
				super.add(i, element);
				return true;
			}
		}
		
		return super.add(element);
	}
	
	/**
	 * つかっちゃヤーン
	 */
	@Deprecated
	@Override
	public void add(int index,LikeReserveItem element) {
		this.add(element);
	}

}
