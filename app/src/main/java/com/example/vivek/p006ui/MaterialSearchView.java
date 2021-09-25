package com.example.vivek.p006ui;

import android.content.Context;
import android.os.Build.VERSION;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.vivek.R;

public class MaterialSearchView extends FrameLayout {
    public final String TAG = MaterialSearchView.class.getSimpleName();
    private ImageView imgSearchPdf,imgSearchClose;
    private Context context;
    public EditText editSearchPdfText;
    private CharSequence charSequence;
    private OnQueryTextListener onQueryTextListener;
    private ConstraintLayout laySearchBarPdf;
    View parent;


    public MaterialSearchView(Context context2) {
        super(context2);
        this.context = context2;
        init();
    }

    public MaterialSearchView(Context context2, @Nullable AttributeSet attributeSet) {
        super(context2, attributeSet);
        this.context = context2;
        init();
    }

    public MaterialSearchView(Context context2, @Nullable AttributeSet attributeSet, int i) {
        super(context2, attributeSet, i);
        this.context = context2;
        init();
    }

    
    private void init() {
        LayoutInflater.from(this.context).inflate(R.layout.material_search_view, this, true);

        laySearchBarPdf = (ConstraintLayout) findViewById(R.id.laySearchBarPdf);
        laySearchBarPdf.setVisibility(GONE);
        parent = (View) laySearchBarPdf.getParent();
        editSearchPdfText = (EditText) laySearchBarPdf.findViewById(R.id.editSearchPdfText);
        imgSearchPdf = (ImageView) laySearchBarPdf.findViewById(R.id.imgSearchPdf);
        imgSearchClose = (ImageView) laySearchBarPdf.findViewById(R.id.imgSearchClose);


        editSearchPdfText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean z) {
                if (z) {
                    MaterialSearchView materialSearchView = MaterialSearchView.this;
                    materialSearchView.showKeyboard(materialSearchView.editSearchPdfText);
                }
            }
        });

        imgSearchPdf.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSearchingPdfData();
            }
        });
        this.imgSearchClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editSearchPdfText.setText("");
                imgSearchClose.setVisibility(GONE);
            }
        });
        this.editSearchPdfText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable editable) {
            }

            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                MaterialSearchView.this.onTextChanged(charSequence);
            }
        });
    }
    

    
    public void onTextChanged(CharSequence charSequence) {
        this.charSequence = this.editSearchPdfText.getText();
        if (!TextUtils.isEmpty(this.charSequence)) {
            this.imgSearchClose.setVisibility(VISIBLE);
        } else {
            this.imgSearchClose.setVisibility(GONE);
        }
        OnQueryTextListener onQueryTextListener = this.onQueryTextListener;
        if (onQueryTextListener != null) {
            onQueryTextListener.onQueryTextChange(charSequence.toString());
        }
    }

    public interface OnQueryTextListener {
        boolean onQueryTextChange(String str);

        boolean onQueryTextSubmit(String str);
    }

    public void openPdfSearchData() {
        this.editSearchPdfText.setText("");
        this.editSearchPdfText.requestFocus();
        if (VERSION.SDK_INT >= 21) {
            this.laySearchBarPdf.setVisibility(VISIBLE);
        } else {
            this.laySearchBarPdf.setVisibility(VISIBLE);
        }
    }

    public void closeSearchingPdfData() {
        this.laySearchBarPdf.setVisibility(GONE);
        this.editSearchPdfText.setText("");
        this.editSearchPdfText.clearFocus();
        hideKeyboard(this.editSearchPdfText);
    }

    public boolean isSearchOpen() {
        return this.laySearchBarPdf.getVisibility() == VISIBLE;
    }

    private boolean isHardKeyboardAvailable() {
        return this.context.getResources().getConfiguration().keyboard != 1;
    }
    
    public void showKeyboard(View view) {
        if (VERSION.SDK_INT <= 10 && view.hasFocus()) {
            view.clearFocus();
        }
        view.requestFocus();
        if (!isHardKeyboardAvailable()) {
            ((InputMethodManager) view.getContext().getSystemService("input_method")).showSoftInput(view, 0);
        }
    }

    private void hideKeyboard(View view) {
        ((InputMethodManager) view.getContext().getSystemService("input_method")).hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void setOnQueryTextListener(OnQueryTextListener onQueryTextListener) {
        this.onQueryTextListener = onQueryTextListener;
    }

    public void clearFocus() {
        hideKeyboard(this);
        super.clearFocus();
        this.editSearchPdfText.clearFocus();
    }

}
