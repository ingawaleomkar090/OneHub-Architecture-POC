package com.catalent.core.common;

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
public final class AppDispatchers_Factory implements Factory<AppDispatchers> {
  @Override
  public AppDispatchers get() {
    return newInstance();
  }

  public static AppDispatchers_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AppDispatchers newInstance() {
    return new AppDispatchers();
  }

  private static final class InstanceHolder {
    static final AppDispatchers_Factory INSTANCE = new AppDispatchers_Factory();
  }
}
