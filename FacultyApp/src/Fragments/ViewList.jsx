
import React, { useEffect, useState } from 'react'
import {db} from "../config/firebase"
import { collection, doc, getDoc } from 'firebase/firestore';

const fetchData = async () => {
  const sessionRef = doc(db, "Attendance","testsession");
  const sessionSnap = await getDoc(sessionRef);
  if(sessionSnap.exists()){
    console.log(sessionSnap.data())  }
  else{
    console.log("no doc");
  }
}
const ViewList = () => {
  const [attendies, setAttendies] = useState([]);
  
  useEffect(()=>{ 
    fetchData();
  },[]);
  return (
    <div>ViewLists</div>
  )
}

export default ViewList