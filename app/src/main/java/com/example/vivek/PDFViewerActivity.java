package com.example.vivek;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;

import androidx.core.content.FileProvider;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.ActionMenuView.OnMenuItemClickListener;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.util.Constants;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.example.vivek.data.DbHelper;
import com.example.vivek.fragments.SettingsFragment;
import com.example.vivek.fragments.TableContentsFragment;
import com.example.vivek.utils.Utils;
import com.shockwave.pdfium.PdfPasswordException;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException;
import java.io.File;

public class PDFViewerActivity extends AppCompatActivity implements OnMenuItemClickListener {
    static final String CONTENTS_PDF_PATH = "com.example.pdfreader.CONTENTS_PDF_PATH";
    static final String PAGE_NUMBER = "com.pdftools.pdfreader.pdfviewer.PAGE_NUMBER";
    final String TAG = PDFViewerActivity.class.getSimpleName();
    ActionMenuView bottomMenuAction;
    Toolbar toolbar, toolbarBottom;
    LinearLayout layBottomMenuBar;
    int colorPrimaryDark, colorPrimaryDarkNight, flags, pageNumber;
    Context context;
    DbHelper dbHelper;
    View divider,view;
    FitPolicy fitPolicy;
    ActionBar mActionBar;
    private final Handler mHideHandler = new Handler();
    private Menu mMenu;
    String mPassword = "", pdfFileLocation, filePath;
    ProgressBar progressOpenPdf;
    public TextView tvPdfPageNumbers;
    PDFView pdfView;
    SharedPreferences sharedPreferences;
    private boolean rememberLastPage, showRemoveAds, stayAwake,mVisible,AUTO_HIDE;
    boolean swipeHorizontalEnabled,nightModeEnabled;
    private Menu topMenu;
    Uri uri;
    private final Runnable mHideRunnable = new Runnable() {
        public void run() {
            PDFViewerActivity.this.hide();
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        public void run() {
            ActionBar supportActionBar = PDFViewerActivity.this.getSupportActionBar();
            if (supportActionBar != null) {
                supportActionBar.show();
            }
            PDFViewerActivity.this.layBottomMenuBar.setVisibility(View.VISIBLE);
        }
    };
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint({"InlinedApi"})
        public void run() {
            PDFViewerActivity.this.pdfView.setSystemUiVisibility(4615);
        }
    };

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_pdf_viewer);

        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.toolbarBottom = (Toolbar) findViewById(R.id.toolbarBottom);
        this.progressOpenPdf = (ProgressBar) findViewById(R.id.progressOpenPdf);
        this.tvPdfPageNumbers = (TextView) findViewById(R.id.tvPdfPageNumbers);
        this.pdfView = (PDFView) findViewById(R.id.pdfView);
        this.bottomMenuAction = (ActionMenuView) findViewById(R.id.bottomMenuAction);
        this.divider = findViewById(R.id.divider);
        this.layBottomMenuBar = (LinearLayout) findViewById(R.id.layBottomMenuBar);
        this.bottomMenuAction.setOnMenuItemClickListener(this);
        this.context = this;
        setSupportActionBar(this.toolbar);
        this.mActionBar = getSupportActionBar();
        this.mActionBar.setDisplayHomeAsUpEnabled(true);
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.stayAwake = this.sharedPreferences.getBoolean(SettingsFragment.KEY_PREFS_STAY_AWAKE, true);
        this.rememberLastPage = this.sharedPreferences.getBoolean(SettingsFragment.KEY_PREFS_REMEMBER_LAST_PAGE, true);
        int i = 0;
        this.AUTO_HIDE = this.sharedPreferences.getBoolean("prefs_auto_full_screen", false);
        this.swipeHorizontalEnabled = this.sharedPreferences.getBoolean("prefs_swipe_horizontal_enabled", false);
        this.nightModeEnabled = this.sharedPreferences.getBoolean("prefs_night_mode_enabled", false);
        this.view = ((Activity) this.context).getWindow().getDecorView();
        this.flags = this.view.getSystemUiVisibility();
        this.colorPrimaryDark = this.context.getResources().getColor(R.color.colorPrimaryDark);
        this.colorPrimaryDarkNight = this.context.getResources().getColor(R.color.colorPrimaryDarkNight);
        Constants.THUMBNAIL_RATIO = 0.7f;
        Intent intent = getIntent();
        this.pdfFileLocation = intent.getStringExtra(BrowsePDFActivity.PDF_LOCATION);
        this.showRemoveAds = intent.getBooleanExtra(BrowsePDFActivity.SHOW_REMOVE_ADS, false);
        this.uri = intent.getData();
        this.dbHelper = DbHelper.getInstance(this);
        this.pdfView.setKeepScreenOn(this.stayAwake);
        if (this.rememberLastPage) {
            i = this.dbHelper.getLastOpenedPage(this.pdfFileLocation);
        }
        this.pageNumber = i;
        this.fitPolicy = (Utils.isTablet(this) || this.swipeHorizontalEnabled) ? FitPolicy.HEIGHT : FitPolicy.WIDTH;
        loadSelectedPdfFile(this.mPassword, this.pageNumber, this.swipeHorizontalEnabled, this.nightModeEnabled, this.fitPolicy);
        this.pdfView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });
