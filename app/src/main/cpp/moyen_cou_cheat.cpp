#include "third_party/stb/stb_image_write.h"
#include <vector>
#include <cmath>
#include <cfloat>
#include <android/log.h>
#include "image_ppm.h"

#define LOG_TAG "mosaic_cheat_color"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define NB_BASE_DE_DONNEE 300

int generateCheatColor(
        const char* inputPath,
        const char* outputPath,
        int  tailleBloc,
        bool /*withRep*/
) {
    try {
        // 读取彩色 PPM
        int nH, nW;
        lire_nb_lignes_colonnes_image_ppm(inputPath, &nH, &nW);
        int N3 = nH * nW * 3;
        OCTET *ImgIn = nullptr, *ImgMos = nullptr, *ImgOut = nullptr;
        allocation_tableau(ImgIn, OCTET, N3);
        allocation_tableau(ImgMos, OCTET, N3);
        allocation_tableau(ImgOut, OCTET, N3);
        lire_image_ppm(inputPath, ImgIn, nH * nW);

        struct Tile { int ID; float avg[3]; };
        std::vector<Tile> tiles;
        tiles.reserve(NB_BASE_DE_DONNEE);
        for (int id = 1; id <= NB_BASE_DE_DONNEE; ++id) {
            OCTET* tmplt = nullptr;
            int hT, wT, tT;
            // 加载彩色小图
            loadImagette_cou(id, tmplt, hT, wT, tT);
            // 缩放到块大小
            OCTET* resz = nullptr;
            allocation_tableau(resz, OCTET, tailleBloc * tailleBloc * 3);
            resize_imagetteCouleur(tmplt, hT, wT, resz, tailleBloc, tailleBloc);

            // 计算平均
            float sum[3] = {0.f, 0.f, 0.f};
            int count = tailleBloc * tailleBloc;
            for (int i = 0; i < count * 3; i += 3) {
                sum[0] += resz[i + 0];
                sum[1] += resz[i + 1];
                sum[2] += resz[i + 2];
            }
            Tile t; t.ID = id;
            t.avg[0] = sum[0] / count;
            t.avg[1] = sum[1] / count;
            t.avg[2] = sum[2] / count;
            tiles.push_back(t);

            free(tmplt);
            free(resz);
        }

        // 4) 对输入图像按块遍历，选择最接近块平均色的 tile
        for (int y = 0; y <= nH - tailleBloc; y += tailleBloc) {
            for (int x = 0; x <= nW - tailleBloc; x += tailleBloc) {
                // 4.1) 计算当前位置块的平均 RGB
                float blocAvg[3] = {0.f,0.f,0.f};
                for (int dy = 0; dy < tailleBloc; ++dy) {
                    for (int dx = 0; dx < tailleBloc; ++dx) {
                        int idx = ((y+dy)*nW + (x+dx)) * 3;
                        blocAvg[0] += ImgIn[idx+0];
                        blocAvg[1] += ImgIn[idx+1];
                        blocAvg[2] += ImgIn[idx+2];
                    }
                }
                int count = tailleBloc * tailleBloc;
                blocAvg[0] /= count;
                blocAvg[1] /= count;
                blocAvg[2] /= count;

                // 4.2) 找到最接近的 tile
                float bestDist = FLT_MAX;
                int   bestId   = 1;
                for (auto& t : tiles) {
                    float d0 = t.avg[0] - blocAvg[0];
                    float d1 = t.avg[1] - blocAvg[1];
                    float d2 = t.avg[2] - blocAvg[2];
                    float dist = sqrt(d0*d0 + d1*d1 + d2*d2);
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestId   = t.ID;
                    }
                }

                // 4.3) 把选中的 tile 缩放后贴到输出图
                OCTET* tmplt = nullptr;
                int hT, wT, tT;
                loadImagette_cou(bestId, tmplt, hT, wT, tT);
                OCTET* resz = nullptr;
                allocation_tableau(resz, OCTET, tailleBloc*tailleBloc*3);
                resize_imagetteCouleur(tmplt, hT, wT, resz, tailleBloc, tailleBloc);

                for (int dy = 0; dy < tailleBloc; ++dy) {
                    for (int dx = 0; dx < tailleBloc; ++dx) {
                        int dst = ((y+dy)*nW + (x+dx)) * 3;
                        int src = (dy*tailleBloc + dx) * 3;
                        ImgMos[dst+0] = resz[src+0];
                        ImgMos[dst+1] = resz[src+1];
                        ImgMos[dst+2] = resz[src+2];
                    }
                }

                free(tmplt);
                free(resz);
            }
        }

        // 2) 融合 30% 原图 + 70% 马赛克
        for (int i = 0; i < N3; ++i) {
            ImgOut[i] = (OCTET)(
                    0.3f * ImgIn[i] +
                    0.7f * ImgMos[i]
            );
        }

        // 输出 PNG
        if (!stbi_write_png(outputPath, nW, nH, 3, ImgOut, nW*3)) {
            LOGE("stbi_write_png failed");
            free(ImgIn); free(ImgMos); free(ImgOut);
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
int main_cheat_color(
        const char* in, const char* out, int blockSize, bool withRep
) {
    return generateCheatColor(in, out, blockSize, withRep);
}
