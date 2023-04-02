package com.shopify.rnandroidauto;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.car.app.model.Action;
import androidx.car.app.model.ActionStrip;
import androidx.car.app.model.CarColor;
import androidx.car.app.model.CarLocation;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.ListTemplate;
import androidx.car.app.model.Metadata;
import androidx.car.app.model.Pane;
import androidx.car.app.model.PaneTemplate;
import androidx.car.app.model.Place;
import androidx.car.app.model.PlaceListMapTemplate;
import androidx.car.app.model.PlaceMarker;
import androidx.car.app.model.Row;
import androidx.car.app.model.SectionedItemList;
import androidx.car.app.model.Template;

import com.facebook.react.bridge.NoSuchKeyException;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableNativeMap;

import java.util.ArrayList;

public class TemplateParser {
    private ReactCarRenderContext mReactCarRenderContext;

    TemplateParser(ReactCarRenderContext reactCarRenderContext) {
        mReactCarRenderContext = reactCarRenderContext;
    }

    public Template parseTemplate(ReadableMap renderMap) {
        String type = renderMap.getString("type");

        switch (type) {
            case "list-template":
                return parseListTemplateChildren(renderMap);
            case "place-list-map-template":
                return parsePlaceListMapTemplate(renderMap);
            case "pane-template":
                return parsePaneTemplate(renderMap);
            default:
                return new PaneTemplate.Builder(
                        new Pane.Builder().setLoading(true).build()
                ).setTitle("Shopify Local Delivery").build();
        }
    }

    private PaneTemplate parsePaneTemplate(ReadableMap map) {
        Pane.Builder paneBuilder = new Pane.Builder();

        ReadableArray children = map.getArray("children");

        boolean loading;

        try {
            loading = map.getBoolean("isLoading");
        } catch (NoSuchKeyException e) {
            loading = children.size() == 0;
        }

        paneBuilder.setLoading(false);


        ArrayList<Action> actions = new ArrayList();

        if (!loading) {
            for (int i = 0; i < children.size(); i++) {
                ReadableMap child = children.getMap(i);
                String type = child.getString("type");

                if (type.equals("row")) {
                    paneBuilder.addRow(buildRow(child));
                }

                if (type.equals("action")) {
                    actions.add(parseAction(child));
                }
            }

            if (actions.size() > 0) {
                for (int i = 0; i < actions.size(); i++) {
                    paneBuilder.addAction(actions.get(i));
                }
            }
        }

        PaneTemplate.Builder Builder = new PaneTemplate.Builder(paneBuilder.build());

        Builder.setTitle(map.getString("title"));

        try {
            Builder.setHeaderAction(getHeaderAction(map.getString("headerAction")));
        } catch (NoSuchKeyException e) {
        }

        try {
            ReadableMap actionStripMap = map.getMap("actionStrip");
            Builder.setActionStrip(parseActionStrip(actionStripMap));
        } catch (NoSuchKeyException e) {
        }


        return Builder.build();
    }

    private ActionStrip parseActionStrip(ReadableMap map) {
        ActionStrip.Builder Builder = new ActionStrip.Builder();

        ReadableArray actions = map.getArray("actions");

        for (int i = 0; i < actions.size(); i++) {
            ReadableMap actionMap = actions.getMap(i);
            Action action = parseAction(actionMap);
            Builder.addAction(action);
        }

        return Builder.build();
    }

    private Action parseAction(ReadableMap map) {
        Action.Builder Builder = new Action.Builder();

        Builder.setTitle(map.getString("title"));
        try {
            Builder.setBackgroundColor(getColor(map.getString("backgroundColor")));
        } catch (NoSuchKeyException e) {
        }

        try {
            int onPress = map.getInt("onPress");

            Builder.setOnClickListener(() -> {
                invokeCallback(onPress);
            });
        } catch (NoSuchKeyException e) {
        }

        return Builder.build();
    }

    private CarColor getColor(String colorName) {
        switch (colorName) {
            case "blue":
                return CarColor.BLUE;
            case "green":
                return CarColor.GREEN;
            case "primary":
                return CarColor.PRIMARY;
            case "red":
                return CarColor.RED;
            case "secondary":
                return CarColor.SECONDARY;
            case "yellow":
                return CarColor.YELLOW;
            default:
            case "default":
                return CarColor.DEFAULT;
        }
    }

