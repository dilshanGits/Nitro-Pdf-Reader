package com.example.vivek.adapters;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.os.Build.VERSION;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.view.ActionMode.Callback;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.vivek.R;
import com.example.vivek.data.DbHelper;
import com.example.vivek.models.BookmarkData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BookmarksAdapter extends Adapter<BookmarksAdapter.PDFBookViewHolder> {
    
    public ActionMode actionMode;
    public ActionModeCallback actionModeCallback;
    private List<BookmarkData> listBookMarkPDF;
    private LinearLayout layEmptyData;
    public Context mContext;
    private OnBookmarkClickedListener onBookmarkClickedListener;
    private SparseBooleanArray selectedBookmarks = new SparseBooleanArray();



    public class PDFBookViewHolder extends ViewHolder {

        public TextView tvBookmarkPageNumber, tvPDFBookMarkTitle;
        public RelativeLayout rLayBookMark;
        public LinearLayout laySelectedItem;

        public PDFBookViewHolder(View view) {
            super(view);

            this.tvPDFBookMarkTitle = (TextView) view.findViewById(R.id.tvPDFBookMarkTitle);
            this.tvBookmarkPageNumber = (TextView) view.findViewById(R.id.tvBookmarkPageNumber);
            this.rLayBookMark = (RelativeLayout) view.findViewById(R.id.rLayBookMark);
            this.laySelectedItem = (LinearLayout) view.findViewById(R.id.laySelectedItem);
        }
    }

    public interface OnBookmarkClickedListener {
        void onBookmarkClicked(BookmarkData bookmarkData);
    }

    public BookmarksAdapter(Context context, List<BookmarkData> list, LinearLayout linearLayout) {
        this.listBookMarkPDF = list;
        this.mContext = context;
        this.layEmptyData = linearLayout;
        this.actionModeCallback = new ActionModeCallback();
        Context context2 = this.mContext;
        if (context2 instanceof OnBookmarkClickedListener) {
            this.onBookmarkClickedListener = (OnBookmarkClickedListener) context2;
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.mContext.toString());
        stringBuilder.append(" must implement OnBookmarkClickedListener");
        throw new RuntimeException(stringBuilder.toString());
    }

    public PDFBookViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new PDFBookViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_bookmark, viewGroup, false));
    }

    public void onBindViewHolder(final PDFBookViewHolder pdfBookVHolder, int i) {
        final BookmarkData bookmarkData = (BookmarkData) this.listBookMarkPDF.get(i);
        pdfBookVHolder.tvPDFBookMarkTitle.setText(bookmarkData.getTitle());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.mContext.getString(R.string.page));
        stringBuilder.append(" ");
        stringBuilder.append(bookmarkData.getPageNumber());
        pdfBookVHolder.tvBookmarkPageNumber.setText(stringBuilder.toString());
        togglePDFSelectedBGColor(pdfBookVHolder, i);

        pdfBookVHolder.rLayBookMark.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (actionMode != null) {
                    getTogglePDFSelected(pdfBookVHolder.getAdapterPosition());
                } else {
                    bookmarkPDFSelected(bookmarkData);
                }
            }
        });
        pdfBookVHolder.rLayBookMark.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View view) {
                if (actionMode == null) {
                    BookmarksAdapter bookmarksAdapter = BookmarksAdapter.this;
                    bookmarksAdapter.actionMode = ((AppCompatActivity) bookmarksAdapter.mContext).startSupportActionMode(actionModeCallback);
                }
                getTogglePDFSelected(pdfBookVHolder.getAdapterPosition());
                return false;
            }
        });
    }


    public void getTogglePDFSelected(int i) {
        if (this.selectedBookmarks.get(i, false)) {
            this.selectedBookmarks.delete(i);
        } else {
            this.selectedBookmarks.put(i, true);
        }
        notifyItemChanged(i);
        int selectedPdfCount = getSelectedPDFCount();
        if (selectedPdfCount == 0) {
            this.actionMode.finish();
            return;
        }
        ActionMode actionMode2 = this.actionMode;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(selectedPdfCount);
        stringBuilder.append(" ");
        stringBuilder.append(this.mContext.getString(R.string.selected));
        actionMode2.setTitle((CharSequence) stringBuilder.toString());
        this.actionMode.invalidate();
    }

    private int getSelectedPDFCount() {
        return this.selectedBookmarks.size();
    }

    private List<Integer> getSelectedPDFPhotos() {
        int size = this.selectedBookmarks.size();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < size; i++) {
            arrayList.add(Integer.valueOf(this.selectedBookmarks.keyAt(i)));
        }
        return arrayList;
    }


    public void clearSelectedPDF() {
        List<Integer> selectedImages = getSelectedPDFPhotos();
        this.selectedBookmarks.clear();
        for (Integer intValue : selectedImages) {
            notifyItemChanged(intValue.intValue());
        }
    }

    private boolean isSelectedPdf(int i) {
        return getSelectedPDFPhotos().contains(Integer.valueOf(i));
    }

    private void togglePDFSelectedBGColor(PDFBookViewHolder PDFBookViewHolder, int i) {
        if (isSelectedPdf(i)) {
            PDFBookViewHolder.laySelectedItem.setBackgroundColor(ContextCompat.getColor(this.mContext, R.color.colorSelectedPDFs));
            return;
        }
        TypedValue typedValue = new TypedValue();
        this.mContext.getTheme().resolveAttribute(16843534, typedValue, true);
        PDFBookViewHolder.laySelectedItem.setBackgroundResource(typedValue.resourceId);
    }

    public int getItemCount() {
        return this.listBookMarkPDF.size();
    }

    public void bookmarkPDFSelected(BookmarkData bookmarkData) {
        this.onBookmarkClickedListener.onBookmarkClicked(bookmarkData);
    }

    private class ActionModeCallback implements Callback {
        int colorFrom, colorTo,flags;
        View actionView;

        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        private ActionModeCallback() {
            this.actionView = ((Activity) mContext).getWindow().getDecorView();
            this.flags = this.actionView.getSystemUiVisibility();
            this.colorFrom = mContext.getResources().getColor(R.color.colorPrimaryDark);
            this.colorTo = mContext.getResources().getColor(R.color.colorDarkerGray);
        }

        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.fragment_bookmarks, menu);
            int i = VERSION.SDK_INT;
            if (i >= 21) {
                if (i >= 23) {
                    this.flags &= -8193;
                    this.actionView.setSystemUiVisibility(this.flags);
                }
                ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), new Object[]{Integer.valueOf(this.colorFrom), Integer.valueOf(this.colorTo)});
                ofObject.setDuration(300);
                ofObject.addUpdateListener(new AnimatorUpdateListener() {
                    @RequiresApi(api = 21)
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        ((Activity) mContext).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                ofObject.start();
            }
            return true;
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.action_delete) {
                deleteSelectedPDF(actionMode);
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode actionMode) {
            clearSelectedPDF();
            int i = VERSION.SDK_INT;
            if (i >= 21) {
                if (i >= 23) {
                    this.flags |= 8192;
                    this.actionView.setSystemUiVisibility(this.flags);
                }
                ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), new Object[]{Integer.valueOf(this.colorTo), Integer.valueOf(this.colorFrom)});
                ofObject.setDuration(300);
                ofObject.addUpdateListener(new AnimatorUpdateListener() {
                    @RequiresApi(api = 21)
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        ((Activity) mContext).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                ofObject.start();
            }
            actionMode = null;
        }
    }

    public void deleteSelectedPDF(ActionMode actionMode2) {
        DbHelper dbHelper = DbHelper.getInstance(this.mContext);
        List totalSelectedBook = getSelectedBookmarks();
        int selectedItemCount = getSelectedPDFCount();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < selectedItemCount; i++) {
            arrayList.add(this.listBookMarkPDF.get(((Integer) totalSelectedBook.get(i)).intValue()));
        }
        removeSelectedPdf(totalSelectedBook);
        actionMode2.finish();
        dbHelper.deleteBookmarks(arrayList);
    }

    private void removeItem(int i) {
        this.listBookMarkPDF.remove(i);
        setLayEmptyData();
        notifyItemRemoved(i);
    }

    private void removeSelectedPdf(List<Integer> pdfList) {
        Collections.sort(pdfList, new Comparator<Integer>() {
            public int compare(Integer num, Integer num2) {
                return num2.intValue() - num.intValue();
            }
        });
        while (!pdfList.isEmpty()) {
            if (pdfList.size() == 1) {
                removeItem(((Integer) pdfList.get(0)).intValue());
                pdfList.remove(0);
            } else {
                int i = 1;
                while (pdfList.size() > i && ((Integer) pdfList.get(i)).equals(Integer.valueOf(((Integer) pdfList.get(i - 1)).intValue() - 1))) {
                    i++;
                }
                if (i == 1) {
                    removeItem(((Integer) pdfList.get(0)).intValue());
                } else {
                    removePDF(((Integer) pdfList.get(i - 1)).intValue(), i);
                }
                for (int i2 = 0; i2 < i; i2++) {
                    pdfList.remove(0);
                }
            }
        }
    }

    private void removePDF(int i, int i2) {
        for (int i3 = 0; i3 < i2; i3++) {
            this.listBookMarkPDF.remove(i);
        }
        setLayEmptyData();
        notifyItemRangeRemoved(i, i2);
    }

    private List<Integer> getSelectedBookmarks() {
        int size = this.selectedBookmarks.size();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < size; i++) {
            arrayList.add(Integer.valueOf(this.selectedBookmarks.keyAt(i)));
        }
        return arrayList;
    }

    private void setLayEmptyData() {
        if (this.listBookMarkPDF.size() > 0) {
            this.layEmptyData.setVisibility(View.GONE);
        } else {
            this.layEmptyData.setVisibility(View.VISIBLE);
        }
    }
}
