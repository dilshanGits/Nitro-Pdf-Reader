package com.example.vivek.adapters;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.vivek.R;
import com.example.vivek.models.Tool;
import java.util.List;

public class ToolsAdapter extends Adapter<ToolsAdapter.ToolsViewHolder> {
    private Context mContext;
    private OnSelectedMenuClickListener toolClickListener;
    private List<Tool> tools;


    public class ToolsViewHolder extends ViewHolder {
        public ImageView imgIconTool;
        public TextView tvMenuToolTitle;
        public LinearLayout layToolItem;

        public ToolsViewHolder(View view) {
            super(view);
            this.tvMenuToolTitle = (TextView) view.findViewById(R.id.tvMenuToolTitle);
            this.imgIconTool = (ImageView) view.findViewById(R.id.imgIconTool);
            this.layToolItem = (LinearLayout) view.findViewById(R.id.layToolItem);
        }
    }

    public ToolsAdapter(Context context, List<Tool> list) {
        this.tools = list;
        this.mContext = context;
        Context context2 = this.mContext;
        if (context2 instanceof OnSelectedMenuClickListener) {
            this.toolClickListener = (OnSelectedMenuClickListener) context2;
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.mContext.toString());
        stringBuilder.append(" must implement OnSelectedMenuClickListener");
        throw new RuntimeException(stringBuilder.toString());
    }

    public ToolsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ToolsViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_tool, viewGroup, false));
    }

    public void onBindViewHolder(final ToolsViewHolder toolsViewHolder, int i) {
        Tool tool = (Tool) this.tools.get(i);
        toolsViewHolder.tvMenuToolTitle.setText(tool.getTitle());
        toolsViewHolder.imgIconTool.setBackgroundResource(tool.getDrawable());
        toolsViewHolder.imgIconTool.setImageDrawable(ContextCompat.getDrawable(this.mContext, tool.getDrawable()));
        toolsViewHolder.layToolItem.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                ToolsAdapter.this.menuToolClick(toolsViewHolder.getAdapterPosition());
            }
        });
    }

    public int getItemCount() {
        return this.tools.size();
    }

    public void menuToolClick(int i) {
        OnSelectedMenuClickListener onSelectedMenuClickListener = this.toolClickListener;
        if (onSelectedMenuClickListener != null) {
            onSelectedMenuClickListener.onToolClicked(i);
        }
    }
    public interface OnSelectedMenuClickListener {
        void onToolClicked(int i);
    }
}
