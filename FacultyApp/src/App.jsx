import { useState } from 'react'
import Secondary from './Components/Secondary'
import Default from './Fragments/Default'
import QRfragment from './Fragments/QRfragment'

function App() {
  const [fragment, setFragment] = useState(<Default />);

  return (
    <>
      <button className=' bg-blue-400 rounded-lg px-10 py-5 text-white font-bold text-xl' onClick={()=>{setFragment(<QRfragment/>)}}>Create Session</button>
      <Secondary fragment = {fragment} />
    </>
  )
}

export default App
