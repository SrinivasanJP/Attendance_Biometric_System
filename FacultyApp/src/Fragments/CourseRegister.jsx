import React, { useState } from 'react';
import SettingSVG from './../assets/personl_settings.svg'
const CourseRegister = ({setFragment}) => { 
  const [pState, setPState] = useState(false)
  const [courseDetails, setcourseDetails] = useState({
    courseName: '',
    courseID: '',
    studentRegisters:[]
  });
  const input_box="border-b-2 w-full pl-8 p-3 mb-6  bg-stone-100 rounded-2xl shadow-sm"
const handleSubmit = async(e)=>{
    setPState(true)
    e.preventDefault()
    localStorage.setItem(courseDetails.courseName,JSON.stringify(courseDetails));
    setFragment("default")
}

  return (
    <div className="bg-[#f6f6f6] flex justify-center items-center">
      <div className="bg-[#fefefe] w-[90%] my-[10%] rounded-2xl shadow-2xl flex flex-wrap md:p-10 p-2">
        <img src={SettingSVG} alt="Login Svg" className="w-1/2 p-5 hidden md:block" />
        <div className="w-full md:w-1/2 p-5 flex flex-col justify-center">
          <h1 className="antialiased font-extrabold font text-3xl text-left mb-10">Enter Course Details</h1>
          <form className='mt-5' onSubmit={(e)=> handleSubmit(e)}>
            <label htmlFor="courseName" className="absolute pt-4 pl-2"></label>
            <input type="text" name="courseName" id="courseName" placeholder="Enter course Name" required title="Course Name" className={input_box} onChange ={(e) => setcourseDetails({...courseDetails, courseName:e.target.value})}/>
            <label htmlFor="id" className="absolute pt-4 pl-2"></label>
            <input type="text" name="id" id="id" placeholder="Enter course ID" required title="Course ID" className={input_box} onChange ={(e) => setcourseDetails({...courseDetails, courseID:e.target.value})}/>
            <label htmlFor="regs" className="absolute pt-4 pl-2"></label>
            <textarea name="" id="" 
            className={input_box} 
            required
            placeholder='Enter Student Register numbers (comma separated)'
            onChange={(e) => setStudentDetails({...studentDetails, about:e.target.value})}></textarea>
           
            <button className="inline-flex items-center px-4 justify-center py-2 mt-5 font-bold leading-6 text-sm shadow rounded-md text-white bg-blue-400 min-w-[7em] transition ease-in-out duration-150">
            <svg className={pState?"animate-spin -ml-1 mr-3 h-5 w-5 text-white":"hidden"} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>{pState?"Processing...":"Create"}
              </button>
          </form>
        </div>
      </div>
    </div>
  )
};

export default CourseRegister