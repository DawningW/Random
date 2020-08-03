package io.github.dawncraft.qingchenw.random.tts;

import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.TtsMode;

public interface IOfflineResourceConst {

    TtsMode DEFAULT_SDK_TTS_MODE = TtsMode.MIX;
    
    // f7 离线女声
    String VOICE_FEMALE = "F";
    
    // m15 离线男声
    String VOICE_MALE = "M";
    
    // yyjw 度逍遥
    String VOICE_DUXY = "X";
    
    // as 度丫丫
    String VOICE_DUYY = "Y";
    
    String TEXT_MODEL = "bd_etts_common_text_txt_all_mand_eng_middle_big_v3.4.2_20190710.dat";

    String VOICE_MALE_MODEL = "bd_etts_common_speech_m15_mand_eng_high_am-mgc_v3.6.0_20190117.dat";

    String VOICE_FEMALE_MODEL = "bd_etts_common_speech_f7_mand_eng_high_am-mgc_v3.6.0_20190117.dat";

    String VOICE_DUXY_MODEL = "bd_etts_common_speech_yyjw_mand_eng_high_am-mgc_v3.6.0_20190117.dat";

    String VOICE_DUYY_MODEL = "bd_etts_common_speech_as_mand_eng_high_am-mgc_v3.6.0_20190117.dat";

    String PARAM_SN_NAME = SpeechSynthesizer.PARAM_AUTH_SN;
}
