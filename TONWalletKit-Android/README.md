# TON WalletKit Android

Kotlin library providing TON wallet capabilities for Android.

- Minimum: Android 8.0 (API 26)
- Requires up-to-date Android System WebView (tested with 138.0.7204.179+)

## Installation

#### Gradle

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("org.ton:walletkit:+") // Check latest at https://central.sonatype.com/artifact/org.ton/walletkit
}
```

## Quick start

#### Initialize TONWalletKit:
```kotlin
import io.ton.walletkit.ITONWalletKit
import io.ton.walletkit.config.TONWalletKitConfiguration
import io.ton.walletkit.model.TONNetwork

// Create configuration that fits your app
val configuration = TONWalletKitConfiguration(
    network = TONNetwork.TESTNET,
    walletManifest = TONWalletKitConfiguration.Manifest(
        name = "MyTONWallet",
        appName = "MyTONWalletIdentifier",
        imageUrl = "https://example.com/image.png",
        aboutUrl = "https://example.com/about",
        universalLink = "https://example.com/universal-link",
        bridgeUrl = "https://bridge.tonapi.io/bridge"
    ),
    // Additional configuration options as needed
)

// Initialize the kit
val kit = ITONWalletKit.initialize(
    context = context,
    config = configuration
)
```

#### Add events listener:
```kotlin
import io.ton.walletkit.listener.TONBridgeEventsHandler
import io.ton.walletkit.event.TONWalletKitEvent

class MyAppEventsListener : TONBridgeEventsHandler {
    override suspend fun handle(event: TONWalletKitEvent) {
        println("TONWalletKit event: $event")
    }
}

val events = MyAppEventsListener()
kit.addEventsHandler(events)
```

#### Create and add a v5r1 wallet using mnemonic:
```kotlin
import io.ton.walletkit.model.TONNetwork

// Generate a new mnemonic
val mnemonic = kit.createTonMnemonic()

// 3-step wallet creation pattern:
// Step 1: Create signer from mnemonic
val signer = kit.createSignerFromMnemonic(mnemonic)

// Step 2: Create V5R1 adapter
val adapter = kit.createV5R1Adapter(
    signer = signer,
    network = TONNetwork.TESTNET
)

// Step 3: Add wallet
val wallet = kit.addWallet(adapter)
```

#### Read wallet address and balance:
```kotlin
val address = wallet.address(testnet = true)
val balance = wallet.balance()

println("Address: $address")
println("Balance: $balance")
```

#### Add wallet with external signer (e.g., hardware wallet):
```kotlin
import io.ton.walletkit.model.WalletSigner
import io.ton.walletkit.model.TONHex

// Create custom signer implementation
val customSigner = object : WalletSigner {
    // Return the public key from your hardware wallet / external signer.
    override fun publicKey(): TONHex = TONHex.fromData(hardwareWalletPublicKey)

    override suspend fun sign(data: ByteArray): TONHex {
        // Forward `data` to your external signer, show a confirmation
        // dialog if needed, and return the resulting signature.
        return TONHex.fromData(externalSignature)
    }
}

// Step 1: Create signer from custom implementation
val signer = kit.createSignerFromCustom(customSigner)

// Step 2: Create adapter
val adapter = kit.createV4R2Adapter(
    signer = signer,
    network = TONNetwork.MAINNET
)

// Step 3: Add wallet
val wallet = kit.addWallet(adapter)
```

#### Get all wallets:
```kotlin
val wallets = kit.getWallets()
```

#### Remove wallet:
```kotlin
kit.removeWallet(wallet.identifier())
```

#### Clean up when done:
```kotlin
// Remove all event handlers and release resources
kit.destroy()
```

#### Notes
- For persistent storage, configure `storage` in `TONWalletKitConfiguration`.
- To add wallets using a secret key, use `createSignerFromSecretKey()`.
- [Demo app](../AndroidDemo) shows a more complete integration.
