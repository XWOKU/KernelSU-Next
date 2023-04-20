package me.weishu.kernelsu.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.alorma.compose.settings.ui.*
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.BuildConfig
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.SimpleDialog
import me.weishu.kernelsu.ui.util.LinkifyText
import me.weishu.kernelsu.ui.util.LocalDialogHost
import me.weishu.kernelsu.ui.util.getBugreportFile

/**
 * @author weishu
 * @date 2023/1/1.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun SettingScreen(navigator: DestinationsNavigator) {

    Scaffold(
        topBar = {
            TopBar(onBack = {
                navigator.popBackStack()
            })
        }
    ) { paddingValues ->

        SimpleDialog {
            SupportCard()
        }

        var showLoadingDialog by remember { mutableStateOf(false) }
        LoadingDialog(showLoadingDialog)

        Column(modifier = Modifier.padding(paddingValues)) {

            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val dialogHost = LocalDialogHost.current
            SettingsMenuLink(
                title = {
                    Text(stringResource(id = R.string.send_log))
                },
                onClick = {
                    scope.launch {
                        showLoadingDialog = true

                        val bugreport = withContext(Dispatchers.IO) {
                            getBugreportFile(context)
                        }

                        showLoadingDialog = false

                        val uri: Uri =
                            FileProvider.getUriForFile(
                                context,
                                "${BuildConfig.APPLICATION_ID}.fileprovider",
                                bugreport
                            )

                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                        shareIntent.setDataAndType(uri, "application/zip")
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                        context.startActivity(
                            Intent.createChooser(
                                shareIntent,
                                context.getString(R.string.send_log)
                            )
                        )
                    }
                }
            )

            val about = stringResource(id = R.string.about)
            val ok = stringResource(id = android.R.string.ok)
            SettingsMenuLink(
                title = {
                    Text(about)
                },
                onClick = {
                    scope.launch {
                        dialogHost.showDialog(about, content = "unused", confirm = ok)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(onBack: () -> Unit = {}) {
    TopAppBar(
        title = { Text(stringResource(R.string.settings)) },
        navigationIcon = {
            IconButton(
                onClick = onBack
            ) { Icon(Icons.Filled.ArrowBack, contentDescription = null) }
        },
    )
}

@Composable
fun LoadingDialog(showLoadingDialog: Boolean) {
    if (!showLoadingDialog) {
        return
    }

    Dialog(
        onDismissRequest = { },
        DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .background(White, shape = RoundedCornerShape(8.dp))
        ) {
            CircularProgressIndicator()
        }
    }
}

@Preview
@Composable
private fun SupportCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
            LinkifyText("Author: weishu")
            LinkifyText("Github: https://github.com/tiann/KernelSU")
            LinkifyText("Telegram: https://t.me/KernelSU")
            LinkifyText("QQ: https://pd.qq.com/s/8lipl1brp")
        }
    }
}