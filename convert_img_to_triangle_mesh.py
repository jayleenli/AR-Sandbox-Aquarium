'''
Takes a 2D Image from a white based background and
creates a 3D mesh and 3D object from the image
'''

import sys
import numpy as np
import cv2
from matplotlib import pyplot as plt
import open3d as o3d
import copy

if (len(sys.argv) != 2):
	print("Incorrect usage: python program <path-to-image-filename> \nMake sure image is an image file that OpenCV can read.")
	sys.exit();

image_file = sys.argv[1]
start_index_filename = 0
# If in a subdirectory
if (image_file.index('/') >= 0):
	start_index_filename = image_file.rfind('/')+1
object_name = image_file[start_index_filename:image_file.index('.')]
print("PROGRESS: image loaded, object name =", object_name)
try:
	orig_img = cv2.imread(image_file)
except cv2.error as e:
	print("File does not exist or has issues")
	sys.exit();

h_orig,w_orig,chn = orig_img.shape
print("PROGRESS: original image size", orig_img.shape)

# Scale image down for faster processing
scale = 1
if (h_orig > 800 or w_orig > 800):
    scale = 800/max(h_orig, w_orig) # should be less than 1

newsize = ((int)(w_orig*scale),(int)(h_orig*scale)) 
scale_img = cv2.resize(orig_img, newsize, interpolation = cv2.INTER_AREA)

h,w,chn = scale_img.shape

# Image to Vertices
mask = np.zeros(scale_img.shape[:2],np.uint8)

img = scale_img

# Replace shades of white to white (thresholding)
img[np.where((img>=[180,180,180]).all(axis=2))] = [255,255,255]

bgdModel = np.zeros((1,65),np.float64)
fgdModel = np.zeros((1,65),np.float64)

print("PROGRESS: scaled image size", img.shape)

# This is an estimate where the window should be and has proven to be a good estimate
rect = ((int)(.05 * w), (int)(.05* h), (int)(.9*w), (int)(.9*h)) #(start_x, start_y, width, height)

cv2.grabCut(img,mask,rect,bgdModel,fgdModel,5,cv2.GC_INIT_WITH_RECT)
# 0-pixels and 2-pixels are put to 0 (ie background) and all 1-pixels and 3-pixels are put to 1(ie foreground pixels).
mask2 = np.where((mask==2)|(mask==0),0,1).astype('uint8')

#Improvement, make sure the threshold we sent makes the mask set to background
mask2[np.where((img>=[150,150,150]).all(axis=2))] = 0

img = img*mask2[:,:,np.newaxis]

print("PROGRESS: done with grabcut, saving image")

# Contouring
gwash = img 
gwashBW = cv2.cvtColor(gwash, cv2.COLOR_BGR2GRAY) #change to grayscale

ret,thresh1 = cv2.threshold(gwashBW,0,255,cv2.THRESH_BINARY) #0 because we already cropped
kernel = np.ones((5,5),np.uint8) #square image kernel used for erosion
erosion = cv2.erode(thresh1, kernel,iterations = 1) #refines all edges in the binary image

opening = cv2.morphologyEx(erosion, cv2.MORPH_OPEN, kernel)
closing = cv2.morphologyEx(opening, cv2.MORPH_CLOSE, kernel) #this is for further removing small noises and holes in the image

contours, hierarchy = cv2.findContours(closing,cv2.RETR_TREE,cv2.CHAIN_APPROX_SIMPLE) #find contours with simple approximation

dst = np.zeros((closing.shape[1], closing.shape[0]),np.uint8)  

# Or find contours and do all the points
contours, hierarchy = cv2.findContours(closing, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_NONE) 
dst = np.zeros((closing.shape[1], closing.shape[0]),np.uint8)  

print("PROGRESS: done with contours")

#Get only the largest area
areas = []

for contour in contours:
    areas.append(cv2.contourArea(contour))

max_area = max(areas)
max_area_index = areas.index(max_area)

cnt = contours[max_area_index] #largest area contour

dst = np.zeros((closing.shape[1], closing.shape[0]),np.uint8)

format_contour_x = [x[0][0] for x in contours[max_area_index]]
format_contour_y = [x[0][1] for x in contours[max_area_index]]

print("PROGRESS: found largest countour")

#Try filling in the points
zipped_all_pts = list(zip(format_contour_x, format_contour_y))
for i in range(closing.shape[0]):
    for j in range(closing.shape[1]):
        inBBx = cv2.pointPolygonTest(contours[max_area_index], (j,i), False)
        if inBBx == 1: #inside
            zipped_all_pts.append([j,i])
        #else is 0 or -1 which is on edge or outside

zipped_all_pts = np.array([list(x) for x in zipped_all_pts])


