#
#  Be sure to run `pod spec lint react-native-arcgis-mapview.podspec' to ensure this is a
#  valid spec and to remove all comments including this before submitting the spec.
#
#  To learn more about Podspec attributes see http://docs.cocoapods.org/specification.html
#  To see working Podspecs in the CocoaPods repo see https://github.com/CocoaPods/Specs/
#

Pod::Spec.new do |s|
  umbrella_header = "Project-Umbrella-Header.h"
  s.name         = "RNArcGISMapView"
  s.version      = "1.0.0"
  s.summary      = "A basic port of ArcGIS for React Native."
  s.homepage     = "https://davidagalindo.com"
  s.description = "Created to utilize Esri's ArcGIS Mobile frameworks for basic tasks such as recentering and drawing graphics."
  s.license      = "MIT"
  s.author             = "David Galindo"
  s.platform     = :ios, "11.0"
  s.source       = { :git => "https://github.com/davidgalindo/react-native-arcgis-mapview.git", :tag => "#{s.version}" }
  s.source_files  = "src/*.{h,m,swift}", "src/RNArcGISMapViewManager/*.{h,m,swift}"
  s.swift_version = '4.2'

  s.dependency 'React'
  s.dependency 'ArcGIS-Runtime-SDK-iOS', '100.4'
end
