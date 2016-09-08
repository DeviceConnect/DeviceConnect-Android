/*
 CommandDetailsFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.app.simplebot.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.deviceconnect.android.app.simplebot.R;
import org.deviceconnect.android.app.simplebot.data.ResultData;

/**
 *　結果詳細画面
 */
public class ResultDetailsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result_details, container, false);
        Bundle bundle = getArguments();
        if (bundle == null) {
            return view;
        }
        ResultData.ResultAdapter adapter = ResultData.INSTANCE.getAdapter(view.getContext());
        ResultData.Result result = adapter.getItem((int) bundle.getLong("id"));

        TextView textFrom = (TextView)view.findViewById(R.id.textResultFrom);
        TextView textChannel = (TextView)view.findViewById(R.id.textResultChannel);
        TextView textDate = (TextView)view.findViewById(R.id.textResultDate);
        TextView textText = (TextView)view.findViewById(R.id.textResultText);
        TextView textServiceId = (TextView)view.findViewById(R.id.textResultServiceId);
        TextView textPath = (TextView)view.findViewById(R.id.textResultPath);
        TextView textData = (TextView)view.findViewById(R.id.textResultData);
        TextView textResult = (TextView)view.findViewById(R.id.textResultResult);

        textFrom.setText(result.from);
        textChannel.setText(result.channel);
        textDate.setText(DateFormat.format("yyyy/MM/dd kk:mm:ss", result.date));
        textText.setText(result.text);
        textServiceId.setText(result.data.serviceId);
        textPath.setText(result.data.path);
        textData.setText(result.data.body);
        textResult.setText(result.response);

        return view;
    }

}
