package com.example.vivek.adapters;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.vivek.BrowsePDFActivity;
import com.example.vivek.R;
import com.example.vivek.data.FileDiffCallback;
import com.example.vivek.models.PdfDataType;
import com.example.vivek.utils.Utils;
import com.squareup.picasso.Picasso;
import java.util.List;

public class FileBrowserAdapter extends Adapter<FileBrowserAdapter.BrowsePdfFileViewHolder> {

    public int intFileFolderColor;
    public boolean isBrowserGridEnabled;
    private Context context;
    private OnPdfClickListener pdfClickListener;
    private List<PdfDataType> listPdfDataTypeFiles;



    public FileBrowserAdapter(Context context, List<PdfDataType> listPdfFile) {
        this.listPdfDataTypeFiles = listPdfFile;
        this.context = context;
        this.isBrowserGridEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(BrowsePDFActivity.GRID_VIEW_ENABLED, false);
        Context context2 = this.context;
        if (context2 instanceof OnPdfClickListener) {
            this.pdfClickListener = (OnPdfClickListener) context2;
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(this.context.toString());
        sb.append(" must implement OnPdfClickListener");
        throw new RuntimeException(sb.toString());
    }


    public class BrowsePdfFileViewHolder extends ViewHolder {

        public TextView tvPdfSize, tvPdfFilModified, tvPdfFileName;
        public ImageView imgPdfImage, imgPdfBrowse;
        public RelativeLayout rLayBrowsePdf;

        private BrowsePdfFileViewHolder(View view) {
            super(view);
            if (isBrowserGridEnabled) {
                this.imgPdfBrowse = (ImageView) view.findViewById(R.id.imgPdfBrowse);
                intFileFolderColor = Color.parseColor("#FFED8B28");
            } else {
                this.imgPdfImage = (ImageView) view.findViewById(R.id.imgPdfImage);
            }
            this.tvPdfFileName = (TextView) view.findViewById(R.id.tvPdfFileName);
            this.tvPdfFilModified = (TextView) view.findViewById(R.id.tvPdfFilModified);
            this.tvPdfSize = (TextView) view.findViewById(R.id.tvPdfSize);
            this.rLayBrowsePdf = (RelativeLayout) view.findViewById(R.id.rLayBrowsePdf);
        }
    }

    public BrowsePdfFileViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view;
        if (this.isBrowserGridEnabled) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_browse_pdf_grid, viewGroup, false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_browse_pdf, viewGroup, false);
        }
        return new BrowsePdfFileViewHolder(view);
    }

    public void onBindViewHolder(final BrowsePdfFileViewHolder browsePdfFileViewHolder, int i) {
        PdfDataType pdfDataType = (PdfDataType) this.listPdfDataTypeFiles.get(i);
        String pdfName = pdfDataType.getName();
        Long pdfSize = pdfDataType.getLength();
        browsePdfFileViewHolder.tvPdfFileName.setText(pdfName);
        if (this.isBrowserGridEnabled) {
            if (pdfDataType.isDirectory()) {
                browsePdfFileViewHolder.imgPdfBrowse.setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_folder_stacked));
                browsePdfFileViewHolder.imgPdfBrowse.setScaleType(ScaleType.FIT_XY);
                StringBuilder sb = new StringBuilder();
                sb.append(pdfDataType.getNumItems());
                sb.append(" ");
                sb.append(this.context.getString(R.string.items));
                browsePdfFileViewHolder.tvPdfSize.setText(sb.toString());
            } else {
                Picasso.with(this.context).load(pdfDataType.getThumbUri()).into((ImageView) browsePdfFileViewHolder.imgPdfBrowse);
                browsePdfFileViewHolder.tvPdfSize.setText(Formatter.formatShortFileSize(this.context, pdfSize.longValue()));
            }
        } else if (pdfDataType.isDirectory()) {
            browsePdfFileViewHolder.imgPdfImage.setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_folder_closed));
            StringBuilder sb2 = new StringBuilder();
            sb2.append(pdfDataType.getNumItems());
            sb2.append(" ");
            sb2.append(this.context.getString(R.string.items));
            browsePdfFileViewHolder.tvPdfFilModified.setText(sb2.toString());
        } else {
            browsePdfFileViewHolder.imgPdfImage.setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_pdf_icon));
            browsePdfFileViewHolder.tvPdfFilModified.setText(Utils.formatDateToHumanReadable(pdfDataType.getLastModified()));
            browsePdfFileViewHolder.tvPdfSize.setText(Formatter.formatShortFileSize(this.context, pdfSize.longValue()));
        }
        browsePdfFileViewHolder.rLayBrowsePdf.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                browsePdfClicked(browsePdfFileViewHolder.getAdapterPosition());
            }
        });
    }

    public interface OnPdfClickListener {
        void onPdfClicked(PdfDataType pdfDataType);
    }

    public int getItemCount() {
        return this.listPdfDataTypeFiles.size();
    }

    public void browsePdfClicked(int i) {
        OnPdfClickListener onPdfClickListener = this.pdfClickListener;
        if (onPdfClickListener != null) {
            onPdfClickListener.onPdfClicked((PdfDataType) this.listPdfDataTypeFiles.get(i));
        }
    }

    public void updateData(List<PdfDataType> list) {
        DiffUtil.calculateDiff(new FileDiffCallback(this.listPdfDataTypeFiles, list)).dispatchUpdatesTo((Adapter) this);
        this.listPdfDataTypeFiles = list;
    }
}
