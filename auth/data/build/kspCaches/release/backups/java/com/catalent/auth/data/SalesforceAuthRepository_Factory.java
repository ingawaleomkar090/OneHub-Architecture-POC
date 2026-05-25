package com.catalent.auth.data;

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
public final class SalesforceAuthRepository_Factory implements Factory<SalesforceAuthRepository> {
  private final Provider<ClientProvider> clientProvider;

  public SalesforceAuthRepository_Factory(Provider<ClientProvider> clientProvider) {
    this.clientProvider = clientProvider;
  }

  @Override
  public SalesforceAuthRepository get() {
    return newInstance(clientProvider.get());
  }

  public static SalesforceAuthRepository_Factory create(Provider<ClientProvider> clientProvider) {
    return new SalesforceAuthRepository_Factory(clientProvider);
  }

  public static SalesforceAuthRepository newInstance(ClientProvider clientProvider) {
    return new SalesforceAuthRepository(clientProvider);
  }
}
