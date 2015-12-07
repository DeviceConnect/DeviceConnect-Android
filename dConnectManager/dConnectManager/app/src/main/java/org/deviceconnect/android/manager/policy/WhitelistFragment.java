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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deviceconnect.android.manager.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
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
import android.widget.Toast;

/**
 * Whitelist fragment.
 * 
 * @author NTT DOCOMO, INC.
 */
public class WhitelistFragment extends Fragment {

    /** The logger. */
    private Logger mLogger = Logger.getLogger("dconnect.manager");

    /** The whitelist of origins. */
    private Whitelist mWhitelist;

    /** The root view. */
    private View mRootView;

    /** The instance of {@link OriginListAdapter}. */
    private OriginListAdapter mListAdapter;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mWhitelist = new Whitelist(getActivity());
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        mListAdapter = new OriginListAdapter(getActivity(), mWhitelist.getOrigins());

        mRootView = inflater.inflate(R.layout.fragment_whitelist, container, false);
        ListView listView = (ListView) mRootView.findViewById(R.id.listview_whitelist);
        listView.setAdapter(mListAdapter);
        refreshView();
        return mRootView;
    }

    /**
     * Refreshes views.
     */
    private void refreshView() {
        View commentView = mRootView.findViewById(R.id.view_no_origin);
        if (mWhitelist.getOrigins().size() == 0) {
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
        final MenuItem menuItem = menu.add(R.string.menu_add_origin);
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

    /**
     * Origin Pattern List Adapter.
     */
    private class OriginListAdapter extends ArrayAdapter<OriginInfo> {
        /** The instance of {@link LayoutInflater}. */
        LayoutInflater mInflater;
        /** The list of origins. */
        List<OriginInfo> mPatternList;

        /**
         * Constructor.
         * 
         * @param context Context
         * @param origins The list of origins.
         */
        OriginListAdapter(final Context context, final List<OriginInfo> origins) {
            super(context, 0, origins);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mPatternList = origins;
        }

        /**
         * Sets item at the specified index.
         * 
         * @param index an index in this list
         * @param item an instance of {@link OriginInfo}
         */
        void setItem(final int index, final OriginInfo item) {
            mPatternList.set(index, item);
        }

        @Override
        public int getCount() {
            return mPatternList.size();
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final OriginInfo info = (OriginInfo) getItem(position);

            View view = convertView;
            if (view == null) {
                view = mInflater.inflate(R.layout.item_whitelist_origin, (ViewGroup) null);
            }

            final TextView textViewTitle = (TextView) view.findViewById(R.id.text_origin_title);
            textViewTitle.setText(info.getTitle());
            final TextView textViewOrigin = (TextView) view.findViewById(R.id.text_origin);
            textViewOrigin.setText(info.getOrigin().toString());

            Button buttonDelete = (Button) view.findViewById(R.id.button_delete_origin);
            buttonDelete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    openDeleteDialog(info);
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    Context context = getActivity();
                    if (context == null) {
                        return false;
                    }
                    ManualEntryDialogBuilder builder = new ManualEntryDialogBuilder(context);
                    builder.mDialogTitle = getString(R.string.dialog_update_origin_title);
                    builder.mDefaultOrigin = info.getOrigin().toString();
                    builder.mDefaultOriginTitle = info.getTitle();
                    builder.mPositiveButtonName = getString(R.string.dialog_update_origin_positive);
                    builder.mNegativeButtonName = getString(R.string.dialog_update_origin_negative);
                    builder.mListener = new OnEntryListener() {
                        @Override
                        public void onEntry(final String newOriginExp, final String newTitle) {
                            try {
                                Origin newOrigin = OriginParser.parse(newOriginExp);
                                OriginInfo newItem = new OriginInfo(info.mId, newOrigin, newTitle, info.mDate);
                                updateOrigin(position, newItem);
                                mLogger.info("Updated origin=" + newOrigin.toString() + " title=" + newTitle);
                            } catch (WhitelistException e) {
                                mLogger.log(Level.WARNING, "Failed to update origin.", e);
                                showPopup(e.getMessage());
                            }
                        }
                    };
                    builder.create().show();
                    return true;
                }
            });
            return view;
        }
    }

    /**
     * Shows a popup on the screen of Android device.
     * 
     * @param text the text message.
     */
    private void showPopup(final String text) {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Deletes an origin.
     * 
     * @param origin an origin.
     * @throws WhitelistException if the origin can not be removed.
     */
    private void deleteOrigin(final OriginInfo origin) throws WhitelistException {
        mWhitelist.removeOrigin(origin);
        mListAdapter.remove(origin);
        mListAdapter.notifyDataSetChanged();
        refreshView();
    }

    /**
     * Adds an origin to be allowed.
     * 
     * @param originExp an string expression of an origin to be allowed.
     * @param title the title of origin.
     * @throws WhitelistException if the origin can not be stored.
     */
    private void addOrigin(final String originExp, final String title) throws WhitelistException {
        Origin origin = OriginParser.parse(originExp);
        OriginInfo info = mWhitelist.addOrigin(origin, title);
        mListAdapter.add(info);
        mListAdapter.notifyDataSetChanged();
        refreshView();
    }

    /**
     * Updates an origin to be allowed.
     * 
     * @param index an index in whitelist
     * @param info the information of an origin
     * @throws WhitelistException if the origin can not be stored.
     */
    private void updateOrigin(final int index, final OriginInfo info) throws WhitelistException {
        mWhitelist.updateOrigin(info);
        mListAdapter.setItem(index, info);
        mListAdapter.notifyDataSetChanged();
        refreshView();
    }

    /**
     * Opens the origin deletion dialog.
     * 
     * @param origin an origin to be deleted.
     */
    private void openDeleteDialog(final OriginInfo origin) {
        String strGuidance = getString(R.string.dialog_delete_origin_message);
        String strPositive = getString(R.string.dialog_delete_origin_positive);
        String strNegative = getString(R.string.dialog_delete_origin_negative);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(origin.getTitle());
        builder.setMessage(strGuidance).setPositiveButton(strPositive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                try {
                    deleteOrigin(origin);
                } catch (WhitelistException e) {
                    showPopup(e.getMessage());
                }
            }
        }).setNegativeButton(strNegative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
            }
        }).setCancelable(true);
        builder.create().show();
    }

    /**
     * Opens the origin addition dialog.
     */
    private void openAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setTitle(R.string.dialog_add_origin_title);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            builder.setItems(R.array.whitelist_menu_origin_pre_marshmallow, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    dialog.dismiss();
                    Context context = getActivity();
                    if (context == null) {
                        return;
                    }
                    switch (which) {
                    case 0:
                        openListDialog(context, getAllBookmarksOfChrome());
                        break;
                    case 1:
                        openListDialog(context, getInstalledNativeApplications());
                        break;
                    case 2:
                        ManualEntryDialogBuilder builder = new ManualEntryDialogBuilder(context);
                        builder.mDialogTitle = getString(R.string.dialog_add_origin_title);
                        builder.mDefaultOrigin = "";
                        builder.mDefaultOriginTitle = "";
                        builder.mPositiveButtonName = getString(R.string.dialog_add_origin_positive);
                        builder.mNegativeButtonName = getString(R.string.dialog_add_origin_negative);
                        builder.mListener = new OnEntryListener() {
                            @Override
                            public void onEntry(final String origin, final String title) {
                                try {
                                    addOrigin(origin, title);
                                    mLogger.info("Updated origin=" + origin + " title=" + title);
                                } catch (WhitelistException e) {
                                    mLogger.log(Level.WARNING, "Failed to add origin.", e);
                                    showPopup(e.getMessage());
                                }
                            }
                        };
                        builder.create().show();
                        break;
                    default:
                        // nothing to do
                        break;
                    }
                }
            });
        } else {
            builder.setItems(R.array.whitelist_menu_origin_post_marshmallow, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    dialog.dismiss();
                    Context context = getActivity();
                    if (context == null) {
                        return;
                    }
                    switch (which) {
                    case 0:
                        openListDialog(context, getInstalledNativeApplications());
                        break;
                    case 1:
                        ManualEntryDialogBuilder builder = new ManualEntryDialogBuilder(context);
                        builder.mDialogTitle = getString(R.string.dialog_add_origin_title);
                        builder.mDefaultOrigin = "";
                        builder.mDefaultOriginTitle = "";
                        builder.mPositiveButtonName = getString(R.string.dialog_add_origin_positive);
                        builder.mNegativeButtonName = getString(R.string.dialog_add_origin_negative);
                        builder.mListener = new OnEntryListener() {
                            @Override
                            public void onEntry(final String origin, final String title) {
                                try {
                                    addOrigin(origin, title);
                                    mLogger.info("Updated origin=" + origin + " title=" + title);
                                } catch (WhitelistException e) {
                                    mLogger.log(Level.WARNING, "Failed to add origin.", e);
                                    showPopup(e.getMessage());
                                }
                            }
                        };
                        builder.create().show();
                        break;
                    default:
                        // nothing to do
                        break;
                    }
                }
            });
        }

        builder.create().show();
    }

    /**
     * Opens a dialog for the list of known application information.
     * 
     * @param context the context
     * @param list the list of known application information.
     */
    private void openListDialog(final Context context, final List<KnownApplicationInfo> list) {
        ApplicationListAdapter adapter = new ApplicationListAdapter(getActivity(), list);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_add_origin_title);
        builder.setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                try {
                    KnownApplicationInfo appInfo = list.get(which);
                    addOrigin(appInfo.getOrigin(), appInfo.getName());
                    mLogger.info("Updated origin=" + appInfo.getOrigin() + " title=" + appInfo.getName());
                } catch (WhitelistException e) {
                    mLogger.log(Level.WARNING, "Failed to add origin.", e);
                    showPopup(e.getMessage());
                } finally {
                    dialog.dismiss();
                }
            }
        });
        builder.setCancelable(true);
        builder.create().show();
    };

    /**
     * Gets all bookmarks of Chrome for Android.
     * 
     * This method works properly only in SDK version 22 or below.
     *
     * @return all bookmarks of Chrome for Android
     */
    private List<KnownApplicationInfo> getAllBookmarksOfChrome() {
        String columnTitle = "title";
        String columnURL = "url";
        String columnFavicon = "favicon";
        String selectionBookmark = "bookmark";

        ContentResolver resolver = getActivity().getContentResolver();
        Uri uri = Uri.parse(getString(R.string.chrome_bookmark_provider_url));
        Cursor c = resolver.query(uri, new String[] { columnTitle, columnURL, columnFavicon }, selectionBookmark, null,
                null);
        List<KnownApplicationInfo> bookmarks = new ArrayList<WhitelistFragment.KnownApplicationInfo>();
        if (c.moveToFirst()) {
            do {
                final int titleIndex = c.getColumnIndex(columnTitle);
                final int urlIndex = c.getColumnIndex(columnURL);
                final int iconIndex = c.getColumnIndex(columnFavicon);
                if (titleIndex < 0 || urlIndex < 0 || iconIndex < 0) {
                    continue;
                }
                final String title = c.getString(titleIndex);
                final String url = c.getString(urlIndex);
                final byte[] icon = c.getBlob(iconIndex);
                KnownApplicationInfo info = new BookmarkInfo(title, url, icon);
                if (info.getOrigin() != null) {
                    bookmarks.add(info);
                }
            } while (c.moveToNext());
        }
        c.close();
        return bookmarks;
    }

    /**
     * Gets information of all applications installed in the Android device.
     * 
     * @return information of all applications installed
     */
    private List<KnownApplicationInfo> getInstalledNativeApplications() {
        PackageManager pm = getActivity().getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        List<KnownApplicationInfo> result = new ArrayList<KnownApplicationInfo>();
        for (ApplicationInfo app : apps) {
            result.add(new InstalledNativeApplicationInfo(app));
        }
        return result;
    }

    /**
     * Information of known application by the device.
     */
    private interface KnownApplicationInfo {

        /**
         * Gets the name of the application.
         * 
         * @return the name of the application
         */
        String getName();

        /**
         * Gets the origin of the application.
         * 
         * @return the origin of the application. if origin cannot be derived,
         *         returns <code>null</code>
         */
        String getOrigin();

        /**
         * Gets binary data of the icon.
         * 
         * @return binary data of the icon. if the icon is not registered,
         *         returns <code>null</code>
         */
        Drawable getIcon();
    }

    /**
     * Information of book-mark.
     */
    private class BookmarkInfo implements KnownApplicationInfo {
        /** The title of the web site. */
        final String mTitle;
        /** The URL of the web site. */
        final String mUrl;
        /** The binary data of the favicon. */
        byte[] mIcon;

        /**
         * Constructor.
         * 
         * @param title The title of the web site
         * @param url The URL of the web site
         * @param icon The binary data of the favicon
         */
        BookmarkInfo(final String title, final String url, final byte[] icon) {
            mTitle = title;
            mUrl = url;
            mIcon = icon;
        }

        @Override
        public String getName() {
            return mTitle;
        }

        @Override
        public String getOrigin() {
            try {
                URI uri = new URI(mUrl);
                StringBuilder origin = new StringBuilder();
                origin.append(uri.getScheme());
                origin.append("://");
                if (uri.getHost() != null) {
                    origin.append(uri.getHost());
                }
                if (uri.getPort() > -1) {
                    origin.append(":");
                    origin.append(uri.getPort());
                }
                return origin.toString();
            } catch (URISyntaxException e) {
                return null;
            }
        }

        @Override
        public Drawable getIcon() {
            if (mIcon == null) {
                return null;
            }
            return new BitmapDrawable(BitmapFactory.decodeByteArray(mIcon, 0, mIcon.length));
        }
    }

    /**
     * Information of installed native application in the Android device.
     */
    private class InstalledNativeApplicationInfo implements KnownApplicationInfo {
        /** The Information of the installed native application. */
        final ApplicationInfo mAppInfo;

        /**
         * Constructor.
         * 
         * @param info The Information of the installed native application
         */
        InstalledNativeApplicationInfo(final ApplicationInfo info) {
            mAppInfo = info;
        }

        @Override
        public String getName() {
            return mAppInfo.loadLabel(getActivity().getPackageManager()).toString();
        }

        @Override
        public String getOrigin() {
            return mAppInfo.packageName;
        }

        @Override
        public Drawable getIcon() {
            return getActivity().getPackageManager().getApplicationIcon(mAppInfo);
        }

    }

    /**
     * Application List Adapter.
     */
    private class ApplicationListAdapter extends ArrayAdapter<KnownApplicationInfo> {
        /** The instance of {@link LayoutInflater}. */
        LayoutInflater mInflater;
        /** The list of application. */
        List<KnownApplicationInfo> mList;

        /**
         * Constructor.
         * 
         * @param context Context
         * @param list The list of book-marks
         */
        ApplicationListAdapter(final Context context, final List<KnownApplicationInfo> list) {
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
            final KnownApplicationInfo info = (KnownApplicationInfo) getItem(position);

            View view = convertView;
            if (view == null) {
                view = mInflater.inflate(R.layout.item_known_applications, (ViewGroup) null);
            }

            ImageView imageIcon = (ImageView) view.findViewById(R.id.image_app_icon);
            Drawable icon = info.getIcon();
            if (icon == null) {
                icon = new BitmapDrawable(BitmapFactory.decodeByteArray(new byte[] {}, 0, 0));
            }
            imageIcon.setImageDrawable(icon);
            TextView textTitle = (TextView) view.findViewById(R.id.text_app_title);
            textTitle.setText(info.getName());
            TextView textOrigin = (TextView) view.findViewById(R.id.text_app_orgin);
            textOrigin.setText(info.getOrigin());

            return view;
        }
    }

    /**
     * A builder of manual entry dialog.
     */
    private static class ManualEntryDialogBuilder {
        /** An instance of {@link AlertDialog.Builder}. */
        final AlertDialog.Builder mBuilder;

        /** An instance of {@link Context}. */
        final Context mContext;

        /** The title of dialog. */
        String mDialogTitle;

        /** The default origin. */
        String mDefaultOrigin;

        /** The default title of origin. */
        String mDefaultOriginTitle;

        /** The name of positive button on dialog. */
        String mPositiveButtonName;

        /** The name of negative button on dialog. */
        String mNegativeButtonName;

        /** An instance of {@link OnEntryListener}. */
        OnEntryListener mListener;

        /**
         * Constructor.
         * 
         * @param context an instance of {@link Context}
         */
        ManualEntryDialogBuilder(final Context context) {
            mBuilder = new AlertDialog.Builder(context);
            mContext = context;
        }

        /**
         * Creates an dialog.
         * 
         * @return an instance of {@link AlertDialog}
         */
        AlertDialog create() {
            final int layoutId = R.layout.dialog_origin_manual_entry;
            final View root = LayoutInflater.from(mContext).inflate(layoutId, null);
            final EditText editOrigin = (EditText) root.findViewById(R.id.dialog_origin_manual_entry_edit_origin);
            final EditText editTitle = (EditText) root.findViewById(R.id.dialog_origin_manual_entry_edit_title);
            editOrigin.setText(mDefaultOrigin);
            editTitle.setText(mDefaultOriginTitle);
            mBuilder.setTitle(mDialogTitle);
            mBuilder.setView(root);
            mBuilder.setPositiveButton(mPositiveButtonName, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    String origin = editOrigin.getText().toString();
                    String title = editTitle.getText().toString();
                    if (TextUtils.isEmpty(origin)) {
                        create().show();
                        return;
                    }
                    if (TextUtils.isEmpty(title)) {
                        title = origin;
                    }
                    if (mListener != null) {
                        mListener.onEntry(origin, title);
                    }
                }
            });
            mBuilder.setNegativeButton(mNegativeButtonName, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                }
            });
            mBuilder.setCancelable(true);
            return mBuilder.create();
        }
    }

    /**
     * A listener for manual entry events.
     */
    private interface OnEntryListener {

        /**
         * Receives a manual entry event.
         * 
         * @param origin the string expression of an origin
         * @param title the title of an origin
         */
        void onEntry(String origin, String title);
    }
}
