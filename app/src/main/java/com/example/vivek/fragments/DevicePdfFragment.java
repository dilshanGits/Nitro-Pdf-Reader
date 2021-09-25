package com.example.vivek.fragments;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.vivek.BrowsePDFActivity;
import com.example.vivek.R;
import com.example.vivek.adapters.DevicePdfsAdapter;
import com.example.vivek.data.DbHelper;
import com.example.vivek.models.PdfDataType;
import com.example.vivek.p006ui.MaterialSearchView;
import com.example.vivek.p006ui.MaterialSearchView.OnQueryTextListener;

import java.util.ArrayList;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import com.example.vivek.DataUpdatedEvent.PdfRenameEvent;
import com.example.vivek.DataUpdatedEvent.PermanetlyDeleteEvent;
import com.example.vivek.DataUpdatedEvent.RecentPDFStaredEvent;
import com.example.vivek.DataUpdatedEvent.SortListEvent;
import com.example.vivek.DataUpdatedEvent.ToggleGridViewEvent;


public class DevicePdfFragment extends Fragment implements OnQueryTextListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static String MORE_OPTIONS_TIP = "prefs_more_options_tip";
    private static final int RC_READ_EXTERNAL_STORAGE = 1;
    final String TAG = DevicePdfFragment.class.getSimpleName();
    public FragmentActivity activityCompat;
    private ImageView imgTapClose;
    public DbHelper dbHelper;
    public RecyclerView recycleDevicePdf;
    public LinearLayout layNoDevicePdf;
    public RelativeLayout rLayTapMore;
    private boolean isFragmentVisibleToUser;
    public boolean isGridViewEnabled;
    public ProgressBar progressDevicePdf;
    private String mParam1, mParam2;
    List<PdfDataType> myPdfDataTypes = new ArrayList();
    int numberOfColumns;
    public DevicePdfsAdapter devicePdfsAdapter;
    private MaterialSearchView searchBrowsePdf;
    SharedPreferences sharedPreferences;
    public boolean showMoreOptionsTip;
    public SwipeRefreshLayout swipePdfRecycle;


    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        activityCompat = getActivity();
        dbHelper = DbHelper.getInstance(activityCompat);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        isGridViewEnabled = sharedPreferences.getBoolean(BrowsePDFActivity.GRID_VIEW_ENABLED, false);
        numberOfColumns = sharedPreferences.getInt(BrowsePDFActivity.GRID_VIEW_NUM_OF_COLUMNS, 2);
        showMoreOptionsTip = sharedPreferences.getBoolean(MORE_OPTIONS_TIP, true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    public void onViewCreated(View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);

        searchBrowsePdf = (MaterialSearchView) activityCompat.findViewById(R.id.searchBarPdf);
        layNoDevicePdf = (LinearLayout) view.findViewById(R.id.layNoDevicePdf);
        recycleDevicePdf = (RecyclerView) view.findViewById(R.id.recycleDevicePdf);
        progressDevicePdf = (ProgressBar) view.findViewById(R.id.progressDevicePdf);
        rLayTapMore = (RelativeLayout) view.findViewById(R.id.rLayTapMore);
        imgTapClose = (ImageView) view.findViewById(R.id.imgTapClose);
        swipePdfRecycle = (SwipeRefreshLayout) view.findViewById(R.id.swipePdfRecycle);

        imgTapClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DevicePdfFragment.this.rLayTapMore.setVisibility(View.GONE);
                DevicePdfFragment.this.rLayTapMore.animate().translationY((float) (-DevicePdfFragment.this.rLayTapMore.getHeight())).alpha(0.0f).setListener(new AnimatorListener() {
                    public void onAnimationCancel(Animator animator) {
                    }

                    public void onAnimationRepeat(Animator animator) {
                    }

                    public void onAnimationStart(Animator animator) {
                    }

                    public void onAnimationEnd(Animator animator) {
                        DevicePdfFragment.this.rLayTapMore.setVisibility(View.GONE);
                        Editor editor = DevicePdfFragment.this.sharedPreferences.edit();
                        editor.putBoolean(DevicePdfFragment.MORE_OPTIONS_TIP, false);
                        editor.apply();
                    }
                });
            }
        });


        if (showMoreOptionsTip) {
            rLayTapMore.setVisibility(View.VISIBLE);
        } else {
            rLayTapMore.setVisibility(View.GONE);
        }
        if (isGridViewEnabled) {
            setPdfForGridView(activityCompat, recycleDevicePdf, numberOfColumns);
        } else {
            setPdfForListView(activityCompat, recycleDevicePdf);
        }
        if (ActivityCompat.checkSelfPermission(activityCompat, "android.permission.READ_EXTERNAL_STORAGE") != 0) {
            requestStoragePermission();
        } else {
            new DevicePdfLoad().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        }
        swipePdfRecycle.setOnRefreshListener(new OnRefreshListener() {
            public void onRefresh() {
                new refreshDevicePdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
            }
        });
    }

    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        if (i == 1 && iArr.length >= 1 && iArr[0] == 0) {
            Log.d(TAG, "Permission read External storage permission granted");
            new DevicePdfLoad().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
            return;
        }
        Log.d(TAG, "Permission read External storage permission not granted");
        super.onRequestPermissionsResult(i, strArr, iArr);
        new Builder(activityCompat).setTitle((int) R.string.app_name).setMessage((int) R.string.exit_app_has_no_permission).setCancelable(false).setPositiveButton((int) R.string.ok, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                DevicePdfFragment.this.activityCompat.finish();
            }
        }).show();
    }


    public static DevicePdfFragment newInstance(String str, String str2) {
        DevicePdfFragment devicePdfFragment = new DevicePdfFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PARAM1, str);
        bundle.putString(ARG_PARAM2, str2);
        devicePdfFragment.setArguments(bundle);
        return devicePdfFragment;
    }

    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_device_pdf, viewGroup, false);
    }

    public void requestStoragePermission() {
        String[] strArr = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), "android.permission.READ_EXTERNAL_STORAGE")) {
            Toast.makeText(this.activityCompat, "Read storage permission is required to list files", Toast.LENGTH_SHORT).show();
        }
        requestPermissions(strArr, 1);
    }

    public class DevicePdfLoad extends AsyncTask<Void, Void, Void> {
        public DevicePdfLoad() {

        }

        public void onPreExecute() {
            super.onPreExecute();
            DevicePdfFragment.this.myPdfDataTypes.clear();
            DevicePdfFragment.this.progressDevicePdf.setVisibility(View.VISIBLE);
        }

        public Void doInBackground(Void... voidArr) {
            DevicePdfFragment devicePdfFragment = DevicePdfFragment.this;
            devicePdfFragment.myPdfDataTypes = devicePdfFragment.dbHelper.getAllPdfs();
            DevicePdfFragment devicePdfFragment2 = DevicePdfFragment.this;
            devicePdfFragment2.devicePdfsAdapter = new DevicePdfsAdapter(devicePdfFragment2.myPdfDataTypes, devicePdfFragment2.activityCompat);
            return null;
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            DevicePdfFragment.this.progressDevicePdf.setVisibility(View.GONE);
            DevicePdfFragment.this.recycleDevicePdf.setAdapter(DevicePdfFragment.this.devicePdfsAdapter);
            if (DevicePdfFragment.this.myPdfDataTypes.isEmpty()) {
                DevicePdfFragment.this.layNoDevicePdf.setVisibility(View.VISIBLE);
            } else {
                DevicePdfFragment.this.layNoDevicePdf.setVisibility(View.GONE);
            }
            DevicePdfFragment.this.devicePdfsAdapter.updatePdfData(DevicePdfFragment.this.myPdfDataTypes);
        }
    }

    public class refreshDevicePdfFiles extends AsyncTask<Void, Void, Void> {

        public Void doInBackground(Void... voidArr) {
            DevicePdfFragment devicePdfFragment = DevicePdfFragment.this;
            devicePdfFragment.myPdfDataTypes = devicePdfFragment.dbHelper.getAllPdfs();
            return null;
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            if (DevicePdfFragment.this.myPdfDataTypes.isEmpty()) {
                DevicePdfFragment.this.layNoDevicePdf.setVisibility(View.VISIBLE);
            } else {
                DevicePdfFragment.this.layNoDevicePdf.setVisibility(View.GONE);
            }
            DevicePdfFragment.this.swipePdfRecycle.setRefreshing(false);
            DevicePdfFragment.this.devicePdfsAdapter.updatePdfData(DevicePdfFragment.this.myPdfDataTypes);
        }
    }


    public void setUserVisibleHint(boolean z) {
        super.setUserVisibleHint(z);
        if (z) {
            this.isFragmentVisibleToUser = true;
            MaterialSearchView materialSearchView = this.searchBrowsePdf;
            if (materialSearchView != null) {
                materialSearchView.setOnQueryTextListener(this);
                return;
            }
            return;
        }
        this.isFragmentVisibleToUser = false;
        MaterialSearchView materialSearchView2 = this.searchBrowsePdf;
        if (materialSearchView2 != null) {
            materialSearchView2.setOnQueryTextListener(null);
        }
    }

    public boolean onQueryTextSubmit(String str) {
        if (this.isFragmentVisibleToUser) {
            searchPDFFiles(str);
        }
        return true;
    }

    public boolean onQueryTextChange(String str) {
        if (this.isFragmentVisibleToUser) {
            searchPDFFiles(str);
        }
        return true;
    }

    public void setPdfForGridView(Context context, RecyclerView recyclerView, int i) {
        Float valueOf = Float.valueOf(getResources().getDisplayMetrics().density);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, i, 1, false);
        recyclerView.setBackgroundColor(getResources().getColor(R.color.colorLightGray));
        recyclerView.setPadding((int) (valueOf.floatValue() * 4.0f), (int) (valueOf.floatValue() * 4.0f), (int) (valueOf.floatValue() * 6.0f), (int) (valueOf.floatValue() * 80.0f));
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    public void setPdfForListView(Context context, RecyclerView recyclerView) {
        Float valueOf = Float.valueOf(getResources().getDisplayMetrics().density);
        recyclerView.setBackgroundColor(getResources().getColor(17170443));
        recyclerView.setPadding(0, 0, (int) (valueOf.floatValue() * 4.0f), (int) (valueOf.floatValue() * 80.0f));
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    public void searchPDFFiles(String str) {
        ArrayList arrayList = new ArrayList();
        for (PdfDataType pdfDataType : this.myPdfDataTypes) {
            if (pdfDataType.getName().toLowerCase().contains(str.toLowerCase())) {
                arrayList.add(pdfDataType);
            }
            this.devicePdfsAdapter.filter(arrayList);
        }
    }

    @Subscribe
    public void onPermanetlyDeleteEvent(PermanetlyDeleteEvent permanetlyDeleteEvent) {
        Log.d(this.TAG, "onPermanetlyDeleteEvent from device");
        new refreshDevicePdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    @Subscribe
    public void onPdfRenameEvent(PdfRenameEvent pdfRenameEvent) {
        Log.d(this.TAG, "onPdfRenameEvent from recent");
        new refreshDevicePdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    @Subscribe
    public void onRecentPDFStaredEvent(RecentPDFStaredEvent recentPDFStaredEvent) {
        Log.d(this.TAG, "onRecentPDFStaredEvent");
        this.recycleDevicePdf.setAdapter(this.devicePdfsAdapter);
    }

    @Subscribe
    public void onToggleGridViewEvent(ToggleGridViewEvent toggleGridViewEvent) {
        Log.d(this.TAG, "onToggleGridViewEvent from devicepdf fragment");
        this.isGridViewEnabled = this.sharedPreferences.getBoolean(BrowsePDFActivity.GRID_VIEW_ENABLED, false);
        if (this.isGridViewEnabled) {
            this.numberOfColumns = this.sharedPreferences.getInt(BrowsePDFActivity.GRID_VIEW_NUM_OF_COLUMNS, 2);
            setPdfForGridView(this.activityCompat, this.recycleDevicePdf, this.numberOfColumns);
        } else {
            setPdfForListView(this.activityCompat, this.recycleDevicePdf);
        }
        String str = this.TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("Values ");
        sb.append(this.myPdfDataTypes.size());
        Log.d(str, sb.toString());
        this.devicePdfsAdapter = new DevicePdfsAdapter(this.myPdfDataTypes, this.activityCompat);
        this.recycleDevicePdf.setAdapter(this.devicePdfsAdapter);
    }

    @Subscribe
    public void onSortListEvent(SortListEvent sortListEvent) {
        new refreshDevicePdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    /*public class refreshPdfFiles extends AsyncTask<Void, Void, Void> {


        public Void doInBackground(Void... voidArr) {
            DevicePdfFragment devicePdfFragment = DevicePdfFragment.this;
            devicePdfFragment.myPdfDataTypes = devicePdfFragment.dbHelper.getAllPdfs();
            return null;
        }

        *//* access modifiers changed from: protected *//*
        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            if (DevicePdfFragment.this.myPdfDataTypes.isEmpty()) {
                DevicePdfFragment.this.layNoDevicePdf.setVisibility(View.VISIBLE);
            } else {
                DevicePdfFragment.this.layNoDevicePdf.setVisibility(View.GONE);
            }
            DevicePdfFragment.this.swipePdfRecycle.setRefreshing(false);
            devicePdfsAdapter.updateData(DevicePdfFragment.this.myPdfDataTypes);
        }
    }*/


}
