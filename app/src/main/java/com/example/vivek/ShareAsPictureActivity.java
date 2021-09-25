package com.example.vivek;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.vivek.adapters.ShareAsPictureAdapter;
import com.example.vivek.models.PDFPage;
import com.example.vivek.utils.Utils;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.tom_roush.pdfbox.pdmodel.common.PDPageLabelRange;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShareAsPictureActivity extends AppCompatActivity {
    public static String ORGANIZE_SHARE_PAGES_TIP = "prefs_organize_share_pages";
    final String TAG = ShareAsPictureActivity.class.getSimpleName();
    String strAllPdfDocuments, strAllPdfPictureDir, pdfDirAsfileName, pdfSavedFilePath;
    FloatingActionButton floatingBtnSave;
    private ImageView imgTapClose;
    Context context;
    List<PDFPage> listPdfPicFinal = new ArrayList();
    List<PDFPage> listPdfPages = new ArrayList();
    public RelativeLayout rLayTapMoreOptions;
    ShareAsPictureAdapter shareAsPictureAdapter;
    ProgressBar progressSharePdfPicture;
    RecyclerView recycleSharePdfPicture;
    public SharedPreferences sharedPreferences;
    boolean isPdfPicturePagesTip;


    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_share_as_picture);

        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory());
        sb.append("/Pictures/AllPdf/tmp/");
        strAllPdfPictureDir = sb.toString();
        StringBuilder sb2 = new StringBuilder();
        sb2.append(Environment.getExternalStorageDirectory());
        sb2.append("/Documents/AllPdf/");
        strAllPdfDocuments = sb2.toString();
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        isPdfPicturePagesTip = sharedPreferences.getBoolean(ORGANIZE_SHARE_PAGES_TIP, true);
        recycleSharePdfPicture = (RecyclerView) findViewById(R.id.recycleSharePdfPicture);
        progressSharePdfPicture = (ProgressBar) findViewById(R.id.progressSharePdfPicture);
        floatingBtnSave = (FloatingActionButton) findViewById(R.id.floatingBtnSave);
        rLayTapMoreOptions = (RelativeLayout) findViewById(R.id.rLayTapMoreOptions);
        imgTapClose = (ImageView) findViewById(R.id.imgTapClose);
        imgTapClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                rLayTapMoreOptions.setVisibility(View.GONE);
                rLayTapMoreOptions.animate().translationY((float) (-rLayTapMoreOptions.getHeight())).alpha(0.0f).setListener(new AnimatorListener() {
                    public void onAnimationCancel(Animator animator) {
                    }

                    public void onAnimationRepeat(Animator animator) {
                    }

                    public void onAnimationStart(Animator animator) {
                    }

                    public void onAnimationEnd(Animator animator) {
                        rLayTapMoreOptions.setVisibility(8);
                        Editor edit = sharedPreferences.edit();
                        edit.putBoolean(ShareAsPictureActivity.ORGANIZE_SHARE_PAGES_TIP, false);
                        edit.apply();
                    }
                });
            }
        });
        this.pdfSavedFilePath = getIntent().getStringExtra("com.example.pdfreader.PDF_PATH");
        if (this.isPdfPicturePagesTip) {
            this.rLayTapMoreOptions.setVisibility(View.VISIBLE);
        } else {
            this.rLayTapMoreOptions.setVisibility(View.GONE);
        }
        new LoadPdfPictureThumbAyn().execute(new String[]{this.pdfSavedFilePath});
        this.floatingBtnSave.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                ShareAsPictureActivity shareAsPictureActivity = ShareAsPictureActivity.this;
                shareAsPictureActivity.listPdfPicFinal = shareAsPictureActivity.shareAsPictureAdapter.getFinalOrganizedPages();
                ShareAsPictureActivity shareAsPictureActivity2 = ShareAsPictureActivity.this;
                new saveShareImagePdfAyn(shareAsPictureActivity2.getPdfPicturePages(shareAsPictureActivity2.listPdfPicFinal)).execute(new Void[0]);
            }
        });
    }


    public class LoadPdfPictureThumbAyn extends AsyncTask<String, Void, Void> {

        public void onPreExecute() {
            super.onPreExecute();
        }

        public Void doInBackground(String... strArr) {
            int i;
            String str;
            FileOutputStream fileOutputStream;
            PdfiumCore pdfiumCore = new PdfiumCore(context);
            pdfDirAsfileName = "share/";
            Uri parse = Uri.parse(strArr[0]);
            String str2 = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("Loading page thumbs from uri ");
            sb.append(parse.toString());
            Log.d(str2, sb.toString());
            try {
                PdfDocument newDocument = pdfiumCore.newDocument(context.getContentResolver().openFileDescriptor(parse, PDPageLabelRange.STYLE_ROMAN_LOWER));
                int pageCount = pdfiumCore.getPageCount(newDocument);
                String str3 = TAG;
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Total number of pages ");
                sb2.append(pageCount);
                Log.d(str3, sb2.toString());
                StringBuilder sb3 = new StringBuilder();
                sb3.append(strAllPdfPictureDir);
                sb3.append(pdfDirAsfileName);
                File file = new File(sb3.toString());
                if (!file.exists()) {
                    file.mkdirs();
                }
                int i2 = 0;
                while (i2 < pageCount) {
                    StringBuilder sb4 = new StringBuilder();
                    sb4.append(strAllPdfPictureDir);
                    sb4.append(pdfDirAsfileName);
                    sb4.append("page-");
                    int i3 = i2 + 1;
                    sb4.append(i3);
                    sb4.append(".jpg");
                    String sb5 = sb4.toString();
                    String str4 = TAG;
                    StringBuilder sb6 = new StringBuilder();
                    sb6.append("Generating temp share img ");
                    sb6.append(sb5);
                    Log.d(str4, sb6.toString());
                    FileOutputStream fileOutputStream2 = new FileOutputStream(sb5);
                    pdfiumCore.openPage(newDocument, i2);
                    int pageWidthPoint = pdfiumCore.getPageWidthPoint(newDocument, i2);
                    int pageHeightPoint = pdfiumCore.getPageHeightPoint(newDocument, i2);
                    OutOfMemoryError e;
                    try {
                        Bitmap createBitmap = Bitmap.createBitmap(pageWidthPoint, pageHeightPoint, Config.ARGB_8888);
                        Bitmap bitmap = createBitmap;
                        FileOutputStream fileOutputStream3 = fileOutputStream2;
                        i = pageCount;
                        str = sb5;
                        try {
                            pdfiumCore.renderPageBitmap(newDocument, createBitmap, i2, 0, 0, pageWidthPoint, pageHeightPoint, true);
                            fileOutputStream = fileOutputStream3;
                            try {
                                bitmap.compress(CompressFormat.JPEG, 100, fileOutputStream);
                            } catch (OutOfMemoryError e3) {
                                e = e3;
                            }
                        } catch (OutOfMemoryError e2) {
                            e = e2;
                            fileOutputStream = fileOutputStream3;
                            Toast.makeText(context, R.string.failed_low_memory, Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                            listPdfPages.add(new PDFPage(i3, Uri.fromFile(new File(str))));
                            fileOutputStream.close();
                            i2 = i3;
                            pageCount = i;
                        }
                    } catch (OutOfMemoryError e3) {
                        e = e3;
                        fileOutputStream = fileOutputStream2;
                        i = pageCount;
                        str = sb5;
                        Toast.makeText(context, R.string.failed_low_memory, Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                        listPdfPages.add(new PDFPage(i3, Uri.fromFile(new File(str))));
                        fileOutputStream.close();
                        i2 = i3;
                        pageCount = i;
                    }
                    listPdfPages.add(new PDFPage(i3, Uri.fromFile(new File(str))));
                    fileOutputStream.close();
                    i2 = i3;
                    pageCount = i;
                }
                pdfiumCore.closeDocument(newDocument);
                return null;
            } catch (Exception e4) {
                e4.printStackTrace();
                return null;
            }
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            ShareAsPictureActivity shareAsPictureActivity = ShareAsPictureActivity.this;
            shareAsPictureActivity.shareAsPictureAdapter = new ShareAsPictureAdapter(shareAsPictureActivity.context, shareAsPictureActivity.listPdfPages);
            int i = Utils.isTablet(context) ? 6 : 3;
            ShareAsPictureActivity shareAsPictureActivity2 = ShareAsPictureActivity.this;
            shareAsPictureActivity2.recycleSharePdfPicture.setLayoutManager(new GridLayoutManager(shareAsPictureActivity2.context, i, 1, false));
            progressSharePdfPicture.setVisibility(8);
            ShareAsPictureActivity shareAsPictureActivity3 = ShareAsPictureActivity.this;
            shareAsPictureActivity3.recycleSharePdfPicture.setAdapter(shareAsPictureActivity3.shareAsPictureAdapter);
            floatingBtnSave.setVisibility(0);
            new ItemTouchHelper(new SimpleCallback(15, 0) {
                public void onSwiped(ViewHolder viewHolder, int i) {
                }

                public boolean onMove(RecyclerView recyclerView, ViewHolder viewHolder, ViewHolder viewHolder2) {
                    int adapterPosition = viewHolder.getAdapterPosition();
                    int adapterPosition2 = viewHolder2.getAdapterPosition();
                    listPdfPages.add(adapterPosition, (PDFPage) listPdfPages.remove(adapterPosition2));
                    shareAsPictureAdapter.notifyItemMoved(adapterPosition2, adapterPosition);
                    String str = TAG;
                    StringBuilder sb = new StringBuilder();
                    sb.append("moved from ");
                    sb.append(adapterPosition);
                    sb.append(" to position ");
                    sb.append(adapterPosition2);
                    Log.d(str, sb.toString());
                    return true;
                }

                public void clearView(RecyclerView recyclerView, ViewHolder viewHolder) {
                    super.clearView(recyclerView, viewHolder);
                    String str = TAG;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Page order after swap ");
                    ShareAsPictureActivity shareAsPictureActivity = ShareAsPictureActivity.this;
                    sb.append(shareAsPictureActivity.getPdfPicturePages(shareAsPictureActivity.listPdfPages).toString());
                    Log.d(str, sb.toString());
                }
            }).attachToRecyclerView(recycleSharePdfPicture);
        }
    }

    public class saveShareImagePdfAyn extends AsyncTask<Void, Void, Void> {
        final int SPACE_BETWEEN_PAGES = 4;
        String imageName;
        private List<Integer> organizedPages;
        ProgressDialog progressDialog;

        public saveShareImagePdfAyn(List<Integer> list) {
            this.organizedPages = list;
        }

        public void onPreExecute() {
            super.onPreExecute();
            this.progressDialog = new ProgressDialog(context);
            this.progressDialog.setMessage(context.getString(R.string.saving_wait));
            this.progressDialog.show();
        }

        public Void doInBackground(Void... voidArr) {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append(strAllPdfPictureDir);
                sb.append(pdfDirAsfileName);
                File file = new File(sb.toString());
                String name = new File(pdfSavedFilePath).getName();
                if (!file.exists()) {
                    file.mkdirs();
                }
                StringBuilder sb2 = new StringBuilder();
                sb2.append(strAllPdfPictureDir);
                sb2.append(pdfDirAsfileName);
                sb2.append(Utils.removePdfExtension(name));
                sb2.append(".jpg");
                this.imageName = sb2.toString();
                ArrayList arrayList = new ArrayList();
                int size = this.organizedPages.size();
                String str = TAG;
                StringBuilder sb3 = new StringBuilder();
                sb3.append("Num of pages to merge ");
                sb3.append(size);
                Log.d(str, sb3.toString());
                Options options = new Options();
                options.inJustDecodeBounds = true;
                int i = 0;
                int i2 = 0;
                int i3 = 0;
                while (i2 < size) {
                    StringBuilder sb4 = new StringBuilder();
                    sb4.append(strAllPdfPictureDir);
                    sb4.append(pdfDirAsfileName);
                    sb4.append("page-");
                    sb4.append(this.organizedPages.get(i2));
                    sb4.append(".jpg");
                    BitmapFactory.decodeFile(sb4.toString(), options);
                    i2++;
                    i3 = i2 == size ? i3 + options.outHeight : i3 + options.outHeight + 4;
                    arrayList.add(Integer.valueOf(options.outWidth));
                }
                int intValue = ((Integer) Collections.max(arrayList)).intValue();
                String str2 = TAG;
                StringBuilder sb5 = new StringBuilder();
                sb5.append("Target width ");
                sb5.append(intValue);
                sb5.append(" Target height ");
                sb5.append(i3);
                Log.d(str2, sb5.toString());
                try {
                    Bitmap createBitmap = Bitmap.createBitmap(intValue, i3, Config.ARGB_8888);
                    createBitmap.eraseColor(getResources().getColor(R.color.colorPDFViewBg));
                    Canvas canvas = new Canvas(createBitmap);
                    int i4 = 0;
                    while (i < size) {
                        StringBuilder sb6 = new StringBuilder();
                        sb6.append(strAllPdfPictureDir);
                        sb6.append(pdfDirAsfileName);
                        sb6.append("page-");
                        sb6.append(this.organizedPages.get(i));
                        sb6.append(".jpg");
                        String sb7 = sb6.toString();
                        String str3 = TAG;
                        StringBuilder sb8 = new StringBuilder();
                        sb8.append("Bitmap decode from ");
                        sb8.append(sb7);
                        Log.d(str3, sb8.toString());
                        Bitmap decodeFile = BitmapFactory.decodeFile(sb7);
                        canvas.drawBitmap(decodeFile, (float) calculateLeft(decodeFile.getWidth(), intValue), (float) i4, null);
                        i++;
                        i4 = i == size ? i4 + decodeFile.getHeight() : i4 + decodeFile.getHeight() + 4;
                        decodeFile.recycle();
                    }
                    FileOutputStream fileOutputStream = new FileOutputStream(new File(this.imageName));
                    createBitmap.compress(CompressFormat.JPEG, 100, fileOutputStream);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (OutOfMemoryError e) {
                    Toast.makeText(context, R.string.failed_low_memory, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                String str4 = TAG;
                StringBuilder sb9 = new StringBuilder();
                sb9.append("File to share generated ");
                sb9.append(strAllPdfPictureDir);
                sb9.append(pdfDirAsfileName);
                sb9.append(Utils.removePdfExtension(name));
                sb9.append(".jpg");
                Log.d(str4, sb9.toString());
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return null;
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            this.progressDialog.dismiss();
            Utils.sharePdfFile(context, FileProvider.getUriForFile(context, Utils.FILE_AUTHORITY, new File(this.imageName)));
        }
    }

    public void onDestroy() {
        super.onDestroy();
        Utils.deletePdfFiles(this.strAllPdfPictureDir);
        String str = this.TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("Deleting temp dir ");
        sb.append(this.strAllPdfPictureDir);
        Log.d(str, sb.toString());
    }

    public List<Integer> getPdfPicturePages(List<PDFPage> list) {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            arrayList.add(Integer.valueOf(((PDFPage) list.get(i)).getPageNumber()));
        }
        return arrayList;
    }

    public int calculateLeft(int i, int i2) {
        if (i2 > i) {
            return (i2 - i) / 2;
        }
        return 0;
    }
}
