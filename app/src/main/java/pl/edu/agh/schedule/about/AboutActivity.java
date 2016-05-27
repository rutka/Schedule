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

package pl.edu.agh.schedule.about;


import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import pl.edu.agh.schedule.BuildConfig;
import pl.edu.agh.schedule.R;
import pl.edu.agh.schedule.ui.BaseActivity;
import pl.edu.agh.schedule.ui.widget.DrawShadowFrameLayout;
import pl.edu.agh.schedule.util.AboutUtils;
import pl.edu.agh.schedule.util.UIUtils;

public class AboutActivity extends BaseActivity {

    private View rootView;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.about_authors:
                    AboutUtils.showEula(AboutActivity.this); //TODO change for authors
                    break;
                case R.id.about_licenses:
                    AboutUtils.showOpenSourceLicenses(AboutActivity.this); //TODO check licenses
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        rootView = findViewById(R.id.about_container);

        TextView body = (TextView) rootView.findViewById(R.id.about_main);
        body.setText(Html.fromHtml(getString(R.string.about_main, BuildConfig.VERSION_NAME))); //TODO change version
        rootView.findViewById(R.id.about_authors).setOnClickListener(mOnClickListener);
        rootView.findViewById(R.id.about_licenses).setOnClickListener(mOnClickListener);

        overridePendingTransition(0, 0);
    }


    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_ABOUT;
    }

    private void setContentTopClearance(int clearance) {
        if (rootView != null) {
            rootView.setPadding(rootView.getPaddingLeft(), clearance,
                    rootView.getPaddingRight(), rootView.getPaddingBottom());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int actionBarSize = UIUtils.calculateActionBarSize(this);
        DrawShadowFrameLayout drawShadowFrameLayout =
                (DrawShadowFrameLayout) findViewById(R.id.main_content);
        if (drawShadowFrameLayout != null) {
            drawShadowFrameLayout.setShadowTopOffset(actionBarSize);
        }
        setContentTopClearance(actionBarSize);
    }

}
