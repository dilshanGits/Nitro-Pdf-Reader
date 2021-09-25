package com.example.vivek;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Toast;

import com.example.vivek.DataUpdatedEvent.SortListEvent;
import com.example.vivek.DataUpdatedEvent.ToggleGridViewEvent;
import com.example.vivek.adapters.BrowsePdfPagerAdapter;
import com.example.vivek.adapters.DevicePdfsAdapter.OnPdfClickListener;
import com.example.vivek.adapters.RecentPdfsAdapter.OnHistoryPdfClickListener;
import com.example.vivek.data.DbHelper;
import com.example.vivek.fragments.RecentPdfFragment.OnRecentPdfClickListener;
import com.example.vivek.fragments.SettingsFragment;
import com.example.vivek.models.PdfDataType;
import com.example.vivek.p006ui.MaterialSearchView;
import com.example.vivek.p006ui.MaterialSearchView.OnQueryTextListener;
import com.example.vivek.utils.LocaleUtils;
import com.example.vivek.utils.Utils;
import com.example.vivek.utils.Utils.BackgroundGenerateThumbnails;

import java.io.File;

import org.greenrobot.eventbus.EventBus;

public class BrowsePDFActivity extends AppCompatActivity implements OnNavigationItemSelectedListener, OnRecentPdfClickListener, OnPdfClickListener, OnHistoryPdfClickListener, OnQueryTextListener {
    public static String GRID_VIEW_ENABLED = "prefs_grid_view_enabled";
    public static String GRID_VIEW_NUM_OF_COLUMNS = "prefs_grid_view_num_of_columns";
    public static final String PDF_LOCATION = "com.example.pdfreader.PDF_LOCATION";
    static final int PICK_PDF_REQUEST = 1;
    public static final String SHOW_REMOVE_ADS = "com.example.pdfreader.SHOW_REMOVE_ADS";
    public int CLICKS_TILL_AD_SHOW = 3;
    private final String TAG = BrowsePDFActivity.class.getSimpleName();
    public BrowsePdfPagerAdapter browsePdfPagerAdapter;
    String currLanguage;
    private DrawerLayout drawerLayBrowsePdf;
    private boolean gridViewEnabled;
    //    private InterstitialAd mInterstitialAd;
    private Menu menu;
    private TabLayout tabBrowsePdf;
    public ViewPager pagerBrowsePdf;
    MenuItem menuGridNumberSize, menulistOrGrid;
    public int pdfClick = 0;
    private MaterialSearchView searchBrowsePdf;
    private SharedPreferences sharedPreferences;
    private SubMenu menuPdfSortList;
    Toolbar toolbarBrowsePdf;
    AdView adview;


    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        LocaleUtils.setUpLanguage(this);
        setContentView(R.layout.activity_browse_pdf);


