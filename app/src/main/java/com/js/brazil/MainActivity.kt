package com.js.brazil

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.*
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
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppUI() }
    }
}

@Composable
fun AppUI() {
    val ctx = LocalContext.current

    var status by remember { mutableStateOf("💀 JS MANAGER ULTRA GOD") }
    var files by remember { mutableStateOf(listOf<String>()) }
    var bigFiles by remember { mutableStateOf(listOf<String>()) }
    var systemInfo by remember { mutableStateOf("") }

    val folderPicker = rememberLauncherForActivityResult(OpenDocumentTree()) { uri ->
        if (uri != null) {
            ctx.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            files = listFiles(ctx, uri)
            bigFiles = findBigFiles(ctx, uri)
            status = "😈 ACESSO LIBERADO"
        }
    }

    val filePicker = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        if (uri != null) installFile(ctx, uri)
    }

    fun openApp(pkg:String){
        val i = ctx.packageManager.getLaunchIntentForPackage(pkg)
        if(i!=null) ctx.startActivity(i) else status="❌ App não encontrado"
    }

    fun openChrome(){
        try{
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("chrome://flags"))
            i.setPackage("com.android.chrome")
            ctx.startActivity(i)
        }catch(e:Exception){status="Erro Chrome"}
    }

    fun allFiles(){
        ctx.startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
    }

    fun systemScan(){
        systemInfo = """
RAM livre: ${Runtime.getRuntime().freeMemory()/1024/1024}MB
RAM total: ${Runtime.getRuntime().totalMemory()/1024/1024}MB
CPU: ${Runtime.getRuntime().availableProcessors()}
        """
    }

    fun root(cmd:String):String{
        return try{
            val p=Runtime.getRuntime().exec("su")
            val os=p.outputStream
            os.write((cmd+"\n").toByteArray())
            os.write("exit\n".toByteArray())
            os.flush()
            BufferedReader(InputStreamReader(p.inputStream)).readText()
        }catch(e:Exception){"SEM ROOT"}
    }

    Column(
        Modifier.fillMaxSize().padding(10.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ){

        Text("💀 JS MANAGER ULTRA GOD", style = MaterialTheme.typography.headlineMedium)
        Text(status)

        Button({folderPicker.launch(null)}){Text("🔥 LIBERAR PASTA")}
        Button({filePicker.launch(arrayOf("*/*"))}){Text("📦 INSTALAR ARQUIVO")}

        Button({openApp("ru.zdevs.zarchiver")}){Text("ZArchiver")}
        Button({openApp("bin.mt.plus")}){Text("MT Manager")}

        Button({openChrome()}){Text("chrome://flags")}
        Button({allFiles()}){Text("ACESSO TOTAL")}

        Button({systemScan()}){Text("🧠 INFO SISTEMA")}

        Button({status=root("ls /data/app")}){Text("💀 LISTAR /data/app")}
        Button({status=root("pm trim-caches 999999999999")}){Text("🔥 LIMPAR CACHE")}
        Button({status=root("ls /data/data")}){Text("📂 VER /data/data")}
        Button({status=root("df")}){Text("📊 DISCO")}

        Text("📂 Arquivos:")
        files.take(8).forEach{Text("• $it")}

        Text("💣 Pesados:")
        bigFiles.take(5).forEach{Text("🔥 $it")}

        Text("🧠 Sistema:")
        Text(systemInfo)
    }
}

fun listFiles(ctx:Context,uri:Uri):List<String>{
    val root=DocumentFile.fromTreeUri(ctx,uri)?:return emptyList()
    return root.listFiles().mapNotNull{it.name}
}

fun findBigFiles(ctx:Context,uri:Uri):List<String>{
    val root=DocumentFile.fromTreeUri(ctx,uri)?:return emptyList()
    return root.listFiles()
        .filter{it.length()>5_000_000}
        .mapNotNull{it.name+" ("+it.length()/1024/1024+"MB)"}
}

fun installFile(ctx:Context,uri:Uri){
    val i=Intent(Intent.ACTION_VIEW)
    i.setDataAndType(uri,"*/*")
    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    ctx.startActivity(i)
}
