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
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.itextpdf.text.html.HtmlTags;
import com.example.vivek.adapters.SelectPDFAdapter;
import com.example.vivek.adapters.SelectPDFAdapter.OnMultiSelectedPDFListener;
import com.example.vivek.adapters.SelectPDFAdapter.OnSelectedPdfClickListener;
import com.example.vivek.data.DbHelper;
import com.example.vivek.models.PdfDataType;
import com.example.vivek.p006ui.MaterialSearchView;
import com.example.vivek.p006ui.MaterialSearchView.OnQueryTextListener;
import com.example.vivek.utils.Utils.BackgroundGenerateThumbnails;
import java.util.ArrayList;
import java.util.List;

public class SelectPDFActivity extends AppCompatActivity implements OnSelectedPdfClickListener, OnMultiSelectedPDFListener, OnQueryTextListener {
    public final String TAG = SelectPDFAdapter.class.getSimpleName();
    private String directoryPath;
    private boolean gridViewEnabled;
    private Boolean isDirectory,isMultiSelect;
    private List<PdfDataType> mPdfDataTypeFiles;
    private MenuItem menuPdfGrid, menuPdfListView;
    private int numberOfColumns;
    private MaterialSearchView searchForSelectPdf;
    private SelectPDFAdapter selectPDFAdapter;
    private RecyclerView recycleSelectPdfFile;
    private SharedPreferences sharedPreferences;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_select_pdf);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.searchForSelectPdf = (MaterialSearchView) findViewById(R.id.searchBarPdf);
        this.searchForSelectPdf.setOnQueryTextListener(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        this.isMultiSelect = Boolean.valueOf(intent.getBooleanExtra(PDFToolsActivity.MULTI_SELECTION, false));
        this.isDirectory = Boolean.valueOf(intent.getBooleanExtra(PDFToolsActivity.IS_DIRECTORY, false));
        this.directoryPath = intent.getStringExtra(PDFToolsActivity.DIRECTORY_PATH);
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.gridViewEnabled = this.sharedPreferences.getBoolean(BrowsePDFActivity.GRID_VIEW_ENABLED, false);
        this.numberOfColumns = this.sharedPreferences.getInt(BrowsePDFActivity.GRID_VIEW_NUM_OF_COLUMNS, 2);
        if (!this.isDirectory.booleanValue()) {
            loadSelectedPDFFiles();
        } else {
            loadPDFsFromDirectory(this.directoryPath);
        }
    }

    public void onBackPressed() {
        if (this.searchForSelectPdf.isSearchOpen()) {
            this.searchForSelectPdf.closeSearchingPdfData();
        } else {
            super.onBackPressed();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_select_pdf, menu);
        this.menuPdfListView = menu.findItem(R.id.menuPdfListView);
        this.menuPdfGrid = menu.findItem(R.id.menuPdfGrid);
        this.menuPdfGrid.getSubMenu().clearHeader();
        if (this.gridViewEnabled) {
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
            case R.id.action_by_date_modified /*2131296273*/:
                pdfSortByDateModified();
                break;
            case R.id.action_by_name /*2131296274*/:
                pdfSortByName();
                break;
            case R.id.action_by_size /*2131296275*/:
                pdfSortBySize();
                break;
            case R.id.menuGridFiveColumns /*2131296288*/:
                selectPdfShowGridView(5);
                break;
            case R.id.menuGridFourColumns /*2131296289*/:
                selectPdfShowGridView(4);
                break;
            case R.id.menuPdfListView /*2131296293*/:
                selectPdfShowListView();
                break;
            case R.id.menuSearchPdfBookmark /*2131296313*/:
                this.searchForSelectPdf.openPdfSearchData();
                break;
            case R.id.menuGridSixColumns /*2131296322*/:
                selectPdfShowGridView(6);
                break;
            case R.id.menuGridThreeColumns /*2131296325*/:
                selectPdfShowGridView(3);
                break;
            case R.id.menuGridTwoColumns /*2131296328*/:
                selectPdfShowGridView(2);
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void onSelectedPdfClicked(PdfDataType pdfDataType) {
        if (!this.isDirectory.booleanValue()) {
            ArrayList arrayList = new ArrayList();
            arrayList.add(pdfDataType.getAbsolutePath());
            sendPdfResults(arrayList);
            return;
        }
        Intent intent = new Intent(this, PDFViewerActivity.class);
        intent.putExtra(BrowsePDFActivity.PDF_LOCATION, pdfDataType.getAbsolutePath());
        startActivity(intent);
    }

    public void onMultiSelectedPDF(ArrayList<String> arrayList) {
        Intent intent = new Intent(this, OrganizeMergePDFActivity.class);
        intent.putStringArrayListExtra(PDFToolsActivity.PDF_PATHS, arrayList);
        if (!TextUtils.isEmpty(getIntent().getStringExtra(PDFToolsActivity.CALLING_ACTIVITY))) {
            sendPdfResults(arrayList);
        } else {
            startActivity(intent);
        }
    }

    public void loadSelectedPDFFiles() {
        this.mPdfDataTypeFiles = DbHelper.getInstance(this).getAllPdfs();
        this.selectPDFAdapter = new SelectPDFAdapter(this.mPdfDataTypeFiles, this, this.isMultiSelect);
        this.recycleSelectPdfFile = (RecyclerView) findViewById(R.id.recycleSelectPdfFile);
        if (this.gridViewEnabled) {
            setPdfInGridView(this, this.recycleSelectPdfFile, this.numberOfColumns);
        } else {
            setPdfInListView(this, this.recycleSelectPdfFile);
        }
        this.recycleSelectPdfFile.setAdapter(this.selectPDFAdapter);
    }

    public void loadPDFsFromDirectory(String str) {
        this.mPdfDataTypeFiles = DbHelper.getInstance(this).getAllPdfFromDirectory(str);
        this.selectPDFAdapter = new SelectPDFAdapter(this.mPdfDataTypeFiles, this, this.isMultiSelect);
        this.recycleSelectPdfFile = (RecyclerView) findViewById(R.id.recycleSelectPdfFile);
        if (this.gridViewEnabled) {
            setPdfInGridView(this, this.recycleSelectPdfFile, this.numberOfColumns);
        } else {
            setPdfInListView(this, this.recycleSelectPdfFile);
        }
        this.recycleSelectPdfFile.setAdapter(this.selectPDFAdapter);
    }

    public void sendPdfResults(ArrayList<String> arrayList) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(PDFToolsActivity.PDF_PATHS, arrayList);
        setResult(-1, intent);
        finish();
    }

    public void searchPDFFiles(String str) {
        ArrayList arrayList = new ArrayList();
        for (PdfDataType pdfDataType : this.mPdfDataTypeFiles) {
            if (pdfDataType.getName().toLowerCase().contains(str.toLowerCase())) {
                arrayList.add(pdfDataType);
            }
            this.selectPDFAdapter.filter(arrayList);
        }
    }

    public boolean onQueryTextSubmit(String str) {
        searchPDFFiles(str);
        return true;
    }

    public boolean onQueryTextChange(String str) {
        searchPDFFiles(str);
        String str2 = this.TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("Searched text ");
        sb.append(str);
        Log.d(str2, sb.toString());
        return true;
    }

    public void selectPdfShowListView() {
        Editor edit = this.sharedPreferences.edit();
        edit.putBoolean(BrowsePDFActivity.GRID_VIEW_ENABLED, false);
        edit.apply();
        setPdfInListView(this, this.recycleSelectPdfFile);
        this.selectPDFAdapter = new SelectPDFAdapter(this.mPdfDataTypeFiles, this, this.isMultiSelect);
        this.recycleSelectPdfFile.setAdapter(this.selectPDFAdapter);
        this.menuPdfListView.setVisible(false);
        this.menuPdfGrid.setVisible(true);
    }

    public void selectPdfShowGridView(int i) {
        new BackgroundGenerateThumbnails(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        Editor edit = this.sharedPreferences.edit();
        edit.putBoolean(BrowsePDFActivity.GRID_VIEW_ENABLED, true);
        edit.putInt(BrowsePDFActivity.GRID_VIEW_NUM_OF_COLUMNS, i);
        edit.apply();
        setPdfInGridView(this, this.recycleSelectPdfFile, i);
        this.selectPDFAdapter = new SelectPDFAdapter(this.mPdfDataTypeFiles, this, this.isMultiSelect);
        this.recycleSelectPdfFile.setAdapter(this.selectPDFAdapter);
        this.menuPdfListView.setVisible(true);
        this.menuPdfGrid.setVisible(false);
    }

    public void setPdfInGridView(Context context, RecyclerView recyclerView, int i) {
        Float valueOf = Float.valueOf(getResources().getDisplayMetrics().density);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, i, 1, false);
        recyclerView.setBackgroundColor(getResources().getColor(R.color.colorLightGray));
        recyclerView.setPadding((int) (valueOf.floatValue() * 4.0f), (int) (valueOf.floatValue() * 4.0f), (int) (valueOf.floatValue() * 6.0f), (int) (valueOf.floatValue() * 5.0f));
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    public void setPdfInListView(Context context, RecyclerView recyclerView) {
        recyclerView.setBackgroundColor(getResources().getColor(17170443));
        recyclerView.setPadding(0, 0, 0, 0);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    public void pdfSortByName() {
        Editor edit = this.sharedPreferences.edit();
        edit.putString(DbHelper.SORT_BY, "name");
        edit.apply();
        this.mPdfDataTypeFiles = DbHelper.getInstance(this).getAllPdfs();
        this.selectPDFAdapter.pdfDataUpdate(this.mPdfDataTypeFiles);
    }

    public void pdfSortByDateModified() {
        Editor edit = this.sharedPreferences.edit();
        edit.putString(DbHelper.SORT_BY, "date modified");
        edit.apply();
        this.mPdfDataTypeFiles = DbHelper.getInstance(this).getAllPdfs();
        this.selectPDFAdapter.pdfDataUpdate(this.mPdfDataTypeFiles);
    }

    public void pdfSortBySize() {
        Editor edit = this.sharedPreferences.edit();
        edit.putString(DbHelper.SORT_BY, HtmlTags.SIZE);
        edit.apply();
        this.mPdfDataTypeFiles = DbHelper.getInstance(this).getAllPdfs();
        this.selectPDFAdapter.pdfDataUpdate(this.mPdfDataTypeFiles);
    }
}
