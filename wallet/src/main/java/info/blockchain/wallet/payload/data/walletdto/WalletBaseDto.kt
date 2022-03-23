package info.blockchain.wallet.payload.data.walletdto

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    creatorVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
@Serializable
class WalletBaseDto(
    // payload could be string in V1
    // V2 and up is WalletWrapper
    @field:JsonProperty("payload")
    @SerialName("payload")
    var payload: String? = null,

    // V3
    @field:JsonProperty("guid")
    @SerialName("guid")
    var guid: String? = null,

    @field:JsonProperty("extra_seed")
    @SerialName("extra_seed")
    var extraSeed: String? = null,

    @field:JsonProperty("payload_checksum")
    @SerialName("payload_checksum")
    var payloadChecksum: String? = null,

    @field:JsonProperty("war_checksum")
    @SerialName("war_checksum")
    var warChecksum: String? = null,

    @field:JsonProperty("language")
    @SerialName("language")
    var language: String? = null,

    @field:JsonProperty("storage_token")
    @SerialName("storage_token")
    var storageToken: String? = null,

    @field:JsonProperty("sync_pubkeys")
    @SerialName("sync_pubkeys")
    var syncPubkeys: Boolean = false
) {
    companion object {
        @JvmStatic
        fun fromJson(json: String): WalletBaseDto {
            val jsonBuilder = Json {
                ignoreUnknownKeys = true
            }
            return jsonBuilder.decodeFromString(json)
        }
    }
}