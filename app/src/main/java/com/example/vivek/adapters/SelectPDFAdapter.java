package com.example.vivek.adapters;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.os.Build.VERSION;
import android.preference.PreferenceManager;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.view.ActionMode.Callback;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.text.format.Formatter;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
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
import com.example.vivek.BrowsePDFActivity;
import com.example.vivek.R;
import com.example.vivek.models.PdfDataType;
import com.example.vivek.utils.Utils;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class SelectPDFAdapter extends Adapter<SelectPDFAdapter.SelectPDFViewHolder> {
    public ActionMode actionMode;
    public ActionModeCallback actionModeCallback;
    public boolean isGridDataEnabled;
    public Boolean isSelectMultiple;
    public Context context;
    private OnMultiSelectedPDFListener onMultiSelectedPDFListener;
    private List<PdfDataType> listPdfTypeFiles;
    private SparseBooleanArray selectedPDFs = new SparseBooleanArray();
    private OnSelectedPdfClickListener pdfStaredClickListener;


    public class SelectPDFViewHolder extends ViewHolder {
        public TextView tvPdfSize, tvPdfModifiedTime, tvPdfTitle;
        public LinearLayout laySelectedItemHyLight, layPdfItemMain;
        public ImageView imgPdfSelectedPhoto;
        public RelativeLayout rlayPdfSelected;

        public SelectPDFViewHolder(View view) {
            super(view);
            if (SelectPDFAdapter.this.isGridDataEnabled) {
                this.imgPdfSelectedPhoto = (ImageView) view.findViewById(R.id.imgPdfSelectedPhoto);
                this.laySelectedItemHyLight = (LinearLayout) view.findViewById(R.id.laySelectedItemHyLight);
            }
            this.tvPdfTitle = (TextView) view.findViewById(R.id.tvPdfTitle);
            this.tvPdfModifiedTime = (TextView) view.findViewById(R.id.tvPdfModifiedTime);
            this.tvPdfSize = (TextView) view.findViewById(R.id.tvPdfSize);
            this.rlayPdfSelected = (RelativeLayout) view.findViewById(R.id.rlayPdfSelected);
            this.layPdfItemMain = (LinearLayout) view.findViewById(R.id.layPdfItemMain);
        }
    }

    public SelectPDFAdapter(List<PdfDataType> listPdfFile, Context context, Boolean bool) {
        this.listPdfTypeFiles = listPdfFile;
        this.context = context;
        this.isSelectMultiple = bool;
        this.actionModeCallback = new ActionModeCallback();
        this.isGridDataEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(BrowsePDFActivity.GRID_VIEW_ENABLED, false);
        Context context2 = this.context;
        if (context2 instanceof OnSelectedPdfClickListener) {
            this.pdfStaredClickListener = (OnSelectedPdfClickListener) context2;
            if (context2 instanceof OnMultiSelectedPDFListener) {
                this.onMultiSelectedPDFListener = (OnMultiSelectedPDFListener) context2;
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(this.context.toString());
            sb.append(" must implement OnMultiSelectedPDFListener");
            throw new RuntimeException(sb.toString());
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append(this.context.toString());
        sb2.append(" must implement OnSelectedPdfClickListener");
        throw new RuntimeException(sb2.toString());
    }

    public SelectPDFViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view;
        if (this.isGridDataEnabled) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_pdf_select_grid, viewGroup, false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_pdf_select, viewGroup, false);
        }
        return new SelectPDFViewHolder(view);
    }

    public void onBindViewHolder(SelectPDFViewHolder selectPDFViewHolder, final int i) {
        PdfDataType pdfDataType = (PdfDataType) this.listPdfTypeFiles.get(i);
        String pdfFileName = pdfDataType.getName();
        Long pdfFileLength = pdfDataType.getLength();
        selectPDFViewHolder.tvPdfTitle.setText(pdfFileName);
        selectPDFViewHolder.tvPdfSize.setText(Formatter.formatShortFileSize(this.context, pdfFileLength.longValue()));
        selectPDFViewHolder.tvPdfModifiedTime.setText(Utils.formatDateToHumanReadable(pdfDataType.getLastModified()));
        changePDFSelectedBGColor(selectPDFViewHolder, i);
        if (this.isGridDataEnabled) {
            Picasso.with(this.context).load(pdfDataType.getThumbUri()).into((ImageView) selectPDFViewHolder.imgPdfSelectedPhoto);
        }
        selectPDFViewHolder.rlayPdfSelected.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (SelectPDFAdapter.this.actionMode == null && SelectPDFAdapter.this.isSelectMultiple.booleanValue()) {
                    SelectPDFAdapter selectPDFAdapter = SelectPDFAdapter.this;
                    selectPDFAdapter.actionMode = ((AppCompatActivity) selectPDFAdapter.context).startSupportActionMode(SelectPDFAdapter.this.actionModeCallback);
                }
                if (SelectPDFAdapter.this.isSelectMultiple.booleanValue()) {
                    SelectPDFAdapter.this.getPDFSelection(i);
                } else {
                    SelectPDFAdapter.this.clickedSelectedPdf(i);
                }
            }
        });
    }

    public void filter(List<PdfDataType> list) {
        this.listPdfTypeFiles = list;
        notifyDataSetChanged();
    }

    public void pdfDataUpdate(List<PdfDataType> list) {
        this.listPdfTypeFiles = list;
        notifyDataSetChanged();
    }

    private class ActionModeCallback implements Callback {
        int colorFrom,colorTo,flags;
        View view;

        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        private ActionModeCallback() {
            this.view = ((Activity) SelectPDFAdapter.this.context).getWindow().getDecorView();
            this.flags = this.view.getSystemUiVisibility();
            this.colorFrom = SelectPDFAdapter.this.context.getResources().getColor(R.color.colorPrimaryDark);
            this.colorTo = SelectPDFAdapter.this.context.getResources().getColor(R.color.colorDarkerGray);
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
                        ((Activity) SelectPDFAdapter.this.context).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                valueAnimator.start();
            }
            return true;
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.action_select) {
                SelectPDFAdapter selectPDFAdapter = SelectPDFAdapter.this;
                selectPDFAdapter.selectedMultiplePDF(selectPDFAdapter.selectedListPdfFiles());
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode actionMode) {
            SelectPDFAdapter.this.clearSelectedPDF();
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
                        ((Activity) SelectPDFAdapter.this.context).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                valueAnimator.start();
            }
            SelectPDFAdapter.this.actionMode = null;
        }
    }

    public interface OnMultiSelectedPDFListener {
        void onMultiSelectedPDF(ArrayList<String> arrayList);
    }

    public interface OnSelectedPdfClickListener {
        void onSelectedPdfClicked(PdfDataType pdfDataType);
    }


    public void getPDFSelection(int i) {
        if (this.selectedPDFs.get(i, false)) {
            this.selectedPDFs.delete(i);
        } else {
            this.selectedPDFs.put(i, true);
        }
        notifyItemChanged(i);
        int selectedItemCount = getSelectedItemCount();
        if (selectedItemCount == 0) {
            this.actionMode.finish();
            return;
        }
        ActionMode actionMode2 = this.actionMode;
        StringBuilder sb = new StringBuilder();
        sb.append(selectedItemCount);
        sb.append(" ");
        sb.append(this.context.getString(R.string.selected));
        actionMode2.setTitle((CharSequence) sb.toString());
        this.actionMode.invalidate();
    }

    private int getSelectedItemCount() {
        return this.selectedPDFs.size();
    }

    private List<Integer> getSelectedPdfFiles() {
        int size = this.selectedPDFs.size();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < size; i++) {
            arrayList.add(Integer.valueOf(this.selectedPDFs.keyAt(i)));
        }
        return arrayList;
    }


    public void clearSelectedPDF() {
        List<Integer> selectedPDFFiles = getSelectedPdfFiles();
        this.selectedPDFs.clear();
        for (Integer intValue : selectedPDFFiles) {
            notifyItemChanged(intValue.intValue());
        }
    }

    private boolean isSelected(int i) {
        return getSelectedPdfFiles().contains(Integer.valueOf(i));
    }

    private void changePDFSelectedBGColor(SelectPDFViewHolder selectPDFViewHolder, int i) {
        if (isSelected(i)) {
            if (this.isGridDataEnabled) {
                selectPDFViewHolder.laySelectedItemHyLight.setVisibility(View.VISIBLE);
            } else {
                selectPDFViewHolder.layPdfItemMain.setBackgroundColor(ContextCompat.getColor(this.context, R.color.colorSelectedPDFs));
            }
        } else if (this.isGridDataEnabled) {
            selectPDFViewHolder.laySelectedItemHyLight.setVisibility(View.GONE);
        } else {
            TypedValue typedValue = new TypedValue();
            this.context.getTheme().resolveAttribute(16843534, typedValue, true);
            selectPDFViewHolder.layPdfItemMain.setBackgroundResource(typedValue.resourceId);
        }
    }


    public ArrayList<String> selectedListPdfFiles() {
        List<Integer> selectedPDFs2 = getSelectedPdfFiles();
        ArrayList<String> arrayList = new ArrayList<>();
        for (Integer intValue : selectedPDFs2) {
            arrayList.add(((PdfDataType) this.listPdfTypeFiles.get(intValue.intValue())).getAbsolutePath());
        }
        return arrayList;
    }

    public int getItemCount() {
        return this.listPdfTypeFiles.size();
    }


    public void clickedSelectedPdf(int i) {
        OnSelectedPdfClickListener onSelectedPdfClickListener = this.pdfStaredClickListener;
        if (onSelectedPdfClickListener != null) {
            onSelectedPdfClickListener.onSelectedPdfClicked((PdfDataType) this.listPdfTypeFiles.get(i));
        }
    }

    public void selectedMultiplePDF(ArrayList<String> arrayList) {
        OnMultiSelectedPDFListener onMultiSelectedPDFListener = this.onMultiSelectedPDFListener;
        if (onMultiSelectedPDFListener != null) {
            onMultiSelectedPDFListener.onMultiSelectedPDF(arrayList);
        }
    }

}
