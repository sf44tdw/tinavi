package tainavi;

import java.util.ArrayList;

import tainavi.HDDRecorder.RecType;


/**
 * {@link HDDRecorder} のリストを実現するクラスです. 
 * @version 3.15.4β～
 */
public class HDDRecorderList extends ArrayList<HDDRecorder> {

	private static final long serialVersionUID = 1L;
	
	// ↓ 自己フィールド生成を行うor自己フィールド生成によるスタックオーバーフローを回避する(static修飾)ための異常なコード（自戒のためにコメントとして残す）
	//private static final HDDRecorderList mylist = new HDDRecorderList();
	
	// レコーダIDから種類を調べる
	public RecType getRecId2Type(String recId) {
		ArrayList<HDDRecorder> rl = this.findPlugin(recId);
		if ( rl.size() > 0 ) {
			return rl.get(0).getType();
		}
		return RecType.RECORDER;
	}
	
	/**
	 *  レコーダIDに合ったプラグイン（一族郎党）を探す
	 */
	public HDDRecorderList findPlugin(String recId) {
		if ( recId == null ) {
			return this;
		}
		
		HDDRecorderList mylist = new HDDRecorderList();
		for ( HDDRecorder rec : this ) {
			if ( recId.equals(rec.getRecorderId()) ) {
				mylist.add(rec);
			}
		}
		return mylist;
	}
	
	/**
	 * 実レコーダのプラグイン（個体）を探す
	 * @param mySelf 「すべて」を指定する場合はNULLをどうぞ
	 * @return
	 * <P> 「すべて」「ピックアップのみ」→全部のインスタンスを返す 
	 * <P> 「個別指定」→本来{@link HDDRecorder}を返すべきだが、呼び出し側の処理を書きやすくするために{@link HDDRecorderList}を返す。よって、==nullではなく.size()==0で確認する。
	 */
	public HDDRecorderList findInstance(String mySelf) {
		if (mySelf == null || mySelf.length() == 0) {
			// 「すべて」「ピックアップのみ」→全部のインスタンスを返す
			return this;
		}
		
		// 個別指定
		HDDRecorderList mylist = new HDDRecorderList();
		for ( HDDRecorder rec : this ) {
			if ( rec.isMyself(mySelf) ) {
				mylist.add(rec);
				break;
			}
		}
		return mylist;
	}
}
