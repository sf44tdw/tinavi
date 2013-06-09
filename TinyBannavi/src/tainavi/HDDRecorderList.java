package tainavi;

import java.util.ArrayList;

import tainavi.HDDRecorder.RecType;

/**
 * {@link HDDRecorder} のリストを実現するクラスです. 
 * @version 3.15.4β～
 */
public class HDDRecorderList extends ArrayList<HDDRecorder> {

	private static final long serialVersionUID = 1L;

	private static final HDDRecorderList mylist = new HDDRecorderList();
	
	// レコーダIDから種類を調べる
	public RecType getRecId2Type(String recId) {
		ArrayList<HDDRecorder> rl = this.findPlugin(recId);
		if ( rl.size() > 0 ) {
			return rl.get(0).getType();
		}
		return RecType.RECORDER;
	}
	
	/**
	 *  レコーダIDに合ったプラグイン（種族）を探す
	 */
	public HDDRecorderList findPlugin(String recId) {
		if ( recId == null ) {
			return this;
		}
		HDDRecorderList list = new HDDRecorderList();
		for ( HDDRecorder rec : this ) {
			if ( recId.equals(rec.getRecorderId()) ) {
				list.add(rec);
			}
		}
		return list;
	}
	
	/**
	 * 実レコーダのプラグイン（個体）を探す
	 * @return 本来{@link HDDRecorder}を返すべきだが、呼び出し側の処理を書きやすくするために{@link HDDRecorderList}を返す。よって、==nullではなく.size()==0で確認する。
	 * @param myself 「すべて」を指定する場合はNULLをどうぞ
	 */
	public HDDRecorderList findInstance(String myself) {
		if (myself == null || myself.length() == 0) {
			return this;
		}
		mylist.clear();
		for ( HDDRecorder rec : this ) {
			if ( rec.isMyself(myself) ) {
				mylist.add(rec);
				break;
			}
		}
		return mylist;
	}
}
