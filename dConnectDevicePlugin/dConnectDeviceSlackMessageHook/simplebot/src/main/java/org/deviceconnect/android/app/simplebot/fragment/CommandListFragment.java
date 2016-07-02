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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
public class CommandListFragment extends ListFragment implements ShowMenuFragment {

    /** Listアダプター */
    private SimpleCursorAdapter adapter;
    /** メニュー */
    private Menu mainMenu;


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
        getActivity().setTitle(getString(R.string.app_name) + " [コマンド一覧]");
        adapter.getCursor().close();
        DataManager dm = new DataManager(getActivity());
        Cursor cursor = dm.getAll();
        adapter.changeCursor(cursor);
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
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


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.command_menu, menu);
        mainMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_command:
                Fragment fragment = new CommandDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("mode", "add");
                fragment.setArguments(bundle);
                Utils.transition(fragment, getFragmentManager(), true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * メニューを表示
     */
    public void showMenu() {
        mainMenu.performIdentifierAction(R.id.overflow_options, 0);
    }
}
