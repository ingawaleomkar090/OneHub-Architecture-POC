package com.catalent.shipment.data.repository;

import com.catalent.core.common.AppDispatchers;
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
  private final Provider<AppDispatchers> dispatchersProvider;

  public SalesforceSObjectRepository_Factory(Provider<AppDispatchers> dispatchersProvider) {
    this.dispatchersProvider = dispatchersProvider;
  }

  @Override
  public SalesforceSObjectRepository get() {
    return newInstance(dispatchersProvider.get());
  }

  public static SalesforceSObjectRepository_Factory create(
      Provider<AppDispatchers> dispatchersProvider) {
    return new SalesforceSObjectRepository_Factory(dispatchersProvider);
  }

  public static SalesforceSObjectRepository newInstance(AppDispatchers dispatchers) {
    return new SalesforceSObjectRepository(dispatchers);
  }
}
