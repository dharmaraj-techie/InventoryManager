package com.example.dharmaraj.inventorymanager.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.example.dharmaraj.inventorymanager.R;


public class UpdateWidgetIntentService extends IntentService {

    private static final String ACTION_UPDATE_PRODUCT_WIDGET = "com.example.dharmaraj.inventorymanager.action.UPDATE_PRODUCT_LIST_WIDGET";


    public UpdateWidgetIntentService() {
        super("UpdateWidgetIntentService");
    }

    /**
     * Starts this service to perform action update the product widgets. If
     * the service is already performing a task this action will be queued.
     */
    public static void startActionUpdateProductWidget(Context context) {
        Intent intent = new Intent(context, UpdateWidgetIntentService.class);
        intent.setAction(ACTION_UPDATE_PRODUCT_WIDGET);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_PRODUCT_WIDGET.equals(action)) {
                handleActionUpdateProductWidget();
            }
        }
    }

    /**
     * Handle action update the product widgets in the provided background thread
     */
    private void handleActionUpdateProductWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, ProductListWidgetProvider.class));
        //Trigger data update to handle the GridView widgets and force a data refresh
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view);
        //Now update all widgets
        ProductListWidgetProvider.updateAppWidgets(this, appWidgetManager, appWidgetIds);
    }

}
