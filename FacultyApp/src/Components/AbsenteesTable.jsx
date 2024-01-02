import React from 'react'
import { FcDeleteRow } from 'react-icons/fc'
const AbsenteesTable = ({absenteesList}) => {
  return (
    <table className="min-w-full">
        <thead className=' bg-gradient-to-r from-blue-300 to-slate-100 rounded-xl '>
          <tr>
            <th className='px-6 py-3 uppercase text-start text-sm font-medium w-[80%]'>Register NO</th>
            <th className='px-6 py-3 uppercase text-start text-sm font-medium w-[20%]'>Action</th>
            
          </tr>
        </thead>
        <tbody>
          {absenteesList.length<=0?(<tr>
            <td colSpan={3} className='text-center px-6 py-3'>No Absentees</td></tr>):absenteesList.map((registerNo, index) => (
            <tr key={index} className='odd:bg-gray-100 '>
              
              <td className={"px-6 py-4 whitespace-nowrap text-lg font-medium text-gray-800  border-2 text-center border-gray-200"}>{registerNo}</td>
              <td className=' text-center p-5 cursor-pointer border-b-2' onClick={()=> handleDelete(key)}><FcDeleteRow size={30} className='inline-block'/></td>
            </tr>
          ))}
        </tbody>
      </table>
  )
}

export default AbsenteesTable