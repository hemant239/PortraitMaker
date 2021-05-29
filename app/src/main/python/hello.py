import cv2
import numpy as np
from PIL import Image
import io
import base64

def process_images(main_image_encoded,all_images_encoded):
    other_images=[]

    imageheight=48
    imageWidth=64
    number_of_images=100
    all_images_encoded=list(all_images_encoded)

    decoded_main_image=base64.b64decode(main_image_encoded)
    np_main_image=np.fromstring(decoded_main_image,np.uint8)
    main_image=cv2.imdecode(np_main_image,cv2.IMREAD_UNCHANGED)
    main_image=cv2.resize(main_image,(imageWidth*number_of_images,imageheight*number_of_images))
    final_image=main_image
    for single_image in all_images_encoded:
        decoded_single_image=base64.b64decode(single_image)
        np_single_image=np.fromstring(decoded_single_image,np.uint8)
        temp_single_image=cv2.imdecode(np_single_image,cv2.IMREAD_UNCHANGED)
        temp_single_image=cv2.resize(temp_single_image,(imageWidth,imageheight))
        other_images.append(temp_single_image)



    for i in range(0,number_of_images):
        for j in range(0,number_of_images):
            roi=main_image[i*imageheight:(i+1)*imageheight, j*imageWidth:(j+1)*imageWidth]
            min=99999999999;
            minImage=other_images[0]
            tempDiff=other_images[0]
            for img in other_images:
                cv2.absdiff(img,roi,tempDiff)
                tempSum=np.sum(cv2.sumElems(tempDiff))
                if(tempSum<min):
                    minImage=img
                    min=tempSum
            main_image[i*imageheight:(i+1)*imageheight, j*imageWidth:(j+1)*imageWidth]=minImage

    main_image=cv2.addWeighted(final_image,1,main_image,1,0)
    main_image=cv2.cvtColor(main_image,cv2.COLOR_BGR2RGB)
    pil_img=Image.fromarray(main_image)
    buff=io.BytesIO()
    pil_img.save(buff,format="PNG")
    resultImage_str=base64.b64encode(buff.getvalue())
    return ""+str(resultImage_str,'utf-8')










