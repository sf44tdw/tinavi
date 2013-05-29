package tainavi;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import tainavi.Env.AAMode;
import tainavi.Env.DblClkCmd;
import tainavi.Env.SnapshotFmt;
import tainavi.Env.UpdateOn;
import tainavi.JTXTButton.FontStyle;
import tainavi.TVProgram.ProgOption;


public class FieldUtils {
	
	/*******************************************************************************
	 * 定数とか
	 ******************************************************************************/
	
	private static final String SPCH_LF = "$LF$";
	private static final String SPCH_NULL = "$NULL$";
	
	private static final String SPMK_CM = "#";
	private static final String SPMK_SEP = "=";
	private static final String SPMK_LF = "\n";
	
	private static final String SPHD_MOD = SPMK_CM+" MODIFIED : ";
	private static final String SPHD_VER = SPMK_CM+" VERSION : ";
	private static final String SPHD_DEP = SPMK_CM+" DEPRECATED : ";
	private static final String SPHD_UNS = SPMK_CM+" UNSUPPORTED : ";
	private static final String SPHD_NOE = SPMK_CM+" NOELEMENT : ";
	
	private static final String MSGID = "[設定保存] ";
	private static final String ERRID = "[ERROR]"+MSGID;
	
	/*******************************************************************************
	 * 保存
	 ******************************************************************************/
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean save(String envText, Object root) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(SPHD_MOD);
		sb.append(CommonUtils.getDateTime(0));
		sb.append(SPMK_LF);
		sb.append(SPHD_VER);
		sb.append(VersionInfo.getVersionNumber());
		sb.append(SPMK_LF);
		sb.append(SPMK_LF);
		
