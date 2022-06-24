package com.blockchain.core.chains.dynamicselfcustody

import com.blockchain.api.adapters.ApiError
import com.blockchain.api.selfcustody.Status
import com.blockchain.api.selfcustody.TransactionDirection
import com.blockchain.api.selfcustody.TransactionResponse
import com.blockchain.api.services.DynamicSelfCustodyService
import com.blockchain.outcome.Outcome
import com.blockchain.outcome.map
import com.blockchain.preferences.CurrencyPrefs
import org.bitcoinj.core.Sha256Hash
import org.spongycastle.util.encoders.Hex
import piuk.blockchain.androidcore.data.payload.PayloadDataManager

internal class NonCustodialRepository(
    private val dynamicSelfCustodyService: DynamicSelfCustodyService,
    private val payloadDataManager: PayloadDataManager,
    private val currencyPrefs: CurrencyPrefs
) : NonCustodialService {

    override suspend fun authenticate(): Outcome<ApiError, Boolean> =
        dynamicSelfCustodyService.authenticate(
            guid = payloadDataManager.guid,
            sharedKey = getHashedString(payloadDataManager.sharedKey)
        )
            .map { it.success }

    override suspend fun subscribe(currency: String, label: String, addresses: List<String>) =
        dynamicSelfCustodyService.subscribe(
            guidHash = getHashedString(payloadDataManager.guid),
            sharedKeyHash = getHashedString(payloadDataManager.sharedKey),
            currency = currency,
            accountName = label,
            addresses = addresses
        )
            .map { it.success }

    override suspend fun unsubscribe(currency: String) =
        dynamicSelfCustodyService.unsubscribe(
            guidHash = getHashedString(payloadDataManager.guid),
            sharedKeyHash = getHashedString(payloadDataManager.sharedKey),
            currency = currency
        )
            .map { it.success }

    override suspend fun getSubscriptions() =
        dynamicSelfCustodyService.getSubscriptions(
            guidHash = getHashedString(payloadDataManager.guid),
            sharedKeyHash = getHashedString(payloadDataManager.sharedKey)
        )
            .map { subscriptionsResponse ->
                subscriptionsResponse.currencies.map { it.ticker }
            }

    override suspend fun getBalances(currencies: List<String>) =
        dynamicSelfCustodyService.getBalances(
            guidHash = getHashedString(payloadDataManager.guid),
            sharedKeyHash = getHashedString(payloadDataManager.sharedKey),
            currencies = currencies,
            fiatCurrency = currencyPrefs.selectedFiatCurrency.networkTicker
        ).map { balancesResponse ->
            balancesResponse.balances.map { balanceResponse ->
                NonCustodialAccountBalance(
                    networkTicker = balanceResponse.currency,
                    amount = balanceResponse.balance.amount,
                    pending = balanceResponse.pending.amount,
                    price = balanceResponse.price
                )
            }
        }

    override suspend fun getAddresses(currencies: List<String>): Outcome<ApiError, List<NonCustodialDerivedAddress>> =
        dynamicSelfCustodyService.getAddresses(
            guidHash = getHashedString(payloadDataManager.guid),
            sharedKeyHash = getHashedString(payloadDataManager.sharedKey),
            currencies = currencies
        ).map { addressesResponse ->
            addressesResponse.addressEntries.map { addressEntry ->
                addressEntry.addresses.map { derivedAddress ->
                    NonCustodialDerivedAddress(
                        pubKey = derivedAddress.pubKey,
                        address = derivedAddress.address,
                        includesMemo = derivedAddress.includesMemo,
                        format = derivedAddress.format,
                        default = derivedAddress.default,
                        accountIndex = addressEntry.accountInfo.index
                    )
                }
            }.flatten()
        }

    override suspend fun getTransactionHistory(
        currency: String,
        contractAddress: String?
    ): Outcome<ApiError, List<NonCustodialTxHistoryItem>> =
        dynamicSelfCustodyService.getTransactionHistory(
            guidHash = getHashedString(payloadDataManager.guid),
            sharedKeyHash = getHashedString(payloadDataManager.sharedKey),
            currency = currency,
            contractAddress = contractAddress
        ).map { response ->
            response.history.map { historyItem ->
                historyItem.toHistoryEvent()
            }
        }

    private fun getHashedString(input: String): String = String(Hex.encode(Sha256Hash.hash(input.toByteArray())))
}

private fun TransactionResponse.toHistoryEvent(): NonCustodialTxHistoryItem {
    val sourceAddress = movements.firstOrNull { transactionMovement ->
        transactionMovement.type == TransactionDirection.SENT
    }?.address ?: ""
    val targetAddress = movements.firstOrNull { transactionMovement ->
        transactionMovement.type == TransactionDirection.RECEIVED
    }?.address ?: ""

    return NonCustodialTxHistoryItem(
        txId = txId,
        value = movements.first().amount,
        from = sourceAddress,
        to = targetAddress,
        timestamp = timestamp,
        fee = fee,
        status = status ?: Status.PENDING
    )
}
