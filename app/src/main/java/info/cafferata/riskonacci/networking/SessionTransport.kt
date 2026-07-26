package info.cafferata.riskonacci.networking

/**
 * Abstraction over "how devices in a room talk to each other." Firebase
 * is the only implementation (`FirebaseSessionTransport`) — same
 * Firestore schema as the iOS app, so a room works across platforms.
 */
interface SessionTransport {
    var onReceive: ((SessionMessage, String) -> Unit)?
    var onPeerConnected: ((String, String) -> Unit)?
    var onPeerDisconnected: ((String) -> Unit)?

    /** Stable id for the local device's own participant. */
    val localParticipantId: String

    fun startHosting(roomId: String, nickname: String)
    fun join(roomId: String, nickname: String)
    fun send(message: SessionMessage)
    fun stop()
}
