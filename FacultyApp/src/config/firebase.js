
import { initializeApp } from "firebase/app";
import { getFirestore } from "firebase/firestore";
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries

const firebaseConfig = {
  apiKey: "AIzaSyDGD_6gTSl8pRGunBrZU5En1Db_OoLLpic",
  authDomain: "digitalidattendance.firebaseapp.com",
  projectId: "digitalidattendance",
  storageBucket: "digitalidattendance.appspot.com",
  messagingSenderId: "399262293959",
  appId: "1:399262293959:web:faad0a817f54aff65683e2",
  measurementId: "G-TG5BQYT2D8"
};
const app = initializeApp(firebaseConfig);
export const db = getFirestore(app);
