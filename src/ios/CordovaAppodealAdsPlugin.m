
#import <Cordova/CDV.h>
#import <UIKit/UIKit.h>
#import <Cordova/CDVPlugin.h>
#import "CordovaAppodealAdsPlugin.h"

#import <AdSupport/AdSupport.h>
#import <AppTrackingTransparency/AppTrackingTransparency.h>
#import <Foundation/Foundation.h>
#import <StackConsentManager/StackConsentManager-Swift.h>

#import <Appodeal/Appodeal.h>
/**
 * Created by EMI INDO So on Fri 21, 2025
 */
@implementation CordovaAppodealAdsPlugin

NSString *bannerPosition = @"bottom";
BOOL setOverlapping = NO;
BOOL isBannerLoad = NO;

// - (void)pluginInitialize {}

- (void)fireEvent:(NSString *)obj
            event:(NSString *)eventName
         withData:(NSString *)jsonStr {
    NSString *js;
    
    if (obj && [obj isEqualToString:@"window"]) {
        js = [NSString stringWithFormat:
              @"var evt = document.createEvent('UIEvents');"
              @"evt.initUIEvent('%@', true, false, window, 0);"
              @"window.dispatchEvent(evt);",
              eventName];
    } else if (jsonStr && [jsonStr length] > 0) {
        js = [NSString stringWithFormat:
              @"javascript:cordova.fireDocumentEvent('%@', %@);",
              eventName, jsonStr];
    } else {
        js = [NSString stringWithFormat:
              @"javascript:cordova.fireDocumentEvent('%@');",
              eventName];
    }
    
    [self.commandDelegate evalJs:js];
}




- (void)initialize:(CDVInvokedUrlCommand *)command {
    if (command.arguments.count == 0) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Missing options"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    
    NSDictionary *options = [command.arguments objectAtIndex:0];
    
    BOOL setTesting = [[options valueForKey:@"setTesting"] boolValue];
    NSString *appKey = [options valueForKey:@"appKey"];
    BOOL setAutoCache = [[options valueForKey:@"setAutoCache"] boolValue]; // Should be called before the SDK initialization.
    BOOL isAdvancedConsent = [[options valueForKey:@"isAdvancedConsent"] boolValue];
    BOOL tagForUnderAgeOfConsent = [[options valueForKey:@"tagForUnderAgeOfConsent"] boolValue];
    BOOL setChildDirectedTreatment = [[options valueForKey:@"setChildDirectedTreatment"] boolValue];
    
    if (appKey == nil || [appKey length] == 0) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Invalid Appodeal appKey"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    
    if (isAdvancedConsent) {
        [self setAdvancedConsentWithAppKey:appKey tagForUnderAgeOfConsent:tagForUnderAgeOfConsent];
    }
    
    // COPPA Should be called before the SDK initialization.
    [Appodeal setChildDirectedTreatment: setChildDirectedTreatment];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [Appodeal setTestingEnabled:setTesting];
        // [Appodeal setLogLevel:APDLogLevelDebug];
        [Appodeal setAutocache:setAutoCache types:(AppodealAdTypeInterstitial | AppodealAdTypeRewardedVideo | AppodealAdTypeBanner)];
        
        AppodealAdType adTypes = AppodealAdTypeInterstitial | AppodealAdTypeRewardedVideo | AppodealAdTypeBanner;
        [Appodeal initializeWithApiKey:appKey types:adTypes];
        
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"on.sdk.initialization"];
        
        
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    });
}




- (void)loadInterstitial:(CDVInvokedUrlCommand *)command {
    
    NSDictionary *options = [command.arguments objectAtIndex:0];
    BOOL autoShow = [[options valueForKey:@"autoShow"] boolValue];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [Appodeal setInterstitialDelegate:self];
        if ([Appodeal isReadyForShowWithStyle:AppodealShowStyleInterstitial]) {
            
            [self fireEvent:@"" event:@"on.interstitial.load" withData:nil];
           
            if (autoShow){
                [Appodeal showAd:AppodealShowStyleInterstitial rootViewController:self.viewController];
                [self fireEvent:@"" event:@"on.interstitial.show" withData:nil];
            }

        } else {
            [self fireEvent:@"" event:@"on.interstitial.load.failed" withData:nil];
        }
    });
}


