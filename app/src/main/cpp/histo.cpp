#include <vector>
#include <array>
#include <cmath>
#include <cfloat>
#include <android/log.h>
#include "image_ppm.h"

#define LOG_TAG "mosaic"
#define LOGE(fmt, ...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##__VA_ARGS__)
#define NB_BASE_DE_DONNEE 300

int histogramMosaic(const char* inputPath,
                    const char* outputPath,
                    int tailleBloc) {
    try {
        int nH, nW;
        lire_nb_lignes_colonnes_image_pgm(inputPath, &nH, &nW);
        int nTaille = nH * nW;

        int blocksX = nW / tailleBloc;
        int blocksY = nH / tailleBloc;
        int totalTiles = blocksX * blocksY;
        bool allowRepeat = (totalTiles > NB_BASE_DE_DONNEE);
        std::vector<bool> used(NB_BASE_DE_DONNEE+1, false);

        OCTET* ImgIn  = nullptr;
        OCTET* ImgOut = nullptr;
        allocation_tableau(ImgIn,  OCTET, nTaille);
        allocation_tableau(ImgOut, OCTET, nTaille);
        lire_image_pgm(inputPath, ImgIn, nTaille);

        struct Imag { int ID; std::array<int,256> h; };
        std::vector<Imag> L; L.reserve(NB_BASE_DE_DONNEE);
        for(int id=1; id<=NB_BASE_DE_DONNEE; ++id){
            OCTET* inT=nullptr; int hT,wT,tT;
            loadImagette(id,inT,hT,wT,tT);
            OCTET* outT=nullptr;
            allocation_tableau(outT, OCTET, tailleBloc*tailleBloc);
            resize_imagette(inT,hT,wT,outT,tailleBloc,tailleBloc);
            std::array<int,256> histo{0};
            for(int i=0;i<tailleBloc*tailleBloc;++i)
                histo[outT[i]]++;
            L.push_back({id,histo});
            free(inT); free(outT);
        }

        for(int y=0; y<=nH-tailleBloc; y+=tailleBloc){
            for(int x=0; x<=nW-tailleBloc; x+=tailleBloc){
                std::array<int,256> hbloc{0};
                for(int dy=0; dy<tailleBloc; ++dy)
                    for(int dx=0; dx<tailleBloc; ++dx)
                        hbloc[ ImgIn[(y+dy)*nW+(x+dx)] ]++;

                float bestD=FLT_MAX; int bestId=1;
                for(auto& im: L){
                    if(!allowRepeat && used[im.ID]) continue;
                    float db=0;
                    for(int c=0;c<256;++c)
                        db += sqrt((float)hbloc[c]*im.h[c]);
                    db = -log(db);
                    if(db<bestD){ bestD=db; bestId=im.ID; }
                }
                if(!allowRepeat) used[bestId] = true;

                OCTET* i2=nullptr; int hT,wT,tT;
                loadImagette(bestId,i2,hT,wT,tT);
                OCTET* o2=nullptr; allocation_tableau(o2,OCTET,tailleBloc*tailleBloc);
                resize_imagette(i2,hT,wT,o2,tailleBloc,tailleBloc);
                for(int dy=0; dy<tailleBloc; ++dy)
                    for(int dx=0; dx<tailleBloc; ++dx)
                        ImgOut[(y+dy)*nW+(x+dx)] = o2[dy*tailleBloc+dx];
                free(i2); free(o2);
            }
        }

        ecrire_image_pgm(outputPath, ImgOut, nH, nW);
        free(ImgIn); free(ImgOut);
        return 0;
    }
    catch(std::exception& ex){
        LOGE("histo failed: %s", ex.what());
        return -1;
    }
}

extern "C"
int main_histo(const char* in, const char* out, int blockSize) {
    return histogramMosaic(in, out, blockSize);
}
