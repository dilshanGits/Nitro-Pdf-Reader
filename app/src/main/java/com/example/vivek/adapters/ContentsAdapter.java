package com.example.vivek.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.vivek.R;
import com.shockwave.pdfium.PdfDocument.Bookmark;
import java.util.List;

public class ContentsAdapter extends Adapter<ContentsAdapter.PdfFileContentsViewHolder> {
    private List<Bookmark> listBookmarksFiles;
    private Context context;
    private OnContentClickedListener onContentClickedListener;

    public class PdfFileContentsViewHolder extends ViewHolder {

        public TextView tvContentFilePage, tvContentFileTitle;
        public RelativeLayout rlayContents;

        public PdfFileContentsViewHolder(View view) {
            super(view);
            this.tvContentFileTitle = (TextView) view.findViewById(R.id.tvContentFileTitle);
            this.tvContentFilePage = (TextView) view.findViewById(R.id.tvContentFilePage);
            this.rlayContents = (RelativeLayout) view.findViewById(R.id.rlayContents);
        }
    }

    public interface OnContentClickedListener {
        void onContentClicked(Bookmark bookmark);
    }

    public ContentsAdapter(Context context, List<Bookmark> bookedFile) {
        this.listBookmarksFiles = bookedFile;
        this.context = context;
        Context context2 = this.context;
        if (context2 instanceof OnContentClickedListener) {
            this.onContentClickedListener = (OnContentClickedListener) context2;
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.context.toString());
        stringBuilder.append(" must implement OnContentClickedListener");
        throw new RuntimeException(stringBuilder.toString());
    }

    public PdfFileContentsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new PdfFileContentsViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_contents, viewGroup, false));
    }

    public void onBindViewHolder(PdfFileContentsViewHolder pdfFileContentsViewHolder, int i) {
        final Bookmark bookmark = (Bookmark) this.listBookmarksFiles.get(i);
        pdfFileContentsViewHolder.tvContentFileTitle.setText(bookmark.getTitle());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.context.getString(R.string.page));
        stringBuilder.append(" ");
        stringBuilder.append(bookmark.getPageIdx() + 1);
        pdfFileContentsViewHolder.tvContentFilePage.setText(stringBuilder.toString());
        pdfFileContentsViewHolder.rlayContents.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                ContentsAdapter.this.contentClicked(bookmark);
            }
        });
    }

    public int getItemCount() {
        return this.listBookmarksFiles.size();
    }

    public void contentClicked(Bookmark bookmark) {
        this.onContentClickedListener.onContentClicked(bookmark);
    }
}
