package tainavi;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;


public class JTXTButton extends JLabel {

	private static final long serialVersionUID = 1L;

	/*******************************************************************************
	 * 定数
	 ******************************************************************************/
	
	/**
	 *  フォントスタイル
	 */
	public static enum FontStyle {
		BOLD	("太字"),
		ITALIC	("斜体"),
		UNDERLINE	("下線");
		
		private String name;
		
		private FontStyle(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		
		public String getId() {
			return super.toString();
		}
		
		public static FontStyle get(String id) {
			for ( FontStyle fs : FontStyle.values() ) {
				if ( fs.getId().equals(id) ) {
					return fs;
				}
			}
			return null;
		}
	};
	
	private static final float DRAWTAB = 2.0F; 
	
	
	/*******************************************************************************
	 * 部品
	 ******************************************************************************/
	
	// 描画バッファ
	private BufferedImage image = null;		// ビットマップ
	
	private int vrow;						// 仮想座標縦位置
	private int vcolumn;					// 仮想座標横位置
	private int vheight;					// 仮想座標高さ
	private int vwidth;						// 仮想座標幅
	
	// 番組情報
	private ProgDetailList tvd = null;		// 番組情報そのまま
	
	// 表示設定
	private static boolean showStart = true;
	private static boolean splitEpno = false;
	private static boolean showDetail = true;
	private static float detailTab = 2.0F;
	private static int detailRows = 3;
	
	private static Font defaultFont = new JLabel().getFont();
	private static Font titleFont = defaultFont;
	private static int titleFontSize = defaultFont.getSize();
	private static Color titleFontColor = Color.BLUE;
	private static int titleFontStyle = Font.BOLD;
	private static boolean titleFontUL = true;
	private static Font detailFont = defaultFont;
	private static int detailFontSize = defaultFont.getSize();
	private static Color detailFontColor = Color.DARK_GRAY;
	private static int detailFontStyle = defaultFont.getStyle();
	private static boolean detailFontUL = false;
	private static Object aahint = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;

	private static int columnWidth = 0;
	private static float heightMultiplier = 0;
	
	/*******************************************************************************
	 * コンストラクタ
	 ******************************************************************************/
	
	// ないよ
	
	
	/*******************************************************************************
	 * メソッド
	 ******************************************************************************/
	
	// 内容をリセットする
	// setVisible(false)するとリソースが解放されてしまうのか再描画に時間がかかるようになるので表示範囲外に出して隠してしまう
	public void clean() {
		tvd = null;
		image = null;
		setBounds(-1,-1,0,0);
	}
	
	// フラグを変えた後に再描画させる
	public void forceRepaint() {
		image = null;
		super.repaint();
	}
	
	// 仮想位置の変更
	//public void setVRow(int n) { vrow = n; }
	public int getVRow() { return vrow; }
	//public void setVColumn(int n) { vcolumn = n; }
	public int getVColumn() { return vcolumn; }
	//public void setVHeight(int n) { vheight = n; }
	public int getVHeight() { return vheight; }
	
	public static void setColumnWidth(int n) { columnWidth = n; }
	public static void setHeightMultiplier(float f) { heightMultiplier = f; }
	
	public void setVBounds(int x, int y, int width, int height) {
		vrow = y;
		vcolumn = x;
		vheight = height;
		vwidth = width;
		super.setBounds(
				vcolumn*columnWidth,
				(int) Math.ceil(((float)vrow)*heightMultiplier),
				vwidth*columnWidth,
				(int) Math.ceil(((float)vheight)*heightMultiplier));
	}
	
	public void reVBounds() {
		super.setBounds(
				vcolumn*columnWidth,
				(int) Math.ceil(((float)vrow)*heightMultiplier),
				vwidth*columnWidth,
				(int) Math.ceil(((float)vheight)*heightMultiplier));
	}
	
