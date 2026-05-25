package com.catalent.core.network;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class ClientProvider_Factory implements Factory<ClientProvider> {
  @Override
  public ClientProvider get() {
    return newInstance();
  }

  public static ClientProvider_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ClientProvider newInstance() {
    return new ClientProvider();
  }

  private static final class InstanceHolder {
    static final ClientProvider_Factory INSTANCE = new ClientProvider_Factory();
  }
}
