package info.cafferata.riskonacci.networking.firebase

import com.google.firebase.firestore.PropertyName
import java.util.Date

/**
 * The single document every device in a room shares (`rooms/{roomId}`).
 * Field names are pinned with `@PropertyName` to match the iOS app's
 * `FirebaseRoomDocument.swift` exactly (Swift's Codable synthesizes
 * field names straight from its property names, e.g. `hostID` /
 * `isRevealed`) — Firestore is the shared wire format between platforms,
 * so both sides must agree on the literal field names, not just the
 * Kotlin/Swift-idiomatic ones.
 *
 * Needs a no-arg constructor for Firestore's POJO mapping, hence the
 * default values on every property.
 */
data class FirebaseRoomDocument(
    @get:PropertyName("hostID") @set:PropertyName("hostID")
    var hostId: String = "",

    var hostHeartbeatAt: Date = Date(),

    var deckRaw: String = "",

    var twoRoundsEnabled: Boolean = true,

    @get:PropertyName("isRevealed") @set:PropertyName("isRevealed")
    var isRevealed: Boolean = false,

    var epoch: Int = 0,
) {
    companion object {
        const val COLLECTION = "rooms"
    }
}
