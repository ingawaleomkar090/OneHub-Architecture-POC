package com.catalent.shipment.data.repository;

import com.catalent.core.network.ClientProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class SalesforceSObjectRepository_Factory implements Factory<SalesforceSObjectRepository> {
  private final Provider<ClientProvider> clientProvider;

  public SalesforceSObjectRepository_Factory(Provider<ClientProvider> clientProvider) {
    this.clientProvider = clientProvider;
  }

  @Override
  public SalesforceSObjectRepository get() {
    return newInstance(clientProvider.get());
  }

  public static SalesforceSObjectRepository_Factory create(
      Provider<ClientProvider> clientProvider) {
    return new SalesforceSObjectRepository_Factory(clientProvider);
  }

  public static SalesforceSObjectRepository newInstance(ClientProvider clientProvider) {
    return new SalesforceSObjectRepository(clientProvider);
  }
}
