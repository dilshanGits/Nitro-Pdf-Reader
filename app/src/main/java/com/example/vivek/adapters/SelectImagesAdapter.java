package com.example.vivek.adapters;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
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

import com.example.vivek.R;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class SelectImagesAdapter extends Adapter<SelectImagesAdapter.SelectPDFViewHolder> {

    public ActionMode actionMode;
    public ActionModeCallback actionModeCallback;
    private List<Uri> listImageUris;
    public Context context;
    private OnImageSelectedListener onImageSelectedListener;
    private SparseBooleanArray selectedImages = new SparseBooleanArray();


    public class SelectPDFViewHolder extends ViewHolder {

        public LinearLayout laySelectedPhoto;
        public ImageView imgThumbPdf;

        public SelectPDFViewHolder(View view) {
            super(view);
            this.imgThumbPdf = (ImageView) view.findViewById(R.id.imgThumbPdf);
            this.laySelectedPhoto = (LinearLayout) view.findViewById(R.id.laySelectedPhoto);
        }
    }

    public SelectImagesAdapter(Context context, List<Uri> listImgUris) {
        this.listImageUris = listImgUris;
        this.context = context;
        this.actionModeCallback = new ActionModeCallback();
        Context context2 = this.context;
        if (context2 instanceof OnImageSelectedListener) {
            this.onImageSelectedListener = (OnImageSelectedListener) context2;
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.context.toString());
        stringBuilder.append(" must implement OnImageSelectedListener");
        throw new RuntimeException(stringBuilder.toString());
    }

    public SelectPDFViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new SelectPDFViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_select_images_grid, viewGroup, false));
    }

    public void onBindViewHolder(final SelectPDFViewHolder selectPDFViewHolder, int i) {
        Uri uri = (Uri) this.listImageUris.get(i);
        backgroundSelection(selectPDFViewHolder, i);
        Picasso.with(this.context).load(uri).fit().centerCrop().into((ImageView) selectPDFViewHolder.imgThumbPdf);
        selectPDFViewHolder.imgThumbPdf.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (SelectImagesAdapter.this.actionMode == null) {
                    SelectImagesAdapter selectImagesAdapter = SelectImagesAdapter.this;
                    selectImagesAdapter.actionMode = ((AppCompatActivity) selectImagesAdapter.context).startSupportActionMode(SelectImagesAdapter.this.actionModeCallback);
                }
                SelectImagesAdapter.this.selectImageAdpPosition(selectPDFViewHolder.getAdapterPosition());
            }
        });
    }

    public interface OnImageSelectedListener {
        void onMultiSelectedPDF(ArrayList<String> arrayList);
    }

    public void selectImageAdpPosition(int i) {
        if (this.selectedImages.get(i, false)) {
            this.selectedImages.delete(i);
        } else {
            this.selectedImages.put(i, true);
        }
        notifyItemChanged(i);
        int selectedItemCount = getSelectedItemCount();
        if (selectedItemCount == 0) {
            this.actionMode.finish();
            return;
        }
        ActionMode actionMode = this.actionMode;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(selectedItemCount);
        stringBuilder.append(" ");
        stringBuilder.append(this.context.getString(R.string.selected));
        actionMode.setTitle((CharSequence) stringBuilder.toString());
        this.actionMode.invalidate();
    }

    private int getSelectedItemCount() {
        return this.selectedImages.size();
    }

    private List<Integer> selectedImagesSize() {
        int imageSize = this.selectedImages.size();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < imageSize; i++) {
            arrayList.add(Integer.valueOf(this.selectedImages.keyAt(i)));
        }
        return arrayList;
    }


    public void imageUnselect() {
        List<Integer> selectedImages2 = selectedImagesSize();
        this.selectedImages.clear();
        for (Integer intValue : selectedImages2) {
            notifyItemChanged(intValue.intValue());
        }
    }

    private boolean isSelected(int i) {
        return selectedImagesSize().contains(Integer.valueOf(i));
    }

    private void backgroundSelection(SelectPDFViewHolder selectPDFViewHolder, int i) {
        if (isSelected(i)) {
            selectPDFViewHolder.laySelectedPhoto.setVisibility(View.VISIBLE);
        } else {
            selectPDFViewHolder.laySelectedPhoto.setVisibility(View.INVISIBLE);
        }
    }


    public ArrayList<String> selectedImages() {
        List<Integer> selectedImages2 = selectedImagesSize();
        ArrayList<String> arrayList = new ArrayList<>();
        for (Integer intValue : selectedImages2) {
            arrayList.add(((Uri) this.listImageUris.get(intValue.intValue())).toString());
        }
        return arrayList;
    }

    public int getItemCount() {
        return this.listImageUris.size();
    }


    public void multiSelectedPDF(ArrayList<String> arrayList) {
        OnImageSelectedListener onImageSelectedListener = this.onImageSelectedListener;
        if (onImageSelectedListener != null) {
            onImageSelectedListener.onMultiSelectedPDF(arrayList);
        }
    }


    private class ActionModeCallback implements Callback {
        int colorFrom,colorTo, flags;
        View view;

        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        private ActionModeCallback() {
            this.view = ((Activity) SelectImagesAdapter.this.context).getWindow().getDecorView();
            this.flags = this.view.getSystemUiVisibility();
            this.colorFrom = SelectImagesAdapter.this.context.getResources().getColor(R.color.colorPrimaryDark);
            this.colorTo = SelectImagesAdapter.this.context.getResources().getColor(R.color.colorDarkerGray);
        }

        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.selected_pdfs, menu);
            int i = VERSION.SDK_INT;
            if (i >= 21) {
                if (i >= 23) {
                    this.flags &= -8193;
                    this.view.setSystemUiVisibility(this.flags);
                }
                ValueAnimator valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), new Object[]{Integer.valueOf(this.colorFrom), Integer.valueOf(this.colorTo)});
                valueAnimator.setDuration(300);
                valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
                    @RequiresApi(api = 21)
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        ((Activity) SelectImagesAdapter.this.context).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                valueAnimator.start();
            }
            return true;
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.action_select) {
                SelectImagesAdapter selectImagesAdapter = SelectImagesAdapter.this;
                selectImagesAdapter.multiSelectedPDF(selectImagesAdapter.selectedImages());
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode actionMode) {
            SelectImagesAdapter.this.imageUnselect();
            int i = VERSION.SDK_INT;
            if (i >= 21) {
                if (i >= 23) {
                    this.flags |= 8192;
                    this.view.setSystemUiVisibility(this.flags);
                }
                ValueAnimator valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), new Object[]{Integer.valueOf(this.colorTo), Integer.valueOf(this.colorFrom)});
                valueAnimator.setDuration(300);
                valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
                    @RequiresApi(api = 21)
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        ((Activity) SelectImagesAdapter.this.context).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                valueAnimator.start();
            }
            SelectImagesAdapter.this.actionMode = null;
        }
    }



}
