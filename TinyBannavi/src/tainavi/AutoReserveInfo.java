package tainavi;

import java.util.ArrayList;

import tainavi.TVProgram.ProgSubgenre;


public class AutoReserveInfo implements Cloneable {
	
	/*******************************************************************************
	 * clone(ディープコピー)
	 ******************************************************************************/
	
	@Override
	public AutoReserveInfo clone() {
		try {
			AutoReserveInfo p = (AutoReserveInfo) super.clone();
			
			p.Channels = new ArrayList<String>();
			for ( String ch : Channels ) {
				p.Channels.add(ch);
			}
			
			p.Subgenres = new ArrayList<ProgSubgenre>();
			for ( ProgSubgenre sg : Subgenres ) {
				p.Subgenres.add(sg);
			}
			
			return p;
		}
		catch ( Exception e ) {
			throw new InternalError(e.toString());
		}
	}

	
	/*******************************************************************************
	 * メンバー変数
	 ******************************************************************************/

	private String Id;
	
	private String Label;
	
	private String IncludeKeyword;
	private String ExcludeKeyword;
	
	private Boolean RegularExpression;
	private Boolean FazzySearch;
	private Boolean TitleOnly;
	
	/*
	 * TvRock : CHコードはコントローラの値と同じ（ただしHEX）
	 * EDCB : CHコードは予約操作時と同じ
	 */
	private ArrayList<String> Channels = new ArrayList<String>();							// *D
	private ArrayList<ProgSubgenre> Subgenres = new ArrayList<TVProgram.ProgSubgenre>();	// *D

	
	/*******************************************************************************
	 * getter/setter
	 ******************************************************************************/
	
	public String getId() { return Id; }
	public void setId(String s) { Id = s; }
	
	public String getLabel() { return Label; }
	public void setLabel(String s) { Label = s; }

	public String getIncludeKeyword() { return IncludeKeyword; }
	public void setIncludeKeyword(String s) { IncludeKeyword = s; }
	public String getExcludeKeyword() { return ExcludeKeyword; }
	public void setExcludeKeyword(String s) { ExcludeKeyword = s; }

	public Boolean getRegularExpression() { return RegularExpression; }
	public void setRegularExpression(Boolean b) { RegularExpression = b; }
	public Boolean getFazzySearch() { return FazzySearch; }
	public void setFazzySearch(Boolean b) { FazzySearch = b; }
	public Boolean getTitleOnly() { return TitleOnly; }
	public void setTitleOnly(Boolean b) { TitleOnly = b; }
	
	public ArrayList<String> getChannels() { return Channels; }
	
	/*******************************************************************************
	 * extra
	 ******************************************************************************/
	
	public String getChNames() { return (Channels.size() > 0) ? Channels.get(0) : null; }

}
