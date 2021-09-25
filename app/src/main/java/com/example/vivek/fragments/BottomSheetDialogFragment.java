package com.example.vivek.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback;
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior;
import androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.vivek.DataUpdatedEvent.DevicePDFStaredEvent;
import com.example.vivek.DataUpdatedEvent.PdfRenameEvent;
import com.example.vivek.DataUpdatedEvent.PermanetlyDeleteEvent;
import com.example.vivek.DataUpdatedEvent.RecentPDFStaredEvent;
import com.example.vivek.PDFToolsActivity;
import com.example.vivek.R;
import com.example.vivek.ShareAsPictureActivity;
import com.example.vivek.data.DbHelper;
import com.example.vivek.utils.Utils;
import java.io.File;
import org.greenrobot.eventbus.EventBus;

public class BottomSheetDialogFragment extends com.google.android.material.bottomsheet.BottomSheetDialogFragment implements OnClickListener {
    public static final String FROM_RECENT = "com.example.pdfreader.FROM_RECENT";
    public static final String PDF_PATH = "com.example.pdfreader.PDF_PATH";
    public final String TAG = BottomSheetDialogFragment.class.getSimpleName();
    Context context;
    String fileName,pdfPath;
    Boolean fromRecent;
    ImageView imgBookMarkPdf;


