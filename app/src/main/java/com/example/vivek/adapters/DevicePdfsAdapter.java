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
import com.example.vivek.DataUpdatedEvent.DevicePDFStaredEvent;
import com.example.vivek.R;
import com.example.vivek.data.DbHelper;
import com.example.vivek.data.FileDiffCallback;
import com.example.vivek.fragments.BottomSheetDialogFragment;
import com.example.vivek.models.PdfDataType;
import com.example.vivek.utils.Utils;

import java.util.List;
import org.greenrobot.eventbus.EventBus;

public class DevicePdfsAdapter extends Adapter<DevicePdfsAdapter.DevicePdfFileViewHolder> {

    public DbHelper dbHelper;
    public boolean isDevicePdfGridEnabled;
    public Context context;
    private OnPdfClickListener pdfClickListener;
    private List<PdfDataType> listDevidePdfDataTypeFiles;

    public interface OnPdfClickListener {
        void onPdfClicked(PdfDataType pdfDataType);
    }

    public class DevicePdfFileViewHolder extends ViewHolder {

        public TextView tvPdfSize, tvLastPdfModified, tvPdfTitle;
        public ImageView imgPdfImage,imgStar;
        public RelativeLayout rLayPdf;

        private DevicePdfFileViewHolder(View view) {
            super(view);
            if (DevicePdfsAdapter.this.isDevicePdfGridEnabled) {
                this.imgPdfImage = (ImageView) view.findViewById(R.id.imgPdfImage);
            }
            this.tvPdfTitle = (TextView) view.findViewById(R.id.tvPdfTitle);
            this.tvLastPdfModified = (TextView) view.findViewById(R.id.tvLastPdfModified);
            this.tvPdfSize = (TextView) view.findViewById(R.id.tvPdfSize);
            this.imgStar = (ImageView) view.findViewById(R.id.imgStar);
            this.rLayPdf = (RelativeLayout) view.findViewById(R.id.rLayPdf);
        }
    }

    public DevicePdfsAdapter(List<PdfDataType> devicePdflist, Context context) {
        this.listDevidePdfDataTypeFiles = devicePdflist;
        this.context = context;
        this.isDevicePdfGridEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(BrowsePDFActivity.GRID_VIEW_ENABLED, false);
        this.dbHelper = DbHelper.getInstance(this.context);
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

    public DevicePdfFileViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view;
        if (this.isDevicePdfGridEnabled) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_pdf_grid, viewGroup, false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_pdf, viewGroup, false);
        }
        return new DevicePdfFileViewHolder(view);
    }

    public void onBindViewHolder(final DevicePdfFileViewHolder devicePdfFileViewHolder, int i) {
        PdfDataType pdfDataType = (PdfDataType) this.listDevidePdfDataTypeFiles.get(i);
        String fileName = pdfDataType.getName();
        Long pfdSize = pdfDataType.getLength();
        final String absolutePdfPath = pdfDataType.getAbsolutePath();
        devicePdfFileViewHolder.tvPdfTitle.setText(fileName);
        devicePdfFileViewHolder.tvPdfSize.setText(Formatter.formatShortFileSize(this.context, pfdSize.longValue()));
        devicePdfFileViewHolder.tvLastPdfModified.setText(Utils.formatDateToHumanReadable(pdfDataType.getLastModified()));
        if (pdfDataType.isStarred()) {
            devicePdfFileViewHolder.imgStar.setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_action_star_yellow));
        } else {
            devicePdfFileViewHolder.imgStar.setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_action_star));
        }
        if (this.isDevicePdfGridEnabled) {
//            Picasso.with(this.context).load(pdfDataType.getThumbUri()).into((ImageView) devicePdfFileViewHolder.imgPdfImage);
        }
        devicePdfFileViewHolder.imgStar.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (DevicePdfsAdapter.this.dbHelper.isStared(absolutePdfPath)) {
                    DevicePdfsAdapter.this.dbHelper.removeStaredPDF(absolutePdfPath);
                    devicePdfFileViewHolder.imgStar.setImageDrawable(DevicePdfsAdapter.this.context.getResources().getDrawable(R.drawable.ic_action_star));
                } else {
                    DevicePdfsAdapter.this.dbHelper.addStaredPDF(absolutePdfPath);
                    devicePdfFileViewHolder.imgStar.setImageDrawable(DevicePdfsAdapter.this.context.getResources().getDrawable(R.drawable.ic_action_star_yellow));
                }
                EventBus.getDefault().post(new DevicePDFStaredEvent());
            }
        });
        devicePdfFileViewHolder.rLayPdf.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                int adapterPosition = devicePdfFileViewHolder.getAdapterPosition();
                DevicePdfsAdapter.this.pdfClicked(adapterPosition);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("PdfDataType ");
                stringBuilder.append(adapterPosition);
                stringBuilder.append(" clicked");

            }
        });
        devicePdfFileViewHolder.rLayPdf.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View view) {
                if (devicePdfFileViewHolder.getAdapterPosition() >= 0) {
                    DevicePdfsAdapter.this.showBottomPDFFile(devicePdfFileViewHolder.getAdapterPosition());
                }
                return true;
            }
        });
    }

    public int getItemCount() {
        return this.listDevidePdfDataTypeFiles.size();
    }


    public void pdfClicked(int i) {
        OnPdfClickListener onPdfClickListener = this.pdfClickListener;
        if (onPdfClickListener != null && i >= 0) {
            onPdfClickListener.onPdfClicked((PdfDataType) this.listDevidePdfDataTypeFiles.get(i));
        }
    }

    public void updatePdfData(List<PdfDataType> list) {
        DiffUtil.calculateDiff(new FileDiffCallback(this.listDevidePdfDataTypeFiles, list)).dispatchUpdatesTo((Adapter) this);
        this.listDevidePdfDataTypeFiles = list;
    }

    public void filter(List<PdfDataType> list) {
        this.listDevidePdfDataTypeFiles = list;
        notifyDataSetChanged();
    }


    public void showBottomPDFFile(int i) {
        String absolutePdfFilePath = ((PdfDataType) this.listDevidePdfDataTypeFiles.get(i)).getAbsolutePath();
        Bundle bundle = new Bundle();
        bundle.putString("com.example.pdfreader.FROM_RECENT", absolutePdfFilePath);
        bundle.putBoolean("fromRecent", true);
//        bundle.putString("com.example.pdfreader.PDF_PATH", absolutePdfFilePath);
//        bundle.putBoolean(BottomSheetDialogFragment.FROM_RECENT, false);
        BottomSheetDialogFragment bottomSheetDialogFragment = new BottomSheetDialogFragment();
        bottomSheetDialogFragment.setArguments(bundle);
        bottomSheetDialogFragment.show(((AppCompatActivity) this.context).getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
    }

}
