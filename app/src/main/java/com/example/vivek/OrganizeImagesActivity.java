package com.example.vivek;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vivek.adapters.OrganizeImagesAdapter;
import com.example.vivek.models.ImagePage;
import com.example.vivek.utils.ImageUtils;
import com.example.vivek.utils.Utils;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OrganizeImagesActivity extends AppCompatActivity {
    public static final String IMAGE_URIS = "com.example.pdfreader.IMAGE_URIS";
    public static String ORGANIZE_PAGES_TIP = "prefs_organize_pages";
    final String TAG = OrganizeImagesActivity.class.getSimpleName();
    public OrganizeImagesAdapter organizeImagesAdapter;
    public String allPdfDocuments;
    public Button btnCancelProgress;
    private Button btnOpenPdfFile;
    FloatingActionButton floatBtnSave;
    private ImageView imgTapClose, imgPdfSuccess;
    public ImageView imgCloseProgress;
    public Context context;
    public TextView tvCurrentAction, tvSavedPdfPath;
    private TextView tvDownloadPercent;
    public List<ImagePage> listImagePages = new ArrayList();
    public ArrayList<String> arrayImageUris;
    public RelativeLayout rLayTapMoreOptions;
    public ConstraintLayout mProgressView, progressMain;
    public ProgressBar progressDownloading, progressOrganizePages;
    public RecyclerView recycleOrganizePages;
    public SharedPreferences sharedPreferences;
    boolean showOrganizePagesTip;


    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_organize_images);

        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory());
        sb.append("/Documents/AllPdf/");
        allPdfDocuments = sb.toString();
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        showOrganizePagesTip = sharedPreferences.getBoolean(ORGANIZE_PAGES_TIP, true);
        recycleOrganizePages = (RecyclerView) findViewById(R.id.recycleOrganizePages);
        progressOrganizePages = (ProgressBar) findViewById(R.id.progressOrganizePages);
        floatBtnSave = (FloatingActionButton) findViewById(R.id.floatBtnSave);
        progressMain = (ConstraintLayout) findViewById(R.id.progressMain);
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
                        rLayTapMoreOptions.setVisibility(View.GONE);
                        Editor edit = sharedPreferences.edit();
                        edit.putBoolean(OrganizeImagesActivity.ORGANIZE_PAGES_TIP, false);
                        edit.apply();
                    }
                });
            }
        });
        arrayImageUris = getIntent().getStringArrayListExtra(IMAGE_URIS);
        if (showOrganizePagesTip) {
            rLayTapMoreOptions.setVisibility(View.VISIBLE);
        } else {
            rLayTapMoreOptions.setVisibility(View.GONE);
        }
        new LoadImagePageThumbAyn(arrayImageUris).execute(new Void[0]);
        floatBtnSave.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (listImagePages.size() >= 1) {
                    showImagePdfFileNameDialog();
                } else {
                    Toast.makeText(context, R.string.select_at_least_one_image, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public Bitmap rotateImageBitmap(Bitmap bitmap, int i) {
        Matrix matrix = new Matrix();
        switch (i) {
            case 1:
                return bitmap;
            case 2:
                matrix.setScale(-1.0f, 1.0f);
                break;
            case 3:
                matrix.setRotate(180.0f);
                break;
            case 4:
                matrix.setRotate(180.0f);
                matrix.postScale(-1.0f, 1.0f);
                break;
            case 5:
                matrix.setRotate(90.0f);
                matrix.postScale(-1.0f, 1.0f);
                break;
            case 6:
                matrix.setRotate(90.0f);
                break;
            case 7:
                matrix.setRotate(-90.0f);
                matrix.postScale(-1.0f, 1.0f);
                break;
            case 8:
                matrix.setRotate(-90.0f);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap createBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return createBitmap;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Integer> getImageOrganizedPages(List<ImagePage> list) {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            arrayList.add(Integer.valueOf(((ImagePage) list.get(i)).getPageNumber()));
        }
        return arrayList;
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
        TextView textView = (TextView) progressMain.findViewById(R.id.tvSavedPdfPath);
        TextView textView2 = (TextView) progressMain.findViewById(R.id.tvDownloadPercent);
        ((ProgressBar) progressMain.findViewById(R.id.progressDownloading)).setProgress(0);
        textView2.setText("0%");
        textView.setText("");
        Utils.clearLightStatusBar(this);
    }

    public void updateDownloadingProgressPercent(int i, int i2) {
        int i3 = ((int) (((float) i) * 100.0f)) / i2;
        StringBuilder sb = new StringBuilder();
        sb.append(i3);
        sb.append("%");
        tvDownloadPercent.setText(sb.toString());
        progressDownloading.setProgress(i);
    }



    public class LoadImagePageThumbAyn extends AsyncTask<Void, Void, Void> {
        public LoadImagePageThumbAyn(ArrayList<String> arrayList) {
            arrayImageUris = arrayList;
        }

        public void onPreExecute() {
            super.onPreExecute();
        }

        public Void doInBackground(Void... voidArr) {
            int i = 0;
            while (i < arrayImageUris.size()) {
                int i2 = i + 1;
                listImagePages.add(new ImagePage(i2, Uri.parse((String) arrayImageUris.get(i))));
                i = i2;
            }
            OrganizeImagesActivity organizeImagesActivity = OrganizeImagesActivity.this;
            organizeImagesActivity.organizeImagesAdapter = new OrganizeImagesAdapter(organizeImagesActivity.context, listImagePages);
            return null;
        }

        @SuppressLint({"RestrictedApi"})
        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            recycleOrganizePages.setLayoutManager(new GridLayoutManager(context, Utils.isTablet(context) ? 6 : 3, 1, false));
            progressOrganizePages.setVisibility(View.GONE);
            recycleOrganizePages.setAdapter(organizeImagesAdapter);
            floatBtnSave.setVisibility(View.VISIBLE);
            new ItemTouchHelper(new SimpleCallback(15, 0) {
                public void onSwiped(ViewHolder viewHolder, int i) {
                }

                public boolean onMove(RecyclerView recyclerView, ViewHolder viewHolder, ViewHolder viewHolder2) {
                    int adapterPosition = viewHolder.getAdapterPosition();
                    int adapterPosition2 = viewHolder2.getAdapterPosition();
                    listImagePages.add(adapterPosition, (ImagePage) listImagePages.remove(adapterPosition2));
                    organizeImagesAdapter.notifyItemMoved(adapterPosition2, adapterPosition);
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
                    OrganizeImagesActivity organizeImagesActivity = OrganizeImagesActivity.this;
                    sb.append(organizeImagesActivity.getImageOrganizedPages(organizeImagesActivity.listImagePages).toString());
                    Log.d(str, sb.toString());
                }
            }).attachToRecyclerView(recycleOrganizePages);
        }
    }

    public class SavePhotosPdfOrganizedPagesAyn extends AsyncTask<Void, Integer, Void> {
        private String generatedPDFPath,newFileName;
        private List<Integer> organizedPages = new ArrayList<>();
        private int numPages = organizedPages.size();


        public SavePhotosPdfOrganizedPagesAyn(List<Integer> list, String str, ConstraintLayout constraintLayout) {
            organizedPages = list;
            numPages = organizedPages.size();
            newFileName = str;
            mProgressView = constraintLayout;
            initProgressView();
            Utils.setLightStatusBar(context);
            btnCancelProgress.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    SavePhotosPdfOrganizedPagesAyn.this.cancel(true);
                    OrganizeImagesActivity organizeImagesActivity = OrganizeImagesActivity.this;
                    organizeImagesActivity.closeProgressBar(organizeImagesActivity.context);          //changewbyown
                    closeProgressBar(organizeImagesActivity.context);
                }
            });
        }

        public void onPreExecute() {
            super.onPreExecute();
            progressDownloading.setMax(numPages);
            tvCurrentAction.setText(R.string.converting);
            mProgressView.setVisibility(View.VISIBLE);
        }

        public Void doInBackground(Void... voidArr) {
            boolean z = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(BrowsePDFActivity.GRID_VIEW_ENABLED, false);
            try {
                File file = new File(allPdfDocuments);
                StringBuilder sb = new StringBuilder();
                sb.append(allPdfDocuments);
                sb.append(newFileName);
                sb.append(".pdf");
                generatedPDFPath = sb.toString();
                if (!file.exists()) {
                    file.mkdirs();
                }
                PDFBoxResourceLoader.init(context);
                PDDocument pDDocument = new PDDocument();
                int i = 0;
                while (i < numPages && !isCancelled()) {
                    String path = ((ImagePage) listImagePages.get(i)).getImageUri().getPath();
                    Bitmap rotateBitmap = rotateImageBitmap(ImageUtils.getInstant().getPdfCompressedBitmap(path), new ExifInterface(path).getAttributeInt("Orientation", 0));
                    float width = (float) rotateBitmap.getWidth();
                    float height = (float) rotateBitmap.getHeight();
                    PDPage pDPage = new PDPage(new PDRectangle(width, height));
                    pDDocument.addPage(pDPage);
                    PDImageXObject createFromImage = JPEGFactory.createFromImage(pDDocument, rotateBitmap);
                    PDPageContentStream pDPageContentStream = new PDPageContentStream(pDDocument, pDPage, true, true, true);
                    pDPageContentStream.drawImage(createFromImage, 0.0f, 0.0f, width, height);
                    pDPageContentStream.close();
                    i++;
                    publishProgress(new Integer[]{Integer.valueOf(i)});
                }
                pDDocument.save(generatedPDFPath);
                pDDocument.close();
                if (z) {
                    Utils.generatePDFThumbnail(context, generatedPDFPath);
                }
                MediaScannerConnection.scanFile(context, new String[]{generatedPDFPath}, new String[]{"application/pdf"}, null);
                String str = TAG;
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Page order");
                sb2.append(organizedPages);
                Log.d(str, sb2.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            updateDownloadingProgressPercent(numArr[0].intValue(), numPages);
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            tvCurrentAction.setText(R.string.done);
            btnCancelProgress.setOnClickListener(null);
            OrganizeImagesActivity organizeImagesActivity = OrganizeImagesActivity.this;
            organizeImagesActivity.showInterstialAd(organizeImagesActivity.context, allPdfDocuments);
            OrganizeImagesActivity organizeImagesActivity2 = OrganizeImagesActivity.this;
            organizeImagesActivity2.OpenImagePathSet(organizeImagesActivity2.context, context.getString(R.string.open_file), generatedPDFPath, true);
        }
    }


    public void OpenImagePathSet(final Context context2, String str, final String str2, final boolean z) {
        btnOpenPdfFile.setText(str);
        btnOpenPdfFile.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (z) {
                    File file = new File(str2);
                    Intent intent = new Intent(context2, PDFViewerActivity.class);
                    intent.putExtra(BrowsePDFActivity.PDF_LOCATION, file.getAbsolutePath());
                    String str = TAG;
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

    public void showImagePdfFileNameDialog() {
        Builder builder = new Builder(context);
        StringBuilder sb = new StringBuilder();
        sb.append("Image_PDF_");
        sb.append(System.currentTimeMillis());
        String sb2 = sb.toString();
        float f = context.getResources().getDisplayMetrics().density;
        final EditText editText = new EditText(context);
        editText.setText(sb2);
        editText.setSelectAllOnFocus(true);
        builder.setTitle((int) R.string.enter_file_name).setPositiveButton((int) R.string.ok, (DialogInterface.OnClickListener) null).setNegativeButton((int) R.string.cancel, (DialogInterface.OnClickListener) null);
        final AlertDialog create = builder.create();
        int i = (int) (24.0f * f);
        create.setView(editText, i, (int) (8.0f * f), i, (int) (f * 5.0f));
        create.show();
        create.getButton(-1).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                String obj = editText.getText().toString();
                if (Utils.isFileNameValid(obj)) {
                    create.dismiss();
                    OrganizeImagesActivity organizeImagesActivity = OrganizeImagesActivity.this;
                    new SavePhotosPdfOrganizedPagesAyn(organizeImagesActivity.getImageOrganizedPages(organizeImagesActivity.listImagePages), obj, progressMain).execute(new Void[0]);
                    return;
                }
                editText.setError(getString(R.string.invalid_file_name));
            }
        });
    }

    public void showInterstialAd(final Context context2, final String str) {

        finishProcessBar(context2, str);         //changewbyown

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
                                    C22482 r0 = C22482.this;
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

}
