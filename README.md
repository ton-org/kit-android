# TON WalletKit Android

TON blockchain wallet SDK for Android.

## Structure

- **[TONWalletKit-Android](TONWalletKit-Android/)** - SDK library (Kotlin/Java)
- **[AndroidDemo](AndroidDemo/)** - Demo application

## Quick Start

See [TONWalletKit-Android/README.md](TONWalletKit-Android/README.md) for SDK documentation and usage examples.

## API Keys

Add to `AndroidDemo/local.properties` (gitignored):

```properties
# TonCenter API key — used by the TonCenter streaming provider
walletkitToncenterApiKey=...

# TonAPI key — used by DemoApiConfig (staking, balance/masterchain queries)
walletkitTonApiKey=...

# Optional per-network TonAPI keys used by the demo's TestAPIClient
tonApiMainnetKey=...
tonApiTestnetKey=...
tetraApiKey=...
```

Keys are optional; features that require them are disabled or fall back to unauthenticated requests when absent.

## Rebuilding the SDK

Use [Scripts/rebuild-sdk.sh](Scripts/rebuild-sdk.sh) when you've made changes to the TypeScript bridge in the [walletkit](https://github.com/ton-blockchain/walletkit) repo and need to propagate them into the Android SDK.

The script runs five steps:
1. Builds the `walletkit-android-bridge` Vite bundle (in the `kit` repo)
2. Copies the bundle into `dist-android/`
3. Regenerates the OpenAPI Kotlin models — **only with `--regen-models`** (skipped otherwise)
4. Builds the SDK AAR and copies it into the demo's `libs`
5. Installs the demo app on a connected device/emulator — **only with `--install-demo`** (skipped otherwise)

**Prerequisites:** `pnpm`, `npx`, and the Android SDK. A connected device or emulator is needed only when passing `--install-demo`.

**Setup:** Place the `kit` and `kit-android` repos as siblings in the same directory, or set `KIT_DIR` to the walletkit repo path.

```sh
# Standard usage (builds the bundle + AAR; does NOT install the demo)
./Scripts/rebuild-sdk.sh

# Also build and install the demo app on a connected device/emulator
./Scripts/rebuild-sdk.sh --install-demo

# Also regenerate OpenAPI Kotlin models (requires openapi-generator)
./Scripts/rebuild-sdk.sh --regen-models

# Custom kit location
KIT_DIR=/path/to/walletkit ./Scripts/rebuild-sdk.sh
```
