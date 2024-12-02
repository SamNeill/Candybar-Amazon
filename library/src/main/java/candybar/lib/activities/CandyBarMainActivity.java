package candybar.lib.activities;

import static candybar.lib.helpers.DrawableHelper.getDrawableId;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.FileHelper;
import com.danimahardhika.android.helpers.core.SoftKeyboardHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.danimahardhika.android.helpers.license.LicenseHelper;
import com.danimahardhika.android.helpers.permission.PermissionCode;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import candybar.lib.R;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.databases.Database;
import candybar.lib.fragments.AboutFragment;
import candybar.lib.fragments.ApplyFragment;
import candybar.lib.fragments.FAQsFragment;
import candybar.lib.fragments.HomeFragment;
import candybar.lib.fragments.IconsBaseFragment;
import candybar.lib.fragments.PresetsFragment;
import candybar.lib.fragments.RequestFragment;
import candybar.lib.fragments.SettingsFragment;
import candybar.lib.fragments.WallpapersFragment;
import candybar.lib.fragments.dialog.ChangelogFragment;
import candybar.lib.fragments.dialog.InAppBillingFragment;
import candybar.lib.fragments.dialog.IntentChooserFragment;
import candybar.lib.fragments.dialog.PrivacyPolicyDialog;
import candybar.lib.fragments.dialog.RequestConsentDialog;
import candybar.lib.helpers.ConfigurationHelper;
import candybar.lib.helpers.IntentHelper;
import candybar.lib.helpers.JsonHelper;
import candybar.lib.helpers.LicenseCallbackHelper;
import candybar.lib.helpers.LocaleHelper;
import candybar.lib.helpers.NavigationViewHelper;
import candybar.lib.helpers.RequestHelper;
import candybar.lib.helpers.ThemeHelper;
import candybar.lib.helpers.TypefaceHelper;
import candybar.lib.helpers.WallpaperHelper;
import candybar.lib.items.Home;
import candybar.lib.items.Icon;
import candybar.lib.items.InAppBilling;
import candybar.lib.items.Purchase;
import candybar.lib.items.Wallpaper;
import candybar.lib.preferences.Preferences;
import candybar.lib.items.Theme;
import candybar.lib.preferences.Preferences;
import candybar.lib.services.CandyBarService;
import candybar.lib.tasks.IconRequestTask;
import candybar.lib.tasks.IconsLoaderTask;
import candybar.lib.tasks.WallpaperThumbPreloaderTask;
import candybar.lib.utils.CandyBarGlideModule;
import candybar.lib.utils.Extras;
import candybar.lib.utils.InAppBillingClient;
import candybar.lib.utils.listeners.InAppBillingListener;
import candybar.lib.utils.listeners.RequestListener;
import candybar.lib.utils.listeners.SearchListener;
import candybar.lib.utils.listeners.WallpapersListener;
import candybar.lib.utils.views.HeaderView;
import candybar.lib.items.BillingResult;
import candybar.lib.utils.BillingProcessor;
import candybar.lib.items.Request;
import candybar.lib.utils.License;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-2016 Dani Mahardhika
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