//        setShowRemoveAds();
    }

    public void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
        if (this.showRemoveAds) {
            delayedHide(9000);
        } else {
            delayedHide(6000);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_pdf_viewer, menu);
        this.topMenu = menu;
        this.mMenu = this.bottomMenuAction.getMenu();
        getMenuInflater().inflate(R.menu.activity_pdf_viewer_bottom, this.mMenu);
        MenuItem itemBookmarkView = this.mMenu.findItem(R.id.itemBookmarkView);
        MenuItem itemThemeNightMode = this.mMenu.findItem(R.id.itemThemeNightMode);
        setupPdfListSwipeIcons(itemBookmarkView, this.swipeHorizontalEnabled);
        setNightModeThemeIcons(itemThemeNightMode, this.nightModeEnabled);
        return true;
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 7 && i2 == -1) {
            this.pdfView.jumpTo(intent.getIntExtra(PAGE_NUMBER, this.pdfView.getCurrentPage()) - 1, true);
        }
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId != R.id.itemPrint) {
            switch (itemId) {
                case R.id.itemShare:
                    sharePdf();
                    break;
                case R.id.itemSharePicture:
                    sharePdfAsPicture();
                    break;
            }
        } else {
            printPdf();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void onDestroy() {
        this.sharedPreferences.edit().putInt(TableContentsFragment.SAVED_STATE, 0).apply();
        if (this.rememberLastPage && !TextUtils.isEmpty(this.pdfFileLocation)) {
            this.dbHelper.addLastOpenedPage(this.filePath, this.pdfView.getCurrentPage());
        }
        super.onDestroy();
    }

    public void loadSelectedPdfFile(String str, int i, boolean z, boolean z2, FitPolicy fitPolicy2) {
        Uri uri2 = this.uri;
        if (uri2 != null) {
            try {
                this.filePath = uri2.getPath();
                this.mActionBar.setTitle((CharSequence) new File(this.filePath).getName());
            } catch (Exception e) {
                this.mActionBar.setTitle((CharSequence) "View PDF");
                e.printStackTrace();
            }
            this.pdfView.fromUri(this.uri).password(str).enableAnnotationRendering(true).pageFitPolicy(fitPolicy2).spacing(6).defaultPage(i).swipeHorizontal(z).autoSpacing(z).pageFling(z).pageSnap(z)/*.setNightMode(z2)*/.onPageChange(this.onPageChangeListener).onLoad(this.onLoadCompleteListener).onError(this.onErrorListener).load();
        } else if (!TextUtils.isEmpty(this.pdfFileLocation)) {
            String str2 = this.pdfFileLocation;
            this.filePath = str2;
            File file = new File(str2);
            String str3 = this.TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("path from selection ");
            sb.append(file.getPath());
            Log.d(str3, sb.toString());
            this.mActionBar.setTitle((CharSequence) file.getName());
            this.pdfView.fromFile(file).password(str).enableAnnotationRendering(true).pageFitPolicy(fitPolicy2).spacing(6).defaultPage(i).swipeHorizontal(z).autoSpacing(z).pageFling(z).pageSnap(z)/*.setNightMode(z2)*/.onPageChange(this.onPageChangeListener).onLoad(this.onLoadCompleteListener).onError(this.onErrorListener).load();
            this.dbHelper.addRecentPDF(file.getAbsolutePath());
        }
    }


    OnErrorListener onErrorListener = new OnErrorListener() {
        public void onError(Throwable th) {
            if (th instanceof PdfPasswordException) {
                enterPasswordDialog();
                return;
            }
            Toast.makeText(PDFViewerActivity.this, th.getMessage(), Toast.LENGTH_LONG).show();
            progressOpenPdf.setVisibility(View.GONE);
        }
    };
    OnLoadCompleteListener onLoadCompleteListener = new OnLoadCompleteListener() {
        public void loadComplete(int i) {
            progressOpenPdf.setVisibility(View.GONE);
            tvPdfPageNumbers.setVisibility(View.VISIBLE);
        }
    };
    OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {
        public void onPageChanged(int i, int i2) {
            StringBuilder sb = new StringBuilder();
            sb.append(i + 1);
            sb.append(" / ");
            sb.append(i2);
            tvPdfPageNumbers.setText(sb.toString());
        }
    };

    public String getName(Uri uri2) {
        Cursor query = getContentResolver().query(uri2, null, null, null, null);
        if (query == null) {
//            return AnalyticsEvents.PARAMETER_DIALOG_OUTCOME_VALUE_UNKNOWN;
        }
        int columnIndex = query.getColumnIndex("_display_name");
        query.moveToFirst();
        String string = query.getString(columnIndex);
        query.getColumnNames();
        query.close();
        return string;
    }

    public void showShareAsPicture(Uri uri2) {
        Intent intent = new Intent(this, ShareAsPictureActivity.class);
        intent.putExtra("com.example.pdfreader.PDF_PATH", uri2.toString());
        startActivity(intent);
    }


    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.itemBookmark /*2131296272*/:
                addPageBookmark(this, this.filePath, this.pdfView.getCurrentPage() + 1);
                break;
            case R.id.itemPdfContents /*2131296280*/:
                showPdfContents(this.filePath);
                break;
            case R.id.itemPageToJump /*2131296292*/:
                jumpToPageOfPdf();
                break;
            case R.id.itemPdfToolsSetting /*2131296304*/:
                showPdfTools();
                break;
            case R.id.itemThemeNightMode /*2131296326*/:
                changeThemeNightMode(menuItem);
                break;
            case R.id.itemBookmarkView /*2131296327*/:
                bookMarkPDFView(menuItem);
                break;
        }
        return false;
    }

    public void bookMarkPDFView(MenuItem menuItem) {
        this.swipeHorizontalEnabled = this.sharedPreferences.getBoolean("prefs_swipe_horizontal_enabled", false);
        boolean z = this.sharedPreferences.getBoolean("prefs_night_mode_enabled", false);
        Editor edit = this.sharedPreferences.edit();
        setupPdfListSwipeIcons(menuItem, !this.swipeHorizontalEnabled);
        if (this.swipeHorizontalEnabled) {
            loadSelectedPdfFile(this.mPassword, this.pdfView.getCurrentPage(), !this.swipeHorizontalEnabled, z, FitPolicy.WIDTH);
            edit.putBoolean("prefs_swipe_horizontal_enabled", !this.swipeHorizontalEnabled).apply();
            Toast.makeText(this.context, "Vertical swipe enabled", Toast.LENGTH_SHORT).show();
            return;
        }
        loadSelectedPdfFile(this.mPassword, this.pdfView.getCurrentPage(), !this.swipeHorizontalEnabled, z, FitPolicy.HEIGHT);
        edit.putBoolean("prefs_swipe_horizontal_enabled", !this.swipeHorizontalEnabled).apply();
        Toast.makeText(this.context, "Horizontal swipe enabled", Toast.LENGTH_SHORT).show();
    }

    public void addPageBookmark(Context context2, String str, int i) {
        Builder builder = new Builder(context2);
        EditText editText = new EditText(context2);
        editText.setHint(R.string.enter_title);
        float f = context2.getResources().getDisplayMetrics().density;
        Builder title = builder.setTitle(R.string.add_bookmark);
        final Context context3 = context2;
        final EditText editText2 = editText;
        final String str2 = str;
        final int i2 = i;
        DialogInterface.OnClickListener r1 = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                DbHelper.getInstance(context3).addBookmark(str2, TextUtils.isEmpty(editText2.getText().toString()) ? getString(R.string.bookmark) : editText2.getText().toString(), i2);
                Context context = context3;
                StringBuilder sb = new StringBuilder();
                sb.append(getString(R.string.page));
                sb.append(" ");
                sb.append(i2);
                sb.append(" ");
                sb.append(getString(R.string.bookmark_added));
                Toast.makeText(context, sb.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        title.setPositiveButton(R.string.ok, r1).setNegativeButton(R.string.cancel, null);
        AlertDialog create = builder.create();
        int i3 = (int) (24.0f * f);
        create.setView(editText, i3, (int) (8.0f * f), i3, (int) (f * 5.0f));
        create.show();
    }

    public void showPdfContents(String str) {
        Intent intent = new Intent(this, ContentsActivity.class);
        intent.putExtra(CONTENTS_PDF_PATH, str);
        startActivityForResult(intent, 7);
    }

    public void jumpToPageOfPdf() {
        float f = this.context.getResources().getDisplayMetrics().density;
        final EditText editText = new EditText(this.context);
        editText.setHint(R.string.enter_page_number);
        editText.setInputType(3);
        Builder builder = new Builder(this.context);
        builder.setTitle(R.string.jump_to_page).setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, null);
        final AlertDialog create = builder.create();
        int i = (int) (24.0f * f);
        create.setView(editText, i, (int) (8.0f * f), i, (int) (f * 5.0f));
        create.show();
        create.getButton(-1).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                String obj = editText.getText().toString();
                if (isValidPageNumber(obj)) {
                    create.dismiss();
                    pdfView.jumpTo(Integer.valueOf(obj).intValue() - 1, true);
                    return;
                }
                editText.setError(getString(R.string.invalid_page_number));
            }
        });
    }

    public void changeThemeNightMode(MenuItem menuItem) {
        this.nightModeEnabled = this.sharedPreferences.getBoolean("prefs_night_mode_enabled", false);
        setNightModeThemeIcons(menuItem, !this.nightModeEnabled);
        this.pdfView.setNightMode(!this.nightModeEnabled);
        this.pdfView.invalidate();
        this.sharedPreferences.edit().putBoolean("prefs_night_mode_enabled", !this.nightModeEnabled).apply();
        setupPdfListSwipeIcons(this.mMenu.findItem(R.id.itemBookmarkView), this.sharedPreferences.getBoolean("prefs_swipe_horizontal_enabled", false));
    }

    public void showPdfTools() {
        try {
            Uri fromFile = this.uri != null ? this.uri : Uri.fromFile(new File(this.filePath));
            Intent intent = new Intent(this, PDFToolsActivity.class);
            intent.putExtra(PDFToolsActivity.PRE_SELECTED_PDF_PATH, fromFile.toString());
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this.context, R.string.cannot_show_tools, Toast.LENGTH_LONG).show();
        }
    }

    public void setupPdfListSwipeIcons(MenuItem menuItem, boolean z) {
        boolean z2 = this.sharedPreferences.getBoolean("prefs_night_mode_enabled", false);
        if (z) {
            if (z2) {
                menuItem.setIcon(R.drawable.ic_action_swipe_vertical_night);
                return;
            }
            menuItem.setIcon(R.drawable.ic_action_swipe_vertical);
            menuItem.setTitle(R.string.swipe_vertical);
        } else if (z2) {
            menuItem.setIcon(R.drawable.ic_action_swipe_horizontal_night);
        } else {
            menuItem.setIcon(R.drawable.ic_action_swipe_horizontal);
            menuItem.setTitle(R.string.swipe_horizontal);
        }
    }

    public void setNightModeThemeIcons(MenuItem menuItem, boolean z) {
        Resources resources = this.context.getResources();
        if (z) {
            menuItem.setIcon(R.drawable.ic_action_light_mode_night);
            menuItem.setTitle(R.string.light_mode);
            this.toolbar.setBackgroundColor(resources.getColor(R.color.colorPrimaryNight));
            this.toolbar.setTitleTextColor(resources.getColor(R.color.colorTitleTextNight));
            this.toolbar.setNavigationIcon((int) R.drawable.ic_action_back_night);
            this.toolbarBottom.setBackgroundColor(resources.getColor(R.color.colorPrimaryNight));
            this.pdfView.setBackgroundColor(resources.getColor(R.color.colorPrimaryDarkNight));
            this.divider.setBackgroundColor(resources.getColor(R.color.colorPrimaryDarkNight));
            this.topMenu.findItem(R.id.itemShare).setIcon(R.drawable.ic_action_share_night);
            this.topMenu.findItem(R.id.itemSharePicture).setIcon(R.drawable.ic_action_share_as_picture_night);
            this.topMenu.findItem(R.id.itemPrint).setIcon(R.drawable.ic_action_print_night);
            this.mMenu.findItem(R.id.itemBookmark).setIcon(R.drawable.ic_action_bookmark_night);
            this.mMenu.findItem(R.id.itemPdfContents).setIcon(R.drawable.ic_action_contents_night);
            this.mMenu.findItem(R.id.itemPageToJump).setIcon(R.drawable.ic_action_jump_to_page_night);
            this.mMenu.findItem(R.id.itemPdfToolsSetting).setIcon(R.drawable.ic_action_pdf_tools_night);
            if (VERSION.SDK_INT >= 23) {
                this.flags &= -8193;
                this.view.setSystemUiVisibility(this.flags);
                ((Activity) this.context).getWindow().setStatusBarColor(this.colorPrimaryDarkNight);
                return;
            }
            return;
        }
        menuItem.setIcon(R.drawable.ic_action_night_mode);
        menuItem.setTitle(R.string.night_mode);
        this.toolbar.setBackgroundColor(-1);
        this.toolbar.setTitleTextColor(this.context.getResources().getColor(R.color.colorTitleTextLight));
        this.toolbar.setNavigationIcon((int) R.drawable.ic_action_back_light);
        this.toolbarBottom.setBackgroundColor(-1);
        this.pdfView.setBackgroundColor(this.context.getResources().getColor(R.color.colorPDFViewBg));
        this.divider.setBackgroundColor(resources.getColor(R.color.colorDividerLight));
        this.topMenu.findItem(R.id.itemShare).setIcon(R.drawable.ic_action_share);
        this.topMenu.findItem(R.id.itemSharePicture).setIcon(R.drawable.ic_action_share_as_picture);
        this.topMenu.findItem(R.id.itemPrint).setIcon(R.drawable.ic_action_print);
        this.mMenu.findItem(R.id.itemBookmark).setIcon(R.drawable.ic_action_bookmark);
        this.mMenu.findItem(R.id.itemPdfContents).setIcon(R.drawable.ic_action_contents);
        this.mMenu.findItem(R.id.itemPageToJump).setIcon(R.drawable.ic_action_jump_to_page);
        this.mMenu.findItem(R.id.itemPdfToolsSetting).setIcon(R.drawable.ic_action_pdf_tools);
        if (VERSION.SDK_INT >= 23) {
            this.flags |= 8192;
            this.view.setSystemUiVisibility(this.flags);
            ((Activity) this.context).getWindow().setStatusBarColor(this.colorPrimaryDark);
        }
    }

    public void enterPasswordDialog() {
        float f = this.context.getResources().getDisplayMetrics().density;
        final EditText editText = new EditText(this.context);
        editText.setHint(R.string.enter_password);
        editText.setInputType(129);
        Builder builder = new Builder(this.context);
        builder.setTitle(R.string.password_protected).setPositiveButton(R.string.ok, null).setCancelable(false).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        final AlertDialog create = builder.create();
        int i = (int) (24.0f * f);
        create.setView(editText, i, (int) (8.0f * f), i, (int) (f * 5.0f));
        create.show();
        create.getButton(-1).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                PDDocument pDDocument;
                mPassword = editText.getText().toString();
                if (!TextUtils.isEmpty(mPassword)) {
                    try {
                        String str = TAG;
                        StringBuilder sb = new StringBuilder();
                        sb.append("This is a path ");
                        sb.append(filePath);
                        Log.d(str, sb.toString());
                        if (uri != null) {
                            pDDocument = PDDocument.load(getContentResolver().openInputStream(uri), mPassword);
                        } else {
                            pDDocument = PDDocument.load(new File(filePath), mPassword);
                        }
                        pDDocument.close();
                        loadSelectedPdfFile(mPassword, pageNumber, swipeHorizontalEnabled, nightModeEnabled, fitPolicy);
                        create.dismiss();
                    } catch (Exception e) {
                        if (e instanceof InvalidPasswordException) {
                            editText.setError(context.getString(R.string.invalid_password));
                            Log.d(TAG, "Invalid Password");
                            return;
                        }
                        e.printStackTrace();
                    }
                } else {
                    editText.setError(context.getString(R.string.invalid_password));
                    Log.d(TAG, "Invalid Password");
                }
            }
        });
    }

    public boolean isValidPageNumber(String str) {
        boolean z = false;
        if (TextUtils.isEmpty(str) || !TextUtils.isDigitsOnly(str)) {
            return false;
        }
        int pageCount = this.pdfView.getPageCount();
        try {
            int intValue = Integer.valueOf(str).intValue();
            if (intValue > 0 && intValue <= pageCount) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void toggle() {
        if (this.mVisible) {
            hide();
            return;
        }
        show();
        if (this.AUTO_HIDE) {
            delayedHide(10000);
        }
    }

  /*  public void hide() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }
        this.layBottomMenuBar.setVisibility(View.GONE);
        this.mVisible = false;
        this.mHideHandler.removeCallbacks(new Runnable() {
            @Override
            public void run() {
                ActionBar supportActionBar = getSupportActionBar();
                if (supportActionBar != null) {
                    supportActionBar.show();
                }
                layBottomMenuBar.setVisibility(View.VISIBLE);
            }
        });
        this.mHideHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pdfView.setSystemUiVisibility(4615);
            }
        }, 1);
    }

    @SuppressLint({"InlinedApi"})
    private void showPdfViwer() {
        this.pdfView.setSystemUiVisibility(1536);
        this.mVisible = true;
        this.mHideHandler.removeCallbacks(new Runnable() {
            @Override
            public void run() {
                pdfView.setSystemUiVisibility(4615);
            }
        });
        this.mHideHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pdfView.setSystemUiVisibility(4615);
            }
        }, 1);
    }

    private void delayedHide(int i) {
        this.mHideHandler.removeCallbacks(new Runnable() {
            @Override
            public void run() {
                hide();
            }
        });
        this.mHideHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hide();
            }
        }, (long) i);
    }*/




    public void hide() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }
        this.layBottomMenuBar.setVisibility(View.GONE);
        this.mVisible = false;
        this.mHideHandler.removeCallbacks(this.mShowPart2Runnable);
        this.mHideHandler.postDelayed(this.mHidePart2Runnable, 1);
    }

    @SuppressLint({"InlinedApi"})
    private void show() {
        this.pdfView.setSystemUiVisibility(1536);
        this.mVisible = true;
        this.mHideHandler.removeCallbacks(this.mHidePart2Runnable);
        this.mHideHandler.postDelayed(this.mShowPart2Runnable, 1);
    }

    private void delayedHide(int i) {
        this.mHideHandler.removeCallbacks(this.mHideRunnable);
        this.mHideHandler.postDelayed(this.mHideRunnable, (long) i);
    }


