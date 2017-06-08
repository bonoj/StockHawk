package com.udacity.stockhawk.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.udacity.stockhawk.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PrefUtils {

    private PrefUtils() {

    }

    public static Set<String> getStocks(Context context) {
        String stocksKey = context.getString(R.string.pref_stocks_key);
        String initializedKey = context.getString(R.string.pref_stocks_initialized_key);
        String[] defaultStocksList = context.getResources().getStringArray(R.array.default_stocks);

        HashSet<String> defaultStocks = new HashSet<>(Arrays.asList(defaultStocksList));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);


        boolean initialized = prefs.getBoolean(initializedKey, false);

        if (!initialized) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(initializedKey, true);
            editor.putStringSet(stocksKey, defaultStocks);
            editor.apply();
            return defaultStocks;
        }
        return prefs.getStringSet(stocksKey, new HashSet<String>());

    }

    private static void editStockPref(Context context, String symbol, Boolean add) {
        String key = context.getString(R.string.pref_stocks_key);
        Set<String> stocks = getStocks(context);

        if (add) {
            stocks.add(symbol);
        } else {
            stocks.remove(symbol);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(key, stocks);
        editor.apply();
    }

    public static void addStock(Context context, String symbol) {
        editStockPref(context, symbol, true);
    }

    public static void removeStock(Context context, String symbol) {
        editStockPref(context, symbol, false);
    }

    public static String getDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String defaultValue = context.getString(R.string.pref_display_mode_default);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, defaultValue);
    }

    public static void toggleDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String absoluteKey = context.getString(R.string.pref_display_mode_absolute_key);
        String percentageKey = context.getString(R.string.pref_display_mode_percentage_key);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String displayMode = getDisplayMode(context);

        SharedPreferences.Editor editor = prefs.edit();

        if (displayMode.equals(absoluteKey)) {
            editor.putString(key, percentageKey);
        } else {
            editor.putString(key, absoluteKey);
        }

        editor.apply();
    }


    public static boolean getSymbolStatus(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(context.getString(R.string.pref_symbol_status_key), false);
    }

    private static String[] reverseHistoricalDataArray(String[] historicalDataArray) {
        List<String> list = Arrays.asList(historicalDataArray);
        Collections.reverse(list);
        return (String[]) list.toArray();
    }

    public static LineGraphSeries<DataPoint> getGraphData(String historicalData, double currentPrice) {
        if (historicalData != null && historicalData.length() != 0) {
            String[] historicalDataArray = historicalData.split("\\n");

            // Uncomment line below if YahooFinanceAPI changes the historical data again
            // historicalDataArray = reverseHistoricalDataArray(historicalDataArray);

            DataPoint[] values = new DataPoint[historicalDataArray.length];

            double initialValue = 0;

            for (int i = 0; i < historicalDataArray.length; i++) {
                String valuePairs = historicalDataArray[i];
                String[] vpString = valuePairs.split(",");
                double price = Double.valueOf(vpString[1]);
                if (i == 0) {
                    initialValue = price;
                }
                DataPoint v = new DataPoint(i, price);
                values[i] = v;
            }
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(values);
            if (initialValue < currentPrice) {
                series.setColor(Color.parseColor("#00C853")); // Graph green line
            } else if (initialValue > currentPrice) {
                series.setColor(Color.parseColor("#D50000")); // Graph red line
            } else {
                series.setColor(Color.parseColor("#FFFFFF")); // Graph white line, extremely rare case
            }
            return series;
        }
        return null;

    }

    public static void formatGraph(GraphView graph) {
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);
        double minX = graph.getViewport().getMinX(true);
        double maxX = graph.getViewport().getMaxX(true);
        double minY = graph.getViewport().getMinY(true);
        double maxY = graph.getViewport().getMaxY(true);
        graph.getViewport().setMinX(minX);
        graph.getViewport().setMaxX(maxX);
        graph.getViewport().setMinY(minY);
        graph.getViewport().setMaxY(maxY);
        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
        graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
    }

    public static double getInitialValue(String historicalData) {
        if (historicalData != null && historicalData.length() != 0) {
            String[] historicalDataArray = historicalData.split("\\n");

            // Uncomment line below if YahooFinanceAPI changes the historical data again
            // historicalDataArray = reverseHistoricalDataArray(historicalDataArray);

            String initialValuePair = historicalDataArray[0];
            String[] ivpString = initialValuePair.split(",");
            return Double.valueOf(ivpString[1]);
        }
        return 0;
    }

    public static String getA11yStockSymbol(Context context, String stockSymbol) {
        String a11yStockSymbol = context.getString(R.string.a11y_symbol) + " ,";
        for (int i = 0; i < stockSymbol.length(); i++) {
            if (stockSymbol.charAt(i) == 'A') {
                a11yStockSymbol = a11yStockSymbol + context.getString(R.string.a11y_a) + ",";
            } else if (stockSymbol.charAt(i) == 'F') {
                a11yStockSymbol = a11yStockSymbol + context.getString(R.string.a11y_f) + ",";
            } else {
                a11yStockSymbol = a11yStockSymbol + stockSymbol.charAt(i) + ",";
            }
        }
        return a11yStockSymbol;
    }
}
