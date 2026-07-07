# Gson deserialisiert die Radio-Browser-Antworten per Reflection anhand der
# Feldnamen. Ohne Keep-Rules würde R8 diese Felder umbenennen/entfernen und
# alle API-Antworten kämen als leere/null Objekte zurück.
-keep class de.rittitservice.frequenzia.data.RadioStation { *; }
-keep class de.rittitservice.frequenzia.data.Country { *; }
-keepattributes Signature
-keepattributes *Annotation*
