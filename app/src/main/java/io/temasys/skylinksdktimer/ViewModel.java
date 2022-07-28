package io.temasys.skylinksdktimer;

import android.util.Pair;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

public class ViewModel extends androidx.lifecycle.ViewModel {
    protected final MutableLiveData<List<Pair<String, Long>>> data;
    protected final MutableLiveData<Boolean> isConnectedToRoom;

    public ViewModel() {
        data = new MutableLiveData<>(new ArrayList<>());
        isConnectedToRoom = new MutableLiveData<>(false);
    }
}
