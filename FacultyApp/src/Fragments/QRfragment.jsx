import React from 'react'
import QRCode from 'qrcode.react';


const generateSessionID = () => {
  const randomChars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  const length = 10; // Adjust the length of the session ID as needed
  let sessionID = '';

  for (let i = 0; i < length; i++) {
    const randomIndex = Math.floor(Math.random() * randomChars.length);
    sessionID += randomChars.charAt(randomIndex);
  }

  return sessionID;
};


const QRfragment = () => {
  const sessionID = generateSessionID();

    return (
      <div>
      <div>QR fragment:</div>
      <QRCode
        value={sessionID}
        size={200} // Adjust the size of the QR code
        fgColor="#000" // Set the foreground color
        bgColor="#fff" // Set the background color
        level="H" // Set the error correction level: 'L', 'M', 'Q', or 'H'
        includeMargin // Add white border around the QR code
      />
    </div>
        )
}

export default QRfragment