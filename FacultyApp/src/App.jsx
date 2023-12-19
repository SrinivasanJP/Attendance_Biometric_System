import { useState } from 'react'
import Default from './Fragments/Default'
import QRfragment from './Fragments/QRfragment'
import { generateSessionID } from './helpers/generateSessionID'
import ViewList from './Fragments/ViewList'

var sessionID;
function App() {
  const [fragment, setFragment] = useState("");
  const [sessionID, setSessionID] = useState("");
  const sessionCreateHandle = ()=>{
    setSessionID(generateSessionID());
    setFragment("qr");
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
      <button className=' bg-blue-400 rounded-lg px-10 py-5 text-white font-bold text-xl' onClick={sessionCreateHandle}>Create Session</button>
      {renderFragment()}
    </>
  )
}

export default App
