package com.shopify.rnandroidauto;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactInstanceManagerBuilder;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.CatalystInstance;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.modules.appregistry.AppRegistry;
import com.facebook.react.modules.core.TimingModule;
import androidx.car.app.CarAppService;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.Session;
import androidx.car.app.hardware.info.CarInfo;
import androidx.car.app.validation.HostValidator;

import com.shopify.rnandroidauto.AndroidAutoModule;
import com.shopify.rnandroidauto.AndroidAutoPackage;

public final class CarService extends CarAppService {
    private ReactInstanceManager mReactInstanceManager;
    private CarScreen screen;

    private CarContext mCarContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mReactInstanceManager = ((ReactApplication) getApplication()).getReactNativeHost().getReactInstanceManager();
        // mReactInstanceManager = makeInstance();
    }

    @NonNull
    @Override
    public HostValidator createHostValidator() {
        return null;
    }

    private ReactInstanceManager makeInstance() {
        ReactInstanceManagerBuilder builder =
                ReactInstanceManager.builder()
                        .setApplication(getApplication())
                        .setJSMainModulePath("android_auto")
                        .setUseDeveloperSupport(true)
                        // .setRedBoxHandler(new CarRedBoxHandler())
                        .setJSIModulesPackage(null)
                        .setInitialLifecycleState(LifecycleState.BEFORE_CREATE);

        // String jsBundleFile = null;
        // if (jsBundleFile != null) {
        //     builder.setJSBundleFile(jsBundleFile);
        // } else {
        //     builder.setBundleAssetName(Assertions.assertNotNull("index.android.bundle"));
        // }

        builder.addPackage(new AndroidAutoPackage());

        ReactInstanceManager reactInstanceManager = builder.build();

        return reactInstanceManager;
    }

    public Session onCreateSession() {
        return new Session() {
            @NonNull
            @Override
            public Screen onCreateScreen(@NonNull Intent intent) {
                mCarContext = getCarContext();
//                CarInfo carInfo = getCarContext().getCarService(CarHardwareManager.class).getCarInfo();
                screen = new CarScreen(getCarContext(), mReactInstanceManager.getCurrentReactContext());
                screen.setMarker("root");
                runJsApplication();

                return screen;
            }

            @Override
            public void onCarConfigurationChanged(@NonNull Configuration newConfiguration) {
                super.onCarConfigurationChanged(newConfiguration);
            }

            @Override
            public void onNewIntent(@NonNull Intent intent) {
                super.onNewIntent(intent);
            }
        };
    }

    private void runJsApplication() {
//        mReactInstanceManager.getDevSupportManager().setHotModuleReplacementEnabled(false);
        ReactContext reactContext = mReactInstanceManager.getCurrentReactContext();

        if (reactContext == null) {
            mReactInstanceManager.addReactInstanceEventListener(
                    new ReactInstanceManager.ReactInstanceEventListener() {
                        @Override
                        public void onReactContextInitialized(ReactContext reactContext) {
                            invokeStartTask(reactContext);
                            mReactInstanceManager.removeReactInstanceEventListener(this);
                        }
                    });
            mReactInstanceManager.createReactContextInBackground();
        } else {
            invokeStartTask(reactContext);
        }
    }

    private void invokeStartTask(ReactContext reactContext) {
        try {
            if (mReactInstanceManager == null) {
                return;
            }

            if (reactContext == null) {
                return;
            }

            CatalystInstance catalystInstance = reactContext.getCatalystInstance();
            String jsAppModuleName = "android_auto";

            WritableNativeMap appParams = new WritableNativeMap();
            appParams.putDouble("rootTag", 1.0);
            @Nullable Bundle appProperties = Bundle.EMPTY;
            if (appProperties != null) {
                appParams.putMap("initialProps", Arguments.fromBundle(appProperties));
            }

            catalystInstance.getJSModule(AppRegistry.class).runApplication(jsAppModuleName, appParams);
            TimingModule timingModule = reactContext.getNativeModule(TimingModule.class);

            AndroidAutoModule carModule = mReactInstanceManager
                    .getCurrentReactContext()
                    .getNativeModule(AndroidAutoModule.class);
            carModule.setCarContext(mCarContext, screen);

            timingModule.onHostResume();
        } finally {
        }
    }
}
