package com.example.vivek.data;

import android.content.Context;
import com.example.vivek.R;
import com.example.vivek.models.Tool;
import java.util.ArrayList;
import java.util.List;

public class ToolsData {
    public static List<Tool> getTools(Context context) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new Tool(1, context.getString(R.string.merge), "#6db9e5", R.drawable.ic_action_merge));
        arrayList.add(new Tool(2, context.getString(R.string.split), "#9ccc66", R.drawable.ic_action_split));
        arrayList.add(new Tool(3, context.getString(R.string.extract_images), "#ffb74d", R.drawable.ic_action_extract_images));
        arrayList.add(new Tool(4, context.getString(R.string.save_as_pictures), "#7986cb", R.drawable.ic_action_save_photos));
        arrayList.add(new Tool(5, context.getString(R.string.organize_pages), "#78909c", R.drawable.ic_action_reorder));
        arrayList.add(new Tool(6, context.getString(R.string.edit_metadata), "#78909c", R.drawable.ic_action_edit_metadata));
        arrayList.add(new Tool(7, context.getString(R.string.compress), "#7ecdc8", R.drawable.ic_action_compress));
        arrayList.add(new Tool(8, context.getString(R.string.extract_text), "#9761a9", R.drawable.ic_action_extract_text));
        arrayList.add(new Tool(9, context.getString(R.string.images_to_pdf), "#f2af49", R.drawable.ic_action_image_to_pdf));
        String string = context.getString(R.string.protect);
        String str = "#7986cb";
        String string2 = context.getString(R.string.unprotect);
        String str2 = "#7ecdc8";
        String string3 = context.getString(R.string.stamp);
        String str3 = "#6db9e5";
        return arrayList;
    }
}