- (void)showInterstitial:(CDVInvokedUrlCommand *)command {
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([Appodeal isReadyForShowWithStyle:AppodealShowStyleInterstitial]) {
            
            [Appodeal showAd:AppodealShowStyleInterstitial rootViewController:self.viewController];
            [self fireEvent:@"" event:@"on.interstitial.show" withData:nil];
            
        } else {
            [self fireEvent:@"" event:@"on.interstitial.load.failed" withData:nil];
        }
    });
}



- (void)loadRewarded:(CDVInvokedUrlCommand *)command {
    
    NSDictionary *options = [command.arguments objectAtIndex:0];
    BOOL autoShow = [[options valueForKey:@"autoShow"] boolValue];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [Appodeal setRewardedVideoDelegate:self];
        if ([Appodeal isReadyForShowWithStyle: AppodealShowStyleRewardedVideo]) {
            
            [self fireEvent:@"" event:@"on.rewarded.load" withData:nil];
            if (autoShow){
                [Appodeal showAd:AppodealShowStyleRewardedVideo rootViewController:self.viewController];
                [self fireEvent:@"" event:@"on.rewarded.show" withData:nil];
            }
        } else {
            
            [self fireEvent:@"" event:@"on.rewarded.load.failed" withData:nil];
        }
    });
}






- (void)showRewarded:(CDVInvokedUrlCommand *)command {
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([Appodeal isReadyForShowWithStyle: AppodealShowStyleRewardedVideo]) {
            [Appodeal showAd:AppodealShowStyleRewardedVideo rootViewController:self.viewController];
            [self fireEvent:@"" event:@"on.rewarded.show" withData:nil];
        } else {
            
            [self fireEvent:@"" event:@"on.rewarded.load.failed" withData:nil];
        }
    });
}



- (void)loadBanner:(CDVInvokedUrlCommand *)command {
    
    NSDictionary *options = [command.arguments objectAtIndex:0];
    NSString *position = [options valueForKey:@"position"];
    BOOL isOverlapping = [[options valueForKey:@"isOverlapping"] boolValue];
    BOOL autoShow = [[options valueForKey:@"autoShow"] boolValue];
    
    self.bannerPosition = position;
    setOverlapping = isOverlapping;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        
        CGSize bannerSize = kAPDAdSize320x50;
        [self.bannerView setAdSize:bannerSize];
        self.height.constant = bannerSize.height;
        [Appodeal setBannerDelegate:self];
        [self.bannerView loadAd];
        
        AppodealShowStyle style = [self getBannerStyle];
        
        if ([Appodeal isReadyForShowWithStyle:style]) {
            isBannerLoad = YES;
            [self fireEvent:@"" event:@"on.banner.load" withData:nil];
        } else {
            isBannerLoad = NO;
            [self fireEvent:@"" event:@"on.banner.not.ready" withData:nil];
        }
        
        if (autoShow) {
            
            if (isOverlapping && isBannerLoad) {
                
                [Appodeal showAd:style rootViewController:self.viewController];
                
                [self fireEvent:@"" event:@"on.banner.show" withData:nil];
                
            } else if (isBannerLoad) {
                
                [self loadCustomWebView];
                
            } else {
                
                CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                messageAsString:@"Banner ad is not ready"];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                
            }
            
        }
           

    });
}



