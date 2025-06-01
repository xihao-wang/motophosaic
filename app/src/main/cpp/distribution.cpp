#include <vector>
#include <cmath>
#include <cfloat>
#include <android/log.h>
#include "image_ppm.h"

#define LOG_TAG "mosaic"
#define LOGE(fmt, ...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##__VA_ARGS__)
#define NB_BASE_DE_DONNEE 300

int distributionMosaic(const char* inputPath,
                       const char* outputPath,
                       int tailleBloc,
                       bool repetition) {
    try {
        int nH, nW;
        lire_nb_lignes_colonnes_image_pgm(inputPath, &nH, &nW);
        int nTaille = nH * nW;
        int blocksX = nW / tailleBloc;
        int blocksY = nH / tailleBloc;
        int totalTiles = blocksX * blocksY;
        bool allowRepeat = totalTiles > NB_BASE_DE_DONNEE;
        std::vector<bool> used(NB_BASE_DE_DONNEE + 1, false);

        OCTET* ImgIn  = nullptr;
        OCTET* ImgOut = nullptr;
        allocation_tableau(ImgIn,  OCTET, nTaille);
        allocation_tableau(ImgOut, OCTET, nTaille);
        lire_image_pgm(inputPath, ImgIn, nTaille);

        struct Imag { int ID; std::vector<int> dist; };
        std::vector<Imag> L;
        L.reserve(NB_BASE_DE_DONNEE);
        for (int id = 1; id <= NB_BASE_DE_DONNEE; ++id) {
            OCTET* inT = nullptr;
            int hT, wT, tT;
            loadImagette(id, inT, hT, wT, tT);
            OCTET* oT = nullptr;
            allocation_tableau(oT, OCTET, tailleBloc * tailleBloc);
            resize_imagette(inT, hT, wT, oT, tailleBloc, tailleBloc);
            std::vector<int> dist(oT, oT + tailleBloc * tailleBloc);
            L.push_back({id, dist});
            free(inT);
            free(oT);
        }

        for (int y = 0; y <= nH - tailleBloc; y += tailleBloc) {
            for (int x = 0; x <= nW - tailleBloc; x += tailleBloc) {
                std::vector<int> bdist(tailleBloc * tailleBloc);
                for (int dy = 0; dy < tailleBloc; ++dy)
                    for (int dx = 0; dx < tailleBloc; ++dx)
                        bdist[dy * tailleBloc + dx] = ImgIn[(y + dy) * nW + (x + dx)];

                float bestD = FLT_MAX;
                int bestId = 1;
                for (auto& im : L) {
                    if (!allowRepeat && used[im.ID]) continue;
                    float dsum = 0;
                    for (int i = 0; i < (int)bdist.size(); ++i) {
                        float diff = im.dist[i] - bdist[i];
                        dsum += sqrt(diff * diff);
                    }
                    if (dsum < bestD) {
                        bestD = dsum;
                        bestId = im.ID;
                    }
                }
                if (!allowRepeat) used[bestId] = true;

                OCTET* i2 = nullptr;
                int h2, w2, t2;
                loadImagette(bestId, i2, h2, w2, t2);
                OCTET* o2 = nullptr;
                allocation_tableau(o2, OCTET, tailleBloc * tailleBloc);
                resize_imagette(i2, h2, w2, o2, tailleBloc, tailleBloc);
                for (int dy = 0; dy < tailleBloc; ++dy)
                    for (int dx = 0; dx < tailleBloc; ++dx)
                        ImgOut[(y + dy) * nW + (x + dx)] = o2[dy * tailleBloc + dx];
                free(i2);
                free(o2);
            }
        }

        ecrire_image_pgm(outputPath, ImgOut, nH, nW);
        free(ImgIn);
        free(ImgOut);
        return 0;
    }
    catch (std::exception& ex) {
        LOGE("distribution failed: %s", ex.what());
        return -1;
    }
}

extern "C"
int main_distribution(const char* in, const char* out, int blockSize, bool repetition) {
    return distributionMosaic(in, out, blockSize, repetition);
}
