package com.yunfie.illustia.ui.screens.pallasync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.yunfie.illustia.pallasync.PalleriaSyncManager
import com.yunfie.illustia.ui.components.HeaderIcon
import com.yunfie.illustia.ui.components.PredictiveBackGestureHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme
import android.graphics.Color
import android.graphics.Bitmap

import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.activity.compose.rememberLauncherForActivityResult
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions


@Composable
fun DevicePairingScreen(
    serverUrl: String,
    onBack: () -> Unit,
    onPairSuccess: () -> Unit,
) {
    PredictiveBackGestureHandler(onBack = onBack)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val syncManager = remember { PalleriaSyncManager(context = context) }
    val clipboardManager = LocalClipboardManager.current
    
    var seedPhrase by remember { mutableStateOf("") }
    var enteredSeedPhrase by remember { mutableStateOf("") }
    var isJoining by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val keystore = syncManager.getPallaSyncKeystore()
            val existingSeed = keystore.getSeedPhrase()
            if (existingSeed != null) {
                seedPhrase = existingSeed
            }
        }
    }
    
    val qrLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            enteredSeedPhrase = result.contents
        }
    }
    
    val tabs = listOf("View Sync Code", "Enter Sync Code")
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    Scaffold(
        containerColor = MiuixTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = "Sync Devices",
                navigationIcon = {
                    HeaderIcon(MiuixIcons.Back, onClick = onBack)
                },
            )
        },
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.surface)
                .padding(scaffoldPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            TabRowWithContour(
                tabs = tabs,
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            if (selectedTabIndex == 0) {
                        // Generate QR code bitmap for the seed phrase
        val qrBitmap = remember(seedPhrase) {
            try {
                val writer = QRCodeWriter()
                val bitMatrix = writer.encode(seedPhrase, BarcodeFormat.QR_CODE, 256, 256)
                val width = bitMatrix.width
                val height = bitMatrix.height
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        bmp.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                    }
                }
                bmp
            } catch (e: Exception) {
                null
            }
        }
        if (qrBitmap != null) {
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "Sync QR Code",
                modifier = Modifier.size(200.dp).padding(bottom = 16.dp)
            )
        }

                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Sync Chain Recovery Phrase",
                        style = MiuixTheme.textStyles.headline1,
                        color = MiuixTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Enter these 24 words in the exact order on another device to sync your data. Do not share this phrase with anyone.",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onBackgroundVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    if (seedPhrase.isNotEmpty()) {
                        val words = seedPhrase.trim().split("\\s+".toRegex())
                        if (words.size == 24) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                itemsIndexed(words) { index, word ->
                                    Row(
                                        modifier = Modifier
                                            .background(
                                                MiuixTheme.colorScheme.surfaceVariant,
                                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${index + 1}.",
                                            style = MiuixTheme.textStyles.body2,
                                            color = MiuixTheme.colorScheme.primary,
                                            modifier = Modifier.width(24.dp)
                                        )
                                        Text(
                                            text = word,
                                            style = MiuixTheme.textStyles.body1,
                                            color = MiuixTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(seedPhrase))
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Copy to Clipboard")
                            }
                        } else {
                            Text(
                                text = "Invalid seed phrase format.",
                                style = MiuixTheme.textStyles.body1,
                                color = MiuixTheme.colorScheme.error
                            )
                        }
                    } else {
                        Text(
                            text = "Loading...",
                            style = MiuixTheme.textStyles.body1,
                            color = MiuixTheme.colorScheme.onBackgroundVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            } else {
                // Enter Sync Code
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Join Sync Chain",
                        style = MiuixTheme.textStyles.headline1,
                        color = MiuixTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Enter the 24-word recovery phrase from another device to sync your data.",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onBackgroundVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    TextField(
                        value = enteredSeedPhrase,
                        onValueChange = { enteredSeedPhrase = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = {
                            if (enteredSeedPhrase.trim().split("\\s+".toRegex()).size == 24) {
                                isJoining = true
                                scope.launch {
                                    val success = try {
                                        syncManager.joinChain(
                                            seedPhrase = enteredSeedPhrase.trim(),
                                            serverUrl = serverUrl,
                                        )
                                    } finally {
                                        isJoining = false
                                    }
                                    if (success) {
                                        Toast.makeText(context, "Successfully joined Sync Chain", Toast.LENGTH_SHORT).show()
                                        onPairSuccess()
                                    } else {
                                        Toast.makeText(context, "Failed to join Sync Chain", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Please enter exactly 24 words", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isJoining
                    ) {
                        Text(if (isJoining) "Joining..." else "Join Sync Chain")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            qrLauncher.launch(ScanOptions().apply {
                                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                                setPrompt("Scan a Sync QR Code")
                                setBeepEnabled(false)
                                setBarcodeImageEnabled(false)
                            })
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColorsPrimary()
                    ) {
                        Text("Scan QR Code")
                    }
                }
            }
        }
    }
}
