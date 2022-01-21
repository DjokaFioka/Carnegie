package rs.djokafioka.carnegie;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import rs.djokafioka.carnegie.controller.LoginFragment;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity
{
    private ProgressBar mProgressBar;

    private FragmentManager.OnBackStackChangedListener mOnBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            syncActionBarArrowState();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportFragmentManager().addOnBackStackChangedListener(mOnBackStackChangedListener);

        if (savedInstanceState == null)
        {
            showFragment(new LoginFragment(), false);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        syncActionBarArrowState();
    }

    @Override
    public void onBackPressed()
    {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (currentFragment instanceof BackButtonHandler && ((BackButtonHandler) currentFragment).onBackPressed())
        {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if (currentFragment instanceof BackButtonHandler && ((BackButtonHandler) currentFragment).onBackPressed())
            {
                return true;
            }
            else if (getSupportFragmentManager().popBackStackImmediate())
            {
                return true;
            }
            else
            {
                return super.onOptionsItemSelected(item);
            }
        }
        else
        {
            return super.onOptionsItemSelected(item);
        }
    }

    private void syncActionBarArrowState()
    {
        boolean shouldShowBackButton = (getSupportFragmentManager().getBackStackEntryCount() > 0);
        if (shouldShowBackButton)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        else
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    public void showFragment(Fragment fragment, boolean addToBackStack)
    {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        if (addToBackStack) {
            ft.addToBackStack(null);
        }

        ft.commit();
    }

    public void showMainProgressBar()
    {
        if (mProgressBar.getVisibility() != View.VISIBLE)
            mProgressBar.setVisibility(View.VISIBLE);
    }

    public void dismissMainProgressBar()
    {
        if (mProgressBar.getVisibility() == View.VISIBLE)
            mProgressBar.setVisibility(View.GONE);
    }
}