import React from 'react'
import QRCode from 'qrcode.react';
import { useState, useEffect } from 'react';

const QRfragment = ({setFragment, sessionID}) => {
  const [timeLeft, setTimeLeft] = useState(120);
  const formatTime = (time) => {
    const minutes = Math.floor(time / 60);
    const seconds = time % 60;
    return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  };

  useEffect(() => {
    const timer = setInterval(() => {
      setTimeLeft((prevTime) => {
        if (prevTime === 0) {
          clearInterval(timer);
          setFragment("viewList");
          return 0; 
        }
        return prevTime - 1;
      });
    }, 1000);

    return () => clearInterval(timer); 
  }, []);
    return (
      <div className=' flex flex-col'>
        <h1 className=' text-center text-2xl font-semibold'>Timer: <span>{formatTime(timeLeft)}</span></h1>
      <QRCode
        value={sessionID}
        size={800} // Adjust the size of the QR code
        fgColor="#000" // Set the foreground color
        bgColor="#fff" // Set the background color
        level="H" // Set the error correction level: 'L', 'M', 'Q', or 'H'
        includeMargin // Add white border around the QR code
      />
    </div>
        )
}

export default QRfragment