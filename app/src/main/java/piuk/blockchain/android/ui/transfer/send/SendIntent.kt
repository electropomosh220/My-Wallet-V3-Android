package piuk.blockchain.android.ui.transfer.send

import info.blockchain.balance.CryptoValue
import piuk.blockchain.android.coincore.CryptoSingleAccount
import piuk.blockchain.android.coincore.ReceiveAddress
import piuk.blockchain.android.ui.base.mvi.MviIntent

sealed class SendIntent : MviIntent<SendState> {

    class Initialise(
        private val account: CryptoSingleAccount,
        private val passwordRequired: Boolean
    ) : SendIntent() {
        override fun reduce(oldState: SendState): SendState =
            SendState(
                sendingAccount = account,
                passwordRequired = passwordRequired,
                currentStep = if (passwordRequired) SendStep.ENTER_PASSWORD else SendStep.ENTER_ADDRESS,
                nextEnabled = passwordRequired
            )
        }

    class ValidatePassword(
        val password: String
    ) : SendIntent() {
        override fun reduce(oldState: SendState): SendState =
            oldState.copy(
                nextEnabled = false
            )
        }

    class UpdatePasswordIsValidated(
        val password: String
    ) : SendIntent() {
        override fun reduce(oldState: SendState): SendState =
            oldState.copy(
                nextEnabled = true,
                secondPassword = password,
                currentStep = SendStep.ENTER_ADDRESS
            )
        }

    object UpdatePasswordNotValidated : SendIntent() {
        override fun reduce(oldState: SendState): SendState =
            oldState.copy(
                nextEnabled = false,
                secondPassword = ""
            )
        }

    class AddressSelected(
        val address: ReceiveAddress
    ) : SendIntent() {
        override fun reduce(oldState: SendState): SendState =
            oldState.copy(
                nextEnabled = true,
                targetAddress = address
            )
    }

    object AddressSelectionConfirmed : SendIntent() {
        override fun reduce(oldState: SendState): SendState =
            oldState.copy(
                nextEnabled = false,
                currentStep = SendStep.ENTER_AMOUNT
            )
    }

    class SendAmountChanged(
        val amount: CryptoValue
    ) : SendIntent() {
        override fun reduce(oldState: SendState): SendState =
            oldState.copy(
                nextEnabled = false
            )
    }

    class UpdateTransactionAmounts(
        val amount: CryptoValue,
        val maxAvailable: CryptoValue
    ) : SendIntent() {
        override fun reduce(oldState: SendState): SendState =
            oldState.copy(
                nextEnabled = amount.isPositive,
                sendAmount = amount,
                availableBalance = maxAvailable
            )
    }

    class PrepareTransaction(
        val amount: CryptoValue
    ) : SendIntent() {
        override fun reduce(oldState: SendState): SendState =
            oldState.copy(
                nextEnabled = false,
                currentStep = SendStep.CONFIRM_DETAIL
            )
    }

    object ExecuteTransaction : SendIntent() {
        override fun reduce(oldState: SendState): SendState =
            oldState.copy(
                nextEnabled = false,
                currentStep = SendStep.IN_PROGRESS
            )
    }

    object FatalTransactionError : SendIntent() {
        override fun reduce(oldState: SendState): SendState =
            oldState.copy(
                nextEnabled = true,
                currentStep = SendStep.SEND_ERROR
            )
    }

    object UpdateTransactionComplete : SendIntent() {
        override fun reduce(oldState: SendState): SendState =
            oldState.copy(
                nextEnabled = true,
                currentStep = SendStep.SEND_COMPLETE
            )
    }
}