# Get color from original image
def get_color(pt_x, pt_y, orig_img):
    if(pt_x >= w or pt_x < 0): 
        return [0, 0, 0]
    elif(pt_y >= h or pt_y < 0):
        return [0, 0, 0]
    else:
        return (orig_img[pt_y][pt_x])[::-1]
    
'''
Input: points will be [[x, y],[x, y]...] format
Output: (width, length, trans_x_to_origin, trans_y_to_origin)
'''
def get_width_and_length_and_trans(points):
    #Since we know points will always be positive, we don't need to take the absolute distance
    width = max([x[0] for x in points]) - min([x[0] for x in points])
    length = max([x[1] for x in points]) - min([x[1] for x in points])
    trans_x = min([x[0] for x in points])
    trans_y = min([x[1] for x in points])
    
    return (width, length, trans_x, trans_y)

'''
Inputs:Point will be x, y in 2D
w_l_trans in format same as get_width_and_length_and_trans function
   (width, length, trans_x_to_origin, trans_y_to_origin)
Output: prints new point moved to origin
'''
def move_to_origin(point, w_l_trans):
    width = w_l_trans[0]
    height = w_l_trans[1]
    return [point[0]-(width/2)-w_l_trans[2], point[1]-(height/2)-w_l_trans[3]]

# As you can see normals hardcoded
normal = [0, 0, 1] 
normal_down = [0, 0, -1]
dummy_neg_z = -10.0
dummy_pos_z = 10.0

data = []
w_l_trans = get_width_and_length_and_trans(zipped_all_pts)

# zipped_all_pts used to be countours[max_area_index] but mesh reconstruction using these just the contour points does not work well.
for x in zipped_all_pts:
    point_color = get_color(x[0], x[1], scale_img)
    moved_pt = move_to_origin(x, w_l_trans)
    
    points_neg = [moved_pt[0], moved_pt[1], dummy_neg_z, point_color[0], point_color[1], point_color[2], normal_down[0], normal_down[1], normal_down[2]]
    data.append(points_neg)
    points_pos = [moved_pt[0], moved_pt[1], dummy_pos_z, point_color[0], point_color[1], point_color[2], normal[0], normal[1], normal[2]]
    data.append(points_pos)
   
f = open("point_mesh.txt", "w")
f.write("X Y Z R G B Nx Ny Nz\n")
for i in data:
    str_ver =  map(str,i)
    f.write(' '.join(str_ver))
    f.write('\n')

f.close()

print("PROGRESS: created and saved point mesh as point_mesh.txt")

input_path=""
output_path=""
points_filename="point_mesh.txt" 
point_cloud= np.loadtxt(input_path+points_filename,skiprows=1)

pcd = o3d.geometry.PointCloud() #make point cloud object
pcd.points = o3d.utility.Vector3dVector(point_cloud[:,:3])
pcd.colors = o3d.utility.Vector3dVector(point_cloud[:,3:6]/255)
pcd.normals = o3d.utility.Vector3dVector(point_cloud[:,6:9]) 

print("PROGRESS: loaded point cloud")
print("PROGRESS: beginning meshing...")

# Meshing (Poisson)
scale = 0.0005
poisson_mesh = o3d.geometry.TriangleMesh.create_from_point_cloud_poisson(pcd, depth=8, width=0, scale=1.1, linear_fit=False)[0]

# Scale down
poisson_mesh.scale(scale, center=poisson_mesh.get_center())
# Rotate because opencv image is upside down, not sure why
R = poisson_mesh.get_rotation_matrix_from_xyz((np.pi, 0, 0))
poisson_mesh.rotate(R, center=(0, 0, 0))

bbox = pcd.get_axis_aligned_bounding_box()
p_mesh_crop = poisson_mesh.crop(bbox)
print("PROGRESS: poisson triangle number", len(poisson_mesh.triangles))

# Export
poisson_mesh = copy.deepcopy(poisson_mesh).translate((0, 0, 0), relative=False)
p_mesh_crop = copy.deepcopy(p_mesh_crop).translate((0, 0, 0), relative=False)
poisson_mesh = poisson_mesh.compute_triangle_normals();
p_mesh_crop = p_mesh_crop.compute_triangle_normals();

# types: obj, ply, stl, gltf
o3d.io.write_triangle_mesh(output_path+ object_name+".gltf", poisson_mesh)
print("PROGRESS: done exporting")

# Simplification of model
def lod_mesh_export(mesh, lods, extension, path):
    mesh_lods={}
    for i in lods:
        mesh_lod = mesh.simplify_quadric_decimation(i)
        o3d.io.write_triangle_mesh(path+"lod_"+str(i)+extension, mesh_lod)
        mesh_lods[i]=mesh_lod
    print("generation of "+str(i)+" LoD successful")
    return mesh_lods