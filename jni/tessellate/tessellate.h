typedef struct Vertex {
    double pt[3];
    int index;
    struct Vertex *prev;
} Vertex;

//void tessellate
//    (double **verts,
//     int *nverts,
//     int **tris,
//     int *ntris,
//     const float **contoursbegin,
//     const float **contoursend);
