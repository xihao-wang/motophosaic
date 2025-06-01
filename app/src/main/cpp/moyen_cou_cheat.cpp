#include "third_party/stb/stb_image_write.h"
#include <vector>
#include <cmath>
#include <cfloat>
#include <android/log.h>
#include "image_ppm.h"

#define LOG_TAG "mosaic_cheat_color"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define NB_BASE_DE_DONNEE 300

int generateCheatColor(const char* inputPath, const char* outputPath, int tailleBloc, bool) {
    try {
        int nH, nW;
        lire_nb_lignes_colonnes_image_ppm(inputPath, &nH, &nW);
        int N3 = nH * nW * 3;
        int blocksX = nW / tailleBloc;
        int blocksY = nH / tailleBloc;
        int totalTiles = blocksX * blocksY;
        bool allowRepeat = totalTiles > NB_BASE_DE_DONNEE;
        std::vector<bool> used(NB_BASE_DE_DONNEE+1, false);

        OCTET *ImgIn = nullptr, *ImgMos = nullptr, *ImgOut = nullptr;
        allocation_tableau(ImgIn,  OCTET, N3);
        allocation_tableau(ImgMos, OCTET, N3);
        allocation_tableau(ImgOut, OCTET, N3);
        lire_image_ppm(inputPath, ImgIn, nH * nW);

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
                sum0 += resz[i + 0];
                sum1 += resz[i + 1];
                sum2 += resz[i + 2];
            }
            tiles.push_back({id, {sum0 / count, sum1 / count, sum2 / count}});
            free(tmplt);
            free(resz);
        }

        for (int y = 0; y <= nH - tailleBloc; y += tailleBloc) {
            for (int x = 0; x <= nW - tailleBloc; x += tailleBloc) {
                float bloc0 = 0, bloc1 = 0, bloc2 = 0;
                for (int dy = 0; dy < tailleBloc; ++dy) {
                    for (int dx = 0; dx < tailleBloc; ++dx) {
                        int idx = ((y + dy) * nW + (x + dx)) * 3;
                        bloc0 += ImgIn[idx + 0];
                        bloc1 += ImgIn[idx + 1];
                        bloc2 += ImgIn[idx + 2];
                    }
                }
                int count = tailleBloc * tailleBloc;
                bloc0 /= count;
                bloc1 /= count;
                bloc2 /= count;

                float bestD = FLT_MAX;
                int bestId = 1;
                for (auto& t : tiles) {
                    if (!allowRepeat && used[t.ID]) continue;
                    float d0 = t.avg[0] - bloc0;
                    float d1 = t.avg[1] - bloc1;
                    float d2 = t.avg[2] - bloc2;
                    float dist = std::sqrt(d0*d0 + d1*d1 + d2*d2);
                    if (dist < bestD) {
                        bestD = dist;
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
                        ImgMos[dst + 0] = resz[src + 0];
                        ImgMos[dst + 1] = resz[src + 1];
                        ImgMos[dst + 2] = resz[src + 2];
                    }
                }
                free(tmplt);
                free(resz);
            }
        }

        for (int i = 0; i < N3; ++i) {
            ImgOut[i] = (OCTET)(0.3f * ImgIn[i] + 0.7f * ImgMos[i]);
        }

        if (!stbi_write_png(outputPath, nW, nH, 3, ImgOut, nW * 3)) {
            LOGE("stbi_write_png failed");
            free(ImgIn);
            free(ImgMos);
            free(ImgOut);
            return -1;
        }

        free(ImgIn);
        free(ImgMos);
        free(ImgOut);
        return 0;
    }
    catch (std::exception &ex) {
        LOGE("cheat_color failed: %s", ex.what());
        return -1;
    }
}

extern "C"
int main_cheat_color(const char* in, const char* out, int blockSize, bool withRep) {
    return generateCheatColor(in, out, blockSize, withRep);
}