	// 番組情報のやりとり
	public void setInfo(ProgDetailList tvd) {
		this.tvd = tvd;
		this.setText(null);	// 簡易表示時代の名残
		
		this.setVerticalAlignment(JButton.TOP);
		this.setHorizontalAlignment(JButton.LEFT);
		//this.setBorder(new LineBorder(Color.BLACK,1));
		this.setOpaque(true);
	}
	public ProgDetailList getInfo() {
		return tvd;
	}
	
	// 予約待機枠を表示するかどうかの確認
	public boolean isStandby() { return tvd.marked && tvd.showinstandby; }
	
	// 表示スタイル
	public static void setShowStart(boolean b) {
		showStart = b;
	}
	public static void setSplitEpno(boolean b) {
		splitEpno = b;
	}
	public static void setShowDetail(boolean b) {
		showDetail = b;
	}
	public static void setDetailTab(float n) {
		detailTab = n;
	}
	public static void setDetailRows(int n) {
		detailRows = n;
	}
	
	// フォントスタイル
	public static void setTitleFont(String fn) {
		if ( fn != null && ! fn.equals("") ) {
			Font f = new Font(fn,titleFontStyle,titleFontSize);
			if ( f != null ) {
				titleFont = f;
				return;
			}
		}
		//titleFont = this.getFont();
	}
	public static void setTitleFontSize(int n) {
		titleFontSize = n;
		titleFont = titleFont.deriveFont((float)titleFontSize);
	}
	public static void setTitleFontColor(Color c) {
		titleFontColor = c;
	}
	public static void setDetailFont(String fn) {
		if ( fn != null && ! fn.equals("") ) {
			Font f = new Font(fn,detailFontStyle,detailFontSize);
			if ( f != null ) {
				detailFont = f;
				return;
			}
		}
		//detailFont = new JLabel().getFont();
	}
	public static void setDetailFontSize(int n) {
		detailFontSize = n;
		detailFont = detailFont.deriveFont((float)detailFontSize);
	}
	public static void setDetailFontColor(Color c) {
		detailFontColor = c;
	}
	
	// フォントスタイルの変更
	public static void setTitleFontStyle(ArrayList<FontStyle> fsa) {
		setTmpFontStyle(fsa);
		titleFontStyle = tmpFontStyle;
		titleFontUL = tmpFontUL;
		titleFont = titleFont.deriveFont((titleFont.getStyle() & ~(Font.BOLD|Font.ITALIC)) | titleFontStyle);
	}
	public static void setDetailFontStyle(ArrayList<FontStyle> fsa) {
		setTmpFontStyle(fsa);
		detailFontStyle = tmpFontStyle;
		detailFontUL = tmpFontUL;
		detailFont = detailFont.deriveFont((detailFont.getStyle() & ~(Font.BOLD|Font.ITALIC)) | detailFontStyle);
	}
	private static void setTmpFontStyle(ArrayList<FontStyle> fsa) {
		tmpFontStyle = 0;
		tmpFontUL = false;
		for ( FontStyle fs : fsa ) {
			switch (fs) {
			case BOLD:
				tmpFontStyle |= Font.BOLD;
				break;
			case ITALIC:
				tmpFontStyle |= Font.ITALIC;
				break;
			case UNDERLINE:
				tmpFontUL = true;
				break;
			}
		}
	}
	private static int tmpFontStyle;
	private static boolean tmpFontUL;
	
	// フォントエイリアスの変更
	public static void setAAHint(Object o) {
		aahint = o;
	}
	
	
	/*******************************************************************************
	 * メソッド
	 ******************************************************************************/
	
