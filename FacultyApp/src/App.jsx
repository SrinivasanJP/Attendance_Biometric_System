import { useState } from 'react'
import Secondary from './Components/Secondary'
import Default from './Fragments/Default'
import QRfragment from './Fragments/QRfragment'
import { generateSessionID } from './helpers/generateSessionID'
import ViewList from './Fragments/ViewList'

var sessionID;
function App() {
  const [fragment, setFragment] = useState(<Default />);
  const sessionCreateHandle = ()=>{
    sessionID = generateSessionID();
    setFragment("qr");
  }

  const renderFragment = ()=>{
    switch (fragment) {
      case "qr":
        return <QRfragment setFragment={setFragment}/>
      case "viewList":
        return <ViewList />
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
