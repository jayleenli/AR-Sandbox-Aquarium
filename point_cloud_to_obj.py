'''
Following this guide to try to convert point clouds into triangle mesh obj file
https://towardsdatascience.com/5-step-guide-to-generate-3d-meshes-from-point-clouds-with-python-36bad397d8ba
Note that the usecase is always going to be 2dish shape

Uses open3d
    pip3 install open3d
'''

import numpy as np
import open3d as o3d

def lod_mesh_export(mesh, lods, extension, path):
    mesh_lods={}
    for i in lods:
        mesh_lod = mesh.simplify_quadric_decimation(i)
        o3d.io.write_triangle_mesh(path+"lod_"+str(i)+extension, mesh_lod)
        mesh_lods[i]=mesh_lod
    print("generation of "+str(i)+" LoD successful")
    return mesh_lods

print("loading...")
input_path=""
output_path=""
dataname="fish_text_points_w_normals.txt" #let's try no normals, since in program opencv prob just gonna give us coords.
point_cloud= np.loadtxt(input_path+dataname,skiprows=1)

print("loaded")
#print(point_cloud)

pcd = o3d.geometry.PointCloud() #make point cloud object
pcd.points = o3d.utility.Vector3dVector(point_cloud[:,:3])
pcd.colors = o3d.utility.Vector3dVector(point_cloud[:,3:6]/255)
pcd.normals = o3d.utility.Vector3dVector(point_cloud[:,6:9])

#visual to see what just was made - needs weird installation, giving up
# o3d.visualization.draw_geometries([pcd])

#Meshing 

#BPA
distances = pcd.compute_nearest_neighbor_distance()
avg_dist = np.mean(distances)
radius = 3 * avg_dist
bpa_mesh = o3d.geometry.TriangleMesh.create_from_point_cloud_ball_pivoting(pcd,o3d.utility.DoubleVector([radius, radius * 2]))
dec_mesh = bpa_mesh.simplify_quadric_decimation(100000)
dec_mesh.remove_degenerate_triangles()
dec_mesh.remove_duplicated_triangles()
dec_mesh.remove_duplicated_vertices()
dec_mesh.remove_non_manifold_edges()

#Poisson
poisson_mesh = o3d.geometry.TriangleMesh.create_from_point_cloud_poisson(pcd, depth=8, width=0, scale=1.1, linear_fit=False)[0]
bbox = pcd.get_axis_aligned_bounding_box()
p_mesh_crop = poisson_mesh.crop(bbox)

#Export
print("exporting...")

o3d.io.write_triangle_mesh(output_path+"bpa_mesh_fish_test.obj", dec_mesh)
o3d.io.write_triangle_mesh(output_path+"p_mesh_c_fish_test.obj", p_mesh_crop)

print("done exporting")
#LoD makes lower quality meshes with less traingles for you, and saves based on the desired output
#You can change by adding to array i. e [10000, 1000, 100]
my_lods = lod_mesh_export(bpa_mesh, [10000], ".obj", output_path)

print("done with my lods")