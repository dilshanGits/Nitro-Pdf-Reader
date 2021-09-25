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
import com.example.vivek.models.PDFPage;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ShareAsPictureAdapter extends Adapter<ShareAsPictureAdapter.OrganizePagesViewHolder> {
    public ActionMode actionMode;
    public ActionModeCallback actionModeCallback;
    public Context context;
    private List<PDFPage> listPdfImagePages;
    private SparseBooleanArray selectedPages = new SparseBooleanArray();


    public class OrganizePagesViewHolder extends ViewHolder {
        LinearLayout layOrgnizePage;
        public TextView tvPageNumber;
        public RelativeLayout rLayMain;
        ImageView imgPdfImage;

        private OrganizePagesViewHolder(View view) {
            super(view);
            this.rLayMain = (RelativeLayout) view.findViewById(R.id.rLayMain);
            this.tvPageNumber = (TextView) view.findViewById(R.id.tvPageNumber);
            this.imgPdfImage = (ImageView) view.findViewById(R.id.imgPdfImage);
            this.layOrgnizePage = (LinearLayout) view.findViewById(R.id.layOrgnizePage);
        }
    }

    public ShareAsPictureAdapter(Context context, List<PDFPage> list) {
        this.listPdfImagePages = list;
        this.context = context;
        this.actionModeCallback = new ActionModeCallback();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("number of thumbs ");
        stringBuilder.append(list.size());
    }

    public OrganizePagesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new OrganizePagesViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_organize_pages_grid, viewGroup, false));
    }

    public void onBindViewHolder(final OrganizePagesViewHolder organizePagesViewHolder, int i) {
        PDFPage pDFPage = (PDFPage) this.listPdfImagePages.get(i);
        Picasso.with(this.context).load(pDFPage.getThumbnailUri()).fit().into((ImageView) organizePagesViewHolder.imgPdfImage);
        organizePagesViewHolder.tvPageNumber.setText(String.valueOf(pDFPage.getPageNumber()));
        changePDFSelectedBGColor(organizePagesViewHolder, i);
        organizePagesViewHolder.rLayMain.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                int adapterPosition = organizePagesViewHolder.getAdapterPosition();
                if (ShareAsPictureAdapter.this.actionMode == null) {
                    ShareAsPictureAdapter shareAsPictureAdapter = ShareAsPictureAdapter.this;
                    shareAsPictureAdapter.actionMode = ((AppCompatActivity) shareAsPictureAdapter.context).startSupportActionMode(ShareAsPictureAdapter.this.actionModeCallback);
                }
                ShareAsPictureAdapter.this.getSelectedImagePdf(adapterPosition);
                StringBuilder sb = new StringBuilder();
                sb.append("Clicked position ");
                sb.append(adapterPosition);
            }
        });
    }

    public void getSelectedImagePdf(int i) {
        if (this.selectedPages.get(i, false)) {
            this.selectedPages.delete(i);
        } else {
            this.selectedPages.put(i, true);
        }
        notifyItemChanged(i);
        int size = this.selectedPages.size();
        if (size == 0) {
            this.actionMode.finish();
            return;
        }
        ActionMode actionMode2 = this.actionMode;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(size);
        stringBuilder.append(" ");
        stringBuilder.append(this.context.getString(R.string.selected));
        actionMode2.setTitle((CharSequence) stringBuilder.toString());
        this.actionMode.invalidate();
    }

    private void changePDFSelectedBGColor(OrganizePagesViewHolder organizePagesViewHolder, int i) {
        if (isSelected(i)) {
            organizePagesViewHolder.layOrgnizePage.setVisibility(View.VISIBLE);
        } else {
            organizePagesViewHolder.layOrgnizePage.setVisibility(View.GONE);
        }
    }

    private boolean isSelected(int i) {
        return getSelectedPdfImagePages().contains(Integer.valueOf(i));
    }

    public int getItemCount() {
        return this.listPdfImagePages.size();
    }

    public void clearSelectedPDF() {
        List<Integer> selectedImgPages = getSelectedPdfImagePages();
        this.selectedPages.clear();
        for (Integer intValue : selectedImgPages) {
            notifyItemChanged(intValue.intValue());
        }
    }

    public List<Integer> getSelectedPdfImagePages() {
        int size = this.selectedPages.size();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < size; i++) {
            arrayList.add(Integer.valueOf(this.selectedPages.keyAt(i)));
        }
        return arrayList;
    }

    private void deletePdfItem(int i) {
        this.listPdfImagePages.remove(i);
        notifyItemRemoved(i);
    }

    public List<PDFPage> getFinalOrganizedPages() {
        return this.listPdfImagePages;
    }


    public void deletePhotoPdfItem(List<Integer> list) {
        Collections.sort(list, new Comparator<Integer>() {
            public int compare(Integer num, Integer num2) {
                return num2.intValue() - num.intValue();
            }
        });
        while (!list.isEmpty()) {
            if (list.size() == 1) {
                deletePdfItem(((Integer) list.get(0)).intValue());
                list.remove(0);
            } else {
                int i = 1;
                while (list.size() > i && ((Integer) list.get(i)).equals(Integer.valueOf(((Integer) list.get(i - 1)).intValue() - 1))) {
                    i++;
                }
                if (i == 1) {
                    deletePdfItem(((Integer) list.get(0)).intValue());
                } else {
                    removeSeletedPdfRange(((Integer) list.get(i - 1)).intValue(), i);
                }
                for (int i2 = 0; i2 < i; i2++) {
                    list.remove(0);
                }
            }
        }
    }

    private void removeSeletedPdfRange(int i, int i2) {
        for (int i3 = 0; i3 < i2; i3++) {
            this.listPdfImagePages.remove(i);
        }
        notifyItemRangeRemoved(i, i2);
    }

    private class ActionModeCallback implements Callback {
        int colorFrom,colorTo,flags;
        View view;

        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        private ActionModeCallback() {
            this.view = ((Activity) ShareAsPictureAdapter.this.context).getWindow().getDecorView();
            this.flags = this.view.getSystemUiVisibility();
            this.colorFrom = ShareAsPictureAdapter.this.context.getResources().getColor(R.color.colorPrimaryDark);
            this.colorTo = ShareAsPictureAdapter.this.context.getResources().getColor(R.color.colorDarkerGray);
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
                        ((Activity) ShareAsPictureAdapter.this.context).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                ofObject.start();
            }
            return true;
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.action_delete) {
                ShareAsPictureAdapter shareAsPictureAdapter = ShareAsPictureAdapter.this;
                shareAsPictureAdapter.deletePhotoPdfItem(shareAsPictureAdapter.getSelectedPdfImagePages());
                actionMode.finish();
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode actionMode) {
            ShareAsPictureAdapter.this.clearSelectedPDF();
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
                        ((Activity) ShareAsPictureAdapter.this.context).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                ofObject.start();
            }
            ShareAsPictureAdapter.this.actionMode = null;
        }
    }

}
