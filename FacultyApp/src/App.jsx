import { useState } from 'react'
import Default from './Fragments/Default'
import QRfragment from './Fragments/QRfragment'
import { generateSessionID } from './helpers/generateSessionID'
import ViewList from './Fragments/ViewList'

var sessionID;
function App() {
  const [fragment, setFragment] = useState("");
  const [sessionID, setSessionID] = useState("NA");
  const sessionCreateHandle = ()=>{
    if(confirm("Do you want to create new session?")){
      setSessionID(generateSessionID());
      setFragment("qr");
    }else{

    }
    
  }

  const renderFragment = ()=>{
    switch (fragment) {
      case "qr":
        return <QRfragment setFragment={setFragment} sessionID={sessionID}/>
      case "viewList":
        return <ViewList sessionID={sessionID} />
      default:
        return <Default />
    }
  }
  return (
    <>
    <div className='flex justify-between m-4 rounded-2xl bg-gray-200 py-5 px-10 items-center'>
    
      <div>
      <h1 className=' font-semibold  text-2xl cursor-pointer'  onClick={()=>{
        setFragment("default");
        setSessionID("NA")
        }}>VAttendance</h1>
      <h2 className=' font-semibold'>Session ID : <span className=' font-mono'>{sessionID}</span></h2>
      </div>
      
    <button className=' bg-blue-400 rounded-xl px-10 py-5 text-white font-bold text-xl' onClick={sessionCreateHandle}>Create New Session</button>
    </div>
      
      {renderFragment()}
    </>
  )
}

export default App
