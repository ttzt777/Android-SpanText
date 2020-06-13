package cc.bear3.view.collapsedtextview;

import android.text.TextUtils;

import androidx.annotation.Nullable;

public class StringUtil {

    /**
     * 当出现字符限制的时候，emoj表情为四个字节，强制去除两个字节后会乱码，因此去除前两个字节
     * @param charSequence 待处理的字符串
     * @return 处理后的字符串
     */
    @Nullable
    public static CharSequence removeLastHalfEmoj(@Nullable CharSequence charSequence) {
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
     * @param string 待处理的字符串
     * @return 处理后的字符串
     */
    @Nullable
    public static String removeLastHalfEmoj(@Nullable String string) {
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
