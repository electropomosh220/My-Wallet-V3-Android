package com.blockchain.featureflags

interface InternalFeatureFlagApi {
    fun isFeatureEnabled(gatedFeature: GatedFeature): Boolean
    fun enable(gatedFeature: GatedFeature)
    fun disable(gatedFeature: GatedFeature)
    fun disableAll()
    fun getAll(): Map<GatedFeature, Boolean>
}

enum class GatedFeature(
    val readableName: String,
    val enabledForCompanyInternalBuild: Boolean = false
) {
    ADD_SUB_WALLET_ADDRESSES("Create BTC sub-wallets"),
    NEW_ONBOARDING("New onboarding design"),
    NEW_SPLIT_DASHBOARD("Enable New Dashboard", true),
    AUTOCOMPLETE_ADDRESS("Enable autocomplete address kyc flow"),
    WITHDRAWAL_LOCKS("Enable Withdrawal locks warning")
}