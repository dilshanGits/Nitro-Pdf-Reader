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

public class ExtractTextsPagesAdapter extends Adapter<ExtractTextsPagesAdapter.PdfOrgnalPagesViewHolder> {

    public ActionMode actionMode;
    public SetActionBarThemeCallback setActionBarThemeCallback;
    boolean clearSelection = true;
    public Context context;
    private List<PDFPage> listPdfFilePages;
    private SparseBooleanArray selectedPages = new SparseBooleanArray();



    public class PdfOrgnalPagesViewHolder extends ViewHolder {
        LinearLayout layOrgnizePage;
        public TextView tvPageNumber;
        public RelativeLayout rLayMain;
        ImageView imgPdfImage;

        private PdfOrgnalPagesViewHolder(View view) {
            super(view);
            this.rLayMain = (RelativeLayout) view.findViewById(R.id.rLayMain);
            this.tvPageNumber = (TextView) view.findViewById(R.id.tvPageNumber);
            this.imgPdfImage = (ImageView) view.findViewById(R.id.imgPdfImage);
            this.layOrgnizePage = (LinearLayout) view.findViewById(R.id.layOrgnizePage);
        }
    }

    public ExtractTextsPagesAdapter(Context context, List<PDFPage> list) {
        this.listPdfFilePages = list;
        this.context = context;
        this.setActionBarThemeCallback = new SetActionBarThemeCallback();
        StringBuilder sb = new StringBuilder();
        sb.append("number of thumbs ");
        sb.append(list.size());
    }

