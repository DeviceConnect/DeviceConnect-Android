/*
 CommandListFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.app.simplebot.fragment;

import android.app.Fragment;
import android.app.ListFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import org.deviceconnect.android.app.simplebot.R;
import org.deviceconnect.android.app.simplebot.data.DataManager;
import org.deviceconnect.android.app.simplebot.utils.Utils;

/**
 * コマンド一覧画面
 */
public class CommandListFragment extends ListFragment {

    /** Listアダプター */
    private SimpleCursorAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataManager dm = new DataManager(getActivity());
        Cursor cursor = dm.getAll();
        String[] from = {DataManager.COLUMN_KEYWORD};
        int[] to = {R.id.textCommandList};
        int layout = R.layout.command_list_item;
        adapter = new SimpleCursorAdapter(getActivity(), layout, cursor, from, to, 0);
        setListAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        adapter.getCursor().close();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.getCursor().close();
        DataManager dm = new DataManager(getActivity());
        Cursor cursor = dm.getAll();
        adapter.changeCursor(cursor);
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_command_list, container, false);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Fragment fragment = new CommandDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("mode", "edit");
        bundle.putLong("id", id);
        fragment.setArguments(bundle);
        Utils.transition(fragment, getFragmentManager(), true);
    }
}