- (void)showBanner:(CDVInvokedUrlCommand *)command {
    dispatch_async(dispatch_get_main_queue(), ^{
        NSString *callbackId = command.callbackId;
        CDVPluginResult *pluginResult = nil;
        
        AppodealShowStyle style = [self getBannerStyle];
        
        if (self.bannerView) {
            self.bannerView.hidden = NO;
  
            if (setOverlapping && isBannerLoad) {
                
                [Appodeal showAd:style rootViewController:self.viewController];
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                
              } else if (isBannerLoad) {

               [self loadCustomWebView];
               
              } else {
                  
                  pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                                         messageAsString:@"Banner Overlapping ad is not ready"];
                }
            
             } else if (self.bannerView == nil && isBannerLoad) {
                 
               [Appodeal showAd:style rootViewController:self.viewController];
               pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];

            } else {
                
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                                       messageAsString:@"Banner Overlapping ad is not ready"];
            
            }
        
        if (self.bannerView != nil || isBannerLoad){
            [self fireEvent:@"" event:@"on.banner.show" withData:nil];
        }
        
        
        [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
    
    });
    
}




- (void)hideBanner:(CDVInvokedUrlCommand *)command {
    dispatch_async(dispatch_get_main_queue(), ^{
        NSString *callbackId = command.callbackId;
        CDVPluginResult *pluginResult = nil;
       
        if (self.bannerView) {
            self.bannerView.hidden = YES;
            
            if (setOverlapping) {
                
                [Appodeal hideBanner];
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Banner overlapping hidden"];
                
            } else if (isBannerLoad) {
                
                [Appodeal hideBanner];
                [self resetWebViewHeight];
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Banner custom webview hidden"];
                
            } else {
                
                if (isBannerLoad) {
                    [Appodeal hideBanner];
                    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Banner overlapping hidden"];
                    
                } else {
                    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"No banner exists"];
                }
                
            }
            
        } else if (self.bannerView == nil && isBannerLoad) {
            
            if (isBannerLoad) {
                [Appodeal hideBanner];
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Banner overlapping hidden"];
                
            } else {
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"No banner exists"];
            }
            
        } else {

            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"No banner exists"];
        }
        
        [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
    });
}



- (void)destroyBanner:(CDVInvokedUrlCommand *)command {
    dispatch_async(dispatch_get_main_queue(), ^{
        NSString *callbackId = command.callbackId;
        CDVPluginResult *pluginResult = nil;
        
        if (self.bannerView) {
            
            [self.bannerView removeFromSuperview];
            self.bannerView.hidden = YES;
            self.bannerView = nil;
            
            if (setOverlapping) {
                [Appodeal hideBanner];
                isBannerLoad = NO;
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Banner overlapping destroy"];
            } else if (isBannerLoad) {
                [Appodeal hideBanner];
                [self resetWebViewHeight];
                isBannerLoad = NO;
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Banner custom destroy"];
            } else {
                [self resetWebViewHeight];
                isBannerLoad = NO;
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"No banner exists"];
            }

        } else if (self.bannerView == nil && isBannerLoad) {

            [Appodeal hideBanner];
            isBannerLoad = NO;
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
           
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                              messageAsString:@"Banner ad is not ready"];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
        }
    });
}



- (AppodealShowStyle)getBannerStyle {
    if ([self.bannerPosition isEqualToString:@"top"]) {
        return AppodealShowStyleBannerTop;
    } else if ([self.bannerPosition isEqualToString:@"bottom"]) {
        return AppodealShowStyleBannerBottom;
    } else {
        return AppodealShowStyleBannerBottom;
    }
}




