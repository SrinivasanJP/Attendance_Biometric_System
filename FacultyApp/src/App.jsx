import { useState } from 'react'
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
  const [loadState,setLoadState] = useState(false);
  const [courseDetails, setCourseDetails] = useState({});
  const sessionCreateHandle = async()=>{
    if(confirm("Do you want to create new session?")){
      if(sessionID!="NA"){
       await deleteSession();
      }
      setCourseDetails({courseName: '',
      courseID: '',
      studentRegisters:''})
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
        return <ViewList sessionID={sessionID} courseDetails={courseDetails} setFragment={setFragment} deleteSession={deleteSession}/>
      case "register":
        return <CourseRegister setFragment={setFragment}/>
      default:
        return <Default setCourseDetails={setCourseDetails} setFragment={setFragment} setSessionID={setSessionID}/>
    }
  }
  const deleteSession = async ()=>{
    setLoadState(true);
    const sessionRef = doc(db, "Attendance", sessionID);
    await deleteDoc(sessionRef).then(()=>{
      console.log("Document deleted!");
      setSessionID("NA")
      setFragment("default");
      setLoadState(false);
    }).catch((err)=>{
      console.error("error removing document: " +err);
      setLoadState(false);
    });
  }
  
  return (
    <>
    <div className='flex justify-between m-4 rounded-2xl bg-gray-200 py-5 px-10 items-center'>
      {loadState && <div className='absolute top-0 right-0 backdrop-blur-md w-full h-full flex justify-center bg-gradient-to-tr from-cyan-300/70 to-sky-300/30  items-center flex-col gap-5'>
        <div className=' border-sky-600 border-x-8 w-10 h-10 bg-transparent rounded-full border-t-8 border-b-8  border-b-white animate-spin '></div>
  <p className=' font-semibold text-2xl   text-sky-800 bg-white px-8 py-2 rounded-3xl animate-pulse'>Hold for a while</p>
      </div>
  
      
      }
    
      <div>
      <h1 className=' font-semibold  text-2xl cursor-pointer'  onClick={()=>{
        deleteSession();
        }}>VAttendance</h1>
      <h2 className=' font-semibold'>Session ID : <span className=' font-mono'>{sessionID}</span></h2>
      </div>
      <div className='flex'>
      <button className=' bg-blue-400 rounded-xl px-10 py-5 text-white font-bold text-xl mx-3' onClick={handleRegister}>Register Course</button>
      <button className=' bg-blue-400 rounded-xl px-10 py-5 text-white font-bold text-xl' onClick={sessionCreateHandle}>Create New Session</button>
      </div>
    </div>
    <div className='flex  justify-center items-center'>
    <p className=' text-center text-red-500'>Note: Don't refresh page once you created session</p>
    <button className=' bg-slate-400 px-5 py-2 rounded-xl text-white font-bold mx-10' onClick={()=>{
      if(sessionID!='NA'){
      deleteSession();
      }
    }}>Click here to delete session</button>
    </div>
    {/* TODO: 1. Register page change 
              2. Optimize the Faculty app make user friendly 
              3. rewrap the Finger printing */}
   

      
      {renderFragment()}
    </>
  )
}

export default App
