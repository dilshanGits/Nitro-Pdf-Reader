package com.example.vivek.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import com.itextpdf.text.html.HtmlTags;
import com.itextpdf.text.xml.xmp.PdfSchema;
import com.example.vivek.DataUpdatedEvent.RecentPdfClearEvent;
import com.example.vivek.DataUpdatedEvent.RecentPdfDeleteEvent;
import com.example.vivek.R;
import com.example.vivek.data.DbContract.BookmarkEntry;
import com.example.vivek.data.DbContract.LastOpenedPageEntry;
import com.example.vivek.data.DbContract.RecentPDFEntry;
import com.example.vivek.data.DbContract.StarredPDFEntry;
import com.example.vivek.models.BookmarkData;
import com.example.vivek.models.PdfDataType;
import com.example.vivek.utils.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;

public class DbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "pdf_history.db";
    private static final int DATABASE_VERSION = 2;
    public static final String SORT_BY = "prefs_sort_by";
    public static final String SORT_ORDER = "prefs_sort_order";
    private static DbHelper sInstance;
    private final String SQL_CREATE_BOOKMARK = "CREATE TABLE IF NOT EXISTS bookmarks ( _id INTEGER PRIMARY KEY AUTOINCREMENT, tvMenuToolTitle TEXT, path TEXT, page_number INTEGER UNIQUE, created_at DATETIME DEFAULT (DATETIME('now','localtime')))";
    private final String SQL_CREATE_HISTORY_PDFS_TABLE = "CREATE TABLE IF NOT EXISTS history_pdfs ( _id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT UNIQUE, last_accessed_at DATETIME DEFAULT (DATETIME('now','localtime')))";
    private final String SQL_CREATE_LAST_OPENED_PAGE = "CREATE TABLE IF NOT EXISTS last_opened_page ( _id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT UNIQUE, page_number INTEGER)";
    private final String SQL_CREATE_STARED_PDFS_TABLE = "CREATE TABLE IF NOT EXISTS stared_pdfs ( _id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT UNIQUE, created_at DATETIME DEFAULT (DATETIME('now','localtime')))";
    private final String TAG = DbHelper.class.getSimpleName();
    private String THUMBNAILS_DIR;
    public Context context;
    private SQLiteDatabase mDatabase;
    private int mOpenCounter;

    public DbHelper(Context context2) {
        super(context2, DATABASE_NAME, null, 2);
        this.context = context2;
        StringBuilder sb = new StringBuilder();
        sb.append(context2.getCacheDir());
        sb.append("/Thumbnails/");
        this.THUMBNAILS_DIR = sb.toString();
    }

    public static synchronized DbHelper getInstance(Context context2) {
        DbHelper dbHelper;
        synchronized (DbHelper.class) {
            if (sInstance == null) {
                sInstance = new DbHelper(context2.getApplicationContext());
            }
            dbHelper = sInstance;
        }
        return dbHelper;
    }

    public synchronized SQLiteDatabase getReadableDb() {
        this.mOpenCounter++;
        if (this.mOpenCounter == 1) {
            this.mDatabase = getWritableDatabase();
        }
        return this.mDatabase;
    }

    public synchronized void closeDb() {
        this.mOpenCounter--;
        if (this.mOpenCounter == 0) {
            this.mDatabase.close();
        }
    }

    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS history_pdfs ( _id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT UNIQUE, last_accessed_at DATETIME DEFAULT (DATETIME('now','localtime')))");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS stared_pdfs ( _id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT UNIQUE, created_at DATETIME DEFAULT (DATETIME('now','localtime')))");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS last_opened_page ( _id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT UNIQUE, page_number INTEGER)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS bookmarks ( _id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, path TEXT, page_number INTEGER UNIQUE, created_at DATETIME DEFAULT (DATETIME('now','localtime')))");
    }

    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        if (i == 1) {
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS last_opened_page ( _id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT UNIQUE, page_number INTEGER)");
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS bookmarks ( _id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, path TEXT, page_number INTEGER UNIQUE, created_at DATETIME DEFAULT (DATETIME('now','localtime')))");
        }
    }

    public List<PdfDataType> getAllPdfFromDirectory(String str) {
        String str2;
        String string = PreferenceManager.getDefaultSharedPreferences(this.context).getString(SORT_BY, "name");
        ArrayList arrayList = new ArrayList();
        try {
            ContentResolver contentResolver = this.context.getContentResolver();
            Uri contentUri = Files.getContentUri("external");
            char c = 65535;
            int hashCode = string.hashCode();
            if (hashCode != 3373707) {
                if (hashCode != 3530753) {
                    if (hashCode == 1375123195) {
                        if (string.equals("date modified")) {
                            c = 0;
                        }
                    }
                } else if (string.equals(HtmlTags.SIZE)) {
                    c = 1;
                }
            } else if (string.equals("name")) {
                c = 2;
            }
            switch (c) {
                case 0:
                    str2 = "date_modified  COLLATE NOCASE ASC";
                    break;
                case 1:
                    str2 = "_size  COLLATE NOCASE ASC";
                    break;
                default:
                    str2 = "tvMenuToolTitle  COLLATE NOCASE ASC";
                    break;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("%");
            sb.append(str);
            sb.append("%");
            Cursor query = contentResolver.query(contentUri, new String[]{"_data"}, "mime_type=? AND _data LIKE ?", new String[]{MimeTypeMap.getSingleton().getMimeTypeFromExtension(PdfSchema.DEFAULT_XPATH_ID), sb.toString()}, str2);
            if (query != null && query.moveToFirst()) {
                do {
                    File file = new File(query.getString(query.getColumnIndex("_data")));
                    if (file.length() != 0) {
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(this.THUMBNAILS_DIR);
                        sb2.append(Utils.removePdfExtension(file.getName()));
                        sb2.append(".jpg");
                        Uri imageUriFromPath = Utils.getImageUriFromPath(sb2.toString());
                        PdfDataType pdfDataType = new PdfDataType();
                        pdfDataType.setName(file.getName());
                        pdfDataType.setAbsolutePath(file.getAbsolutePath());
                        pdfDataType.setPdfUri(Uri.fromFile(file));
                        pdfDataType.setLength(Long.valueOf(file.length()));
                        pdfDataType.setLastModified(Long.valueOf(file.lastModified()));
                        pdfDataType.setThumbUri(imageUriFromPath);
                        pdfDataType.setStarred(isStared(file.getAbsolutePath()));
                        arrayList.add(pdfDataType);
                    }
                } while (query.moveToNext());
                query.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String str3 = this.TAG;
        StringBuilder sb3 = new StringBuilder();
        sb3.append("no of files in db ");
        sb3.append(arrayList.size());
        Log.d(str3, sb3.toString());
        return arrayList;
    }

    public List<PdfDataType> getAllPdfs() {
        String str;
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        String string = defaultSharedPreferences.getString(SORT_BY, "name");
        String string2 = defaultSharedPreferences.getString(SORT_ORDER, "ascending");
        ArrayList arrayList = new ArrayList();
        ContentResolver contentResolver = this.context.getContentResolver();
        Uri contentUri = Files.getContentUri("external");
        String str2 = TextUtils.equals(string2, "descending") ? "DESC" : "ASC";
        char c = 65535;
        int hashCode = string.hashCode();
        if (hashCode != 3373707) {
            if (hashCode != 3530753) {
                if (hashCode == 1375123195 && string.equals("date modified")) {
                    c = 0;
                }
            } else if (string.equals(HtmlTags.SIZE)) {
                c = 1;
            }
        } else if (string.equals("name")) {
            c = 2;
        }
        switch (c) {
            case 0:
                StringBuilder sb = new StringBuilder();
                sb.append("date_modified  COLLATE NOCASE ");
                sb.append(str2);
                str = sb.toString();
                break;
            case 1:
                StringBuilder sb2 = new StringBuilder();
                sb2.append("_size  COLLATE NOCASE ");
                sb2.append(str2);
                str = sb2.toString();
                break;
            default:
                StringBuilder sb3 = new StringBuilder();
//                sb3.append("tvMenuToolTitle  COLLATE NOCASE ");
                sb3.append("title  COLLATE NOCASE ");
                sb3.append(str2);
                str = sb3.toString();
                break;
        }
        try {
            Cursor query = contentResolver.query(contentUri, new String[]{"_data"}, "mime_type=?", new String[]{MimeTypeMap.getSingleton().getMimeTypeFromExtension(PdfSchema.DEFAULT_XPATH_ID)}, str);
            if (query != null && query.moveToFirst()) {
                do {
                    File file = new File(query.getString(query.getColumnIndex("_data")));
                    if (file.length() != 0) {
                        StringBuilder sb4 = new StringBuilder();
                        sb4.append(this.THUMBNAILS_DIR);
                        sb4.append(Utils.removePdfExtension(file.getName()));
                        sb4.append(".jpg");
                        Uri imageUriFromPath = Utils.getImageUriFromPath(sb4.toString());
                        PdfDataType pdfDataType = new PdfDataType();
                        pdfDataType.setName(file.getName());
                        pdfDataType.setAbsolutePath(file.getAbsolutePath());
                        pdfDataType.setPdfUri(Uri.fromFile(file));
                        pdfDataType.setLength(Long.valueOf(file.length()));
                        pdfDataType.setLastModified(Long.valueOf(file.lastModified()));
                        pdfDataType.setThumbUri(imageUriFromPath);
                        pdfDataType.setStarred(isStared(file.getAbsolutePath()));
                        arrayList.add(pdfDataType);
                    }
                } while (query.moveToNext());
                query.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public void addRecentPDF(String str) {
        SQLiteDatabase readableDb = getReadableDb();
        ContentValues contentValues = new ContentValues();
        contentValues.put("path", str);
        readableDb.replace(RecentPDFEntry.TABLE_NAME, null, contentValues);
        closeDb();
    }

    public List<PdfDataType> getRecentPDFs() {
        ArrayList arrayList = new ArrayList();
        SQLiteDatabase readableDb = getReadableDb();
        Cursor rawQuery = readableDb.rawQuery("SELECT * FROM history_pdfs ORDER BY last_accessed_at DESC", null);
        if (rawQuery.moveToFirst()) {
            do {
                String string = rawQuery.getString(rawQuery.getColumnIndex("path"));
                File file = new File(string);
                if (file.exists()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(this.THUMBNAILS_DIR);
                    sb.append(Utils.removePdfExtension(file.getName()));
                    sb.append(".jpg");
                    Uri imageUriFromPath = Utils.getImageUriFromPath(sb.toString());
                    PdfDataType pdfDataType = new PdfDataType();
                    pdfDataType.setName(file.getName());
                    pdfDataType.setAbsolutePath(file.getAbsolutePath());
                    pdfDataType.setPdfUri(Uri.fromFile(file));
                    pdfDataType.setLength(Long.valueOf(file.length()));
                    pdfDataType.setLastModified(Long.valueOf(file.lastModified()));
                    pdfDataType.setThumbUri(imageUriFromPath);
                    pdfDataType.setStarred(isStared(readableDb, file.getAbsolutePath()));
                    arrayList.add(pdfDataType);
                } else {
                    deleteRecentPDF(string);
                }
            } while (rawQuery.moveToNext());
        }
        rawQuery.close();
        closeDb();
        return arrayList;
    }

    public void deleteRecentPDF(String str) {
        getReadableDb().delete(RecentPDFEntry.TABLE_NAME, "path =?", new String[]{str});
        closeDb();
        EventBus.getDefault().post(new RecentPdfDeleteEvent());
    }

    public void updateHistory(String str, String str2) {
        try {
            SQLiteDatabase readableDb = getReadableDb();
            ContentValues contentValues = new ContentValues();
            contentValues.put("path", str2);
            readableDb.update(RecentPDFEntry.TABLE_NAME, contentValues, "path=?", new String[]{str});
            closeDb();
        } catch (Exception e) {
            Toast.makeText(this.context, R.string.failed, 1).show();
            e.printStackTrace();
        }
    }

    public void updateStarred(String str, String str2) {
        SQLiteDatabase readableDb = getReadableDb();
        ContentValues contentValues = new ContentValues();
        contentValues.put("path", str2);
        readableDb.update(StarredPDFEntry.TABLE_NAME, contentValues, "path=?", new String[]{str});
        closeDb();
    }

    public void clearRecentPDFs() {
        getReadableDb().delete(RecentPDFEntry.TABLE_NAME, null, null);
        closeDb();
        EventBus.getDefault().post(new RecentPdfClearEvent());
    }

    public List<PdfDataType> getStarredPdfs() {
        ArrayList arrayList = new ArrayList();
        Cursor rawQuery = getReadableDb().rawQuery("SELECT * FROM stared_pdfs ORDER BY created_at DESC", null);
        if (rawQuery.moveToFirst()) {
            do {
                String string = rawQuery.getString(rawQuery.getColumnIndex("path"));
                File file = new File(string);
                if (file.exists()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(this.THUMBNAILS_DIR);
                    sb.append(Utils.removePdfExtension(file.getName()));
                    sb.append(".jpg");
                    Uri imageUriFromPath = Utils.getImageUriFromPath(sb.toString());
                    PdfDataType pdfDataType = new PdfDataType();
                    pdfDataType.setName(file.getName());
                    pdfDataType.setAbsolutePath(file.getAbsolutePath());
                    pdfDataType.setPdfUri(Uri.fromFile(file));
                    pdfDataType.setLength(Long.valueOf(file.length()));
                    pdfDataType.setLastModified(Long.valueOf(file.lastModified()));
                    pdfDataType.setThumbUri(imageUriFromPath);
                    pdfDataType.setStarred(true);
                    arrayList.add(pdfDataType);
                } else {
                    removeStaredPDF(string);
                }
            } while (rawQuery.moveToNext());
        }
        rawQuery.close();
        closeDb();
        return arrayList;
    }

    public void addStaredPDF(String str) {
        SQLiteDatabase readableDb = getReadableDb();
        ContentValues contentValues = new ContentValues();
        contentValues.put("path", str);
        readableDb.replace(StarredPDFEntry.TABLE_NAME, null, contentValues);
        closeDb();
    }

    public void removeStaredPDF(String str) {
        getReadableDb().delete(StarredPDFEntry.TABLE_NAME, "path =?", new String[]{str});
        closeDb();
    }

    public void updateStaredPDF(String str, String str2) {
        try {
            SQLiteDatabase readableDb = getReadableDb();
            ContentValues contentValues = new ContentValues();
            contentValues.put("path", str2);
            readableDb.update(StarredPDFEntry.TABLE_NAME, contentValues, "path =?", new String[]{str});
            closeDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isStared(String str) {
        Cursor query = getReadableDb().query(StarredPDFEntry.TABLE_NAME, new String[]{"path"}, "path =?", new String[]{str}, null, null, null);
        Boolean valueOf = Boolean.valueOf(query.moveToFirst());
        query.close();
        closeDb();
        return valueOf.booleanValue();
    }

    public boolean isStared(SQLiteDatabase sQLiteDatabase, String str) {
        SQLiteDatabase sQLiteDatabase2 = sQLiteDatabase;
        Cursor query = sQLiteDatabase2.query(StarredPDFEntry.TABLE_NAME, new String[]{"path"}, "path =?", new String[]{str}, null, null, null);
        Boolean valueOf = Boolean.valueOf(query.moveToFirst());
        query.close();
        return valueOf.booleanValue();
    }

    public List<Uri> getAllImages(String str) {
        ArrayList arrayList = new ArrayList();
        try {
            Uri uri = Media.EXTERNAL_CONTENT_URI;
            StringBuilder sb = new StringBuilder();
            sb.append("%");
            sb.append(str);
            sb.append("%");
            Cursor query = this.context.getContentResolver().query(uri, null, "_data LIKE ? AND mime_type LIKE ? ", new String[]{sb.toString(), "%image/%"}, null);
            if (query != null && query.moveToFirst()) {
                do {
                    String string = query.getString(query.getColumnIndex("_data"));
                    Log.d(this.TAG, string);
                    arrayList.add(Uri.fromFile(new File(string)));
                } while (query.moveToNext());
                query.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public int getLastOpenedPage(String str) {
        int i;
        try {
            Cursor query = getReadableDb().query(LastOpenedPageEntry.TABLE_NAME, new String[]{"page_number"}, "path = ? ", new String[]{str}, null, null, null);
            if (query == null || !query.moveToFirst()) {
                i = 0;
                closeDb();
                return i;
            }
            i = query.getInt(query.getColumnIndex("page_number"));
            try {
                query.close();
            } catch (Exception e) {
                e = e;
            }
            closeDb();
            return i;
        } catch (Exception e2) {
            Exception e = e2;
            i = 0;
            e.printStackTrace();
            closeDb();
            return i;
        }
    }

    public void addLastOpenedPage(String str, int i) {
        try {
            SQLiteDatabase readableDb = getReadableDb();
            ContentValues contentValues = new ContentValues();
            contentValues.put("path", str);
            contentValues.put("page_number", Integer.valueOf(i));
            readableDb.replace(LastOpenedPageEntry.TABLE_NAME, null, contentValues);
            closeDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateLastOpenedPagePath(String str, String str2) {
        try {
            SQLiteDatabase readableDb = getReadableDb();
            ContentValues contentValues = new ContentValues();
            contentValues.put("path", str2);
            readableDb.update(LastOpenedPageEntry.TABLE_NAME, contentValues, "path =?", new String[]{str});
            closeDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addBookmark(String str, String str2, int i) {
        try {
            SQLiteDatabase readableDb = getReadableDb();
            ContentValues contentValues = new ContentValues();
            contentValues.put("path", str);
            contentValues.put("tvMenuToolTitle", str2);
            contentValues.put("page_number", Integer.valueOf(i));
            readableDb.replace(BookmarkEntry.TABLE_NAME, null, contentValues);
            closeDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<BookmarkData> getBookmarks(String str) {
        ArrayList arrayList = new ArrayList();
        try {
            Cursor query = getReadableDb().query(BookmarkEntry.TABLE_NAME, new String[]{"tvMenuToolTitle", "path", "page_number"}, "path = ? ", new String[]{str}, null, null, "created_at DESC");
            if (query != null && query.moveToFirst()) {
                do {
                    BookmarkData bookmarkData = new BookmarkData();
                    bookmarkData.setTitle(query.getString(query.getColumnIndex("tvMenuToolTitle")));
                    bookmarkData.setPageNumber(query.getInt(query.getColumnIndex("page_number")));
                    bookmarkData.setPath(query.getString(query.getColumnIndex("path")));
                    arrayList.add(bookmarkData);
                } while (query.moveToNext());
                query.close();
                closeDb();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public void updateBookmarkPath(String str, String str2) {
        try {
            SQLiteDatabase readableDb = getReadableDb();
            ContentValues contentValues = new ContentValues();
            contentValues.put("path", str2);
            readableDb.update(BookmarkEntry.TABLE_NAME, contentValues, "path =?", new String[]{str});
            closeDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteBookmarks(List<BookmarkData> list) {
        try {
            SQLiteDatabase readableDb = getReadableDb();
            int size = list.size();
            readableDb.beginTransaction();
            for (int i = 0; i < size; i++) {
                readableDb.delete(BookmarkEntry.TABLE_NAME, "path = ? AND page_number = ? ", new String[]{((BookmarkData) list.get(i)).getPath(), String.valueOf(((BookmarkData) list.get(i)).getPageNumber())});
            }
            readableDb.setTransactionSuccessful();
            readableDb.endTransaction();
            closeDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
