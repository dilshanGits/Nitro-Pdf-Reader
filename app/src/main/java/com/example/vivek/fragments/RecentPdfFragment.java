package com.example.vivek.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.example.vivek.BrowsePDFActivity;
import com.example.vivek.R;
import com.example.vivek.adapters.RecentPdfsAdapter;
import com.example.vivek.data.DbHelper;
import com.example.vivek.models.PdfDataType;
import com.example.vivek.p006ui.MaterialSearchView;
import com.example.vivek.p006ui.MaterialSearchView.OnQueryTextListener;
import java.util.ArrayList;
import java.util.List;
import com.example.vivek.DataUpdatedEvent.DevicePDFStaredEvent;
import com.example.vivek.DataUpdatedEvent.PdfRenameEvent;
import com.example.vivek.DataUpdatedEvent.PermanetlyDeleteEvent;
import com.example.vivek.DataUpdatedEvent.RecentPdfClearEvent;
import com.example.vivek.DataUpdatedEvent.RecentPdfDeleteEvent;
import com.example.vivek.DataUpdatedEvent.RecentPdfInsert;
import com.example.vivek.DataUpdatedEvent.ToggleGridViewEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class RecentPdfFragment extends Fragment implements OnQueryTextListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private final String TAG = RecentPdfFragment.class.getCanonicalName();
    public DbHelper dbHelper;
    public LinearLayout layNoRecentPdf;
    public RecyclerView recyclerRecentPdfHistory;
    public List<PdfDataType> listRecentPdfHistory = new ArrayList();
    public RecentPdfsAdapter recentPdfsAdapter;
    private boolean isFragVisibleToUser;
    public boolean isGridEnabled;
    public Context context;
    private OnRecentPdfClickListener onRecentPdfClickListener;
    private String mParam1, mParam2;
    private int numberOfColumnsRecent;
    public ProgressBar progressRecentPdfHistory;
    private MaterialSearchView materialSearchView;
    private SharedPreferences sharedPreferences;
    public SwipeRefreshLayout swipeRecentPdfRefresh;


    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.context = getContext();
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        this.isGridEnabled = this.sharedPreferences.getBoolean(BrowsePDFActivity.GRID_VIEW_ENABLED, false);
        this.numberOfColumnsRecent = this.sharedPreferences.getInt(BrowsePDFActivity.GRID_VIEW_NUM_OF_COLUMNS, 2);
        this.recentPdfsAdapter = new RecentPdfsAdapter(this.listRecentPdfHistory, this.context);
        this.dbHelper = DbHelper.getInstance(this.context);
        if (getArguments() != null) {
            this.mParam1 = getArguments().getString(ARG_PARAM1);
            this.mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    public void onViewCreated(View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.recyclerRecentPdfHistory = (RecyclerView) view.findViewById(R.id.recyclerRecentPdfHistory);
        this.layNoRecentPdf = (LinearLayout) view.findViewById(R.id.layNoRecentPdf);
        this.materialSearchView = (MaterialSearchView) getActivity().findViewById(R.id.searchBarPdf);
//        this.materialSearchView = (MaterialSearchView) getActivity().findViewById(R.id.search_view);               //changewbyown
        this.swipeRecentPdfRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swipeRecentPdfRefresh);
        this.materialSearchView.setOnQueryTextListener(this);
        if (this.isGridEnabled) {
            setRecentPdfGrid(this.context, this.recyclerRecentPdfHistory, this.numberOfColumnsRecent);
        } else {
            setRecentPdfListView(this.context, this.recyclerRecentPdfHistory);
        }
        this.progressRecentPdfHistory = (ProgressBar) view.findViewById(R.id.progressRecentPdfHistory);
        new LoadRecentPdfHistory().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        this.swipeRecentPdfRefresh.setOnRefreshListener(new OnRefreshListener() {
            public void onRefresh() {
                new UpdateRecentPdf().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
            }
        });
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_recents_pdf, viewGroup, false);
    }


    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRecentPdfClickListener) {
            this.onRecentPdfClickListener = (OnRecentPdfClickListener) context;
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(context.toString());
        sb.append(" must implement OnRecentPdfClickListener");
        throw new RuntimeException(sb.toString());
    }

    public class LoadRecentPdfHistory extends AsyncTask<Void, Void, Void> {

        public void onPreExecute() {
            super.onPreExecute();
            RecentPdfFragment.this.progressRecentPdfHistory.setVisibility(View.VISIBLE);
        }


        public Void doInBackground(Void... voidArr) {
            DbHelper dbHelper1 = DbHelper.getInstance(RecentPdfFragment.this.context);
            RecentPdfFragment.this.listRecentPdfHistory.clear();
            RecentPdfFragment.this.listRecentPdfHistory = dbHelper1.getRecentPDFs();
            RecentPdfFragment recentPdfFragment = RecentPdfFragment.this;
            recentPdfFragment.recentPdfsAdapter = new RecentPdfsAdapter(recentPdfFragment.listRecentPdfHistory, RecentPdfFragment.this.context);
            return null;
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            RecentPdfFragment.this.progressRecentPdfHistory.setVisibility(View.GONE);
            if (RecentPdfFragment.this.listRecentPdfHistory.isEmpty()) {
                RecentPdfFragment.this.layNoRecentPdf.setVisibility(View.VISIBLE);
            } else {
                RecentPdfFragment.this.layNoRecentPdf.setVisibility(View.GONE);
            }
            RecentPdfFragment.this.recyclerRecentPdfHistory.setAdapter(RecentPdfFragment.this.recentPdfsAdapter);
        }
    }

    public interface OnRecentPdfClickListener {
        void onRecentPdfClick(Uri uri);
    }

    public class UpdateRecentPdf extends AsyncTask<Void, Void, Void> {

        public Void doInBackground(Void... voidArr) {
            if (RecentPdfFragment.this.recyclerRecentPdfHistory != null) {
                RecentPdfFragment recentPdfFragment = RecentPdfFragment.this;
                recentPdfFragment.listRecentPdfHistory = recentPdfFragment.dbHelper.getRecentPDFs();
            }
            return null;
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            if (RecentPdfFragment.this.listRecentPdfHistory.isEmpty()) {
                RecentPdfFragment.this.layNoRecentPdf.setVisibility(View.VISIBLE);
            } else {
                RecentPdfFragment.this.layNoRecentPdf.setVisibility(View.GONE);
            }
            RecentPdfFragment.this.swipeRecentPdfRefresh.setRefreshing(false);
            RecentPdfFragment.this.recentPdfsAdapter.updateData(RecentPdfFragment.this.listRecentPdfHistory);
        }
    }

    public static RecentPdfFragment newInstance(String str, String str2) {
        RecentPdfFragment recentPdfFragment = new RecentPdfFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PARAM1, str);
        bundle.putString(ARG_PARAM2, str2);
        recentPdfFragment.setArguments(bundle);
        return recentPdfFragment;
    }

    public void onDetach() {
        super.onDetach();
        this.onRecentPdfClickListener = null;
    }

    public void setUserVisibleHint(boolean z) {
        super.setUserVisibleHint(z);
        if (z) {
            this.isFragVisibleToUser = true;
            MaterialSearchView materialSearchView = this.materialSearchView;
            if (materialSearchView != null) {
                materialSearchView.setOnQueryTextListener(this);
                return;
            }
            return;
        }
        this.isFragVisibleToUser = false;
        MaterialSearchView materialSearchView2 = this.materialSearchView;
        if (materialSearchView2 != null) {
            materialSearchView2.setOnQueryTextListener(null);
        }
    }

    public boolean onQueryTextSubmit(String str) {
        String str2 = this.TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("Search query from recent fragment ");
        sb.append(str);
        Log.d(str2, sb.toString());
        if (this.isFragVisibleToUser) {
            searchRecentOpenPDF(str);
        }
        return true;
    }

    public boolean onQueryTextChange(String str) {
        String str2 = this.TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("Search query from recent fragment ");
        sb.append(str);
        Log.d(str2, sb.toString());
        if (this.isFragVisibleToUser) {
            searchRecentOpenPDF(str);
        }
        return true;
    }


    public void setRecentPdfGrid(Context context, RecyclerView recyclerView, int i) {
        Float valueOf = Float.valueOf(getResources().getDisplayMetrics().density);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, i, 1, false);
        recyclerView.setBackgroundColor(getResources().getColor(R.color.colorLightGray));
        recyclerView.setPadding((int) (valueOf.floatValue() * 4.0f), (int) (valueOf.floatValue() * 4.0f), (int) (valueOf.floatValue() * 6.0f), (int) (valueOf.floatValue() * 80.0f));
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    public void setRecentPdfListView(Context context, RecyclerView recyclerView) {
        Float valueOf = Float.valueOf(getResources().getDisplayMetrics().density);
        recyclerView.setBackgroundColor(getResources().getColor(17170443));
        recyclerView.setPadding(0, 0, (int) (valueOf.floatValue() * 4.0f), (int) (valueOf.floatValue() * 80.0f));
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    public void searchRecentOpenPDF(String str) {
        ArrayList arrayList = new ArrayList();
        for (PdfDataType pdfDataType : this.listRecentPdfHistory) {
            if (pdfDataType.getName().toLowerCase().contains(str.toLowerCase())) {
                arrayList.add(pdfDataType);
            }
            this.recentPdfsAdapter.filter(arrayList);
        }
    }

    public void onResume() {
        super.onResume();
        new UpdateRecentPdf().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }


    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
    @Subscribe
    public void onRecentPdfInsert(RecentPdfInsert recentPdfInsert) {
        Log.d(this.TAG, "onRecentPdfInsert from recent");
        new UpdateHistoryPdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    @Subscribe
    public void onRecentPdfDeleteEvent(RecentPdfDeleteEvent recentPdfDeleteEvent) {
        Log.d(this.TAG, "onRecentPdfDeleteEvent from recent");
        new UpdateHistoryPdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    @Subscribe
    public void onPermanetlyDeleteEvent(PermanetlyDeleteEvent permanetlyDeleteEvent) {
        Log.d(this.TAG, "onPermanetlyDeleteEvent from recent");
        new UpdateHistoryPdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    @Subscribe
    public void onRecentPdfClearEvent(RecentPdfClearEvent recentPdfClearEvent) {
        Log.d(this.TAG, "onRecentPdfClearEvent from recent");
        new UpdateHistoryPdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    @Subscribe
    public void onPdfRenameEvent(PdfRenameEvent pdfRenameEvent) {
        Log.d(this.TAG, "onPdfRenameEvent from recent");
        new UpdateHistoryPdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    @Subscribe
    public void onDevicePDFStaredEvent(DevicePDFStaredEvent devicePDFStaredEvent) {
        Log.d(this.TAG, "onDevicePDFStaredEvent");
        this.recyclerRecentPdfHistory.setAdapter(this.recentPdfsAdapter);
    }

    @Subscribe
    public void onToggleGridViewEvent(ToggleGridViewEvent toggleGridViewEvent) {
        Log.d(this.TAG, "onToggleGridViewEvent from recent fragment");
        this.isGridEnabled = this.sharedPreferences.getBoolean(BrowsePDFActivity.GRID_VIEW_ENABLED, false);
        this.numberOfColumnsRecent = this.sharedPreferences.getInt(BrowsePDFActivity.GRID_VIEW_NUM_OF_COLUMNS, 2);
        if (this.isGridEnabled) {
            setRecentPdfGrid(context, this.recyclerRecentPdfHistory, this.numberOfColumnsRecent);
        } else {
            setRecentPdfListView(context, this.recyclerRecentPdfHistory);
        }
        String str = this.TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("Recent item size ");
        sb.append(this.listRecentPdfHistory.size());
        Log.d(str, sb.toString());
        this.recentPdfsAdapter = new RecentPdfsAdapter(this.listRecentPdfHistory, context);
        this.recyclerRecentPdfHistory.setAdapter(this.recentPdfsAdapter);
        this.recentPdfsAdapter.notifyDataSetChanged();
    }

    public class UpdateHistoryPdfFiles extends AsyncTask<Void, Void, Void> {

        public Void doInBackground(Void... voidArr) {
            if (RecentPdfFragment.this.recyclerRecentPdfHistory != null) {
                RecentPdfFragment recentPdfFragment = RecentPdfFragment.this;
                recentPdfFragment.listRecentPdfHistory = recentPdfFragment.dbHelper.getRecentPDFs();
            }
            return null;
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            if (RecentPdfFragment.this.listRecentPdfHistory.isEmpty()) {
                RecentPdfFragment.this.layNoRecentPdf.setVisibility(0);
            } else {
                RecentPdfFragment.this.layNoRecentPdf.setVisibility(8);
            }
            swipeRecentPdfRefresh.setRefreshing(false);
            recentPdfsAdapter.updateData(RecentPdfFragment.this.listRecentPdfHistory);
        }
    }

}
