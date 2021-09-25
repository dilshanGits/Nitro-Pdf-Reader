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
import com.example.vivek.models.ImagePage;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OrganizeImagesAdapter extends Adapter<OrganizeImagesAdapter.PdfImagePagesViewHolder> {

    public ActionMode actionMode;
    public ActionModeCallback actionModeCallback;
    private List<ImagePage> listPdfImagePages;
    public Context context;
    private SparseBooleanArray imageSelectedPages = new SparseBooleanArray();


    public class PdfImagePagesViewHolder extends ViewHolder {
        LinearLayout layOrgnizePage;
        public TextView tvPageNumber;
        public RelativeLayout rLayMain;
        ImageView imgPdfImage;

        private PdfImagePagesViewHolder(View view) {
            super(view);
            this.rLayMain = (RelativeLayout) view.findViewById(R.id.rLayMain);
            this.tvPageNumber = (TextView) view.findViewById(R.id.tvPageNumber);
            this.imgPdfImage = (ImageView) view.findViewById(R.id.imgPdfImage);
            this.layOrgnizePage = (LinearLayout) view.findViewById(R.id.layOrgnizePage);
        }
    }

    public OrganizeImagesAdapter(Context context, List<ImagePage> list) {
        this.listPdfImagePages = list;
        this.context = context;
        this.actionModeCallback = new ActionModeCallback();
    }

    public PdfImagePagesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new PdfImagePagesViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_organize_pages_grid, viewGroup, false));
    }

    public void onBindViewHolder(final PdfImagePagesViewHolder pdfImagePagesViewHolder, int i) {
        ImagePage imagePage = (ImagePage) this.listPdfImagePages.get(i);
        Picasso.with(this.context).load(imagePage.getImageUri()).fit().into((ImageView) pdfImagePagesViewHolder.imgPdfImage);
        pdfImagePagesViewHolder.tvPageNumber.setText(String.valueOf(imagePage.getPageNumber()));
        selectChangeBackground(pdfImagePagesViewHolder, i);
        pdfImagePagesViewHolder.rLayMain.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                int adapterPosition = pdfImagePagesViewHolder.getAdapterPosition();
                if (OrganizeImagesAdapter.this.actionMode == null) {
                    OrganizeImagesAdapter organizeImagesAdapter = OrganizeImagesAdapter.this;
                    organizeImagesAdapter.actionMode = ((AppCompatActivity) organizeImagesAdapter.context).startSupportActionMode(OrganizeImagesAdapter.this.actionModeCallback);
                }
                OrganizeImagesAdapter.this.adpPosSelection(adapterPosition);
                StringBuilder sb = new StringBuilder();
                sb.append("Clicked position ");
                sb.append(adapterPosition);

            }
        });
    }


    public void adpPosSelection(int i) {
        if (this.imageSelectedPages.get(i, false)) {
            this.imageSelectedPages.delete(i);
        } else {
            this.imageSelectedPages.put(i, true);
        }
        notifyItemChanged(i);
        int size = this.imageSelectedPages.size();
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

    private void selectChangeBackground(PdfImagePagesViewHolder pdfImagePagesViewHolder, int i) {
        if (isSelected(i)) {
            pdfImagePagesViewHolder.layOrgnizePage.setVisibility(View.VISIBLE);
        } else {
            pdfImagePagesViewHolder.layOrgnizePage.setVisibility(View.GONE);
        }
    }

    private boolean isSelected(int i) {
        return getImageSelectedPages().contains(Integer.valueOf(i));
    }

    public int getItemCount() {
        return this.listPdfImagePages.size();
    }


    public void DeleteSelection() {
        List<Integer> selectedPages2 = getImageSelectedPages();
        this.imageSelectedPages.clear();
        for (Integer intValue : selectedPages2) {
            notifyItemChanged(intValue.intValue());
        }
    }


    public List<Integer> getImageSelectedPages() {
        int size = this.imageSelectedPages.size();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < size; i++) {
            arrayList.add(Integer.valueOf(this.imageSelectedPages.keyAt(i)));
        }
        return arrayList;
    }

    private void deleteImageFilePage(int i) {
        this.listPdfImagePages.remove(i);
        notifyItemRemoved(i);
    }


    public void deleteImagePage(List<Integer> list) {
        Collections.sort(list, new Comparator<Integer>() {
            public int compare(Integer num, Integer num2) {
                return num2.intValue() - num.intValue();
            }
        });
        while (!list.isEmpty()) {
            if (list.size() == 1) {
                deleteImageFilePage(((Integer) list.get(0)).intValue());
                list.remove(0);
            } else {
                int i = 1;
                while (list.size() > i && ((Integer) list.get(i)).equals(Integer.valueOf(((Integer) list.get(i - 1)).intValue() - 1))) {
                    i++;
                }
                if (i == 1) {
                    deleteImageFilePage(((Integer) list.get(0)).intValue());
                } else {
                    deleteImageRange(((Integer) list.get(i - 1)).intValue(), i);
                }
                for (int i2 = 0; i2 < i; i2++) {
                    list.remove(0);
                }
            }
        }
    }

    private void deleteImageRange(int i, int i2) {
        for (int i3 = 0; i3 < i2; i3++) {
            this.listPdfImagePages.remove(i);
        }
        notifyItemRangeRemoved(i, i2);
    }

    private class ActionModeCallback implements Callback {
        int colorFrom, colorTo, flags;
        View view;

        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        private ActionModeCallback() {
            this.view = ((Activity) OrganizeImagesAdapter.this.context).getWindow().getDecorView();
            this.flags = this.view.getSystemUiVisibility();
            this.colorFrom = OrganizeImagesAdapter.this.context.getResources().getColor(R.color.colorPrimaryDark);
            this.colorTo = OrganizeImagesAdapter.this.context.getResources().getColor(R.color.colorDarkerGray);
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
                        ((Activity) OrganizeImagesAdapter.this.context).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                ofObject.start();
            }
            return true;
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.action_delete) {
                OrganizeImagesAdapter organizeImagesAdapter = OrganizeImagesAdapter.this;
                organizeImagesAdapter.deleteImagePage(organizeImagesAdapter.getImageSelectedPages());
                actionMode.finish();
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode actionMode) {
            OrganizeImagesAdapter.this.DeleteSelection();
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
                        ((Activity) OrganizeImagesAdapter.this.context).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                ofObject.start();
            }
            OrganizeImagesAdapter.this.actionMode = null;
        }
    }


}
