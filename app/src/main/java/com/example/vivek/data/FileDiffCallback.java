package com.example.vivek.data;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil.Callback;
import com.example.vivek.models.PdfDataType;
import java.util.List;

public class FileDiffCallback extends Callback {
    private List<PdfDataType> newPDFDataTypeList;
    private List<PdfDataType> oldPDFDataTypeList;

    public FileDiffCallback(List<PdfDataType> list, List<PdfDataType> list2) {
        this.oldPDFDataTypeList = list;
        this.newPDFDataTypeList = list2;
    }

    public int getOldListSize() {
        return this.oldPDFDataTypeList.size();
    }

    public int getNewListSize() {
        return this.newPDFDataTypeList.size();
    }

    public boolean areItemsTheSame(int i, int i2) {
        return ((PdfDataType) this.oldPDFDataTypeList.get(i)).getAbsolutePath().equals(((PdfDataType) this.newPDFDataTypeList.get(i2)).getAbsolutePath());
    }

    public boolean areContentsTheSame(int i, int i2) {
        return ((PdfDataType) this.oldPDFDataTypeList.get(i)).equals(this.newPDFDataTypeList.get(i2));
    }

    @Nullable
    public Object getChangePayload(int i, int i2) {
        return super.getChangePayload(i, i2);
    }
}
