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
	private JTabbedPane jTabbedPane_settings = null;

	
	/*
	 *  コンストラクタ
	 */
	
	public VWMainWindow() {
		this.setLayout(new BorderLayout());
		this.add(getJTabbedPane(), BorderLayout.CENTER);
		getJTabbedPane_settings();

		// タブを全部準備する
		for ( MWinTab tab : MWinTab.values() ) {
			if ( tab == MWinTab.SETTING ) {
				jTabbedPane.add(jTabbedPane_settings, "設定");
			}
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
		if ( jTabbedPane.getTabCount() + jTabbedPane_settings.getTabCount() < tab.getIndex() ) {
			System.err.println(String.format("[DEBUG][メインウィンドウ] タブの数があわない： %d/%d", jTabbedPane.getTabCount(), tab.getIndex()));
			return false;
		}

		int tabIndex = tab.getIndex();
		JTabbedPane tabPane = jTabbedPane;
		if ( tabIndex >= MWinTab.SETTING.getIndex() ) {
			tabIndex -= MWinTab.SETTING.getIndex();
			tabPane = jTabbedPane_settings;
		}
		if ( tabPane.getTabCount() > tabIndex ) {
			tabPane.remove(tabIndex);
		}
		tabPane.add(comp, tab.getName(), tabIndex);
		return true;
	}

	// タブを切り替える
	public void setSelectedTab(MWinTab tab) {
		if ( tab == null ) {
			jTabbedPane.setSelectedIndex(-1);
			return;
		}
		if ( tab.getIndex() >= MWinTab.SETTING.getIndex() ) {
			jTabbedPane_settings.setSelectedIndex(tab.getIndex()-MWinTab.SETTING.getIndex());
			jTabbedPane.setSelectedIndex(MWinTab.SETTING.getIndex());
			return;
		}
		jTabbedPane.setSelectedIndex(tab.getIndex());
	}
	
	//
	public Component getTab(MWinTab tab) {
		if ( tab.getIndex() >= MWinTab.SETTING.getIndex() ) {
			return jTabbedPane_settings.getComponent(tab.getIndex() - MWinTab.SETTING.getIndex());
		}
		return jTabbedPane.getComponent(tab.getIndex());
	}
	
	// タブが選択されているか確認する
	public boolean isTabSelected(MWinTab tab) {
		if ( tab.getIndex() >= MWinTab.SETTING.getIndex() ) {
			return (jTabbedPane.getSelectedIndex() == MWinTab.SETTING.getIndex() && jTabbedPane_settings.getSelectedIndex() == tab.getIndex()-MWinTab.SETTING.getIndex());
		}
		return (jTabbedPane.getSelectedIndex() == tab.getIndex());
	}
	
	// どのタブが選択されているのやら
	public MWinTab getSelectedTab() {
		if ( jTabbedPane.getSelectedIndex() == MWinTab.SETTING.getIndex() ) {
			return MWinTab.getAt(jTabbedPane.getSelectedIndex());
		}
		return MWinTab.getAt(jTabbedPane.getSelectedIndex());
	}
	
	// 設定タブをトグル切り替え
	private final int firstSettingTab = MWinTab.SETTING.ordinal();
	private final int countSettingTab = MWinTab.size()-firstSettingTab;
	private Component[] st_comp = new Component[countSettingTab];
	private String[] st_title = new String[countSettingTab];
	public boolean toggleShowSettingTabs() {
		return true;
	}
	
	public boolean getShowSettingTabs() {
		return true;
	}
	public void setShowSettingTabs(boolean b) {
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

	private JTabbedPane getJTabbedPane_settings() {
		if (jTabbedPane_settings == null) {
			jTabbedPane_settings = new JTabbedPane();
		}
		return jTabbedPane_settings;
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
