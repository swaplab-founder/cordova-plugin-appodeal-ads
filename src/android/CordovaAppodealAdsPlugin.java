package emi.indo.cordova.plugin.appodeal.ads;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.appodeal.ads.BannerView;
import com.appodeal.ads.InterstitialCallbacks;
import com.appodeal.ads.RewardedVideoCallbacks;
import com.appodeal.ads.initializing.ApdInitializationCallback;
import com.appodeal.ads.initializing.ApdInitializationError;
import com.appodeal.consent.ConsentInfoUpdateCallback;
import com.appodeal.consent.ConsentManager;
import com.appodeal.consent.ConsentManagerError;
import com.appodeal.consent.ConsentStatus;
import com.appodeal.consent.ConsentUpdateRequestParameters;
import com.appodeal.consent.OnConsentFormDismissedListener;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by EMI INDO So on Fri 22, 2025
 */
public class CordovaAppodealAdsPlugin extends CordovaPlugin {

    private static final String TAG = "CordovaAppodealAdsPlugin";

    private CallbackContext PUBLIC_CALLBACKS = null;

    protected CordovaWebView mCordovaWebView;

    private Activity mActivity;

    private FrameLayout rootLayout;
    private BannerView bannerView = null;
   // private Boolean overlappingBanner = false;
  //  private String isPositionBanner = "bottom";
    private Boolean isBannerLoaded = false;
    private Boolean isCustomWebView = false;


    private Boolean isFullscreen = false;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        mCordovaWebView = webView;
        mActivity = this.cordova.getActivity();

    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        PUBLIC_CALLBACKS = callbackContext;
        if ("initialize".equals(action)) {
            JSONObject options = args.getJSONObject(0);
            boolean setTesting = options.getBoolean("setTesting");
            String appKey = options.getString("appKey");
            boolean setAutoCache = options.getBoolean("setAutoCache");
            int adTypes = Appodeal.INTERSTITIAL | Appodeal.BANNER | Appodeal.REWARDED_VIDEO;
            boolean isAdvancedConsent = options.getBoolean("isAdvancedConsent");
            boolean tagForUnderAgeOfConsent = options.getBoolean("tagForUnderAgeOfConsent");
            boolean setChildDirectedTreatment = options.getBoolean("setChildDirectedTreatment");
            isFullscreen = isFullScreenMode(mActivity);
            this.initializeSDK(setTesting, appKey, setAutoCache, adTypes, isAdvancedConsent, tagForUnderAgeOfConsent, setChildDirectedTreatment, callbackContext);
            return true;

        } else if ("loadInterstitial".equals(action)) {
            JSONObject options = args.getJSONObject(0);
            boolean autoShow = options.getBoolean("autoShow");
            this.loadInterstitial(autoShow);
            return true;

        } else if ("showInterstitial".equals(action)) {
            this.showInterstitial(callbackContext);
            return true;

        } else if ("loadRewarded".equals(action)) {
            JSONObject options = args.getJSONObject(0);
            boolean autoShow = options.getBoolean("autoShow");
            this.loadRewarded(autoShow);
            return true;

        } else if ("showRewarded".equals(action)) {
            this.showRewarded(callbackContext);
            return true;

        } else if ("loadBanner".equals(action)) {
           JSONObject options = args.getJSONObject(0);
           String position = options.getString("position");
           boolean isOverlapping = options.getBoolean("isOverlapping");
           boolean autoShow = options.getBoolean("autoShow");

           try {
               if (!isOverlapping){
                   this.isCustomWebView = true;
               }
              // this.isPositionBanner = position;
              // this.overlappingBanner = isOverlapping;
               this.loadBanner(position, isOverlapping, autoShow, callbackContext);
           } catch (Exception e){
               Log.e(TAG, "loadBanner: " + e.getMessage());
           }
           return true;

        } else if ("showBanner".equals(action)) {
            this.showBanner(callbackContext);
            return true;

        } else if ("destroyBanner".equals(action)) {
            this.destroyBanner(callbackContext);
            return true;

        } else if ("hideBanner".equals(action)) {
            this.hideBanner(callbackContext);
            return true;
        } else if ("revokeUserConsent".equals(action)) {

            this.revokeUserConsent(callbackContext);
            return true;
        }

