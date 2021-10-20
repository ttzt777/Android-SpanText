package cc.bear3.span.kernel

/**
 *
 * @author TT
 * @since 2021-10-19
 */
val emojFirstCharArray = intArrayOf(0xd83d, 0xd83c, 0xd83c)

fun CharSequence?.removeLastHalfEmoj(): CharSequence {
    if (this.isNullOrEmpty()) {
        return ""
    }

    val lastChar = get(length - 1)
    var result: CharSequence = this
    for (element in emojFirstCharArray) {
        if (lastChar.code == element) {
            result = subSequence(0, length - 1)
            break
        }
    }

    return result
}