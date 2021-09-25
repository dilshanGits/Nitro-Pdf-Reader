package com.example.vivek.adapters;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.vivek.BrowsePDFActivity;
import com.example.vivek.DataUpdatedEvent.RecentPDFStaredEvent;
import com.example.vivek.R;
import com.example.vivek.data.DbHelper;
import com.example.vivek.data.FileDiffCallback;
import com.example.vivek.fragments.BottomSheetDialogFragment;
import com.example.vivek.models.PdfDataType;
import com.example.vivek.utils.Utils;

import java.util.List;
import org.greenrobot.eventbus.EventBus;

public class RecentPdfsAdapter extends Adapter<RecentPdfsAdapter.RecentPdfViewHolder> {

    private OnHistoryPdfClickListener historyPdfClickListener;
    public boolean isReccentPdfGridEnabled;
    public Context context;
    private List<PdfDataType> listRecentPdfFile;


    public class RecentPdfViewHolder extends ViewHolder {

        public TextView tvPdfSize, tvLastPdfModified, tvPdfTitle;
        public ImageView imgPdfImage, imgStar;
        public RelativeLayout rLayPdf;

        private RecentPdfViewHolder(View view) {
            super(view);
            if (RecentPdfsAdapter.this.isReccentPdfGridEnabled) {
                this.imgPdfImage = (ImageView) view.findViewById(R.id.imgPdfImage);
            }
            this.tvPdfTitle = (TextView) view.findViewById(R.id.tvPdfTitle);
            this.tvLastPdfModified = (TextView) view.findViewById(R.id.tvLastPdfModified);
            this.tvPdfSize = (TextView) view.findViewById(R.id.tvPdfSize);
            this.imgStar = (ImageView) view.findViewById(R.id.imgStar);
            this.rLayPdf = (RelativeLayout) view.findViewById(R.id.rLayPdf);
        }
    }

    public RecentPdfsAdapter(List<PdfDataType> recentPdfData, Context context) {
        this.listRecentPdfFile = recentPdfData;
        this.context = context;
        this.isReccentPdfGridEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(BrowsePDFActivity.GRID_VIEW_ENABLED, false);
        Context context2 = this.context;
        if (context2 instanceof OnHistoryPdfClickListener) {
            this.historyPdfClickListener = (OnHistoryPdfClickListener) context2;
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(this.context.toString());
        sb.append(" must implement OnHistoryPdfClickListener");
        throw new RuntimeException(sb.toString());
    }

    public interface OnHistoryPdfClickListener {
        void onHistoryPdfClicked(PdfDataType pdfDataType);
    }

    public RecentPdfViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view;
        if (this.isReccentPdfGridEnabled) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_pdf_grid, viewGroup, false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_pdf, viewGroup, false);
        }
        return new RecentPdfViewHolder(view);
    }

    public void onBindViewHolder(final RecentPdfViewHolder recentPdfViewHolder, int i) {
        PdfDataType pdfDataType = (PdfDataType) this.listRecentPdfFile.get(i);
        final String absolutePath = pdfDataType.getAbsolutePath();
        String recentPdfName = pdfDataType.getName();
        Long recentPdfSize = pdfDataType.getLength();
        final DbHelper dbHelper = DbHelper.getInstance(this.context);
        recentPdfViewHolder.tvPdfTitle.setText(recentPdfName);
        recentPdfViewHolder.tvPdfSize.setText(Formatter.formatShortFileSize(this.context, recentPdfSize.longValue()));
        recentPdfViewHolder.tvLastPdfModified.setText(Utils.formatDateToHumanReadable(pdfDataType.getLastModified()));
        if (pdfDataType.isStarred()) {
            recentPdfViewHolder.imgStar.setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_action_star_yellow));
        } else {
            recentPdfViewHolder.imgStar.setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_action_star));
        }
        if (this.isReccentPdfGridEnabled) {
//            Picasso.with(this.context).load(pdfDataType.getThumbUri()).into((ImageView) recentPdfViewHolder.imgPdfImage);
        }
        recentPdfViewHolder.imgStar.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (dbHelper.isStared(absolutePath)) {
                    dbHelper.removeStaredPDF(absolutePath);
                    recentPdfViewHolder.imgStar.setImageDrawable(RecentPdfsAdapter.this.context.getResources().getDrawable(R.drawable.ic_action_star));
                } else {
                    dbHelper.addStaredPDF(absolutePath);
                    recentPdfViewHolder.imgStar.setImageDrawable(RecentPdfsAdapter.this.context.getResources().getDrawable(R.drawable.ic_action_star_yellow));
                }
                EventBus.getDefault().post(new RecentPDFStaredEvent());
            }
        });
        recentPdfViewHolder.rLayPdf.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                int adapterPosition = recentPdfViewHolder.getAdapterPosition();
                RecentPdfsAdapter.this.pdfClickedHistory(adapterPosition);
                StringBuilder sb = new StringBuilder();
                sb.append("PdfDataType ");
                sb.append(adapterPosition);
                sb.append(" clicked");
            }
        });
        recentPdfViewHolder.rLayPdf.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View view) {
                RecentPdfsAdapter.this.showBottomRecentPDF(recentPdfViewHolder.getAdapterPosition());
                return true;
            }
        });
    }

    public int getItemCount() {
        return this.listRecentPdfFile.size();
    }

    public void filter(List<PdfDataType> list) {
        this.listRecentPdfFile = list;
        notifyDataSetChanged();
    }


    public void pdfClickedHistory(int i) {
        OnHistoryPdfClickListener onHistoryPdfClickListener = this.historyPdfClickListener;
        if (onHistoryPdfClickListener != null && i >= 0) {
            onHistoryPdfClickListener.onHistoryPdfClicked((PdfDataType) this.listRecentPdfFile.get(i));
        }
    }

    public void updateData(List<PdfDataType> list) {
        DiffUtil.calculateDiff(new FileDiffCallback(this.listRecentPdfFile, list)).dispatchUpdatesTo((Adapter) this);
        this.listRecentPdfFile = list;
    }

    public void showBottomRecentPDF(int i) {
        String absolutePath = ((PdfDataType) this.listRecentPdfFile.get(i)).getAbsolutePath();
        Bundle bundle = new Bundle();
        bundle.putString("com.example.pdfreader.FROM_RECENT", absolutePath);
        bundle.putBoolean("fromRecent", true);
        BottomSheetDialogFragment bottomSheetDialogFragment = new BottomSheetDialogFragment();
        bottomSheetDialogFragment.setArguments(bundle);
        bottomSheetDialogFragment.show(((AppCompatActivity) this.context).getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
    }
}
