import React from 'react';
import { IoMdAddCircleOutline } from 'react-icons/io';
import { calculateAttendeeProximity } from '../helpers/locationData';

const ReviewListTable = ({ reviewList, userLocation, setAttendees, attendees, setReviewList }) => {
  
  const handleAddtoAttendees = (index) => {
    // Get the attendee to move
    const attendeeToAdd = reviewList[index];

    // Update the reviewList by removing the selected attendee
    const updatedReviewList = reviewList.filter((_, i) => i !== index);
    setReviewList(updatedReviewList);

    // Update the attendees list by adding the selected attendee
    setAttendees((prevAttendees) => [...prevAttendees, attendeeToAdd]);
  };

  return (
    <table className="min-w-full overflow-scroll">
      <thead className="bg-gradient-to-r from-blue-300 to-slate-100 rounded-xl">
        <tr>
          <th className="px-6 py-3 uppercase text-start text-sm font-medium w-[20%]">Name</th>
          <th className="px-6 py-3 uppercase text-start text-sm font-medium w-[20%]">Register NO</th>
          <th className="px-6 py-3 w-[20%] uppercase text-start text-sm font-medium">Registered Image</th>
          <th className="px-6 py-3 w-[20%] uppercase text-start text-sm font-medium">Current Image</th>
          <th className="px-6 py-3 w-[10%] uppercase text-start text-sm font-medium">Face Match</th>
          <th className="px-6 py-3 w-[10%] uppercase text-start text-sm font-medium">Angle of Scan</th>
          <th className="px-6 py-3 w-[10%] uppercase text-start text-sm font-medium">Proximity</th>
          <th className="px-6 py-3 w-[10%] uppercase text-start text-sm font-medium">Action</th>
        </tr>
      </thead>
      <tbody>
        {reviewList.length <= 0 ? (
          <tr>
            <td colSpan={8} className="text-center px-6 py-3">
              No attendees in review list
            </td>
          </tr>
        ) : (
          reviewList.map((attendee, index) => (
            <tr key={index} className="odd:bg-gray-100">
              <td className="px-6 py-4 whitespace-nowrap text-lg font-medium text-gray-800 border-2 border-gray-200">
                {attendee.userName}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-lg font-medium text-gray-800 border-2 text-center border-gray-200">
                {attendee.registerNo}
              </td>
              <td className="px-6 py-4 text-center">
                <img className="inline-block" src={attendee.initImage} alt="attendee image" width={100} />
              </td>
              <td className="px-6 py-4 text-center">
                <img className="inline-block" src={attendee.imageURL} alt="attendee image" width={100} />
              </td>
              <td className="px-6 py-4 text-center">
                {attendee?.match === true ? <h1>Matches</h1> : "Not Match"}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-800 border-2 text-center border-gray-200">
                {Math.round(attendee?.angle * 100) / 100}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-800 border-2 text-center border-gray-200">
                {calculateAttendeeProximity(attendee, userLocation)}
              </td>
              <td
                className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-800 border-2 text-center border-gray-200"
                onClick={() => handleAddtoAttendees(index)}
              >
                <IoMdAddCircleOutline size={25} color="#00af00" className="mx-auto" />
              </td>
            </tr>
          ))
        )}
      </tbody>
    </table>
  );
};

export default ReviewListTable;
