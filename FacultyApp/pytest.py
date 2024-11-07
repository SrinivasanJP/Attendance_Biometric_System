from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from deepface import DeepFace
import requests
from io import BytesIO
from PIL import Image

app = FastAPI()
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173"],  # Update with the URL of your frontend
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class FaceComparisonRequest(BaseModel):
    image1_url: str
    image2_url: str
    model_name: str = "VGG-Face"  # Optional, defaults to 'VGG-Face'

def download_image(url: str) -> Image.Image:
    response = requests.get(url)
    if response.status_code != 200:
        raise HTTPException(status_code=404, detail="Image not found at URL.")
    return Image.open(BytesIO(response.content))

@app.post("/compare-faces/")
async def compare_faces(request: FaceComparisonRequest):
    try:
        # Download images from provided URLs
        image1 = download_image(request.image1_url)
        image2 = download_image(request.image2_url)
        
        # Save images temporarily to pass to DeepFace
        image1.save("temp_image1.jpg")
        image2.save("temp_image2.jpg")
        
        # Perform face verification using DeepFace
        result = DeepFace.verify(
            img1_path="temp_image1.jpg",
            img2_path="temp_image2.jpg",
            model_name=request.model_name
        )
        
        # Return the result to the frontend
        if result["verified"]:
            return {"match": True, "distance": result["distance"]}
        else:
            return {"match": False, "distance": result["distance"]}
    
    except Exception as e:
        print(e)
        raise HTTPException(status_code=500, detail=str(e))

# Run the server with:
# uvicorn script_name:app --reload
