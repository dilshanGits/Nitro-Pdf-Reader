package com.example.vivek.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

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
import com.example.vivek.adapters.BookmarksAdapter;
import com.example.vivek.data.DbHelper;
import com.example.vivek.models.BookmarkData;

import java.util.ArrayList;
import java.util.List;

public class BookmarksFragment extends Fragment {

    private static final String PDF_PATH = "pdf_path";
    BookmarksAdapter bookmarksAdapter;
    RecyclerView recyclerBookmarksPdf;
    Context context;
    public LinearLayout layNoBookmark;
    public String pdfFilesPath;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_bookmarks, viewGroup, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        recyclerBookmarksPdf = (RecyclerView) view.findViewById(R.id.recyclerBookmarksPdf);
        layNoBookmark = (LinearLayout) view.findViewById(R.id.layNoBookmark);
        new BookmarksPdfLoad().execute(new Void[0]);
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        context = getContext();
        if (getArguments() != null) {
            pdfFilesPath = getArguments().getString(PDF_PATH);
        }
    }

    public class BookmarksPdfLoad extends AsyncTask<Void, Void, Void> {
        List<BookmarkData> listBookmarkData = new ArrayList();

        public void onPreExecute() {
            super.onPreExecute();
        }

        public Void doInBackground(Void... voidArr) {
            this.listBookmarkData = DbHelper.getInstance(context).getBookmarks(pdfFilesPath);
            BookmarksFragment bookmarksFragment = BookmarksFragment.this;
            bookmarksFragment.bookmarksAdapter = new BookmarksAdapter(bookmarksFragment.context, this.listBookmarkData, bookmarksFragment.layNoBookmark);
            return null;
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            if (this.listBookmarkData.size() == 0) {
                layNoBookmark.setVisibility(View.VISIBLE);
                return;
            }
            BookmarksFragment bookmarksFragment = BookmarksFragment.this;
            bookmarksFragment.recyclerBookmarksPdf.setLayoutManager(new LinearLayoutManager(bookmarksFragment.context, 1, false));
            BookmarksFragment bookmarksFragment2 = BookmarksFragment.this;
            bookmarksFragment2.recyclerBookmarksPdf.setAdapter(bookmarksFragment2.bookmarksAdapter);
            layNoBookmark.setVisibility(View.GONE);
        }
    }

    public static BookmarksFragment newInstance(String str) {
        BookmarksFragment bookmarksFragment = new BookmarksFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PDF_PATH, str);
        bookmarksFragment.setArguments(bundle);
        return bookmarksFragment;
    }


}
