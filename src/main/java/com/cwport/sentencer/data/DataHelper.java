package com.cwport.sentencer.data;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.util.Log;

/**
 * Created by isayev on 26.02.14.
 */
public class DataHelper {
    public static final String TAG = DataHelper.class.getSimpleName();

    public static final String EXTRA_LESSON_TITLE = "lesson_title";
    public static final String EXTRA_LESSON_INDEX = "lesson_index";
    public static final String EXTRA_LESSON_SOURCE = "lesson_source";
    public static final String EXTRA_LESSON_MODE = "lesson_mode";

    public static final String PARAM_LESSON_ID = "lesson_id";
    public static final String PARAM_LESSON_SOURCE = "lesson_source";
    public static final String PARAM_LESSON = "currentLesson";
    public static final String PARAM_CARD_INDEX = "cardIndex";
    public static final String PARAM_FLIP = "flip";
    public static final String PARAM_SHUFFLED = "shuffled";
    public static final String PARAM_SHOWMARKED = "showmarked";
    public static final String PARAM_SHOWBACKFIRST = "showbackfirst";
    public static final String PARAM_MARKED_CARDS = "markedcards";
    public static final String PREF_PREFIX = "com.cwport.sentencer.";

    public static final String QUERY_PARAM_FILE = "file";

    /**
     * Use this function to generate MD5 hash for a Card object
     * @param s String to be hashed by MD5
     * @return MD5 hash
     */
    public static final String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes("UTF-8"));
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (UnsupportedEncodingException ue) {
            Log.e(TAG, ue.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.getMessage());
        }
        return "";
    }

    public static String userLessonFileName(URI uriFile) {
        String fileName = new String();
        String query = uriFile.getQuery();
        if(query != null) {
            String[] pairs = query.split("&");
            try {
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    String param = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                    String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                    if (param == QUERY_PARAM_FILE) fileName = value;
                }
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            // try to get file name from path
            String path = uriFile.getPath();
            int lastIdx = path.lastIndexOf("/");
            fileName = path.substring(lastIdx + 1);
        }
        return fileName;
    }

}
