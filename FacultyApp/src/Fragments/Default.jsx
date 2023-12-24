import React from 'react'
import { useState } from 'react';
import { FcDeleteRow } from "react-icons/fc";

const Default = () => {
  const [selectedKey, setSelectedKey] = useState(null);

  const keys = Object.keys(localStorage);

  const handleClick = (key) => {
    setSelectedKey(key);
    
  };
  const AttendiesTable = ({getAttendees}) => {
    console.log(getAttendees)
    const attendees = Array.isArray(getAttendees) ? attendees : JSON.parse(getAttendees);
    return (
      <table className="min-w-full">
        <thead className=' bg-gradient-to-r from-blue-300 to-slate-100 rounded-xl '>
          <tr>
            <th className='px-6 py-3 uppercase text-start text-sm font-medium'>Name</th>
            <th className='px-6 py-3 uppercase text-start text-sm font-medium'>Register NO</th>
            <th className='px-6 py-3 uppercase text-start text-sm font-medium'>Image</th>
          </tr>
        </thead>
        <tbody>
          {attendees.length<=0?(<tr>
            <td colSpan={3} className='text-center px-6 py-3'>No Attendees</td></tr>):attendees.map((attendee, index) => (
            <tr key={index} className='odd:bg-gray-100 '>
              <td className={"px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-800 border-2 border-gray-200"}>{attendee.userName}</td>
              <td className={"px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-800  border-2 border-gray-200"}>{attendee.registerNo}</td>
              <td className={"px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-800 dark:text-gray-200  border-2 border-gray-200"}><img src={attendee.imageURL} alt="attendee image" width={100} /></td>
            </tr>
          ))}
        </tbody>
      </table>
    );
  };

  return (
    <div className=' rounded-xl w-[90%] m-auto'>
      <table className=' w-full bg-slate-300'>
        <thead>
          <tr>
            <th>Previous Attendance</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {keys.map((key, index) => (
            <tr key={index} onClick={() => handleClick(key)}>
              <td className=' px-10 py-5 font-semibold cursor-pointer'>{key.slice(11,21)}</td>
              <td className=' text-center p-5 cursor-pointer'><FcDeleteRow size={30} className='inline-block'/></td>
            </tr>
          
          ))}
        </tbody>
      </table>
      {selectedKey && (
        <div>
          Fragment for {selectedKey}
          <AttendiesTable getAttendees={localStorage.getItem(selectedKey)}/>
        </div>
      )}
    </div>
  );
};

export default Default