
# React Native ArcGIS MapView
A basic port of ArcGIS for React Native. Handles basemap URLs, map recentering, Callout views (iOS only for now), drawing/moving/updating graphics onto the map, and single tap listeners.

# Installation Instructions
### Install the package and link it
`yarn install react-native-arcgis-mapview` or `npm install react-native-arcgis-mapview`

Then run `react-native link react-native-arcgis-mapview`

### Modify your Android native project
First off, make sure your minSdk is 19 and your targetSdk is at least 28. ArcGIS requires a minimum SDK level of 19.

Inside your Project Gradle file, inside of allProjects, add the following:
```
maven {
  url 'https://esri.bintray.com/arcgis'
}
```
Then, inside your App Gradle file, if your app does not already have Java 8 Support (ArcGIS Requires this from 100.4 onwards), add the following line inside the android bracket:
```
    compileOptions {
        sourceCompatibility 1.8
        targetCompatibility 1.8
    }
```
That's it. Your project should build.

### Modify your iOS native project
iOS is a bit trickier. Create a podfile in your iOS directory with the following contents:
```ruby
platform :ios, '11.0'

target 'Example' do
  # Uncomment the next line if you're using Swift or would like to use dynamic frameworks
  # use_frameworks!

    rn_path = '../node_modules/react-native'

  # See http://facebook.github.io/react-native/docs/integration-with-existing-apps.html#configuring-cocoapods-dependencies
  pod 'yoga', path: "#{rn_path}/ReactCommon/yoga/yoga.podspec"
  pod 'React', path: rn_path, subspecs: [
    'Core',
    'CxxBridge',
    'DevSupport',
    'RCTActionSheet',
    'RCTAnimation',
    'RCTGeolocation',
    'RCTImage',
    'RCTLinkingIOS',
    'RCTNetwork',
    'RCTSettings',
    'RCTText',
    'RCTVibration',
    'RCTWebSocket',
  ], :modular_headers => true

  # React Native third party dependencies podspecs
  pod 'DoubleConversion', :podspec => "#{rn_path}/third-party-podspecs/DoubleConversion.podspec"
  pod 'glog', :podspec => "#{rn_path}/third-party-podspecs/glog.podspec"

  # If you are using React Native <0.54, you will get the following error:
  # "The name of the given podspec `GLog` doesn't match the expected one `glog`"
  # Use the following line instead:
  #pod 'GLog', :podspec => "#{rn_path}/third-party-podspecs/GLog.podspec"
  pod 'Folly', :podspec => "#{rn_path}/third-party-podspecs/Folly.podspec"
  pod 'ArcGIS-Runtime-SDK-iOS', '100.4'
  pod 'RNArcGISMapView', :path => "../node_modules/react-native-arcgis-mapview/"

end

# The following is needed to ensure the "archive" step works in XCode.
# It removes React from the Pods project, as it is already included in the main project.
post_install do |installer|
  installer.pods_project.targets.each do |target|
    if target.name == "React"
      target.remove_from_project
    end
  end
end
```
If you already have a podfile, add the postinstall lines and the declaraction for the ArcGIS-Runtime-SDK-iOS Pod and the RNArcGISMapView Pod.

If you have Swift code within your project, that should be it. 

If not, open your project directory and make a new Swift file. Name it whatever you want, it doesn't matter. Upon making this file, XCode should ask if you want to create a briding header. **CHOOSE CREATE!!** Then clean, rebuild, and you should be good to go.

### License your map
A license is not required to develop and test. However, to release your app, you must provide a license. See [here for iOS](https://developers.arcgis.com/ios/latest/swift/guide/license-your-app.htm) or [here for Android](https://developers.arcgis.com/android/latest/guide/license-your-app.htm) for more information on how to get a license.

Once you have one, follow these steps:
##### Android
Add the following to your MainApplication.java:
```java
import com.davidgalindo.rnarcgismapview.RNArcGISMapViewPackage;
  ...
  public void onCreate(){
    ...
    RNArcGISMapViewPackage.setLicenseKey("Your License Key");
  }
```
##### iOS
Add the following to your main.m:
```objc
import <ArcGIS/ArcGIS.h>
...
int main() {
  ...
    NSError *error;
  AGSLicenseResult *licenseResult = [AGSArcGISRuntimeEnvironment setLicenseKey:@"Your License Key" error:&error];
  if (error) {
    .. Print the error      
  }
  else {
   NSLog(@"license status = %li", licenseResult.licenseStatus);
  }
  ...
}
```
# Usage
TODO: Do this
