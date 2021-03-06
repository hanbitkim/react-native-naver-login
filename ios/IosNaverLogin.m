
#import "IosNaverLogin.h"

#import <React/RCTLog.h>
    #import <React/RCTConvert.h>

    #import "NaverThirdPartyConstantsForApp.h"
#import "NaverThirdPartyLoginConnection.h"
#import "NLoginThirdPartyOAuth20InAppBrowserViewController.h"

    @interface IosNaverLogin() {
    NaverThirdPartyLoginConnection *naverConn;
    RCTResponseSenderBlock naverTokenSend;
}
    @end

    @implementation IosNaverLogin

    -(void)oauth20Connection:(NaverThirdPartyLoginConnection *)oauthConnection didFailWithError:(NSError *)error {
    RCTLogInfo(@"oauth20Connection error = %@", [NSString stringWithFormat:@"%@", error]);
if (naverTokenSend != nil) {
    naverTokenSend(@[error, [NSNull null]]);
naverTokenSend = nil;
}
}

-(void)oauth20ConnectionDidFinishRequestACTokenWithAuthCode {
    RCTLogInfo(@"oauth20ConnectionDidFinishRequestACTokenWithAuthCode");
NSString *token = [naverConn accessToken];
if (naverTokenSend != nil) {
    naverTokenSend(@[[NSNull null], token]);
naverTokenSend = nil;
}
}
-(void)oauth20ConnectionDidFinishRequestACTokenWithRefreshToken {
    RCTLogInfo(@"oauth20ConnectionDidFinishRequestACTokenWithRefreshToken");
NSString *token = [naverConn accessToken];
if (naverTokenSend != nil) {
    naverTokenSend(@[[NSNull null], token]);
naverTokenSend = nil;
}
}

-(void)oauth20ConnectionDidOpenInAppBrowserForOAuth:(NSURLRequest *)request {
    RCTLogInfo(@"oauth20ConnectionDidOpenInAppBrowserForOAuth");
// 웹뷰 띄우기. RN에서는 ViewController 에서 띄우는 것이 아니므로 앱의 뷰를 가져와서 띄운다.
dispatch_async(dispatch_get_main_queue(), ^{
    NLoginThirdPartyOAuth20InAppBrowserViewController *inappAuthBrowser =
[[NLoginThirdPartyOAuth20InAppBrowserViewController alloc] initWithRequest:request];
UIViewController *topController = [UIApplication sharedApplication].keyWindow.rootViewController;
while (topController.presentedViewController) {
    topController = topController.presentedViewController;
}

[topController presentViewController:inappAuthBrowser animated:YES completion:nil];
});
}

-(void)oauth20ConnectionDidFinishDeleteToken {
    RCTLogInfo(@"oauth20ConnectionDidFinishDeleteToken");
}

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(login:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"login");
naverTokenSend = callback;

naverConn = [NaverThirdPartyLoginConnection getSharedInstance];
naverConn.delegate = self;

dispatch_async(dispatch_get_main_queue(), ^{
[naverConn requestThirdPartyLogin];
});
}

RCT_EXPORT_METHOD(logout) {
    RCTLogInfo(@"logout");
[naverConn resetToken];
naverTokenSend = nil;
}

RCT_EXPORT_METHOD(getToken:(RCTResponseSenderBlock)callback) {
    naverConn = [NaverThirdPartyLoginConnection getSharedInstance];
if ([naverConn accessToken] != nil) {
    callback(@[[NSNull null], [naverConn accessToken]]);
}
else {
    callback(@[[NSNull null]]);
}
}

RCT_EXPORT_METHOD(loginSilently:(RCTResponseSenderBlock)callback) {
    naverTokenSend = callback;

    naverConn = [NaverThirdPartyLoginConnection getSharedInstance];
    naverConn.delegate = self;

    dispatch_async(dispatch_get_main_queue(), ^{
[naverConn requestAccessTokenWithRefreshToken];
});
}

//RCT_EXPORT_METHOD(getProfile:(NSString *)token resp(RCTResponseSenderBlock)response) {
//  if (NO == [naverConn isValidAccessTokenExpireTimeNow]) {
//    return;
    //  }
//
//  NSString *urlString = @"https://openapi.naver.com/v1/nid/getUserProfile.xml";  // 사용자 프로필 호출
//  NSMutableURLRequest *urlRequest = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:urlString]];
//  NSString *authValue = [NSString stringWithFormat:@"Bearer %@", token];
//
//  [urlRequest setValue:authValue forHTTPHeaderField:@"Authorization"];
//
//  NSError *error = nil;
    //  NSHTTPURLResponse *urlResponse = nil;
    //  NSData *receivedData = [NSURLConnection sendSynchronousRequest:urlRequest returningResponse:&urlResponse error:&error];
//  NSString *decodingString = [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding];
//
//  if (error) {
//    NSLog(@"Error happened - %@", [error description]);
//
//  } else {
//    NSLog(@"recevied data - %@", decodingString);
//    response(@[[NSNull null], decodingString]);
//  }
//}


@end
