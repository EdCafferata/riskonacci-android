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

    /** Fired once, on the main thread, as soon as the local device's
     * anonymous sign-in completes and [localParticipantId] is known.
     * Lets the room view model show the local player with the correct id
     * instead of the empty placeholder it would otherwise read back
     * synchronously (sign-in is asynchronous). */
    var onLocalParticipantReady: ((String) -> Unit)?

    /** Fired the first time the room roster confirms our own presence —
     * proof that reads and writes to the backend actually work. The UI
     * uses this to move from "connecting" to "connected". */
    var onConnected: (() -> Unit)?

    /** Fired when the transport can't reach the backend at all (no
     * network, sign-in failed). Lets the UI show a clear error instead of
     * an empty room that looks frozen. */
    var onConnectFailed: (() -> Unit)?

    /** Stable id for the local device's own participant. */
    val localParticipantId: String

    fun startHosting(roomId: String, nickname: String)
    fun join(roomId: String, nickname: String)
    fun send(message: SessionMessage)
    fun stop()
}