- (void)resetWebViewHeight {
    dispatch_async(dispatch_get_main_queue(), ^{
        UIWindow *keyWindow = [UIApplication sharedApplication].delegate.window;
        UIViewController *rootViewController = keyWindow.rootViewController;

        if (!rootViewController) {
            NSLog(@"[CordovaBodyHeight] Root ViewController not found on reset");
            return;
        }
        
        UIEdgeInsets safeAreaInsets = rootViewController.view.safeAreaInsets;

        if (safeAreaInsets.bottom == 0) {
            safeAreaInsets = keyWindow.safeAreaInsets;
        }

        CGFloat screenHeight = UIScreen.mainScreen.bounds.size.height;

        CGRect contentFrame = rootViewController.view.frame;
        contentFrame.origin.y = 0;
        contentFrame.size.height = screenHeight - safeAreaInsets.bottom;
        rootViewController.view.frame = contentFrame;

        UIView *webView = [self findWebViewInView:rootViewController.view];
        if (webView) {
            CGRect webViewFrame = webView.frame;
            webViewFrame.origin.y = 0;
            webViewFrame.size.height = screenHeight - safeAreaInsets.bottom;
            webView.frame = webViewFrame;

        } else {
            NSLog(@"[CordovaBodyHeight] WebView not found on reset");
        }

        [rootViewController.view setNeedsLayout];
        [rootViewController.view layoutIfNeeded];
    });
}
                   
                   
                   
                   
                   
 - (UIView *)findWebViewInView:(UIView *)view {
                       if ([view isKindOfClass:NSClassFromString(@"WKWebView")] ||
                           [view isKindOfClass:NSClassFromString(@"UIWebView")]) {
                           return view;
                       }
                       
                       for (UIView *subview in view.subviews) {
                           UIView *found = [self findWebViewInView:subview];
                           if (found) {
                               return found;
                           }
                       }
                       return nil;
                   }



- (void)loadCustomWebView {
             dispatch_async(dispatch_get_main_queue(), ^{
                           UIWindow *keyWindow = [UIApplication sharedApplication].delegate.window;
                           UIViewController *rootVC = keyWindow.rootViewController;
                           if (!rootVC) {
                               
                               return;
                           }
                           
                           UIEdgeInsets safeAreaInsets = rootVC.view.safeAreaInsets;
                           if (safeAreaInsets.bottom == 0) {
                               safeAreaInsets = keyWindow.safeAreaInsets;
                           }
                           
                           CGFloat screenWidth  = UIScreen.mainScreen.bounds.size.width;
                           CGFloat screenHeight = UIScreen.mainScreen.bounds.size.height;
                           
                           CGSize bannerSize = kAppodealUnitSize_320x50;
                           CGFloat bannerHeight = bannerSize.height;
                           CGFloat bannerX = 0;
                           CGFloat bannerY = 0;
                           
                           if (isBannerLoad) {
                               
                               if ([self.bannerPosition isEqualToString:@"top"]) {
                                   bannerY = safeAreaInsets.top;
                               } else if ([self.bannerPosition isEqualToString:@"bottom"]) {
                                   bannerY = screenHeight - safeAreaInsets.bottom - bannerHeight;
                               } else {
                                   bannerY = screenHeight - safeAreaInsets.bottom - bannerHeight;
                               }
                               
                               self.bannerView = [[AppodealBannerView alloc] initWithSize:kAppodealUnitSize_320x50
                                                                       rootViewController:rootVC];
                               
                               self.bannerView.frame = CGRectMake(bannerX, bannerY, screenWidth, bannerHeight);
                               [rootVC.view addSubview:self.bannerView];
                               [self.bannerView loadAd];
                               
                               UIView *webView = [self findWebViewInView:rootVC.view];
                               if (webView) {
                                   if ([self.bannerPosition isEqualToString:@"top"]) {
                                       webView.frame = CGRectMake(webView.frame.origin.x,
                                                                  safeAreaInsets.top + bannerHeight,
                                                                  screenWidth,
                                                                  screenHeight - safeAreaInsets.top - safeAreaInsets.bottom - bannerHeight);
                                   } else if ([self.bannerPosition isEqualToString:@"bottom"]) {
                                       webView.frame = CGRectMake(webView.frame.origin.x,
                                                                  safeAreaInsets.top,
                                                                  screenWidth,
                                                                  screenHeight - safeAreaInsets.top - safeAreaInsets.bottom - bannerHeight);
                                   }
                               }
                               
                               [rootVC.view bringSubviewToFront:self.bannerView];
                               
                               if (self.bannerView != nil || isBannerLoad){
                                   [self fireEvent:@"" event:@"on.banner.show" withData:nil];
                               }
                               
                           }
                           
                       });
                  }






