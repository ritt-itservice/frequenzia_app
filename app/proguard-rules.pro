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
