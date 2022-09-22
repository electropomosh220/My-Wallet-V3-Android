package com.blockchain.nfts.data.repository

import com.blockchain.api.nfts.data.NftAssetsDto
import com.blockchain.data.DataResource
import com.blockchain.data.FreshnessStrategy
import com.blockchain.nfts.data.dataresources.NftCollectionStore
import com.blockchain.nfts.domain.models.NftAsset
import com.blockchain.nfts.domain.models.NftData
import com.blockchain.nfts.domain.models.NftTrait
import com.blockchain.nfts.domain.service.NftService
import com.blockchain.store.mapData
import kotlinx.coroutines.flow.Flow

class NftRepository(private val nftCollectionStore: NftCollectionStore) : NftService {

    override suspend fun getNftForAddress(
        freshnessStrategy: FreshnessStrategy,
        network: String,
        address: String
    ): Flow<DataResource<List<NftAsset>>> {
        return nftCollectionStore.stream(freshnessStrategy)
            .mapData {
                it.mapToDomain()
            }
    }

    private fun NftAssetsDto.mapToDomain(): List<NftAsset> =
        this.assets.map { nftAsset ->
            NftAsset(
                tokenId = nftAsset.tokenId.orEmpty(),
                iconUrl = nftAsset.imageUrl ?: nftAsset.imagePreviewUrl.orEmpty(),
                nftData = NftData(
                    name = nftAsset.name.orEmpty(),
                    description = nftAsset.description.orEmpty(),
                    traits = nftAsset.traits.map { nftTrait ->
                        NftTrait(
                            name = nftTrait.name,
                            value = nftTrait.value
                        )
                    }
                )
            )
        }
}