	/**
	 * ビットマップの描画処理
	 */
	@Override
	protected void paintComponent(Graphics g) { 
		
		super.paintComponent(g);
		
		// 初回描画時
		if (image == null) {
			//
			Dimension  d  = getSize();
			int imgw = d.width;
			int imgh = d.height;

			float draww = (float)imgw-DRAWTAB*2.0F;
			float drawh = (float)imgh;
			float detailw = draww-detailTab;

			image = new BufferedImage(imgw, imgh, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = (Graphics2D)image.createGraphics();
			
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,aahint);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			
			float baseline = 0.0F;
			
			// 開始時刻と延長警告の描画
			if (showStart && tvd.start != null && tvd.start.length() > 0) {
				//
				Font fs = detailFont;
				String sStr = tvd.start+" "+tvd.extension_mark;
				//
				Font f = fs.deriveFont(fs.getStyle() | Font.BOLD);
				AttributedString as = new AttributedString(sStr);
				as.addAttribute(TextAttribute.FONT, f);
				as.addAttribute(TextAttribute.FOREGROUND, Color.BLACK, 0, 5);
				if (sStr.length() > 6) {
					as.addAttribute(TextAttribute.FOREGROUND, Color.RED, 6, sStr.length());
				}
				AttributedCharacterIterator ac = as.getIterator();
				FontRenderContext fc = g2.getFontRenderContext();
				LineBreakMeasurer m = new LineBreakMeasurer(ac,fc);
				while ( m.getPosition() < sStr.length() ) {
					TextLayout tl = m.nextLayout(draww);
					baseline += tl.getAscent();
					tl.draw(g2, DRAWTAB, baseline);
					baseline += tl.getDescent() + tl.getLeading();
				}
			}
			
			// タイトルの描画
			String title = ( splitEpno ) ? tvd.splitted_title : tvd.title;
			if (title.length() > 0) {
				//
				String aMark;
				if (showStart && tvd.start.length() > 0) {
					aMark = tvd.prefix_mark + tvd.newlast_mark;
				}
				else {
					if (tvd.start.length() > 0) {
						aMark = tvd.extension_mark + tvd.prefix_mark + tvd.newlast_mark;
					}
					else {
						aMark = tvd.prefix_mark + tvd.newlast_mark;
					}
				}
				String tStr = aMark+title+tvd.postfix_mark;
				//
				AttributedString as = new AttributedString(tStr);
				as.addAttribute(TextAttribute.FONT, titleFont);
				{
					if (titleFontUL) {
						as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL, aMark.length(), aMark.length()+title.length());
					}
					as.addAttribute(TextAttribute.FOREGROUND, titleFontColor, aMark.length(), tStr.length());
					if (aMark.length() > 0) {
						as.addAttribute(TextAttribute.FOREGROUND, Color.RED, 0, aMark.length());
					}
				}
				AttributedCharacterIterator ac = as.getIterator();
				FontRenderContext fc = g2.getFontRenderContext();
				LineBreakMeasurer m = new LineBreakMeasurer(ac,fc);
				while (m.getPosition() < tStr.length()) {
					TextLayout tl = m.nextLayout(draww);
					baseline += tl.getAscent();
					tl.draw(g2, DRAWTAB, baseline);
					baseline += tl.getDescent() + tl.getLeading();
				}
			}
			
			// 番組詳細の描画
			if ( showDetail ) {
				String detail;
				if ( splitEpno ) {
					detail = tvd.splitted_detail;
				}
				else {
					detail = tvd.detail;
				}
				if ( detail.length() > 0 ) {
					AttributedString as = new AttributedString(detail);
					as.addAttribute(TextAttribute.FONT, detailFont);
					if (detailFontUL) {
						as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
					}
					as.addAttribute(TextAttribute.FOREGROUND, detailFontColor);
					AttributedCharacterIterator ac = as.getIterator();
					FontRenderContext fc = g2.getFontRenderContext();
					LineBreakMeasurer m = new LineBreakMeasurer(ac,fc);
					for ( int row=0; m.getPosition()<detail.length() && baseline<=drawh && (detailRows>0 && row<detailRows); row++ ) {
						TextLayout tl = m.nextLayout(detailw);
						baseline += tl.getAscent();
						tl.draw(g2, (DRAWTAB+detailTab), baseline);
						baseline += tl.getDescent() + tl.getLeading();
					}
				}
			}
		}
		
		// 反映
		g.drawImage(image, 0, 0, this);
	}
}
