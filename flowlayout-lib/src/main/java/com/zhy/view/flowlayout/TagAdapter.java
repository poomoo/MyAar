package com.zhy.view.flowlayout;

import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class TagAdapter<T> {
    private List<T> mTagDatas;
    private OnDataChangedListener mOnDataChangedListener;
    private HashSet<Integer> mCheckedPosList = new HashSet<Integer>();

    public TagAdapter(List<T> datas) {
        mTagDatas = datas;
        Log.d("TagAdapter","mTagDatas:"+mTagDatas.size());
    }

//    public TagAdapter(T[] datas) {
//        mTagDatas = new ArrayList<T>(Arrays.asList(datas));
//    }

    static interface OnDataChangedListener {
        void onChanged();
    }

    void setOnDataChangedListener(OnDataChangedListener listener) {
        mOnDataChangedListener = listener;
    }

    public void setSelectedList(List<Integer> pos) {
        for (int i = 0; i < pos.size(); i++)
            mCheckedPosList.add(pos.get(i));
        notifyDataChanged();
    }

    public void setSelectedList(Set<Integer> set) {
        mCheckedPosList.clear();
        mCheckedPosList.addAll(set);
        notifyDataChanged();
    }

    HashSet<Integer> getPreCheckedList() {
        return mCheckedPosList;
    }


    public int getCount() {
        return mTagDatas == null ? 0 : mTagDatas.size();
    }

    public void notifyDataChanged() {
        mOnDataChangedListener.onChanged();
    }

    public T getItem(int position) {
        return mTagDatas.get(position);
    }

    public abstract View getView(FlowLayout parent, int position, T t);

    public boolean setSelected(int position, T t) {
        return false;
    }


}