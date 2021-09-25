package com.example.vivek;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.vivek.adapters.BookmarksAdapter.OnBookmarkClickedListener;
import com.example.vivek.adapters.ContentsAdapter.OnContentClickedListener;
import com.example.vivek.adapters.ContentsPagerAdapter;
import com.example.vivek.models.BookmarkData;
import com.shockwave.pdfium.PdfDocument;
import java.io.File;

public class ContentsActivity extends AppCompatActivity implements OnBookmarkClickedListener, OnContentClickedListener {
    
    TabLayout tabBookmarkPdf;
    ViewPager pager;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_contents);
        
        String stringExtra = getIntent().getStringExtra("com.example.pdfreader.CONTENTS_PDF_PATH");
        tabBookmarkPdf = (TabLayout) findViewById(R.id.tabBookmarkPdf);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));


        File file = new File(stringExtra);
        getSupportActionBar().setTitle(file.getName());
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new ContentsPagerAdapter(getSupportFragmentManager(), stringExtra));

        TabLayout tabAllPdf = tabBookmarkPdf;
        tabAllPdf.addTab(tabAllPdf.newTab().setText((int) R.string.contents));

        TabLayout tabBookmarkPdf = this.tabBookmarkPdf;
        tabBookmarkPdf.addTab(tabBookmarkPdf.newTab().setText((int) R.string.bookmarks));
        pager.addOnPageChangeListener(new TabLayoutOnPageChangeListener(this.tabBookmarkPdf));
        tabBookmarkPdf.addOnTabSelectedListener(new OnTabSelectedListener() {
            @Override
            public void onTabSelected(Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(Tab tab) {

            }

            @Override
            public void onTabReselected(Tab tab) {

            }
        });
    }

    public void onBookmarkClicked(BookmarkData bookmarkData) {
        Intent intent = new Intent();
        intent.putExtra("com.example.pdfreader.PAGE_NUMBER", bookmarkData.getPageNumber());
        setResult(-1, intent);
        finish();
    }

    public void onContentClicked(PdfDocument.Bookmark bookmark) {
        Intent intent = new Intent();
        intent.putExtra("com.example.pdfreader.PAGE_NUMBER", ((int) bookmark.getPageIdx()) + 1);
        setResult(-1, intent);
        finish();
    }
}
