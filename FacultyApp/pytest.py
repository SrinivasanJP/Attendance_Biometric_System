from deepface import DeepFace

def compare_faces(image1_path, image2_path, model_name='VGG-Face'):
    # Compare the two images using DeepFace
    result = DeepFace.verify(img1_path=image1_path, img2_path=image2_path, model_name=model_name)
    print(result)
    # Check the result
    if result["verified"]:
        print("The faces match!")
    else:
        print("The faces do not match.")

# Provide paths to the two images you want to compare
compare_faces("gokul.jpg", "gokul.jpg")
