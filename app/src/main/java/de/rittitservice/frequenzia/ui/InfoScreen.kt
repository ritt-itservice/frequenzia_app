package de.rittitservice.frequenzia.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.rittitservice.frequenzia.R

private const val PACKAGE_NAME = "de.rittitservice.frequenzia"
private const val REPO_URL = "https://github.com/ritt-itservice/frequenzia_app"
private const val LICENSE_URL = "$REPO_URL/blob/main/LICENSE"
private const val AUTHOR_MAIL = "mailto:kontakt@ritt-itservice.de"
private const val RADIO_BROWSER_URL = "https://www.radio-browser.info/"

@Composable
fun InfoScreen() {
    val context = LocalContext.current
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "—"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Auf kurzen Bildschirmen (z. B. Tablet im Querformat, wo die
            // Navigationsleiste zusätzlich Höhe kostet) reicht sonst der
            // Platz nicht für alle InfoRows – ohne Scroll wird der Rest
            // einfach abgeschnitten statt erreichbar zu sein.
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(88.dp)
                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .padding(10.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(0.75f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Frequenzia",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Version $versionName",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InfoRow(
                icon = Icons.Default.StarRate,
                title = "App bewerten",
                subtitle = "Im Google Play Store",
                onClick = { openPlayStoreRating(context) }
            )
            InfoRow(
                icon = Icons.Default.Code,
                title = "Quellcode",
                subtitle = "GitHub – ritt-itservice/frequenzia_app",
                onClick = { openUrl(context, REPO_URL) }
            )
            InfoRow(
                icon = Icons.Default.Description,
                title = "Lizenz",
                subtitle = "GNU General Public License v3.0",
                onClick = { openUrl(context, LICENSE_URL) }
            )
            InfoRow(
                icon = Icons.Default.Person,
                title = "Autor",
                subtitle = "Eduard Ritt",
                onClick = { openUrl(context, AUTHOR_MAIL) }
            )
            InfoRow(
                icon = Icons.Default.Radio,
                title = "Radio Browser",
                subtitle = "Herzlichen Dank für die offene Sender-Datenbank",
                onClick = { openUrl(context, RADIO_BROWSER_URL) }
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

private fun openUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}

// Öffnet die Play-Store-App direkt auf der Bewertungs-Ansicht; wenn keine
// Play-Store-App installiert ist (z. B. auf einem Test-Emulator ohne Play
// Store), fällt es auf die Web-Ansicht zurück.
private fun openPlayStoreRating(context: Context) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$PACKAGE_NAME"))
        )
    }.onFailure {
        openUrl(context, "https://play.google.com/store/apps/details?id=$PACKAGE_NAME")
    }
}
