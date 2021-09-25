package com.example.vivek.adapters;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.os.Build.VERSION;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.view.ActionMode.Callback;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.vivek.R;
import com.example.vivek.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MergeOrganalPDFAdp extends Adapter<MergeOrganalPDFAdp.MergePdfOrignalPagesViewHolder> {

    static String thmubImagePath;
    public ActionMode actionMode;
    public ActionModeCallback actionModeCallback;
    public Context context;
    private List<File> listMergePDFFile;
    private SparseBooleanArray mergeOrgSelectedPages = new SparseBooleanArray();


    public class MergePdfOrignalPagesViewHolder extends ViewHolder {

        public TextView tvPdfFileNamw;
        LinearLayout layOrgnalPdfPage;
        public RelativeLayout rLayMain;
        ImageView imgPdfImage;

        public MergePdfOrignalPagesViewHolder(View view) {
            super(view);
            this.rLayMain = (RelativeLayout) view.findViewById(R.id.rLayMain);
            this.tvPdfFileNamw = (TextView) view.findViewById(R.id.tvPdfFileNamw);
            this.imgPdfImage = (ImageView) view.findViewById(R.id.imgPdfImage);
            this.layOrgnalPdfPage = (LinearLayout) view.findViewById(R.id.layOrgnalPdfPage);
        }
    }

    public MergeOrganalPDFAdp(Context context, List<File> list) {
        this.listMergePDFFile = list;
        this.context = context;
        this.actionModeCallback = new ActionModeCallback();
        StringBuilder sb = new StringBuilder();
        sb.append(context.getCacheDir());
        sb.append("/Thumbnails/");
        thmubImagePath = sb.toString();
        StringBuilder sb2 = new StringBuilder();
        sb2.append("number of thumbs ");
        sb2.append(list.size());
    }

    public MergePdfOrignalPagesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new MergePdfOrignalPagesViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_organize_pdfs_merge_grid, viewGroup, false));
    }

    public void onBindViewHolder(final MergePdfOrignalPagesViewHolder mergePdfOrignalPagesViewHolder, int i) {
        File file = (File) this.listMergePDFFile.get(i);
        StringBuilder sb = new StringBuilder();
        sb.append(thmubImagePath);
        sb.append(Utils.removePdfExtension(file.getName()));
        sb.append(".jpg");
        Picasso.with(this.context).load(Utils.getImageUriFromPath(sb.toString())).fit().into((ImageView) mergePdfOrignalPagesViewHolder.imgPdfImage);
        mergePdfOrignalPagesViewHolder.tvPdfFileNamw.setText(file.getName());
        selectChangeBackground(mergePdfOrignalPagesViewHolder, i);
        mergePdfOrignalPagesViewHolder.rLayMain.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                int adapterPosition = mergePdfOrignalPagesViewHolder.getAdapterPosition();
                if (MergeOrganalPDFAdp.this.actionMode == null) {
                    MergeOrganalPDFAdp mergeOrganalPDFAdp = MergeOrganalPDFAdp.this;
                    mergeOrganalPDFAdp.actionMode = ((AppCompatActivity) mergeOrganalPDFAdp.context).startSupportActionMode(MergeOrganalPDFAdp.this.actionModeCallback);
                }
                MergeOrganalPDFAdp.this.adpPosSelection(adapterPosition);
                StringBuilder sb = new StringBuilder();
                sb.append("Clicked position ");
                sb.append(adapterPosition);
            }
        });
    }

    public List<File> getPDFsToMerge() {
        return this.listMergePDFFile;
    }

    public void adpPosSelection(int i) {
        if (this.mergeOrgSelectedPages.get(i, false)) {
            this.mergeOrgSelectedPages.delete(i);
        } else {
            this.mergeOrgSelectedPages.put(i, true);
        }
        notifyItemChanged(i);
        int pdfPageSize = this.mergeOrgSelectedPages.size();
        if (pdfPageSize == 0) {
            this.actionMode.finish();
            return;
        }
        ActionMode actionMode2 = this.actionMode;
        StringBuilder sb = new StringBuilder();
        sb.append(pdfPageSize);
        sb.append(" ");
        sb.append(this.context.getString(R.string.selected));
        actionMode2.setTitle((CharSequence) sb.toString());
        this.actionMode.invalidate();
    }

    private void selectChangeBackground(MergePdfOrignalPagesViewHolder mergePdfOrignalPagesViewHolder, int i) {
        if (isSelected(i)) {
            mergePdfOrignalPagesViewHolder.layOrgnalPdfPage.setVisibility(View.VISIBLE);
        } else {
            mergePdfOrignalPagesViewHolder.layOrgnalPdfPage.setVisibility(View.GONE);
        }
    }

    private boolean isSelected(int i) {
        return getMergeOrgSelectedPages().contains(Integer.valueOf(i));
    }

    public int getItemCount() {
        return this.listMergePDFFile.size();
    }

    public void DeleteSelection() {
        List<Integer> selectedPages2 = getMergeOrgSelectedPages();
        this.mergeOrgSelectedPages.clear();
        for (Integer intValue : selectedPages2) {
            notifyItemChanged(intValue.intValue());
        }
    }

    public List<Integer> getMergeOrgSelectedPages() {
        int size = this.mergeOrgSelectedPages.size();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < size; i++) {
            arrayList.add(Integer.valueOf(this.mergeOrgSelectedPages.keyAt(i)));
        }
        return arrayList;
    }

    private void deleteMergePdf(int i) {
        this.listMergePDFFile.remove(i);
        notifyItemRemoved(i);
    }

    public void deleteMergePdfItems(List<Integer> list) {
        Collections.sort(list, new Comparator<Integer>() {
            public int compare(Integer num, Integer num2) {
                return num2.intValue() - num.intValue();
            }
        });
        while (!list.isEmpty()) {
            if (list.size() == 1) {
                deleteMergePdf(((Integer) list.get(0)).intValue());
                list.remove(0);
            } else {
                int i = 1;
                while (list.size() > i && ((Integer) list.get(i)).equals(Integer.valueOf(((Integer) list.get(i - 1)).intValue() - 1))) {
                    i++;
                }
                if (i == 1) {
                    deleteMergePdf(((Integer) list.get(0)).intValue());
                } else {
                    deleteMergePdfRange(((Integer) list.get(i - 1)).intValue(), i);
                }
                for (int i2 = 0; i2 < i; i2++) {
                    list.remove(0);
                }
            }
        }
    }

    private void deleteMergePdfRange(int i, int i2) {
        for (int i3 = 0; i3 < i2; i3++) {
            this.listMergePDFFile.remove(i);
        }
        notifyItemRangeRemoved(i, i2);
    }

    public void finishActionMode() {
        ActionMode actionMode2 = this.actionMode;
        if (actionMode2 != null) {
            actionMode2.finish();
        }
    }

    private class ActionModeCallback implements Callback {
        int colorFrom, colorTo, flags;
        View view;

        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        private ActionModeCallback() {
            this.view = ((Activity) MergeOrganalPDFAdp.this.context).getWindow().getDecorView();
            this.flags = this.view.getSystemUiVisibility();
            this.colorFrom = MergeOrganalPDFAdp.this.context.getResources().getColor(R.color.colorPrimaryDark);
            this.colorTo = MergeOrganalPDFAdp.this.context.getResources().getColor(R.color.colorDarkerGray);
        }

        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.activity_organize_pages_action_mode, menu);
            int i = VERSION.SDK_INT;
            if (i >= 21) {
                if (i >= 23) {
                    this.flags &= -8193;
                    this.view.setSystemUiVisibility(this.flags);
                }
                ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), new Object[]{Integer.valueOf(this.colorFrom), Integer.valueOf(this.colorTo)});
                ofObject.setDuration(300);
                ofObject.addUpdateListener(new AnimatorUpdateListener() {
                    @RequiresApi(api = 21)
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        ((Activity) MergeOrganalPDFAdp.this.context).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                ofObject.start();
            }
            return true;
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.action_delete) {
                MergeOrganalPDFAdp mergeOrganalPDFAdp = MergeOrganalPDFAdp.this;
                mergeOrganalPDFAdp.deleteMergePdfItems(mergeOrganalPDFAdp.getMergeOrgSelectedPages());
                actionMode.finish();
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode actionMode) {
            MergeOrganalPDFAdp.this.DeleteSelection();
            int i = VERSION.SDK_INT;
            if (i >= 21) {
                if (i >= 23) {
                    this.flags |= 8192;
                    this.view.setSystemUiVisibility(this.flags);
                }
                ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), new Object[]{Integer.valueOf(this.colorTo), Integer.valueOf(this.colorFrom)});
                ofObject.setDuration(300);
                ofObject.addUpdateListener(new AnimatorUpdateListener() {
                    @RequiresApi(api = 21)
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        ((Activity) MergeOrganalPDFAdp.this.context).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                ofObject.start();
            }
            MergeOrganalPDFAdp.this.actionMode = null;
        }
    }


}
