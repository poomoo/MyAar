package com.zhy.view.flowlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by zhy on 15/9/10.
 */
public class TagFlowLayout extends FlowLayout implements TagAdapter.OnDataChangedListener {
    private TagAdapter mTagAdapter;
    private boolean mAutoSelectEffect = true;
    private int mSelectedMax = -1;//-1为不限制数量
    private static final String TAG = "TagFlowLayout";
    private MotionEvent mMotionEvent;
    private int len = 0;

    private Set<Integer> mSelectedView = new HashSet<>();


    public TagFlowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TagFlowLayout);
        mAutoSelectEffect = ta.getBoolean(R.styleable.TagFlowLayout_auto_select_effect, true);
        mSelectedMax = ta.getInt(R.styleable.TagFlowLayout_max_select, -1);
        ta.recycle();

        if (mAutoSelectEffect) {
            setClickable(true);
        }
    }

    public TagFlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TagFlowLayout(Context context) {
        this(context, null);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        changeAdapter();
        int cCount = getChildCount();
        for (int i = 0; i < cCount; i++) {
            TagView tagView = (TagView) getChildAt(i);
            if (tagView.getVisibility() == View.GONE) continue;
            if (tagView.getTagView().getVisibility() == View.GONE) {
                tagView.setVisibility(View.GONE);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public interface OnSelectListener {
        void onSelected(Set<Integer> selectPosSet);
    }

    private OnSelectListener mOnSelectListener;

    public void setOnSelectListener(OnSelectListener onSelectListener) {
        mOnSelectListener = onSelectListener;
        if (mOnSelectListener != null) setClickable(true);
    }

    public interface OnTagClickListener {
        boolean onTagClick(View view, int position, FlowLayout parent);
    }

    private OnTagClickListener mOnTagClickListener;


    public void setOnTagClickListener(OnTagClickListener onTagClickListener) {
        mOnTagClickListener = onTagClickListener;
        if (onTagClickListener != null) setClickable(true);
    }


    public void setAdapter(TagAdapter adapter) {
        //if (mTagAdapter == adapter)
        //  return;
        mTagAdapter = adapter;
        mTagAdapter.setOnDataChangedListener(this);
        mSelectedView.clear();
//        changeAdapter();

    }

    private void changeAdapter() {
        removeAllViews();
        TagAdapter adapter = mTagAdapter;
        len = adapter.getCount();
        TagView tagViewContainer;
        HashSet preCheckedList = mTagAdapter.getPreCheckedList();
        int cellWidth = getMeasuredWidth() / 4;
        int spaceWidth = cellWidth / 6;
        cellWidth = (getMeasuredWidth() - spaceWidth * 3) / 4;
        Log.d(TAG, "cellWidth:" + cellWidth + "getMeasuredWidth:" + getMeasuredWidth() + "getWidth()" + getWidth());
        for (int i = 0; i < len; i++) {
            View tagView = adapter.getView(this, i, adapter.getItem(i));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(cellWidth, LayoutParams.WRAP_CONTENT);
            if ((i + 1) % 4 == 0)
                layoutParams.setMargins(0, dip2px(getContext(), 10), 0, 0);
            else
                layoutParams.setMargins(0, dip2px(getContext(), 10), spaceWidth, 0);
            tagView.setLayoutParams(layoutParams);
            tagViewContainer = new TagView(getContext());
            tagView.setDuplicateParentStateEnabled(true);
            tagViewContainer.setLayoutParams(tagView.getLayoutParams());
            tagViewContainer.addView(tagView);
            addView(tagViewContainer);
            if (i == 0 && preCheckedList.size() == 0) {
                tagViewContainer.setChecked(true);
                mSelectedView.add(0);
            }

            if (preCheckedList.contains(i)) {
                tagViewContainer.setChecked(true);
                mSelectedView.add(i);
            }

//            if (mTagAdapter.setSelected(i, adapter.getItem(i))) {
//                mSelectedView.add(i);
//                tagViewContainer.setChecked(true);
//            }
        }
        mSelectedView.addAll(preCheckedList);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            mMotionEvent = MotionEvent.obtain(event);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        if (mMotionEvent == null) return super.performClick();

        int x = (int) mMotionEvent.getX();
        int y = (int) mMotionEvent.getY();
        mMotionEvent = null;

        TagView child = findChild(x, y);
        int pos = findPosByView(child);
        if (child != null) {
            doSelect(child, pos);
            if (mOnTagClickListener != null) {
                return mOnTagClickListener.onTagClick(child.getTagView(), pos, this);
            }
        }
        return true;
    }


    public void setMaxSelectCount(int count) {
        if (mSelectedView.size() > count) {
            Log.w(TAG, "you has already select more than " + count + " views , so it will be clear .");
            mSelectedView.clear();
        }
        mSelectedMax = count;
    }

    public Set<Integer> getSelectedList() {
        return new HashSet<>(mSelectedView);
    }

    public void reSet() {
        Iterator<Integer> iterator = mSelectedView.iterator();

        while (iterator.hasNext()) {
            Integer preIndex = iterator.next();
            TagView pre = (TagView) getChildAt(preIndex);
            pre.setChecked(false);
        }
        mSelectedView.clear();
        TagView defaultView = (TagView) getChildAt(0);
        defaultView.setChecked(true);
        mSelectedView.add(0);
        if (mOnSelectListener != null) {
            mOnSelectListener.onSelected(new HashSet<>(mSelectedView));
        }
    }

    private void doSelect(TagView child, int position) {
        if (mAutoSelectEffect) {
            if (!child.isChecked()) {//false状态
                //处理max_select=1的情况
                if (mSelectedMax == 1 && mSelectedView.size() == 1) {
                    Iterator<Integer> iterator = mSelectedView.iterator();
                    Integer preIndex = iterator.next();
                    TagView pre = (TagView) getChildAt(preIndex);
                    pre.setChecked(false);
                    child.setChecked(true);
                    mSelectedView.remove(preIndex);
                    mSelectedView.add(position);
                } else {
                    if (mSelectedMax > 0 && mSelectedView.size() >= mSelectedMax)
                        return;

                    if (position != 0) {
                        //设置第一个为false
                        TagView defaultView = (TagView) getChildAt(0);
                        defaultView.setChecked(false);
                        mSelectedView.remove(defaultView);
                        Log.d(TAG, "点击前是false状态1:" + position + ":" + mSelectedView.size() + "len:" + len);
                        if (mSelectedView.size() == len - 1) {
                            Iterator<Integer> iterator = mSelectedView.iterator();
                            while (iterator.hasNext()) {
                                Integer preIndex = iterator.next();
                                TagView pre = (TagView) getChildAt(preIndex);
                                pre.setChecked(false);
                            }
                            mSelectedView.clear();
                            defaultView.setChecked(true);
                            mSelectedView.add(0);
                        } else {
                            child.setChecked(true);
                            mSelectedView.add(position);
                        }
                    } else {
                        Iterator<Integer> iterator = mSelectedView.iterator();
                        while (iterator.hasNext()) {
                            Integer preIndex = iterator.next();
                            TagView pre = (TagView) getChildAt(preIndex);
                            pre.setChecked(false);
                        }
                        mSelectedView.clear();
                        child.setChecked(true);
                        mSelectedView.add(position);
                    }
                    Log.d(TAG, "点击前是false状态:" + position + ":" + mSelectedView.size());
                }
            } else {//true状态
                if (position != 0) {
                    child.setChecked(false);
                    mSelectedView.remove(position);
                    Log.d(TAG, "点击前是true状态" + mSelectedView.size());
                    if (mSelectedView.size() == 0) {
                        TagView defaultView = (TagView) getChildAt(0);
                        defaultView.setChecked(true);
                        mSelectedView.add(0);
                    }
                } else if (position == 0) {

                }
            }

            if (mOnSelectListener != null) {
                mOnSelectListener.onSelected(new HashSet<>(mSelectedView));
            }
        }
    }

    private static final String KEY_CHOOSE_POS = "key_choose_pos";
    private static final String KEY_DEFAULT = "key_default";


    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_DEFAULT, super.onSaveInstanceState());

        String selectPos = "";
        if (mSelectedView.size() > 0) {
            for (int key : mSelectedView) {
                selectPos += key + "|";
            }
            selectPos = selectPos.substring(0, selectPos.length() - 1);
        }
        bundle.putString(KEY_CHOOSE_POS, selectPos);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            String mSelectPos = bundle.getString(KEY_CHOOSE_POS);
            if (!TextUtils.isEmpty(mSelectPos)) {
                String[] split = mSelectPos.split("\\|");
                for (String pos : split) {
                    int index = Integer.parseInt(pos);
                    mSelectedView.add(index);

                    TagView tagView = (TagView) getChildAt(index);
                    if (tagView != null)
                        tagView.setChecked(true);
                }

            }
            super.onRestoreInstanceState(bundle.getParcelable(KEY_DEFAULT));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    private int findPosByView(View child) {
        final int cCount = getChildCount();
        for (int i = 0; i < cCount; i++) {
            View v = getChildAt(i);
            if (v == child) return i;
        }
        return -1;
    }

    private TagView findChild(int x, int y) {
        final int cCount = getChildCount();
        for (int i = 0; i < cCount; i++) {
            TagView v = (TagView) getChildAt(i);
            if (v.getVisibility() == View.GONE) continue;
            Rect outRect = new Rect();
            v.getHitRect(outRect);
            if (outRect.contains(x, y)) {
                return v;
            }
        }
        return null;
    }

    @Override
    public void onChanged() {
        mSelectedView.clear();
        changeAdapter();
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