        findId();
        adview = (AdView) findViewById(R.id.adview);
        banner();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        gridViewEnabled = sharedPreferences.getBoolean(GRID_VIEW_ENABLED, false);
        currLanguage = sharedPreferences.getString(SettingsFragment.KEY_PREFS_LANGUAGE, "en");
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayBrowsePdf, toolbarBrowsePdf, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayBrowsePdf.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        ((NavigationView) findViewById(R.id.navigationSider)).setNavigationItemSelectedListener(this);
        searchBrowsePdf = (MaterialSearchView) findViewById(R.id.searchBarPdf);
        searchBrowsePdf.setOnQueryTextListener(this);
        if (gridViewEnabled) {
            new BackgroundGenerateThumbnails(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        }
        TabLayout tabLayout = tabBrowsePdf;
        tabLayout.addTab(tabLayout.newTab().setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_recent_tab, null)));
        TabLayout tabLayout2 = tabBrowsePdf;
        tabLayout2.addTab(tabLayout2.newTab().setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_phone_tab, null)));
        browsePdfPagerAdapter = new BrowsePdfPagerAdapter(getSupportFragmentManager());
        pagerBrowsePdf.setAdapter(browsePdfPagerAdapter);
        pagerBrowsePdf.addOnPageChangeListener(new TabLayoutOnPageChangeListener(tabBrowsePdf));
        tabBrowsePdf.addOnTabSelectedListener(new OnTabSelectedListener() {
            public void onTabReselected(Tab tab) {
            }

            public void onTabUnselected(Tab tab) {
            }

            public void onTabSelected(Tab tab) {
                pagerBrowsePdf.setCurrentItem(tab.getPosition());
            }
        });


    }

    private void findId() {

        toolbarBrowsePdf = (Toolbar) findViewById(R.id.toolbarBrowsePdf);
        tabBrowsePdf = (TabLayout) findViewById(R.id.tabBrowsePdf);
        drawerLayBrowsePdf = (DrawerLayout) findViewById(R.id.drawerLayBrowsePdf);
        pagerBrowsePdf = (ViewPager) findViewById(R.id.pagerBrowsePdf);
        setSupportActionBar(toolbarBrowsePdf);

    }


    public boolean onNavigationItemSelected(final MenuItem menuItem) {
        drawerLayBrowsePdf.closeDrawer((int) GravityCompat.START);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            public void run() {
                Context applicationContext = getApplicationContext();
                switch (menuItem.getItemId()) {
                    case R.id.menuAbout:
                        startActivity(new Intent(applicationContext, AboutActivity.class));
                        return;
                    case R.id.menuRate:
                        Utils.launchMarket(applicationContext);
                        return;
                    case R.id.menuSettings:
                        startActivity(new Intent(applicationContext, SettingsActivity.class));
                        return;
                    case R.id.menuShare:
                        Utils.startShareActivity(applicationContext);
                        return;
                    case R.id.menuBookMark:
                        startActivity(new Intent(applicationContext, StarredPDFActivity.class));
                        return;
                    case R.id.menuTools:
                        startActivity(new Intent(applicationContext, PDFToolsActivity.class));
                        return;
                    default:
                        return;
                }
            }
        }, 200);
        return true;
    }

    @SuppressLint({"RestrictedApi"})
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        this.menuPdfSortList = this.menu.findItem(R.id.menuPdfSortList).getSubMenu();
        this.menuPdfSortList.clearHeader();
        MenuCompat.setGroupDividerEnabled(menu, true);
        this.menulistOrGrid = menu.findItem(R.id.menulistOrGrid);
        this.menuGridNumberSize = menu.findItem(R.id.menuGridNumberSize);
