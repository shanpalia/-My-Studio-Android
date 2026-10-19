package com.paliaapkhub.mystudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Blue = Color(0xFF087CFF)
private val Navy = Color(0xFF0A214B)
private val Pale = Color(0xFFF2F8FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.WHITE
        window.navigationBarColor = android.graphics.Color.WHITE
        window.decorView.systemUiVisibility =
            android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
            android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        setContent { MyStudioApp() }
    }
}

@Composable
private fun MyStudioApp() {
    var showSplash by remember { mutableStateOf(true) }
    var showExit by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Home") }
    var selectedFeature by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1600)
        showSplash = false
    }

    BackHandler(enabled = !showSplash) {
        if (selectedFeature != null) selectedFeature = null
        else if (selectedTab != "Home") selectedTab = "Home"
        else showExit = true
    }

    MaterialTheme(
        colorScheme = lightColorScheme(primary = Blue, background = Color.White, surface = Color.White)
    ) {
        if (showSplash) SplashScreen() else {
            Box(Modifier.fillMaxSize().background(Color.White)) {
                if (selectedFeature != null) FeaturePage(selectedFeature!!) { selectedFeature = null }
                else if (selectedTab == "Home") HomeScreen(
                    onFeature = { selectedFeature = it },
                    onTab = { selectedTab = it }
                ) else SimpleTab(selectedTab) { selectedTab = "Home" }
            }
            if (showExit) AlertDialog(
                onDismissRequest = { showExit = false },
                title = { Text("Exit My Studio?") },
                text = { Text("क्या आप My Studio से बाहर निकलना चाहते हैं?") },
                confirmButton = {
                    TextButton(onClick = {
                        showExit = false
                        android.os.Process.killProcess(android.os.Process.myPid())
                    }) { Text("Exit", color = Blue) }
                },
                dismissButton = { TextButton(onClick = { showExit = false }) { Text("Stay") }
                }
            )
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.White, Color(0xFFE5F5FF), Color.White))),
        contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            CameraMark(124)
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("My", fontSize = 43.sp, fontWeight = FontWeight.Black, color = Navy)
                Text("Studio", fontSize = 43.sp, fontWeight = FontWeight.Black, color = Blue)
            }
            Text("P H O T O G R A P H Y", fontSize = 11.sp, color = Navy, letterSpacing = 3.sp)
            Spacer(Modifier.height(24.dp))
            Text("Capture • Edit • Create", color = Blue, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(90.dp))
            Text("by PaliaAPK HUB", color = Navy, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Developer by shanpalia", color = Blue, fontSize = 14.sp)
            Spacer(Modifier.height(20.dp))
            CircularProgressIndicator(color = Blue, strokeWidth = 3.dp, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun CameraMark(size: Int) {
    Box(Modifier.size(size.dp).clip(RoundedCornerShape(30.dp)).background(
        Brush.linearGradient(listOf(Color(0xFF4ACDFF), Blue, Color(0xFF233CBA)))
    ), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.PhotoCamera, null, tint = Color.White, modifier = Modifier.size((size * .55f).dp))
    }
}

@Composable
private fun HomeScreen(onFeature: (String) -> Unit, onTab: (String) -> Unit) {
    val mainTools = listOf(
        Triple("Edit Photo", "Photo editing", Icons.Default.Image),
        Triple("AI Studio", "Background & AI", Icons.Default.AutoAwesome),
        Triple("Camera", "Capture photos", Icons.Default.PhotoCamera),
        Triple("Collage", "Frames & layout", Icons.Default.Dashboard)
    )
    val extras = listOf(
        Triple("Filters", "DSLR & cinematic", Icons.Default.Tune),
        Triple("Text", "Fonts & styles", Icons.Default.TextFields),
        Triple("Stickers", "Fun & creative", Icons.Default.EmojiEmotions),
        Triple("Frames", "Beautiful frames", Icons.Default.CropSquare)
    )
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            CameraMark(42)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("My Studio", fontSize = 25.sp, fontWeight = FontWeight.Black, color = Navy)
                Text("PHOTOGRAPHY", fontSize = 9.sp, letterSpacing = 2.sp, color = Blue)
            }
            Surface(color = Pale, shape = CircleShape) { Text("✦ Pro", Modifier.padding(horizontal = 14.dp, vertical = 8.dp), color = Blue, fontWeight = FontWeight.Bold) }
            IconButton(onClick = { onFeature("Settings") }) { Icon(Icons.Default.Settings, "Settings", tint = Navy) }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(
                Brush.horizontalGradient(listOf(Color(0xFFE5F6FF), Color(0xFFBDE7FF), Color(0xFFF5FAFF)))
            ).padding(20.dp)) {
                Column(Modifier.fillMaxWidth(.72f)) {
                    Text("Turn Your Photos into", color = Navy, fontSize = 15.sp)
                    Text("Amazing\nCreations", color = Blue, fontSize = 30.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Edit • Enhance • Create with AI", color = Navy, fontSize = 13.sp)
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = { onFeature("Edit Photo") }, shape = CircleShape) {
                        Text("Start Editing")
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
                Icon(Icons.Default.PhotoCamera, null, tint = Blue.copy(alpha = .18f), modifier = Modifier.align(Alignment.CenterEnd).size(90.dp))
            }
            Spacer(Modifier.height(18.dp))
            ToolGrid(mainTools, onFeature, large = true)
            Spacer(Modifier.height(12.dp))
            ToolGrid(extras, onFeature, large = false)
            Spacer(Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, null, tint = Blue)
                Spacer(Modifier.width(8.dp))
                Text("AI Tools", color = Navy, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { onTab("AI Studio") }) { Text("See all") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AiCard("Remove Background", Icons.Default.PersonRemove, Modifier.weight(1f)) { onFeature("Remove Background") }
                AiCard("Change Background", Icons.Default.Landscape, Modifier.weight(1f)) { onFeature("Change Background") }
                AiCard("AI Photo", Icons.Default.AutoAwesome, Modifier.weight(1f)) { onFeature("AI Photo Generation") }
            }
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, null, tint = Blue)
                Spacer(Modifier.width(8.dp))
                Text("Recent Photos", color = Navy, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { onTab("Gallery") }) { Text("View all") }
            }
            Text("Your edited photos will appear here.", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
            Spacer(Modifier.height(12.dp))
            Text("PaliaAPK HUB  •  Developer by shanpalia", color = Color(0xFF70809A), fontSize = 11.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), textAlign = TextAlign.Center)
        }
        NavigationBar(containerColor = Color.White) {
            listOf("Home", "Editor", "AI Studio", "Gallery", "More").forEach { item ->
                NavigationBarItem(selected = false, onClick = { if (item == "Editor") onFeature("Edit Photo") else onTab(item) },
                    icon = { Icon(when(item) { "Home" -> Icons.Default.Home; "Editor" -> Icons.Default.Tune; "AI Studio" -> Icons.Default.AutoAwesome; "Gallery" -> Icons.Default.Image; else -> Icons.Default.GridView }, null) },
                    label = { Text(item, fontSize = 10.sp) })
            }
        }
    }
}

