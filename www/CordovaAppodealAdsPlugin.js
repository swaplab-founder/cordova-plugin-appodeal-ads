var exec = require('cordova/exec');

exports.initialize = function(options, success, error) {
    exec(success, error, "CordovaAppodealAdsPlugin", "initialize", [options]);
};

exports.loadInterstitial = function(options, success, error) {
    exec(success, error, "CordovaAppodealAdsPlugin", "loadInterstitial", [options]);
};

exports.showInterstitial = function(success, error) {
    exec(success, error, "CordovaAppodealAdsPlugin", "showInterstitial", []);
};

exports.loadRewarded = function(options, success, error) {
    exec(success, error, "CordovaAppodealAdsPlugin", "loadRewarded", [options]);
};

exports.showRewarded = function(success, error) {
    exec(success, error, "CordovaAppodealAdsPlugin", "showRewarded", []);
};

exports.loadBanner = function(options, success, error) {
    exec(success, error, "CordovaAppodealAdsPlugin", "loadBanner", [options]);
};

exports.showBanner = function(success, error) {
    exec(success, error, "CordovaAppodealAdsPlugin", "showBanner", []);
};

exports.destroyBanner = function(success, error) {
    exec(success, error, "CordovaAppodealAdsPlugin", "destroyBanner", []);
};

exports.hideBanner = function(success, error) {
    exec(success, error, "CordovaAppodealAdsPlugin", "hideBanner", []);
};

exports.requestTrackingAuthorization = function(success, error) {
    exec(success, error, "CordovaAppodealAdsPlugin", "requestTrackingAuthorization", []);
};

exports.revokeUserConsent = function(success, error) {
    exec(success, error, "CordovaAppodealAdsPlugin", "revokeUserConsent", []);
};



