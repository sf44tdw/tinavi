package epgdump;

public class EIThead {
    /**table_id(テーブル識別):8bit
     * 0x4E EIT(自ストリームの現在と次の番組)
     * 0x4F EIT(他ストリームの現在と次の番組) 
     * 0x50 - 0x5F EIT(自ストリームの8日以内の番組もしくは自ストリームの8日以降の番組) 
     * 0x60 - 0x6F EIT(他ストリームの8日以内の番組もしくは他ストリームの8日以降の番組)*/
	byte table_id;
        /**section_syntax_indicator(セクションシンタクス指示):1bit。常時1*/
	int section_syntax_indicator;
        /**1bit*/
	int reserved_future_use;
        /**2bit*/
	int reserved1;
	int section_length;
	int service_id;
	int reserved2;
	int version_number;
	int current_next_indicator;
	int section_number;
	int last_section_number;
	int transport_stream_id;
	int original_network_id;
	int segment_last_section_number;
	int last_table_id;

}
