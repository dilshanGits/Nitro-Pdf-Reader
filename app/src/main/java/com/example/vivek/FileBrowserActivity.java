package com.example.vivek;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import com.example.vivek.adapters.FileBrowserAdapter.OnPdfClickListener;
import com.example.vivek.fragments.FileListFragment;
import com.example.vivek.models.PdfDataType;

import java.io.File;

public class FileBrowserActivity extends AppCompatActivity implements OnPdfClickListener {
    final String TAG = FileBrowserActivity.class.getSimpleName();
    String rootPath;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_file_browser);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory());
        sb.append("/");
        this.rootPath = sb.toString();
        String str = this.TAG;
        StringBuilder builderRootPath = new StringBuilder();
        builderRootPath.append("Root path ");
        builderRootPath.append(this.rootPath);
        Log.d(str, builderRootPath.toString());
        browseFileDirPathlList(this.rootPath);
    }

    public void browseFileDirPathlList(String str) {
        if (new File(str).isDirectory()) {
            FileListFragment newInstance = FileListFragment.newInstance(str);
            if (TextUtils.equals(str, this.rootPath)) {
                getSupportFragmentManager().beginTransaction().replace(R.id.framePdfFileList, newInstance).commit();
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.framePdfFileList, newInstance).addToBackStack(null).commit();
            }
        } else {
            Intent intent = new Intent(this, PDFViewerActivity.class);
            intent.putExtra(BrowsePDFActivity.PDF_LOCATION, str);
            startActivity(intent);
        }
    }

    public void onPdfClicked(PdfDataType pdfDataType) {
        browseFileDirPathlList(pdfDataType.getAbsolutePath());
    }
}
