# Ghostty upstream integration

Ghostty is fetched source code, not a Git submodule or a vendored tree. The
repository and immutable revision are declared in `build.gradle`.
`prepareNativeSources` checks out that revision under `build/native-sources`,
and `buildGhosttyAndroid` clones it into the terminal emulator build directory,
applies `native/patches/ghostty-android.patch`, and builds `libghostty-vt` for
the configured Android ABIs. Keep the patch limited to C APIs needed by the
Android JNI and Vulkan renderer.

## 2026-09-12 sync

The pin was advanced from `492300cad104195411d12217dd22f1cd05f31376` to
`e2e53f861482e080bf45054ba49ef471f9849937`, the tip of upstream `main` when
the sync was performed.

The 43-commit range contains these changes relevant to this application:

- libghostty-vt now returns C-safe null pointers for empty allocated outputs.
  The existing JNI callers already accept an empty pointer/length pair, so the
  fix is inherited without a new binding.
- POSIX TinyIo and the C API/build integration were refactored. Android uses
  this path and gains the upstream fixes without changing its host contract.
- Compressed terminal pages clear only the written memory before returning
  pooled scratch storage, reducing unnecessary memory work for scrollback.

No new Android-facing feature binding was added for this range. The Kitty
graphics file-medium hardening is Windows-specific (UNC, device namespace,
and reserved device-name rejection); Android already uses Ghostty's POSIX path
validation, so copying the Windows policy would reject valid Android/Termux
paths without adding protection. The macOS display-link deadlock fix, Windows
page-memory reclamation and TinyIo implementation, GTK/macOS build changes,
translations, color-scheme data, and fontconfig update are desktop or unused
by the `libghostty-vt` Android build and are intentionally not adapted.

For future updates, compare the pinned revision with upstream `main`, check the
Android patch with `git apply --check`, then run the native build for every ABI,
the Android unit tests, and an APK build. Update this section when the range
introduces a host-facing terminal API or behavior decision.
