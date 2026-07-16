# Favoriten: Suche + Alphabet-Gruppierung — Umsetzungsnotizen

**Status:** Entwurf, geplant für Q3 2026 (siehe [Roadmap](../../roadmap/)).
**Visueller Entwurf:** [index.html](index.html) (gleicher Ordner).

## Ausgangslage

`FavoritesScreen.kt` rendert aktuell eine flache, unsortierte `LazyColumn`
(Reihenfolge = Einfügereihenfolge in der DB, keine Suche, keine Gruppierung).
Bei vielen gespeicherten Sendern geht dadurch schnell der Überblick verloren.

## Kernidee (sollte in jedem Fall umgesetzt werden)

1. **Suchfeld** über der Liste, gleiche Optik/Verhalten wie das bestehende
   Suchfeld in `SearchScreen.kt` (`OutlinedTextField`, `RoundedCornerShape(28.dp)`,
   `surfaceVariant`-Hintergrund). Filtert lokal über die bereits geladenen
   `favorites` (kein Netzwerk-Call nötig, da alle Favoriten schon im Speicher
   sind) — einfaches `contains`-Filtern auf `station.name`, live bei jedem
   Tastendruck, kein Debounce nötig, da rein lokal.

2. **Gruppierung nach Anfangsbuchstabe** des Sendernamens (Großschreibung,
   `Locale.GERMAN`). Sender, deren Name nicht mit einem Buchstaben beginnt
   (Ziffern, Sonderzeichen wie im Mockup `102.7 KIIS FM`, `80s80s Radio`),
   landen in einem gemeinsamen `#`-Abschnitt, der vor `A` einsortiert wird.

3. **Sticky Section Header** pro Buchstabe. Compose Foundation bietet dafür
   `LazyColumn`'s natives `stickyHeader { ... }` (Teil von
   `androidx.compose.foundation.lazy`, keine neue Abhängigkeit nötig) —
   pro Buchstaben-Gruppe ein `stickyHeader` mit dem Buchstaben, darunter die
   `items(...)` dieser Gruppe mit der bestehenden `StationRow`.

## Optional (im Mockup als "Extra-Idee" markiert)

4. **A–Z-Schnellzugriff** als schmale Spalte am rechten Bildschirmrand,
   überlagert über die Liste. Antippen/Draggen eines Buchstabens scrollt die
   `LazyColumn` per `LazyListState.scrollToItem(index)` zum passenden
   Abschnitt. Buchstaben ohne vorhandene Favoriten werden abgeblendet
   dargestellt (nicht antippbar oder ohne Effekt).

   Das ist der aufwändigere Teil (eigene Gesten-Erkennung fürs Draggen über
   die Buchstaben, Index-Berechnung der Scroll-Position). Kann in einem
   zweiten Schritt nachgezogen werden, falls Punkt 1–3 allein nicht reichen.

## Betroffene Dateien

- `app/src/main/java/de/rittitservice/frequenzia/ui/FavoritesScreen.kt`
  (Hauptänderung: Suchfeld, Gruppierung, `stickyHeader`)
- Keine Änderungen an `StationRow` (aus `SearchScreen.kt`) nötig — wird
  unverändert wiederverwendet.
- Kein Datenbank-/DAO-Schema-Update nötig, da rein clientseitige
  Sortierung/Filterung der bereits geladenen Favoriten-Liste.

## Nicht Teil dieses Entwurfs

- Keine Änderung an `RecentlyPlayedScreen.kt` oder `SearchScreen.kt` — die
  Gruppierung betrifft ausschließlich die Favoriten, da nur dort typischerweise
  viele Einträge über lange Zeit anwachsen.
- Kein serverseitiges/DB-seitiges Sortieren — die Liste ist klein genug
  (typische Nutzer-Favoritenlisten), um clientseitig zu gruppieren.
