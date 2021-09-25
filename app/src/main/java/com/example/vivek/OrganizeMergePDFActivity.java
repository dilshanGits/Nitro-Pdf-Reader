package com.example.vivek;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.vivek.adapters.MergeOrganalPDFAdp;
import com.example.vivek.utils.PDFTools;
import com.example.vivek.utils.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OrganizeMergePDFActivity extends AppCompatActivity {
    public static String ORGANIZE_MERGE_PAGES_TIP = "prefs_organize_merge_pages";
    int REQUEST_CODE_ADD_FILE = 62;
    final String TAG = OrganizeMergePDFActivity.class.getSimpleName();
    private String allPdfDocuments,allPdfPictureDir;
    FloatingActionButton floatBtnSave;
    private ImageView imgTapClose;
    public Context context;
    public RelativeLayout rLayTapMoreOptions;
    public List<File> mPDFFiles = new ArrayList();
    public MergeOrganalPDFAdp organizePagesAdapter;
    private List<String> pdfFilePaths;
    public ProgressBar progressMergePdf;
    public ConstraintLayout progressMain;
    public RecyclerView recycleOrganizePages;
    public SharedPreferences sharedPreferences;
    boolean showOrganizePagesTip;



    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_organize_merge_pdf);

        String file = Environment.getExternalStorageDirectory().toString();
        StringBuilder sb = new StringBuilder();
        sb.append(file);
        sb.append("/Pictures/AllPdf/tmp/");
        this.allPdfPictureDir = sb.toString();
        StringBuilder sb2 = new StringBuilder();
        sb2.append(file);
        sb2.append("/Documents/AllPdf/");
        this.allPdfDocuments = sb2.toString();
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.context = this;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.showOrganizePagesTip = this.sharedPreferences.getBoolean(ORGANIZE_MERGE_PAGES_TIP, true);
        this.recycleOrganizePages = (RecyclerView) findViewById(R.id.recycleOrganizePages);
        this.progressMergePdf = (ProgressBar) findViewById(R.id.progressMergePdf);
        this.progressMain = (ConstraintLayout) findViewById(R.id.progressMain);
        this.floatBtnSave = (FloatingActionButton) findViewById(R.id.floatBtnSave);
        this.rLayTapMoreOptions = (RelativeLayout) findViewById(R.id.rLayTapMoreOptions);
        this.imgTapClose = (ImageView) findViewById(R.id.imgTapClose);
        this.imgTapClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                OrganizeMergePDFActivity.this.rLayTapMoreOptions.setVisibility(View.GONE);
                OrganizeMergePDFActivity.this.rLayTapMoreOptions.animate().translationY((float) (-OrganizeMergePDFActivity.this.rLayTapMoreOptions.getHeight())).alpha(0.0f).setListener(new AnimatorListener() {
                    public void onAnimationCancel(Animator animator) {
                    }

                    public void onAnimationRepeat(Animator animator) {
                    }

                    public void onAnimationStart(Animator animator) {
                    }

                    public void onAnimationEnd(Animator animator) {
                        OrganizeMergePDFActivity.this.rLayTapMoreOptions.setVisibility(View.GONE);
                        Editor edit = OrganizeMergePDFActivity.this.sharedPreferences.edit();
                        edit.putBoolean(OrganizeMergePDFActivity.ORGANIZE_MERGE_PAGES_TIP, false);
                        edit.apply();
                    }
                });
            }
        });
        this.pdfFilePaths = getIntent().getStringArrayListExtra(PDFToolsActivity.PDF_PATHS);
        if (this.showOrganizePagesTip) {
            this.rLayTapMoreOptions.setVisibility(View.VISIBLE);
        } else {
            this.rLayTapMoreOptions.setVisibility(View.GONE);
        }
        new LoadMergePdfThumbAyn().execute(new List[]{this.pdfFilePaths});
        this.floatBtnSave.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                OrganizeMergePDFActivity.this.organizePagesAdapter.finishActionMode();
                if (OrganizeMergePDFActivity.this.organizePagesAdapter.getPDFsToMerge().size() >= 2) {
                    OrganizeMergePDFActivity organizeMergePDFActivity = OrganizeMergePDFActivity.this;
                    organizeMergePDFActivity.getMergePDFFileList(organizeMergePDFActivity.organizePagesAdapter.getPDFsToMerge());
                    return;
                }
                Toast.makeText(OrganizeMergePDFActivity.this.context, R.string.at_least_two_files, 1).show();
            }
        });
    }

    public void onBackPressed() {
        if (this.progressMain.findViewById(R.id.imgCloseProgress).getVisibility() == View.VISIBLE) {
            closeDownloadingProgressBar(this.progressMain);
        } else if (this.progressMain.getVisibility() != View.VISIBLE) {
            super.onBackPressed();
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

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_organize_pages, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_add_file) {
            Intent intent = new Intent(this, SelectPDFActivity.class);
            intent.putExtra(PDFToolsActivity.MULTI_SELECTION, true);
            intent.putExtra(PDFToolsActivity.CALLING_ACTIVITY, OrganizeMergePDFActivity.class.getSimpleName());
            startActivityForResult(intent, this.REQUEST_CODE_ADD_FILE);
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (intent != null) {
            ArrayList stringArrayListExtra = intent.getStringArrayListExtra(PDFToolsActivity.PDF_PATHS);
            this.pdfFilePaths = stringArrayListExtra;
            new LoadMergePdfThumbAyn().execute(new List[]{stringArrayListExtra});
            return;
        }
        Toast.makeText(this, "An error occured", Toast.LENGTH_SHORT).show();
    }

    public class LoadMergePdfThumbAyn extends AsyncTask<List<String>, Void, Void> {

        public void onPreExecute() {
            super.onPreExecute();
        }

        public Void doInBackground(List<String>... listArr) {
            List<String> list = listArr[0];
            int size = list.size();
            for (int i = 0; i < size; i++) {
                String str = (String) list.get(i);
                if (!Utils.isThumbnailPresent(OrganizeMergePDFActivity.this.context, str)) {
                    Utils.generatePDFThumbnail(OrganizeMergePDFActivity.this.context, str);
                }
                OrganizeMergePDFActivity.this.mPDFFiles.add(new File(str));
            }
            return null;
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            OrganizeMergePDFActivity organizeMergePDFActivity = OrganizeMergePDFActivity.this;
            organizeMergePDFActivity.organizePagesAdapter = new MergeOrganalPDFAdp(organizeMergePDFActivity.context, OrganizeMergePDFActivity.this.mPDFFiles);
            OrganizeMergePDFActivity.this.recycleOrganizePages.setLayoutManager(new GridLayoutManager(OrganizeMergePDFActivity.this.context, Utils.isTablet(OrganizeMergePDFActivity.this.context) ? 6 : 3, 1, false));
            OrganizeMergePDFActivity.this.progressMergePdf.setVisibility(View.GONE);
            OrganizeMergePDFActivity.this.recycleOrganizePages.setAdapter(OrganizeMergePDFActivity.this.organizePagesAdapter);
            OrganizeMergePDFActivity.this.floatBtnSave.setVisibility(View.VISIBLE);
            new ItemTouchHelper(new SimpleCallback(15, 0) {
                public void onSwiped(ViewHolder viewHolder, int i) {
                }

                public boolean onMove(RecyclerView recyclerView, ViewHolder viewHolder, ViewHolder viewHolder2) {
                    int adapterPosition = viewHolder.getAdapterPosition();
                    int adapterPosition2 = viewHolder2.getAdapterPosition();
                    OrganizeMergePDFActivity.this.mPDFFiles.add(adapterPosition, (File) OrganizeMergePDFActivity.this.mPDFFiles.remove(adapterPosition2));
                    OrganizeMergePDFActivity.this.organizePagesAdapter.notifyItemMoved(adapterPosition2, adapterPosition);
                    String str = OrganizeMergePDFActivity.this.TAG;
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
                }
            }).attachToRecyclerView(OrganizeMergePDFActivity.this.recycleOrganizePages);
        }
    }

    public void getMergePDFFileList(List<File> list) {
        final ArrayList arrayList = new ArrayList();
        for (File absolutePath : list) {
            arrayList.add(absolutePath.getAbsolutePath());
        }
        Builder builder = new Builder(this.context);
        StringBuilder sb = new StringBuilder();
        sb.append("Merged");
        sb.append(System.currentTimeMillis());
        String sb2 = sb.toString();
        float f = this.context.getResources().getDisplayMetrics().density;
        final EditText editText = new EditText(this.context);
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
                    PDFTools pDFTools = new PDFTools();

                    PDFTools.MergePDFFiles mergePDFFiles = new PDFTools.MergePDFFiles(OrganizeMergePDFActivity.this.context, arrayList, obj, OrganizeMergePDFActivity.this.progressMain);
                    mergePDFFiles.execute(new Void[0]);


                    return;
                }
                editText.setError(OrganizeMergePDFActivity.this.getString(R.string.invalid_file_name));
            }
        });
    }

    public void closeDownloadingProgressBar(View view) {
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
}
