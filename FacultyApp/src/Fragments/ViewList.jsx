
import React, { useEffect, useState } from 'react'
import {db} from "../config/firebase"
import { doc, getDoc } from 'firebase/firestore';


const ViewList = ({sessionID}) => {
  
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
          fingerprint: key,
          imageURL: sessionSnap.data()[key],
          userName: userSnap.data().name,
          registerNo: userSnap.data().registerNo
        });
      } else {
        console.log("Some unknown user entered security breach");
      }
    }

    // Update state with the combined array of data
    setAttendees(combinedData);
  } else {
    console.log("No document found.");
  }
  };
  
  useEffect(()=>{ 
    fetchData();
  },[]);
  const AttendiesTable = () => {
    return (
      <table className="min-w-full">
        <thead className=' bg-gradient-to-r from-blue-300 to-slate-100 rounded-xl '>
          <tr>
            <th className='px-6 py-3 uppercase text-start text-sm font-medium'>Name</th>
            <th className='px-6 py-3 uppercase text-start text-sm font-medium'>Register NO</th>
            <th className='px-6 py-3 uppercase text-start text-sm font-medium'>Image</th>
          </tr>
        </thead>
        <tbody>
          {attendees.length<=0?(<tr>
            <td colSpan={3} className='text-center px-6 py-3'>No Attendees</td></tr>):attendees.map((attendee, index) => (
            <tr key={index} className='odd:bg-gray-100 '>
              <td className={"px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-800 border-2 border-gray-200"}>{attendee.userName}</td>
              <td className={"px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-800  border-2 border-gray-200"}>{attendee.registerNo}</td>
              <td className={"px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-800 dark:text-gray-200  border-2 border-gray-200"}><img src={attendee.imageURL} alt="attendee image" width={100} /></td>
            </tr>
          ))}
        </tbody>
      </table>
    );
  };
  const handleExport = () => {
    if(attendees.length<=0){
      alert("No attendies data to export");
      return
    }
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
      <div className='block rounded-lg border shadow-2xl m-4'>
      <AttendiesTable />
      </div>
      
      
    </div>
  )
}

export default ViewList