
import React, { useEffect, useState } from 'react'
import {db} from "../config/firebase"
import { doc, getDoc } from 'firebase/firestore';


const ViewList = ({sessionID}) => {
  const [attendanceFingerprint, setattendanceFingerprint] = useState([]);
  const [attendees, setAttendees] = useState([]);

const fetchData = async () => {
  const sessionRef = doc(db, "Attendance", sessionID);
  const sessionSnap = await getDoc(sessionRef);

  if (sessionSnap.exists()) {
    const dataKeys = Object.keys(sessionSnap.data());

    // Create an array to hold the combined data
    const combinedData = [];

    // Loop through the keys and fetch data for each key
    for (const key of dataKeys) {
      const userRef = doc(db, "users", key);
      const userSnap = await getDoc(userRef);

      if (userSnap.exists()) {
        // Push combined data into the array
        combinedData.push({
          key,
          imageURL: sessionSnap.data()[key],
          userName: userSnap.data().name,
          registerNo: userSnap.data().registerNo
        });
      } else {
        // Handle unknown user
        // TODO: Handle the scenario where the user is unknown
      }
    }

    // Update state with the combined array of data
    setAttendees(combinedData);
  } else {
    console.log("No document found.");
  }
};
  
  // useEffect(()=>{ 
  //   fetchData();
  // },[]);
  const AttendiesTable = () => {
    return (
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Register NO</th>
            <th>Image</th>
          </tr>
        </thead>
        <tbody>
          {attendees.map((attendee, index) => (
            <tr key={index}>
              <td>{attendee.userName}</td>
              <td>{attendee.registerNo}</td>
              <td><img src={attendee.imageURL} alt="attendee image" width={100} /></td>
            </tr>
          ))}
        </tbody>
      </table>
    );
  };
  const handleExport = () => {
    // Create CSV content
    const csvContent = "Name,Register No,Image URL\n" + attendees.map(a =>
      `${a.userName},${a.registerNo},${a.imageURL}`
    ).join("\n");

    // Create a Blob with the CSV content
    const csvBlob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });

    // Create a URL for the Blob
    const csvURL = window.URL.createObjectURL(csvBlob);

    // Create a link element and click it to trigger download
    const link = document.createElement('a');
    link.href = csvURL;
    link.setAttribute('download', `attendance_${new Date().toISOString()}.csv`);
    document.body.appendChild(link);
    link.click();

    // Save JSON data to local storage
    localStorage.setItem(`attendance_${new Date().toISOString()}`, JSON.stringify(attendees));
  };
  return (
    <div>
      <div className=' flex justify-between mx-10 my-5 items-center'>
        <h2 className=' font-bold text-xl'>Attendees</h2>
        <button onClick={handleExport} className=' rounded-xl bg-blue-400 text-white font-semibold px-10 py-3'>Export Attendance</button>
      </div>
      
      <AttendiesTable />
      
    </div>
  )
}

export default ViewList