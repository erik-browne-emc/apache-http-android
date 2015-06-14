# apache-http-android
Android HTTP client repackaged and fixed up for Android, useful for Android M where it's been removed from the system

** Started with:

Official Apache client port for Android

https://hc.apache.org/httpcomponents-client-4.3.x/android-port.html

** Packaging changes:

New package name, original.apache.http

Reverted the renaming of some classes to have HC4 suffix (details as link above), now class names are as they come from Apache.

Removed individual files from Apache-http-core (included in the official port and mixed with Apache-http-client files there), added httcomponents-core 4.3.4 instead, as a separate build unit.

* Rationale:

Makes the library fully independent of Android code.

The official Android port referenced some classes from Apache HTTP Core which are still present in the M preview (I guess), but who knows, they may disappear in later M builds.

I also wasn't happy with mixing Apache HTTP Client 4.3.5 with older versions of Apache HTTP Core classes from Android (which may even be changed by some clever device manufacturer...)

As a result, the library is self-contained and is more predictable at runtime on all Android versions, not just M.

** Fixed Basic auth, broken in official release

Was brokenBasic auth, broken because of a typo when changing [Apache Base64 to Android Base64]( https://github.com/kmansoft/apache-http-android/commit/1f748ecc3ef765deea97fa2d86aa4db8d40b0342).

** Logging can now be routed through callbacks

In the official release, Apache Commons logging has been replaced with direct calls to Android Log.*.

That's great, but in my app, I need to be able to log to a file (for remote debugging).

Now an application can supply callbacks (one for wire level logging, one for all the reset) and write log output to a file, or whatever.
