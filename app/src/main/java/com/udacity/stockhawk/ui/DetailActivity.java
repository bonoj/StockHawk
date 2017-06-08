package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.udacity.stockhawk.data.Contract.Quote.POSITION_HISTORY;
import static com.udacity.stockhawk.data.Contract.Quote.POSITION_PRICE;
import static com.udacity.stockhawk.data.Contract.Quote.POSITION_SYMBOL;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private final DecimalFormat dollarFormat;
    private final DecimalFormat percentageFormat;

    @BindView(R.id.detail_stock_symbol) TextView symbolTextView;
    @BindView(R.id.detail_timeframe) TextView timeframeTextView;
    @BindView(R.id.detail_percentage) TextView percentageTextView;
    @BindView(R.id.detail_percentage_label) TextView percentageLabelTextView;
    @BindView(R.id.detail_lowest_price) TextView lowestPriceTextView;
    @BindView(R.id.detail_lowest_label) TextView lowestLabelTextView;
    @BindView(R.id.detail_highest_price) TextView highestPriceTextView;
    @BindView(R.id.detail_highest_label) TextView highestLabelTextView;
    @BindView(R.id.detail_current_price) TextView currentPriceTextView;
    @BindView(R.id.detail_current_label) TextView currentLabelTextView;

    private static final int STOCK_DETAIL_LOADER = 1;

    private Uri mUri;

    public DetailActivity() {
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        String stockSymbol = intent.getStringExtra(MainActivity.STOCK_DETAIL_SYMBOL);
        mUri = Contract.Quote.makeUriForStock(stockSymbol);

        getSupportLoaderManager().initLoader(STOCK_DETAIL_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            return new CursorLoader(this,
                    mUri,
                    Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                    null, null, Contract.Quote.COLUMN_SYMBOL);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursor.moveToFirst();
        String stockSymbol = cursor.getString(POSITION_SYMBOL);
        String historicalData = cursor.getString(POSITION_HISTORY);
        String historyTimeframe = String.valueOf(QuoteSyncJob.YEARS_OF_HISTORY) + " " +
                getString(R.string.years);
        double currentPrice = cursor.getDouble(POSITION_PRICE);

        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.addSeries(PrefUtils.getGraphData(historicalData, currentPrice));
        PrefUtils.formatGraph(graph);


        double initialPrice = PrefUtils.getInitialValue(historicalData);
        double lowestPrice = graph.getViewport().getMinY(true);
        double highestPrice = graph.getViewport().getMaxY(true);
        double percentageChange = (currentPrice - initialPrice) / initialPrice;

        String lowestPriceString = dollarFormat.format(lowestPrice);
        String highestPriceString = dollarFormat.format(highestPrice);
        String percentageChangeString = percentageFormat.format(percentageChange);
        String currentPriceString = dollarFormat.format(currentPrice);

        String a11yStockSymbol = PrefUtils.getA11yStockSymbol(this, stockSymbol);

        symbolTextView.setText(stockSymbol);
        timeframeTextView.setText(historyTimeframe);
        percentageTextView.setText(percentageChangeString);
        percentageLabelTextView.setText(getString(R.string.percentage_change));
        lowestPriceTextView.setText(lowestPriceString);
        lowestLabelTextView.setText(getString(R.string.lowest_value));
        highestPriceTextView.setText(highestPriceString);
        highestLabelTextView.setText(getString(R.string.highest_value));
        currentPriceTextView.setText(currentPriceString);
        currentLabelTextView.setText(getString(R.string.current_value));

        // Assign content descriptions for a11y
        symbolTextView.setContentDescription(historyTimeframe + " " + getString(R.string.a11y_for) + " " + a11yStockSymbol);
        lowestPriceTextView.setContentDescription(getString(R.string.a11y_lowest_value) + " " + lowestPriceString);
        highestPriceTextView.setContentDescription(getString(R.string.a11y_highest_value) + " " + highestPriceString);
        currentPriceTextView.setContentDescription(getString(R.string.a11y_current_value) + " " + currentPriceString);
        percentageTextView.setContentDescription(getString(R.string.a11y_percentage_change) + " " + percentageChangeString);
        if (currentPrice < initialPrice) {
            graph.setContentDescription(getString(R.string.a11y_graph_downward));
        } else if (currentPrice > initialPrice) {
            graph.setContentDescription(getString(R.string.a11y_graph_upward));
        } else {
            graph.setContentDescription(getString(R.string.a11y_graph_neutral)); // extremely rare case
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}