public abstract class CandyBarMainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback, RequestListener, InAppBillingListener,
        SearchListener, WallpapersListener {

    private TextView mToolbarTitle;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private BottomNavigationView mBottomNavigationView;

    private Extras.Tag mFragmentTag;
    private int mPosition, mLastPosition;
    private ActionBarDrawerToggle mDrawerToggle;
    private FragmentManager mFragManager;
    private LicenseHelper mLicenseHelper;

    private boolean mIsMenuVisible = true;
    private boolean prevIsDarkTheme;
    private boolean isBottomNavigation;

    public static List<Request> sMissedApps;
    public static List<Icon> sSections;
    public static Home sHomeIcon;
    public static int sInstalledAppsCount;
    public static int sIconsCount;

    private ActivityConfiguration mConfig;

    private Handler mTimesVisitedHandler;
    private Runnable mTimesVisitedRunnable;

    private static final int NOTIFICATION_PERMISSION_CODE = 10;

    @NonNull
    public abstract ActivityConfiguration onInit();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show privacy policy dialog before anything else
        PrivacyPolicyDialog.show(this);

        final boolean isMaterialYou = Preferences.get(this).isMaterialYou();
        final int nightMode;
        switch (Preferences.get(this).getTheme()) {
            case LIGHT:
                nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case DARK:
                nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            default:
                nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }
        AppCompatDelegate.setDefaultNightMode(nightMode);

        LocaleHelper.setLocale(this);
        super.setTheme(isMaterialYou ? R.style.CandyBar_Theme_App_MaterialYou : R.style.CandyBar_Theme_App_DayNight);
        setContentView(R.layout.activity_main);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.navigation_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mToolbarTitle = findViewById(R.id.toolbar_title);

        toolbar.setPopupTheme(isMaterialYou ? R.style.CandyBar_Theme_App_MaterialYou : R.style.CandyBar_Theme_App_DayNight);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        mFragManager = getSupportFragmentManager();

        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        isBottomNavigation = CandyBarApplication.getConfiguration().getNavigationViewStyle() 
                        == CandyBarApplication.NavigationViewStyle.BOTTOM_NAVIGATION;
        
        // Hide navigation icon and lock drawer when using bottom navigation
        if (isBottomNavigation) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setHomeButtonEnabled(false);
            }
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
        
        initNavigationView(toolbar);
        initNavigationViewHeader();
        
        // Show/hide navigation views based on style
        mNavigationView.setVisibility(isBottomNavigation ? View.GONE : View.VISIBLE);
        mBottomNavigationView.setVisibility(isBottomNavigation ? View.VISIBLE : View.GONE);
        
        if (isBottomNavigation) {
            initBottomNavigation();
        }

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.topMargin = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            findViewById(R.id.inset_padding).getLayoutParams().height = params.topMargin;
            return WindowInsetsCompat.CONSUMED;
        });

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // Set status bar icons color based on theme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = getWindow().getDecorView();
            int flags = decorView.getSystemUiVisibility();
            if (!ThemeHelper.isDarkTheme(this)) {
                // Light theme -> dark status bar icons
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                // Dark theme -> light status bar icons
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!ThemeHelper.isDarkTheme(this)) {
                    // Light theme -> dark navigation bar icons
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                } else {
                    // Dark theme -> light navigation bar icons
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }
            }
            decorView.setSystemUiVisibility(flags);
        }

        try {
            startService(new Intent(this, CandyBarService.class));
        } catch (IllegalStateException e) {
            LogUtil.e("Unable to start CandyBarService. App is probably running in background.");
        }

        //Todo: wait until google fix the issue, then enable wallpaper crop again on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Preferences.get(this).setCropWallpaper(false);
        }

        mConfig = onInit();
        InAppBillingClient.get(this).init();

        mPosition = mLastPosition = 0;
        if (savedInstanceState != null) {
            mPosition = mLastPosition = savedInstanceState.getInt(Extras.EXTRA_POSITION, 0);
            onSearchExpanded(false);
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            int position = bundle.getInt(Extras.EXTRA_POSITION, -1);
            if (position >= 0 && position < 6) {
                mPosition = mLastPosition = position;
            }
        }

        IntentHelper.sAction = IntentHelper.getAction(getIntent());
        if (IntentHelper.sAction == IntentHelper.ACTION_DEFAULT) {
            setFragment(getFragment(mPosition));
        } else {
            setFragment(getActionFragment(IntentHelper.sAction));
        }

        checkWallpapers();
        new WallpaperThumbPreloaderTask(this).execute();
        new IconRequestTask(this).executeOnThreadPool();
        new IconsLoaderTask(this).execute();

        /*
        The below code does this
        #1. If new version - set `firstRun` to `true`
        #2. If `firstRun` equals `true`, run the following steps
            #X. License check
                - Enabled: Run check, when completed run #Y
                - Disabled: Run #Y
            #Y. Reset icon request limit, clear cache and show changelog
        */

        if (Preferences.get(this).isNewVersion()) {
            // Check licenses on new version
            Preferences.get(this).setFirstRun(true);
        }

        final Runnable askNotificationPermission = () -> {
            final Runnable showToast = () -> {
                Toast.makeText(this, getResources().getString(R.string.permission_notification_denied_1), Toast.LENGTH_LONG).show();
                Toast.makeText(this, getResources().getString(R.string.permission_notification_denied_2), Toast.LENGTH_LONG).show();
            };

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && CandyBarApplication.getConfiguration().isNotificationEnabled()) {
                if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                    CandyBarApplication.getConfiguration().getNotificationHandler().setMode(Preferences.get(this).isNotificationsEnabled());
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        int permissionState = ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS);
                        if (permissionState != PackageManager.PERMISSION_GRANTED) {
                            if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                                showToast.run();
                            } else {
                                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
                            }
                        }
                    } else {
                        showToast.run();
                    }
                }
            }
        };

        final Runnable onNewVersion = () -> {
            ChangelogFragment.showChangelog(mFragManager, askNotificationPermission);
            File cache = getCacheDir();
            FileHelper.clearDirectory(cache);
        };

        if (Preferences.get(this).isFirstRun()) {
            final Runnable checkLicenseIfEnabled = () -> {
                final Runnable onAllChecksCompleted = () -> {
                    Preferences.get(this).setFirstRun(false);
                    onNewVersion.run();
                };

                if (mConfig.isLicenseCheckerEnabled()) {
                    mLicenseHelper = new LicenseHelper(this);
                    mLicenseHelper.run(mConfig.getLicenseKey(), mConfig.getRandomString(),
                            new LicenseCallbackHelper(this, onAllChecksCompleted));
                } else {
                    onAllChecksCompleted.run();
                }
            };

            checkLicenseIfEnabled.run();

            return;
        }

        if (mConfig.isLicenseCheckerEnabled() && !Preferences.get(this).isLicensed()) {
            finish();
        }

        if (getResources().getBoolean(R.bool.enable_in_app_review)) {
            int timesVisited = Preferences.get(this).getTimesVisited();
            int afterVisits = getResources().getInteger(R.integer.in_app_review_after_visits);
            int nextReviewVisitIdx = Preferences.get(this).getNextReviewVisit();

            if (timesVisited == afterVisits || (timesVisited > afterVisits && timesVisited == nextReviewVisitIdx)) {
                ReviewManager manager = ReviewManagerFactory.create(this);
                Task<ReviewInfo> request = manager.requestReviewFlow();
                request.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ReviewInfo reviewInfo = task.getResult();
                        manager.launchReviewFlow(this, reviewInfo);

                        Preferences.get(this).setNextReviewVisit(timesVisited + 3);
                        // We are scheduling next review to be on 3rd visit from the current visit
                    } else {
                        LogUtil.e(Log.getStackTraceString(task.getException()));
                    }
                });
            }

            mTimesVisitedHandler = new Handler(Looper.getMainLooper());
            mTimesVisitedRunnable = () -> Preferences.get(this).setTimesVisited(timesVisited + 1);
            mTimesVisitedHandler.postDelayed(mTimesVisitedRunnable, getResources().getInteger(R.integer.in_app_review_visit_time) * 1000L);
        }

        askNotificationPermission.run();

        // Show privacy policy dialog
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (!isBottomNavigation) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleHelper.setLocale(this);
        if (mIsMenuVisible && !isBottomNavigation) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
        invalidateOptionsMenu(); // Refresh overflow menu
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        LocaleHelper.setLocale(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        int action = IntentHelper.getAction(intent);
        if (action != IntentHelper.ACTION_DEFAULT)
            setFragment(getActionFragment(action));
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        RequestHelper.checkPiracyApp(this);
        IntentHelper.sAction = IntentHelper.getAction(getIntent());
        super.onResume();
        InAppBillingClient.get(this).checkForUnprocessedPurchases();
    }

    @Override
    protected void onDestroy() {
        InAppBillingClient.get(this).destroy();

        if (mLicenseHelper != null) {
            mLicenseHelper.destroy();
        }

        CandyBarMainActivity.sMissedApps = null;
        CandyBarMainActivity.sHomeIcon = null;
        stopService(new Intent(this, CandyBarService.class));
        Database.get(this.getApplicationContext()).closeDatabase();
        if (mTimesVisitedHandler != null) {
            mTimesVisitedHandler.removeCallbacks(mTimesVisitedRunnable);
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(Extras.EXTRA_POSITION, mPosition);
        Database.get(this.getApplicationContext()).closeDatabase();
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (isBottomNavigation) {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
            
            // Check if current fragment is from overflow menu
            if (currentFragment instanceof FAQsFragment ||
                currentFragment instanceof SettingsFragment ||
                currentFragment instanceof AboutFragment ||
                currentFragment instanceof PresetsFragment) {
                
                // Return to home fragment
                Fragment homeFragment = new HomeFragment();
                setFragment(homeFragment);
                return;
            }
        }
        
        if (mFragManager.getBackStackEntryCount() > 0) {
            clearBackStack();
            return;
        }

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }

        if (mFragmentTag != Extras.Tag.HOME) {
            mPosition = mLastPosition = 0;
            setFragment(getFragment(mPosition));
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionCode.STORAGE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate();
                return;
            }
            Toast.makeText(this, R.string.permission_storage_denied, Toast.LENGTH_LONG).show();
        }
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CandyBarApplication.getConfiguration().getNotificationHandler().setMode(Preferences.get(this).isNotificationsEnabled());
            } else {
                Toast.makeText(this, getResources().getString(R.string.permission_notification_denied_1), Toast.LENGTH_LONG).show();
                Toast.makeText(this, getResources().getString(R.string.permission_notification_denied_2), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPiracyAppChecked(boolean isPiracyAppInstalled) {
        MenuItem menuItem = mNavigationView.getMenu().findItem(R.id.navigation_view_request);
        if (menuItem != null) {
            menuItem.setVisible(getResources().getBoolean(
                    R.bool.enable_icon_request) || !isPiracyAppInstalled);
        }
    }

    @Override
    public void onRequestSelected(int count) {
        if (mFragmentTag == Extras.Tag.REQUEST) {
            String title = getResources().getString(R.string.navigation_view_request);
            if (count > 0) title += " (" + count + ")";
            mToolbarTitle.setText(title);
        }
    }

    @Override
    public void onBuyPremiumRequest() {
        if (!License.isPremiumRequestEnabled()) {
            return;
        }

        if (Preferences.get(this).isPremiumRequest()) {
            RequestHelper.showPremiumRequestStillAvailable(this);
            return;
        }

        if (License.isRestorePurchasesEnabled()) {
            // Check for existing purchases
            // ... rest of code ...
        } else {
            showInAppBillingDialog();
        }
    }

    @Override
    public void onRequestBuilt(Intent intent, int type) {
        if (type == IntentChooserFragment.ICON_REQUEST) {
            if (RequestFragment.sSelectedRequests == null)
                return;

            if (Preferences.get(this).isPremiumRequest()) {
                int count = Preferences.get(this).getPremiumRequestCount() - RequestFragment.sSelectedRequests.size();
                Preferences.get(this).setPremiumRequestCount(count);
                if (count == 0) {
                    InAppBillingClient.get(this).getProcessor().queryPurchases(
                        new BillingProcessor.QueryPurchasesCallback() {
                            @Override
                            public void onSuccess(List<Purchase> purchases) {
                                String premiumRequestProductId = Preferences.get(CandyBarMainActivity.this).getPremiumRequestProductId();
                                for (Purchase purchase : purchases) {
                                    if (purchase.getProducts().contains(premiumRequestProductId)) {
                                        InAppBillingClient.get(CandyBarMainActivity.this).getProcessor()
                                            .consumePurchase(purchase.getPurchaseToken(),
                                                new BillingProcessor.ConsumeCallback() {
                                                    @Override
                                                    public void onSuccess() {
                                                        Preferences.get(CandyBarMainActivity.this).setPremiumRequest(false);
                                                        Preferences.get(CandyBarMainActivity.this).setPremiumRequestProductId("");
                                                    }

                                                    @Override
                                                    public void onError(String error) {
                                                        RequestHelper.showPremiumRequestConsumeFailed(CandyBarMainActivity.this);
                                                    }
                                                }
                                            );
                                        break;
                                    }
                                }
                            }

                            @Override
                            public void onError(String error) {
                                LogUtil.e("Failed to query purchases: " + error);
                            }
                        }
                    );
                }
            }
        }

        if (intent != null) {
            try {
                startActivity(intent);
            } catch (IllegalArgumentException e) {
                startActivity(Intent.createChooser(intent,
                        getResources().getString(R.string.app_client)));
            }
        }

        CandyBarApplication.sRequestProperty = null;
        CandyBarApplication.sZipPath = null;
    }

    @Override
    public void onRestorePurchases() {
        InAppBillingClient.get(this).getProcessor().queryPurchases(
            new BillingProcessor.QueryPurchasesCallback() {
                @Override
                public void onSuccess(List<Purchase> purchases) {
                    List<String> productIds = new ArrayList<>();
                    for (Purchase purchase : purchases) {
                        productIds.add(purchase.getProducts().get(0));
                    }
                    runOnUiThread(() -> {
                        SettingsFragment fragment = (SettingsFragment) mFragManager
                            .findFragmentByTag(Extras.Tag.SETTINGS.value);
                        if (fragment != null) fragment.restorePurchases(productIds,
                                mConfig.getPremiumRequestProductsId(), 
                                mConfig.getPremiumRequestProductsCount());
                    });
                }

                @Override
                public void onError(String error) {
                    LogUtil.e("Failed to load purchase data: " + error);
                }
            }
        );
    }

    @Override
    public void onProcessPurchase(Purchase purchase) {
        if (Preferences.get(this).getInAppBillingType() == InAppBilling.DONATE) {
            InAppBillingClient.get(this).getProcessor().consumePurchase(
                purchase.getPurchaseToken(),
                new BillingProcessor.ConsumeCallback() {
                    @Override
                    public void onSuccess() {
                        Preferences.get(CandyBarMainActivity.this).setInAppBillingType(-1);
                        runOnUiThread(() -> new MaterialDialog.Builder(CandyBarMainActivity.this)
                                .typeface(TypefaceHelper.getMedium(CandyBarMainActivity.this), 
                                        TypefaceHelper.getRegular(CandyBarMainActivity.this))
                                .title(R.string.navigation_view_donate)
                                .content(R.string.donation_success)
                                .positiveText(R.string.close)
                                .show());
                    }

                    @Override
                    public void onError(String error) {
                        LogUtil.e("Failed to consume donation product: " + error);
                    }
                }
            );
        } else if (Preferences.get(this).getInAppBillingType() == InAppBilling.PREMIUM_REQUEST) {
            if (!purchase.isAcknowledged()) {
                InAppBillingClient.get(this).getProcessor().acknowledgePurchase(
                    purchase.getPurchaseToken(),
                    new BillingProcessor.AcknowledgeCallback() {
                        @Override
                        public void onSuccess() {
                            Preferences.get(CandyBarMainActivity.this).setPremiumRequest(true);
                            Preferences.get(CandyBarMainActivity.this)
                                    .setPremiumRequestProductId(purchase.getProducts().get(0));
                            Preferences.get(CandyBarMainActivity.this).setInAppBillingType(-1);
                        }

                        @Override
                        public void onError(String error) {
                            LogUtil.e("Failed to acknowledge purchase: " + error);
                        }
                    }
                );
            }
        }
    }

    @Override
    public void onInAppBillingSelected(int type, InAppBilling product) {
        Preferences.get(this).setInAppBillingType(type);
        if (type == InAppBilling.PREMIUM_REQUEST) {
            // Use License settings for premium request
            String[] premiumProductIds = License.getPremiumRequestProductsId();
            int[] premiumProductCounts = License.getPremiumRequestProductsCount();
            
            // Find matching product count
            for (int i = 0; i < premiumProductIds.length; i++) {
                if (premiumProductIds[i].equals(product.getProductId())) {
                    Preferences.get(this).setPremiumRequestCount(premiumProductCounts[i]);
                    Preferences.get(this).setPremiumRequestTotal(premiumProductCounts[i]);
                    break;
                }
            }
        }
        
        InAppBillingClient.get(this).getProcessor().launchBillingFlow(this, product.getProductId());
    }

    @Override
    public void onInAppBillingRequest() {
        if (mFragmentTag == Extras.Tag.REQUEST) {
            RequestFragment fragment = (RequestFragment) mFragManager.findFragmentByTag(Extras.Tag.REQUEST.value);
            if (fragment != null) fragment.prepareRequest();
        }
    }

    @Override
    public void onWallpapersChecked(int wallpaperCount) {
        Preferences.get(this).setAvailableWallpapersCount(wallpaperCount);

        if (mFragmentTag == Extras.Tag.HOME) {
            HomeFragment fragment = (HomeFragment) mFragManager.findFragmentByTag(Extras.Tag.HOME.value);
            if (fragment != null) fragment.resetWallpapersCount();
        }
    }

    @Override
    public void onSearchExpanded(boolean expand) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        mIsMenuVisible = !expand;

        if (expand) {
            int color = ColorHelper.getAttributeColor(this, R.attr.cb_toolbarIcon);
            toolbar.setNavigationIcon(DrawableHelper.getTintedDrawable(
                    this, R.drawable.ic_toolbar_back, color));
            // It does not work and causes issue with back press on icon search fragment
            // toolbar.setNavigationOnClickListener(view -> onBackPressed());
        } else {
            SoftKeyboardHelper.closeKeyboard(this);
            ColorHelper.setStatusBarColor(this, Color.TRANSPARENT, true);
            
            if (!isBottomNavigation) {
                // Only show navigation drawer icon if using side navigation
                if (CandyBarApplication.getConfiguration().getNavigationIcon() == CandyBarApplication.NavigationIcon.DEFAULT) {
                    mDrawerToggle.setDrawerArrowDrawable(new DrawerArrowDrawable(this));
                } else {
                    toolbar.setNavigationIcon(ConfigurationHelper.getNavigationIcon(this,
                            CandyBarApplication.getConfiguration().getNavigationIcon()));
                }
                
                toolbar.setNavigationOnClickListener(view ->
                        mDrawerLayout.openDrawer(GravityCompat.START));
            }
        }

        mDrawerLayout.setDrawerLockMode(expand || isBottomNavigation ? 
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED : DrawerLayout.LOCK_MODE_UNLOCKED);
        supportInvalidateOptionsMenu();
    }

    public void showSupportDevelopmentDialog() {
        InAppBillingFragment.showInAppBillingDialog(mFragManager,
                InAppBilling.DONATE,
                mConfig.getLicenseKey(),
                mConfig.getDonationProductsId(),
                null);
    }

    private void initNavigationView(Toolbar toolbar) {
        // Skip drawer toggle setup if using bottom navigation
        if (isBottomNavigation) {
            return;
        }

        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.txt_open, R.string.txt_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                SoftKeyboardHelper.closeKeyboard(CandyBarMainActivity.this);
                toolbar.setNavigationIcon(ConfigurationHelper.getNavigationIcon(CandyBarMainActivity.this,
                        CandyBarApplication.getConfiguration().getNavigationIcon()));
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                selectPosition(mPosition);
                toolbar.setNavigationIcon(ConfigurationHelper.getNavigationIcon(CandyBarMainActivity.this,
                        CandyBarApplication.getConfiguration().getNavigationIcon()));
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
                if (newState == DrawerLayout.STATE_DRAGGING) {
                    toolbar.setNavigationIcon(ConfigurationHelper.getNavigationIcon(CandyBarMainActivity.this,
                            CandyBarApplication.getConfiguration().getNavigationIcon()));
                }
            }
        };

        if (CandyBarApplication.getConfiguration().getNavigationIcon() == CandyBarApplication.NavigationIcon.DEFAULT) {
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            DrawerArrowDrawable drawerArrowDrawable = new DrawerArrowDrawable(this);
            drawerArrowDrawable.setColor(ColorHelper.getAttributeColor(this, R.attr.cb_toolbarIcon));
            drawerArrowDrawable.setSpinEnabled(true);
            mDrawerToggle.setDrawerArrowDrawable(drawerArrowDrawable);
        } else {
            mDrawerToggle.setDrawerIndicatorEnabled(false);
            toolbar.setNavigationIcon(ConfigurationHelper.getNavigationIcon(this,
                    CandyBarApplication.getConfiguration().getNavigationIcon()));
            toolbar.setNavigationOnClickListener(view ->
                    mDrawerLayout.openDrawer(GravityCompat.START));
        }

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                // Always show navigation icon in side navigation mode
                if (toolbar != null) {
                    toolbar.setNavigationIcon(ConfigurationHelper.getNavigationIcon(CandyBarMainActivity.this,
                            CandyBarApplication.getConfiguration().getNavigationIcon()));
                }
            }
        });

        NavigationViewHelper.initApply(mNavigationView);
        NavigationViewHelper.initIconRequest(mNavigationView);
        NavigationViewHelper.initWallpapers(mNavigationView);
        NavigationViewHelper.initPresets(mNavigationView);

        ColorStateList itemStateList = ContextCompat.getColorStateList(this,
                R.color.navigation_view_item_highlight);
        mNavigationView.setItemTextColor(itemStateList);
        mNavigationView.setItemIconTintList(itemStateList);
