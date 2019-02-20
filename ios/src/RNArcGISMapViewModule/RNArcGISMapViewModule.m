//
//  RNArcGISMapViewModule.m
//  RNArcGISMapView
//
//  Created by David Galindo on 2/20/19.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import "React/RCTBridgeModule.h"
#import "React/RCTEventEmitter.h"
@interface RCT_EXTERN_MODULE(RNArcGISMapViewModule, RCTEventEmitter)
RCT_EXTERN_METHOD(sendIsRoutingChanged: (BOOL*)value)
@end
