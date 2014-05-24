package com.cwport.sentencer.media;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by isayev on 10.03.14.
 * Use Google Text-to-Speech API
 * at https://gist.github.com/alotaiba/1728771
 * Example: http://translate.google.com/translate_tts?ie=UTF-8&q=Hello%20World&tl=en-us
 * Speech API supports only 100 characters, If the text is more than 100 characters it gives an error.
 * The text should be split by 100-chars chunks
 * http://stackoverflow.com/questions/14257598/what-are-language-codes-for-voice-recognition-languages-in-chromes-implementati
 * Supported languages: https://developers.google.com/translate/v2/using_rest#language-params
 * Example:
 * https://translate.google.com/translate_tts?ie=UTF-8&q=%D1%87%D1%82%D0%BE%20%D1%82%D0%B2%D0%BE%D1%8E%20%D0%BA%D0%BE%D0%BC%D0%BD%D0%B0%D1%82%D1%83%20%D0%B2%D1%81%D0%B5%D0%B3%D0%B4%D0%B0%20%D1%83%D0%B1%D0%B8%D1%80%D0%B0%D1%8E%D1%82%20%D0%BA%20%D0%BF%D1%80%D0%B8%D1%85%D0%BE%D0%B4%D1%83%20%D1%82%D0%B2%D0%BE%D0%B8%D1%85%20%D0%B4%D1%80%D1%83%D0%B7%D0%B5%D0%B9.
 * &tl=ru&total=2&idx=1&textlen=55&client=t&prev=input
 */
public class TextToSpeech {
    private static final String TAG = TextToSpeech.class.getSimpleName();
    public static final String TTS_URL = "http://translate.google.com/translate_tts";
    public static final int TTS_TEXT_MAXLEN = 96;
    public static final String supportedLocales[] = {
            "en_US",
            "sv_SE", // + Swedish
            "cs", // + Czech
            "nl_NL", // + Dutch
            "fi", //    + Finnish
            "fr_FR", // + French
            "gl", //    + Galician
            "de_DE", // + German
            "pt_PT", // + Portuguese
            "ro_RO", // + Romanian
            "ru_RU", // + Russian
            "sr_SP",  //+ Serbian
            "sk", //    + Slovak
            "es_ES", // All Spanish
            "cs", // + Czech
            "nl_NL", // + Dutch
            "fi", //    + Finnish
            "fr_FR", // + French
            "gl", //    + Galician
            "de_DE", // + German
            "he", //     + Hebrew
            "hu", //     + Hungarian
            "is", //     + Icelandic
            "it_IT" //  + Italian
        /*
            + English (Australia) en-AU
            +? English (Canada) en-CA
            + English (India) en-IN
            + English (New Zealand) en-NZ
            + English (South Africa) en-ZA
            + English(UK) en-GB
            + English(US) en-US
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
            .+ Arabic (Yemen) ar-YE
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
            +? Pig Latin xx-piglatin
            "pt_PT", //    + Portuguese
            "pt_BR", // .+ Portuguese (brasil)
            "ro_RO", //     + Romanian
            "ru_RU", //     + Russian
            "sr_SP",  //   + Serbian
            "sk", //     + Slovak
            "es_ES", // All Spanish
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
            "sv_SE", //     + Swedish
            "tr", //     + Turkish
            "zu" //     + Zulu
        */
    };

    /**
     * Convert text to a list of Google text-to-speech API urls
     * @param text
     * @param locale
     * @return List of urls
     * @throws UnsupportedEncodingException
     */
    public static ArrayList<String> textToSpeech(String text, String locale)
            throws UnsupportedEncodingException {
        ArrayList<String> urls = new ArrayList<String>();
        StringBuilder url = new StringBuilder();
        ArrayList<String> chunks = new ArrayList<String>();
        if(text.length() > TTS_TEXT_MAXLEN) {
            StringBuilder chunk = new StringBuilder();
            String delim = ",|\\s+|,\\s*|\\.\\s*|\\!\\s*|\\?\\s*|\\/\\s*|\\\\s*";
            String[] tokens = text.split(delim);
            if(tokens.length > 0) {
                int i = 0;
                while (true) {
                    if (tokens[i].length() > TTS_TEXT_MAXLEN) {
                        chunk.append(tokens[i].substring(0, TTS_TEXT_MAXLEN));
                        url = url.append(buildTtsUrl(text, locale).toString());
                        urls.add(url.toString());
                        break; // crop the token and leave a loop because of too long strange string w/o delimeters
                    } else {
                        if (chunk.length() + tokens[i].length() < TTS_TEXT_MAXLEN) {
                            chunk.append(tokens[i]).append(" "); // append token and one space
                            i++;
                        } else {
                            chunks.add(chunk.toString());
                            url = url.append(buildTtsUrl(text, locale).toString());
                            urls.add(url.toString());
                            chunk = new StringBuilder();
                        }
                    }
                    if(i > tokens.length - 1) {
                        if(chunk.length() > 0) {
                            chunks.add(chunk.toString());
                            url = url.append(buildTtsUrl(text, locale).toString());
                            urls.add(url.toString());
                        }
                        break;
                    }
                }
            }

        } else {
            url = url.append(buildTtsUrl(text, locale).toString());
            urls.add(url.toString());
        }

        return urls;
    }

    public static StringBuilder buildTtsUrl(String text, String locale)
            throws UnsupportedEncodingException {
        return (new StringBuilder()).append(TTS_URL)
                .append("?ie=UTF-8&tl=")
                .append(locale)
                .append("&q=")
                .append(java.net.URLEncoder.encode(text, "UTF-8"));
    }

    public static boolean localeSupported(String locale) {
        boolean found;
        found = Arrays.asList(supportedLocales).contains(locale);
        return found;
    }

}
