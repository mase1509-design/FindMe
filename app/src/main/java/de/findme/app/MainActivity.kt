package de.findme.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONArray
import org.json.JSONObject

data class Entry(val date: String, val found: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("findme_local", Context.MODE_PRIVATE)
        setContent { FindMeApp(prefs) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindMeApp(prefs: android.content.SharedPreferences) {
    val formatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY) }
    var date by remember { mutableStateOf(formatter.format(Date())) }
    var found by remember { mutableStateOf("Ja") }
    var expanded by remember { mutableStateOf(false) }
    var entries by remember { mutableStateOf(loadEntries(prefs)) }

    fun save() {
        entries = listOf(Entry(date, found)) + entries
        saveEntries(prefs, entries)
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF087EF5),
            background = Color(0xFFF7F9FC),
            surface = Color.White
        )
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Find", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        Text("Me", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold,
                            color = Color(0xFF087EF5))
                    }
                    Text("Nichts verpassen", color = Color.Gray)
                }

                item {
                    Card(shape = RoundedCornerShape(20.dp)) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            OutlinedTextField(
                                value = date,
                                onValueChange = { date = it },
                                label = { Text("Datum") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = found,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Hoden gefunden") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    listOf("Ja", "Nein").forEach {
                                        DropdownMenuItem(
                                            text = { Text(it) },
                                            onClick = { found = it; expanded = false }
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { save() },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) { Text("＋  Eintrag speichern") }
                        }
                    }
                }

                item {
                    Text("Letzte Einträge", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                if (entries.isEmpty()) {
                    item {
                        Text("Noch keine Einträge.", color = Color.Gray)
                    }
                } else {
                    items(entries) { entry ->
                        Card(shape = RoundedCornerShape(14.dp)) {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(entry.date, Modifier.weight(1f), fontWeight = FontWeight.Medium)
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (entry.found == "Ja") Color(0xFFD9F7E5) else Color(0xFFFFE0E0)
                                ) {
                                    Text(
                                        entry.found,
                                        Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        color = if (entry.found == "Ja") Color(0xFF087A42) else Color(0xFFB3261E),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        "🔒 Alle Daten bleiben lokal auf deinem Gerät.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

fun loadEntries(prefs: android.content.SharedPreferences): List<Entry> {
    val raw = prefs.getString("entries", "[]") ?: "[]"
    val arr = JSONArray(raw)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        Entry(o.getString("date"), o.getString("found"))
    }
}

fun saveEntries(prefs: android.content.SharedPreferences, entries: List<Entry>) {
    val arr = JSONArray()
    entries.forEach {
        arr.put(JSONObject().apply {
            put("date", it.date)
            put("found", it.found)
        })
    }
    prefs.edit().putString("entries", arr.toString()).apply()
}
