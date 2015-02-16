/*
 WhitelistFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.policy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.deviceconnect.android.manager.R;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Browser;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Whitelist fragment.
 */
public class WhitelistFragment extends Fragment {

    private Whitelist mWhitelist;

    private View mRootView;

    private OriginPatternListAdapter mListAdapter;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mWhitelist = new Whitelist(getActivity());
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        mListAdapter = new OriginPatternListAdapter(getActivity(), mWhitelist.getPatterns());

        mRootView = inflater.inflate(R.layout.fragment_whitelist, container, false);
        ListView listView = (ListView) mRootView.findViewById(R.id.listview_whitelist);
        listView.setAdapter(mListAdapter);
        refreshView();
        return mRootView;
    }
    
    private void refreshView() {
        View commentView = mRootView.findViewById(R.id.view_no_origin_pattern);
        if (mWhitelist.getPatterns().size() == 0) {
            commentView.setVisibility(View.VISIBLE);
        } else {
            commentView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        menu.clear();
        final MenuItem menuItem = menu.add(R.string.menu_add_origin_pattern);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final MenuItem item) {
                if (item.getTitle().equals(menuItem.getTitle())) {
                    openAddDialog();
                }
                return true;
            }
        });
    }

    private class OriginPatternListAdapter extends ArrayAdapter<OriginPattern> {
        LayoutInflater mInflater;
        List<OriginPattern> mPatternList;

        OriginPatternListAdapter(final Context context, final List<OriginPattern> patterns) {
            super(context, 0, patterns);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mPatternList = patterns;
        }

        @Override
        public int getCount() {
            return mPatternList.size();
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final OriginPattern pattern = (OriginPattern) getItem(position);

            View view = convertView;
            if (view == null) {
                view = mInflater.inflate(R.layout.item_whitelist_origin_pattern, (ViewGroup) null);
            }

            // オリジンパターンを表示
            TextView textViewAccessToken = (TextView) view.findViewById(R.id.text_origin_pattern);
            textViewAccessToken.setText(pattern.getGlob());

            // 行背景をクリックしたときの処理
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View view) {
                    // TODO
                }
            });

            // オリジンパターン削除ボタン
            Button buttonDelete = (Button) view.findViewById(R.id.button_delete_origin_pattern);
            buttonDelete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    openDeleteDialog(pattern);
                }
            });

            return view;
        }
    }

    private void delete(final OriginPattern pattern) {
        mWhitelist.removePattern(pattern);
        mListAdapter.remove(pattern);
        mListAdapter.notifyDataSetChanged();
        refreshView();
    }
    
    private void add(final String patternExp) {
        OriginPattern pattern = mWhitelist.addPattern(patternExp);
        mListAdapter.add(pattern);
        mListAdapter.notifyDataSetChanged();
        refreshView();
    }
    
    private void openDeleteDialog(final OriginPattern pattern) {
        String strGuidance = getString(R.string.dialog_delete_origin_pattern_message);
        String strPositive = getString(R.string.dialog_delete_origin_pattern_positive);
        String strNegative = getString(R.string.dialog_delete_origin_pattern_negative);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(pattern.getGlob());
        builder.setMessage(strGuidance)
                .setPositiveButton(strPositive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        delete(pattern);
                    }
                }).setNegativeButton(strNegative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                    }
                }).setCancelable(true);
        builder.create().show();
    }

    private void openAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setItems(R.array.whitelist_menu_origin_pattern, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                switch (which) {
                case 0:
                    openBookmarkedWebAppDialog();
                    break;
                case 1:
                    openInstalledNativeAppDialog();
                    break;
                case 2:
                    openManualDialog();
                    break;
                default:
                    // nothing to do
                    break;
                }
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void openBookmarkedWebAppDialog() {
        final List<BookmarkInfo> bookmarks = getAllBookmarks();
        BookmarkListAdapter adapter = new BookmarkListAdapter(getActivity(), bookmarks);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_add_origin_pattern_title);
        builder.setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                BookmarkInfo bookmark = bookmarks.get(which);
                add(bookmark.getOrigin());
                dialog.dismiss();
            }
        });
        builder.setCancelable(true);
        builder.create().show();
    };
    
    private void openInstalledNativeAppDialog() {
        
    }
    
    private void openManualDialog() {
        final EditText editText = new EditText(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(editText);
        builder.setPositiveButton(R.string.dialog_add_origin_pattern_positive,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        add(editText.getText().toString());
                    }
                });
        builder.setNegativeButton(R.string.dialog_add_origin_pattern_negative,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                    }
                });
        builder.setCancelable(true);
        builder.create().show();
    }
    
    private List<BookmarkInfo> getAllBookmarks() {
        ContentResolver resolver = getActivity().getContentResolver();
        Cursor c = resolver.query(Browser.BOOKMARKS_URI, new String[] {
                Browser.BookmarkColumns.TITLE,
                Browser.BookmarkColumns.URL,
                Browser.BookmarkColumns.FAVICON
            }, android.provider.Browser.BookmarkColumns.BOOKMARK, null, null);
        List<BookmarkInfo> bookmarks = new ArrayList<WhitelistFragment.BookmarkInfo>();
        if (c.moveToFirst()) {
            do {
                final int titleIndex = c.getColumnIndex(Browser.BookmarkColumns.TITLE);
                final int urlIndex = c.getColumnIndex(Browser.BookmarkColumns.URL);
                final int iconIndex = c.getColumnIndex(Browser.BookmarkColumns.FAVICON);
                BookmarkInfo info = new BookmarkInfo();
                info.title = c.getString(titleIndex);
                info.url = c.getString(urlIndex);
                info.icon = c.getBlob(iconIndex);
                bookmarks.add(info);
            } while (c.moveToNext());
        }
        return bookmarks;
    }
    
    private class BookmarkInfo {
        String title;
        String url;
        byte[] icon;
        String getOrigin() {
            try {
                URI uri = new URI(url);
                String scheme = uri.getScheme();
                String host = uri.getHost();
                int port = uri.getPort();
                return scheme + "://" + host + (port > 0 ? ":" + port : "");
            } catch (URISyntaxException e) {
                // bug if this block is entered.
                throw new RuntimeException();
            }
        }
    }
    
    private class BookmarkListAdapter extends ArrayAdapter<BookmarkInfo> {
        LayoutInflater mInflater;
        List<BookmarkInfo> mList;

        BookmarkListAdapter(final Context context, final List<BookmarkInfo> list) {
            super(context, 0, list);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final BookmarkInfo bookmark = (BookmarkInfo) getItem(position);

            View view = convertView;
            if (view == null) {
                view = mInflater.inflate(R.layout.item_bookmarks, (ViewGroup) null);
            }

            if (bookmark.icon != null) {
                ImageView imageIcon = (ImageView) view.findViewById(R.id.image_bookmark_icon);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bookmark.icon, 0, bookmark.icon.length);
                imageIcon.setImageBitmap(bitmap);
            }
            TextView textTitle = (TextView) view.findViewById(R.id.text_bookmark_title);
            textTitle.setText(bookmark.title);
            TextView textOrigin = (TextView) view.findViewById(R.id.text_bookmark_orgin);
            textOrigin.setText(bookmark.getOrigin());

            return view;
        }
    }
}
