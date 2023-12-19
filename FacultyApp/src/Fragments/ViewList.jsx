
import React, { useEffect, useState } from 'react'
import {db} from "../config/firebase"
import { doc, getDoc } from 'firebase/firestore';


const ViewList = ({sessionID}) => {
  const [attendanceFingerprint, setattendanceFingerprint] = useState([]);
  const [attendies, setAttendies] = useState([]);

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
    setAttendies(combinedData);
  } else {
    console.log("No document found.");
  }
};
  
  useEffect(()=>{ 
    fetchData();
  },[]);
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
          {attendies.map((attendee, index) => (
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
  return (
    <div>
      <h2>Attendies</h2>
      <AttendiesTable />
    </div>
  )
}

export default ViewList