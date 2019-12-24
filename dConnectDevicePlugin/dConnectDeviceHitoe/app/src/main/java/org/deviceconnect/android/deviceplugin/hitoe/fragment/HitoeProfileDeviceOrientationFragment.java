/*
 HitoeProfileDeviceOrientationFragment
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
import org.deviceconnect.android.deviceplugin.hitoe.data.AccelerationData;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeConstants;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;
import org.deviceconnect.android.deviceplugin.hitoe.util.HitoeScheduler;

import java.util.ArrayList;
import java.util.List;


/**
 * This fragment do setting of the control DeviceOrientation profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class HitoeProfileDeviceOrientationFragment extends Fragment implements HitoeScheduler.OnRegularNotify {

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
    private static final int DATA_COUNT = 3;
    /** orientation title. */
    private static final String[] TITLES = new String[] {"X", "Y", "Z" };
    /** data color. */
    private static final int[] COLORS = new int[] {Color.RED, Color.GREEN, Color.BLUE };
    /** data count. */
    private static final long MAX_RANGE = 5000;


    /** Acc data. */
    private List<XYSeries> mACCList = null;
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
     * Hitoe's Scheduler.
     */
    private HitoeScheduler mScheduler;

    @Override
    public View onCreateView(final LayoutInflater inflater, final @Nullable ViewGroup container,
                             final @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_orientation_instructions, null);
        mScheduler = new HitoeScheduler(this,HitoeConstants.ACC_CHART_UPDATE_CYCLE_TIME,
                                                                HitoeConstants.ACC_CHART_UPDATE_CYCLE_TIME);

        rootView.findViewById(R.id.button_register).setOnClickListener((view) -> {
            clear();
            mScheduler.scanHitoeDevice(true);
        });
        rootView.findViewById(R.id.button_unregister).setOnClickListener((view) -> {
            mScheduler.scanHitoeDevice(false);
        });
        TextView title = (TextView) rootView.findViewById(R.id.view_title);
        Bundle args = getArguments();
        if (args != null) {

            String serviceId = args.getString(HitoeDeviceControlActivity.FEATURE_SERVICE_ID);
            HitoeApplication app = (HitoeApplication) getActivity().getApplication();
            HitoeManager manager = app.getHitoeManager();

            mCurrentDevice = manager.getHitoeDeviceForServiceId(serviceId);
            if (mCurrentDevice != null) {
                String[] profiles = getResources().getStringArray(R.array.support_profiles);
                title.setText(profiles[2] + getString(R.string.title_control));
            }
        }
        init();
        ((LinearLayout) rootView.findViewById(R.id.acc_chart)).addView(mGraphicalView);
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

            AccelerationData acc = manager.getAccelerationData(mCurrentDevice.getId());
            if (acc != null) {
                double[] accs = new double[3];
                accs[0] = acc.getAccelX();
                accs[1] = acc.getAccelY();
                accs[2] = acc.getAccelZ();
                setACC(System.currentTimeMillis(), accs);
                updateChart();
            }
        });
    }

    /**
     * Initialilze chart.
     */
    private void init() {
        mACCList = new ArrayList<>();
        mACCList.add(new XYSeries(TITLES[0]));
        mACCList.add(new XYSeries(TITLES[1]));
        mACCList.add(new XYSeries(TITLES[2]));
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addAllSeries(mACCList);

        this.mXYMultipleSeriesRenderer = buildRenderer();
        mLineChart = new LineChart(dataset, mXYMultipleSeriesRenderer);
        mGraphicalView = new GraphicalView(getActivity(), mLineChart);
    }

    /**
     * Set Acceleration data.
     * @param timestamp timestamp
     * @param accList acceleration data list
     */
    private void setACC(final long timestamp, final double[] accList) {

        if (this.mACCList.get(0).getItemCount() == 0) {
            mMinX = timestamp;
            mMaxX = timestamp + MAX_RANGE;
        }

        if (timestamp > mMaxX || this.mACCList.get(0).getItemCount() > MAX_RANGE / 40) {
            this.mACCList.get(0).clear();
            this.mACCList.get(1).clear();
            this.mACCList.get(2).clear();

            mMinX = timestamp;
            mMaxX = timestamp + MAX_RANGE;
        }

        this.mACCList.get(0).add(timestamp, accList[0]);
        this.mACCList.get(1).add(timestamp, accList[1]);
        this.mACCList.get(2).add(timestamp, accList[2]);
    }

    /**
     * Update chart.
     */
    private void updateChart() {
        mXYMultipleSeriesRenderer.setXAxisMin(mMinX);
        mXYMultipleSeriesRenderer.setXAxisMax(mMaxX);

        mGraphicalView.repaint();
    }

    /**
     * Clear chart.
     */
    private void clear() {
        mACCList.get(0).clear();
        mACCList.get(1).clear();
        mACCList.get(2).clear();
        mGraphicalView.repaint();
    }

    /**
     * Build chart's renderer.
     * @return chart renderer
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
        renderer.setChartTitle("加速度");
        renderer.setChartTitleTextSize(CHART_TITLE_SIZE);

        renderer.setXTitle("経過時間 [ms]");
        renderer.setYTitle("                [G]");

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
        renderer.setBackgroundColor(Color.WHITE);

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