- (void)requestTrackingAuthorization:(CDVInvokedUrlCommand *)command {
    
    if (@available(iOS 14, *)) {
        dispatch_async(dispatch_get_main_queue(), ^{
            // Added a 1-second pause before performing a tracking authorization request
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 1 * NSEC_PER_SEC), dispatch_get_main_queue(), ^{
                
                [ATTrackingManager requestTrackingAuthorizationWithCompletionHandler:^(ATTrackingManagerAuthorizationStatus status) {
                    NSDictionary *data = @{ @"status": @(status)};
                    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:data options:0 error:nil];
                    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
                    [self fireEvent:@"" event:@"on.att.status" withData:jsonString];
                }];
            });
        });
    } else {
        [self fireEvent:@"" event:@"on.att.error" withData:@"iOS 14+ not found"];
    }
}



// Force Present Consent Dialog and Check Consent Status
// https://docs.appodeal.com/ios/data-protection/gdpr-and-ccpa

- (void)setAdvancedConsentWithAppKey:(NSString *)appKey
             tagForUnderAgeOfConsent:(BOOL)tagForUnderAgeOfConsent {
    dispatch_async(dispatch_get_main_queue(), ^{
        
        // NSString *mediationSdkName = @"Cordova";
        //  NSString *mediationSdkVersion = @"1.0.0";
        
        APDConsentUpdateRequestParameters *parameters =
        [[APDConsentUpdateRequestParameters alloc] initWithAppKey:appKey
                                                 mediationSdkName:@"default"
                                              mediationSdkVersion:@"default"
                                                            COPPA:tagForUnderAgeOfConsent];
        
        [APDConsentManager.shared requestConsentInfoUpdateWithParameters:parameters completion:^(NSError *error) {
            if (error) {
                [self fireEvent:@"" event:@"on.consent.error" withData:error.localizedDescription];
                return;
            }
            
            APDConsentStatus status = [APDConsentManager.shared status];
            
            NSDictionary *data = @{ @"status": @(status)};
            NSData *jsonData = [NSJSONSerialization dataWithJSONObject:data options:0 error:nil];
            NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
            
            [self fireEvent:@"" event:@"on.consent.status" withData:jsonString];
            
            if (status == APDConsentStatusRequired || status == APDConsentStatusUnknown) {
                [APDConsentManager.shared loadWithCompletion:^(APDConsentDialog *dialog, NSError *error) {
                    if (error) {
                        [self fireEvent:@"" event:@"on.consent.error" withData:error.localizedDescription];
                        return;
                    }
                    
                    if (dialog) {
                        [dialog presentWithRootViewController:self.viewController completion:^(NSError *error) {
                            if (error) {
                                [self fireEvent:@"" event:@"on.consent.error" withData:error.localizedDescription];
                            } else {
                                [self fireEvent:@"" event:@"on.consent.dialog.successfully" withData:nil];
                               // NSLog(@"[Appodeal] Consent dialog presented successfully.");
                            }
                        }];
                    }
                }];
            } else {
               // NSLog(@"[Appodeal] Consent status already determined: %ld", (long)status);
            }
        }];
    });
}




// https://docs.appodeal.com/ios/data-protection/gdpr-and-ccpa
- (void)revokeUserConsent:(CDVInvokedUrlCommand *)command {
    
    @try {
        
        [APDConsentManager.shared revoke];
        
    } @catch (NSException *exception) {
        
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:exception.reason];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

    }
}





