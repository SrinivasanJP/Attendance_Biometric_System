export const generateSessionID = () => {
    const randomChars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    const length = 10; // Adjust the length of the session ID as needed
    let sessionID = '';
  
    for (let i = 0; i < length; i++) {
      const randomIndex = Math.floor(Math.random() * randomChars.length);
      sessionID += randomChars.charAt(randomIndex);
    }
  
    return sessionID;
  };
