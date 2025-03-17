#
#  Be sure to run `pod spec lint RNArcGISMapView.podspec' to ensure this is a
#  valid spec and to remove all comments including this before submitting the spec.
#
#  To learn more about Podspec attributes see http://docs.cocoapods.org/specification.html
#  To see working Podspecs in the CocoaPods repo see https://github.com/CocoaPods/Specs/
#
require 'json'

package = JSON.parse(File.read(File.join(__dir__, '../package.json')))

Pod::Spec.new do |s|
  umbrella_header = "Project-Umbrella-Header.h"
  s.name         = "RNArcGISMapView"
  s.version      = package['version']
  s.summary      = package['description']
  s.homepage     = package['homepage']
  s.description = "Created to utilize Esri's ArcGIS Mobile frameworks for basic tasks such as recentering and drawing graphics."
  s.license      = "MIT"
  s.author       = package['author']
  s.platform     = :ios, "13.0"
  s.source       = { :git => "https://github.com/davidgalindo/react-native-arcgis-mapview.git", :tag => "#{s.version}" }
  s.source_files  = "src/*.{h,m,swift}", "src/RNArcGISMapViewManager/*.{h,m,swift}"
  s.swift_version = '4.2'

  s.dependency 'React'
  s.dependency 'ArcGIS-Runtime-SDK-iOS', '100.11.2'
end
