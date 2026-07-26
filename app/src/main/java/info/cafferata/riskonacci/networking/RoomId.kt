package info.cafferata.riskonacci.networking

/**
 * A short, shareable room code — always 5 characters, letters + digits,
 * regardless of how many rooms have ever existed. Mirrors the iOS app's
 * `RoomID.swift` so codes generated on either platform are always valid
 * on the other.
 */
object RoomId {
    /** O/0 and I/1 excluded — the pair people misread most often when
     * reading a code off someone else's screen. */
    private val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toList()

    fun generate(): String = (0 until 5).map { alphabet.random() }.joinToString("")

    fun isValid(code: String): Boolean {
        val normalized = code.uppercase()
        if (normalized.length != 5) return false
        return normalized.all { alphabet.contains(it) }
    }
}
