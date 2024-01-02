import { useEffect, useState } from 'react'
import Default from './Fragments/Default'
import QRfragment from './Fragments/QRfragment'
import { generateSessionID } from './helpers/generateSessionID'
import ViewList from './Fragments/ViewList'
import { doc, deleteDoc } from 'firebase/firestore' 
import { db } from './config/firebase'
import CourseRegister from './Fragments/CourseRegister'

function App() {
  const [fragment, setFragment] = useState("");
  const [sessionID, setSessionID] = useState("NA");
  const [courseDetails, setCourseDetails] = useState("");
  const sessionCreateHandle = async()=>{
    if(confirm("Do you want to create new session?")){
      if(sessionID!="NA"){
       await deleteSession();
      }
      setSessionID(generateSessionID());
      setFragment("qr");
    }
    
  }
  const handleRegister = async ()=>{
    if (sessionID !== 'NA') {
      await deleteSession();
    }
    setFragment("register");
  }

  const renderFragment = ()=>{
    switch (fragment) {
      case "qr":
        return <QRfragment setFragment={setFragment} sessionID={sessionID}/>
      case "viewList":
        return <ViewList sessionID={sessionID} courseDetails={courseDetails}/>
      case "register":
        return <CourseRegister setFragment={setFragment}/>
      default:
        return <Default setCourseDetails={setCourseDetails} setFragment={setFragment} setSessionID={setSessionID}/>
    }
  }
  const deleteSession = async ()=>{
    const sessionRef = doc(db, "Attendance", sessionID);
    await deleteDoc(sessionRef).then(()=>{
      console.log("Document deleted!");
      setSessionID("NA")
    }).catch((err)=>{
      console.error("error removing document: " +err);
    });
  }
  const deleteSessionOnUnload = async () => {
    if (sessionID !== 'NA') {
      await deleteSession();
    }
  };

  useEffect(() => {
    window.addEventListener('beforeunload', deleteSessionOnUnload);

    return () => {
      window.removeEventListener('beforeunload', deleteSessionOnUnload);
    };
  }, [sessionID]);
  return (
    <>
    <div className='flex justify-between m-4 rounded-2xl bg-gray-200 py-5 px-10 items-center'>
    
      <div>
      <h1 className=' font-semibold  text-2xl cursor-pointer'  onClick={()=>{
        setFragment("default");
        deleteSession();
        }}>VAttendance</h1>
      <h2 className=' font-semibold'>Session ID : <span className=' font-mono'>{sessionID}</span></h2>
      </div>
      <div className='flex'>
      <button className=' bg-blue-400 rounded-xl px-10 py-5 text-white font-bold text-xl mx-3' onClick={handleRegister}>Register Course</button>
      <button className=' bg-blue-400 rounded-xl px-10 py-5 text-white font-bold text-xl' onClick={sessionCreateHandle}>Create New Session</button>
      </div>
    
    </div>
      
      {renderFragment()}
    </>
  )
}

export default App
