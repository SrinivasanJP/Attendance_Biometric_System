import React from 'react'
import { useState, useEffect } from 'react';
import { FcDeleteRow } from "react-icons/fc";

const Default = () => {
  const [selectedKey, setSelectedKey] = useState(null);

  const [keys, setKeys] = useState(Object.keys(localStorage));

  const handleClick = (key) => {
    setSelectedKey(key);
    
  };
  const handleDelete = (key) =>{
    if(confirm("Do you want to remove the attendance data")){
      localStorage.removeItem(key)
      setKeys(Object.keys(localStorage));
    }
    
  }
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
    <div className=' rounded-2xl border shadow-2xl m-4'>
      <table className=' w-full table-fixed'>
        <thead>
          <tr>
            <th className=' border-b-2 border-r-2  py-5 w-[80%]'>Previous Attendance</th>
            <th className=' border-b-2 border-l-2  py-5'>Action</th>
          </tr>
        </thead>
        <tbody>
          {keys.length<=0?<tr><td colSpan={2} className='text-center py-5'>No attendance data available</td></tr>:keys.map((key, index) => (
            <tr key={index}>
              <td className=' px-10 py-5 font-semibold cursor-pointer border-b-2 hover:text-xl' onClick={() => handleClick(key)}>{key.slice(11,21)}</td>
              <td className=' text-center p-5 cursor-pointer border-b-2' onClick={()=> handleDelete(key)}><FcDeleteRow size={30} className='inline-block'/></td>
            </tr>
          
          ))}
        </tbody>
      </table>
      {selectedKey && (
        <div className=' my-10 mx-4'>
          Fragment for {selectedKey}
          <AttendiesTable getAttendees={localStorage.getItem(selectedKey)}/>
        </div>
      )}
    </div>
  );
};

export default Default