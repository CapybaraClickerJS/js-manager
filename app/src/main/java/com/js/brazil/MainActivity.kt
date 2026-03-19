package com.js.brazil

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { UI() }
    }
}

@Composable
fun UI() {
    val ctx = LocalContext.current

    var status by remember { mutableStateOf("💀 JS SUPER APP") }
    var output by remember { mutableStateOf("") }

    fun runRoot(cmd:String):String{
        return try{
            val p=Runtime.getRuntime().exec("su")
            val os=p.outputStream
            os.write((cmd+"\n").toByteArray())
            os.write("exit\n".toByteArray())
            os.flush()
            BufferedReader(InputStreamReader(p.inputStream)).readText()
        }catch(e:Exception){
            "SEM ROOT"
        }
    }

    fun systemInfo(){
        output = """
RAM livre: ${Runtime.getRuntime().freeMemory()/1024/1024}MB
RAM total: ${Runtime.getRuntime().totalMemory()/1024/1024}MB
CPU: ${Runtime.getRuntime().availableProcessors()}
        """
    }

    fun limparCache(){
        output = runRoot("pm trim-caches 999999999999")
    }

    fun listarData(){
        output = runRoot("ls /data/data")
    }

    fun listarApps(){
        output = runRoot("ls /data/app")
    }

    fun abrirConfig(){
        ctx.startActivity(Intent(Settings.ACTION_SETTINGS))
    }

    fun abrirChromeFlags(){
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("chrome://flags"))
        i.setPackage("com.android.chrome")
        ctx.startActivity(i)
    }

    Column(
        Modifier.fillMaxSize().padding(10.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ){

        Text("💀 JS SUPER APP", style = MaterialTheme.typography.headlineMedium)
        Text(status)

        Button({systemInfo()}){Text("🧠 INFO SISTEMA")}
        Button({limparCache()}){Text("🔥 LIMPAR CACHE (ROOT)")}
        Button({listarData()}){Text("📂 /data/data (ROOT)")}
        Button({listarApps()}){Text("📦 /data/app (ROOT)")}

        Button({abrirConfig()}){Text("⚙️ CONFIG")}
        Button({abrirChromeFlags()}){Text("🌐 chrome://flags")}

        Text("💣 OUTPUT:")
        Text(output)
    }
}