    private PlaceListMapTemplate parsePlaceListMapTemplate(ReadableMap map) {
        PlaceListMapTemplate.Builder Builder = new PlaceListMapTemplate.Builder();

        Builder.setTitle(map.getString("title"));
        ReadableArray children = map.getArray("children");


        try {
            Builder.setHeaderAction(getHeaderAction(map.getString("headerAction")));
        } catch (NoSuchKeyException e) {
        }

        boolean loading;

        try {
            loading = map.getBoolean("isLoading");
        } catch (NoSuchKeyException e) {
            loading = children.size() == 0;
        }

        Builder.setLoading(loading);

        if (!loading) {
            ItemList.Builder itemListBuilder = new ItemList.Builder();

            for (int i = 0; i < children.size(); i++) {
                ReadableMap child = children.getMap(i);
                String type = child.getString("type");

                if (type.equals("row")) {
                    itemListBuilder.addItem(buildRow(child));
                }
            }

            Builder.setItemList(itemListBuilder.build());
        }


        try {
            ReadableMap actionStripMap = map.getMap("actionStrip");
            Builder.setActionStrip(parseActionStrip(actionStripMap));
        } catch (NoSuchKeyException e) {
        }

        return Builder.build();
    }

    private ListTemplate parseListTemplateChildren(ReadableMap map) {
        ReadableArray children = map.getArray("children");

        ListTemplate.Builder Builder = new ListTemplate.Builder();

        boolean loading;

        try {
            loading = map.getBoolean("isLoading");
        } catch (NoSuchKeyException e) {
            loading = children.size() == 0;
        }

        Builder.setLoading(loading);

        if (!loading) {
            for (int i = 0; i < children.size(); i++) {
                ReadableMap child = children.getMap(i);
                String type = child.getString("type");
                if (type.equals("item-list")) {
                    Builder.setSingleList(parseItemListChildren(child));
                } else if (type.equals("section-list")) {
                    Builder.addSectionedList(SectionedItemList.create(parseItemListChildren(child), child.getString("header")));
                }
            }
        }

        try {
            Builder.setHeaderAction(getHeaderAction(map.getString("headerAction")));
        } catch (NoSuchKeyException e) {
        }

        try {
            ReadableMap actionStripMap = map.getMap("actionStrip");
            Builder.setActionStrip(parseActionStrip(actionStripMap));
        } catch (NoSuchKeyException e) {
        }

        Builder.setTitle(map.getString("title"));

        return Builder.build();
    }

    private ItemList parseItemListChildren(ReadableMap itemList) {
        ReadableArray children = itemList.getArray("children");
        ItemList.Builder Builder = new ItemList.Builder();

        for (int i = 0; i < children.size(); i++) {
            ReadableMap child = children.getMap(i);
            String type = child.getString("type");
            if (type.equals("row")) {
                Builder.addItem(buildRow(child));
            }
        }

        try {
            Builder.setNoItemsMessage(itemList.getString("noItemsMessage"));
        } catch (NoSuchKeyException e) {
        }

        return Builder.build();
    }

    @NonNull
    private Row buildRow(ReadableMap rowRenderMap) {
        Row.Builder Builder = new Row.Builder();

        Builder.setTitle(rowRenderMap.getString("title"));

        try {
            ReadableArray texts = rowRenderMap.getArray("texts");

            for (int i = 0; i < texts.size(); i++) {
                Builder.addText(texts.getString(i));
            }
        } catch (NoSuchKeyException e) {
        }

        try {
            int onPress = rowRenderMap.getInt("onPress");

            Builder.setBrowsable(true);

            Builder.setOnClickListener(() -> {
                invokeCallback(onPress);
            });
        } catch (NoSuchKeyException e) {
        }

        try {
            Builder.setMetadata(parseMetaData(rowRenderMap.getMap("metadata")));
        } catch (NoSuchKeyException e) {
        }

        return Builder.build();
    }

    private Metadata parseMetaData(ReadableMap map) {
        switch (map.getString("type")) {
            case "place":
                return new Metadata.Builder()
                        .setPlace(
                                new Place.Builder(CarLocation.create(map.getDouble("latitude"), map.getDouble("longitude")))
                                        .setMarker(new PlaceMarker.Builder().build())
                                        .build()
                        ).build();
            default:
                return null;
        }
    }

    private Action getHeaderAction(String actionName) {
        switch (actionName) {
            case "back":
                return Action.BACK;
            case "app_icon":
                return Action.APP_ICON;
            default:
                return null;
        }
    }

    private void invokeCallback(int callbackId) {
        invokeCallback(callbackId, null);
    }

    private void invokeCallback(int callbackId, WritableNativeMap params) {
        if (params == null) {
            params = new WritableNativeMap();
        }

        params.putInt("id", callbackId);
        params.putString("screen", mReactCarRenderContext.getScreenMarker());

        mReactCarRenderContext.getEventCallback().invoke(params);
    }
}
