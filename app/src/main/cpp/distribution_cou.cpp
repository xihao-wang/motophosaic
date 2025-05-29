#include "third_party/stb/stb_image_write.h"

#include <vector>
#include <cmath>
#include <cfloat>
#include <android/log.h>
#include "image_ppm.h"

#define LOG_TAG "mosaic_color_distribution"
#define LOGE(fmt, ...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##__VA_ARGS__)

// 彩色库大小，根据实际资产数量调整
#define NB_BASE_DE_DONNEE 300

// 计算彩色分布马赛克（像素级 L2 距离总和）
int generateColorDistribution(
        const char* inputPath,
        const char* outputPath,
        int  tailleBloc,
        bool /* repetition 不影响分布算法 */
) {
    try {
        // 1) 读取输入图尺寸（PPM）
        int nH, nW;
        lire_nb_lignes_colonnes_image_ppm(inputPath, &nH, &nW);
        int nPixels   = nH * nW;
        int nPixels3  = nPixels * 3;
        int stride3   = nW * 3;

        // 2) 分配并读取整张图
        OCTET* ImgIn  = nullptr;
        OCTET* ImgOut = nullptr;
        allocation_tableau(ImgIn,  OCTET, nPixels3);
        allocation_tableau(ImgOut, OCTET, nPixels3);
        lire_image_ppm(inputPath, ImgIn, nPixels);

        // 3) 预加载每个 tile 的 per-pixel 分布 (tailleBloc*tailleBloc × 3)
        struct Tile {
            int ID;
            std::vector<std::array<int,3>> distrib;  // 长度 = tailleBloc*tailleBloc
        };
        std::vector<Tile> tiles;
        tiles.reserve(NB_BASE_DE_DONNEE);
        for (int id=1; id<=NB_BASE_DE_DONNEE; ++id) {
            OCTET* tmplt = nullptr;
            int hT,wT,tT;
            loadImagette_cou(id, tmplt, hT, wT, tT);

            // resize 到块大小
            OCTET* resz = nullptr;
            allocation_tableau(resz, OCTET, tailleBloc*tailleBloc*3);
            resize_imagetteCouleur(tmplt, hT, wT, resz, tailleBloc, tailleBloc);

            // 提取每个像素的 RGB 分布
            std::vector<std::array<int,3>> distrib(tailleBloc*tailleBloc);
            for (int i=0; i<tailleBloc*tailleBloc; ++i) {
                distrib[i][0] = resz[i*3 + 0];
                distrib[i][1] = resz[i*3 + 1];
                distrib[i][2] = resz[i*3 + 2];
            }
            tiles.push_back({id, std::move(distrib)});

            free(tmplt);
            free(resz);
        }

        // 4) 对输入图逐块，选最优 tile
        for (int y=0; y<=nH-tailleBloc; y+=tailleBloc) {
            for (int x=0; x<=nW-tailleBloc; x+=tailleBloc) {
                // 4.1) 读出当前块的 per-pixel RGB
                std::vector<std::array<int,3>> block(tailleBloc*tailleBloc);
                for (int dy=0; dy<tailleBloc; ++dy) {
                    for (int dx=0; dx<tailleBloc; ++dx) {
                        int idxIn  = ((y+dy)*nW + (x+dx)) * 3;
                        int idxBlk = dy*tailleBloc + dx;
                        block[idxBlk][0] = ImgIn[idxIn+0];
                        block[idxBlk][1] = ImgIn[idxIn+1];
                        block[idxBlk][2] = ImgIn[idxIn+2];
                    }
                }

                // 4.2) 计算每个 tile 的 L2 距离总和，选择最小
                float bestDist = FLT_MAX;
                int   bestId   = tiles[0].ID;
                for (auto& t : tiles) {
                    float sumDist = 0.f;
                    for (int i=0; i<tailleBloc*tailleBloc; ++i) {
                        float d0 = t.distrib[i][0] - block[i][0];
                        float d1 = t.distrib[i][1] - block[i][1];
                        float d2 = t.distrib[i][2] - block[i][2];
                        sumDist += std::sqrt(d0*d0 + d1*d1 + d2*d2);
                    }
                    if (sumDist < bestDist) {
                        bestDist = sumDist;
                        bestId   = t.ID;
                    }
                }

                // 4.3) 加载并贴图
                OCTET* tmplt = nullptr;
                int hT,wT,tT;
                loadImagette_cou(bestId, tmplt, hT, wT, tT);
                OCTET* resz = nullptr;
                allocation_tableau(resz, OCTET, tailleBloc*tailleBloc*3);
                resize_imagetteCouleur(tmplt, hT, wT, resz, tailleBloc, tailleBloc);

                for (int dy=0; dy<tailleBloc; ++dy) {
                    for (int dx=0; dx<tailleBloc; ++dx) {
                        int dst = ((y+dy)*nW + (x+dx)) * 3;
                        int src = (dy*tailleBloc + dx) * 3;
                        ImgOut[dst+0] = resz[src+0];
                        ImgOut[dst+1] = resz[src+1];
                        ImgOut[dst+2] = resz[src+2];
                    }
                }
                free(tmplt);
                free(resz);
            }
        }

        // 5) 写出 PNG
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
    catch (std::exception &ex) {
        LOGE("color_distribution failed: %s", ex.what());
        return -1;
    }
}

// JNI 调用入口
extern "C"
int main_color_distribution(
        const char* in,
        const char* out,
        int blockSize,
        bool repetition
) {
    // distribution 算法不使用 repetition 标记，但保留参数签名
    return generateColorDistribution(in, out, blockSize, repetition);
}
