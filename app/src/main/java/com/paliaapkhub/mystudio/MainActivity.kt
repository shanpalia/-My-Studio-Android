package com.paliaapkhub.mystudio

import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Color as AndroidColor
import android.content.ContentValues
import android.provider.MediaStore
import android.widget.Toast
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
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
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) { val loaded = try { context.contentResolver.openInputStream(uri)?.use { stream -> BitmapFactory.decodeStream(stream) } } catch (_: Exception) { null }; selectedBitmap = loaded; originalBitmap = loaded }
    }
    val cameraCapture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) { selectedBitmap = bitmap; originalBitmap = bitmap }
    }

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
            Box(Modifier.fillMaxSize().background(Color.White).statusBarsPadding().navigationBarsPadding()) {
                if (selectedFeature != null) FeaturePage(
                    title = selectedFeature!!,
                    bitmap = selectedBitmap,
                    onBack = { selectedFeature = null },
                    onChoosePhoto = { photoPicker.launch("image/*") },
                    onOpenCamera = { cameraCapture.launch(null) },
                    onTransform = { action -> selectedBitmap = if (action == "reset") originalBitmap else selectedBitmap?.let { transformBitmap(it, action) } },
                    onSave = { selectedBitmap?.let { saveBitmap(context, it) } }
                )
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
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
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
private fun FeaturePage(
    title: String,
    bitmap: Bitmap?,
    onBack: () -> Unit,
    onChoosePhoto: () -> Unit,
    onOpenCamera: () -> Unit,
    onTransform: (String) -> Unit,
    onSave: () -> Unit
) {
    val isCamera = title == "Camera"
    val isPhotoTool = title in listOf("Edit Photo", "Filters", "Text", "Stickers", "Frames", "Collage", "AI Studio", "Remove Background", "Change Background", "AI Photo Generation")
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Navy) }
            Text(title, color = Navy, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        if (bitmap != null) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Photo preview", modifier = Modifier.fillMaxWidth().heightIn(max = 410.dp).clip(RoundedCornerShape(18.dp)))
                Spacer(Modifier.height(12.dp))
                Text("Photo ready to edit", color = Navy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onChoosePhoto) { Icon(Icons.Default.Image, null); Spacer(Modifier.width(5.dp)); Text("Change") }
                    Button(onClick = onSave) { Icon(Icons.Default.SaveAlt, null); Spacer(Modifier.width(5.dp)); Text("Save") }
                }
                if (title in listOf("Edit Photo", "Filters", "Frames", "Text", "Stickers", "Collage", "AI Studio")) {
                    Text("Editing tools", color = Navy, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 6.dp))
                    when (title) {
                        "Filters" -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("Original", "B&W", "Sepia", "Bright").forEach { label ->
                                    OutlinedButton(onClick = { onTransform(when(label) { "B&W" -> "grayscale"; "Sepia" -> "sepia"; "Bright" -> "bright"; else -> "reset" }) }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) { Text(label, fontSize = 12.sp) }
                                }
                            }
                        }
                        "Frames" -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { onTransform("frame") }) { Text("Add white frame") }
                            OutlinedButton(onClick = { onTransform("reset") }) { Text("Reset") }
                        }
                        "Text" -> {
                            var caption by remember { mutableStateOf("My Studio") }
                            OutlinedTextField(value = caption, onValueChange = { caption = it }, label = { Text("Caption text") }, singleLine = true)
                            Button(onClick = { onTransform("text:$caption") }) { Text("Add caption") }
                        }
                        "Stickers" -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("♥", "★", "✨").forEach { sticker -> OutlinedButton(onClick = { onTransform("text:$sticker") }) { Text(sticker) } }
                        }
                        "Collage" -> Text("Basic collage needs a second photo; select another image in the next update.", color = Color.Gray, fontSize = 12.sp)
                        else -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { onTransform("rotate") }) { Icon(Icons.Default.RotateRight, null); Text("Rotate") }
                            OutlinedButton(onClick = { onTransform("flip") }) { Icon(Icons.Default.Flip, null); Text("Flip") }
                            OutlinedButton(onClick = { onTransform("crop") }) { Icon(Icons.Default.Crop, null); Text("Square crop") }
                            OutlinedButton(onClick = { onTransform("reset") }) { Text("Reset") }
                        }
                    }
                } else if (isCamera) {
                    OutlinedButton(onClick = onOpenCamera) { Icon(Icons.Default.PhotoCamera, null); Text("Retake") }
                }
                Spacer(Modifier.height(12.dp))
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CameraMark(88)
                    Spacer(Modifier.height(18.dp))
                    Text(title, fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Navy)
                    Text(if (isCamera) "Take a photo using your phone camera." else "Choose a photo from your phone to get started.", color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
                    if (isCamera) {
                        Button(onClick = onOpenCamera, shape = CircleShape) { Icon(Icons.Default.PhotoCamera, null); Spacer(Modifier.width(8.dp)); Text("Open Camera") }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = onChoosePhoto) { Text("Choose from Gallery") }
                    } else if (isPhotoTool) {
                        Button(onClick = onChoosePhoto, shape = CircleShape) { Icon(Icons.Default.Image, null); Spacer(Modifier.width(8.dp)); Text("Choose Photo") }
                        if (title == "Edit Photo") {
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(onClick = onOpenCamera) { Icon(Icons.Default.PhotoCamera, null); Spacer(Modifier.width(8.dp)); Text("Take a Photo") }
                        }
                    } else Text("This section is being prepared.", color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        }
        Text("PaliaAPK HUB  •  Developer by shanpalia", color = Color(0xFF9AA6B6), modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 4.dp), fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}