/*    public void setShowRemoveAds() {
        if (this.showRemoveAds) {
            Snackbar.make((View) this.toolbarBottom, (int) R.string.dont_like_ads, 4000).setAction((int) R.string.remove, (OnClickListener) new OnClickListener() {
                public void onClick(View view) {
                    Utils.openProVersionPlayStore(getApplicationContext());
                }
            }).showPdfViwer();
        }
    }*/

    private void printPdf() {
        Uri uri2 = this.uri;
        if (uri2 != null) {
            Utils.printPdfFile(this, uri2);
        } else {
            Utils.printPdfFile(this, Uri.fromFile(new File(this.filePath)));
        }
    }

    private void sharePdf() {
        Uri uri2 = this.uri;
        if (uri2 != null) {
            Utils.sharePdfFile(this, uri2);
            return;
        }
        try {
            Utils.sharePdfFile(this, FileProvider.getUriForFile(this.context, Utils.FILE_AUTHORITY, new File(this.filePath)));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this.context, R.string.cant_share_file, Toast.LENGTH_LONG).show();
        }
    }

    private void sharePdfAsPicture() {
        Uri uri2 = this.uri;
        if (uri2 != null) {
            showShareAsPicture(uri2);
            return;
        }
        try {
            showShareAsPicture(Uri.fromFile(new File(this.filePath)));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this.context, R.string.cant_share_file, Toast.LENGTH_LONG).show();
        }
    }
}
