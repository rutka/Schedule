package pl.edu.agh.schedule.details;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.view.View;

import pl.edu.agh.schedule.R;
import pl.edu.agh.schedule.model.ScheduleItem;
import pl.edu.agh.schedule.myschedule.MyScheduleAdapter;
import pl.edu.agh.schedule.ui.BaseActivity;
import pl.edu.agh.schedule.util.LogUtils;

public class DetailActivity extends BaseActivity {
    private static final String TAG = LogUtils.makeLogTag(DetailActivity.class);

    private Handler mHandler = new Handler();
    private ScheduleItem details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean shouldBeFloatingWindow = shouldBeFloatingWindow();
        if (shouldBeFloatingWindow) {
            setupFloatingWindow(R.dimen.session_details_floating_width,
                    R.dimen.session_details_floating_height, 1, 0.4f);
        }
        super.onCreate(savedInstanceState);
        details = new ScheduleItem();
        details.startTime = getIntent().getLongExtra(MyScheduleAdapter.START_TIME, 0);
        details.endTime = getIntent().getLongExtra(MyScheduleAdapter.END_TIME, 0);
        details.title = getIntent().getStringExtra(MyScheduleAdapter.TITLE);
        details.description = getIntent().getStringExtra(MyScheduleAdapter.DESCRIPTION);

        setContentView(R.layout.detail_act);

        final Toolbar toolbar = getActionBarToolbar();
        toolbar.setNavigationIcon(shouldBeFloatingWindow
                ? R.drawable.ic_ab_close : R.drawable.ic_up);
        toolbar.setNavigationContentDescription(R.string.close_and_go_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // Do not display the Activity name in the toolbar
                toolbar.setTitle("");
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.detail_frag);
        ((DetailFragment)fragment).setDetails(details);
        super.onPostCreate(savedInstanceState);
    }
}
