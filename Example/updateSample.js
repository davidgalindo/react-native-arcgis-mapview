const timer = require('react-native-timer');

export const beginUpdate = (context, callback) => {
    // Check if timer exists before executing - if it does, cancel it
    endUpdate(context);

    // Create timer that sends data across 
    timer.setInterval(context, 'updatingInterval', () => {
        // I'll be sending a location update through the callback to simulate
        // server pings
        if (typeof(callback) === 'function') {
            callback(
                {
                    overlayReferenceId: 'graphicsOverlay',
                    updates: [{ 
                    referenceId: 'movingImage',
                    latitude: 39.739235 - (20 * Math.random()),
                    longitude: -104.990250 - (20 * Math.random()),
                    rotation: 360 * Math.random(),
            }]
        });
        console.log('Update logged');
        }
    }, 1500);
}

export const endUpdate = (context) => {
    if (timer.intervalExists(context, 'updatingInterval')) {
        timer.clearInterval(context, 'updatingInterval')
    }
}