    public PdfOrgnalPagesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new PdfOrgnalPagesViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_organize_pages_grid, viewGroup, false));
    }

    public void onBindViewHolder(final PdfOrgnalPagesViewHolder pdfOrgnalPagesViewHolder, int i) {
        PDFPage pDFPage = (PDFPage) this.listPdfFilePages.get(i);
        Picasso.with(this.context).load(pDFPage.getThumbnailUri()).fit().into((ImageView) pdfOrgnalPagesViewHolder.imgPdfImage);
        pdfOrgnalPagesViewHolder.tvPageNumber.setText(String.valueOf(pDFPage.getPageNumber()));
        selectChangeBackground(pdfOrgnalPagesViewHolder, i);
        pdfOrgnalPagesViewHolder.rLayMain.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                int adapterPosition = pdfOrgnalPagesViewHolder.getAdapterPosition();
                if (ExtractTextsPagesAdapter.this.actionMode == null) {
                    ExtractTextsPagesAdapter extractTextsPagesAdapter = ExtractTextsPagesAdapter.this;
                    extractTextsPagesAdapter.actionMode = ((AppCompatActivity) extractTextsPagesAdapter.context).startSupportActionMode(ExtractTextsPagesAdapter.this.setActionBarThemeCallback);
                }
                ExtractTextsPagesAdapter.this.adpPosSelection(adapterPosition);
                StringBuilder sb = new StringBuilder();
                sb.append("Clicked position ");
                sb.append(adapterPosition);
            }
        });
    }


    public void adpPosSelection(int i) {
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
        StringBuilder sb = new StringBuilder();
        sb.append(size);
        sb.append(" ");
        sb.append(this.context.getString(R.string.selected));
        actionMode2.setTitle((CharSequence) sb.toString());
        this.actionMode.invalidate();
    }

    private void selectChangeBackground(PdfOrgnalPagesViewHolder pdfOrgnalPagesViewHolder, int i) {
        if (isPAgeSelected(i)) {
            pdfOrgnalPagesViewHolder.layOrgnizePage.setVisibility(0);
        } else {
            pdfOrgnalPagesViewHolder.layOrgnizePage.setVisibility(8);
        }
    }

    private boolean isPAgeSelected(int i) {
        return getSelectedPages().contains(Integer.valueOf(i));
    }

    public int getItemCount() {
        return this.listPdfFilePages.size();
    }

    public void deleteSelectedPAge() {
        List<Integer> selectedPages2 = getSelectedPages();
        this.selectedPages.clear();
        for (Integer intValue : selectedPages2) {
            notifyItemChanged(intValue.intValue());
        }
    }

    public List<Integer> getSelectedPages() {
        int size = this.selectedPages.size();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < size; i++) {
            arrayList.add(Integer.valueOf(this.selectedPages.keyAt(i)));
        }
        return arrayList;
    }

    private void deletePdfFilePage(int i) {
        this.listPdfFilePages.remove(i);
        notifyItemRemoved(i);
    }

    public void deletePdfPage(List<Integer> list) {
        Collections.sort(list, new Comparator<Integer>() {
            public int compare(Integer num, Integer num2) {
                return num2.intValue() - num.intValue();
            }
        });
        while (!list.isEmpty()) {
            if (list.size() == 1) {
                deletePdfFilePage(((Integer) list.get(0)).intValue());
                list.remove(0);
            } else {
                int i = 1;
                while (list.size() > i && ((Integer) list.get(i)).equals(Integer.valueOf(((Integer) list.get(i - 1)).intValue() - 1))) {
                    i++;
                }
                if (i == 1) {
                    deletePdfFilePage(((Integer) list.get(0)).intValue());
                } else {
                    deletePdfRange(((Integer) list.get(i - 1)).intValue(), i);
                }
                for (int i2 = 0; i2 < i; i2++) {
                    list.remove(0);
                }
            }
        }
    }

    private void deletePdfRange(int i, int i2) {
        for (int i3 = 0; i3 < i2; i3++) {
            this.listPdfFilePages.remove(i);
        }
        notifyItemRangeRemoved(i, i2);
    }

    public void selectAllPdf() {
        int size = this.listPdfFilePages.size();
        for (int i = 0; i < size; i++) {
            this.selectedPages.put(i, true);
        }
        ActionMode actionMode2 = this.actionMode;
        StringBuilder sb = new StringBuilder();
        sb.append(size);
        sb.append(" ");
        sb.append(this.context.getString(R.string.selected));
        actionMode2.setTitle((CharSequence) sb.toString());
        this.actionMode.invalidate();
        notifyItemRangeChanged(0, size);
    }

    public void unselectAllPdf() {
        this.selectedPages.clear();
        ActionMode actionMode2 = this.actionMode;
        StringBuilder sb = new StringBuilder();
        sb.append("0 ");
        sb.append(this.context.getString(R.string.selected));
        actionMode2.setTitle((CharSequence) sb.toString());
        this.actionMode.invalidate();
        notifyItemRangeChanged(0, this.listPdfFilePages.size());
    }

    public void finishActionBarMode(boolean z) {
        this.clearSelection = z;
        ActionMode actionMode2 = this.actionMode;
        if (actionMode2 != null) {
            actionMode2.finish();
        }
    }

    private class SetActionBarThemeCallback implements Callback {
        int colorFrom,colorTo, flags;
        View view;

        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        private SetActionBarThemeCallback() {
            this.view = ((Activity) ExtractTextsPagesAdapter.this.context).getWindow().getDecorView();
            this.flags = this.view.getSystemUiVisibility();
            this.colorFrom = ExtractTextsPagesAdapter.this.context.getResources().getColor(R.color.colorPrimaryDark);
            this.colorTo = ExtractTextsPagesAdapter.this.context.getResources().getColor(R.color.colorDarkerGray);
        }

        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.activity_extract_texts_pages, menu);
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
                        ((Activity) ExtractTextsPagesAdapter.this.context).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                ofObject.start();
            }
            return true;
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.action_delete) {
                ExtractTextsPagesAdapter extractTextsPagesAdapter = ExtractTextsPagesAdapter.this;
                extractTextsPagesAdapter.deletePdfPage(extractTextsPagesAdapter.getSelectedPages());
                actionMode.finish();
            } else if (itemId == R.id.action_deselect_all) {
                ExtractTextsPagesAdapter.this.unselectAllPdf();
            } else if (itemId == R.id.action_select_all) {
                ExtractTextsPagesAdapter.this.selectAllPdf();
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode actionMode) {
            ValueAnimator valueAnimator;
            ExtractTextsPagesAdapter extractTextsPagesAdapter = ExtractTextsPagesAdapter.this;
            if (extractTextsPagesAdapter.clearSelection) {
                extractTextsPagesAdapter.deleteSelectedPAge();
            }
            int i = VERSION.SDK_INT;
            if (i >= 21) {
                if (i >= 23) {
                    this.flags |= 8192;
                    this.view.setSystemUiVisibility(this.flags);
                }
                if (!ExtractTextsPagesAdapter.this.clearSelection) {
                    valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), new Object[]{Integer.valueOf(this.colorTo), Integer.valueOf(ExtractTextsPagesAdapter.this.context.getResources().getColor(R.color.colorAccent))});
                } else {
                    valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), new Object[]{Integer.valueOf(this.colorTo), Integer.valueOf(this.colorFrom)});
                }
                valueAnimator.setDuration(300);
                valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
                    @RequiresApi(api = 21)
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        ((Activity) ExtractTextsPagesAdapter.this.context).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                valueAnimator.start();
            }
            ExtractTextsPagesAdapter.this.actionMode = null;
            ExtractTextsPagesAdapter.this.clearSelection = true;
        }
    }


}
