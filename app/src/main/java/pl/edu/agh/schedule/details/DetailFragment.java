/*
 * Copyright 2015 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.edu.agh.schedule.details;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import pl.edu.agh.schedule.BuildConfig;
import pl.edu.agh.schedule.R;
import pl.edu.agh.schedule.model.ScheduleItem;
import pl.edu.agh.schedule.ui.widget.ObservableScrollView;
import pl.edu.agh.schedule.util.ImageLoader;
import pl.edu.agh.schedule.util.UIUtils;

/**
 * Displays the details about a classes.
 */
public class DetailFragment extends Fragment implements ObservableScrollView.Callbacks {

    private static final float PHOTO_ASPECT_RATIO = 1.7777777f;

    private View mScrollViewChild;

    private TextView mTitle;

    private ObservableScrollView mScrollView;

    private TextView mAbstract;

    private View mHeaderBox;

    private View mDetailsContainer;

    private int mPhotoHeightPixels;

    private View mPhotoViewContainer;

    private ImageView mPhotoView;

    private float mMaxHeaderElevation;

    private ImageLoader mNoPlaceholderImageLoader;

    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.detail_frag, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHandler = new Handler();
        initViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mScrollView == null) {
            return;
        }

        ViewTreeObserver vto = mScrollView.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.removeGlobalOnLayoutListener(mGlobalLayoutListener);
        }
    }

    private void initViews() {
        mMaxHeaderElevation = getResources().getDimensionPixelSize(
                R.dimen.detail_max_header_elevation);

        mScrollView = (ObservableScrollView) getActivity().findViewById(R.id.scroll_view);
        mScrollView.addCallbacks(this);
        ViewTreeObserver vto = mScrollView.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(mGlobalLayoutListener);
        }

        mScrollViewChild = getActivity().findViewById(R.id.scroll_view_child);
        mScrollViewChild.setVisibility(View.INVISIBLE);

        mDetailsContainer = getActivity().findViewById(R.id.details_container);
        mHeaderBox = getActivity().findViewById(R.id.header_session);
        mTitle = (TextView) getActivity().findViewById(R.id.session_title);
        mPhotoViewContainer = getActivity().findViewById(R.id.session_photo_container);
        mPhotoView = (ImageView) getActivity().findViewById(R.id.session_photo);

        mAbstract = (TextView) getActivity().findViewById(R.id.session_abstract);

        ViewCompat.setTransitionName(mPhotoView, "photo");

        mNoPlaceholderImageLoader = new ImageLoader(getContext());
    }

    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener
            = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            recomputePhotoAndScrollingMetrics();
        }
    };

    private void recomputePhotoAndScrollingMetrics() {
        int mHeaderHeightPixels = mHeaderBox.getHeight();

        mPhotoHeightPixels = 0;

        mPhotoHeightPixels = (int) (mPhotoView.getWidth() / PHOTO_ASPECT_RATIO);
        mPhotoHeightPixels = Math.min(mPhotoHeightPixels, mScrollView.getHeight() * 2 / 3);


        ViewGroup.LayoutParams lp;
        lp = mPhotoViewContainer.getLayoutParams();
        if (lp.height != mPhotoHeightPixels) {
            lp.height = mPhotoHeightPixels;
            mPhotoViewContainer.setLayoutParams(lp);
        }

        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)
                mDetailsContainer.getLayoutParams();
        if (mlp.topMargin != mHeaderHeightPixels + mPhotoHeightPixels) {
            mlp.topMargin = mHeaderHeightPixels + mPhotoHeightPixels;
            mDetailsContainer.setLayoutParams(mlp);
        }

        onScrollChanged(0, 0); // trigger scroll handling
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        // Reposition the header bar -- it's normally anchored to the top of the content,
        // but locks to the top of the screen on scroll
        int scrollY = mScrollView.getScrollY();

        float newTop = Math.max(mPhotoHeightPixels, scrollY);
        mHeaderBox.setTranslationY(newTop);

        float gapFillProgress = 1;
        if (mPhotoHeightPixels != 0) {
            gapFillProgress = Math.min(Math.max(UIUtils.getProgress(scrollY,
                    0,
                    mPhotoHeightPixels), 0), 1);
        }

        ViewCompat.setElevation(mHeaderBox, gapFillProgress * mMaxHeaderElevation);

        // Move background photo (parallax effect)
        mPhotoViewContainer.setTranslationY(scrollY * 0.5f);
    }

    public void setDetails(ScheduleItem details) {
        if (details == null) {
            return;
        }
        mTitle.setText(details.title);
        mNoPlaceholderImageLoader.loadImage(BuildConfig.SERVER_URL + BuildConfig.BACKGROUND_IMAGE, mPhotoView, new RequestListener<String, Bitmap>() {
            @Override
            public boolean onException(Exception e, String model, Target<Bitmap> target,
                                       boolean isFirstResource) {
                recomputePhotoAndScrollingMetrics();
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target,
                                           boolean isFromMemoryCache, boolean isFirstResource) {
                // Trigger image transition
                recomputePhotoAndScrollingMetrics();
                return false;
            }
        });
        recomputePhotoAndScrollingMetrics();

        if (!TextUtils.isEmpty(details.description)) {
            UIUtils.setTextMaybeHtml(mAbstract, details.description);
            mAbstract.setVisibility(View.VISIBLE);
        } else {
            mAbstract.setVisibility(View.GONE);
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onScrollChanged(0, 0); // trigger scroll handling
                mScrollViewChild.setVisibility(View.VISIBLE);
            }
        });
    }
}
