package com.example.pocstriperm2

import androidx.lifecycle.ViewModel
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.PaymentIntentCallback
import com.stripe.stripeterminal.external.models.PaymentIntent
import com.stripe.stripeterminal.external.models.PaymentIntentParameters
import com.stripe.stripeterminal.external.models.TerminalException

class PaymentViewModel : ViewModel() {
    fun startPayment(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val params = PaymentIntentParameters.Builder()
            .setAmount(1000L)
            .setCurrency("usd")
            .build()
        Terminal.getInstance().createPaymentIntent(params, object : PaymentIntentCallback {
            override fun onSuccess(paymentIntent: PaymentIntent) {
                collectPayment(paymentIntent, onSuccess, onError)
            }

            override fun onFailure(e: TerminalException) {
                onError(e.errorMessage ?: "Erro ao criar PaymentIntent")
            }
        })
    }

    private fun collectPayment(
        paymentIntent: PaymentIntent,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Terminal.getInstance().collectPaymentMethod(paymentIntent, object : PaymentIntentCallback {
            override fun onSuccess(paymentIntent: PaymentIntent) {
                processPayment(paymentIntent, onSuccess, onError)
            }

            override fun onFailure(e: TerminalException) {
                onError(e.errorMessage ?: "Erro ao coletar pagamento")
            }
        })
    }

    private fun processPayment(
        paymentIntent: PaymentIntent,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Terminal.getInstance().confirmPaymentIntent(paymentIntent, object : PaymentIntentCallback {
            override fun onSuccess(paymentIntent: PaymentIntent) {
                onSuccess()
            }

            override fun onFailure(e: TerminalException) {
                onError(e.errorMessage ?: "Erro ao processar pagamento")
            }
        })
    }
}