#pragma mark - AppodealBannerViewDelegate
/*
- (void)bannerDidLoadAdIsPrecache:(BOOL)precache {
    NSLog(@"[AppodealBannerViewDelegate] bannerDidLoadAdIsPrecache called. Precache: %@", precache ? @"YES" : @"NO");
 
    If setAutocache is true, the event will be triggered continuously.
    [self fireEvent:@"" event:@"on.banner.load" withData:nil];
 
}

- (void)bannerDidShow{
    
    If setAutocache is true, the event will be triggered continuously.
    [self fireEvent:@"" event:@"on.banner.show" withData:nil];
    
}
 */

- (void)bannerDidFailToLoadAd:(NSError *)error {
    
    NSDictionary *errorDict = @{@"error": error.localizedDescription};
    NSError *jsonError = nil;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:errorDict options:0 error:&jsonError];
    NSString *jsonStr = (jsonData && !jsonError) ? [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding] : error.localizedDescription;
    
    [self fireEvent:@"" event:@"on.banner.failed.load" withData:jsonStr];
}

- (void)bannerDidClick{
    [self fireEvent:@"" event:@"on.banner.click" withData:nil];
}

- (void)bannerDidExpired{
    [self fireEvent:@"" event:@"on.banner.expired" withData:nil];
}




#pragma mark - AppodealInterstitialDelegate

// - (void)interstitialDidLoadAdIsPrecache:(BOOL)precache {[self fireEvent:@"" event:@"" withData:nil];}
- (void)interstitialDidFailToLoadAd {[self fireEvent:@"" event:@"on.interstitial.load.failed2" withData:nil];}
- (void)interstitialDidFailToPresent {[self fireEvent:@"" event:@"on.interstitial.show.failed" withData:nil];}
- (void)interstitialWillPresent {[self fireEvent:@"" event:@"on.interstitial.show2" withData:nil];}
- (void)interstitialDidDismiss {[self fireEvent:@"" event:@"on.interstitial.close" withData:nil];}
- (void)interstitialDidClick {[self fireEvent:@"" event:@"on.interstitial.click" withData:nil];}




#pragma mark - AppodealRewardedVideoDelegate
/*
- (void)rewardedVideoDidLoadAdIsPrecache:(BOOL)precache {
    NSLog(@"[rewardedVideo] did load (precache: %@)", precache ? @"YES" : @"NO");
}

- (void)rewardedVideoDidFailToLoadAd {}
*/
 

- (void)rewardedVideoDidPresent {[self fireEvent:@"" event:@"on.rewarded.show2" withData:nil];}

- (void)rewardedVideoDidFailToPresentWithError:(NSError *)error {
    NSDictionary *errorDict = @{@"error": error.localizedDescription};
    NSError *jsonError = nil;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:errorDict options:0 error:&jsonError];
    NSString *jsonStr = (jsonData && !jsonError) ? [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding] : error.localizedDescription;
    
    [self fireEvent:@"" event:@"on.rewarded.load.failed2" withData:jsonStr];
}

- (void)rewardedVideoWillDismissAndWasFullyWatched:(BOOL)wasFullyWatched {
    NSDictionary *data = @{@"wasFullyWatched": @(wasFullyWatched)};
    NSError *jsonError = nil;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:data options:0 error:&jsonError];
    NSString *jsonStr = (jsonData && !jsonError) ? [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding] : @"{}";
    
    [self fireEvent:@"" event:@"on.rewarded.close" withData:jsonStr];
}

- (void)rewardedVideoDidFinish:(float)rewardAmount name:(NSString *)rewardName {
    
    NSDictionary *data = @{@"rewardAmount": @(rewardAmount),
                           @"rewardName": rewardName ? rewardName : @""};
    
    NSError *jsonError = nil;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:data options:0 error:&jsonError];
    NSString *jsonStr = (jsonData && !jsonError) ? [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding] : @"{}";
    
    [self fireEvent:@"" event:@"on.rewarded.finished" withData:jsonStr];
}




@end
