package tainavi;

import java.util.EventListener;


/**
 * レコーダの状態変化を各コンポーネントに伝達するリスナー
 * @see HDDRecorderSelectionEvent
 */
public interface HDDRecorderListener extends EventListener {
	
	// 定数
	public static final String SELECTED_ALL = null;
	public static final String SELECTED_PICKUP = "";
	
	/**
	 * レコーダーの選択イベント
	 */
	public void valueChanged(HDDRecorderSelectionEvent e);

	/**
	 * レコーダーの情報変更イベント
	 */
	public void stateChanged(HDDRecorderChangeEvent e);

	
}
