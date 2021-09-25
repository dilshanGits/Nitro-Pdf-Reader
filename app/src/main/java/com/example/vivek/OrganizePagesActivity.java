package com.example.vivek;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vivek.adapters.OrganizePagesAdapter;
import com.example.vivek.models.PDFPage;
import com.example.vivek.utils.Utils;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.common.PDPageLabelRange;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class OrganizePagesActivity extends AppCompatActivity {
    public static String ORGANIZE_PAGES_TIP = "prefs_organize_pages";
    final String TAG = OrganizePagesActivity.class.getSimpleName();
    public String allPdfDocuments,allPdfPictureDir,pdfFilePath;
    public Button btnCancelProgress;
    private Button btnOpenPdfFile;
    FloatingActionButton floatBtnSave;
    private ImageView imgTapClose;
    public ImageView imgCloseProgress;
    public Context context;
    public TextView tvCurrentAction;
    public RelativeLayout rLayInfoTapMoreOptions;
    public ConstraintLayout mProgressView;
    public List<PDFPage> pdfPages = new ArrayList();
    private TextView tvDownloadPercent,tvSavedPdfPath;
    public ProgressBar progressBar, progressOrganizePages;
    public ConstraintLayout progressMain;
    public RecyclerView recycleOrganizePages;
    public SharedPreferences sharedPreferences;
    boolean showOrganizePagesTip;
    private ImageView imgPdfSuccess;


    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_organize_pages);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Environment.getExternalStorageDirectory());
        stringBuilder.append("/Pictures/AllPdf/tmp/");
        this.allPdfPictureDir = stringBuilder.toString();
        StringBuilder sb2 = new StringBuilder();
        sb2.append(Environment.getExternalStorageDirectory());
        sb2.append("/Documents/AllPdf/");
        this.allPdfDocuments = sb2.toString();
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.context = this;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.showOrganizePagesTip = this.sharedPreferences.getBoolean(ORGANIZE_PAGES_TIP, true);
        this.recycleOrganizePages = (RecyclerView) findViewById(R.id.recycleOrganizePages);
        this.progressOrganizePages = (ProgressBar) findViewById(R.id.progressOrganizePages);
        this.floatBtnSave = (FloatingActionButton) findViewById(R.id.floatBtnSave);
        this.progressMain = (ConstraintLayout) findViewById(R.id.progressMain);
        this.rLayInfoTapMoreOptions = (RelativeLayout) findViewById(R.id.rLayInfoTapMoreOptions);
        this.imgTapClose = (ImageView) findViewById(R.id.imgTapClose);
        this.imgTapClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                OrganizePagesActivity.this.rLayInfoTapMoreOptions.setVisibility(View.GONE);
                OrganizePagesActivity.this.rLayInfoTapMoreOptions.animate().translationY((float) (-OrganizePagesActivity.this.rLayInfoTapMoreOptions.getHeight())).alpha(0.0f).setListener(new AnimatorListener() {
                    public void onAnimationCancel(Animator animator) {
                    }

                    public void onAnimationRepeat(Animator animator) {
                    }

                    public void onAnimationStart(Animator animator) {
                    }

                    public void onAnimationEnd(Animator animator) {
                        OrganizePagesActivity.this.rLayInfoTapMoreOptions.setVisibility(View.GONE);
                        Editor edit = OrganizePagesActivity.this.sharedPreferences.edit();
                        edit.putBoolean(OrganizePagesActivity.ORGANIZE_PAGES_TIP, false);
                        edit.apply();
                    }
                });
            }
        });
        this.pdfFilePath = getIntent().getStringExtra("com.example.pdfreader.PDF_PATH");
        if (this.showOrganizePagesTip) {
            this.rLayInfoTapMoreOptions.setVisibility(View.VISIBLE);
        } else {
            this.rLayInfoTapMoreOptions.setVisibility(View.GONE);
        }
        new LoadOriginePageThumbnails().execute(new String[]{this.pdfFilePath});
        this.floatBtnSave.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                OrganizePagesActivity organizePagesActivity = OrganizePagesActivity.this;
                new SaveOrganizedPages(organizePagesActivity.getOrganizedPages(organizePagesActivity.pdfPages), OrganizePagesActivity.this.progressMain).execute(new Void[0]);
            }
        });
    }


    public class LoadOriginePageThumbnails extends AsyncTask<String, Void, Void> {
        OrganizePagesAdapter organizePagesAdapter;

        public void onPreExecute() {
            super.onPreExecute();
        }

        public Void doInBackground(String... strArr) {
            int i;
            String str;
            PdfiumCore pdfiumCore = new PdfiumCore(OrganizePagesActivity.this.context);
            Uri fromFile = Uri.fromFile(new File(strArr[0]));
            String str2 = OrganizePagesActivity.this.TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("Loading page thumbs from uri ");
            sb.append(fromFile.toString());
            Log.d(str2, sb.toString());
            try {
                PdfDocument newDocument = pdfiumCore.newDocument(OrganizePagesActivity.this.context.getContentResolver().openFileDescriptor(fromFile, PDPageLabelRange.STYLE_ROMAN_LOWER));
                int pageCount = pdfiumCore.getPageCount(newDocument);
                String str3 = OrganizePagesActivity.this.TAG;
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Total number of pages ");
                sb2.append(pageCount);
                Log.d(str3, sb2.toString());
                File file = new File(OrganizePagesActivity.this.allPdfPictureDir);
                if (!file.exists()) {
                    file.mkdirs();
                }
                int i2 = 0;
                while (i2 < pageCount) {
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append(OrganizePagesActivity.this.allPdfPictureDir);
                    sb3.append(System.currentTimeMillis());
                    sb3.append(".jpg");
                    String sb4 = sb3.toString();
                    String str4 = OrganizePagesActivity.this.TAG;
                    StringBuilder sb5 = new StringBuilder();
                    sb5.append("Generating temp img ");
                    sb5.append(sb4);
                    Log.d(str4, sb5.toString());
                    FileOutputStream fileOutputStream = new FileOutputStream(sb4);
                    pdfiumCore.openPage(newDocument, i2);
                    int pageWidthPoint = pdfiumCore.getPageWidthPoint(newDocument, i2) / 2;
                    int pageHeightPoint = pdfiumCore.getPageHeightPoint(newDocument, i2) / 2;
                    try {
                        Bitmap createBitmap = Bitmap.createBitmap(pageWidthPoint, pageHeightPoint, Config.RGB_565);
                        Bitmap bitmap = createBitmap;
                        i = pageCount;
                        str = sb4;
                        try {
                            pdfiumCore.renderPageBitmap(newDocument, createBitmap, i2, 0, 0, pageWidthPoint, pageHeightPoint, true);
                            bitmap.compress(CompressFormat.JPEG, 50, fileOutputStream);
                        } catch (OutOfMemoryError e) {
                            e = e;
                        }
                    } catch (OutOfMemoryError e2) {
                        OutOfMemoryError e = e2;
                        i = pageCount;
                        str = sb4;
                        Toast.makeText(OrganizePagesActivity.this.context, R.string.failed_low_memory, Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                        i2++;
                        OrganizePagesActivity.this.pdfPages.add(new PDFPage(i2, Uri.fromFile(new File(str))));
                        pageCount = i;
                    }
                    i2++;
                    OrganizePagesActivity.this.pdfPages.add(new PDFPage(i2, Uri.fromFile(new File(str))));
                    pageCount = i;
                }
                pdfiumCore.closeDocument(newDocument);
                return null;
            } catch (Exception e3) {
                e3.printStackTrace();
                return null;
            }
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            this.organizePagesAdapter = new OrganizePagesAdapter(OrganizePagesActivity.this.context, OrganizePagesActivity.this.pdfPages);
            OrganizePagesActivity.this.recycleOrganizePages.setLayoutManager(new GridLayoutManager(OrganizePagesActivity.this.context, Utils.isTablet(OrganizePagesActivity.this.context) ? 6 : 3, 1, false));
            OrganizePagesActivity.this.progressOrganizePages.setVisibility(View.GONE);
            OrganizePagesActivity.this.recycleOrganizePages.setAdapter(this.organizePagesAdapter);
            OrganizePagesActivity.this.floatBtnSave.setVisibility(View.VISIBLE);
            new ItemTouchHelper(new SimpleCallback(15, 0) {
                public void onSwiped(ViewHolder viewHolder, int i) {
                }

                public boolean onMove(RecyclerView recyclerView, ViewHolder viewHolder, ViewHolder viewHolder2) {
                    int adapterPosition = viewHolder.getAdapterPosition();
                    int adapterPosition2 = viewHolder2.getAdapterPosition();
                    OrganizePagesActivity.this.pdfPages.add(adapterPosition, (PDFPage) OrganizePagesActivity.this.pdfPages.remove(adapterPosition2));
                    LoadOriginePageThumbnails.this.organizePagesAdapter.notifyItemMoved(adapterPosition2, adapterPosition);
                    String str = OrganizePagesActivity.this.TAG;
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
                    String str = OrganizePagesActivity.this.TAG;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Page order after swap ");
                    OrganizePagesActivity organizePagesActivity = OrganizePagesActivity.this;
                    sb.append(organizePagesActivity.getOrganizedPages(organizePagesActivity.pdfPages).toString());
                    Log.d(str, sb.toString());
                }
            }).attachToRecyclerView(OrganizePagesActivity.this.recycleOrganizePages);
        }
    }

    public class SaveOrganizedPages extends AsyncTask<Void, Integer, Void> {
        private int numPages = 0;
        String organizedFilePath;
        private List<Integer> organizedPages;

        public SaveOrganizedPages(List<Integer> list, ConstraintLayout constraintLayout) {
            this.organizedPages = list;
            numPages = this.organizedPages.size();
            OrganizePagesActivity.this.mProgressView = constraintLayout;
            OrganizePagesActivity.this.initProgressView();
            Utils.setLightStatusBar(OrganizePagesActivity.this.context);
            OrganizePagesActivity.this.btnCancelProgress.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    SaveOrganizedPages.this.cancel(true);
                    OrganizePagesActivity organizePagesActivity = OrganizePagesActivity.this;
                    organizePagesActivity.closeProgressBar(organizePagesActivity.context);
                }
            });
        }

        public void onPreExecute() {
            super.onPreExecute();
            OrganizePagesActivity.this.progressBar.setMax(this.numPages);
            OrganizePagesActivity.this.tvCurrentAction.setText(R.string.organizing);
            OrganizePagesActivity.this.mProgressView.setVisibility(View.VISIBLE);
        }

        public Void doInBackground(Void... voidArr) {
            boolean z = PreferenceManager.getDefaultSharedPreferences(OrganizePagesActivity.this.context).getBoolean(BrowsePDFActivity.GRID_VIEW_ENABLED, false);
            try {
                File file = new File(OrganizePagesActivity.this.allPdfDocuments);
                File file2 = new File(OrganizePagesActivity.this.pdfFilePath);
                String name = file2.getName();
                StringBuilder sb = new StringBuilder();
                sb.append(OrganizePagesActivity.this.allPdfDocuments);
                sb.append(Utils.removePdfExtension(name));
                sb.append("-Organized.pdf");
                this.organizedFilePath = sb.toString();
                if (!file.exists()) {
                    file.mkdirs();
                }
                PDFBoxResourceLoader.init(OrganizePagesActivity.this.context);
                PDDocument load = PDDocument.load(file2);
                PDDocument pDDocument = new PDDocument();
                int i = 0;
                while (i < this.numPages && !isCancelled()) {
                    String str = OrganizePagesActivity.this.TAG;
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("Get page at from pdf ");
                    sb2.append(((Integer) this.organizedPages.get(i)).intValue() - 1);
                    Log.d(str, sb2.toString());
                    pDDocument.addPage(load.getPage(((Integer) this.organizedPages.get(i)).intValue() - 1));
                    i++;
                    publishProgress(new Integer[]{Integer.valueOf(i)});
                }
                pDDocument.save(new File(this.organizedFilePath));
                load.close();
                pDDocument.close();
                if (z) {
                    Utils.generatePDFThumbnail(OrganizePagesActivity.this.context, this.organizedFilePath);
                }
                MediaScannerConnection.scanFile(OrganizePagesActivity.this.context, new String[]{this.organizedFilePath}, new String[]{"application/pdf"}, null);
                String str2 = OrganizePagesActivity.this.TAG;
                StringBuilder sb3 = new StringBuilder();
                sb3.append("Page order");
                sb3.append(this.organizedPages);
                Log.d(str2, sb3.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            OrganizePagesActivity.this.updateDownloadingProgressPercent(numArr[0].intValue(), this.numPages);
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            OrganizePagesActivity.this.tvCurrentAction.setText(R.string.done);
            OrganizePagesActivity.this.btnCancelProgress.setOnClickListener(null);
            showInterstialAd(context, OrganizePagesActivity.this.allPdfDocuments);
            OrganizePagesActivity organizePagesActivity2 = OrganizePagesActivity.this;
            organizePagesActivity2.openPdfPath(organizePagesActivity2.context, OrganizePagesActivity.this.context.getString(R.string.open_file), this.organizedFilePath, true);
        }
    }


    public void onDestroy() {
        super.onDestroy();
        Utils.deletePdfFiles(this.allPdfPictureDir);
        String str = this.TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("Deleting temp dir ");
        sb.append(this.allPdfPictureDir);
        Log.d(str, sb.toString());
    }

    public List<Integer> getOrganizedPages(List<PDFPage> list) {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            arrayList.add(Integer.valueOf(((PDFPage) list.get(i)).getPageNumber()));
        }
        return arrayList;
    }

    public void initProgressView() {
        this.tvDownloadPercent = (TextView) this.mProgressView.findViewById(R.id.tvDownloadPercent);
        this.tvCurrentAction = (TextView) this.mProgressView.findViewById(R.id.tvCurrentAction);
        this.progressBar = (ProgressBar) this.mProgressView.findViewById(R.id.progressDownloading);
        this.tvSavedPdfPath = (TextView) this.mProgressView.findViewById(R.id.tvSavedPdfPath);
        this.imgPdfSuccess = (ImageView) this.mProgressView.findViewById(R.id.imgPdfSuccess);
        this.btnOpenPdfFile = (Button) this.mProgressView.findViewById(R.id.btnOpenPdfFile);
        this.btnCancelProgress = (Button) this.mProgressView.findViewById(R.id.btnCancelProgress);
        this.imgCloseProgress = (ImageView) this.mProgressView.findViewById(R.id.imgCloseProgress);
    }

    public void finishProcessBar(Context context2, String str) {
        this.tvDownloadPercent.setVisibility(View.INVISIBLE);
        this.progressBar.setVisibility(View.INVISIBLE);
        this.imgPdfSuccess.setVisibility(View.VISIBLE);
        this.imgCloseProgress.setVisibility(View.VISIBLE);
        this.btnOpenPdfFile.setVisibility(View.VISIBLE);
        this.btnCancelProgress.setVisibility(View.GONE);
        StringBuilder sb = new StringBuilder();
        sb.append(context2.getString(R.string.saved_to));
        sb.append(" ");
        sb.append(str);
        this.tvSavedPdfPath.setText(sb.toString());
    }

    public void closeProgressBar(Context context2) {
        this.mProgressView.setVisibility(View.GONE);
        this.imgPdfSuccess.setVisibility(View.GONE);
        this.btnOpenPdfFile.setVisibility(View.GONE);
        this.imgCloseProgress.setVisibility(View.GONE);
        this.progressBar.setVisibility(View.VISIBLE);
        this.tvDownloadPercent.setVisibility(View.VISIBLE);
        this.btnCancelProgress.setVisibility(View.VISIBLE);
        this.progressBar.setProgress(0);
        this.tvDownloadPercent.setText("0%");
        this.tvSavedPdfPath.setText("");
        Utils.clearLightStatusBar(context2);
    }

    public void closeProgressBar(View view) {
        this.progressMain.setVisibility(View.GONE);
        this.progressMain.findViewById(R.id.imgPdfSuccess).setVisibility(View.GONE);
        this.progressMain.findViewById(R.id.btnOpenPdfFile).setVisibility(View.GONE);
        this.progressMain.findViewById(R.id.imgCloseProgress).setVisibility(View.GONE);
        this.progressMain.findViewById(R.id.progressDownloading).setVisibility(View.VISIBLE);
        this.progressMain.findViewById(R.id.tvDownloadPercent).setVisibility(View.VISIBLE);
        this.progressMain.findViewById(R.id.btnCancelProgress).setVisibility(View.VISIBLE);
        TextView tvSavedPdfPath = (TextView) this.progressMain.findViewById(R.id.tvSavedPdfPath);
        TextView tvDownloadPercent = (TextView) this.progressMain.findViewById(R.id.tvDownloadPercent);
        ((ProgressBar) this.progressMain.findViewById(R.id.progressDownloading)).setProgress(0);
        tvDownloadPercent.setText("0%");
        tvSavedPdfPath.setText("");
        Utils.clearLightStatusBar(this);
    }

    public void updateDownloadingProgressPercent(int i, int i2) {
        int i3 = ((int) (((float) i) * 100.0f)) / i2;
        StringBuilder sb = new StringBuilder();
        sb.append(i3);
        sb.append("%");
        this.tvDownloadPercent.setText(sb.toString());
        this.progressBar.setProgress(i);
    }



    public void openPdfPath(final Context context2, String str, final String str2, final boolean z) {
        this.btnOpenPdfFile.setText(str);
        this.btnOpenPdfFile.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (z) {
                    File file = new File(str2);
                    Intent intent = new Intent(context2, PDFViewerActivity.class);
                    intent.putExtra(BrowsePDFActivity.PDF_LOCATION, file.getAbsolutePath());
                    String str = OrganizePagesActivity.this.TAG;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Open PDF from location ");
                    sb.append(file.getAbsolutePath());
                    Log.d(str, sb.toString());
                    context2.startActivity(intent);
                    return;
                }
                Intent intent2 = new Intent(context2, SelectPDFActivity.class);
                intent2.putExtra(PDFToolsActivity.IS_DIRECTORY, true);
                context2.startActivity(intent2.putExtra(PDFToolsActivity.DIRECTORY_PATH, str2));
            }
        });
    }

    public void showInterstialAd(final Context context2, final String str) {

        OrganizePagesActivity.this.finishProcessBar(context2, str);

        /*new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            public void run() {
                InterstitialAd ad = AdManager.getAd();
                if (ad != null) {
                    ad.setAdListener(new AdListener() {
                        public void onAdClosed() {
                            super.onAdClosed();
                            new AdManager((AppCompatActivity) context2, "ca-app-pub-5632270525178750/9317666956").createAd();
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                public void run() {
                                    C22642 r0 = C22642.this;
                                    OrganizePagesActivity.this.finishProcessBar(context2, str);
                                    Snackbar.make((View) OrganizePagesActivity.this.imgCloseProgress, (int) R.string.dont_like_ads, 4000).setAction((int) R.string.remove, (OnClickListener) new OnClickListener() {
                                        public void onClick(View view) {
                                            Utils.openProVersionPlayStore(context2);
                                        }
                                    }).show();
                                }
                            }, 800);
                        }
                    });
                    ad.show();
                    return;
                }
                OrganizePagesActivity.this.finishProcessBar(context2, str);
            }
        }, 1000);*/
    }

}
