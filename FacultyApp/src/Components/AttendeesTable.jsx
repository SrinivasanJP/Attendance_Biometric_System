
import {calculateAttendeeFloorFromElevation, calculateAttendeeProximity} from '../helpers/locationData';
const AttendeesTable = ({attendees, userLocation}) => {
    if(attendees.length === 0){
      return (
        <div className='flex items-center justify-center'>
      <p className=' text-center my-10 mx-5  animate-pulse'>Hold on a sec! I just divided by zero. Let's give the universe a moment to sort itself out.</p>
      </div>
      )
    }
    return (
      <table className="min-w-full">
        <thead className=' bg-gradient-to-r from-blue-300 to-slate-100 rounded-xl '>
          <tr>
            <th className='px-6 py-3 uppercase text-start text-sm font-medium w-[30%]'>Name</th>
            <th className='px-6 py-3 uppercase text-start text-sm font-medium w-[20%]'>Register NO</th>
            <th className='px-6 py-3 uppercase text-start text-sm font-medium'>Image</th>
            <th className='px-6 py-3 uppercase text-start text-sm font-medium'>Proximity</th>
          </tr>
        </thead>
        <tbody>
          {attendees.length<=0?(<tr>
            <td colSpan={3} className='text-center px-6 py-3'>No Attendees</td></tr>):attendees.map((attendee, index) => (
            <tr key={index} className='odd:bg-gray-100 '>
              <td className={"px-6 py-4 whitespace-nowrap text-lg font-medium text-gray-800 border-2 border-gray-200"}>{attendee.userName}</td>
              <td className={"px-6 py-4 whitespace-nowrap text-lg font-medium text-gray-800  border-2 text-center border-gray-200"}>{attendee.registerNo}</td>
              <td className={"px-6 py-4 text-center w-[30%]"}><img className='inline-block' src={attendee.imageURL} alt="attendee image" width={100} /></td>
              <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-800  border-2 text-center border-gray-200 w-[20%]">{calculateAttendeeProximity(attendee, userLocation)}</td>
              {console.log(calculateAttendeeFloorFromElevation(attendee.altitude))}
            </tr>
          ))}
        </tbody>
      </table>
    );
  };
  export default AttendeesTable