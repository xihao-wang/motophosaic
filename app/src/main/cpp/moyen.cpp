#include <vector>
#include <cmath>
#include <cfloat>
#include <android/log.h>
#include "image_ppm.h"

#define LOG_TAG "mosaic"
#define LOGE(fmt, ...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##__VA_ARGS__)
#define NB_BASE_DE_DONNEE 300

int generatePhotoMosaic(const char* inputPath,
                 const char* outputPath,
                 int tailleBloc) {
    try {
        int nH, nW;
        lire_nb_lignes_colonnes_image_pgm(inputPath, &nH, &nW);
        int nTaille = nH * nW;
        OCTET* ImgIn  = nullptr;
        OCTET* ImgOut = nullptr;
        allocation_tableau(ImgIn,  OCTET, nTaille);
        allocation_tableau(ImgOut, OCTET, nTaille);
        lire_image_pgm(inputPath, ImgIn, nTaille);


        std::vector<float> moy(NB_BASE_DE_DONNEE+1);
        for (int id=1; id<=NB_BASE_DE_DONNEE; ++id) {
            OCTET* tile = nullptr;
            int hT,wT,tT;
            loadImagette(id, tile,hT,wT,tT);
            float s=0;
            for(int i=0;i<tT;++i) s+=tile[i];
            moy[id] = s / tT;
            free(tile);
        }


        for(int y=0; y<=nH-tailleBloc; y+=tailleBloc){
            for(int x=0; x<=nW-tailleBloc; x+=tailleBloc){
                float sumB=0;
                for(int dy=0;dy<tailleBloc;++dy)
                    for(int dx=0;dx<tailleBloc;++dx)
                        sumB += ImgIn[(y+dy)*nW + (x+dx)];
                sumB /= (tailleBloc*tailleBloc);

                int bestId=1; float bestD=FLT_MAX;
                for(int id=1; id<=NB_BASE_DE_DONNEE; ++id){
                    float d = fabs(moy[id] - sumB);
                    if(d<bestD){ bestD=d; bestId=id; }
                }

                OCTET* inT=nullptr;
                int hT,wT,tT;
                loadImagette(bestId, inT,hT,wT,tT);
                OCTET* outT=nullptr;
                allocation_tableau(outT, OCTET, tailleBloc*tailleBloc);
                resize_imagette(inT,hT,wT,outT,tailleBloc,tailleBloc);
                for(int dy=0; dy<tailleBloc; ++dy)
                    for(int dx=0; dx<tailleBloc; ++dx)
                        ImgOut[(y+dy)*nW + (x+dx)] = outT[dy*tailleBloc + dx];
                free(inT); free(outT);
            }
        }

        ecrire_image_pgm(outputPath, ImgOut, nH, nW);
        free(ImgIn); free(ImgOut);
        return 0;
    }
    catch(std::exception& ex){
        LOGE("average failed: %s", ex.what());
        return -1;
    }
}

extern "C"
int main_average(const char* in, const char* out, int blockSize) {
    return generatePhotoMosaic(in, out, blockSize);
}