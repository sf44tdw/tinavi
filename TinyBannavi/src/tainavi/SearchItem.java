package tainavi;

import java.util.ArrayList;

public interface SearchItem {

	/**
	 * 検索アイテムのラベル
	 */
	public String toString();
	
	/**
	 * 検索マッチ数のクリア
	 */
	public void clearMatchedList();

	/**
	 * 検索マッチ数のカウントアップ
	 */
	public void addMatchedList(ProgDetailList pdl);

	/**
	 * 検索マッチ数の取得
	 */
	public ArrayList<ProgDetailList> getMatchedList();

	/**
	 * 検索にマッチした番組が存在するかどうか
	 */
	public boolean isMatched();

}
