package epgdump;

/**
 * 短形式イベント記述子 短形式イベント記述子はイベント名およびそのイベントの短い記述をテキスト形式で表す。
 */
public class SEVTdesc {

    /**
     * descriptor_tag(記述子タグ):記述子タグは8 ビットのフィールドで、各記述子を識別する。 このタイプは0x4D固定
     */
    int descriptor_tag;

    /**
     * descriptor_length(記述子長):記述子長は8 ビットのフィールドで、このフィールドの
     * 直後に続く記述子のデータ部分の全バイト長を規定する。
     */
    int descriptor_length;
    /**
     * ISO_639_language_code(言語コード):この24 ビットのフィールドは、後続の文字情報フィールドの言語をISO
     * 639-2(22)に規定されるアルファベット3 文字コードで表す。 各文字はISO 8859-1(24)に従って8
     * ビットで符号化され、その順で24 ビットフィールドに挿入される。 例: 日本語はアルファベット3
     * 文字コードで「jpn」であり、次のように符号化される。 「0110 1010 0111 0000 0110 1110」
     */
    byte[] ISO_639_language_code = new byte[3];
    /**
     * event_name_length(番組名長):この8 ビットのフィールドは、後続の番組名のバイト長を表す。
     */
    int event_name_length;
    /**
     * event_name_char(番組名):これは8 ビットのフィールドである。一連の文字情報フィールドは、番組名を表す。
     * 文字情報の符号化に関しては、付録A を参照。
     */
    byte[] event_name = new byte[Util.MAXSECLEN];
    /**
     * text_length(番組記述長):この8 ビットのフィールドは、後続の番組記述のバイト長を表す。
     */
    int text_length;
    /**
     * text_char(番組記述):これは8 ビットのフィールドである。一連の文字情報フィールド
     * は番組の説明を記述する。文字情報の符号化に関しては、付録A を参照。
     */
    byte[] text = new byte[Util.MAXSECLEN];

}
