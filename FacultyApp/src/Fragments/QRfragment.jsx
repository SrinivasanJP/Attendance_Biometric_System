import React from 'react'
import QRCode from 'qrcode.react';
import { useState, useEffect } from 'react';

const QRfragment = ({setFragment, sessionID}) => {
  const steps = [
    "Open the Digital ID application on your Android mobile device.",
    "Enter your registration number and name (skip if already completed).",
    "Click on 'Mark Attendance.'",
    "Scan the displayed QR code.",
    "Once the scan is complete, your picture will be taken for attendance.",
    "Please wait for a moment for your data to be sent."
];
  const [timeLeft, setTimeLeft] = useState(10);
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
      <div className=' flex flex-wrap justify-center items-center'>
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
        <ol>
          {steps.map((instruct, index)=>(
            <li key={index} className=' m-5 text-xl border-2 border-blue-400 shadow-xl rounded-xl p-5 font-semibold'>{`Step ${index+1}: ${instruct}`}</li>
          ))}
        </ol>
      </div>
      
        )
}

export default QRfragment