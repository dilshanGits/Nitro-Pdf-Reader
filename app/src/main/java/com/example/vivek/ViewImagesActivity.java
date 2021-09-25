package com.example.vivek;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import com.example.vivek.adapters.ViewImagesAdapter;
import com.example.vivek.data.DbHelper;
import java.util.List;

public class ViewImagesActivity extends AppCompatActivity {
    public static final String GENERATED_IMAGES_PATH = "com.example.pdfreader.GENERATED_IMAGES_PATH";
    public final String TAG = ViewImagesActivity.class.getSimpleName();
    public ProgressBar progressViewImage;
    public RecyclerView recycleViewPdfImage;
    
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_view_images);
        
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        recycleViewPdfImage = (RecyclerView) findViewById(R.id.recycleViewPdfImage);
        progressViewImage = (ProgressBar) findViewById(R.id.progressViewImage);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String string = extras.getString(GENERATED_IMAGES_PATH);
            recycleViewPdfImage.setLayoutManager(new LinearLayoutManager(this, 1, false));
            new LoadPdfViewImages(this, string).execute(new Void[0]);
        }
    }

    public class LoadPdfViewImages extends AsyncTask<Void, Void, Void> {
        ViewImagesAdapter viewImagesAdapter;
        Context context;
        String imageDirectory;

        public LoadPdfViewImages(Context context2, String str) {
            context = context2;
            imageDirectory = str;
        }

        public void onPreExecute() {
            super.onPreExecute();
        }

        public Void doInBackground(Void... voidArr) {
            List allPdfImages = DbHelper.getInstance(context).getAllImages(imageDirectory);
            String access$000 = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("Images so far ");
            sb.append(allPdfImages.size());
            Log.d(access$000, sb.toString());
            viewImagesAdapter = new ViewImagesAdapter(context, allPdfImages);
            return null;
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            progressViewImage.setVisibility(View.GONE);
            recycleViewPdfImage.setAdapter(viewImagesAdapter);
        }
    }
    
}
