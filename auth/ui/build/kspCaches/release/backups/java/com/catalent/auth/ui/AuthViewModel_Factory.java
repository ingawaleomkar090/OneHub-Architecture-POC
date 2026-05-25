package com.catalent.auth.ui;

import com.catalent.auth.domain.AuthRepository;
import com.catalent.auth.domain.usecase.LogoutUseCase;
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
public final class AuthViewModel_Factory implements Factory<AuthViewModel> {
  private final Provider<LogoutUseCase> logoutUseCaseProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  public AuthViewModel_Factory(Provider<LogoutUseCase> logoutUseCaseProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.logoutUseCaseProvider = logoutUseCaseProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public AuthViewModel get() {
    return newInstance(logoutUseCaseProvider.get(), authRepositoryProvider.get());
  }

  public static AuthViewModel_Factory create(Provider<LogoutUseCase> logoutUseCaseProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new AuthViewModel_Factory(logoutUseCaseProvider, authRepositoryProvider);
  }

  public static AuthViewModel newInstance(LogoutUseCase logoutUseCase,
      AuthRepository authRepository) {
    return new AuthViewModel(logoutUseCase, authRepository);
  }
}