		Field[] fd = root.getClass().getDeclaredFields();
		for ( Field fx : fd ) {
			fx.setAccessible(true);
			if ( Modifier.isFinal(fx.getModifiers()) ) {
				continue;
			}
			if ( Modifier.isStatic(fx.getModifiers()) ) {
				continue;
			}

			try {
				
				String key = fx.getName();
				Class cls = fx.getType();
				Object obj = fx.get(root);
				
				if ( fx.getAnnotation(Deprecated.class) != null ) {
					sb.append(SPHD_DEP);
					sb.append(key);
					sb.append(" AS ");
					sb.append(cls.getName());
					sb.append(SPMK_LF);
					continue;
				}
				
				int n = -1;
				ArrayList objlst = null;
				Class ocls = null;
				if ( cls == ArrayList.class ) {
					objlst = (ArrayList) obj;
					
					// nullの要素がある可能性がある
					if ( objlst.size() > 0 ) {
						ParameterizedType paramType = (ParameterizedType) fx.getGenericType();
						ocls = (Class) paramType.getActualTypeArguments()[0];
					}
					
					n = 1;
				}
				else if ( cls == HashMap.class ) {
					objlst = new ArrayList();
					HashMap map = (HashMap) obj;
					for ( Object o : map.entrySet().toArray() ) {
						objlst.add(o);
						
						// これは必ずEntrySetだね
						if ( ocls == null ) {
							ocls = o.getClass();
						}
					}
					n = 1;
				}
				else {
					objlst = new ArrayList();
					objlst.add(obj);
					
					// obj==nullの可能性がある
					ocls = cls;
				}
				
				if ( ! objlst.isEmpty() ) {
					for ( Object o : objlst ) {
						
						//Class ocls = o.getClass();
						String val = obj2str(o,ocls);
						
						if ( val != null ) {
							sb.append(key);
							if ( n >= 1 ) {
								// ArrayList or HashMap
								sb.append("[");
								sb.append(String.valueOf(n));
								sb.append("]");
								n++;
							}
							sb.append(SPMK_SEP);
							sb.append(val);
							sb.append(SPMK_LF);
						}
						else {
							sb.append(SPHD_UNS);
							sb.append(key);
							if ( n >= 1 ) {
								sb.append("[");
								sb.append(String.valueOf(n));
								sb.append("]");
								n++;
							}
							sb.append(" AS ");
							sb.append(ocls.getName());
							sb.append(SPMK_LF);
						}
					}
				}
				else {
					sb.append(key);
					sb.append("[0]");
					sb.append(SPMK_SEP);
					sb.append(SPMK_LF);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				return false;
			} 
		}
		
    	System.out.println(MSGID+"テキスト形式で保存します: "+envText);
    	if ( ! CommonUtils.write2file(envText, sb.toString()) ) {
        	System.err.println(ERRID+"保存に失敗しました");
        	return false;
    	}
		
		return true;
	}
	
	
	/*******************************************************************************
	 * 取得
	 ******************************************************************************/
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean load(String envText, Object root) {
		
    	if ( new File(envText).exists() ) {
        	System.out.println(MSGID+"テキスト形式で読み込みます: "+envText);
        	String buf = CommonUtils.read4file(envText, false);
        	if ( buf != null ) {
	        	
	    		Field[] fd = root.getClass().getDeclaredFields();
	    		
	        	int lineno = 0 ;
	        	for ( String str : buf.split(SPMK_LF) ) {
	        		
	        		++lineno;
	        		
	        		if ( str.startsWith(SPHD_MOD) ) {
	        			System.out.println(MSGID+str);
	        			continue;
	        		}
	        		
	        		if ( str.startsWith(SPMK_CM) || str.matches("^\\s*$") ) {
	        			continue;
	        		}
	        		
	        		String[] a = str.split(SPMK_SEP, 2);
	        		if ( a.length != 2 ) {
	        			System.err.println(ERRID+"不正な記述： "+envText+" at "+lineno+"行目 "+str);
	        			break;
	        		}
	        		
	        		Field fx = null;
	        		for ( Field f : fd ) {
        				if ( f.getName().equals(a[0].replaceFirst("\\[\\d+\\]$","")) ) {
        					fx = f;
        					break;
        				}
	        		}
	        		if ( fx == null ) {
	        			System.err.println(ERRID+"不正な記述： "+envText+" at "+lineno+"行目 "+str);
	        			break;
	        		}
	        		
    				fx.setAccessible(true);
    				
					if ( Modifier.isFinal(fx.getModifiers()) ) {
						continue;
					}
					if ( Modifier.isStatic(fx.getModifiers()) ) {
						continue;
					}
					
					if ( fx.getAnnotation(Deprecated.class) != null ) {
						System.out.println(MSGID+SPHD_DEP+fx.getName());
						break;
					}
					
					Class cls = fx.getType();
					
					try {
						if ( cls == ArrayList.class ) {
							ArrayList list = (ArrayList) fx.get(root);
							if ( fx.get(root) == null ) {
								System.out.println(ERRID+"初期化されていないフィールド: "+envText+" at "+lineno+"行目 ("+cls.getName()+") "+str);
								break;
							}
							
							if ( a[0].endsWith("[0]") ) {
								// no element.
								list.clear();
							}
							else {
								if ( a[0].endsWith("[1]") ) {
									// newした時のデフォルト値が入っているからリセットじゃー
									list.clear();
								}
								ParameterizedType paramType = (ParameterizedType) fx.getGenericType();
								Class ocls = (Class) paramType.getActualTypeArguments()[0];
								Object obj = str2obj(a[1],ocls);
								list.add(obj);
							}
						}
						else if ( cls == HashMap.class ) {
							HashMap map = (HashMap) fx.get(root);
							if ( fx.get(root) == null ) {
								System.out.println(ERRID+"初期化されていないフィールド: "+envText+" at "+lineno+"行目 ("+cls.getName()+") "+str);
								break;
							}
							String[] b = a[1].split(Pattern.quote(SPCH_LF),2);
							if ( b.length != 2 ) {
			        			System.err.println(ERRID+"不正な記述： "+envText+" at "+lineno+"行目 "+str);
			        			break;
							}
							
							if ( a[0].endsWith("[0]") ) {
								// no element.
								map.clear();
							}
							else {
								if ( a[0].endsWith("[1]") ) {
									// newした時のデフォルト値が入っているからリセットじゃー
									map.clear();
								}
								ParameterizedType paramType = (ParameterizedType) fx.getGenericType();
								Class kcls = (Class) paramType.getActualTypeArguments()[0];
								Class vcls = (Class) paramType.getActualTypeArguments()[1];
								Object k = str2obj(b[0],kcls);
								Object v = str2obj(b[1],vcls);
								map.put(k,v);
							}
						}
						else {
							Object obj = str2obj(a[1],cls);
							fx.set(root, obj);
						}
					}
					catch (UnsupportedOperationException e) {
						System.out.println(ERRID+e.getMessage()+": "+envText+" at "+lineno+"行目 ("+cls.getName()+") "+str);
					}
					catch (Exception e) {
						// エラー項目はスキップする
						System.out.println(ERRID+e.getMessage()+": "+envText+" at "+lineno+"行目 ("+cls.getName()+") "+str);
						e.printStackTrace();
					}
	        	}
        	}
	        
        	return true;
    	}
		
		return false;
	}
	
	
	/*******************************************************************************
	 * 保存（部品）
	 ******************************************************************************/
	
