
import { IoMdRemoveCircleOutline } from 'react-icons/io';
import {calculateAttendeeFloorFromElevation, calculateAttendeeProximity} from '../helpers/locationData';
const AttendeesTable = ({attendees, userLocation, loading, setAttendees, setAbsenteesList,absenteesList}) => {
  console.log(attendees[0]);
  const handleRemoveAttendee = (index)=>{
    const attendeeToRemove = attendees[index].registerNo;

    // Remove attendee from the attendees list
    const updatedAttendees = [...attendees];
    updatedAttendees.splice(index, 1);

    // Add attendee to the absentees list
    setAttendees(updatedAttendees|| []);
    setAbsenteesList((prevAbsentees) => [...prevAbsentees, attendeeToRemove]);
    console.log(absenteesList) 
  };
   
    if(loading){
      return (  
        <div className='flex items-center justify-center'>
      <p className=' text-center my-10 mx-5  animate-pulse'>Hold on a sec! I just divided by zero. Let's give the universe a moment to sort itself out.</p>
      </div>
      )
    }
    if(attendees.length === 0){
      return (
        <div className='flex items-center justify-center'>
      <p className=' text-center my-10 mx-5  animate-pulse'>No Attendees</p>
      </div>
      )
    }
    return (
      <table className="min-w-full overflow-scroll">
        <thead className=' bg-gradient-to-r from-blue-300 to-slate-100 rounded-xl '>
          <tr>
            <th className='px-6 py-3 uppercase text-start text-sm font-medium w-[20%]'>Name</th>
            <th className='px-6 py-3 uppercase text-start text-sm font-medium w-[20%]'>Register NO</th>
            <th className='px-6 py-3 w-[20%] uppercase text-start text-sm font-medium'>Registered Image</th>
            <th className='px-6 py-3 w-[20%] uppercase text-start text-sm font-medium'>Current Image</th>
            <th className='px-6 py-3 w-[10%] uppercase text-start text-sm font-medium'>face match</th>
            <th className='px-6 py-3 w-[10%] uppercase text-start text-sm font-medium'>Proximity</th>
            <th className='px-6 py-3 w-[10%] uppercase text-start text-sm font-medium'>Action</th>
          </tr>
        </thead>
        <tbody>
          {attendees.length<=0?(<tr>
            <td colSpan={3} className='text-center px-6 py-3'>No Attendees</td></tr>):attendees.map((attendee, index) => (
            <tr key={index} className='odd:bg-gray-100 '>
              <td className={"px-6 py-4 whitespace-nowrap text-lg font-medium text-gray-800 border-2 border-gray-200"}>{attendee.userName}</td>
              <td className={"px-6 py-4 whitespace-nowrap text-lg font-medium text-gray-800  border-2 text-center border-gray-200"}>{attendee.registerNo}</td>
              <td className={"px-6 py-4 text-center"}><img className='inline-block' src={attendee.initImage} alt="attendee image" width={100} /></td>
              <td className={"px-6 py-4 text-center"}><img className='inline-block' src={attendee.imageURL} alt="attendee image" width={100} /></td>
              <td className={"px-6 py-4 text-center"}>{attendee?.match== true? <h1>Matchs {Math.ceil((1-attendee?.distance)*100)}%</h1>:"not match"}</td>
              <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-800  border-2 text-center border-gray-200 ">{calculateAttendeeProximity(attendee, userLocation)}</td>
              <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-800  border-2 text-center border-gray-200" onClick={()=>handleRemoveAttendee(index)}><IoMdRemoveCircleOutline size={25} color='#fa0000' className='mx-auto '/></td>
            </tr>
          ))}
        </tbody>
      </table>
    );
  };
  export default AttendeesTable