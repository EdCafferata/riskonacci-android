# Tip Jar — Google Play product setup

The tip jar code (`app/src/main/java/info/cafferata/riskonacci/billing/`) is complete and
the screen works, but the three tip products must **exist and be active** in the Google
Play Console before the tip options can appear. The app loads them by product ID at runtime.

> Verified 2026-10-25 via the Play Developer API: `oneTimeProducts/<id>.get` returns **404**
> for all three — they do **not** exist yet, so on the live app the tip jar currently shows
> only the "Rate" button and no tip options.

## The three products to create

| Product ID (exact, permanent) | Name            | Price (recommended) | Suggested description                                   |
| ----------------------------- | --------------- | ------------------- | ------------------------------------------------------- |
| `tip_espresso`                | Espresso        | € 0,99              | A small espresso-sized thank-you for Riskonacci.        |
| `tip_cornetto`                | Cornetto        | € 2,99              | A cornetto on the house — thanks for the support.       |
| `tip_aperitivo`               | Aperitivo       | € 4,99              | An aperitivo-level tip — grazie mille!                  |

Prices mirror the iOS app ($0.99 / $2.99 / $4.99). Adjust to taste; set the default price in €
and Google auto-converts the other currencies.

## Where / how (Play Console)

1. Play Console → select **Riskonacci** → **Monetize → Products → In-app products**
   (this account uses the new product model, so the menu may read **One-time products**).
2. **Create product** for each of the three rows above:
   - **Product ID**: type it **exactly** as in the table — lowercase, underscore.
     ⚠️ The ID is permanent and must match the app code character-for-character, or the
     product will not load. (Code: `TipProduct.kt`.)
   - **Name**: as in the table (shown in Google's purchase sheet).
   - **Description**: as in the table, or your own wording.
   - **Price**: set the default price in €; Google fills in the rest of the world.
3. **Activate** each product — the status must be **Active**, not Draft.

## Notes

- **Repeatable tips (consumable):** the app already calls `consumeAsync` after each purchase,
  so a user can tip again and again. You don't need a special console setting beyond creating
  a normal one-time product.
- **What the user sees:** the tip rows show the hardcoded labels (Espresso / Cornetto /
  Aperitivo) **plus the price from Play**. Once the three products are active and priced, the
  tip jar fills in automatically — **no app update needed**.
- The Play Developer API cannot *create* products (same limitation as iOS App Store Connect),
  so this is a manual web-console step.
- After activating, allow a few minutes to propagate; then the live app's tip jar should list
  the three options with their prices.
