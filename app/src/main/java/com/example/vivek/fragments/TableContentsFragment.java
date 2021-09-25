package com.example.vivek.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.vivek.R;
import com.example.vivek.adapters.ContentsAdapter;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfDocument.Bookmark;
import com.shockwave.pdfium.PdfiumCore;
import com.tom_roush.pdfbox.pdmodel.common.PDPageLabelRange;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TableContentsFragment extends Fragment {
    private static final String PDF_PATH = "pdf_path";
    public static final String SAVED_STATE = "prefs_saved_state";
    ContentsAdapter contentsAdapter;
    Context context;
    public LinearLayout layNoPdfTableData;
    int lastFirstVisiblePosition = 0;
    public String mPdfPath;
    SharedPreferences sharedPreferences;
    RecyclerView recyclerPdfTable;


    public static TableContentsFragment newInstance(String str) {
        TableContentsFragment tableContentsFragment = new TableContentsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PDF_PATH, str);
        tableContentsFragment.setArguments(bundle);
        return tableContentsFragment;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.context = getContext();
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        if (getArguments() != null) {
            this.mPdfPath = getArguments().getString(PDF_PATH);
        }
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.recyclerPdfTable = (RecyclerView) view.findViewById(R.id.recyclerPdfTable);
        this.layNoPdfTableData = (LinearLayout) view.findViewById(R.id.layNoPdfTableData);
        new LoadTableOfPdfContentsData().execute(new Void[0]);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_table_contents, viewGroup, false);
    }

    public void onDestroy() {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) this.recyclerPdfTable.getLayoutManager();
        if (linearLayoutManager != null) {
            this.sharedPreferences.edit().putInt(SAVED_STATE, linearLayoutManager.findFirstCompletelyVisibleItemPosition()).apply();
        }
        super.onDestroy();
    }

    public class LoadTableOfPdfContentsData extends AsyncTask<Void, Void, Void> {
        List<Bookmark> contents = new ArrayList();
        private PdfDocument pdfDocument;
        private PdfiumCore pdfiumCore;

        public void onPreExecute() {
            super.onPreExecute();
        }

        public Void doInBackground(Void... voidArr) {
            TableContentsFragment tableContentsFragment = TableContentsFragment.this;
            tableContentsFragment.lastFirstVisiblePosition = tableContentsFragment.sharedPreferences.getInt(TableContentsFragment.SAVED_STATE, 0);
            try {
                this.pdfiumCore = new PdfiumCore(TableContentsFragment.this.context);
                this.pdfDocument = this.pdfiumCore.newDocument(TableContentsFragment.this.context.getContentResolver().openFileDescriptor(Uri.fromFile(new File(TableContentsFragment.this.mPdfPath)), PDPageLabelRange.STYLE_ROMAN_LOWER));
                this.contents = this.pdfiumCore.getTableOfContents(this.pdfDocument);
            } catch (Exception e) {
                e.printStackTrace();
            } catch (StackOverflowError e2) {
                e2.printStackTrace();
            }
            TableContentsFragment tableContentsFragment2 = TableContentsFragment.this;
            tableContentsFragment2.contentsAdapter = new ContentsAdapter(tableContentsFragment2.context, this.contents);
            return null;
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            if (this.contents.size() == 0) {
                TableContentsFragment.this.layNoPdfTableData.setVisibility(0);
            } else {
                TableContentsFragment.this.layNoPdfTableData.setVisibility(8);
            }
            TableContentsFragment tableContentsFragment = TableContentsFragment.this;
            tableContentsFragment.recyclerPdfTable.setLayoutManager(new LinearLayoutManager(tableContentsFragment.context, 1, false));
            TableContentsFragment tableContentsFragment2 = TableContentsFragment.this;
            tableContentsFragment2.recyclerPdfTable.setAdapter(tableContentsFragment2.contentsAdapter);
            TableContentsFragment.this.recyclerPdfTable.getLayoutManager().scrollToPosition(TableContentsFragment.this.lastFirstVisiblePosition);
        }
    }


}
