package info.cafferata.riskonacci.networking

/**
 * Deliberately just an id + a self-chosen nickname — no avatar/photo, so
 * nothing but a display name ever has to travel through Firebase. `id`
 * is Firebase Auth's own anonymous-user UID string, same as on iOS.
 */
data class SessionParticipant(
    val id: String,
    val nickname: String,
)
