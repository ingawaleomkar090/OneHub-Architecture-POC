package com.catalent.onehub.presentation.ui;

import com.catalent.core.network.ClientProvider;
import com.catalent.core.network.NetworkManager;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<ClientProvider> clientProvider;

  private final Provider<NetworkManager> networkManagerProvider;

  public MainActivity_MembersInjector(Provider<ClientProvider> clientProvider,
      Provider<NetworkManager> networkManagerProvider) {
    this.clientProvider = clientProvider;
    this.networkManagerProvider = networkManagerProvider;
  }

  public static MembersInjector<MainActivity> create(Provider<ClientProvider> clientProvider,
      Provider<NetworkManager> networkManagerProvider) {
    return new MainActivity_MembersInjector(clientProvider, networkManagerProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectClientProvider(instance, clientProvider.get());
    injectNetworkManager(instance, networkManagerProvider.get());
  }

  @InjectedFieldSignature("com.catalent.onehub.presentation.ui.MainActivity.clientProvider")
  public static void injectClientProvider(MainActivity instance, ClientProvider clientProvider) {
    instance.clientProvider = clientProvider;
  }

  @InjectedFieldSignature("com.catalent.onehub.presentation.ui.MainActivity.networkManager")
  public static void injectNetworkManager(MainActivity instance, NetworkManager networkManager) {
    instance.networkManager = networkManager;
  }
}
