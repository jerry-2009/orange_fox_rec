package com.fordownloads.orangefox.ui.scripts;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ScriptsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ScriptsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}