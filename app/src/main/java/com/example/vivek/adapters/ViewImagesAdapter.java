package com.example.vivek.adapters;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build.VERSION;
import androidx.annotation.RequiresApi;
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

import com.example.vivek.R;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ViewImagesAdapter extends Adapter<ViewImagesAdapter.ViewImagesViewHolder> {

    public ActionMode actionMode;
    private ActionModeCallback actionModeCallback;
    private List<Uri> listImageUris;
    public Context context;
    private SparseBooleanArray selectedPages = new SparseBooleanArray();


    public class ViewImagesViewHolder extends ViewHolder {
        ImageView imgMainPic;

        private ViewImagesViewHolder(View view) {
            super(view);
            this.imgMainPic = (ImageView) view.findViewById(R.id.imgMainPic);
        }
    }

    public ViewImagesAdapter(Context context, List<Uri> listUri) {
        this.listImageUris = listUri;
        this.context = context;
        actionModeCallback = new ActionModeCallback();
    }

    public ViewImagesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewImagesViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_view_image, viewGroup, false));
    }

    public void onBindViewHolder(ViewImagesViewHolder viewImagesViewHolder, int i) {
        Picasso.with(this.context).load((Uri) this.listImageUris.get(i)).into((ImageView) viewImagesViewHolder.imgMainPic);
        viewImagesViewHolder.imgMainPic.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
            }
        });
    }



    public int getItemCount() {
        return this.listImageUris.size();
    }

    public void removeImagePdf() {
        List<Integer> imagePdf = selectedPdfPage();
        this.selectedPages.clear();
        for (Integer intValue : imagePdf) {
            notifyItemChanged(intValue.intValue());
        }
    }


    public List<Integer> selectedPdfPage() {
        int size = this.selectedPages.size();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < size; i++) {
            arrayList.add(Integer.valueOf(this.selectedPages.keyAt(i)));
        }
        return arrayList;
    }

    private void removeImageItem(int i) {
        this.listImageUris.remove(i);
        notifyItemRemoved(i);
    }

    public void removeImagePdfItems(List<Integer> list) {
        Collections.sort(list, new Comparator<Integer>() {
            public int compare(Integer num, Integer num2) {
                return num2.intValue() - num.intValue();
            }
        });
        while (!list.isEmpty()) {
            if (list.size() == 1) {
                removeImageItem(((Integer) list.get(0)).intValue());
                list.remove(0);
            } else {
                int i = 1;
                while (list.size() > i && ((Integer) list.get(i)).equals(Integer.valueOf(((Integer) list.get(i - 1)).intValue() - 1))) {
                    i++;
                }
                if (i == 1) {
                    removeImageItem(((Integer) list.get(0)).intValue());
                } else {
                    removePdfImageRange(((Integer) list.get(i - 1)).intValue(), i);
                }
                for (int i2 = 0; i2 < i; i2++) {
                    list.remove(0);
                }
            }
        }
    }

    private void removePdfImageRange(int i, int i2) {
        for (int i3 = 0; i3 < i2; i3++) {
            this.listImageUris.remove(i);
        }
        notifyItemRangeRemoved(i, i2);
    }


    private class ActionModeCallback implements Callback {
        int colorFrom;
        int colorTo;
        int flags;
        View view;

        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        private ActionModeCallback() {
            this.view = ((Activity) ViewImagesAdapter.this.context).getWindow().getDecorView();
            this.flags = this.view.getSystemUiVisibility();
            this.colorFrom = ViewImagesAdapter.this.context.getResources().getColor(R.color.colorPrimaryDark);
            this.colorTo = ViewImagesAdapter.this.context.getResources().getColor(R.color.colorDarkerGray);
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
                        ((Activity) ViewImagesAdapter.this.context).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                ofObject.start();
            }
            return true;
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.action_delete) {
                ViewImagesAdapter viewImagesAdapter = ViewImagesAdapter.this;
                viewImagesAdapter.removeImagePdfItems(viewImagesAdapter.selectedPdfPage());
                actionMode.finish();
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode actionMode) {
            ViewImagesAdapter.this.removeImagePdf();
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
                        ((Activity) ViewImagesAdapter.this.context).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                ofObject.start();
            }
            ViewImagesAdapter.this.actionMode = null;
        }
    }

}
