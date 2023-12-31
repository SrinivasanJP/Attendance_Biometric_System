const getLocation = () => {
    return new Promise((resolve, reject) => {
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const { latitude, longitude, altitude } = position.coords;
            resolve({ latitude, longitude, altitude });
          },
          (error) => {
            reject(error);
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
  export const calculateAttendeeProximity = (attendee) => {
    if(!userLocation){return "0%"}
    const distance = calculateDistance(
      userLocation.latitude,
      userLocation.longitude,
      parseFloat(attendee.latitude),
      parseFloat(attendee.longitude)
    );
    const maxProximity = 1; // Maximum proximity distance (in km)
    const proximityPercentage = ((maxProximity - distance) / maxProximity) * 100;
    return proximityPercentage.toFixed(2)>0? proximityPercentage.toFixed(2)+ '%':"0%";
  };
  const [userLocation, setUserLocation] = useState(null);
  export const fetchLocation = async () => {
    try {
      const locationData = await getLocation(); // Get user's current location
      setUserLocation(locationData); // Update user's location in state
    } catch (error) {
      console.error('Error fetching location:', error);
    }
  };