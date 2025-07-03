// app/src/main/java/com/example/pocstriperm2/MainActivity.kt
package com.example.pocstriperm2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.Callback
import com.stripe.stripeterminal.external.callable.Cancelable
import com.stripe.stripeterminal.external.callable.DiscoveryListener
import com.stripe.stripeterminal.external.callable.MobileReaderListener
import com.stripe.stripeterminal.external.callable.ReaderCallback
import com.stripe.stripeterminal.external.callable.TerminalListener
import com.stripe.stripeterminal.external.models.ConnectionConfiguration
import com.stripe.stripeterminal.external.models.ConnectionStatus
import com.stripe.stripeterminal.external.models.DisconnectReason
import com.stripe.stripeterminal.external.models.DiscoveryConfiguration
import com.stripe.stripeterminal.external.models.PaymentStatus
import com.stripe.stripeterminal.external.models.Reader
import com.stripe.stripeterminal.external.models.TerminalException
import com.stripe.stripeterminal.log.LogLevel

class MainActivity : ComponentActivity() {

    private lateinit var terminal: Terminal

    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            discoverReaders()
        } else {
            showToast("Permissões de Bluetooth e localização são necessárias.")
        }
    }

    private val listener = object : TerminalListener {
        override fun onConnectionStatusChange(status: ConnectionStatus) {}
        override fun onPaymentStatusChange(status: PaymentStatus) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Terminal.initTerminal(application, LogLevel.VERBOSE, TokenProvider(), listener)
        terminal = Terminal.getInstance()
        setContent {
            MaterialTheme {
                Surface {
                    PaymentScreen()
                }
            }
        }
        requestPermissionsAndDiscover()
    }

    private fun requestPermissionsAndDiscover() {
        val notGranted = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isEmpty()) {
            discoverReaders()
        } else {
            permissionLauncher.launch(notGranted.toTypedArray())
        }
    }

    private fun discoverReaders() {
        val notGranted = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            showToast("Permissões necessárias não concedidas.")
            return
        }
        try {
            val config = DiscoveryConfiguration.BluetoothDiscoveryConfiguration(
                timeout = 0,
                isSimulated = true
            )
            terminal.discoverReaders(config, object : DiscoveryListener {
                override fun onUpdateDiscoveredReaders(readers: List<Reader>) {
                    if (readers.isNotEmpty()) {
                        connectToReader(readers.first())
                    }
                }
            }, object : Callback {
                override fun onSuccess() {
                    Log.d("Discovery", "Readers discovered successfully")
                }

                override fun onFailure(e: TerminalException) {
                    Log.d("Discovery", "Failed to discover readers: ${e.message}")
                    showToast("Failed to discover readers: ${e.message}")
                }
            })
        } catch (e: SecurityException) {
            showToast("Permissões necessárias não concedidas.")
            Log.e("Discovery", "SecurityException: ${e.message}")
        }
    }

    private val yourMobileReaderListener = object : MobileReaderListener {
        override fun onReaderReconnectStarted(
            reader: Reader,
            cancelReconnect: Cancelable,
            reason: DisconnectReason
        ) {
        }

        override fun onReaderReconnectSucceeded(reader: Reader) {}
        override fun onReaderReconnectFailed(reader: Reader) {}
    }

    private fun connectToReader(selectedReader: Reader) {
        val connectionConfig = ConnectionConfiguration.BluetoothConnectionConfiguration(
            "{{LOCATION_ID}}",
            true,
            yourMobileReaderListener
        )
        terminal.connectReader(selectedReader, connectionConfig, object : ReaderCallback {
            override fun onSuccess(reader: Reader) {
                showToast("Conectado ao leitor: ${reader.serialNumber}")
            }

            override fun onFailure(e: TerminalException) {
                showToast("Falha ao conectar ao leitor: ${e.message}")
            }
        })
    }

    private fun showToast(msg: String) {
        runOnUiThread { Toast.makeText(this, msg, Toast.LENGTH_LONG).show() }
    }
}

@Composable
fun PaymentScreen(paymentViewModel: PaymentViewModel = viewModel()) {
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    Column(modifier = androidx.compose.ui.Modifier.padding(16.dp)) {
        Button(
            onClick = {
                isLoading = true
                paymentViewModel.startPayment(
                    onSuccess = {
                        message = "Pagamento realizado com sucesso!"
                        isLoading = false
                    },
                    onError = { error ->
                        message = "Erro: $error"
                        isLoading = false
                    }
                )
            },
            enabled = !isLoading
        ) {
            Text("Iniciar Pagamento")
        }
        if (isLoading) {
            CircularProgressIndicator()
        }
        if (message.isNotEmpty()) {
            Text(message)
        }
    }
}