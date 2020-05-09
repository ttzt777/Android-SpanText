package cc.bear3.weight.textview.collapsed;

import android.text.TextUtils;

import androidx.annotation.NonNull;

public class StringUtil {

    /**
     * 当出现字符限制的时候，emoj表情为四个字节，强制去除两个字节后会乱码，因此去除前两个字节
     * @param charSequence
     * @return
     */
    public static CharSequence removeLastHalfEmoj(@NonNull CharSequence charSequence) {
        if (TextUtils.isEmpty(charSequence)) {
            return charSequence;
        }

        char last = charSequence.charAt(charSequence.length() - 1);
        if (last == 0xd83d || last == 0xd83c || last == 0xd83e) {
            charSequence = charSequence.subSequence(0, charSequence.length() - 1);
        }

        return charSequence;
    }

    /**
     * 当出现字符限制的时候，emoj表情为四个字节，强制去除两个字节后会乱码，因此去除前两个字节
     * @param string
     * @return
     */
    public static String removeLastHalfEmoj(@NonNull String string) {
        if (TextUtils.isEmpty(string)) {
            return string;
        }

        char last = string.charAt(string.length() - 1);
        if (last == 0xd83d || last == 0xd83c || last == 0xd83e) {
            string = string.substring(0, string.length() - 1);
        }

        return string;
    }
}
