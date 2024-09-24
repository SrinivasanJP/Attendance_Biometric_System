export const getLocation = () => {
  return new Promise((resolve, reject) => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { latitude, longitude, altitude } = position.coords;
          resolve({ latitude, longitude, altitude });
        },
        (error) => {
          reject(error);
        },
        {
          enableHighAccuracy: true, // Enable GPS for higher accuracy
          timeout: 30000, // Increased timeout to allow GPS more time
          maximumAge: 0 // No cache, request a new position every time
        }
      );
    } else {
      reject(new Error('Geolocation is not supported.'));
    }
  });
};

  const calculateDistance = (lat1, lon1, lat2, lon2) => {
    const R = 6371; // Radius of the earth in km
    const dLat = (lat2 - lat1) * (Math.PI / 180);
    const dLon = (lon2 - lon1) * (Math.PI / 180);
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(lat1 * (Math.PI / 180)) * Math.cos(lat2 * (Math.PI / 180)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distance = R * c; // Distance in km
    return distance;
  };
 

  
  export const calculateAttendeeFloorFromElevation = (attendeeAltitudeDegrees) => {
    const averageElevationPerFloor = 7; // Assumed average elevation change per floor in degrees

    const absoluteAltitude = Math.abs(attendeeAltitudeDegrees);
    console.log(absoluteAltitude)
    
    if (!isNaN(absoluteAltitude)) {
      const estimatedFloor = Math.floor(absoluteAltitude / averageElevationPerFloor) + 1; // Adding 1 to start counting from 1st floor
      const direction = attendeeAltitudeDegrees >= 0 ? 'above' : 'below';
      return `Estimated floor: ${estimatedFloor} (${direction} ground level)`;
    } else {
      return "Unable to determine floor";
    }
  };
  export const calculateAttendeeProximity = (attendee, userLocation) => {
    if (!userLocation) {
      return "location not available";
    }
    
    const distance = calculateDistance(
      userLocation.latitude,
      userLocation.longitude,
      parseFloat(attendee.latitude),
      parseFloat(attendee.longitude)
    );
    console.log(userLocation.latitude,
      userLocation.longitude,
      parseFloat(attendee.latitude),
      parseFloat(attendee.longitude));
    
  
    console.log("Calculated distance: ", distance);
  
    const maxProximity = .5; // Increase max proximity to 10 km for better granularity
  
    // Ensure the proximity percentage does not go negative
    const proximityPercentage = Math.max(((maxProximity - distance) / maxProximity) * 100, 0);
  
    console.log("Proximity percentage: ", proximityPercentage);
  
    return proximityPercentage.toFixed(2) + '%';
  };
  
  
  
