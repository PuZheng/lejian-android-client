package com.puzheng.the_genuine;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.apache.http.HttpException;

import java.io.IOException;

/**
 * Created by abc549825@163.com(https://github.com/abc549825) at 12-16.
 */
public class MaskableManager {
    private View targetView;
    private RefreshInterface mRefreshInterface;
    private View mRootView;
    private ProgressBar mProgressBar;
    private TextView mTextView;
    private ImageButton mImageButton;


    public MaskableManager(View view, RefreshInterface i) {
        this.targetView = view;
        this.mRefreshInterface = i;
        this.mRootView = getMaskView(view);
    }

    public void mask() {
        if (mRootView.getParent() == null) {
            ViewGroup parent = (ViewGroup) targetView.getParent();
            parent.removeView(targetView);
            parent.addView(mRootView, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        mProgressBar.setVisibility(View.VISIBLE);
        mTextView.setVisibility(View.GONE);
        mImageButton.setVisibility(View.GONE);
    }

    /**
     * @param exception
     * @return 如果没有发生异常，则还原view，并且返回true
     */
    public boolean unmask(Exception exception) {
        if (exception == null) {
            ViewGroup parent = (ViewGroup) mRootView.getParent();
            parent.removeView(mRootView);
            parent.addView(targetView);
            return true;
        } else {
            exception.printStackTrace();
            if (isNetworkException(exception)) {
                mImageButton.setImageResource(R.drawable.wifi_not_connected);
                mTextView.setText("请检查网络连接，点击重试");
            } else {
                mImageButton.setImageResource(R.drawable.ic_action_refresh);
                mTextView.setText("系统异常，点击重试");
            }
            mProgressBar.setVisibility(View.GONE);
            mTextView.setVisibility(View.VISIBLE);
            mImageButton.setVisibility(View.VISIBLE);
            return false;
        }
    }

    private View getMaskView(View view) {
        LinearLayout maskView = new LinearLayout(view.getContext());
        maskView.setOrientation(LinearLayout.VERTICAL);
        maskView.setGravity(Gravity.CENTER);

        mProgressBar = new ProgressBar(view.getContext(), null,
                android.R.attr.progressBarStyleLarge);
        maskView.addView(mProgressBar, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mImageButton = new ImageButton(view.getContext());
        mImageButton.setImageResource(R.drawable.wifi_not_connected);
        mImageButton.setBackgroundColor(view.getResources().getColor(android.R.color.transparent));
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRefreshInterface.refresh();
            }
        });
        maskView.addView(mImageButton, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mTextView = new TextView(view.getContext());
        mTextView.setText(R.string.error_message);
        maskView.addView(mTextView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        return maskView;
    }

    private boolean isNetworkException(Exception e) {
        return e instanceof HttpException || e instanceof IOException;
    }


}