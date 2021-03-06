package com.puzheng.lejian;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

/**
 * Activity which displays a register_or_login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity implements BackPressedInterface {
    private BackPressedHandle mBackPressedHandle = new BackPressedHandle();

    // TODO what is this method for?
    @Override
    public void doBackPressed() {
        super.onBackPressed();
    }

    // TODO
    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        if (isTop()) {
            mBackPressedHandle.doBackPressed(this, this);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    public void showLoginFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.main, new LoginFragment());
        ft.commit();
    }

    /**
     * Attempts to sign in or register the account specified by the register_or_login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual register_or_login attempt is made.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        showLoginFragment();

    }

    private boolean isTop() {
        return getIntent().getBooleanExtra("ISTOPACTIVITY", false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RegisterActivity.TAG_REGISTER) {
                setResult(RESULT_OK);
                this.finish();
            }
        }
    }
}
