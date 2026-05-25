package com.catalent.shipment.data.di

import com.catalent.shipment.data.repository.SalesforceSObjectRepository
import com.catalent.shipment.domain.repository.SObjectRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ShipmentDataModule {

    @Binds
    @Singleton
    abstract fun bindSObjectRepository(
        impl: SalesforceSObjectRepository,
    ): SObjectRepository
}