	@SuppressWarnings("rawtypes")
	private static String obj2str(Object obj, Class cls) {
		if ( cls == String.class ) {
			return(obj == null ? SPCH_NULL : ((String) obj).replaceAll(SPMK_LF, SPCH_LF));
		}
		else if ( cls == int.class || cls == Integer.class ) {
			return(obj == null ? SPCH_NULL : String.valueOf((Integer) obj));
		}
		else if ( cls == float.class || cls == Float.class ) {
			return(obj == null ? SPCH_NULL : String.valueOf((Float) obj));
		}
		else if ( cls == boolean.class || cls == Boolean.class ) {
			return(obj == null ? SPCH_NULL : String.valueOf((Boolean) obj));
		}
		else if ( cls == Rectangle.class ) {
			Rectangle ra = (Rectangle) obj;
			return(obj == null ? SPCH_NULL : String.format("%d,%d,%d,%d",ra.x,ra.y,ra.width,ra.height));
		}
		else if ( cls == Color.class ) {
			return(obj == null ? SPCH_NULL : CommonUtils.color2str((Color) obj));
		}
		else if ( cls == DblClkCmd.class ) {
			return(obj == null ? SPCH_NULL : String.valueOf(((DblClkCmd) obj).getId()));
		}
		else if ( cls == SnapshotFmt.class ) {
			return(obj == null ? SPCH_NULL : String.valueOf(((SnapshotFmt) obj).getId()));
		}
		else if ( cls == AAMode.class ) {
			return(obj == null ? SPCH_NULL : String.valueOf(((AAMode) obj).getId()));
		}
		else if ( cls == UpdateOn.class ) {
			return(obj == null ? SPCH_NULL : String.valueOf(((UpdateOn) obj).getId()));
		}
		else if ( cls == FontStyle.class ) {
			return(obj == null ? SPCH_NULL : String.valueOf(((FontStyle) obj).getId()));
		}
		else if ( cls == TextValueSet.class ) {
			TextValueSet t = (TextValueSet) obj;
			return(obj == null ? SPCH_NULL : t.getText()+SPCH_LF+t.getValue());
		}
		else if ( cls == ProgOption.class ) {
			return(obj == null ? SPCH_NULL : ((ProgOption) obj).toString());
		}
		else if ( obj instanceof Entry ) {
			Entry t = (Entry) obj;
			String k = obj2str(t.getKey(), t.getKey().getClass());
			String v = obj2str(t.getValue(), t.getValue().getClass());
			return(obj == null ? SPCH_NULL : k+SPCH_LF+v);
		}
		
		return null;
	}
	
	
	/*******************************************************************************
	 * 取得（部品）
	 ******************************************************************************/
	
