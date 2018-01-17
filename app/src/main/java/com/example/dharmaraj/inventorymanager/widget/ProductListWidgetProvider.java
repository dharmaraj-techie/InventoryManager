package com.example.dharmaraj.inventorymanager.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.dharmaraj.inventorymanager.R;
import com.example.dharmaraj.inventorymanager.ui.CatalogActivity;
import com.example.dharmaraj.inventorymanager.ui.ProductDetailActivity;

/**
 * Implementation of App Widget functionality.
 */
public class ProductListWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.product_list_widget);

        Intent backIntent = new Intent(context, CatalogActivity.class);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent action = new Intent(context, ProductDetailActivity.class);

        PendingIntent operation =
                PendingIntent.getActivities(context, 0, new Intent[]{backIntent, action}, PendingIntent.FLAG_CANCEL_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_list_view, operation);


        Intent intent = new Intent(context, ProductWidgetService.class);
        views.setRemoteAdapter(R.id.widget_list_view, intent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void updateAppWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        UpdateWidgetIntentService.startActionUpdateProductWidget(context);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

