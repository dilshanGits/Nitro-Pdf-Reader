package com.example.vivek.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.preference.PreferenceManager;
import android.print.PrintManager;
import androidx.core.app.ShareCompat.IntentBuilder;
import androidx.print.PrintHelper;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.widget.AppCompatButton;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.example.vivek.R;
import com.example.vivek.adapters.PrintDocumentAdapter;
import com.example.vivek.data.DbHelper;
import com.example.vivek.models.PdfDataType;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfPasswordException;
import com.shockwave.pdfium.PdfiumCore;
import com.tom_roush.pdfbox.pdmodel.common.PDPageLabelRange;
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDWindowsLaunchParams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Utils {
    public static final String FILE_AUTHORITY = "com.example.pdfreader.fileprovider";

    public static class BackgroundGenerateThumbnails extends AsyncTask<Void, Void, Void> {
        private Context mContext;

        public BackgroundGenerateThumbnails(Context context) {
            this.mContext = context;
        }

        public Void doInBackground(Void... voidArr) {
            List allPdfs = DbHelper.getInstance(this.mContext).getAllPdfs();
            int size = allPdfs.size();
            for (int i = 0; i < size; i++) {
                String absolutePath = ((PdfDataType) allPdfs.get(i)).getAbsolutePath();
                if (!Utils.isThumbnailPresent(this.mContext, absolutePath)) {
                    Utils.generatePDFThumbnail(this.mContext, absolutePath);
                }
            }
            return null;
        }
    }

    public static boolean isTablet(Context context) {
        return context.getResources().getBoolean(R.bool.isTablet);
    }


    public static String formatDateToHumanReadable(Long l) {
        return new SimpleDateFormat("MMM dd yyyy", Locale.getDefault()).format(new Date(l.longValue()));
    }


    public static String formatMetadataDate(Context context, String str) {
        try {
            return new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).parse(str.split("\\+")[0].split(":")[1]));
        } catch (Exception e) {
            e.printStackTrace();
            return context.getString(R.string.unknown);
        }
    }


    public static String removePdfExtension(String str) {
        int lastIndexOf = str.lastIndexOf(System.getProperty("file.separator"));
        if (lastIndexOf != -1) {
            str = str.substring(lastIndexOf + 1);
        }
        int lastIndexOf2 = str.lastIndexOf(".");
        if (lastIndexOf2 == -1) {
            return str;
        }
        return str.substring(0, lastIndexOf2);
    }

    public static void sharePdfFile(Context context, Uri uri) {
        try {
            Intent intent = IntentBuilder.from((Activity) context).setType(context.getContentResolver().getType(uri)).setStream(uri).getIntent();
            intent.addFlags(1);
            Intent createChooser = Intent.createChooser(intent, context.getResources().getString(R.string.share_this_file_via));
            createChooser.setFlags(268435456);
            if (createChooser.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(createChooser);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.cant_share_file, 1).show();
        }
    }

    public static boolean isFileNameValid(String str) {
        String trim = str.trim();
        return !TextUtils.isEmpty(trim) && trim.matches("[a-zA-Z0-9-_ ]*");
    }

    public static void deletePdfFiles(String str) {
        File file = new File(str);
        if (file.exists() && file.isDirectory()) {
            StringBuilder sb = new StringBuilder();
            sb.append("find ");
            sb.append(str);
            sb.append(" -xdev -mindepth 1 -delete");
            try {
                Runtime.getRuntime().exec(sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Uri getImageUriFromPath(String str) {
        return Uri.fromFile(new File(str.replace(".pdf", ".jpg")));
    }

    public static boolean isThumbnailPresent(Context context, String str) {
        String name = new File(str).getName();
        StringBuilder sb = new StringBuilder();
        sb.append(context.getCacheDir());
        sb.append("/Thumbnails/");
        String sb2 = sb.toString();
        StringBuilder sb3 = new StringBuilder();
        sb3.append(sb2);
        sb3.append(removePdfExtension(name));
        sb3.append(".jpg");
        return new File(sb3.toString()).exists();
    }

    public static void generatePDFThumbnail(Context context, String str) {
        PdfiumCore pdfiumCore = new PdfiumCore(context);
        File file = new File(str);
        String name = file.getName();
        try {
            PdfDocument newDocument = pdfiumCore.newDocument(context.getContentResolver().openFileDescriptor(Uri.fromFile(file), PDPageLabelRange.STYLE_ROMAN_LOWER));
            StringBuilder sb = new StringBuilder();
            sb.append(context.getCacheDir());
            sb.append("/Thumbnails/");
            String sb2 = sb.toString();
            File file2 = new File(sb2);
            if (!file2.exists()) {
                file2.mkdirs();
            }
            StringBuilder sb3 = new StringBuilder();
            sb3.append(sb2);
            sb3.append(removePdfExtension(name));
            sb3.append(".jpg");
            String sb4 = sb3.toString();
            StringBuilder sb5 = new StringBuilder();
            sb5.append("Generating thumb img ");
            sb5.append(sb4);
            FileOutputStream fileOutputStream = new FileOutputStream(sb4);
            pdfiumCore.openPage(newDocument, 0);
            int pageWidthPoint = pdfiumCore.getPageWidthPoint(newDocument, 0) / 2;
            int pageHeightPoint = pdfiumCore.getPageHeightPoint(newDocument, 0) / 2;
            try {
                Bitmap createBitmap = Bitmap.createBitmap(pageWidthPoint, pageHeightPoint, Config.RGB_565);
                pdfiumCore.renderPageBitmap(newDocument, createBitmap, 0, 0, 0, pageWidthPoint, pageHeightPoint, true);
                createBitmap.compress(CompressFormat.JPEG, 50, fileOutputStream);
            } catch (OutOfMemoryError e) {
                Toast.makeText(context, R.string.failed_low_memory, 1).show();
                e.printStackTrace();
            }
            pdfiumCore.closeDocument(newDocument);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public static void printPdfFile(Context context, Uri uri) {
        try {
            new PdfiumCore(context).newDocument(context.getContentResolver().openFileDescriptor(uri, PDPageLabelRange.STYLE_ROMAN_LOWER));
            if (PrintHelper.systemSupportsPrint()) {
                PrintManager printManager = (PrintManager) context.getSystemService(PDWindowsLaunchParams.OPERATION_PRINT);
                StringBuilder sb = new StringBuilder();
                sb.append(context.getString(R.string.app_name));
                sb.append(" Document");
                String sb2 = sb.toString();
                if (printManager != null) {
                    printManager.print(sb2, new PrintDocumentAdapter(context, uri), null);
                    return;
                }
                return;
            }
            Toast.makeText(context, R.string.device_does_not_support_printing, Toast.LENGTH_LONG).show();
        } catch (PdfPasswordException e) {
            Toast.makeText(context, R.string.cant_print_password_protected_pdf, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException e2) {
            Toast.makeText(context, R.string.cannot_print_malformed_pdf, Toast.LENGTH_LONG).show();
            e2.printStackTrace();
        } catch (Exception e3) {
            Toast.makeText(context, R.string.cannot_print_unknown_error, Toast.LENGTH_LONG).show();
            e3.printStackTrace();
        }
    }


    public static void startShareActivity(Context context) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");
        intent.putExtra("android.intent.extra.TEXT", "Hi! 'am using this nice app 'PDF Reader' for reading and manipulating PDF files. You can find it on Google Play or at this link https://play.google.com/store/apps/details?id=com.pdftools.pdfreader.pdfviewer");
        intent.setType("text/plain");
        Intent createChooser = Intent.createChooser(intent, context.getResources().getString(R.string.chooser_title));
        createChooser.setFlags(268435456);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(new Intent(createChooser));
        }
    }

    public static void openProVersionPlayStore(Context context) {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=com.pdftools.pdfreader.pdfviewer"));
        intent.setFlags(268435456);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                Intent intent2 = new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=com.example.pdfreader"));
                intent2.setFlags(268435456);
                context.startActivity(intent2);
            } catch (Exception unused) {
                Toast.makeText(context, R.string.unable_to_find_play_store, 1).show();
            }
        }
    }

    public static void setLightStatusBar(Context context) {
        Activity activity = (Activity) context;
        View decorView = activity.getWindow().getDecorView();
        int systemUiVisibility = decorView.getSystemUiVisibility();
        int color = context.getResources().getColor(R.color.colorAccent);
        int i = VERSION.SDK_INT;
        if (i >= 21 && i >= 23) {
            decorView.setSystemUiVisibility(systemUiVisibility & -8193);
            activity.getWindow().setStatusBarColor(color);
        }
    }

    public static void clearLightStatusBar(Context context) {
        Activity activity = (Activity) context;
        View decorView = activity.getWindow().getDecorView();
        int systemUiVisibility = decorView.getSystemUiVisibility();
        int color = context.getResources().getColor(R.color.colorPrimaryDark);
        int i = VERSION.SDK_INT;
        if (i >= 21 && i >= 23) {
            decorView.setSystemUiVisibility(systemUiVisibility | 8192);
            activity.getWindow().setStatusBarColor(color);
        }
    }

    public static MemoryInfo getAvailableMemory(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
        MemoryInfo memoryInfo = new MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo;
    }

    public static void showPremiumFeatureDialog(final Context context) {
        Builder builder = new Builder(context);
        builder.setView(((Activity) context).getLayoutInflater().inflate(R.layout.dialog_premium_feature, null));
        final AlertDialog create = builder.create();
        create.show();
        AppCompatButton appCompatButton = (AppCompatButton) create.findViewById(R.id.btn_later);
        ((AppCompatButton) create.findViewById(R.id.btn_get_premium)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                create.dismiss();
                Utils.openProVersionPlayStore(context);
            }
        });
        appCompatButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                create.dismiss();
            }
        });
    }

    public static void launchMarket(Context context) {
        Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.putBoolean("prefs_show_rate_us", false);
        edit.apply();
        StringBuilder sb = new StringBuilder();
        sb.append("market://details?id=");
        sb.append(context.getPackageName());
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(sb.toString()));
        intent.addFlags(268435456);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException unused) {
            Toast.makeText(context, R.string.unable_to_find_play_store, 1).show();
        }
    }
}
