package com.shopify.rnandroidauto;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.model.Pane;
import androidx.car.app.model.PaneTemplate;
import androidx.car.app.Screen;
import androidx.car.app.model.Template;

import com.facebook.react.bridge.ReactContext;

public class CarScreen extends Screen {
    private Template mTemplate;

    public CarScreen(CarContext carContext, ReactContext reactContext) {
        super(carContext);
    }

    public void setTemplate(Template template) {
        mTemplate = template;
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        if (mTemplate != null) {
            return mTemplate;
        }

        return new PaneTemplate.Builder(
                new Pane.Builder().setLoading(true).build()
        ).setTitle("Shopify Local Delivery").build();
    }
}