@Composable
private fun ToolGrid(tools: List<Triple<String, String, androidx.compose.ui.graphics.vector.ImageVector>>, onFeature: (String) -> Unit, large: Boolean) {
    tools.chunked(4).forEach { rowTools ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            rowTools.forEach { (title, subtitle, icon) ->
                Column(Modifier.weight(1f).padding(bottom = 8.dp).clip(RoundedCornerShape(18.dp))
                    .background(if (large) Brush.linearGradient(listOf(Color(0xFF168BFF), Color(0xFF0759E8))) else Brush.linearGradient(listOf(Color(0xFFEAF8FF), Pale)))
                    .clickable { onFeature(title) }.padding(horizontal = 5.dp, vertical = if (large) 17.dp else 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(icon, null, tint = if (large) Color.White else Blue, modifier = Modifier.size(if (large) 30.dp else 25.dp))
                    Spacer(Modifier.height(7.dp))
                    Text(title, color = if (large) Color.White else Navy, fontWeight = FontWeight.Bold, fontSize = if (large) 12.sp else 11.sp, textAlign = TextAlign.Center, maxLines = 1)
                    Text(subtitle, color = if (large) Color.White.copy(alpha = .9f) else Color(0xFF536B8C), fontSize = 8.sp, textAlign = TextAlign.Center, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun AiCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(Pale).clickable(onClick = onClick).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Blue, modifier = Modifier.size(32.dp))
        Spacer(Modifier.height(8.dp))
        Text(title, fontSize = 11.sp, color = Navy, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, lineHeight = 13.sp)
    }
}

@Composable
private fun FeaturePage(title: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
            Text(title, color = Navy, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(30.dp))
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CameraMark(88)
                Spacer(Modifier.height(18.dp))
                Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Navy)
                Text("This tool is ready for the next implementation step.", color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                if (title.contains("Edit") || title.contains("Background")) {
                    Button(onClick = { /* photo picker will be wired in next step */ }) { Text("Choose Photo") }
                }
            }
        }
        Text("PaliaAPK HUB  •  Developer by shanpalia", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally), fontSize = 11.sp)
    }
}

@Composable
private fun SimpleTab(title: String, onHome: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CameraMark(80)
        Spacer(Modifier.height(16.dp))
        Text(title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Navy)
        Text("My Studio • PaliaAPK HUB", color = Blue)
        Spacer(Modifier.height(18.dp))
        Button(onClick = onHome) { Icon(Icons.Default.Home, null); Spacer(Modifier.width(8.dp)); Text("Back to Home") }
    }
}
