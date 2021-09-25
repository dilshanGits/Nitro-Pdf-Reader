package com.example.vivek;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ProgressBar;
import android.widget.Spinner;
import com.example.vivek.adapters.SelectImagesAdapter;
import com.example.vivek.adapters.SelectImagesAdapter.OnImageSelectedListener;
import com.example.vivek.data.DbHelper;
import com.example.vivek.utils.Utils;
import java.util.ArrayList;

public class SelectImagesActivity extends AppCompatActivity implements OnImageSelectedListener {
    public Context context;
    public DbHelper dbHelper;
    public RecyclerView recyclerSelectImgFromGallery;
    public int numberOfColumns;
    public ProgressBar progressImgSelect;
    private SharedPreferences sharedPreferences;

  

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_select_images);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Spinner spinnerGalleryDirectory = (Spinner) findViewById(R.id.spinnerGalleryDirectory);
        this.recyclerSelectImgFromGallery = (RecyclerView) findViewById(R.id.recyclerSelectImgFromGallery);
        this.progressImgSelect = (ProgressBar) findViewById(R.id.progressImgSelect);
        this.dbHelper = DbHelper.getInstance(this);
        this.context = this;
        int i = Utils.isTablet(this) ? 6 : 3;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.numberOfColumns = this.sharedPreferences.getInt(BrowsePDFActivity.GRID_VIEW_NUM_OF_COLUMNS, i);
        spinnerGalleryDirectory.setSelection(3);
        spinnerGalleryDirectory.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (i) {
                    case 0:
                        new LoadImages("/").execute(new Void[0]);
                        return;
                    case 1:
                        new LoadImages("/DCIM/").execute(new Void[0]);
                        return;
                    case 2:
                        new LoadImages("/Download/").execute(new Void[0]);
                        return;
                    case 3:
                        new LoadImages("/Pictures/").execute(new Void[0]);
                        return;
                    case 4:
                        new LoadImages("/WhatsApp/Media/WhatsApp Images/").execute(new Void[0]);
                        return;
                    default:
                        return;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void onMultiSelectedPDF(ArrayList<String> arrayList) {
        Intent intent = new Intent(this, OrganizeImagesActivity.class);
        intent.putStringArrayListExtra(OrganizeImagesActivity.IMAGE_URIS, arrayList);
        startActivity(intent);
    }

    public class LoadImages extends AsyncTask<Void, Void, Void> {
        private SelectImagesAdapter adapter;
        private String imageDir;

        public LoadImages(String str) {
            this.imageDir = str;
        }

        public void onPreExecute() {
            super.onPreExecute();
            progressImgSelect.setVisibility(View.VISIBLE);
        }

        public Void doInBackground(Void... voidArr) {
            DbHelper access$100 = dbHelper;
            StringBuilder sb = new StringBuilder();
            sb.append(Environment.getExternalStorageDirectory());
            sb.append(this.imageDir);
            this.adapter = new SelectImagesAdapter(context, access$100.getAllImages(sb.toString()));
            return null;
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            progressImgSelect.setVisibility(View.GONE);
            recyclerSelectImgFromGallery.setLayoutManager(new GridLayoutManager(context, numberOfColumns, 1, false));
            recyclerSelectImgFromGallery.setAdapter(this.adapter);
        }
    }
    
    
}
