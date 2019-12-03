/*
 HitoeProfileECGFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.fragment;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.achartengine.GraphicalView;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.deviceconnect.android.deviceplugin.hitoe.HitoeApplication;
import org.deviceconnect.android.deviceplugin.hitoe.R;
import org.deviceconnect.android.deviceplugin.hitoe.activity.HitoeDeviceControlActivity;
import org.deviceconnect.android.deviceplugin.hitoe.data.HeartRateData;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeConstants;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;
import org.deviceconnect.android.deviceplugin.hitoe.util.HitoeScheduler;

import java.util.ArrayList;
import java.util.List;


/**
 * This fragment do setting of the control ECG profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class HitoeProfileECGFragment extends Fragment implements HitoeScheduler.OnRegularNotify {

    /** Title size. */
    public static final int CHART_TITLE_SIZE = 25;
    /** Label size. */
    public static final int LABELS_SIZE = 16;
    /** Axis Title. */
    public static final int AXIS_TITLE_SIZE = 25;
    /** Axis color. */
    public static final int AXIS_COLOR = Color.GRAY;
    /** Grid color. */
    public static final int GRID_COLOR = Color.GRAY;
    /** Title color. */
    public static final int TITLE_COLOR = Color.GRAY;
    /** x label color. */
    public static final int XLABEL_COLOR = Color.GRAY;
    /** y label color. */
    public static final int YLABEL_COLOR = Color.GRAY;

    /** data count. */
    private static final int DATA_COUNT = 1;
    /** orientation title. */
    private static final String[] TITLES = new String[] {"ECG" };
    /** data color. */
    private static final int[] COLORS = new int[] {Color.GREEN };
    /** data count. */
    private static final long MAX_RANGE = 10000;

    /** ECG. */
    private List<XYSeries> mECGList = null;
    /** Graph data . */
    private LineChart mLineChart;
    /** Graph view. */
    private GraphicalView mGraphicalView;
    /** Graph render. */
    private XYMultipleSeriesRenderer mXYMultipleSeriesRenderer;
    /** min data. */
    private long mMinX = 0;
    /** max data. */
    private long mMaxX = mMinX + MAX_RANGE;



    /**
     * Current Hitoe Device object.
     */
    private HitoeDevice mCurrentDevice;

    /**
     * Hitoe Scheduler.
     */
    private HitoeScheduler mScheduler;

    @Override
    public View onCreateView(final LayoutInflater inflater, final @Nullable ViewGroup container,
                             final @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ecg_instructions, null);
        mScheduler = new HitoeScheduler(this, HitoeConstants.ACC_CHART_UPDATE_CYCLE_TIME,
                                                                HitoeConstants.ACC_CHART_UPDATE_CYCLE_TIME);

        rootView.findViewById(R.id.button_register).setOnClickListener((view) -> {
            clear();
            mScheduler.scanHitoeDevice(true);
        });
        rootView.findViewById(R.id.button_unregister).setOnClickListener((view) -> {
            mScheduler.scanHitoeDevice(false);
        });
        TextView title = rootView.findViewById(R.id.view_title);
        Bundle args = getArguments();
        if (args != null) {

            String serviceId = args.getString(HitoeDeviceControlActivity.FEATURE_SERVICE_ID);
            HitoeApplication app = (HitoeApplication) getActivity().getApplication();
            HitoeManager manager = app.getHitoeManager();

            mCurrentDevice = manager.getHitoeDeviceForServiceId(serviceId);
            if (mCurrentDevice != null) {
                String[] profiles = getResources().getStringArray(R.array.support_profiles);
                title.setText(profiles[3] + getString(R.string.title_control));
            }
        }
        init();
        ((LinearLayout) rootView.findViewById(R.id.ecg_chart)).addView(mGraphicalView);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScheduler.scanHitoeDevice(false);
    }


    @Override
    public void onRegularNotify() {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(() -> {
            HitoeApplication app = (HitoeApplication) getActivity().getApplication();
            HitoeManager manager = app.getHitoeManager();
            HeartRateData ecg = manager.getECGData(mCurrentDevice.getId());
            if (ecg != null) {
                setECG(ecg.getECG().getTimeStamp(), ecg.getECG().getValue());
                updateChart();
            }
        });
    }

    /**
     * Initialize ECG chart.
     */
    private void init() {
        this.mECGList = new ArrayList<>();
        this.mECGList.add(new XYSeries(TITLES[0]));
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addAllSeries(this.mECGList);

        this.mXYMultipleSeriesRenderer = buildRenderer();
        mLineChart = new LineChart(dataset, mXYMultipleSeriesRenderer);
        mGraphicalView = new GraphicalView(getActivity(), mLineChart);
    }

    /**
     * Set ECG data.
     * @param timestamp timestamp
     * @param ecg ecg data
     */
    private void setECG(final long timestamp, final double ecg) {
        if (mECGList.get(0).getItemCount() == 0) {
            mMinX = timestamp;
            mMaxX = timestamp + MAX_RANGE;
        }

        if (timestamp > mMaxX || mECGList.get(0).getItemCount() > MAX_RANGE / 40) {
            this.mECGList.get(0).clear();
            mMinX = timestamp;
            mMaxX = timestamp + MAX_RANGE;
        }
        mECGList.get(0).add(timestamp, ecg / 1000);
    }

    /**
     * Update chart.
     */
    private synchronized void updateChart() {
        mXYMultipleSeriesRenderer.setXAxisMin(mMinX);
        mXYMultipleSeriesRenderer.setXAxisMax(mMaxX);

        mGraphicalView.repaint();
    }

    /**
     * Clear chart.
     */
    private synchronized void clear() {
        this.mECGList.get(0).clear();
        mGraphicalView.repaint();
    }

    /**
     * Build ECG Chart renderer.
     * @return ecg chart renderer
     */
    private XYMultipleSeriesRenderer buildRenderer() {

        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

        for (int i = 0; i < DATA_COUNT; i++) {
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setColor(COLORS[i]);
            r.setLineWidth(4f);
            r.setPointStyle(PointStyle.CIRCLE);
            r.setFillPoints(true);
            r.setPointStrokeWidth(1f);
            renderer.addSeriesRenderer(r);
        }

        renderer.setPointSize(1f);
        renderer.setChartTitle("心電");
        renderer.setChartTitleTextSize(CHART_TITLE_SIZE);

        renderer.setXTitle("経過時間 [ms]");
        renderer.setYTitle("                [μv]");

        renderer.setLabelsTextSize(LABELS_SIZE);
        renderer.setLabelsColor(TITLE_COLOR);
        renderer.setXLabelsAlign(Paint.Align.LEFT);
        renderer.setYLabelsAlign(Paint.Align.RIGHT);

        renderer.setXLabelsColor(XLABEL_COLOR);
        renderer.setYLabelsColor(0, YLABEL_COLOR);

        renderer.setAxisTitleTextSize(AXIS_TITLE_SIZE);
        renderer.setAxesColor(AXIS_COLOR);
        renderer.setXAxisMin(mMinX);
        renderer.setXAxisMax(mMaxX);
        renderer.setYAxisMin(-3.0);
        renderer.setYAxisMax(3.0);

        renderer.setShowGridX(true);
        renderer.setShowGridY(true);
        renderer.setGridColor(GRID_COLOR);

        renderer.setApplyBackgroundColor(true);
        renderer.setBackgroundColor(Color.BLACK);

        renderer.setMargins(new int[] {16, 48, 16, 8 });
        renderer.setMarginsColor(Color.argb(0, 255, 255, 255));
        renderer.setPanEnabled(false, false);
        renderer.setShowLegend(true);
        renderer.setLegendTextSize(15);
        renderer.setFitLegend(false);

        renderer.setZoomButtonsVisible(false);
        renderer.setZoomEnabled(false, false);

        return renderer;
    }
}
