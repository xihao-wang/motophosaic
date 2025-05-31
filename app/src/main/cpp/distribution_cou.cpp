#include "third_party/stb/stb_image_write.h"
#include <vector>
#include <array>
#include <cmath>
#include <cfloat>
#include <android/log.h>
#include "image_ppm.h"

#define LOG_TAG "mosaic_color_distribution"
#define LOGE(fmt, ...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##__VA_ARGS__)
#define NB_BASE_DE_DONNEE 1000

int generateColorDistribution(
        const char* inputPath,
        const char* outputPath,
        int  tailleBloc,
        bool /* repetition */) {
    try {
        int nH, nW;
        lire_nb_lignes_colonnes_image_ppm(inputPath, &nH, &nW);
        int nPixels  = nH * nW;
        int stride3  = nW * 3;

        int blocksX   = nW / tailleBloc;
        int blocksY   = nH / tailleBloc;
        int totalTiles= blocksX * blocksY;
        bool allowRepeat = totalTiles > NB_BASE_DE_DONNEE;
        std::vector<bool> used(NB_BASE_DE_DONNEE + 1, false);

        OCTET* ImgIn  = nullptr;
        OCTET* ImgOut = nullptr;
        allocation_tableau(ImgIn,  OCTET, nPixels * 3);
        allocation_tableau(ImgOut, OCTET, nPixels * 3);
        lire_image_ppm(inputPath, ImgIn, nPixels);

        struct Tile {
            int ID;
            std::vector<std::array<int,3>> distrib;
        };
        std::vector<Tile> tiles;
        tiles.reserve(NB_BASE_DE_DONNEE);

        for (int id = 1; id <= NB_BASE_DE_DONNEE; ++id) {
            OCTET* tmplt = nullptr;
            int hT,wT,tT;
            loadImagette_cou(id, tmplt, hT, wT, tT);

            OCTET* resz = nullptr;
            allocation_tableau(resz, OCTET, tailleBloc * tailleBloc * 3);
            resize_imagetteCouleur(tmplt, hT, wT, resz, tailleBloc, tailleBloc);

            std::vector<std::array<int,3>> distrib(tailleBloc * tailleBloc);
            for (int i = 0; i < tailleBloc * tailleBloc; ++i) {
                distrib[i][0] = resz[i*3 + 0];
                distrib[i][1] = resz[i*3 + 1];
                distrib[i][2] = resz[i*3 + 2];
            }
            tiles.push_back({id, std::move(distrib)});

            free(tmplt);
            free(resz);
        }

        for (int y = 0; y <= nH - tailleBloc; y += tailleBloc) {
            for (int x = 0; x <= nW - tailleBloc; x += tailleBloc) {
                std::vector<std::array<int,3>> block(tailleBloc * tailleBloc);
                for (int dy = 0; dy < tailleBloc; ++dy) {
                    for (int dx = 0; dx < tailleBloc; ++dx) {
                        int idxIn  = ((y + dy) * nW + (x + dx)) * 3;
                        int idxBlk = dy * tailleBloc + dx;
                        block[idxBlk][0] = ImgIn[idxIn + 0];
                        block[idxBlk][1] = ImgIn[idxIn + 1];
                        block[idxBlk][2] = ImgIn[idxIn + 2];
                    }
                }

                float bestDist = FLT_MAX;
                int   bestId   = tiles[0].ID;
                for (auto& t : tiles) {
                    if (!allowRepeat && used[t.ID]) continue;
                    float sumDist = 0.f;
                    for (int i = 0; i < tailleBloc * tailleBloc; ++i) {
                        float d0 = t.distrib[i][0] - block[i][0];
                        float d1 = t.distrib[i][1] - block[i][1];
                        float d2 = t.distrib[i][2] - block[i][2];
                        sumDist += std::sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                    }
                    if (sumDist < bestDist) {
                        bestDist = sumDist;
                        bestId   = t.ID;
                    }
                }
                if (!allowRepeat) used[bestId] = true;

                OCTET* tmplt = nullptr;
                int h2,w2,t2;
                loadImagette_cou(bestId, tmplt, h2, w2, t2);

                OCTET* resz = nullptr;
                allocation_tableau(resz, OCTET, tailleBloc * tailleBloc * 3);
                resize_imagetteCouleur(tmplt, h2, w2, resz, tailleBloc, tailleBloc);

                for (int dy = 0; dy < tailleBloc; ++dy) {
                    for (int dx = 0; dx < tailleBloc; ++dx) {
                        int dst = ((y + dy) * nW + (x + dx)) * 3;
                        int src = (dy * tailleBloc + dx) * 3;
                        ImgOut[dst + 0] = resz[src + 0];
                        ImgOut[dst + 1] = resz[src + 1];
                        ImgOut[dst + 2] = resz[src + 2];
                    }
                }
                free(tmplt);
                free(resz);
            }
        }

        if (!stbi_write_png(outputPath, nW, nH, 3, ImgOut, stride3)) {
            LOGE("stbi_write_png failed");
            free(ImgIn);
            free(ImgOut);
            return -1;
        }

        free(ImgIn);
        free(ImgOut);
        return 0;
    }
    catch (std::exception& ex) {
        LOGE("color_distribution failed: %s", ex.what());
        return -1;
    }
}

extern "C"
int main_color_distribution(
        const char* in,
        const char* out,
        int blockSize,
        bool repetition) {
    return generateColorDistribution(in, out, blockSize, repetition);
}