    @SuppressLint({"RestrictedApi"})
    public void setupDialog(Dialog dialog, int i) {
        super.setupDialog(dialog, i);
        Bundle arguments = getArguments();
        this.pdfPath = arguments.getString(FROM_RECENT);
        this.fileName = new File(this.pdfPath).getName();
        this.fromRecent = Boolean.valueOf(arguments.getBoolean("fromRecent"));
        this.context = getContext();
        View inflate = View.inflate(this.context, R.layout.fragment_bottom_sheet_dialog, null);

        dialog.setContentView(inflate);
        TextView tvPdfFileName = (TextView) inflate.findViewById(R.id.tvPdfFileName);
        this.imgBookMarkPdf = (ImageView) inflate.findViewById(R.id.imgBookMarkPdf);
        tvPdfFileName.setText(this.fileName);
        setupPdfStared();
        Behavior behavior = ((LayoutParams) ((View) inflate.getParent()).getLayoutParams()).getBehavior();
        inflate.findViewById(R.id.laySharePdf).setOnClickListener(this);
        inflate.findViewById(R.id.layRenamePdf).setOnClickListener(this);
        inflate.findViewById(R.id.layPdfReader).setOnClickListener(this);
        inflate.findViewById(R.id.layPdfPrint).setOnClickListener(this);
        inflate.findViewById(R.id.layPdfDelete).setOnClickListener(this);
        inflate.findViewById(R.id.laySharePdfPicture).setOnClickListener(this);
        inflate.findViewById(R.id.layPdfSaveLocation).setOnClickListener(this);
        this.imgBookMarkPdf.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialogFragment.this.dismiss();
                DbHelper instance = DbHelper.getInstance(BottomSheetDialogFragment.this.context);
                if (instance.isStared(BottomSheetDialogFragment.this.pdfPath)) {
                    instance.removeStaredPDF(BottomSheetDialogFragment.this.pdfPath);
                } else {
                    instance.addStaredPDF(BottomSheetDialogFragment.this.pdfPath);
                }
                EventBus.getDefault().post(new RecentPDFStaredEvent());
                EventBus.getDefault().post(new DevicePDFStaredEvent());
            }
        });
        if (behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(this.mBottomSheetBehaviorCallback);
        }
    }

    public void onClick(View view) {
        dismiss();
        switch (view.getId()) {
            case R.id.layPdfDelete:
                deletePdf();
                return;
            case R.id.layRenamePdf:
                renamePdf();
                return;
            case R.id.layPdfSaveLocation:
                Toast.makeText(this.context, this.pdfPath, Toast.LENGTH_LONG).show();
                return;
            case R.id.layPdfReader:
                openPdfReader();
                return;
            case R.id.layPdfPrint:
                Utils.printPdfFile(this.context, Uri.fromFile(new File(this.pdfPath)));
                return;
            case R.id.laySharePdfPicture:
                shareAsPicture(Uri.fromFile(new File(this.pdfPath)));
                return;
            case R.id.laySharePdf:
                try {
                    Utils.sharePdfFile(this.context, FileProvider.getUriForFile(this.context, Utils.FILE_AUTHORITY, new File(this.pdfPath)));
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this.context, R.string.cant_share_file, Toast.LENGTH_LONG).show();
                    return;
                }
            default:
                return;
        }
    }

    public void deletePdf() {
        if (this.fromRecent.booleanValue()) {
            DbHelper.getInstance(this.context).deleteRecentPDF(this.pdfPath);
            return;
        }
        deletePdfConfirmDialog();
    }

    public void deletePdfConfirmDialog() {
        Builder builder = new Builder(this.context);
        builder.setTitle((int) R.string.permanently_delete_file).setPositiveButton((int) R.string.delete, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                File file = new File(BottomSheetDialogFragment.this.pdfPath);
                if (file.delete()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(BottomSheetDialogFragment.this.context.getCacheDir());
                    stringBuilder.append("/Thumbnails/");
                    stringBuilder.append(Utils.removePdfExtension(file.getName()));
                    stringBuilder.append(".jpg");
                    new File(stringBuilder.toString()).delete();
                    BottomSheetDialogFragment bottomSheetDialogFragment = BottomSheetDialogFragment.this;
                    MediaScannerConnection.scanFile(bottomSheetDialogFragment.context, new String[]{bottomSheetDialogFragment.pdfPath}, null, new OnScanCompletedListener() {
                        public void onScanCompleted(String str, Uri uri) {
                            EventBus.getDefault().post(new PermanetlyDeleteEvent());
                            String str2 = BottomSheetDialogFragment.this.TAG;
                            StringBuilder sb = new StringBuilder();
                            sb.append("File deleted ");
                            sb.append(BottomSheetDialogFragment.this.pdfPath);
                            Log.d(str2, sb.toString());
                        }
                    });
                    return;
                }
                Toast.makeText(BottomSheetDialogFragment.this.context, "Can't delete pdf file", Toast.LENGTH_LONG).show();
            }
        }).setNegativeButton((int) R.string.cancel, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }

    public void renamePdf() {
        Builder builder = new Builder(this.context);
        final File fileOldPdfName = new File(this.pdfPath);
        final String removeExtension = Utils.removePdfExtension(fileOldPdfName.getName());
        float size = this.context.getResources().getDisplayMetrics().density;
        final EditText etPdfNewName = new EditText(this.context);
        etPdfNewName.setText(removeExtension);
        etPdfNewName.setSelectAllOnFocus(true);
        builder.setTitle((int) R.string.rename_file).setPositiveButton((int) R.string.ok, (DialogInterface.OnClickListener) null).setNegativeButton((int) R.string.cancel, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        int i = (int) (24.0f * size);
        final AlertDialog alertDialog = dialog;
        alertDialog.setView(etPdfNewName, i, (int) (8.0f * size), i, (int) (size * 5.0f));
        dialog.show();
        Button btnNamePdfOk = dialog.getButton(-1);
        btnNamePdfOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String strPdfNewName = etPdfNewName.getText().toString();
                if (TextUtils.equals(removeExtension, strPdfNewName)) {
                    alertDialog.dismiss();
                    Log.d(BottomSheetDialogFragment.this.TAG, "File name not changed");
                } else if (Utils.isFileNameValid(strPdfNewName)) {
                    final String replacePdfName = BottomSheetDialogFragment.this.pdfPath.replace(removeExtension, strPdfNewName);
                    if (fileOldPdfName.renameTo(new File(replacePdfName))) {
                        alertDialog.dismiss();
                        DbHelper dbHelper = DbHelper.getInstance(BottomSheetDialogFragment.this.context);
                        dbHelper.updateHistory(BottomSheetDialogFragment.this.pdfPath, replacePdfName);
                        dbHelper.updateStaredPDF(BottomSheetDialogFragment.this.pdfPath, replacePdfName);
                        dbHelper.updateBookmarkPath(BottomSheetDialogFragment.this.pdfPath, replacePdfName);
                        dbHelper.updateLastOpenedPagePath(BottomSheetDialogFragment.this.pdfPath, replacePdfName);
                        StringBuilder builderThumb = new StringBuilder();
                        builderThumb.append(BottomSheetDialogFragment.this.context.getCacheDir());
                        builderThumb.append("/Thumbnails/");
                        String sb2 = builderThumb.toString();
                        StringBuilder sb3 = new StringBuilder();
                        sb3.append(sb2);
                        sb3.append(Utils.removePdfExtension(fileOldPdfName.getName()));
                        sb3.append(".jpg");
                        String sb4 = sb3.toString();
                        StringBuilder sb5 = new StringBuilder();
                        sb5.append(sb2);
                        sb5.append(Utils.removePdfExtension(strPdfNewName));
                        sb5.append(".jpg");
                        String sb6 = sb5.toString();
                        String str = BottomSheetDialogFragment.this.TAG;
                        StringBuilder sb7 = new StringBuilder();
                        sb7.append("Rename thumbnail from ");
                        sb7.append(sb4);
                        Log.d(str, sb7.toString());
                        String str2 = BottomSheetDialogFragment.this.TAG;
                        StringBuilder sb8 = new StringBuilder();
                        sb8.append("Rename thumbnail to ");
                        sb8.append(sb6);
                        Log.d(str2, sb8.toString());
                        new File(sb4).renameTo(new File(sb6));
                        MediaScannerConnection.scanFile(BottomSheetDialogFragment.this.context, new String[]{replacePdfName}, null, new OnScanCompletedListener() {
                            public void onScanCompleted(String str, Uri uri) {
                                EventBus.getDefault().post(new PdfRenameEvent());
                                String str2 = BottomSheetDialogFragment.this.TAG;
                                StringBuilder sb = new StringBuilder();
                                sb.append("Old pdf path");
                                sb.append(BottomSheetDialogFragment.this.pdfPath);
                                Log.d(str2, sb.toString());
                                String str3 = BottomSheetDialogFragment.this.TAG;
                                StringBuilder sb2 = new StringBuilder();
                                sb2.append("New pdf path");
                                sb2.append(replacePdfName);
                                Log.d(str3, sb2.toString());
                            }
                        });
                        return;
                    }
                    Toast.makeText(BottomSheetDialogFragment.this.context, R.string.failed_to_rename_file, Toast.LENGTH_LONG).show();
                } else {
                    etPdfNewName.setError(BottomSheetDialogFragment.this.context.getString(R.string.invalid_file_name));
                }
            }
        });
    }

    public void shareAsPicture(Uri uri) {
        Intent intent = new Intent(this.context, ShareAsPictureActivity.class);
        intent.putExtra("com.example.pdfreader.PDF_PATH", uri.toString());
        startActivity(intent);
    }

    public void openPdfReader() {
        Uri fromFile = Uri.fromFile(new File(this.pdfPath));
        Intent intent = new Intent(this.context, PDFToolsActivity.class);
        intent.putExtra(PDFToolsActivity.PRE_SELECTED_PDF_PATH, fromFile.toString());
        startActivity(intent);
    }



    private BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetCallback() {
        public void onSlide(@NonNull View view, float f) {
        }

        public void onStateChanged(@NonNull View view, int i) {
            if (i == 5) {
                BottomSheetDialogFragment.this.dismiss();
            }
        }
    };


    public void setupPdfStared() {
        if (DbHelper.getInstance(this.context).isStared(this.pdfPath)) {
            this.imgBookMarkPdf.setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_action_star_yellow));
        }
    }




}
