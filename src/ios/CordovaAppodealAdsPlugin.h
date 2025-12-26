
#import <Cordova/CDV.h>
#import <UIKit/UIKit.h>
#import <Cordova/CDVPlugin.h>
#import <Appodeal/Appodeal.h>
/**
 * Created by EMI INDO So on Fri 21, 2025
 */
@interface CordovaAppodealAdsPlugin : CDVPlugin<AppodealBannerDelegate, AppodealInterstitialDelegate, AppodealRewardedVideoDelegate>
@property(nonatomic, strong) CDVInvokedUrlCommand *command;
@property (strong, nonatomic) IBOutlet APDBannerView *bannerView;
@property (nonatomic, strong) NSLayoutConstraint *height;
@property (nonatomic, copy)   NSString *bannerPosition;

- (void)requestTrackingAuthorization:(CDVInvokedUrlCommand*)command;
- (void)revokeUserConsent:(CDVInvokedUrlCommand*)command;

- (void)loadInterstitial:(CDVInvokedUrlCommand*)command;
- (void)showInterstitial:(CDVInvokedUrlCommand*)command;

- (void)loadRewarded:(CDVInvokedUrlCommand*)command;
- (void)showRewarded:(CDVInvokedUrlCommand*)command;

- (void)loadBanner:(CDVInvokedUrlCommand*)command;
- (void)showBanner:(CDVInvokedUrlCommand*)command;
- (void)hideBanner:(CDVInvokedUrlCommand*)command;
- (void)destroyBanner:(CDVInvokedUrlCommand*)command;

- (void) fireEvent:(NSString *)obj event:(NSString *)eventName withData:(NSString *)jsonStr;

@end

