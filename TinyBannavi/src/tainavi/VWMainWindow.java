package tainavi;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;


public class VWMainWindow extends JPanel {

	private static final long serialVersionUID = 1L;

	/*
	 * 定数
	 */
	public static enum MWinTab {
		LISTED	("リスト形式"),
		PAPER	("新聞形式"),
		RSVED	("本体予約一覧"),
		RECED	("録画結果一覧"),
		AUTORES	("自動予約一覧"),
		SETTING	("各種設定"),
		RECSET	("レコーダ設定"),
		CHSET	("CH設定"),
		CHSORT	("CHソート設定"),
		CHCONV	("CHコンバート設定"),
		CHDAT	("CHコード設定"),
		;
		
		String name;
		
		private MWinTab(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		public int getIndex() {
			return ordinal();
		}
		
		public static MWinTab getAt(int index) {
			for ( MWinTab tab : MWinTab.values() ) {
				if ( tab.ordinal() == index ) {
					return tab;
				}
			}
			return null;
		}
		
		public static int size() { return MWinTab.values().length; }
		
	}
	
	/*
	 * 部品
	 */
	
	private JTabbedPane jTabbedPane = null;

	
	/*
	 *  コンストラクタ
	 */
	
	public VWMainWindow() {
		this.setLayout(new BorderLayout());
		this.add(getJTabbedPane(), BorderLayout.CENTER);
		
		// タブを全部準備する
		for ( MWinTab tab : MWinTab.values() ) {
			addTab(null, tab);
		}
	}
	
	
	/*
	 * 公開メソッド
	 */
	
	// ツールバーを追加する
	public void addToolBar(Component comp){
		this.add(comp, BorderLayout.PAGE_START);
	}
	
	
	public void addStatusArea(Component comp) {
		this.add(comp, BorderLayout.PAGE_END);
	}
	
	// タブを追加する
	public boolean addTab(Component comp, MWinTab tab) {
		if ( jTabbedPane.getTabCount() < tab.getIndex() ) {
			System.err.println(String.format("[DEBUG][メインウィンドウ] タブの数があわない： %d/%d",jTabbedPane.getTabCount(),tab.getIndex()));
			return false;
		}
		if ( jTabbedPane.getTabCount() > tab.getIndex() ) {
			jTabbedPane.remove(tab.getIndex());
		}
		jTabbedPane.add(comp, tab.getName(), tab.getIndex());
		return true;
	}

	// タブを切り替える
	public void setSelectedTab(MWinTab tab) {
		if ( tab == null ) {
			jTabbedPane.setSelectedIndex(-1);
		}
		else if (jTabbedPane.getTabCount() > tab.getIndex()) {
			jTabbedPane.setSelectedIndex(tab.getIndex());
		}
	}
	
	//
	public Component getTab(MWinTab tab) {
		return this.getComponent(tab.getIndex());
	}
	
	// タブが選択されているか確認する
	public boolean isTabSelected(MWinTab tab) {
		return (jTabbedPane.getSelectedIndex() == tab.getIndex());
	}
	
	// どのタブが選択されているのやら
	public MWinTab getSelectedTab() { return MWinTab.getAt(jTabbedPane.getSelectedIndex()); }
	
	// 設定タブをトグル切り替え
	private final int firstSettingTab = MWinTab.SETTING.ordinal();
	private final int countSettingTab = MWinTab.size()-firstSettingTab;
	private Component[] st_comp = new Component[countSettingTab];
	private String[] st_title = new String[countSettingTab];
	public boolean toggleShowSettingTabs() {
		if (st_comp[0] == null) {
			for (int i=countSettingTab-1; i>=0; i--) {
				st_comp[i] = this.jTabbedPane.getComponentAt(firstSettingTab+i);
				st_title[i] = this.jTabbedPane.getTitleAt(firstSettingTab+i);
				this.jTabbedPane.remove(firstSettingTab+i);
			}
			return false;
		}
		else {
			for (int i=0; i<countSettingTab; i++) {
				this.jTabbedPane.add(st_comp[i]);
				this.jTabbedPane.setTitleAt(firstSettingTab+i, st_title[i]);
				st_comp[i] = null;
			}
			this.jTabbedPane.setSelectedIndex(firstSettingTab);
			return true;
		}
	}
	
	public boolean getShowSettingTabs() {
		return (jTabbedPane.getTabCount() > firstSettingTab);
	}
	public void setShowSettingTabs(boolean b) {
		if ((b && jTabbedPane.getTabCount() <= firstSettingTab) ||
				( ! b && jTabbedPane.getTabCount() > firstSettingTab)) {
			toggleShowSettingTabs();
		}
	}
	
	
	/*
	 * 
	 */
	
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();
		}
		return jTabbedPane;
	}
	
	/**
	 * @deprecated
	 */
	public void appendStatusMessage(String s) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @deprecated
	 * @see Viewer#setStatusVisible(boolean)
	 */
	public void setStatusVisible(boolean b) {
		throw new UnsupportedOperationException();
	}
	
}
