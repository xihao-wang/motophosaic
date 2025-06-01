#define STB_IMAGE_WRITE_IMPLEMENTATION
#include "third_party/stb/stb_image_write.h"
#include <vector>
#include <cmath>
#include <cfloat>
#include <android/log.h>
#include "image_ppm.h"

#define LOG_TAG "mosaic_color"
#define LOGE(fmt, ...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##__VA_ARGS__)
#define NB_BASE_DE_DONNEE 300

int generateColorAverage(const char* inputPath, const char* outputPath, int tailleBloc, bool repetition) {
    try {
        int nH, nW;
        lire_nb_lignes_colonnes_image_ppm(inputPath, &nH, &nW);
        int nPixels3 = nH * nW * 3;
        OCTET* ImgIn  = nullptr;
        OCTET* ImgOut = nullptr;
        allocation_tableau(ImgIn,  OCTET, nPixels3);
        allocation_tableau(ImgOut, OCTET, nPixels3);
        lire_image_ppm(inputPath, ImgIn, nH * nW);

        int blocksX = nW / tailleBloc;
        int blocksY = nH / tailleBloc;
        int totalTiles = blocksX * blocksY;
        bool allowRepeat = totalTiles > NB_BASE_DE_DONNEE;
        std::vector<bool> used(NB_BASE_DE_DONNEE + 1, false);

        struct Tile { int ID; float avg[3]; };
        std::vector<Tile> tiles;
        tiles.reserve(NB_BASE_DE_DONNEE);
        for (int id = 1; id <= NB_BASE_DE_DONNEE; ++id) {
            OCTET* tmplt = nullptr;
            int hT, wT, tT;
            loadImagette_cou(id, tmplt, hT, wT, tT);
            OCTET* resz = nullptr;
            allocation_tableau(resz, OCTET, tailleBloc * tailleBloc * 3);
            resize_imagetteCouleur(tmplt, hT, wT, resz, tailleBloc, tailleBloc);

            float sum0 = 0, sum1 = 0, sum2 = 0;
            int count = tailleBloc * tailleBloc;
            for (int i = 0; i < count * 3; i += 3) {
                sum0 += resz[i];
                sum1 += resz[i + 1];
                sum2 += resz[i + 2];
            }
            Tile t; t.ID = id; t.avg[0] = sum0 / count; t.avg[1] = sum1 / count; t.avg[2] = sum2 / count;
            tiles.push_back(t);

            free(tmplt);
            free(resz);
        }

        for (int y = 0; y <= nH - tailleBloc; y += tailleBloc) {
            for (int x = 0; x <= nW - tailleBloc; x += tailleBloc) {
                float blocAvg0 = 0, blocAvg1 = 0, blocAvg2 = 0;
                for (int dy = 0; dy < tailleBloc; ++dy) {
                    for (int dx = 0; dx < tailleBloc; ++dx) {
                        int idx = ((y + dy) * nW + (x + dx)) * 3;
                        blocAvg0 += ImgIn[idx];
                        blocAvg1 += ImgIn[idx + 1];
                        blocAvg2 += ImgIn[idx + 2];
                    }
                }
                int count = tailleBloc * tailleBloc;
                blocAvg0 /= count;
                blocAvg1 /= count;
                blocAvg2 /= count;

                float bestDist = FLT_MAX;
                int bestId = 1;
                for (auto& t : tiles) {
                    if (!allowRepeat && used[t.ID]) continue;
                    float d0 = t.avg[0] - blocAvg0;
                    float d1 = t.avg[1] - blocAvg1;
                    float d2 = t.avg[2] - blocAvg2;
                    float dist = sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestId = t.ID;
                    }
                }
                if (!allowRepeat) used[bestId] = true;

                OCTET* tmplt = nullptr;
                int hT, wT, tT;
                loadImagette_cou(bestId, tmplt, hT, wT, tT);
                OCTET* resz = nullptr;
                allocation_tableau(resz, OCTET, tailleBloc * tailleBloc * 3);
                resize_imagetteCouleur(tmplt, hT, wT, resz, tailleBloc, tailleBloc);
                for (int dy = 0; dy < tailleBloc; ++dy) {
                    for (int dx = 0; dx < tailleBloc; ++dx) {
                        int dst = ((y + dy) * nW + (x + dx)) * 3;
                        int src = (dy * tailleBloc + dx) * 3;
                        ImgOut[dst]     = resz[src];
                        ImgOut[dst + 1] = resz[src + 1];
                        ImgOut[dst + 2] = resz[src + 2];
                    }
                }
                free(tmplt);
                free(resz);
            }
        }

        if (!stbi_write_png(outputPath, nW, nH, 3, ImgOut, nW * 3)) {
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
        LOGE("color_average failed: %s", ex.what());
        return -1;
    }
}

extern "C"
int main_color_average(const char* in, const char* out, int blockSize, bool repetition) {
    return generateColorAverage(in, out, blockSize, repetition);
}
