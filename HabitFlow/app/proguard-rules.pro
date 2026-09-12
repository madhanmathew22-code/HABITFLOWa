# Add project specific ProGuard rules here.
# Room, Hilt, and Compose all ship consumer ProGuard rules, so this file
# is mostly a place to add app-specific keep rules as they come up.

-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <fields>;
}
