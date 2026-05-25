package com.catalent.auth.data.di;

import com.catalent.auth.domain.AuthRepository;
import com.catalent.auth.domain.usecase.LogoutUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AuthDataModule_Companion_ProvideLogoutUseCaseFactory implements Factory<LogoutUseCase> {
  private final Provider<AuthRepository> authRepositoryProvider;

  public AuthDataModule_Companion_ProvideLogoutUseCaseFactory(
      Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public LogoutUseCase get() {
    return provideLogoutUseCase(authRepositoryProvider.get());
  }

  public static AuthDataModule_Companion_ProvideLogoutUseCaseFactory create(
      Provider<AuthRepository> authRepositoryProvider) {
    return new AuthDataModule_Companion_ProvideLogoutUseCaseFactory(authRepositoryProvider);
  }

  public static LogoutUseCase provideLogoutUseCase(AuthRepository authRepository) {
    return Preconditions.checkNotNullFromProvides(AuthDataModule.Companion.provideLogoutUseCase(authRepository));
  }
}