        callbackContext.error("Unknown action: " + action);
        return false;
    }



    private void initializeSDK(boolean setTesting, String appKey, boolean setAutoCache, int adTypes, boolean isAdvancedConsent, boolean tagForUnderAgeOfConsent, boolean setChildDirectedTreatment, CallbackContext callbackContext) {
        final Activity activity = this.cordova.getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (isAdvancedConsent) {
                    // https://docs.appodeal.com/android/data-protection/gdpr-and-ccpa
                    setAdvancedConsent(appKey, tagForUnderAgeOfConsent);
                }

                Appodeal.setTesting(setTesting);
                Appodeal.setChildDirectedTreatment(setChildDirectedTreatment);
                Appodeal.setAutoCache(adTypes, setAutoCache);
                //Appodeal.setLogLevel(Log.LogLevel.verbose);
                Appodeal.initialize(activity, appKey, adTypes, new ApdInitializationCallback() {
                    @Override
                    public void onInitializationFinished(List<ApdInitializationError> errors) {
                        if (errors == null) {
                            callbackContext.success("on.sdk.initialization");
                        } else {
                            callbackContext.error("on.sdk.initialization.failed: " + errors.toString());
                        }

                    }

                });
            }
        });
    }



    private void showBanner(CallbackContext callbackContext) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (Appodeal.isLoaded(Appodeal.BANNER)) {

                        Appodeal.show(mActivity, Appodeal.BANNER_VIEW);

                        mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.banner.show');");

                    } else {
                        callbackContext.error("Banner not ready for display");
                    }
                    setBannerCallbacks();
                }
            });
        } else {
            callbackContext.error("Activity is null, cannot show banner.");
        }
    }



    private void hideBanner(CallbackContext callbackContext) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (Appodeal.isLoaded(Appodeal.BANNER)) {
                        Appodeal.hide(mActivity, Appodeal.BANNER);
                        if (isCustomWebView) {
                            resetCustomWebView();
                        }
                        mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.banner.hide');");
                        // callbackContext.success("Hide displayed");
                        } else {
                            callbackContext.error("Banner failed to display");
                        }
                  }
            });
        } else {
            callbackContext.error("Activity is null, cannot show banner.");
        }
    }



    private void destroyBanner(CallbackContext callbackContext) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isBannerLoaded = false;
                    if (Appodeal.isLoaded(Appodeal.BANNER)) {
                        if (bannerView != null && bannerView.getParent() != null && rootLayout != null) {
                            rootLayout.removeView(bannerView);
                        }
                        Appodeal.destroy(Appodeal.BANNER);
                        bannerView = null;
                        resetCustomWebView();
                        mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.banner.destroyed');");
                       // callbackContext.success("Banner destroyed.");
                    } else {
                        callbackContext.error("Banner not loaded or not ready to destroy.");
                    }
                }
            });
        } else {
            callbackContext.error("Activity is null, cannot destroy banner.");
        }
    }



    private void resetCustomWebView() {
        if (mActivity == null || mCordovaWebView == null) return;

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final View webView = mCordovaWebView.getView();
                if (webView == null) return;

                final View rootView = (View) webView.getParent();
                if (rootView == null) return;

                rootView.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int totalHeight = rootView.getHeight();

                            ViewGroup.LayoutParams lpRaw = webView.getLayoutParams();
                            if (lpRaw instanceof ViewGroup.MarginLayoutParams) {
                                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) lpRaw;
                                lp.height = totalHeight;
                                lp.topMargin = 0;
                                lp.bottomMargin = 0;
                                webView.setLayoutParams(lp);
                            } else {
                                lpRaw.height = totalHeight;
                                webView.setLayoutParams(lpRaw);
                            }

                            webView.setPadding(0, 0, 0, 0);
                            if (rootView instanceof ViewGroup) {
                                ((ViewGroup) rootView).setPadding(0, 0, 0, 0);
                            }

                            webView.setTranslationY(0f);

                            webView.requestLayout();

                        } catch (Exception e) {
                            if (PUBLIC_CALLBACKS != null) {
                                PUBLIC_CALLBACKS.error("Error resetCustomWebView: " + e.getMessage());
                            }
                        }
                    }
                });
            }
        });
    }





    // if overlapping: false Custom WebView
    private void loadBanner(String position, boolean overlapping, boolean autoShow, CallbackContext callbackContext) {
        if (mActivity == null) {
            callbackContext.error("mActivity is null, it cannot display the banner.");
            return;
        }

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (rootLayout == null) {
                    rootLayout = new FrameLayout(mActivity);
                    FrameLayout.LayoutParams rootParams = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                    );
                    ViewGroup decorView = (ViewGroup) mActivity.getWindow().getDecorView();
                    decorView.addView(rootLayout, rootParams);
                }

                if (bannerView == null) {
                    bannerView = Appodeal.getBannerView(mActivity);
                    FrameLayout.LayoutParams bannerParams = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                    );
                    if (position.equalsIgnoreCase("top")) {
                        bannerParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                    } else {
                        bannerParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                    }
                    bannerView.setLayoutParams(bannerParams);
                    rootLayout.addView(bannerView);
                } else {
                    callbackContext.error("loadBanner: BannerView already exists, using the existing one.");
                }

                boolean isInitialized = Appodeal.isInitialized(Appodeal.BANNER);

                if (isInitialized) {
                    isBannerLoaded = true;
                    mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.banner.load');");
                    if (autoShow) {
                        Appodeal.show(mActivity, Appodeal.BANNER_VIEW);
                        mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.banner.show');");
                        callbackContext.success("Banner successfully displayed.");
                    } else {
                        callbackContext.error("Failed to display the Banner.");
                    }

                } else {

                    callbackContext.error("The BANNER appodeal has not been initialized.");
                }
                setBannerCallbacks();

                int statusBarHeight = 0;
                int navBarHeight = 0;
                int screenHeightPx;
                @SuppressLint("InternalInsetResource") int resourceId = mActivity.getResources().getIdentifier("status_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    statusBarHeight = mActivity.getResources().getDimensionPixelSize(resourceId);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowMetrics wm = mActivity.getWindowManager().getCurrentWindowMetrics();
                    WindowInsets insets = wm.getWindowInsets();
                    navBarHeight = insets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars()).bottom;
                    screenHeightPx = wm.getBounds().height();
                } else {
                    Display display = mActivity.getWindowManager().getDefaultDisplay();
                    Point realSize = new Point();
                    display.getRealSize(realSize);
                    screenHeightPx = realSize.y;
                    Rect rect = new Rect();
                    mActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
                    navBarHeight = Math.max(0, screenHeightPx - rect.height());
                }

                if (bannerView != null) {
                    FrameLayout.LayoutParams currentParams = (FrameLayout.LayoutParams) bannerView.getLayoutParams();
                    if (position.equalsIgnoreCase("top")) {
                        if (!isFullscreen) {
                            currentParams.topMargin = statusBarHeight;
                            currentParams.bottomMargin = 0;
                        } else {
                            currentParams.topMargin = 0;
                            currentParams.bottomMargin = 0;
                        }
                    } else {
                        if (!isFullscreen) {
                            currentParams.bottomMargin = navBarHeight;
                        } else {
                            currentParams.bottomMargin = 0;
                        }
                        currentParams.topMargin = 0;
                    }
                    bannerView.setLayoutParams(currentParams);
                }

                final int finalStatusBarHeight = statusBarHeight;
                final int finalNavBarHeight = navBarHeight;
                final int finalScreenHeightPx = screenHeightPx;

                if (bannerView != null) {
                    bannerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            int bannerHeight = bannerView.getHeight();
                            if (bannerHeight > 0) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    bannerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                } else {
                                    bannerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                }
                                if (mCordovaWebView != null && mCordovaWebView.getView() != null) {
                                    View webView = mCordovaWebView.getView();
                                    FrameLayout.LayoutParams webParams = (FrameLayout.LayoutParams) webView.getLayoutParams();
                                    if (position.equalsIgnoreCase("top")) {
                                        if (!overlapping) {
                                            if (!isFullscreen) {
                                                webParams.topMargin = finalStatusBarHeight;
                                                webParams.bottomMargin = 0;
                                                webParams.height = finalScreenHeightPx - finalStatusBarHeight;
                                            } else {
                                                webParams.topMargin = bannerHeight;
                                                webParams.bottomMargin = 0;
                                                webParams.height = finalScreenHeightPx;
                                            }
                                        }
                                    } else {
                                        if (!overlapping) {
                                            if (isFullscreen) {
                                                webParams.topMargin = 0;
                                                webParams.bottomMargin = 0;
                                                webParams.height = finalScreenHeightPx - bannerHeight;
                                            } else {
                                                webParams.topMargin = 0;
                                                int bottom = finalNavBarHeight + bannerHeight;
                                                webParams.bottomMargin = bottom;
                                                webParams.height = finalScreenHeightPx - finalStatusBarHeight - bottom;
                                            }
                                        } else {
                                                if (isFullscreen) {
                                                    webParams.topMargin = 0;
                                                    webParams.bottomMargin = 0;
                                                    webParams.height = finalScreenHeightPx;
                                                } else {
                                                    webParams.topMargin = 0;
                                                    int bottom = finalNavBarHeight + bannerHeight;
                                                    webParams.bottomMargin = finalNavBarHeight;
                                                    webParams.height = finalScreenHeightPx - bottom;
                                                }
                                        }
                                    }
                                    webView.setLayoutParams(webParams);
                                    webView.requestLayout();
                                }
                            }
                        }
                    });
                }
            }
        });
    }





    private boolean isFullScreenMode(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsets insets = activity.getWindow()
                    .getDecorView()
                    .getRootWindowInsets();
            return insets != null && !insets.isVisible(WindowInsets.Type.statusBars());
        } else {
            @SuppressWarnings("deprecation")
            int flags = activity.getWindow()
                    .getAttributes()
                    .flags;
            return (flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
        }
    }



    private void loadRewarded(boolean autoShow) {

        if (mActivity != null) {

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (Appodeal.isLoaded(Appodeal.REWARDED_VIDEO)) {
                        mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.rewarded.load');");
                        if (autoShow) {
                            setRewardedCallbacks();
                            Appodeal.show(mActivity, Appodeal.REWARDED_VIDEO);

                        } else {

                            setRewardedCallbacks();
                        }

                    } else {
                        setRewardedCallbacks();
                    }
                }

            });

        }

    }



    private void showRewarded(CallbackContext callbackContext) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (Appodeal.isLoaded(Appodeal.REWARDED_VIDEO)) {
                        Appodeal.show(mActivity, Appodeal.REWARDED_VIDEO);
                        callbackContext.success("Rewarded displayed");
                    } else {
                        callbackContext.error("Rewarded not ready for display");
                    }
                }
            });
        }
    }






    private void loadInterstitial(boolean autoShow) {

        if (mActivity != null) {

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (Appodeal.isLoaded(Appodeal.INTERSTITIAL)) {
                        mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.interstitial.load');");
                        if (autoShow) {
                            setInterstitialCallbacks();
                            Appodeal.show(mActivity, Appodeal.INTERSTITIAL);

                        } else {

                            setInterstitialCallbacks();
                        }

                    } else {
                        setInterstitialCallbacks();
                    }
                }
            });

        }

    }



    private void showInterstitial(CallbackContext callbackContext) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (Appodeal.isLoaded(Appodeal.INTERSTITIAL)) {
                        Appodeal.show(mActivity, Appodeal.INTERSTITIAL);
                        callbackContext.success("Interstitial displayed");
                    } else {
                        callbackContext.error("Interstitials not ready for display");
                    }
                }
            });
        }
    }



    private void setBannerCallbacks() {

        if (mActivity != null) {

            Appodeal.setBannerCallbacks(new BannerCallbacks() {
                @Override
                public void onBannerLoaded(int height, boolean isPrecache) {
                    // Called when banner is loaded

                }
                @Override
                public void onBannerFailedToLoad() {
                    if (isCustomWebView) {
                        resetCustomWebView();
                    }
                    mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.banner.failed.load');");
                }
                @Override
                public void onBannerShown() {

                }
                @Override
                public void onBannerShowFailed() {
                    if (isCustomWebView) {
                        resetCustomWebView();
                    }
                    mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.banner.show.failed');");
                }
                @Override
                public void onBannerClicked() {
                    mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.banner.click');");
                }
                @Override
                public void onBannerExpired() {
                    mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.banner.expired');");
                }
            });

        }

    }





    private void setInterstitialCallbacks() {

        if (mActivity != null) {

            Appodeal.setInterstitialCallbacks(new InterstitialCallbacks() {
                @Override
                public void onInterstitialLoaded(boolean isPrecache) {
                    //      LOG.d(TAG, "InterstitialCallbacks.onInterstitialLoaded: isPrecache = " + isPrecache);
                    //  mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.interstitial.load');");

                }

                @Override
                public void onInterstitialFailedToLoad() {
                    mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.interstitial.load.failed');");
                }

                @Override
                public void onInterstitialShown() {
                    mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.interstitial.show');");
                }

                @Override
                public void onInterstitialShowFailed() {
                    mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.interstitial.show.failed');");
                }

                @Override
                public void onInterstitialClicked() {
                    mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.interstitial.click');");
                }

                @Override
                public void onInterstitialClosed() {
                    mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.interstitial.close');");
                }

                @Override
                public void onInterstitialExpired() {
                    mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.interstitial.expired');");
                }
            });
        }

    }




    private void setRewardedCallbacks() {

        if (mActivity != null) {

            Appodeal.setRewardedVideoCallbacks(new RewardedVideoCallbacks() {
                @Override
                public void onRewardedVideoLoaded(boolean isPrecache) {
                    // Called when rewarded video is loaded
                }
                @Override
                public void onRewardedVideoFailedToLoad() {
                    mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.rewarded.load.failed');");
                }
                @Override
                public void onRewardedVideoShown() {
                    mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.rewarded.show');");
                }
                @Override
                public void onRewardedVideoShowFailed() {
                    mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.rewarded.show.failed');");
                }
                @Override
                public void onRewardedVideoClicked() {
                    mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.rewarded.click');");
                }
                @Override
                public void onRewardedVideoFinished(double amount, String name) {
                    try {
                        JSONObject jsonData = new JSONObject();
                        jsonData.put("amount", amount);
                        jsonData.put("name", name);

                        mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.rewarded.finished', " + jsonData.toString() + ");");
                    } catch (JSONException e) {
                        PUBLIC_CALLBACKS.error("Error creating JSON object" + e.getMessage());
                    }
                }
                @Override
                public void onRewardedVideoClosed(boolean finished) {
                    try {

                        JSONObject jsonData = new JSONObject();
                        jsonData.put("finished", finished);
                        mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.rewarded.close', " + jsonData.toString() + ");");
                    } catch (JSONException e) {
                        PUBLIC_CALLBACKS.error("Error creating JSON object" + e.getMessage());
                    }
                }
                @Override
                public void onRewardedVideoExpired() {
                    mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.rewarded.expired');");
                }
            });


        }

    }



    private void setAdvancedConsent(String appKey, boolean tagForUnderAgeOfConsent) {
        // https://docs.appodeal.com/android/data-protection/gdpr-and-ccpa
        ConsentManager.requestConsentInfoUpdate(
                new ConsentUpdateRequestParameters(
                        mActivity,
                        appKey,
                        tagForUnderAgeOfConsent,
                        "Appodeal",
                        Appodeal.getVersion()
                ),
                new ConsentInfoUpdateCallback() {
                    @Override
                    public void onUpdated() {
                        ConsentStatus status = ConsentManager.getStatus();
                        switch (status) {
                            case Unknown:
                                // No special action if the status is Unknown
                                break;
                            case Required:
                                ConsentManager.loadAndShowConsentFormIfRequired(
                                        mActivity,
                                        new OnConsentFormDismissedListener() {
                                            @Override
                                            public void onConsentFormDismissed(ConsentManagerError error) {
                                                try {
                                                    JSONObject jsonData = new JSONObject();
                                                    jsonData.put("message", error.getMessage());
                                                    mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.consent.error', " + jsonData.toString() + ");");
                                                } catch (JSONException e) {
                                                    PUBLIC_CALLBACKS.error("Error creating JSON object" + e.getMessage());
                                                }
                                            }
                                        }
                                );
                                break;
                            case NotRequired:
                                // No special action if the status is NotRequired
                                break;
                            case Obtained:
                                // No special action if the status is Obtained
                                break;
                        }
                        try {
                            JSONObject jsonData = new JSONObject();
                            jsonData.put("status", status.toString());
                            mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.consent.status', " + jsonData.toString() + ");");
                        } catch (JSONException e) {
                            PUBLIC_CALLBACKS.error("Error creating JSON object" + e.getMessage());
                        }
                    }

                    @Override
                    public void onFailed(@NonNull ConsentManagerError error) {
                        try {
                            JSONObject jsonData = new JSONObject();
                            jsonData.put("message", error.getMessage());
                            mCordovaWebView.loadUrl("javascript:cordova.fireDocumentEvent('on.consent.failed', " + jsonData.toString() + ");");
                        } catch (JSONException e) {
                            PUBLIC_CALLBACKS.error("Error creating JSON object" + e.getMessage());
                        }
                    }
                }
        );
    }




    private void revokeUserConsent(CallbackContext callbackContext) {

        if (mActivity != null) {
            try {
                ConsentManager.revoke(mActivity);
            } catch (Exception e) {
                callbackContext.error("Failed to revoke user consent, " + e.getMessage());
            }
        }
    }



    @Override
    public void onDestroy() {

        if (mActivity != null) {
            if (rootLayout != null) {
                ViewGroup parent = (ViewGroup) rootLayout.getParent();
                if (parent != null) {
                    parent.removeView(rootLayout);
                }
                rootLayout = null;
            }
            if (Appodeal.isLoaded(Appodeal.BANNER)) {
                Appodeal.destroy(Appodeal.BANNER);
            }
            bannerView = null;
        }
        super.onDestroy();
    }



    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        if (mActivity != null) {
            if (isBannerLoaded && Appodeal.isLoaded(Appodeal.BANNER)) {
                Appodeal.show(mActivity, Appodeal.BANNER_VIEW);
            }
        }
    }



}
