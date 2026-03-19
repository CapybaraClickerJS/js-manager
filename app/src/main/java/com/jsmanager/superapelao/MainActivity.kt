package com.jsmanager.superapelao

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                SuperAppScreen()
            }
        }
    }
}

@Composable
private fun SuperAppScreen() {
    val context = LocalContext.current
    val pm = context.packageManager

    var selectedTreeUri by remember { mutableStateOf<String?>(null) }
    var fileList by remember { mutableStateOf(listOf<String>()) }
    var zarchiverPackage by remember { mutableStateOf("ru.zdevs.zarchiver") }
    var mtManagerPackage by remember { mutableStateOf("bin.mt.plus") }
    var chromeTarget by remember { mutableStateOf("chrome://flags") }
    var status by remember { mutableStateOf("Pronto.") }

    val treeLauncher = rememberLauncherForActivityResult(OpenDocumentTree()) { uri ->
        if (uri != null) {
            selectedTreeUri = uri.toString()
            try {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, flags)
                status = "Pasta liberada via SAF."
                fileList = listTreeFiles(context, uri)
            } catch (e: SecurityException) {
                status = "Sem permissão persistente para essa pasta."
            }
        }
    }

    fun openPackage(pkg: String) {
        val launchIntent = pm.getLaunchIntentForPackage(pkg)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            status = "Abrindo $pkg"
        } else {
            status = "Pacote não encontrado: $pkg"
        }
    }

    fun openUrlInChrome(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            setPackage("com.android.chrome")
        }
        try {
            context.startActivity(intent)
            status = "Abrindo Chrome: $url"
        } catch (_: ActivityNotFoundException) {
            val fallback = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
            status = "Chrome não encontrado, usando navegador padrão."
        }
    }

    fun openAllFilesSettings() {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
            status = "Abrindo permissão de todos os arquivos."
        } catch (_: ActivityNotFoundException) {
            status = "Tela de all-files não disponível nesse Android."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("JS Manager Super Apelão", style = MaterialTheme.typography.headlineSmall)
        Text(status, style = MaterialTheme.typography.bodyMedium)

        Card {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("SAF / pasta liberada")
                Text(selectedTreeUri ?: "Nenhuma pasta escolhida ainda.")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { treeLauncher.launch(null) }) {
                        Text("Escolher pasta")
                    }
                    OutlinedButton(onClick = {
                        if (selectedTreeUri != null) {
                            fileList = listTreeFiles(context, Uri.parse(selectedTreeUri))
                            status = "Lista atualizada."
                        } else {
                            status = "Escolhe uma pasta primeiro."
                        }
                    }) {
                        Text("Atualizar lista")
                    }
                }
                fileList.take(12).forEach { name ->
                    Text("• $name")
                }
            }
        }

        Card {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Atalhos externos")
                OutlinedTextField(
                    value = zarchiverPackage,
                    onValueChange = { zarchiverPackage = it },
                    label = { Text("Pacote do ZArchiver") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = mtManagerPackage,
                    onValueChange = { mtManagerPackage = it },
                    label = { Text("Pacote do MT Manager") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = chromeTarget,
                    onValueChange = { chromeTarget = it },
                    label = { Text("URL / chrome://...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { openPackage(zarchiverPackage) }) {
                        Text("Abrir ZArchiver")
                    }
                    Button(onClick = { openPackage(mtManagerPackage) }) {
                        Text("Abrir MT")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { openUrlInChrome(chromeTarget) }) {
                        Text("Abrir Chrome")
                    }
                    OutlinedButton(onClick = { openAllFilesSettings() }) {
                        Text("All files")
                    }
                }
            }
        }

        Card {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Shizuku / root")
                Text("Deixei como módulo separado para integrar depois com segurança.")
                Text("No MVP, o app já prepara a interface e os atalhos.")
            }
        }
    }
}

private fun listTreeFiles(context: android.content.Context, treeUri: Uri): List<String> {
    val root = DocumentFile.fromTreeUri(context, treeUri) ?: return emptyList()
    return root.listFiles()
        .mapNotNull { doc ->
            val type = when {
                doc.isDirectory -> "[pasta]"
                doc.isFile -> "[arquivo]"
                else -> "[item]"
            }
            doc.name?.let { "$type $it" }
        }
        .sorted()
}