//        Drawable background = ContextCompat.getDrawable(this,
//                ThemeHelper.isDarkTheme(this) ?
//                        R.drawable.navigation_view_item_background_dark :
//                        R.drawable.navigation_view_item_background);
//        mNavigationView.setItemBackground(background);
        mNavigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_view_home) mPosition = Extras.Tag.HOME.idx;
            else if (id == R.id.navigation_view_apply) mPosition = Extras.Tag.APPLY.idx;
            else if (id == R.id.navigation_view_icons) mPosition = Extras.Tag.ICONS.idx;
            else if (id == R.id.navigation_view_request) mPosition = Extras.Tag.REQUEST.idx;
            else if (id == R.id.navigation_view_wallpapers) mPosition = Extras.Tag.WALLPAPERS.idx;
            else if (id == R.id.navigation_view_kustom) mPosition = Extras.Tag.PRESETS.idx;
            else if (id == R.id.navigation_view_settings) mPosition = Extras.Tag.SETTINGS.idx;
            else if (id == R.id.navigation_view_faqs) mPosition = Extras.Tag.FAQS.idx;
            else if (id == R.id.navigation_view_about) mPosition = Extras.Tag.ABOUT.idx;

            item.setChecked(true);
            mDrawerLayout.closeDrawers();
            return true;
        });
    }

    private void initNavigationViewHeader() {
        if (CandyBarApplication.getConfiguration().getNavigationViewHeader() == CandyBarApplication.NavigationViewHeader.NONE) {
            mNavigationView.removeHeaderView(mNavigationView.getHeaderView(0));
            return;
        }

        String imageUrl = getResources().getString(R.string.navigation_view_header);
        String titleText = getResources().getString(R.string.navigation_view_header_title);
        View header = mNavigationView.getHeaderView(0);
        HeaderView image = header.findViewById(R.id.header_image);
        LinearLayout container = header.findViewById(R.id.header_title_container);
        TextView title = header.findViewById(R.id.header_title);
        TextView version = header.findViewById(R.id.header_version);

        if (CandyBarApplication.getConfiguration().getNavigationViewHeader() == CandyBarApplication.NavigationViewHeader.MINI) {
            image.setRatio(16, 9);
        }

        if (titleText.length() == 0) {
            container.setVisibility(View.GONE);
        } else {
            title.setText(titleText);
            try {
                String versionText = "v" + getPackageManager()
                        .getPackageInfo(getPackageName(), 0).versionName;
                version.setText(versionText);
            } catch (Exception ignored) {
            }
        }

        if (ColorHelper.isValidColor(imageUrl)) {
            image.setBackgroundColor(Color.parseColor(imageUrl));
            return;
        }

        if (!URLUtil.isValidUrl(imageUrl)) {
            imageUrl = "drawable://" + getDrawableId(imageUrl);
        }

        final Context context = this;
        if (CandyBarGlideModule.isValidContextForGlide(context)) {
            Glide.with(context)
                    .load(imageUrl)
                    .override(720)
                    .optionalCenterInside()
                    .diskCacheStrategy(imageUrl.contains("drawable://")
                            ? DiskCacheStrategy.NONE
                            : DiskCacheStrategy.RESOURCE)
                    .into(image);
        }
    }

    private void checkWallpapers() {
        // Skip wallpaper check if wallpaper_json is empty
        String wallpaperJson = getResources().getString(R.string.wallpaper_json);
        if (wallpaperJson.isEmpty()) {
            return;
        }

        if (Preferences.get(this).isConnectedToNetwork()) {
            new Thread(() -> {
                try {
                    if (WallpaperHelper.getWallpaperType(this) != WallpaperHelper.CLOUD_WALLPAPERS)
                        return;

                    InputStream stream = WallpaperHelper.getJSONStream(this);

                    if (stream != null) {
                        List<?> list = JsonHelper.parseList(stream);
                        if (list == null) return;

                        List<Wallpaper> wallpapers = new ArrayList<>();
                        for (int i = 0; i < list.size(); i++) {
                            Wallpaper wallpaper = JsonHelper.getWallpaper(list.get(i));
                            if (wallpaper != null) {
                                if (!wallpapers.contains(wallpaper)) {
                                    wallpapers.add(wallpaper);
                                } else {
                                    LogUtil.e("Duplicate wallpaper found: " + wallpaper.getURL());
                                }
                            }
                        }

                        this.runOnUiThread(() -> onWallpapersChecked(wallpapers.size()));
                    }
                } catch (IOException e) {
                    LogUtil.e(Log.getStackTraceString(e));
                }
            }).start();
        }

        int size = Preferences.get(this).getAvailableWallpapersCount();
        if (size > 0) {
            onWallpapersChecked(size);
        }
    }

    private void clearBackStack() {
        if (mFragManager.getBackStackEntryCount() > 0) {
            mFragManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            onSearchExpanded(false);
        }
    }

    public void selectPosition(int position) {
        // Get wallpaper_json and kustom status once
        String wallpaperJson = getResources().getString(R.string.wallpaper_json);
        boolean isWallpaperHidden = wallpaperJson.isEmpty();
        boolean isKustomHidden = !getResources().getBoolean(R.bool.enable_kustom);

        // Adjust position if sections are hidden
        if (isWallpaperHidden && position >= 4) {
            position = position - 1;
        }
        if (isKustomHidden && position >= (isWallpaperHidden ? 4 : 5)) {
            position = position - 1;
        }

        if (position == 3) { // Request position
            if (!getResources().getBoolean(R.bool.enable_icon_request) &&
                    getResources().getBoolean(R.bool.enable_premium_request)) {
                if (!Preferences.get(this).isPremiumRequestEnabled()) {
                    mPosition = mLastPosition;
                    mNavigationView.getMenu().getItem(mPosition).setChecked(true);
                    onBuyPremiumRequest();
                    return;
                }
            }
        }

        if (position == 4 && !isWallpaperHidden) { // Wallpapers position (only if not hidden)
            if (WallpaperHelper.getWallpaperType(this) == WallpaperHelper.EXTERNAL_APP) {
                mPosition = mLastPosition;
                mNavigationView.getMenu().getItem(mPosition).setChecked(true);
                WallpaperHelper.launchExternalApp(CandyBarMainActivity.this);
                return;
            }
        }

        if (position != mLastPosition) {
            mLastPosition = mPosition = position;
            setFragment(getFragment(position));
        }
    }

    private void setFragment(Fragment fragment) {
        if (fragment == null) return;
        clearBackStack();

        try {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction()
                    .replace(R.id.container, fragment, fragment.getClass().getSimpleName());
            if (mIsMenuVisible) ft.commit();
            else ft.commitAllowingStateLoss();

            // Update toolbar title based on fragment
            String title = "";
            int menuItemId = -1;

            if (fragment instanceof HomeFragment) {
                title = getResources().getString(R.string.navigation_view_home);
                menuItemId = R.id.navigation_view_home;
            } else if (fragment instanceof ApplyFragment) {
                title = getResources().getString(R.string.navigation_view_apply);
                menuItemId = R.id.navigation_view_apply;
            } else if (fragment instanceof IconsBaseFragment) {
                title = getResources().getString(R.string.navigation_view_icons);
                menuItemId = R.id.navigation_view_icons;
            } else if (fragment instanceof RequestFragment) {
                title = getResources().getString(R.string.navigation_view_request);
                menuItemId = R.id.navigation_view_request;
            } else if (fragment instanceof WallpapersFragment) {
                title = getResources().getString(R.string.navigation_view_wallpapers);
                menuItemId = R.id.navigation_view_wallpapers;
            } else if (fragment instanceof PresetsFragment) {
                title = getResources().getString(R.string.navigation_view_kustom);
            } else if (fragment instanceof FAQsFragment) {
                title = getResources().getString(R.string.navigation_view_faqs);
            } else if (fragment instanceof SettingsFragment) {
                title = getResources().getString(R.string.navigation_view_settings);
            } else if (fragment instanceof AboutFragment) {
                title = getResources().getString(R.string.navigation_view_about);
            }

            if (!title.isEmpty() && mToolbarTitle != null) {
                mToolbarTitle.setText(title);
            }

            // Update bottom navigation selection without triggering listener
            if (isBottomNavigation && menuItemId != -1 && mBottomNavigationView != null) {
                mBottomNavigationView.setOnItemSelectedListener(null);
                mBottomNavigationView.setSelectedItemId(menuItemId);
                initBottomNavigation(); // Restore the listener
            }

        } catch (Exception e) {
            // Handle any potential exceptions
        }
    }

    private Fragment getFragment(int position) {
        // Get wallpaper_json and kustom status once
        String wallpaperJson = getResources().getString(R.string.wallpaper_json);
        boolean isWallpaperHidden = wallpaperJson.isEmpty();
        boolean isKustomHidden = !getResources().getBoolean(R.bool.enable_kustom);

        mFragmentTag = Extras.Tag.HOME;
        if (position == 0) {
            mFragmentTag = Extras.Tag.HOME;
            return new HomeFragment();
        } else if (position == 1) {
            mFragmentTag = Extras.Tag.APPLY;
            return new ApplyFragment();
        } else if (position == 2) {
            mFragmentTag = Extras.Tag.ICONS;
            return new IconsBaseFragment();
        } else if (position == 3) {
            mFragmentTag = Extras.Tag.REQUEST;
            return new RequestFragment();
        } else if (position == 4 && !isWallpaperHidden) {
            mFragmentTag = Extras.Tag.WALLPAPERS;
            return new WallpapersFragment();
        } else if ((position == 4 && isWallpaperHidden && !isKustomHidden) || 
                   (position == 5 && !isWallpaperHidden && !isKustomHidden)) {
            mFragmentTag = Extras.Tag.PRESETS;
            return new PresetsFragment();
        } else if ((position == 4 && isWallpaperHidden && isKustomHidden) ||
                   (position == 5 && ((isWallpaperHidden && !isKustomHidden) || (!isWallpaperHidden && isKustomHidden))) ||
                   (position == 6 && !isWallpaperHidden && !isKustomHidden)) {
            mFragmentTag = Extras.Tag.SETTINGS;
            return new SettingsFragment();
        } else if ((position == 5 && isWallpaperHidden && isKustomHidden) ||
                   (position == 6 && ((isWallpaperHidden && !isKustomHidden) || (!isWallpaperHidden && isKustomHidden))) ||
                   (position == 7 && !isWallpaperHidden && !isKustomHidden)) {
            mFragmentTag = Extras.Tag.FAQS;
            return new FAQsFragment();
        } else if ((position == 6 && isWallpaperHidden && isKustomHidden) ||
                   (position == 7 && ((isWallpaperHidden && !isKustomHidden) || (!isWallpaperHidden && isKustomHidden))) ||
                   (position == 8 && !isWallpaperHidden && !isKustomHidden)) {
            mFragmentTag = Extras.Tag.ABOUT;
            return new AboutFragment();
        }
        return new HomeFragment();
    }

    private Fragment getActionFragment(int action) {
        switch (action) {
            case IntentHelper.ICON_PICKER:
            case IntentHelper.IMAGE_PICKER:
                mPosition = mLastPosition = (mFragmentTag = Extras.Tag.ICONS).idx;
                return new IconsBaseFragment();
            case IntentHelper.WALLPAPER_PICKER:
                if (WallpaperHelper.getWallpaperType(this) == WallpaperHelper.CLOUD_WALLPAPERS) {
                    mPosition = mLastPosition = (mFragmentTag = Extras.Tag.WALLPAPERS).idx;
                    return new WallpapersFragment();
                }
            default:
                mPosition = mLastPosition = (mFragmentTag = Extras.Tag.HOME).idx;
                return new HomeFragment();
        }
    }

    private void showInAppBillingDialog() {
        InAppBillingFragment.showInAppBillingDialog(getSupportFragmentManager(),
                InAppBilling.PREMIUM_REQUEST,
                License.getLicenseKey(),
                License.getPremiumRequestProductsId(),
                License.getPremiumRequestProductsCount());
    }

    private void initBottomNavigation() {
        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        if (mBottomNavigationView == null) return;

        // Hide wallpapers menu item if wallpaper_json is empty
        String wallpaperJson = getResources().getString(R.string.wallpaper_json);
        if (wallpaperJson.isEmpty()) {
            MenuItem wallpapersItem = mBottomNavigationView.getMenu().findItem(R.id.navigation_view_wallpapers);
            if (wallpapersItem != null) {
                wallpapersItem.setVisible(false);
            }
        }

        // Get theme colors
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.cb_colorAccent, typedValue, true);
        int accentColor = typedValue.data;
        
        // Get card background color from theme
        getTheme().resolveAttribute(R.attr.cb_cardBackground, typedValue, true);
        int cardBackground = typedValue.data;
        
        // Get colors based on theme
        boolean isLightTheme = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) 
                == Configuration.UI_MODE_NIGHT_NO;
        int unselectedColor = isLightTheme ? 
                Color.parseColor("#99000000") : // 60% black for light theme
                Color.parseColor("#80FFFFFF"); // 50% white for dark theme
        
        int[][] states = new int[][] {
            new int[] { android.R.attr.state_checked }, // checked
            new int[] { -android.R.attr.state_checked }  // unchecked
        };
        
        int[] colors = new int[] {
            accentColor, // checked color (accent)
            unselectedColor // unchecked color
        };
        
        ColorStateList colorStateList = new ColorStateList(states, colors);
        
        // Create semi-transparent version of accent color for indicator background
        int semiTransparentAccent = Color.argb(64, Color.red(accentColor), 
                                             Color.green(accentColor), 
                                             Color.blue(accentColor));
        
        ColorStateList backgroundColorStateList = new ColorStateList(
            new int[][] {
                new int[] { android.R.attr.state_selected },
                new int[] { android.R.attr.state_checked },
                new int[] { }
            },
            new int[] {
                semiTransparentAccent,
                semiTransparentAccent,
                Color.TRANSPARENT
            }
        );
        
        mBottomNavigationView.setItemIconTintList(colorStateList);
        mBottomNavigationView.setItemTextColor(colorStateList);
        mBottomNavigationView.setBackgroundColor(cardBackground);
        mBottomNavigationView.setItemBackgroundResource(0);

        // Only show pill background for Material You theme
        boolean isMaterialYou = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && 
                Preferences.get(this).isMaterialYou();
        
        if (isMaterialYou) {
            mBottomNavigationView.setItemActiveIndicatorColor(backgroundColorStateList);
            mBottomNavigationView.setItemActiveIndicatorEnabled(true);
        } else {
            mBottomNavigationView.setItemActiveIndicatorEnabled(false);
        }

        mBottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_view_home) {
                mPosition = 0;
                setFragment(getFragment(mPosition));
                return true;
            } else if (id == R.id.navigation_view_icons) {
                mPosition = 2;
                setFragment(getFragment(mPosition));
                return true;
            } else if (id == R.id.navigation_view_wallpapers) {
                mPosition = 4;
                setFragment(getFragment(mPosition));
                return true;
            } else if (id == R.id.navigation_view_apply) {
                mPosition = 1;
                setFragment(getFragment(mPosition));
                return true;
            } else if (id == R.id.navigation_view_request) {
                mPosition = 3;
                setFragment(getFragment(mPosition));
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_overflow, menu);
        
        // Only show overflow menu in bottom navigation mode
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(isBottomNavigation);
        }
        
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.menu_changelog) {
            ChangelogFragment.showChangelog(mFragManager, null);
            return true;
        }
        
        Fragment fragment = null;
        
        if (id == R.id.navigation_view_faqs) {
            fragment = new FAQsFragment();
        } else if (id == R.id.navigation_view_settings) {
            fragment = new SettingsFragment();
        } else if (id == R.id.navigation_view_about) {
            fragment = new AboutFragment();
        } else if (id == R.id.navigation_view_kustom) {
            fragment = new PresetsFragment();
        }
        
        if (fragment != null) {
            setFragment(fragment);
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    public boolean isBottomNavigationEnabled() {
        return isBottomNavigation;
    }

    public static class ActivityConfiguration {

        private boolean mIsLicenseCheckerEnabled;
        private byte[] mRandomString;
        private String mLicenseKey;
        private String[] mDonationProductsId;
        private String[] mPremiumRequestProductsId;
        private int[] mPremiumRequestProductsCount;

        public ActivityConfiguration setLicenseCheckerEnabled(boolean enabled) {
            mIsLicenseCheckerEnabled = enabled;
            return this;
        }

        public ActivityConfiguration setRandomString(@NonNull byte[] randomString) {
            mRandomString = randomString;
            return this;
        }

        public ActivityConfiguration setLicenseKey(@NonNull String licenseKey) {
            mLicenseKey = licenseKey;
            return this;
        }

        public ActivityConfiguration setDonationProductsId(@NonNull String[] productsId) {
            mDonationProductsId = productsId;
            return this;
        }

        public ActivityConfiguration setPremiumRequestProducts(@NonNull String[] ids, @NonNull int[] counts) {
            mPremiumRequestProductsId = ids;
            mPremiumRequestProductsCount = counts;
            return this;
        }

        public boolean isLicenseCheckerEnabled() {
            return mIsLicenseCheckerEnabled;
        }

        public byte[] getRandomString() {
            return mRandomString;
        }

        public String getLicenseKey() {
            return mLicenseKey;
        }

        public String[] getDonationProductsId() {
            return mDonationProductsId;
        }

        public String[] getPremiumRequestProductsId() {
            return mPremiumRequestProductsId;
        }

        public int[] getPremiumRequestProductsCount() {
            return mPremiumRequestProductsCount;
        }
    }

    public void showLauncherNotInstalledDialog(String launcherName) {
        new MaterialDialog.Builder(this)
            .typeface(TypefaceHelper.getMedium(this), TypefaceHelper.getRegular(this))
            .title(getString(R.string.apply_launcher_not_installed_title, launcherName))
            .content(getString(R.string.apply_launcher_not_installed_message, launcherName))
            .positiveText(R.string.close)
            .cancelable(true)
            .show();
    }
}
