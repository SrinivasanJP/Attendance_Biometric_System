import { IoMdRefresh } from "react-icons/io"
import { IoMdAdd } from "react-icons/io";
import React, { useEffect, useState } from 'react'
import {db} from "../config/firebase"
import { doc, getDoc } from 'firebase/firestore';
import {getLocation} from '../helpers/locationData';
import AttendeesTable from '../Components/AttendeesTable';
import AbsenteesTable from "../Components/AbsenteesTable"
import { getAbsenteesList } from "../helpers/AbsenteesList";

const ViewList = ({sessionID, courseDetails}) => {
  sessionID="test"
  const [attendees, setAttendees] = useState([]);
  const [userLocation, setUserLocation] = useState(null);
  const [loading, setLoading] = useState(false);
  
  const [absenteesList, setAbsenteesList] = useState([]);
  const [registeredStudents, setRegisteredStudents] = useState(courseDetails.studentRegisters.split(","))
  const getAbsenteesList = (combinedData)=>{
    const presentStudents = combinedData.map((data) => data.registerNo);
  const absentees = registeredStudents.filter((student) => !presentStudents.includes(student));

  // Set the list of absentees in state
  setAbsenteesList(absentees);
  }
  useEffect(()=>{
    getAbsenteesList(attendees)
  },[attendees]);
  useEffect(() => {
    const fetchLocation = async () => {
      try {
        const locationData = await getLocation(); // Get user's current location
        setUserLocation(locationData); // Update user's location in state
      } catch (error) {
        console.error('Error fetching location:', error);
      }
    };
    fetchLocation();
  }, []);

  

  const fetchData = async () => {
    setLoading(true);
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
        var data = sessionSnap.data()[key].split(",");
        // Push combined data into the array
        combinedData.push({
          fingerprint: key,
          imageURL: data[0],
          latitude: data[1],
          longitude: data[2],
          altitude: data[3],
          userName: userSnap.data().name,
          registerNo: userSnap.data().registerNo
        });
      } else {
        console.log("Some unknown user entered security breach");
      }
    }

    // Update state with the combined array of data
    setAttendees(combinedData);
    getAbsenteesList(combinedData);
  } else {
    console.log("No document found.");
  }
  setLoading(false);
  };
  
  useEffect(()=>{ 
    fetchData();
  },[]);
  
  const handleExport = () => {
    fetchData().then(()=>{
      if(attendees.length<=0){
        alert("No attendies data to export");
        return
      }
      // Create CSV content
      const csvContent = "Name,Register No,Image URL,Latitude,Longitude,Altitude\n" + attendees.map(a =>
        `${a.userName},${a.registerNo},${a.imageURL},${a.latitude},${a.longitude},${a.altitude}`
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
    })
   
  };


  
  return (
    <div>
      <div className=' flex justify-between mx-10 my-5 items-center'>
        <div>
          <div className="flex items-center ">
          <h2 className=' font-bold text-xl'>Attendees</h2>
          <IoMdRefresh size={23} className={`inline-block mx-5 ${loading?'animate-spin':''}`} onClick={()=>{
            
            setAttendees([]);
            fetchData()}}/>
          </div>
          <div className="flex">
            <h1 className=" font-semibold text-lg">{courseDetails.courseName}</h1>
            <h2 className="mx-4 font-mono text-lg">{`(${courseDetails.courseID})`}</h2>
          </div>
        </div>
       
        
        <button onClick={handleExport} className=' rounded-xl bg-blue-400 text-white font-semibold px-10 py-3'>Export Attendance</button>
      </div>
      <div className='block rounded-xl border shadow-2xl m-4 overflow-hidden'>
      <AttendeesTable attendees={attendees} userLocation={userLocation}/>
      
      </div>
      <h1 className=" mx-10 text-xl font-bold my-5 ">Absentees List</h1>
      <div className='block rounded-xl border shadow-2xl m-4 overflow-hidden'>
      <AbsenteesTable absenteesList={absenteesList} setAttendees={setAttendees} attendees={attendees}/>
      
      </div>
      
      
    </div>
  )
}

export default ViewList