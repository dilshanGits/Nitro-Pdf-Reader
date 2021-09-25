package com.example.vivek;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.example.vivek.adapters.ToolsAdapter;
import com.example.vivek.adapters.ToolsAdapter.OnSelectedMenuClickListener;
import com.example.vivek.data.ToolsData;
import com.example.vivek.utils.PDFTools;
import com.example.vivek.utils.Utils;
import com.shockwave.pdfium.PdfiumCore;
import com.tom_roush.pdfbox.pdmodel.common.PDPageLabelRange;

import java.io.File;
import java.util.ArrayList;

import static com.example.vivek.utils.PDFTools.progressDownloading;

public class PDFToolsActivity extends AppCompatActivity implements OnSelectedMenuClickListener {
    public static final String CALLING_ACTIVITY = "com.example.pdfreader.CALLING_ACTIVITY";
    public static final String DIRECTORY_PATH = "com.example.pdfreader.DIRECTORY_PATH";
    public static final String IS_DIRECTORY = "com.example.pdfreader.IS_DIRECTORY";
    public static final String MULTI_SELECTION = "com.example.pdfreader.MULTI_SELECTION";
    public static final String PDF_PATH = "com.example.pdfreader.PDF_PATH";
    public static final String PDF_PATHS = "com.example.pdfreader.PDF_PATHS";
    public static final String PRE_SELECTED_PDF_PATH = "com.example.pdfreader.PRE_SELECTED_PDF_PATH";
    public final String TAG = PDFToolsActivity.class.getSimpleName();
    public Context context;
    public Uri preSelectedPdfUri;
    public ConstraintLayout progressMain;
    private int toolPosition;
    ImageView imgCloseProgress;
    AdView adview;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_pdftools);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.context = this;
        imgCloseProgress = (ImageView) findViewById(R.id.imgCloseProgress);
        RecyclerView recyclePdfTools = (RecyclerView) findViewById(R.id.recyclePdfTools);

        adview = (AdView) findViewById(R.id.adview);
        banner();


        this.progressMain = (ConstraintLayout) findViewById(R.id.progressMain);
        GridLayoutManager gridLayoutManager = new GridLayoutManager((Context) this, 3, 1, false);
        ToolsAdapter toolsAdapter = new ToolsAdapter(this, ToolsData.getTools(this));
        recyclePdfTools.setLayoutManager(gridLayoutManager);
        recyclePdfTools.setAdapter(toolsAdapter);
        this.preSelectedPdfUri = getPreSelectedPdfUri();
        new RemoveTempFolderData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);

    }

    public void closeProgressBar(Context context2) {
//        mProgressView.setVisibility(View.GONE);
//        imgPdfSuccess.setVisibility(View.GONE);
//        btnOpenPdfFile.setVisibility(View.GONE);
        imgCloseProgress.setVisibility(View.GONE);
        progressDownloading.setVisibility(View.VISIBLE);
//        tvDownloadPercent.setVisibility(View.VISIBLE);
//        btnCancelProgress.setVisibility(View.VISIBLE);
        progressDownloading.setProgress(0);
//        tvDownloadPercent.setText("0%");
//        tvSavedPdfPath.setText("");
        Utils.clearLightStatusBar(context2);
        finish();
    }


    public class RemoveTempFolderData extends AsyncTask<Void, Void, Void> {

        public Void doInBackground(Void... voidArr) {
            StringBuilder sb = new StringBuilder();
            sb.append(Environment.getExternalStorageDirectory());
            sb.append("/Pictures/AllPdf/tmp/");
            String sb2 = sb.toString();
            if (new File(sb2).exists()) {
                Log.d(PDFToolsActivity.this.TAG, "Start clearing temp folder");
                Utils.deletePdfFiles(sb2);
            }
            return null;
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            Log.d(PDFToolsActivity.this.TAG, "Cleared temp folder");
        }
    }

    public class LoadSplittingPdfTools extends AsyncTask<Void, Void, Void> {
        String mPdfPath;
        int numPages;
        ProgressDialog progressDialog;

        public LoadSplittingPdfTools(String str) {
            this.mPdfPath = str;
        }

        public void onPreExecute() {
            super.onPreExecute();
            this.progressDialog = new ProgressDialog(PDFToolsActivity.this.context);
            this.progressDialog.setMessage(PDFToolsActivity.this.getString(R.string.loading_wait));
            this.progressDialog.setCancelable(false);
            this.progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.show();
        }

        public Void doInBackground(Void... voidArr) {
            PdfiumCore pdfiumCore = new PdfiumCore(PDFToolsActivity.this.context);
            try {
                this.numPages = pdfiumCore.getPageCount(pdfiumCore.newDocument(PDFToolsActivity.this.context.getContentResolver().openFileDescriptor(Uri.fromFile(new File(this.mPdfPath)), PDPageLabelRange.STYLE_ROMAN_LOWER)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            this.progressDialog.dismiss();
            PDFToolsActivity.this.pdfSplittingDialog(this.mPdfPath, this.numPages);
        }
    }


    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 4842 && i2 == -1) {
            startPdfTool(this.toolPosition, intent.getStringArrayListExtra(PDF_PATHS));
        }
    }

    public void startPdfTool(int i, ArrayList<String> arrayList) {
        PDFTools pDFTools = new PDFTools();
        switch (i) {
            case 0:
                Log.d(this.TAG, "Just called but can't do anything");
                return;
            case 1:
                new LoadSplittingPdfTools((String) arrayList.get(0)).execute(new Void[0]);
                return;
            case 2:
                imgQualityDialog((String) arrayList.get(0));
                return;
            case 3:
                pDFTools.getClass();
                new PDFTools.ConvertPDFToPdfPictures(this, (String) arrayList.get(0), this.progressMain).execute(new Void[0]);
                return;
            case 4:
                pdfOrganizePages((String) arrayList.get(0));
                return;
            case 5:
                pdfDocumentMetadata((String) arrayList.get(0));
                return;
            case 6:
                pdfCompressionOptions((String) arrayList.get(0));
                return;
            case 7:
                extractPdfTextsPages((String) arrayList.get(0));
                return;
            default:
                Toast.makeText(this, "Clicked", Toast.LENGTH_LONG).show();
                return;
        }
    }

    public Uri getPreSelectedPdfUri() {
        String stringExtra = getIntent().getStringExtra(PRE_SELECTED_PDF_PATH);
        if (!TextUtils.isEmpty(stringExtra)) {
            return Uri.parse(stringExtra);
        }
        return null;
    }

    public void onBackPressed() {
        if (this.progressMain.findViewById(R.id.imgCloseProgress).getVisibility() == View.VISIBLE) {
            closeLoadingProgressBar(this.progressMain);

//            ExtractTextsPagesActivity.ExtractPdfTextPageAyn.this.cancel(true);
//            ExtractTextsPagesActivity.ExtractPdfTextPageAyn extractPdfTextPageAyn = ExtractTextsPagesActivity.ExtractPdfTextPageAyn.this;
//            closeProgressBar(extractPdfTextPageAyn.mContext);
        } else if (this.progressMain.getVisibility() != View.VISIBLE) {
            super.onBackPressed();
        }
    }

    public void pdfDocumentMetadata(String str) {
        Intent intent = new Intent(this, EditMetadataActivity.class);
        intent.putExtra("com.example.pdfreader.PDF_PATH", str);
        startActivity(intent);
    }

    public void pdfOrganizePages(String str) {
        Intent intent = new Intent(this, OrganizePagesActivity.class);
        intent.putExtra("com.example.pdfreader.PDF_PATH", str);
        startActivity(intent);
    }

    public void extractPdfTextsPages(String str) {
        Intent intent = new Intent(this, ExtractTextsPagesActivity.class);
        intent.putExtra("com.example.pdfreader.PDF_PATH", str);
        startActivity(intent);
    }

    public void onToolClicked(int i) {
        this.toolPosition = i;
        switch (i) {
            case 8:
                startActivityForResult(new Intent(this, SelectImagesActivity.class), 4842);
                return;
            case 9:
            case 10:
            case 11:
                Utils.showPremiumFeatureDialog(this);
                return;
            default:
                Intent intent = new Intent(this, SelectPDFActivity.class);
                if (i != 0 || this.preSelectedPdfUri == null) {
                    if (i == 0) {
                        intent.putExtra(MULTI_SELECTION, true);
                    }
                    if (this.preSelectedPdfUri != null) {
                        ArrayList arrayList = new ArrayList();
                        arrayList.add(this.preSelectedPdfUri.getPath());
                        startPdfTool(i, arrayList);
                        return;
                    }
                    startActivityForResult(intent, 4842);
                    return;
                }
                Intent intent2 = new Intent(this, OrganizeMergePDFActivity.class);
                ArrayList arrayList2 = new ArrayList();
                arrayList2.add(this.preSelectedPdfUri.getPath());
                intent2.putStringArrayListExtra(PDF_PATHS, arrayList2);
                startActivity(intent2);
                return;
        }
    }

    public void closeLoadingProgressBar(View view) {
        this.progressMain.setVisibility(View.GONE);
        this.progressMain.findViewById(R.id.imgPdfSuccess).setVisibility(View.GONE);
        this.progressMain.findViewById(R.id.btnOpenPdfFile).setVisibility(View.GONE);
        this.progressMain.findViewById(R.id.imgCloseProgress).setVisibility(View.GONE);
        this.progressMain.findViewById(R.id.progressDownloading).setVisibility(View.VISIBLE);
        this.progressMain.findViewById(R.id.tvDownloadPercent).setVisibility(View.VISIBLE);
        this.progressMain.findViewById(R.id.btnCancelProgress).setVisibility(View.VISIBLE);
        ProgressBar progressBar = (ProgressBar) this.progressMain.findViewById(R.id.progressDownloading);
        TextView tvSavedPdfPath = (TextView) this.progressMain.findViewById(R.id.tvSavedPdfPath);
        TextView tvDownloadPercent = (TextView) this.progressMain.findViewById(R.id.tvDownloadPercent);
        TextView tvDescription = (TextView) this.progressMain.findViewById(R.id.tvDescription);
        tvDescription.setVisibility(View.GONE);
        progressBar.setProgress(0);
        tvDownloadPercent.setText("0%");
        tvSavedPdfPath.setText("");
        tvDescription.setText("");
        Utils.clearLightStatusBar(this);
    }

    public void pdfCompressionLevelDialog(final String str) {
        final int[] iArr = {50};
        Builder builder = new Builder(this);
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        final Editor edit = defaultSharedPreferences.edit();
        builder.setTitle(R.string.compression_level).setSingleChoiceItems(R.array.compression_level, defaultSharedPreferences.getInt("prefs_checked_compression_quality", 1), new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                edit.putInt("prefs_checked_compression_quality", i);
                switch (i) {
                    case 0:
                        iArr[0] = 70;
                        return;
                    case 1:
                        iArr[0] = 50;
                        return;
                    case 2:
                        iArr[0] = 20;
                        return;
                    default:
                        return;
                }
            }
        }).setPositiveButton(R.string.compress, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                edit.apply();
                PDFTools pDFTools = new PDFTools();
                pDFTools.getClass();
                PDFTools.CompressPDFImproved compressPDFImproved = new PDFTools.CompressPDFImproved(PDFToolsActivity.this.context, str, iArr[0], PDFToolsActivity.this.progressMain);
                compressPDFImproved.execute(new Void[0]);

            }
        }).setNegativeButton(R.string.cancel, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.create().show();
    }

    public void pdfSplittingDialog(String str, int i) {
        Builder builder = new Builder(this);
        builder.setTitle(R.string.split_pdf).setView(getLayoutInflater().inflate(R.layout.dialog_split_options, null)).setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, null);
        final AlertDialog create = builder.create();
        create.show();
        Spinner spinnerSplitting = (Spinner) create.findViewById(R.id.spinnerSplitting);
        final EditText etSplitFrom = (EditText) create.findViewById(R.id.etSplitFrom);
        final EditText etSplitTo = (EditText) create.findViewById(R.id.etSplitTo);
        TextView tvNumberOfPages = (TextView) create.findViewById(R.id.tvNumberOfPages);
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.number_of_pages));
        sb.append(" ");
        sb.append(i);
        tvNumberOfPages.setText(sb.toString());
        final int[] iArr = {0};
        spinnerSplitting.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> adapterView) {
            }

            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {
                switch (i) {
                    case 0:
                        iArr[0] = 0;
                        etSplitFrom.setVisibility(View.INVISIBLE);
                        etSplitTo.setVisibility(View.INVISIBLE);
                        etSplitFrom.setText("");
                        etSplitTo.setText("");
                        return;
                    case 1:
                        iArr[0] = 1;
                        etSplitFrom.setVisibility(View.VISIBLE);
                        etSplitTo.setVisibility(View.INVISIBLE);
                        etSplitFrom.setHint(R.string.at);
                        etSplitFrom.setText("");
                        etSplitTo.setText("");
                        return;
                    case 2:
                        iArr[0] = 2;
                        etSplitFrom.setVisibility(View.VISIBLE);
                        etSplitTo.setVisibility(View.VISIBLE);
                        etSplitFrom.setHint(R.string.from);
                        etSplitFrom.setText("");
                        etSplitTo.setText("");
                        return;
                    default:
                        return;
                }
            }
        });
        Button button = create.getButton(-1);
        final String str2 = str;
        final int i2 = i;
        View.OnClickListener r4 = new View.OnClickListener() {
            public void onClick(View view) {
                PDFTools pDFTools = new PDFTools();
                switch (iArr[0]) {
                    case 0:
                        pDFTools.getClass();
                        new PDFTools.SplitPDF(PDFToolsActivity.this.context, str2, PDFToolsActivity.this.progressMain).execute(new Void[0]);
                        create.cancel();
                        return;
                    case 1:
                        int intValue = TextUtils.isEmpty(etSplitFrom.getText().toString()) ? 0 : Integer.valueOf(etSplitFrom.getText().toString()).intValue();
                        if (intValue <= 0 || intValue > i2) {
                            etSplitFrom.setError(PDFToolsActivity.this.getString(R.string.invalid_value));
                            return;
                        }
                        pDFTools.getClass();
                        PDFTools.SplitPDF splitPDF = new PDFTools.SplitPDF(PDFToolsActivity.this.context, str2, PDFToolsActivity.this.progressMain, intValue);
                        splitPDF.execute(new Void[0]);
                        create.cancel();
                        return;
                    case 2:
                        int intValue2 = TextUtils.isEmpty(etSplitFrom.getText().toString()) ? 0 : Integer.valueOf(etSplitFrom.getText().toString()).intValue();
                        int intValue3 = TextUtils.isEmpty(etSplitTo.getText().toString()) ? 0 : Integer.valueOf(etSplitTo.getText().toString()).intValue();
                        if (intValue2 > 0) {
                            int i = i2;
                            if (intValue2 <= i) {
                                if (intValue3 <= 0 || intValue3 > i || intValue3 <= intValue2) {
                                    etSplitTo.setError(PDFToolsActivity.this.getString(R.string.invalid_value));
                                    return;
                                }
                                pDFTools.getClass();
                                PDFTools.SplitPDF splitPDF2 = new PDFTools.SplitPDF(PDFToolsActivity.this.context, str2, PDFToolsActivity.this.progressMain, intValue2, intValue3);
                                splitPDF2.execute(new Void[0]);


                                create.cancel();
                                return;
                            }
                        }
                        etSplitFrom.setError(PDFToolsActivity.this.getString(R.string.invalid_value));
                        return;
                    default:
                        return;
                }
            }
        };
        button.setOnClickListener(r4);
    }

    public void imgQualityDialog(final String str) {
        final int[] iArr = {50};
        Builder builder = new Builder(this);
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        final Editor edit = defaultSharedPreferences.edit();
        builder.setTitle(R.string.image_quality).setSingleChoiceItems(R.array.compression_level, defaultSharedPreferences.getInt("prefs_checked_img_quality", 1), new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                edit.putInt("prefs_checked_img_quality", i);
                switch (i) {
                    case 0:
                        iArr[0] = 30;
                        return;
                    case 1:
                        iArr[0] = 65;
                        return;
                    case 2:
                        iArr[0] = 100;
                        return;
                    default:
                        return;
                }
            }
        }).setPositiveButton(R.string.extract, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                edit.apply();
                PDFTools pDFTools = new PDFTools();
                pDFTools.getClass();
                PDFTools.ExtractPdfImages extractPdfImages = new PDFTools.ExtractPdfImages(PDFToolsActivity.this.context, str, iArr[0], PDFToolsActivity.this.progressMain);
                extractPdfImages.execute(new Void[0]);

            }
        }).setNegativeButton(R.string.cancel, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.create().show();
    }

    public void pdfCompressionOptions(String str) {
        if (!Utils.getAvailableMemory(this).lowMemory) {
            pdfCompressionLevelDialog(str);
        } else {
            Toast.makeText(this, R.string.cant_compress_low_memory, Toast.LENGTH_LONG).show();
        }
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
