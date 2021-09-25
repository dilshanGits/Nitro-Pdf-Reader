package com.example.vivek.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;

import androidx.constraintlayout.widget.ConstraintLayout;

import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import com.example.vivek.BrowsePDFActivity;
import com.example.vivek.PDFToolsActivity;
import com.example.vivek.PDFViewerActivity;
import com.example.vivek.R;
import com.example.vivek.SelectPDFActivity;
import com.example.vivek.ViewImagesActivity;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility;
import com.tom_roush.pdfbox.pdmodel.common.PDPageLabelRange;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PDFTools {

    public static final String TAG = "PDFTools";
    public static Button btnCancelProgress;
    private static Button btnOpenPdfFile;
    public static ImageView imgCloseProgress;
    public static TextView tvCurrentAction;
    public static TextView tvDownloadPercent;
    private static TextView tvDescription;
    private static TextView tvSavedPdfPath;
    public static ConstraintLayout mProgressView;
    public static ProgressBar progressDownloading;
    private static ImageView imgPdfSuccess;


    public static class CompressPDFImproved extends AsyncTask<Void, Integer, Void> {
        String allPdfDocumentDir;
        Long compressedFileLength;
        String compressedFileSize;
        String compressedPDF;
        int compressionQuality;
        boolean isEcrypted = false;
        Context mContext;
        String pdfPath;
        String reducedPercent;
        Long uncompressedFileLength;
        String uncompressedFileSize;
        int xrefSize;

        public CompressPDFImproved(Context context, String str, int i, ConstraintLayout constraintLayout) {
            mContext = context;
            pdfPath = str;
            mProgressView = constraintLayout;
            initializeProgressView();
            Utils.setLightStatusBar(context);
            this.compressionQuality = i;
            btnCancelProgress.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    CompressPDFImproved.this.cancel(true);
                    CompressPDFImproved compressPDFImproved = CompressPDFImproved.this;
                    closeProgressView(compressPDFImproved.mContext);
                }
            });
        }

        public void onPreExecute() {
            super.onPreExecute();
            tvCurrentAction.setText(R.string.compressing);
            mProgressView.setVisibility(View.VISIBLE);
        }


        public Void doInBackground(Void... voidArr) {
            boolean z = PreferenceManager.getDefaultSharedPreferences(this.mContext).getBoolean(BrowsePDFActivity.GRID_VIEW_ENABLED, false);
            File file = new File(this.pdfPath);
            String strPdfName = file.getName();
            this.uncompressedFileLength = Long.valueOf(file.length());
            this.uncompressedFileSize = Formatter.formatShortFileSize(this.mContext, this.uncompressedFileLength.longValue());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Environment.getExternalStorageDirectory());
            stringBuilder.append("/Documents/AllPdf/");
            this.allPdfDocumentDir = stringBuilder.toString();
            StringBuilder builderCompressed = new StringBuilder();
            builderCompressed.append(this.allPdfDocumentDir);
            builderCompressed.append(Utils.removePdfExtension(strPdfName));
            builderCompressed.append("-Compressed.pdf");
            this.compressedPDF = builderCompressed.toString();
            File file2 = new File(this.allPdfDocumentDir);
            if (!file2.exists()) {
                file2.mkdirs();
            }
            try {
                PdfReader pdfReader = new PdfReader(this.pdfPath);
                if (pdfReader.isEncrypted()) {
                    this.isEcrypted = true;
                    return null;
                }
                this.xrefSize = pdfReader.getXrefSize();
                progressDownloading.setMax(this.xrefSize);
                for (int i = 0; i < this.xrefSize && !isCancelled(); i++) {
                    PdfObject pdfObject = pdfReader.getPdfObject(i);
                    if (pdfObject != null) {
                        if (pdfObject.isStream()) {
                            PRStream pRStream = (PRStream) pdfObject;
                            PdfObject pdfObject2 = pRStream.get(PdfName.SUBTYPE);
                            if (pdfObject2 != null && pdfObject2.toString().equals(PdfName.IMAGE.toString())) {
                                try {
                                    Bitmap compressedBitmap = ImageUtils.getInstant().getPdfCompressedBitmap(new PdfImageObject(pRStream).getImageAsBytes());
                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                    compressedBitmap.compress(CompressFormat.JPEG, this.compressionQuality, byteArrayOutputStream);
                                    pRStream.setData(byteArrayOutputStream.toByteArray(), false, 9);
                                    pRStream.put(PdfName.FILTER, PdfName.DCTDECODE);
                                    byteArrayOutputStream.close();
                                    pRStream.clear();
                                    pRStream.setData(byteArrayOutputStream.toByteArray(), false, 0);
                                    pRStream.put(PdfName.TYPE, PdfName.XOBJECT);
                                    pRStream.put(PdfName.SUBTYPE, PdfName.IMAGE);
                                    pRStream.put(PdfName.FILTER, PdfName.DCTDECODE);
                                    pRStream.put(PdfName.WIDTH, new PdfNumber(compressedBitmap.getWidth()));
                                    pRStream.put(PdfName.HEIGHT, new PdfNumber(compressedBitmap.getHeight()));
                                    pRStream.put(PdfName.BITSPERCOMPONENT, new PdfNumber(8));
                                    pRStream.put(PdfName.COLORSPACE, PdfName.DEVICERGB);
                                    if (!compressedBitmap.isRecycled()) {
                                        compressedBitmap.recycle();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            publishProgress(new Integer[]{Integer.valueOf(i + 1)});
                        }
                    }
                }
                pdfReader.removeUnusedObjects();
                PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(this.compressedPDF));
                pdfStamper.setFullCompression();
                pdfStamper.close();
                this.compressedFileLength = Long.valueOf(new File(this.compressedPDF).length());
                this.compressedFileSize = Formatter.formatShortFileSize(this.mContext, this.compressedFileLength.longValue());
                StringBuilder sb3 = new StringBuilder();
                sb3.append(100 - ((int) ((this.compressedFileLength.longValue() * 100) / this.uncompressedFileLength.longValue())));
                sb3.append("%");
                this.reducedPercent = sb3.toString();
                MediaScannerConnection.scanFile(this.mContext, new String[]{this.compressedPDF}, new String[]{"application/pdf"}, null);
                if (z) {
                    Utils.generatePDFThumbnail(this.mContext, this.compressedPDF);
                }
                return null;
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return null;
        }


        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            updateProgressPercent(numArr[0].intValue(), this.xrefSize);
        }


        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            if (!this.isEcrypted) {
                StringBuilder sb = new StringBuilder();
                sb.append(this.mContext.getString(R.string.reduced_from));
                sb.append(" ");
                sb.append(this.uncompressedFileSize);
                sb.append(" ");
                sb.append(this.mContext.getString(R.string.to));
                sb.append(" ");
                sb.append(this.compressedFileSize);
                String sb2 = sb.toString();
                tvDownloadPercent.setText(R.string.hundred_percent);
                progressDownloading.setProgress(this.xrefSize);
                tvCurrentAction.setText(R.string.done);
                btnCancelProgress.setOnClickListener(null);
                showInterstialAd(this.mContext, this.reducedPercent, sb2, this.allPdfDocumentDir);
                Context context = this.mContext;
                setupOpenPath(context, context.getString(R.string.open_file), this.compressedPDF, true);
                return;
            }
            closeProgressView(this.mContext);
            Toast.makeText(this.mContext, R.string.file_protected_unprotect, Toast.LENGTH_LONG).show();
        }
    }

    public static class ConvertPDFToPdfPictures extends AsyncTask<Void, Integer, Void> {
        String allPdfPictureDir;
        String fileName;
        Context mContext;
        int numPages;
        PdfDocument pdfDocument;
        String pdfPath;
        PdfiumCore pdfiumCore;

        public ConvertPDFToPdfPictures(Context context, String str, ConstraintLayout constraintLayout) {
            this.mContext = context;
            this.pdfPath = str;
            mProgressView = constraintLayout;
            initializeProgressView();
            Utils.setLightStatusBar(context);
            btnCancelProgress.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    ConvertPDFToPdfPictures.this.cancel(true);
                    ConvertPDFToPdfPictures convertPDFToPdfPictures = ConvertPDFToPdfPictures.this;
                    closeProgressView(convertPDFToPdfPictures.mContext);
                }
            });
        }

        public void onPreExecute() {
            super.onPreExecute();
            tvCurrentAction.setText(R.string.converting);
            mProgressView.setVisibility(View.VISIBLE);
        }

        public Void doInBackground(Void... voidArr) {
            Void voidR;
            int i, i2;
            String str;
            this.fileName = Utils.removePdfExtension(new File(this.pdfPath).getName());
            String strPdfName = new File(this.pdfPath).getName();
            ArrayList arrayList = new ArrayList();
            ArrayList arrayList2 = new ArrayList();
            StringBuilder sb = new StringBuilder();
            sb.append(Environment.getExternalStorageDirectory());
            sb.append("/Pictures/AllPdf/");
            sb.append(Utils.removePdfExtension(strPdfName));
            sb.append("/");
            this.allPdfPictureDir = sb.toString();
            File file = new File(this.allPdfPictureDir);
            if (!file.exists()) {
                file.mkdirs();
            }
            this.pdfiumCore = new PdfiumCore(this.mContext);
            try {
                this.pdfDocument = this.pdfiumCore.newDocument(this.mContext.getContentResolver().openFileDescriptor(Uri.fromFile(new File(this.pdfPath)), PDPageLabelRange.STYLE_ROMAN_LOWER));
                this.numPages = this.pdfiumCore.getPageCount(this.pdfDocument);
                progressDownloading.setMax(this.numPages);
                for (int i3 = 0; i3 < this.numPages && !isCancelled(); i3 = i) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(this.allPdfPictureDir);
                    sb2.append(Utils.removePdfExtension(strPdfName));
                    sb2.append("-Page");
                    int i4 = i3 + 1;
                    sb2.append(i4);
                    sb2.append(".jpg");
                    String sb3 = sb2.toString();
                    FileOutputStream fileOutputStream = new FileOutputStream(sb3);
                    String access$600 = PDFTools.TAG;
                    StringBuilder sb4 = new StringBuilder();
                    sb4.append("Creating page image ");
                    sb4.append(sb3);
                    Log.d(access$600, sb4.toString());
                    this.pdfiumCore.openPage(this.pdfDocument, i3);
                    int pageWidthPoint = this.pdfiumCore.getPageWidthPoint(this.pdfDocument, i3) * 2;
                    int pageHeightPoint = this.pdfiumCore.getPageHeightPoint(this.pdfDocument, i3) * 2;
                    try {
                        Bitmap createBitmap = Bitmap.createBitmap(pageWidthPoint, pageHeightPoint, Config.ARGB_8888);
                        Bitmap bitmap = createBitmap;
                        i2 = 1;
                        str = sb3;
                        i = i4;
                        try {
                            this.pdfiumCore.renderPageBitmap(this.pdfDocument, createBitmap, i3, 0, 0, pageWidthPoint, pageHeightPoint, true);
                            bitmap.compress(CompressFormat.JPEG, 60, fileOutputStream);
                        } catch (OutOfMemoryError e) {
                            e = e;
                        }
                    } catch (OutOfMemoryError e2) {
                        OutOfMemoryError e = e2;
                        str = sb3;
                        i = i4;
                        i2 = 1;
                        Toast.makeText(this.mContext, R.string.failed_low_memory, i2).show();
                        e.printStackTrace();
                        arrayList.add(str);
                        arrayList2.add("image/jpg");
                        Integer[] numArr = new Integer[i2];
                        numArr[0] = Integer.valueOf(i);
                        publishProgress(numArr);
                    }
                    arrayList.add(str);
                    arrayList2.add("image/jpg");
                    Integer[] numArr2 = new Integer[i2];
                    numArr2[0] = Integer.valueOf(i);
                    publishProgress(numArr2);
                }
                this.pdfiumCore.closeDocument(this.pdfDocument);
                try {
                    MediaScannerConnection.scanFile(this.mContext, (String[]) arrayList.toArray(new String[arrayList.size()]), (String[]) arrayList2.toArray(new String[arrayList2.size()]), null);
                    return null;
                } catch (Exception unused) {
                    voidR = null;
                    return voidR;
                }
            } catch (Exception unused2) {
                voidR = null;
                return voidR;
            }
        }


        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            updateProgressPercent(numArr[0].intValue(), this.numPages);
        }


        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            tvCurrentAction.setText(R.string.done);
            btnCancelProgress.setOnClickListener(null);
            showInterstialAd(this.mContext, "", "", this.allPdfPictureDir);
            Context context = this.mContext;
            openImageDirectory(context, context.getString(R.string.open_directory), this.allPdfPictureDir);
        }
    }

    public static class ExtractPdfImages extends AsyncTask<Void, Integer, Void> {
        String allPdfPictureDir;
        int compressionQuality;
        Context mContext;
        String pdfPath;
        int xrefSize;

        public ExtractPdfImages(Context context, String str, int i, ConstraintLayout constraintLayout) {
            this.mContext = context;
            this.pdfPath = str;
            mProgressView = constraintLayout;
            initializeProgressView();
            Utils.setLightStatusBar(context);
            this.compressionQuality = i;
            btnCancelProgress.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    ExtractPdfImages.this.cancel(true);
                    ExtractPdfImages extractPdfImages = ExtractPdfImages.this;
                    closeProgressView(extractPdfImages.mContext);
                }
            });
        }

        public void onPreExecute() {
            super.onPreExecute();
            tvCurrentAction.setText(R.string.extracting);
            mProgressView.setVisibility(View.VISIBLE);
        }

        public Void doInBackground(Void... voidArr) {
            String str = null;
            Exception e;
            ArrayList arrayList = new ArrayList();
            ArrayList arrayList2 = new ArrayList();
            String str2 = "";
            String name = new File(this.pdfPath).getName();
            StringBuilder sb = new StringBuilder();
            sb.append(Environment.getExternalStorageDirectory());
            sb.append("/Pictures/AllPdf/");
            sb.append(Utils.removePdfExtension(name));
            sb.append("/");
            this.allPdfPictureDir = sb.toString();
            File file = new File(this.allPdfPictureDir);
            if (!file.exists()) {
                file.mkdirs();
            }
            try {
                PdfReader pdfReader = new PdfReader(this.pdfPath);
                this.xrefSize = pdfReader.getXrefSize();
                progressDownloading.setMax(this.xrefSize);
                String str3 = str2;
                int i = 1;
                for (int i2 = 0; i2 < this.xrefSize && !isCancelled(); i2++) {
                    PdfObject pdfObject = pdfReader.getPdfObject(i2);
                    if (pdfObject != null) {
                        if (pdfObject.isStream()) {
                            PRStream pRStream = (PRStream) pdfObject;
                            PdfObject pdfObject2 = pRStream.get(PdfName.SUBTYPE);
                            if (pdfObject2 == null || !pdfObject2.toString().equals(PdfName.IMAGE.toString())) {
                                str = str3;
                            } else {
                                try {
                                    byte[] imageAsBytes = new PdfImageObject(pRStream).getImageAsBytes();
                                    Bitmap decodeByteArray = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
                                    if (decodeByteArray != null) {
                                        StringBuilder sb2 = new StringBuilder();
                                        sb2.append(this.allPdfPictureDir);
                                        sb2.append("image-");
                                        sb2.append(i);
                                        sb2.append(".jpg");
                                        str = sb2.toString();
                                        try {
                                            decodeByteArray.compress(CompressFormat.JPEG, this.compressionQuality, new FileOutputStream(str));
                                            String access$600 = PDFTools.TAG;
                                            StringBuilder sb3 = new StringBuilder();
                                            sb3.append("Image extracted ");
                                            sb3.append(this.allPdfPictureDir);
                                            sb3.append("image-");
                                            sb3.append(i);
                                            sb3.append(".jpg");
                                            Log.d(access$600, sb3.toString());
                                            pRStream.clear();
                                            if (!decodeByteArray.isRecycled()) {
                                                decodeByteArray.recycle();
                                            }
                                            i++;
                                        } catch (Exception e2) {
                                            e = e2;
                                            e.printStackTrace();
                                            arrayList.add(str);
                                            arrayList2.add("image/jpg");
                                            publishProgress(new Integer[]{Integer.valueOf(i2 + 1)});
                                            str3 = str;
                                        }
                                    }
                                } catch (Exception e3) {
                                    str = str3;
                                    e = e3;
                                    e.printStackTrace();
                                    arrayList.add(str);
                                    arrayList2.add("image/jpg");
                                    publishProgress(new Integer[]{Integer.valueOf(i2 + 1)});
                                    str3 = str;
                                }
                            }
                            arrayList.add(str);
                            arrayList2.add("image/jpg");
                            publishProgress(new Integer[]{Integer.valueOf(i2 + 1)});
                            str3 = str;
                        }
                    }
                }
                MediaScannerConnection.scanFile(this.mContext, (String[]) arrayList.toArray(new String[arrayList.size()]), (String[]) arrayList2.toArray(new String[arrayList2.size()]), null);
            } catch (Exception e4) {
                e4.printStackTrace();
            }
            return null;
        }

        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            updateProgressPercent(numArr[0].intValue(), this.xrefSize);
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            tvDownloadPercent.setText(R.string.hundred_percent);
            progressDownloading.setProgress(this.xrefSize);
            tvCurrentAction.setText(R.string.done);
            btnCancelProgress.setOnClickListener(null);
            showInterstialAd(this.mContext, "", "", this.allPdfPictureDir);
            Context context = this.mContext;
            openImageDirectory(context, context.getString(R.string.open_directory), this.allPdfPictureDir);
        }
    }

    public static class MergePDFFiles extends AsyncTask<Void, Integer, Void> {
        String allPdfMergedDir;
        Context mContext;
        boolean mergeSuccess = true;
        String mergedFileName;
        String mergedFilePath;
        int numFiles;
        ArrayList<String> pdfPaths;

        public MergePDFFiles(Context context, ArrayList<String> arrayList, String str, ConstraintLayout constraintLayout) {
            this.mContext = context;
            this.pdfPaths = arrayList;
            this.mergedFileName = str;
            mProgressView = constraintLayout;
            initializeProgressView();
            Utils.setLightStatusBar(context);
            btnCancelProgress.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    MergePDFFiles.this.cancel(true);
                    MergePDFFiles mergePDFFiles = MergePDFFiles.this;
                    closeProgressView(mergePDFFiles.mContext);
                }
            });
        }

        public void onPreExecute() {
            super.onPreExecute();
            progressDownloading.setIndeterminate(true);
            tvCurrentAction.setText(R.string.merging);
            mProgressView.setVisibility(View.VISIBLE);
        }

        public Void doInBackground(Void... voidArr) {
            boolean z = PreferenceManager.getDefaultSharedPreferences(this.mContext).getBoolean(BrowsePDFActivity.GRID_VIEW_ENABLED, false);
            StringBuilder sb = new StringBuilder();
            sb.append(Environment.getExternalStorageDirectory());
            sb.append("/Documents/AllPdf/Merged/");
            this.allPdfMergedDir = sb.toString();
            File file = new File(this.allPdfMergedDir);
            if (!file.exists()) {
                file.mkdirs();
            }
            StringBuilder sb2 = new StringBuilder();
            sb2.append(this.allPdfMergedDir);
            sb2.append(this.mergedFileName);
            sb2.append(".pdf");
            this.mergedFilePath = sb2.toString();
            this.numFiles = this.pdfPaths.size();
            progressDownloading.setMax(this.numFiles + 1);
            PDFBoxResourceLoader.init(this.mContext);
            PDFMergerUtility pDFMergerUtility = new PDFMergerUtility();
            pDFMergerUtility.setDestinationFileName(this.mergedFilePath);
            removeProgressBarIndeterminate(this.mContext, progressDownloading);
            int i = 0;
            while (i < this.numFiles && !isCancelled()) {
                try {
                    pDFMergerUtility.addSource(new File((String) this.pdfPaths.get(i)));
                    i++;
                    publishProgress(new Integer[]{Integer.valueOf(i)});
                } catch (Exception e) {
                    e.printStackTrace();
                    this.mergeSuccess = false;
                }
            }
            try {
                pDFMergerUtility.mergeDocuments(true);         //---------changew
            } catch (IOException e) {
                e.printStackTrace();
            }
            publishProgress(new Integer[]{Integer.valueOf(this.numFiles + 1)});
            if (isCancelled()) {
                new File(this.mergedFilePath).delete();
            }
            MediaScannerConnection.scanFile(this.mContext, new String[]{this.mergedFilePath}, new String[]{"application/pdf"}, null);
            if (z) {
                Utils.generatePDFThumbnail(this.mContext, this.mergedFilePath);
            }
            return null;
        }

        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            updateProgressPercent(numArr[0].intValue(), this.numFiles + 1);
        }


        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            tvCurrentAction.setText(R.string.done);
            btnCancelProgress.setOnClickListener(null);
            showInterstialAd(this.mContext, "", "", this.allPdfMergedDir);
            Context context = this.mContext;
            setupOpenPath(context, context.getString(R.string.open_file), this.mergedFilePath, true);
            if (!this.mergeSuccess) {
                Toast.makeText(this.mContext, R.string.merge_failed, Toast.LENGTH_LONG).show();
            }
        }
    }

    public static class SplitPDF extends AsyncTask<Void, Integer, Void> {

        public Context mContext;
        private int numPages = 0, splitAt = 0, splitFrom = 0, splitTo = 0;
        private String pdfPath, splittedPdfDocumentDir;

        public SplitPDF(Context context, String str, ConstraintLayout constraintLayout) {
            this.mContext = context;
            this.pdfPath = str;
            mProgressView = constraintLayout;
            initializeProgressView();
            Utils.setLightStatusBar(context);
            btnCancelProgress.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    SplitPDF.this.cancel(true);
                    SplitPDF splitPDF = SplitPDF.this;
                    closeProgressView(splitPDF.mContext);
                }
            });
        }

        public SplitPDF(Context context, String str, ConstraintLayout constraintLayout, int i, int i2) {
            this.mContext = context;
            this.pdfPath = str;
            mProgressView = constraintLayout;
            initializeProgressView();
            Utils.setLightStatusBar(context);
            this.splitFrom = i;
            this.splitTo = i2;
            btnCancelProgress.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    SplitPDF.this.cancel(true);
                    SplitPDF splitPDF = SplitPDF.this;
                    closeProgressView(splitPDF.mContext);
                }
            });
        }

        public SplitPDF(Context context, String str, ConstraintLayout constraintLayout, int i) {
            this.mContext = context;
            this.pdfPath = str;
            mProgressView = constraintLayout;
            initializeProgressView();
            Utils.setLightStatusBar(context);
            this.splitAt = i;
            btnCancelProgress.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    SplitPDF.this.cancel(true);
                    SplitPDF splitPDF = SplitPDF.this;
                    closeProgressView(splitPDF.mContext);
                }
            });
        }

        public void onPreExecute() {
            super.onPreExecute();
            progressDownloading.setIndeterminate(true);
            tvCurrentAction.setText(R.string.splitting);
            mProgressView.setVisibility(View.VISIBLE);
        }

        public Void doInBackground(Void... r14) {


            ArrayList arrayList = new ArrayList();
            ArrayList arrayList2 = new ArrayList();

            try {

                Document doc = null;

                PdfReader pr = new PdfReader(pdfPath);

                PdfCopy pc = null;

                File f = new File(pdfPath);

                for (int i = 1; i <= pr.getNumberOfPages(); i++) { // looping through the pdf file

                    doc = new Document();

                    StringBuilder sb = new StringBuilder();
                    sb.append(Environment.getExternalStorageDirectory());

                    sb.append("/Documents/AllPdf/Split/");


                    splittedPdfDocumentDir = sb.toString();
                    File file2 = new File(this.splittedPdfDocumentDir);
                    if (!file2.exists()) {
                        file2.mkdirs();
                    }

                    pc = new PdfCopy(doc, new FileOutputStream(sb + f.getName() + "page" + i + ".pdf"));

                    doc.open();

                    pc.addPage(pc.getImportedPage(pr, i));

                    Utils.generatePDFThumbnail(this.mContext, sb + f.getName() + "page" + i + ".pdf");


                    arrayList.add(sb + f.getName() + "page" + i + ".pdf");
                    arrayList2.add("application/pdf");
                    doc.close();


                }
                MediaScannerConnection.scanFile(this.mContext, (String[]) arrayList.toArray(new String[arrayList.size()]), (String[]) arrayList2.toArray(new String[arrayList2.size()]), null);

                Log.e("done-->>", "The pdf is split successfully");


            } catch (Exception e) {


                Log.e("error-->>", e.toString());

            }


            return null;

        }

        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            updateProgressPercent(numArr[0].intValue(), this.numPages);
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            tvCurrentAction.setText(R.string.done);
            btnCancelProgress.setOnClickListener(null);
            showInterstialAd(this.mContext, "", "", this.splittedPdfDocumentDir);
            Context context = this.mContext;
            setupOpenPath(context, context.getString(R.string.open_directory), this.splittedPdfDocumentDir, false);
        }
    }


    public static void initializeProgressView() {
        tvDownloadPercent = (TextView) mProgressView.findViewById(R.id.tvDownloadPercent);
        tvCurrentAction = (TextView) mProgressView.findViewById(R.id.tvCurrentAction);
        progressDownloading = (ProgressBar) mProgressView.findViewById(R.id.progressDownloading);
        tvDescription = (TextView) mProgressView.findViewById(R.id.tvDescription);
        tvSavedPdfPath = (TextView) mProgressView.findViewById(R.id.tvSavedPdfPath);
        imgPdfSuccess = (ImageView) mProgressView.findViewById(R.id.imgPdfSuccess);
        btnOpenPdfFile = (Button) mProgressView.findViewById(R.id.btnOpenPdfFile);
        btnCancelProgress = (Button) mProgressView.findViewById(R.id.btnCancelProgress);
        imgCloseProgress = (ImageView) mProgressView.findViewById(R.id.imgCloseProgress);
    }

    public static void processingFinished(Context context, String str, String str2, String str3) {
        tvDownloadPercent.setVisibility(View.INVISIBLE);
        progressDownloading.setVisibility(View.INVISIBLE);
        imgPdfSuccess.setVisibility(View.VISIBLE);
        imgCloseProgress.setVisibility(View.VISIBLE);
        btnOpenPdfFile.setVisibility(View.VISIBLE);
        btnCancelProgress.setVisibility(View.GONE);
        StringBuilder sb = new StringBuilder();
        sb.append(context.getString(R.string.saved_to));
        sb.append(" ");
        sb.append(str3);
        String sb2 = sb.toString();
        if (!TextUtils.isEmpty(str)) {
            tvCurrentAction.setText(str);
        }
        if (!TextUtils.isEmpty(str2)) {
            tvDescription.setText(str2);
            tvDescription.setVisibility(View.VISIBLE);
        }
        tvSavedPdfPath.setText(sb2);
    }

    public static void closeProgressView(Context context) {
        mProgressView.setVisibility(View.GONE);
        imgPdfSuccess.setVisibility(View.GONE);
        btnOpenPdfFile.setVisibility(View.GONE);
        imgCloseProgress.setVisibility(View.GONE);
        tvDescription.setVisibility(View.GONE);
        progressDownloading.setVisibility(View.VISIBLE);
        tvDownloadPercent.setVisibility(View.VISIBLE);
        btnCancelProgress.setVisibility(View.VISIBLE);
        progressDownloading.setProgress(0);
        tvDownloadPercent.setText("0%");
        tvDescription.setText("");
        tvSavedPdfPath.setText("");
        Utils.clearLightStatusBar(context);
    }

    public static void showInterstialAd(Context context, String str, String str2, String str3) {

        final Context context2 = context;
        final String str4 = str;
        final String str5 = str2;
        final String str6 = str3;
        processingFinished(context2, str4, str5, str6);

    }


    public static void updateProgressPercent(int i, int i2) {
        int i3 = ((int) (((float) i) * 100.0f)) / i2;
        StringBuilder sb = new StringBuilder();
        sb.append(i3);
        sb.append("%");
        tvDownloadPercent.setText(sb.toString());
        progressDownloading.setProgress(i);
    }

    public static void setupOpenPath(final Context context, String str, final String str2, final boolean z) {
        btnOpenPdfFile.setText(str);
        btnOpenPdfFile.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (z) {
                    File file = new File(str2);
                    Intent intent = new Intent(context, PDFViewerActivity.class);
                    intent.putExtra(BrowsePDFActivity.PDF_LOCATION, file.getAbsolutePath());
                    String access$600 = PDFTools.TAG;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Open PDF from location ");
                    sb.append(file.getAbsolutePath());
                    Log.d(access$600, sb.toString());
                    context.startActivity(intent);
                    return;
                }
                Intent intent2 = new Intent(context, SelectPDFActivity.class);
                intent2.putExtra(PDFToolsActivity.IS_DIRECTORY, true);
                context.startActivity(intent2.putExtra(PDFToolsActivity.DIRECTORY_PATH, str2));
            }
        });
    }

    public static void openImageDirectory(final Context context, String str, final String str2) {
        btnOpenPdfFile.setText(str);
        btnOpenPdfFile.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(context, ViewImagesActivity.class);
                intent.putExtra(ViewImagesActivity.GENERATED_IMAGES_PATH, str2);
                context.startActivity(intent);
            }
        });
    }

    public static void removeProgressBarIndeterminate(Context context, final ProgressBar progressBar2) {
        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                progressBar2.setIndeterminate(false);
            }
        });
    }
}
