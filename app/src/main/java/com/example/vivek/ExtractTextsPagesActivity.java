package com.example.vivek;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.app.ShareCompat.IntentBuilder;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vivek.adapters.ExtractTextsPagesAdapter;
import com.example.vivek.models.PDFPage;
import com.example.vivek.utils.Utils;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.common.PDPageLabelRange;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


public class ExtractTextsPagesActivity extends AppCompatActivity {
    public static String ORGANIZE_PAGES_TIP = "prefs_organize_pages";
    final String TAG = ExtractTextsPagesActivity.class.getSimpleName();
    public String allPdfDocuments, allPdfPictureDir, pdfFilePath;
    public Button btnCancelProgress, btnOpenPdfFile;
    public FloatingActionButton floatBtnSave;
    private ImageView imgTapClose, imgPdfSuccess;
    public ImageView imgCloseProgress;
    public Context context;
    public TextView tvCurrentAction, tvDownloadPercent, tvSavedPdfPath;
    public ExtractTextsPagesAdapter extractTextsPagesAdapter;
    public RelativeLayout rLayInfoTapMoreOptions;
    public ConstraintLayout mProgressView, progressMain;
    public List<PDFPage> pdfPages = new ArrayList();
    public ProgressBar progressDownloading, progressOrganizePages;
    public RecyclerView recycleOrganizePages;
    public SharedPreferences sharedPreferences;
    boolean showOrganizePagesTip;


    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_extract_texts_pages);

        StringBuilder builderTmp = new StringBuilder();
        builderTmp.append(Environment.getExternalStorageDirectory());
        builderTmp.append("/Pictures/AllPdf/tmp/");
        allPdfPictureDir = builderTmp.toString();
        StringBuilder sb2 = new StringBuilder();
        sb2.append(Environment.getExternalStorageDirectory());
        sb2.append("/Documents/AllPdf/");
        allPdfDocuments = sb2.toString();
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        showOrganizePagesTip = sharedPreferences.getBoolean(ORGANIZE_PAGES_TIP, true);
        recycleOrganizePages = (RecyclerView) findViewById(R.id.recycleOrganizePages);
        progressOrganizePages = (ProgressBar) findViewById(R.id.progressOrganizePages);
        floatBtnSave = (FloatingActionButton) findViewById(R.id.floatBtnSave);
        progressMain = (ConstraintLayout) findViewById(R.id.progressMain);
        rLayInfoTapMoreOptions = (RelativeLayout) findViewById(R.id.rLayInfoTapMoreOptions);
        imgTapClose = (ImageView) findViewById(R.id.imgTapClose);
        imgTapClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                rLayInfoTapMoreOptions.setVisibility(View.GONE);
                rLayInfoTapMoreOptions.animate().translationY((float) (-rLayInfoTapMoreOptions.getHeight())).alpha(0.0f).setListener(new AnimatorListener() {
                    public void onAnimationCancel(Animator animator) {
                    }

                    public void onAnimationRepeat(Animator animator) {
                    }

                    public void onAnimationStart(Animator animator) {
                    }

                    public void onAnimationEnd(Animator animator) {
                        rLayInfoTapMoreOptions.setVisibility(View.GONE);
                        Editor edit = sharedPreferences.edit();
                        edit.putBoolean(ExtractTextsPagesActivity.ORGANIZE_PAGES_TIP, false);
                        edit.apply();
                    }
                });
            }
        });
        pdfFilePath = getIntent().getStringExtra("com.example.pdfreader.PDF_PATH");
        if (showOrganizePagesTip) {
            rLayInfoTapMoreOptions.setVisibility(View.VISIBLE);
        } else {
            rLayInfoTapMoreOptions.setVisibility(View.GONE);
        }
        new LoadPdfPageThumbPhotos().execute(new String[]{pdfFilePath});
        floatBtnSave.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                extractTextsPagesAdapter.finishActionBarMode(false);
                if (extractTextsPagesAdapter.getSelectedPages().size() > 0) {
                    ExtractTextsPagesActivity extractTextsPagesActivity = ExtractTextsPagesActivity.this;
                    ExtractPdfTextPageAyn extractPdfTextPageAyn = new ExtractPdfTextPageAyn(extractTextsPagesActivity.context, pdfFilePath, extractTextsPagesAdapter.getSelectedPages(), progressMain);
                    extractPdfTextPageAyn.execute(new Void[0]);
                    String str = TAG;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Selected pages ");
                    sb.append(extractTextsPagesAdapter.getSelectedPages().toString());
                    Log.d(str, sb.toString());
                    return;
                }
                Toast.makeText(context, R.string.select_at_least_one_page, Toast.LENGTH_LONG).show();
            }
        });
    }


    public class ExtractPdfTextPageAyn extends AsyncTask<Void, Integer, Void> {
        String errorMessage, extractedTextDir, extractedTextFilePath;
        Context mContext;
        int mNumPages;
        String pdfPath;
        List<Integer> selectedPages;
        boolean textExtractSuccess = true;

        public ExtractPdfTextPageAyn(Context context, String str, List<Integer> list, ConstraintLayout constraintLayout) {
            mContext = context;
            pdfPath = str;
            selectedPages = list;
            mNumPages = list.size();
            mProgressView = constraintLayout;
            initProgressView();
            Utils.setLightStatusBar(context);
            btnCancelProgress.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    ExtractPdfTextPageAyn.this.cancel(true);
                    ExtractPdfTextPageAyn extractPdfTextPageAyn = ExtractPdfTextPageAyn.this;
                    closeProgressBar(extractPdfTextPageAyn.mContext);
                }
            });
        }

        public void onPreExecute() {
            super.onPreExecute();
            progressDownloading.setIndeterminate(true);
            progressDownloading.setMax(mNumPages);
            tvCurrentAction.setText(R.string.extracting);
            mProgressView.setVisibility(View.VISIBLE);
        }

        public Void doInBackground(Void... voidArr) {
            StringBuilder sb = new StringBuilder();
            sb.append(allPdfDocuments);
            sb.append("Texts/");
            extractedTextDir = sb.toString();
            File file = new File(extractedTextDir);
            if (!file.exists()) {
                file.mkdirs();
            }
            try {
                File file2 = new File(pdfPath);
                String name = file2.getName();
                StringBuilder sb2 = new StringBuilder();
                sb2.append(extractedTextDir);
                sb2.append(Utils.removePdfExtension(name));
                sb2.append(".txt");
                extractedTextFilePath = sb2.toString();
                PDFBoxResourceLoader.init(mContext);
                PDDocument load = PDDocument.load(file2);
                if (!load.isEncrypted()) {
                    PDFTextStripper pDFTextStripper = new PDFTextStripper();
                    StringBuilder sb3 = new StringBuilder();
                    String pageEnd = pDFTextStripper.getPageEnd();
                    removeProgresIndeterminate(mContext, progressDownloading);
                    int i = 0;
                    while (true) {
                        if (!(i < mNumPages) || !(!isCancelled())) {
                            break;
                        }
                        int intValue = ((Integer) selectedPages.get(i)).intValue() + 1;
                        pDFTextStripper.setStartPage(intValue);
                        pDFTextStripper.setEndPage(intValue);
                        StringBuilder sb4 = new StringBuilder();
                        sb4.append(pDFTextStripper.getText(load));
                        sb4.append(pageEnd);
                        sb3.append(sb4.toString());
                        i++;
                        publishProgress(new Integer[]{Integer.valueOf(i)});
                    }
                    FileOutputStream fileOutputStream = new FileOutputStream(new File(extractedTextFilePath));
                    fileOutputStream.write(sb3.toString().getBytes());
                    load.close();
                    fileOutputStream.close();
                } else {
                    errorMessage = mContext.getString(R.string.file_protected_unprotect);
                    textExtractSuccess = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = mContext.getString(R.string.extraction_failed);
                textExtractSuccess = false;
            }
            return null;
        }

        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            updateProgressBarPercentage(numArr[0].intValue(), mNumPages);
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            tvCurrentAction.setText(R.string.done);
            btnCancelProgress.setOnClickListener(null);
            showInterstialAd(mContext, extractedTextDir);
            ExtractTextsPagesActivity extractTextsPagesActivity = ExtractTextsPagesActivity.this;
            Context context = mContext;
            extractTextsPagesActivity.openPdfPath(context, context.getString(R.string.open_file), extractedTextFilePath);
            if (!textExtractSuccess) {
                Toast.makeText(mContext, errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }


    public void initProgressView() {
        tvDownloadPercent = (TextView) mProgressView.findViewById(R.id.tvDownloadPercent);
        tvCurrentAction = (TextView) mProgressView.findViewById(R.id.tvCurrentAction);
        progressDownloading = (ProgressBar) mProgressView.findViewById(R.id.progressDownloading);
        tvSavedPdfPath = (TextView) mProgressView.findViewById(R.id.tvSavedPdfPath);
        imgPdfSuccess = (ImageView) mProgressView.findViewById(R.id.imgPdfSuccess);
        btnOpenPdfFile = (Button) mProgressView.findViewById(R.id.btnOpenPdfFile);
        btnCancelProgress = (Button) mProgressView.findViewById(R.id.btnCancelProgress);
        imgCloseProgress = (ImageView) mProgressView.findViewById(R.id.imgCloseProgress);
    }

    public void onDestroy() {
        super.onDestroy();
        Utils.deletePdfFiles(allPdfPictureDir);
        String str = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("Deleting temp dir ");
        sb.append(allPdfPictureDir);
        Log.d(str, sb.toString());
    }


    public void finishProcessBar(Context context2, String str) {
        tvDownloadPercent.setVisibility(View.INVISIBLE);
        progressDownloading.setVisibility(View.INVISIBLE);
        imgPdfSuccess.setVisibility(View.VISIBLE);
        imgCloseProgress.setVisibility(View.VISIBLE);
        btnOpenPdfFile.setVisibility(View.VISIBLE);
        btnCancelProgress.setVisibility(View.GONE);
        StringBuilder sb = new StringBuilder();
        sb.append(context2.getString(R.string.saved_to));
        sb.append(" ");
        sb.append(str);
        tvSavedPdfPath.setText(sb.toString());
    }

    public void closeProgressBar(Context context2) {
        mProgressView.setVisibility(View.GONE);
        imgPdfSuccess.setVisibility(View.GONE);
        btnOpenPdfFile.setVisibility(View.GONE);
        imgCloseProgress.setVisibility(View.GONE);
        progressDownloading.setVisibility(View.VISIBLE);
        tvDownloadPercent.setVisibility(View.VISIBLE);
        btnCancelProgress.setVisibility(View.VISIBLE);
        progressDownloading.setProgress(0);
        tvDownloadPercent.setText("0%");
        tvSavedPdfPath.setText("");
        Utils.clearLightStatusBar(context2);
    }

    public void closeProgressBar(View view) {
        progressMain.setVisibility(View.GONE);
        progressMain.findViewById(R.id.imgPdfSuccess).setVisibility(View.GONE);
        progressMain.findViewById(R.id.btnOpenPdfFile).setVisibility(View.GONE);
        progressMain.findViewById(R.id.imgCloseProgress).setVisibility(View.GONE);
        progressMain.findViewById(R.id.progressDownloading).setVisibility(View.VISIBLE);
        progressMain.findViewById(R.id.tvDownloadPercent).setVisibility(View.VISIBLE);
        progressMain.findViewById(R.id.btnCancelProgress).setVisibility(View.VISIBLE);
        TextView tvSavedPdfPath = (TextView) progressMain.findViewById(R.id.tvSavedPdfPath);
        TextView tvDownloadPercent = (TextView) progressMain.findViewById(R.id.tvDownloadPercent);
        ((ProgressBar) progressMain.findViewById(R.id.progressDownloading)).setProgress(0);
        tvDownloadPercent.setText("0%");
        tvSavedPdfPath.setText("");
        Utils.clearLightStatusBar(this);
    }

    public void updateProgressBarPercentage(int i, int i2) {
        int i3 = ((int) (((float) i) * 100.0f)) / i2;
        StringBuilder sb = new StringBuilder();
        sb.append(i3);
        sb.append("%");
        tvDownloadPercent.setText(sb.toString());
        progressDownloading.setProgress(i);
    }


    public class LoadPdfPageThumbPhotos extends AsyncTask<String, Void, Void> {

        public void onPreExecute() {
            super.onPreExecute();
        }

        public Void doInBackground(String... strArr) {
            int i;
            String str;
            PdfiumCore pdfiumCore = new PdfiumCore(context);
            Uri fromFile = Uri.fromFile(new File(strArr[0]));
            String str2 = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("Loading page thumbs from uri ");
            sb.append(fromFile.toString());
            Log.d(str2, sb.toString());
            try {
                PdfDocument newDocument = pdfiumCore.newDocument(context.getContentResolver().openFileDescriptor(fromFile, PDPageLabelRange.STYLE_ROMAN_LOWER));
                int pageCount = pdfiumCore.getPageCount(newDocument);
                String str3 = TAG;
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Total number of pages ");
                sb2.append(pageCount);
                Log.d(str3, sb2.toString());
                File file = new File(allPdfPictureDir);
                if (!file.exists()) {
                    file.mkdirs();
                }
                int i2 = 0;
                while (i2 < pageCount) {
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append(allPdfPictureDir);
                    sb3.append(System.currentTimeMillis());
                    sb3.append(".jpg");
                    String sb4 = sb3.toString();
                    String str4 = TAG;
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
                        Toast.makeText(context, R.string.failed_low_memory, Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                        i2++;
                        pdfPages.add(new PDFPage(i2, Uri.fromFile(new File(str))));
                        pageCount = i;
                    }
                    i2++;
                    pdfPages.add(new PDFPage(i2, Uri.fromFile(new File(str))));
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
            ExtractTextsPagesActivity extractTextsPagesActivity = ExtractTextsPagesActivity.this;
            extractTextsPagesActivity.extractTextsPagesAdapter = new ExtractTextsPagesAdapter(extractTextsPagesActivity.context, pdfPages);
            recycleOrganizePages.setLayoutManager(new GridLayoutManager(context, Utils.isTablet(context) ? 6 : 3, 1, false));
            progressOrganizePages.setVisibility(View.GONE);
            recycleOrganizePages.setAdapter(extractTextsPagesAdapter);
            floatBtnSave.setVisibility(View.VISIBLE);
        }
    }


    public void showInterstialAd(final Context context2, final String str) {

        finishProcessBar(context2, str);                 //changewbyown

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
                                    C22382 r0 = C22382.this;
                                    finishProcessBar(context2, str);
                                    Snackbar.make((View) imgCloseProgress, (int) R.string.dont_like_ads, 4000).setAction((int) R.string.remove, (OnClickListener) new OnClickListener() {
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
                finishProcessBar(context2, str);
            }
        }, 1000);*/
    }

    public void openPdfPath(final Context context2, String str, final String str2) {
        btnOpenPdfFile.setText(str);
        btnOpenPdfFile.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Uri uriForFile = FileProvider.getUriForFile(context2, Utils.FILE_AUTHORITY, new File(str2));
                Intent intent = IntentBuilder.from((Activity) context2).setType(context2.getContentResolver().getType(uriForFile)).setStream(uriForFile).getIntent();
                intent.setData(uriForFile);
                intent.addFlags(1);
                intent.setAction("android.intent.action.VIEW");
                intent.addFlags(268435456);
                if (intent.resolveActivity(context2.getPackageManager()) != null) {
                    context2.startActivity(intent);
                } else {
                    Toast.makeText(context2, R.string.no_proper_app_for_opening_text, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void removeProgresIndeterminate(Context context2, final ProgressBar progressBar2) {
        ((Activity) context2).runOnUiThread(new Runnable() {
            public void run() {
                progressBar2.setIndeterminate(false);
            }
        });
    }
}