private fun transformBitmap(source: Bitmap, action: String): Bitmap {
    if (action == "reset") return source
    if (action == "rotate" || action == "flip") {
        val matrix = Matrix()
        if (action == "rotate") matrix.postRotate(90f) else matrix.postScale(-1f, 1f)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
    if (action == "crop") {
        val side = minOf(source.width, source.height)
        return Bitmap.createBitmap(source, (source.width-side)/2, (source.height-side)/2, side, side)
    }
    val output = source.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(output)
    if (action == "frame") {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.WHITE; style = Paint.Style.STROKE; strokeWidth = maxOf(12f, output.width * .025f) }
        canvas.drawRect(paint.strokeWidth/2, paint.strokeWidth/2, output.width-paint.strokeWidth/2, output.height-paint.strokeWidth/2, paint)
        return output
    }
    if (action.startsWith("text:")) {
        val caption = action.removePrefix("text:")
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.WHITE; textSize = maxOf(36f, output.width * .07f); typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD); setShadowLayer(4f, 2f, 2f, AndroidColor.BLACK) }
        canvas.drawText(caption, output.width * .05f, output.height * .92f, paint)
        return output
    }
    val pixels = IntArray(output.width * output.height)
    output.getPixels(pixels, 0, output.width, 0, 0, output.width, output.height)
    for (i in pixels.indices) {
        val color = pixels[i]
        val a = AndroidColor.alpha(color); val r = AndroidColor.red(color).toFloat(); val g = AndroidColor.green(color).toFloat(); val b = AndroidColor.blue(color).toFloat()
        val nr: Int; val ng: Int; val nb: Int
        when (action) {
            "grayscale" -> { val y = (.299f*r + .587f*g + .114f*b).toInt(); nr=y; ng=y; nb=y }
            "sepia" -> { nr=(.393f*r+.769f*g+.189f*b).toInt().coerceAtMost(255); ng=(.349f*r+.686f*g+.168f*b).toInt().coerceAtMost(255); nb=(.272f*r+.534f*g+.131f*b).toInt().coerceAtMost(255) }
            "bright" -> { nr=(r*1.12f+12).toInt().coerceAtMost(255); ng=(g*1.12f+12).toInt().coerceAtMost(255); nb=(b*1.12f+12).toInt().coerceAtMost(255) }
            else -> { nr=r.toInt(); ng=g.toInt(); nb=b.toInt() }
        }
        pixels[i] = AndroidColor.argb(a, nr, ng, nb)
    }
    output.setPixels(pixels, 0, output.width, 0, 0, output.width, output.height)
    return output
}

private fun saveBitmap(context: android.content.Context, bitmap: Bitmap) {
    try {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "MyStudio_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyStudio")
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
            Toast.makeText(context, "Saved to Pictures/MyStudio", Toast.LENGTH_LONG).show()
        } else Toast.makeText(context, "Could not save photo", Toast.LENGTH_SHORT).show()
    } catch (_: Exception) { Toast.makeText(context, "Could not save photo", Toast.LENGTH_SHORT).show() }
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
