import React from 'react'

const Default = () => {
  const getLocalStorage = ()=>{
    const keys = localStorage.key(3)
    console.log(keys+ localStorage.getItem(keys));
  }
  getLocalStorage()
  return (
    <div>Default</div>
  )
}

export default Default