package com.blockchain.api.services

import com.blockchain.api.nfts.api.NftApi
import com.blockchain.api.nfts.data.NftAssetsDto
import io.reactivex.rxjava3.core.Single

class NftApiService internal constructor(
    private val nftApi: NftApi
) {
    fun getNftsForAddress(network: String = "ETH", address: String): Single<NftAssetsDto> =
        nftApi.getAssetsForAddress(
            ownerAddress = address
            //            requestBody = NftAssetRequestBody(
            //                network = network,
            //                address = address
            //            )
        )
}
