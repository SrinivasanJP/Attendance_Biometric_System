import { IoMdRefresh } from "react-icons/io";
import React, { useEffect, useState } from 'react';
import { db } from "../config/firebase";
import { doc, getDoc } from 'firebase/firestore';
import { getLocation } from '../helpers/locationData';
import AttendeesTable from '../Components/AttendeesTable';
import AbsenteesTable from "../Components/AbsenteesTable";
import ReviewListTable from "../Components/ReviewListTable"; 
import axios from 'axios';

const ViewList = ({ sessionID, courseDetails, setFragment, deleteSession }) => {
  const [attendees, setAttendees] = useState([]);
  const [reviewList, setReviewList] = useState([]); 
  const [userLocation, setUserLocation] = useState(null);
  const [loading, setLoading] = useState(false);
  const [absenteesList, setAbsenteesList] = useState([]);
  const [registeredStudents, setRegisteredStudents] = useState(
    courseDetails.studentRegisters.length > 0
      ? courseDetails.studentRegisters.split(",")
      : []
  );

  const getAbsenteesList = (combinedData) => {
    const presentStudents = combinedData.map((data) => data.registerNo.trim());
    const absentees = registeredStudents.filter((student) => !presentStudents.includes(student.trim()));
    setAbsenteesList(absentees);
  };

  useEffect(() => {
    getAbsenteesList(attendees);
  }, [attendees]);

  useEffect(() => {
    const fetchLocation = async () => {
      try {
        const locationData = await getLocation();
        setUserLocation(locationData);
      } catch (error) {
        console.error('Error fetching location:', error);
      }
    };
    fetchLocation();
  }, []);

  const compareFaces = async (image1_url, image2_url) => {
    try {
      const response = await axios.post('http://localhost:8000/compare-faces/', {
        image1_url: image1_url,
        image2_url: image2_url,
        model_name: "VGG-Face",
      });
      return response.data; // { match, distance }
    } catch (error) {
      console.error("Face comparison failed:", error);
      return { match: false, distance: 1 }; // Default to unmatched
    }
  };

  const fetchData = async () => {
    setLoading(true);
    const sessionRef = doc(db, "Attendance", "ybmVEKNZPM");
    const sessionSnap = await getDoc(sessionRef);

    if (sessionSnap.exists()) {
      const dataKeys = Object.keys(sessionSnap.data());
      const combinedData = [];

      for (const key of dataKeys) {
        const userRef = doc(db, "users", key);
        const userSnap = await getDoc(userRef);

        if (userSnap.exists()) {
          const data = sessionSnap.data()[key].split(",");
          combinedData.push({
            fingerprint: key,
            imageURL: data[0],
            latitude: data[1],
            longitude: data[2],
            altitude: data[3],
            angle: data[4],
            initImage: userSnap.data().initImageURL,
            userName: userSnap.data().name,
            registerNo: userSnap.data().registerNo,
          });
        } else {
          console.log("Unknown user detected.");
        }
      }

      await fetchFaceComparisons(combinedData);
      getAbsenteesList(combinedData);
    } else {
      console.log("No document found.");
    }
    setLoading(false);
  };

  useEffect(() => {
    fetchData();
  }, []);

  const fetchFaceComparisons = async (combinedData) => {
    const attendeesList = [];
    const reviewListData = [];

    await Promise.all(
      combinedData.map(async (attendee) => {
        const comparisonResult = await compareFaces(attendee.initImage, attendee.imageURL);
        const updatedAttendee = { ...attendee, ...comparisonResult };

        if (comparisonResult.match) {
          attendeesList.push(updatedAttendee);
        } else {
          reviewListData.push(updatedAttendee);
        }
      })
    );

    setAttendees(attendeesList);
    setReviewList(reviewListData); // Update review list
  };

  const handleExport = () => {
    if (attendees.length <= 0) {
      alert("No attendees data to export");
      return;
    }
    const csvContent = "Name,Register No,initImage URL,Image URL,FaceMatch,matchPercent,Latitude,Longitude,Altitude,Device Signature\n" +
      attendees.map(a =>
        `${a.userName},${a.registerNo},${a.initImage},${a.imageURL},${a?.match},${1 - a?.distance},${a.latitude},${a.longitude},${a.altitude},${a.fingerprint}`
      ).join("\n");

    const csvBlob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const csvURL = window.URL.createObjectURL(csvBlob);
    const link = document.createElement('a');
    link.href = csvURL;
    link.setAttribute('download', `attendance_${new Date().toISOString()}.csv`);
    document.body.appendChild(link);
    link.click();

    localStorage.setItem(`attendance_${new Date().toISOString()}`, JSON.stringify(attendees));
    if (confirm("May I close this session?")) {
      deleteSession();
      setFragment("default");
    }
  };

  return (
    <div>
      {reviewList.length!=0 && <>
        <h1 className="mx-10 text-xl font-bold my-5">Review List</h1>
      <div className='block rounded-xl border shadow-2xl m-4 overflow-hidden'>
        <ReviewListTable reviewList={reviewList} userLocation={userLocation} loading={loading} setAttendees={setAttendees} setReviewList={setReviewList} />
      </div>
      </>}
      <div className='flex justify-between mx-10 my-5 items-center'>
        <div>
          <div className="flex items-center">
            <h2 className='font-bold text-xl'>Attendees</h2>
            <IoMdRefresh size={23} className={`inline-block mx-5 ${loading ? 'animate-spin' : ''}`} onClick={() => {
              setAttendees([]);
              setReviewList([]);
              fetchData();
            }} />
          </div>
          <div className="flex">
            <h1 className="font-semibold text-lg">{courseDetails.courseName}</h1>
            <h2 className="mx-4 font-mono text-lg">{`${courseDetails.courseID}`}</h2>
          </div>
        </div>
        <button onClick={handleExport} className='rounded-xl bg-blue-400 text-white font-semibold px-10 py-3'>Export Attendance</button>
      </div>
      <div className='block rounded-xl border shadow-2xl m-4 overflow-auto'>
        <AttendeesTable attendees={attendees} userLocation={userLocation} loading={loading} setAttendees={setAttendees} setAbsenteesList={setAbsenteesList} absenteesList={absenteesList} />
      </div>
      <h1 className="mx-10 text-xl font-bold my-5">Absentees List</h1>
      <div className='block rounded-xl border shadow-2xl m-4 overflow-hidden'>
        <AbsenteesTable absenteesList={absenteesList} setAttendees={setAttendees} attendees={attendees} />
      </div>
      
    </div>
  );
};

export default ViewList;
