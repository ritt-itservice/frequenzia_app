# Gson deserialisiert die Radio-Browser-Antworten per Reflection anhand der
# Feldnamen. Ohne Keep-Rules würde R8 diese Felder umbenennen/entfernen und
# alle API-Antworten kämen als leere/null Objekte zurück.
-keep class de.rittitservice.frequenzia.data.RadioStation { *; }
-keep class de.rittitservice.frequenzia.data.Country { *; }
-keepattributes Signature
-keepattributes *Annotation*

# ConnectionDiagnostics loggt error::class.java.simpleName für die lokale
# Fehlerdiagnose (siehe de.rittitservice.frequenzia.diagnostics) – ohne diese
# Regel verkürzt R8 Exception-Klassen wie retrofit2.HttpException auf
# unlesbare Kurznamen (z. B. "st"), wodurch der Dump seinen Zweck verliert.
-keepnames class * extends java.lang.Throwable

# StationCache (de.rittitservice.frequenzia.data) nutzt Gsons
# TypeToken<List<RadioStation>>() für De-/Serialisierung. Ohne diese von
# Gson selbst empfohlenen Regeln wirft das im minifizierten Release-Build
# eine IllegalStateException beim Start ("TypeToken must be created with a
# type argument") – hat den App-Start auf einem echten Gerät gecrasht
# (2026-07-18, v1.8.0), weil Debug-Builds ohne R8-Minifizierung das Problem
# nicht zeigen.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
