package com.example.vivek.adapters;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.text.format.Formatter;
import android.util.Log;
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
import com.example.vivek.fragments.BottomSheetDialogFragment;
import com.example.vivek.models.PdfDataType;
import com.example.vivek.utils.Utils;
import com.squareup.picasso.Picasso;
import java.util.List;
import org.greenrobot.eventbus.EventBus;

public class StarredPDFAdapter extends Adapter<StarredPDFAdapter.SharedPDFViewHolder> {

    public final String TAG = StarredPDFAdapter.class.getSimpleName();
    String THUMBNAILS_DIR;
    public boolean isGridViewEnabled;
    public Context mContext;
    private List<PdfDataType> pdfDataTypeFiles;
    private OnStaredPdfClickListener staredPdfClickListener;

    public interface OnStaredPdfClickListener {
        void onStaredPdfClicked(PdfDataType pdfDataType);
    }

    public class SharedPDFViewHolder extends ViewHolder {

        public TextView tvPdfSize;
        public TextView tvLastPdfModified;
        public TextView tvPdfTitle;
        public ImageView imgPdfImage;
        public RelativeLayout rLayPdf;
        public ImageView imgStar;

        private SharedPDFViewHolder(View view) {
            super(view);
            if (StarredPDFAdapter.this.isGridViewEnabled) {
                this.imgPdfImage = (ImageView) view.findViewById(R.id.imgPdfImage);
            }
            this.tvPdfTitle = (TextView) view.findViewById(R.id.tvPdfTitle);
            this.tvLastPdfModified = (TextView) view.findViewById(R.id.tvLastPdfModified);
            this.tvPdfSize = (TextView) view.findViewById(R.id.tvPdfSize);
            this.imgStar = (ImageView) view.findViewById(R.id.imgStar);
            this.rLayPdf = (RelativeLayout) view.findViewById(R.id.rLayPdf);
        }
    }

    public StarredPDFAdapter(Context context, List<PdfDataType> list) {
        this.pdfDataTypeFiles = list;
        this.mContext = context;
        StringBuilder sb = new StringBuilder();
        sb.append(context.getCacheDir());
        sb.append("/Thumbnails/");
        THUMBNAILS_DIR = sb.toString();
        this.isGridViewEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(BrowsePDFActivity.GRID_VIEW_ENABLED, false);
        Context context2 = this.mContext;
        if (context2 instanceof OnStaredPdfClickListener) {
            this.staredPdfClickListener = (OnStaredPdfClickListener) context2;
            return;
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append(this.mContext.toString());
        sb2.append(" must implement OnStaredPdfClickListener");
        throw new RuntimeException(sb2.toString());
    }

    public SharedPDFViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view;
        if (this.isGridViewEnabled) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_pdf_grid, viewGroup, false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_pdf, viewGroup, false);
        }
        return new SharedPDFViewHolder(view);
    }

    public void onBindViewHolder(final SharedPDFViewHolder sharedPDFViewHolder, int i) {
        PdfDataType pdfDataType = (PdfDataType) this.pdfDataTypeFiles.get(i);
        final String absolutePath = pdfDataType.getAbsolutePath();
        String name = pdfDataType.getName();
        Long length = pdfDataType.getLength();
        final DbHelper instance = DbHelper.getInstance(this.mContext);
        sharedPDFViewHolder.tvPdfTitle.setText(name);
        sharedPDFViewHolder.tvPdfSize.setText(Formatter.formatShortFileSize(this.mContext, length.longValue()));
        sharedPDFViewHolder.tvLastPdfModified.setText(Utils.formatDateToHumanReadable(pdfDataType.getLastModified()));
        if (pdfDataType.isStarred()) {
            sharedPDFViewHolder.imgStar.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.ic_action_star_yellow));
        }
        if (this.isGridViewEnabled) {
            Picasso.with(this.mContext).load(pdfDataType.getThumbUri()).into((ImageView) sharedPDFViewHolder.imgPdfImage);
        }
        sharedPDFViewHolder.imgStar.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (instance.isStared(absolutePath)) {
                    instance.removeStaredPDF(absolutePath);
                    sharedPDFViewHolder.imgStar.setImageDrawable(StarredPDFAdapter.this.mContext.getResources().getDrawable(R.drawable.ic_action_star));
                } else {
                    instance.addStaredPDF(absolutePath);
                    sharedPDFViewHolder.imgStar.setImageDrawable(StarredPDFAdapter.this.mContext.getResources().getDrawable(R.drawable.ic_action_star_yellow));
                }
                EventBus.getDefault().post(new RecentPDFStaredEvent());
            }
        });
        sharedPDFViewHolder.rLayPdf.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                int adapterPosition = sharedPDFViewHolder.getAdapterPosition();
                StarredPDFAdapter.this.staredPdfClicked(adapterPosition);
                String access$900 = StarredPDFAdapter.this.TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("PdfDataType ");
                sb.append(adapterPosition);
                sb.append(" clicked");
                Log.d(access$900, sb.toString());
            }
        });
        sharedPDFViewHolder.rLayPdf.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View view) {
                StarredPDFAdapter.this.showBottomSheet(sharedPDFViewHolder.getAdapterPosition());
                return true;
            }
        });
    }

    public int getItemCount() {
        return this.pdfDataTypeFiles.size();
    }

    public void filter(List<PdfDataType> list) {
        this.pdfDataTypeFiles = list;
        notifyDataSetChanged();
    }


    public void staredPdfClicked(int i) {
        OnStaredPdfClickListener onStaredPdfClickListener = this.staredPdfClickListener;
        if (onStaredPdfClickListener != null) {
            onStaredPdfClickListener.onStaredPdfClicked((PdfDataType) this.pdfDataTypeFiles.get(i));
        }
    }


    public void showBottomSheet(int i) {
        String absolutePath = ((PdfDataType) this.pdfDataTypeFiles.get(i)).getAbsolutePath();
        Bundle bundle = new Bundle();
        bundle.putString("com.example.pdfreader.FROM_RECENT", absolutePath);
        bundle.putBoolean("fromRecent", true);
//        bundle.putString("com.example.pdfreader.PDF_PATH", absolutePath);
//        bundle.putBoolean(BottomSheetDialogFragment.FROM_RECENT, true);
        BottomSheetDialogFragment bottomSheetDialogFragment = new BottomSheetDialogFragment();
        bottomSheetDialogFragment.setArguments(bundle);
        bottomSheetDialogFragment.show(((AppCompatActivity) this.mContext).getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
    }
}