//        checkDefaultMenuItem();               //changewbyown
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        if (this.gridViewEnabled) {
            this.menulistOrGrid.setVisible(true);
            this.menuGridNumberSize.setVisible(false);
        } else {
            this.menulistOrGrid.setVisible(false);
            this.menuGridNumberSize.setVisible(true);
        }
        return true;
    }


    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 1 && i2 == -1) {
            Uri data = intent.getData();
            if (data != null) {
                String path = data.getPath();
                if (path.contains(":")) {
                    path = path.split(":")[1];
                }
                openFilePdfViewer(new File(path).getAbsolutePath());
                return;
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menuSortByPdfModifiedDate:
                pdfSortByModifiedDate();
                break;
            case R.id.menuSortByPdfName:
                pdfSortByName();
                break;
            case R.id.menuSortByPdfSize:
                pdfSortBySize();
                break;
            case R.id.menuClearRecentPdfs:
                clearAllRecentPdf();
                break;
            case R.id.menuFiveColumns:
                setPdfInGridView(5);
                break;
            case R.id.menuFourColumns:
                setPdfInGridView(4);
                break;
            case R.id.menulistOrGrid:
                setPdfInListView();
                break;
            case R.id.menuSortPdfByAscending:
                pdfSortByAscendingOrder();
                break;
            case R.id.menuSortPdfByDescending:
                pdfSortByDescendingOrder();
                break;
            case R.id.menuSearchPdf:
                this.searchBrowsePdf.openPdfSearchData();
                break;
            case R.id.menuSixColumns:
                setPdfInGridView(6);
                break;
            case R.id.menuThreeColumns:
                setPdfInGridView(3);
                break;
            case R.id.menuTwoColumns:
                setPdfInGridView(2);
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void onPdfClicked(PdfDataType pdfDataType) {
        openFilePdfViewer(pdfDataType.getAbsolutePath());
    }

    public void onHistoryPdfClicked(PdfDataType pdfDataType) {
        openFilePdfViewer(pdfDataType.getAbsolutePath());
    }

    public void openFilePdfViewer(String str) {
        this.pdfClick++;
        final Intent intent = new Intent(this, PDFViewerActivity.class);
        intent.putExtra(PDF_LOCATION, str);
        StringBuilder sb = new StringBuilder();
        sb.append("PdfDataType location ");
        sb.append(str);

//        if (/*!this.mInterstitialAd.isLoaded() ||*/ this.pdfClick % this.CLICKS_TILL_AD_SHOW != 0) {
        startActivity(intent);
//            return;
//        }


    }

    public void openBrowsePDFFiles(View view) {
        startActivity(new Intent(this, FileBrowserActivity.class));
    }

    public void onResume() {
        super.onResume();
        changeAppLanguage();
        if (pdfClick == 16) {
            pdfClick = 0;
        }
    }


    public void clearAllRecentPdf() {
        DbHelper.getInstance(this).clearRecentPDFs();
        Toast.makeText(this, R.string.recent_cleared, Toast.LENGTH_SHORT).show();
    }

    public void setPdfInListView() {
        Editor edit = this.sharedPreferences.edit();
        edit.putBoolean(GRID_VIEW_ENABLED, false);
        edit.apply();
        EventBus.getDefault().post(new ToggleGridViewEvent());
        this.menulistOrGrid.setVisible(false);
        this.menuGridNumberSize.setVisible(true);
    }

    public void setPdfInGridView(int i) {
        new BackgroundGenerateThumbnails(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        Editor edit = this.sharedPreferences.edit();
        edit.putBoolean(GRID_VIEW_ENABLED, true);
        edit.putInt(GRID_VIEW_NUM_OF_COLUMNS, i);
        edit.apply();
        EventBus.getDefault().post(new ToggleGridViewEvent());
        this.menulistOrGrid.setVisible(true);
        this.menuGridNumberSize.setVisible(false);
    }



    public boolean onQueryTextChange(String str) {
        Log.d(this.TAG, str);
        return true;
    }

    public void pdfSortByName() {
        this.menuPdfSortList.findItem(R.id.menuSortByPdfName).setChecked(true);
        Editor edit = this.sharedPreferences.edit();
        edit.putString(DbHelper.SORT_BY, "name");
        edit.apply();
        EventBus.getDefault().post(new SortListEvent());
    }

    public void pdfSortByModifiedDate() {
        this.menuPdfSortList.findItem(R.id.menuSortByPdfModifiedDate).setChecked(true);
        Editor edit = this.sharedPreferences.edit();
        edit.putString(DbHelper.SORT_BY, "date modified");
        edit.apply();
        EventBus.getDefault().post(new SortListEvent());
    }

    public void pdfSortBySize() {
        this.menuPdfSortList.findItem(R.id.menuSortByPdfSize).setChecked(true);
        Editor edit = this.sharedPreferences.edit();
        edit.putString(DbHelper.SORT_BY, "size");
        edit.apply();
        EventBus.getDefault().post(new SortListEvent());
    }

    public void pdfSortByAscendingOrder() {
        this.menuPdfSortList.findItem(R.id.menuSortPdfByAscending).setChecked(true);
        Editor edit = this.sharedPreferences.edit();
        edit.putString(DbHelper.SORT_ORDER, "ascending");
        edit.apply();
        EventBus.getDefault().post(new SortListEvent());
    }

    public void pdfSortByDescendingOrder() {
        this.menuPdfSortList.findItem(R.id.menuSortPdfByDescending).setChecked(true);
        Editor edit = this.sharedPreferences.edit();
        edit.putString(DbHelper.SORT_ORDER, "descending");
        edit.apply();
        EventBus.getDefault().post(new SortListEvent());
    }

    public void changeAppLanguage() {
        if (!TextUtils.equals(this.currLanguage, this.sharedPreferences.getString(SettingsFragment.KEY_PREFS_LANGUAGE, "en"))) {
            recreate();
        }
    }

    public boolean onQueryTextSubmit(String str) {
        return false;
    }

    public void onRecentPdfClick(Uri uri) {
    }

    public void onBackPressed() {
        if (searchBrowsePdf.isSearchOpen()) {
            searchBrowsePdf.closeSearchingPdfData();
        } else if (drawerLayBrowsePdf.isDrawerOpen((int) GravityCompat.START)) {
            drawerLayBrowsePdf.closeDrawer((int) GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    public void banner() {
        AdRequest adRequest_banner = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
//                .addTestDevice("DC6E68F3F8A237F39235706823FCCA48").build();

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
