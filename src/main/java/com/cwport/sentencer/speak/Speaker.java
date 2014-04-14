package com.cwport.sentencer.speak;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by isayev on 10.03.14.
 * Use Google Text-to-Speech API
 * at https://gist.github.com/alotaiba/1728771
 * Example: http://translate.google.com/translate_tts?ie=UTF-8&q=Hello%20World&tl=en-us
 */
public class Speaker {
    private static final String TAG = Speaker.class.getSimpleName();
    // text-to-speech API language support according to
    // http://stackoverflow.com/questions/14257598/what-are-language-codes-for-voice-recognition-languages-in-chromes-implementati
    public static final String supportedLocales[] = {
            "en_US",
        /*
            + English (Australia) en-AU
            +? English (Canada) en-CA
            + English (India) en-IN
            + English (New Zealand) en-NZ
            + English (South Africa) en-ZA
            + English(UK) en-GB
            + English(US) en-US
        */
            "af", // + Afrikaans
            "eu", // + Basque
            "bg", // + Bulgarian
            "ca", // + Catalan
            "ar", /* all Arabic
            "ar-EG", + Arabic (Egypt)
            +? Arabic (Jordan) ar-JO
            + Arabic (Kuwait) ar-KW
            +? Arabic (Lebanon) ar-LB
            + Arabic (Qatar) ar-QA
            + Arabic (UAE) ar-AE
            .+ Arabic (Morocco) ar-MA
            .+ Arabic (Iraq) ar-IQ
            .+ Arabic (Algeria) ar-DZ
            .+ Arabic (Bahrain) ar-BH
            .+ Arabic (Lybia) ar-LY
            .+ Arabic (Oman) ar-OM
            .+ Arabic (Saudi Arabia) ar-SA
            .+ Arabic (Tunisia) ar-TN
            .+ Arabic (Yemen) ar-YE */
            "cs", // + Czech
            "nl_NL", // + Dutch
            "fi", //    + Finnish
            "fr_FR", // + French
            "gl", //    + Galician
            "de_DE", // + German
            "he", //     + Hebrew
            "hu", //     + Hungarian
            "is", //     + Icelandic
            "it_IT", //     + Italian
            "id", //     + Indonesian
            "ja", //     + Japanese
            "ko", //     + Korean
            "la", //     + Latin
            "zh_CN", //     + Mandarin Chinese
            "zh_TW", //     + Traditional Taiwan
            "zh_CN", // ?     +? Simplified China
            "zh_HK", //     + Simplified Hong Kong
            "zh_yue", //     + Yue Chinese (Traditional Hong Kong)
            "ms_MY", //     + Malaysian
            "no_NO", //     + Norwegian
            "pl", //     + Polish
        /*    +? Pig Latin xx-piglatin */
            "pt_PT", //    + Portuguese
            "pt_BR", // .+ Portuguese (brasil)
            "ro_RO", //     + Romanian
            "ru_RU", //     + Russian
            "sr_SP",  //   + Serbian
            "sk", //     + Slovak
            "es", // All Spanish
        /*
            + Spanish (Argentina) es-AR
            + Spanish(Bolivia) es-BO
            +? Spanish( Chile) es-CL
            +? Spanish (Colombia) es-CO
            +? Spanish(Costa Rica) es-CR
            + Spanish(Dominican Republic) es-DO
            + Spanish(Ecuador) es-EC
            + Spanish(El Salvador) es-SV
            + Spanish(Guatemala) es-GT
            + Spanish(Honduras) es-HN
            + Spanish(Mexico) es-MX
            + Spanish(Nicaragua) es-NI
            + Spanish(Panama) es-PA
            + Spanish(Paraguay) es-PY
            + Spanish(Peru) es-PE
            + Spanish(Puerto Rico) es-PR
            + Spanish(Spain) es-ES
            + Spanish(US) es-US
            + Spanish(Uruguay) es-UY
            + Spanish(Venezuela) es-VE
        */
            "sv_SE", //     + Swedish
            "tr", //     + Turkish
            "zu" //     + Zulu
    };

    public static boolean localeSupported(String locale) {
        boolean found;
        found = Arrays.asList(supportedLocales).contains(locale);
        return found;
    }

}
