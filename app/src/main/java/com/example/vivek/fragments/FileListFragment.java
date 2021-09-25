package com.example.vivek.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.example.vivek.BrowsePDFActivity;
import com.example.vivek.R;
import com.example.vivek.adapters.FileBrowserAdapter;
import com.example.vivek.adapters.FileBrowserAdapter.OnPdfClickListener;
import com.example.vivek.models.PdfDataType;
import com.example.vivek.utils.Utils;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileListFragment extends Fragment implements OnPdfClickListener {
    private static final String FILE_PATH = "file_path";
    LinearLayout layNoBrowserData;
    ProgressBar progressBrowsePdf;
    RecyclerView recyclerBrowsePdf;

    String thumbleDir;
    FileBrowserAdapter fileBrowserAdapter;
    Context context;
    List<PdfDataType> listPdfdir = new ArrayList();
    boolean isGridViewEnabled;
    public String filePath;
    int numberOfColumns;




    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.context = getContext();
        if (getArguments() != null) {
            this.filePath = getArguments().getString(FILE_PATH);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.context.getCacheDir());
            stringBuilder.append("/Thumbnails/");
            this.thumbleDir = stringBuilder.toString();
            int i = Utils.isTablet(this.context) ? 6 : 3;
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
            this.numberOfColumns = defaultSharedPreferences.getInt(BrowsePDFActivity.GRID_VIEW_NUM_OF_COLUMNS, i);
            this.isGridViewEnabled = defaultSharedPreferences.getBoolean(BrowsePDFActivity.GRID_VIEW_ENABLED, false);
        }
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.recyclerBrowsePdf = (RecyclerView) view.findViewById(R.id.recyclerBrowsePdf);
        this.progressBrowsePdf = (ProgressBar) view.findViewById(R.id.progressBrowsePdf);
        this.layNoBrowserData = (LinearLayout) view.findViewById(R.id.layNoBrowserData);
        new listPdfFileDirectory().execute(new Void[0]);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_file_list, viewGroup, false);
    }

    public void onAttach(Context context2) {
        super.onAttach(context2);
    }

    public void onDetach() {
        super.onDetach();
    }

    public List<PdfDataType> getAllFilesList(String str) {
        Uri uri;
        File file = new File(str);
        ArrayList arrayList = new ArrayList();
        if (file.isDirectory()) {
            File[] listFiles = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    String fileExtensionFromUrl = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
                    return (file.isDirectory() && !file.isHidden()) || TextUtils.equals(fileExtensionFromUrl, "pdf") || TextUtils.equals(fileExtensionFromUrl, "PDF");
                }
            });
            if (listFiles != null) {
                for (File file2 : listFiles) {
                    if (!file2.isDirectory()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(this.thumbleDir);
                        sb.append(Utils.removePdfExtension(file2.getName()));
                        sb.append(".jpg");
                        uri = Utils.getImageUriFromPath(sb.toString());
                    } else {
                        uri = null;
                    }
                    int size = file2.isDirectory() ? getAllFilesList(file2.getAbsolutePath()).size() : 0;
                    PdfDataType pdfDataType = new PdfDataType();
                    pdfDataType.setName(file2.getName());
                    pdfDataType.setAbsolutePath(file2.getAbsolutePath());
                    pdfDataType.setPdfUri(Uri.fromFile(file2));
                    pdfDataType.setLength(Long.valueOf(file2.length()));
                    pdfDataType.setLastModified(Long.valueOf(file2.lastModified()));
                    pdfDataType.setThumbUri(uri);
                    pdfDataType.setDirectory(file2.isDirectory());
                    pdfDataType.setNumItems(size);
                    arrayList.add(pdfDataType);
                }
            }
        }
        Collections.sort(arrayList, new Comparator<PdfDataType>() {
            public int compare(PdfDataType pdfDataType, PdfDataType pdfDataType2) {
                if (pdfDataType.isDirectory() && !pdfDataType2.isDirectory()) {
                    return -1;
                }
                if (pdfDataType.isDirectory() || !pdfDataType2.isDirectory()) {
                    return pdfDataType.getName().compareToIgnoreCase(pdfDataType2.getName());
                }
                return 1;
            }
        });
        return arrayList;
    }

    public class listPdfFileDirectory extends AsyncTask<Void, Void, Void> {

        public void onPreExecute() {
            super.onPreExecute();
            FileListFragment.this.progressBrowsePdf.setVisibility(View.VISIBLE);
            FileListFragment fileListFragment = FileListFragment.this;
            if (fileListFragment.isGridViewEnabled) {
                fileListFragment.recyclerBrowsePdf.setBackgroundColor(fileListFragment.getResources().getColor(R.color.colorLightGray));
                FileListFragment fileListFragment2 = FileListFragment.this;
                fileListFragment2.recyclerBrowsePdf.setLayoutManager(new GridLayoutManager(fileListFragment2.context, fileListFragment2.numberOfColumns, 1, false));
                return;
            }
            fileListFragment.recyclerBrowsePdf.setBackgroundColor(fileListFragment.getResources().getColor(17170443));
            FileListFragment fileListFragment3 = FileListFragment.this;
            fileListFragment3.recyclerBrowsePdf.setLayoutManager(new LinearLayoutManager(fileListFragment3.context, 1, false));
        }

        public Void doInBackground(Void... voidArr) {
            FileListFragment fileListFragment = FileListFragment.this;
            fileListFragment.listPdfdir = fileListFragment.getAllFilesList(fileListFragment.filePath);
            FileListFragment fileListFragment2 = FileListFragment.this;
            fileListFragment2.fileBrowserAdapter = new FileBrowserAdapter(fileListFragment2.context, fileListFragment2.listPdfdir);
            return null;
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            FileListFragment.this.progressBrowsePdf.setVisibility(View.GONE);
            FileListFragment fileListFragment = FileListFragment.this;
            fileListFragment.recyclerBrowsePdf.setAdapter(fileListFragment.fileBrowserAdapter);
            if (FileListFragment.this.listPdfdir.size() == 0) {
                FileListFragment.this.layNoBrowserData.setVisibility(View.VISIBLE);
            } else {
                FileListFragment.this.layNoBrowserData.setVisibility(View.GONE);
            }
        }
    }

    public void onPdfClicked(PdfDataType pdfDataType) {
    }

    public static FileListFragment newInstance(String str) {
        FileListFragment fileListFragment = new FileListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(FILE_PATH, str);
        fileListFragment.setArguments(bundle);
        return fileListFragment;
    }


}
