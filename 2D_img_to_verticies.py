'''
2d img using opencv grabcut. 
'''
import numpy as np
import cv2
from matplotlib import pyplot as plt
from PIL import Image, ImageFilter

img = cv2.imread('fish_test.jpg')
h,w,chn = img.shape
mask = np.zeros(img.shape[:2],np.uint8)
#print(img)

#Replace shades of white to white (thresholding)
img[np.where((img>=[150,150,150]).all(axis=2))] = [255,255,255]
cv2.imwrite('img_whitened.png', img)


bgdModel = np.zeros((1,65),np.float64)
fgdModel = np.zeros((1,65),np.float64)

print("image size", img.shape)
#As suggested, 10
rect = ((int)(.05 * w), (int)(.05* h), (int)(.9*w), (int)(.8*h))#(start_x, start_y, width, height)

cv2.grabCut(img,mask,rect,bgdModel,fgdModel,5,cv2.GC_INIT_WITH_RECT)
# 0-pixels and 2-pixels are put to 0 (ie background) and all 1-pixels and 3-pixels are put to 1(ie foreground pixels).
mask2 = np.where((mask==2)|(mask==0),0,1).astype('uint8')

#Improval, make sure the threshold we sent makes the mask set to background
mask2[np.where((img>=[150,150,150]).all(axis=2))] = 0

# Another way to improve is to find the largest grab cut pixel area that is connected and only use this part.

'''
"So here we used cv2.grabCut, which took quite a few parameters. First the input image, 
then the mask, then the rectangle for our main object, the background model, foreground model, 
the amount of iterations to run, and what mode you are using."

 " From here, the mask is changed so that all 0 and 2 pixels are converted to the background, 
 where the 1 and 3 pixels are now the foreground. From here, we multiply with the input image, 
 and we get our final result:"
'''
img = img*mask2[:,:,np.newaxis]

print("done, saving image")

cv2.imwrite('cropped_fish_test.png', img)

#I guess wherever its black will be the edges for our verticies.

# #Make verticies
# img_edge = Image.open('cropped.png')
# img_edge = img_edge.filter(ImageFilter.FIND_EDGES)
# img_edge.save('edges.png') 
# print("saved the edges")

