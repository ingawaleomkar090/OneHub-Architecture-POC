package com.catalent.shipment.ui.presentation.viewmodel;

import com.catalent.shipment.domain.repository.SObjectRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class SObjectListViewModel_Factory implements Factory<SObjectListViewModel> {
  private final Provider<SObjectRepository> repositoryProvider;

  public SObjectListViewModel_Factory(Provider<SObjectRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public SObjectListViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static SObjectListViewModel_Factory create(
      Provider<SObjectRepository> repositoryProvider) {
    return new SObjectListViewModel_Factory(repositoryProvider);
  }

  public static SObjectListViewModel newInstance(SObjectRepository repository) {
    return new SObjectListViewModel(repository);
  }
}
