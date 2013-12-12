package com.puzheng.the_genuine;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import com.puzheng.the_genuine.data_structure.ProductResponse;
import com.puzheng.the_genuine.data_structure.VerificationInfo;
import com.puzheng.the_genuine.netutils.WebService;
import com.puzheng.the_genuine.utils.Misc;
import com.puzheng.the_genuine.utils.PoliteBackgroundTask;
import com.puzheng.the_genuine.views.NavBar;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.viewpagerindicator.CirclePageIndicator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SPUActivity extends FragmentActivity implements ViewPager.OnPageChangeListener, TabHost.OnTabChangeListener {

    private static final String TAG = "SPUActivity";
    private ViewPager viewPager;
    private VerificationInfo verificationInfo;
    private ProductResponse productResponse;
    private ViewPager viewPagerCover;
    private TabHost tabHost;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onPageScrollStateChanged(int i) {
        int pos = viewPager.getCurrentItem();
        tabHost.setCurrentTab(pos);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onTabChanged(String s) {
        int pos = tabHost.getCurrentTab();
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); ++i) {
            int color = getResources().getColor(android.R.color.darker_gray);
            if (i == pos) {
                color = getResources().getColor(R.color.highlighted_tab);
            }
            TextView title = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            title.setTextColor(color);
        }
        viewPager.setCurrentItem(pos);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == MyApp.LOGIN_ACTION) {
                doAddFavor();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        verificationInfo = getIntent().getParcelableExtra(MainActivity.TAG_VERIFICATION_INFO);
        productResponse = getIntent().getParcelableExtra(MainActivity.TAG_PRODUCT_RESPONSE);

        if (verificationInfo == null && productResponse == null) {
            throw new IllegalArgumentException("必须传入产品信息或者验证信息");
        }

        setupActionBar();
        shareInit();
        favorInit();

        if (verificationInfo == null) {
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setVisibility(View.GONE);
        }


        RatingBar rb = (RatingBar) findViewById(R.id.productRatingBar);
        rb.setRating(getRating());

        Button button = (Button) findViewById(R.id.buttonComment);

        button.setText("评论\n(" + Misc.humanizeNum(getCommentsCnt()) + ")");
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SPUActivity.this, CommentsActivity.class);
                intent.putExtra(Constants.TAG_SPU_ID, verificationInfo.getSku().getSpu().getId());
                startActivity(intent);
            }
        });


        viewPagerCover = (ViewPager) findViewById(R.id.viewPagerCover);
        viewPagerCover.setAdapter(new MyCoverAdapter(getSupportFragmentManager(), getPicUrlList()));
        CirclePageIndicator titleIndicator = (CirclePageIndicator) findViewById(R.id.titles);
        titleIndicator.setViewPager(viewPagerCover);


        tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        String s = "验证信息";
        if (productResponse != null) {
            s = "产品信息";
        }
        TabHost.TabSpec tabSpec = tabHost.newTabSpec("tab1").setIndicator(s);
        tabSpec.setContent(new MyTabFactory(this));
        tabHost.addTab(tabSpec);

        s = "周边推荐(" + Misc.humanizeNum(getNearbyRecommendationsCnt()) + ")";
        tabSpec = tabHost.newTabSpec("tab2").setIndicator(s);
        tabSpec.setContent(new MyTabFactory(this));
        tabHost.addTab(tabSpec);

        s = "同厂推荐(" + Misc.humanizeNum(getSameVendorRecommendationsCnt()) + ")";
        tabSpec = tabHost.newTabSpec("tab3").setIndicator(s);
        tabSpec.setContent(new MyTabFactory(this));
        tabHost.addTab(tabSpec);
        tabHost.setOnTabChangedListener(this);


        setBottomTabs();

        viewPager = (ViewPager) findViewById(R.id.viewPagerBottom);
        viewPager.setAdapter(new MyPageAdapter(getSupportFragmentManager()));
        viewPager.setOnPageChangeListener(this);
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.good);
        mediaPlayer.setLooping(false);
        mediaPlayer.start();

        NavBar navBar = (NavBar) findViewById(R.id.navBar);
        navBar.setContext(SPUActivity.this);
    }

    private void doAddFavor() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SPUActivity.this);
        builder.setTitle("收藏");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PoliteBackgroundTask.Builder<Boolean> task = new PoliteBackgroundTask.Builder<Boolean>(SPUActivity.this);
                task.msg("正在收藏");
                task.run(new PoliteBackgroundTask.XRunnable<Boolean>() {
                    @Override
                    public Boolean run() throws Exception {
                        try {
                            return WebService.getInstance(SPUActivity.this).addFavor(getProductId());
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }).after(new PoliteBackgroundTask.OnAfter<Boolean>() {
                    @Override
                    public void onAfter(Boolean aBoolean) {
                        if (aBoolean) {
                            Toast.makeText(SPUActivity.this, "收藏成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SPUActivity.this, "收藏失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).create().start();
            }
        }).setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private void favorInit() {
        ImageButton button = (ImageButton) findViewById(R.id.imageButtonFavor);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MyApp.getCurrentUser() == null) {
                    MyApp.doLoginIn(SPUActivity.this);
                } else {
                    doAddFavor();
                }

            }
        });
    }

    private int getCommentsCnt() {
        return verificationInfo != null ? verificationInfo.getCommentsCnt() : productResponse.getCommentsCnt();
    }

    private int getNearbyRecommendationsCnt() {
        return verificationInfo != null ? verificationInfo.getNearbyRecommendationsCnt() : productResponse.getNearbyRecommendationsCnt();
    }

    private List<String> getPicUrlList() {
        return verificationInfo != null ? verificationInfo.getSku().getSpu().getPicUrlList() : productResponse.getSPU().getPicUrlList();
    }

    private int getProductId() {
        return verificationInfo != null ? verificationInfo.getSku().getSpu().getId() : productResponse.getSPU().getId();
    }

    private float getRating() {
        return verificationInfo != null ? verificationInfo.getSku().getSpu().getRating() : productResponse.getSPU().getRating();
    }

    private int getSameVendorRecommendationsCnt() {
        return verificationInfo != null ? verificationInfo.getSameVendorRecommendationsCnt() : productResponse.getSameVendorRecommendationsCnt();
    }

    private int getVendorId() {
        return verificationInfo != null ? verificationInfo.getSku().getSpu().getVendorId() :
                productResponse.getSPU().getVendorId();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setBottomTabs() {
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); ++i) {
            View v = tabHost.getTabWidget().getChildTabViewAt(i);
            v.setBackground(getResources().getDrawable(R.drawable.tab_indicator_holo));
            TextView title = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            int color = getResources().getColor(android.R.color.darker_gray);
            if (i == 0) {
                color = getResources().getColor(R.color.highlighted_tab);
            }
            title.setTextColor(color);
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            if (verificationInfo != null) {
                getActionBar().setTitle(verificationInfo.getSku().getSpu().getName());
            } else {
                getActionBar().setTitle(productResponse.getSPU().getName());
            }
        }
    }

    private void shareInit() {
        final UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.share",
                RequestType.SOCIAL);

        mController.setShareContent("360真品鉴别让您不再上当, http://www.foo.com");
    /*
            mController.setShareMedia(new UMImage(this,
                    "http://www.umeng.com/images/pic/banner_module_social.png"));
    */
        mController.getConfig().removePlatform(SHARE_MEDIA.EMAIL, SHARE_MEDIA.DOUBAN, SHARE_MEDIA.RENREN);
        String appID = "wx061490cf3011fbd0";
        // 微信图文分享必须设置一个url
        String contentUrl = "http://www.umeng.com/social";
        // 添加微信平台，参数1为当前Activity, 参数2为用户申请的AppID, 参数3为点击分享内容跳转到的目标url
        mController.getConfig().supportWXPlatform(this, appID, contentUrl);
        // 支持微信朋友圈
        mController.getConfig().supportWXCirclePlatform(this, appID, contentUrl);

        ImageButton imageButton = (ImageButton) findViewById(R.id.imageButtonShare);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 打开平台选择面板，参数2为打开分享面板时是否强制登录,false为不强制登录
                mController.openShare(SPUActivity.this, false);
            }
        });
    }

    class MyTabFactory implements TabHost.TabContentFactory {

        private final Context mContext;

        public MyTabFactory(Context context) {
            mContext = context;
        }

        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    }

    class MyPageAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;

        public MyPageAdapter(FragmentManager fm) {
            super(fm);
            fragments = new ArrayList<Fragment>();
            if (verificationInfo != null) {
                fragments.add(VerificationInfoFragment.getInstance(SPUActivity.this, verificationInfo));
            } else {
                fragments.add(SPUFragment.getInstance(SPUActivity.this, productResponse.getSPU()));
            }
            fragments.add(RecommendationsFragment.createNearByProductsFragment(SPUActivity.this, getProductId()));

            fragments.add(RecommendationsFragment.createSameVendorProductsFragment(SPUActivity.this, getProductId()));
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }
    }

    class MyCoverAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;

        public MyCoverAdapter(FragmentManager fm, List<String> picUrlList) {
            super(fm);
            fragments = new ArrayList<Fragment>();
            for (String url : picUrlList) {
                fragments.add(new CoverFragment(SPUActivity.this, url));
                Log.d(TAG, url);
            }
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }
    }
}