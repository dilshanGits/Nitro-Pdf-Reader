package com.example.vivek;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.example.vivek.utils.Utils;
import com.shockwave.pdfium.PdfDocument.Meta;
import com.shockwave.pdfium.PdfiumCore;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation;
import com.tom_roush.pdfbox.pdmodel.common.PDPageLabelRange;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.IOException;

public class EditMetadataActivity extends AppCompatActivity {

    public final String TAG = EditMetadataActivity.class.getSimpleName();
    EditText etAuthor, etCreatedDate, etCreatorName, etKeywords, etModifiedDate, etProducerName, etSubject, etTitle;
    String pdfPath;
    Context mContext;
    AdView adview;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_edit_metadata);

        if (Utils.isTablet(this)) {
            Utils.setLightStatusBar(this);
        } else {
            Utils.clearLightStatusBar(this);
        }
        setSupportActionBar((Toolbar) findViewById(R.id.toolBar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mContext = this;
        etTitle = (EditText) findViewById(R.id.etTitle);
        etAuthor = (EditText) findViewById(R.id.etAuthor);
        etCreatorName = (EditText) findViewById(R.id.etCreatorName);
        etProducerName = (EditText) findViewById(R.id.etProducerName);
        etSubject = (EditText) findViewById(R.id.etSubject);
        etKeywords = (EditText) findViewById(R.id.etKeywords);
        etCreatedDate = (EditText) findViewById(R.id.etCreatedDate);
        etModifiedDate = (EditText) findViewById(R.id.etModifiedDate);
        pdfPath = getIntent().getStringExtra("com.example.pdfreader.PDF_PATH");
        new LoadMetadata().execute(new Void[0]);

        adview = (AdView) findViewById(R.id.adview);
        banner();


    }


    public class LoadMetadata extends AsyncTask<Void, Void, Void> {
        Meta meta;
        ProgressDialog progressDialog;

        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage(getString(R.string.loading_wait));
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        public Void doInBackground(Void... voidArr) {
            try {
                PdfiumCore pdfiumCore = new PdfiumCore(EditMetadataActivity.this.mContext);
                this.meta = pdfiumCore.getDocumentMeta(pdfiumCore.newDocument(EditMetadataActivity.this.mContext.getContentResolver().openFileDescriptor(Uri.fromFile(new File(EditMetadataActivity.this.pdfPath)), PDPageLabelRange.STYLE_ROMAN_LOWER)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            this.progressDialog.dismiss();
            Meta meta2 = this.meta;
            if (meta2 != null) {
                EditMetadataActivity.this.etTitle.setText(meta2.getTitle());
                EditMetadataActivity.this.etAuthor.setText(this.meta.getAuthor());
                EditMetadataActivity.this.etCreatorName.setText(this.meta.getCreator());
                EditMetadataActivity.this.etProducerName.setText(this.meta.getProducer());
                EditMetadataActivity.this.etSubject.setText(this.meta.getSubject());
                EditMetadataActivity.this.etKeywords.setText(this.meta.getKeywords());
                EditMetadataActivity editMetadataActivity = EditMetadataActivity.this;
                editMetadataActivity.etCreatedDate.setText(Utils.formatMetadataDate(editMetadataActivity.getApplicationContext(), this.meta.getCreationDate()));
                EditMetadataActivity editMetadataActivity2 = EditMetadataActivity.this;
                editMetadataActivity2.etModifiedDate.setText(Utils.formatMetadataDate(editMetadataActivity2.getApplicationContext(), this.meta.getModDate()));
                return;
            }
            Toast.makeText(EditMetadataActivity.this.mContext, R.string.cant_load_metadata, Toast.LENGTH_LONG).show();
        }
    }

    public class SaveMetadata extends AsyncTask<Void, Void, Void> {
        boolean isSaved = false;
        ProgressDialog progressDialog;

        public void onPreExecute() {
            super.onPreExecute();
            this.progressDialog = new ProgressDialog(EditMetadataActivity.this.mContext);
            this.progressDialog.setMessage(EditMetadataActivity.this.mContext.getResources().getString(R.string.saving_wait));
            this.progressDialog.setCancelable(false);
            this.progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.show();
        }

        public Void doInBackground(Void... voidArr) {
            PDFBoxResourceLoader.init(EditMetadataActivity.this.mContext);
            try {
                PDDocument load = PDDocument.load(new File(EditMetadataActivity.this.pdfPath));
                if (!load.isEncrypted()) {
                    PDDocumentInformation documentInformation = load.getDocumentInformation();
                    documentInformation.setTitle(EditMetadataActivity.this.etTitle.getText().toString());
                    documentInformation.setAuthor(EditMetadataActivity.this.etAuthor.getText().toString());
                    documentInformation.setCreator(EditMetadataActivity.this.etCreatorName.getText().toString());
                    documentInformation.setProducer(EditMetadataActivity.this.etProducerName.getText().toString());
                    documentInformation.setSubject(EditMetadataActivity.this.etSubject.getText().toString());
                    documentInformation.setKeywords(EditMetadataActivity.this.etKeywords.getText().toString());
                    load.setDocumentInformation(documentInformation);
                    load.save(new File(EditMetadataActivity.this.pdfPath));
                    this.isSaved = true;
                    MediaScannerConnection.scanFile(EditMetadataActivity.this.mContext, new String[]{EditMetadataActivity.this.pdfPath}, new String[]{"application/pdf"}, null);
                } else {
                    Log.d(EditMetadataActivity.this.TAG, "Document is encrypted");
                    EditMetadataActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            EditMetadataActivity.this.showFileProtectedDialog();
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            this.progressDialog.dismiss();
            if (this.isSaved) {
                Toast.makeText(EditMetadataActivity.this.mContext, R.string.saved, Toast.LENGTH_LONG).show();
            }
        }
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_edit_metadata, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.menuSave) {
            new SaveMetadata().execute(new Void[0]);
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void showFileProtectedDialog() {
        Builder builder = new Builder(this);
        builder.setTitle(R.string.file_protected).setMessage(R.string.file_protected_unprotect).setPositiveButton(R.string.ok, null);
        builder.create().show();
    }

    public void banner() {
        AdRequest adRequest_banner = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();

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
