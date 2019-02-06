
# react-native-arc-gis-map-view

## Getting started

`$ npm install react-native-arc-gis-map-view --save`

### Mostly automatic installation

`$ react-native link react-native-arc-gis-map-view`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-arc-gis-map-view` and add `RNArcGisMapView.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNArcGisMapView.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.davidgalindo.rnarcgismapview.RNArcGisMapViewPackage;` to the imports at the top of the file
  - Add `new RNArcGisMapViewPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-arc-gis-map-view'
  	project(':react-native-arc-gis-map-view').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-arc-gis-map-view/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-arc-gis-map-view')
  	```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNArcGisMapView.sln` in `node_modules/react-native-arc-gis-map-view/windows/RNArcGisMapView.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Arc.Gis.Map.View.RNArcGisMapView;` to the usings at the top of the file
  - Add `new RNArcGisMapViewPackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import RNArcGisMapView from 'react-native-arc-gis-map-view';

// TODO: What to do with the module?
RNArcGisMapView;
```
  