	@SuppressWarnings("rawtypes")
	private static Object str2obj(String str, Class cls) throws UnsupportedOperationException {
		if ( cls == String.class ) {
			return(str.equals(SPCH_NULL) ? null : str.replaceAll(SPCH_LF, SPMK_LF));
		}
		else if ( cls == int.class || cls == Integer.class ) {
			try {
				return(str.equals(SPCH_NULL) ? null : Integer.valueOf(str));
			}
			catch ( NumberFormatException e ) {
				throw new UnsupportedOperationException("数値に変換できない");
			}
		}
		else if ( cls == float.class || cls == Float.class ) {
			try {
				return(str.equals(SPCH_NULL) ? null : Float.valueOf(str));
			}
			catch ( NumberFormatException e ) {
				throw new UnsupportedOperationException("数値に変換できない");
			}
		}
		else if ( cls == boolean.class || cls == Boolean.class ) {
			return(str.equals(SPCH_NULL) ? null : Boolean.valueOf(str));
		}
		else if ( cls == Rectangle.class ) {
			try {
				String[] a = str.split(",");
				if ( a.length == 4 ) {
					return(str.equals(SPCH_NULL) ? null : new Rectangle(Integer.valueOf(a[0]),Integer.valueOf(a[1]),Integer.valueOf(a[2]),Integer.valueOf(a[3])));
				}
				else {
					throw new UnsupportedOperationException("変換できない");
				}
			}
			catch ( NumberFormatException e ) {
				throw new UnsupportedOperationException("数値に変換できない");
			}
		}
		else if ( cls == Color.class ) {
			Color c = CommonUtils.str2color(str);
			if ( c != null ) {
				return(c);
			}
			else {
				throw new UnsupportedOperationException("色に変換できない ");
			}
		}
		else if ( cls == DblClkCmd.class ) {
			DblClkCmd dcc = DblClkCmd.get(str);
			if ( dcc != null ) {
				return(dcc);
			}
			else {
				throw new UnsupportedOperationException("変換できない");
			}
		}
		else if ( cls == SnapshotFmt.class ) {
			SnapshotFmt sf = SnapshotFmt.get(str);
			if ( sf != null ) {
				return(sf);
			}
			else {
				throw new UnsupportedOperationException("変換できない");
			}
		}
		else if ( cls == AAMode.class ) {
			AAMode aam = AAMode.get(str);
			if ( aam != null ) {
				return(aam);
			}
			else {
				throw new UnsupportedOperationException("変換できない");
			}
		}
		else if ( cls == UpdateOn.class ) {
			UpdateOn uo = UpdateOn.get(str);
			if ( uo != null ) {
				return(uo);
			}
			else {
				throw new UnsupportedOperationException(ERRID+"変換できない");
			}
		}
		else if ( cls == FontStyle.class ) {
			FontStyle fs = FontStyle.get(str);
			if ( fs != null ) {
				return(fs);
			}
			else {
				throw new UnsupportedOperationException("変換できない");
			}
		}
		else if ( cls == TextValueSet.class ) {
			String a[] = str.split(Pattern.quote(SPCH_LF),2);
			if ( a.length == 2 ) {
				TextValueSet t = new TextValueSet();
				t.setText(a[0]);
				t.setValue(a[1]);
				return (t);
			}
			else {
				throw new UnsupportedOperationException("変換できない");
			}
		}
		else if ( cls == ProgOption.class ) {
			for ( ProgOption po : ProgOption.values() ) {
				if ( po.toString().equals(str) ) {
					return po;
				}
			}
			throw new UnsupportedOperationException("変換できない");
		}
		
		throw new UnsupportedOperationException("未対応の項目");
	}

}
