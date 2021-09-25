package com.example.vivek;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.example.vivek.adapters.StarredPDFAdapter;
import com.example.vivek.adapters.StarredPDFAdapter.OnStaredPdfClickListener;
import com.example.vivek.data.DbHelper;
import com.example.vivek.models.PdfDataType;
import com.example.vivek.p006ui.MaterialSearchView;
import com.example.vivek.p006ui.MaterialSearchView.OnQueryTextListener;
import com.example.vivek.utils.Utils.BackgroundGenerateThumbnails;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.Subscribe;

public class StarredPDFActivity extends AppCompatActivity implements OnStaredPdfClickListener, OnQueryTextListener {
    private static final String TAG = "StarredPDFActivity";
    public Context context;
    public LinearLayout layNoBookmarkPdf;
    public boolean isPdfGridEnabled;
    private MenuItem menuPdfGrid, menuPdfListView;
    public int numOfColumnsPdfList;
    public RecyclerView recycleBookedPdf;
    private MaterialSearchView searchBarBookedPdf;
    private SharedPreferences sharedPreferences;
    public StarredPDFAdapter starredPDFAdapter;
    public List<PdfDataType> listBookmarkPdfDataTypes = new ArrayList();
    AdView adview;
  
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_starred_pdf);


        this.recycleBookedPdf = (RecyclerView) findViewById(R.id.recycleBookedPdf);
        this.layNoBookmarkPdf = (LinearLayout) findViewById(R.id.layNoBookmarkPdf);
        this.searchBarBookedPdf = (MaterialSearchView) findViewById(R.id.searchBarPdf);

        adview = (AdView) findViewById(R.id.adview);
        banner();


        this.context = this;
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.searchBarBookedPdf.setOnQueryTextListener(this);



        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.isPdfGridEnabled = this.sharedPreferences.getBoolean(BrowsePDFActivity.GRID_VIEW_ENABLED, false);
        this.numOfColumnsPdfList = this.sharedPreferences.getInt(BrowsePDFActivity.GRID_VIEW_NUM_OF_COLUMNS, 2);
        new LoadBookMarkPdfFiles().execute(new Void[0]);
    }

    public class LoadBookMarkPdfFiles extends AsyncTask<Void, Void, Void> {

        public void onPreExecute() {
            super.onPreExecute();
        }

        public Void doInBackground(Void... voidArr) {
            listBookmarkPdfDataTypes = DbHelper.getInstance(context).getStarredPdfs();
            StarredPDFActivity starredPDFActivity = StarredPDFActivity.this;
            starredPDFActivity.starredPDFAdapter = new StarredPDFAdapter(starredPDFActivity.context, listBookmarkPdfDataTypes);
            return null;
        }


        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            if (listBookmarkPdfDataTypes.isEmpty()) {
                layNoBookmarkPdf.setVisibility(View.VISIBLE);
            } else {
                layNoBookmarkPdf.setVisibility(View.GONE);
            }
            if (isPdfGridEnabled) {
                StarredPDFActivity starredPDFActivity = StarredPDFActivity.this;
                starredPDFActivity.setupForGridView(starredPDFActivity.context, recycleBookedPdf, numOfColumnsPdfList);
            } else {
                StarredPDFActivity starredPDFActivity2 = StarredPDFActivity.this;
                starredPDFActivity2.setupForListView(starredPDFActivity2.context, recycleBookedPdf);
            }
            recycleBookedPdf.setAdapter(starredPDFAdapter);
        }
    }
    
    public void onBackPressed() {
        if (this.searchBarBookedPdf.isSearchOpen()) {
            this.searchBarBookedPdf.closeSearchingPdfData();
        } else {
            super.onBackPressed();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_select_pdf, menu);

        this.menuPdfListView = menu.findItem(R.id.menuPdfListView);
        this.menuPdfGrid = menu.findItem(R.id.menuPdfGrid);
        this.menuPdfGrid.getSubMenu().clearHeader();
        if (this.isPdfGridEnabled) {
            this.menuPdfListView.setVisible(true);
            this.menuPdfGrid.setVisible(false);
        } else {
            this.menuPdfListView.setVisible(false);
            this.menuPdfGrid.setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menuGridFiveColumns:
                setPdfGridView(5);
                break;
            case R.id.menuGridFourColumns:
                setPdfGridView(4);
                break;
            case R.id.menuPdfListView:
                setBookedPdfListView();
                break;
            case R.id.menuSearchPdfBookmark:
                this.searchBarBookedPdf.openPdfSearchData();
                break;
            case R.id.menuGridSixColumns:
                setPdfGridView(6);
                break;
            case R.id.menuGridThreeColumns:
                setPdfGridView(3);
                break;
            case R.id.menuGridTwoColumns:
                setPdfGridView(2);
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }


    public void openPdfForPDFReader(String str) {
        Intent intent = new Intent(this, PDFViewerActivity.class);
        intent.putExtra(BrowsePDFActivity.PDF_LOCATION, str);
        String str2 = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("PdfDataType location ");
        sb.append(str);
        Log.d(str2, sb.toString());
        startActivity(intent);
    }

    public void setBookedPdfListView() {
        Editor editor = this.sharedPreferences.edit();
        editor.putBoolean(BrowsePDFActivity.GRID_VIEW_ENABLED, false);
        editor.apply();
        setupForListView(this, this.recycleBookedPdf);
        this.starredPDFAdapter = new StarredPDFAdapter(this, this.listBookmarkPdfDataTypes);
        this.recycleBookedPdf.setAdapter(this.starredPDFAdapter);
        this.menuPdfListView.setVisible(false);
        this.menuPdfGrid.setVisible(true);
    }

    public void setPdfGridView(int i) {
        new BackgroundGenerateThumbnails(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        Editor edit = this.sharedPreferences.edit();
        edit.putBoolean(BrowsePDFActivity.GRID_VIEW_ENABLED, true);
        edit.putInt(BrowsePDFActivity.GRID_VIEW_NUM_OF_COLUMNS, i);
        edit.apply();
        setupForGridView(this, this.recycleBookedPdf, i);
        this.starredPDFAdapter = new StarredPDFAdapter(this, this.listBookmarkPdfDataTypes);
        this.recycleBookedPdf.setAdapter(this.starredPDFAdapter);
        this.menuPdfListView.setVisible(true);
        this.menuPdfGrid.setVisible(false);
    }

    public void setupForGridView(Context context2, RecyclerView recyclerView, int i) {
        Float valueOf = Float.valueOf(getResources().getDisplayMetrics().density);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context2, i, 1, false);
        recyclerView.setBackgroundColor(getResources().getColor(R.color.colorLightGray));
        recyclerView.setPadding((int) (valueOf.floatValue() * 4.0f), (int) (valueOf.floatValue() * 4.0f), (int) (valueOf.floatValue() * 6.0f), (int) (valueOf.floatValue() * 5.0f));
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    public void setupForListView(Context context2, RecyclerView recyclerView) {
        recyclerView.setBackgroundColor(getResources().getColor(17170443));
        recyclerView.setPadding(0, 0, 0, 0);
        recyclerView.setLayoutManager(new LinearLayoutManager(context2));
    }

    public void onStaredPdfClicked(PdfDataType pdfDataType) {
        openPdfForPDFReader(pdfDataType.getAbsolutePath());
    }

    public void searchBookedPDFFiles(String str) {
        ArrayList arrayList = new ArrayList();
        for (PdfDataType pdfDataType : this.listBookmarkPdfDataTypes) {
            if (pdfDataType.getName().toLowerCase().contains(str.toLowerCase())) {
                arrayList.add(pdfDataType);
            }
            this.starredPDFAdapter.filter(arrayList);
        }
    }

    @Subscribe
    public void onPdfRenameEvent(DataUpdatedEvent.PdfRenameEvent pdfRenameEvent) {
        Log.d(TAG, "PdfRenameEvent from stared");
        new LoadBookMarkPdfFiles().execute(new Void[0]);
    }

    public boolean onQueryTextSubmit(String str) {
        Log.d(TAG, "This is called form onQueryTextSubmit");
        searchBookedPDFFiles(str);
        return true;
    }

    public boolean onQueryTextChange(String str) {
        Log.d(TAG, "This is called form onQueryTextSubmit");
        searchBookedPDFFiles(str);
        return true;
    }

    public void banner() {
        AdRequest adRequest_banner = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
//                .addTestDevice("DC6E68F3F8A237F39235706823FCCA48").build();

        adview.loadAd(adRequest_banner);
        adview.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adview.setVisibility(View.VISIBLE);
                super.onAdLoaded();
            }
        });
    }

}
