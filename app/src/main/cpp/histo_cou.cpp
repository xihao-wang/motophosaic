#include "third_party/stb/stb_image_write.h"

#include <vector>
#include <cmath>
#include <cfloat>
#include <android/log.h>
#include "image_ppm.h"

#define LOG_TAG "mosaic_color_histo"
#define LOGE(fmt, ...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##__VA_ARGS__)

// 彩色库大小，根据实际资产数量调整
#define NB_BASE_DE_DONNEE 300

// 计算彩色直方图马赛克（Bhattacharyya 距离）
int generateColorHisto(
        const char* inputPath,
        const char* outputPath,
        int  tailleBloc,
        bool repetition   // 暂时在此算法里没用，但保留签名
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

        // 3) 预加载每个 tile 的 3×256 直方图
        struct Tile { int ID; std::vector<std::array<int,3>> histo; bool isUsed; };
        std::vector<Tile> tiles;
        tiles.reserve(NB_BASE_DE_DONNEE);
        for (int id=1; id<=NB_BASE_DE_DONNEE; ++id) {
            // load tile
            OCTET* tmplt = nullptr;
            int hT,wT,tT;
            loadImagette_cou(id, tmplt, hT, wT, tT);
            // resize
            OCTET* resz = nullptr;
            allocation_tableau(resz, OCTET, tailleBloc*tailleBloc*3);
            resize_imagetteCouleur(tmplt, hT, wT, resz, tailleBloc, tailleBloc);

            // compute histo
            std::vector<std::array<int,3>> histo(256);
            for (int i=0; i<tailleBloc*tailleBloc*3; i+=3) {
                ++histo[ (unsigned char)resz[i+0] ][0];
                ++histo[ (unsigned char)resz[i+1] ][1];
                ++histo[ (unsigned char)resz[i+2] ][2];
            }
            tiles.push_back({id, std::move(histo), false});

            free(tmplt);
            free(resz);
        }

        // 4) 对输入图逐块，选最优 tile
        for (int y=0; y<=nH-tailleBloc; y+=tailleBloc) {
            for (int x=0; x<=nW-tailleBloc; x+=tailleBloc) {
                // 4.1) 计算当前块直方图
                std::vector<std::array<int,3>> histoBlock(256);
                int base = y*stride3 + x*3;
                for (int dy=0; dy<tailleBloc; ++dy) {
                    int rowOff = base + dy*stride3;
                    for (int dx=0; dx<tailleBloc; ++dx) {
                        int idx = rowOff + dx*3;
                        ++histoBlock[(unsigned char)ImgIn[idx+0]][0];
                        ++histoBlock[(unsigned char)ImgIn[idx+1]][1];
                        ++histoBlock[(unsigned char)ImgIn[idx+2]][2];
                    }
                }

                // 4.2) Bhattacharyya 距离选最小
                float bestDist = FLT_MAX;
                int   bestId   = 1;
                for (auto &t : tiles) {
                    if (!repetition && t.isUsed) continue;
                    // 三个通道分别算
                    float sumLog = 0.f;
                    for (int c=0; c<3; ++c) {
                        float coeff=0.f;
                        for (int v=0; v<256; ++v) {
                            coeff += std::sqrt(
                                    histoBlock[v][c] * (float)t.histo[v][c]
                            );
                        }
                        // 防 zero
                        coeff = std::max(coeff, 1e-6f);
                        sumLog += -std::log(coeff);
                    }
                    sumLog /= 3.f;
                    if (sumLog < bestDist) {
                        bestDist = sumLog;
                        bestId   = t.ID;
                    }
                }
                // 标记已用
                if (!repetition) {
                    for (auto &t : tiles) {
                        if (t.ID == bestId) { t.isUsed = true; break; }
                    }
                }

                // 4.3) 加载并贴图
                OCTET* tmplt = nullptr;
                int hT,wT,tT;
                loadImagette_cou(bestId, tmplt, hT, wT, tT);
                OCTET* resz = nullptr;
                allocation_tableau(resz, OCTET, tailleBloc*tailleBloc*3);
                resize_imagetteCouleur(tmplt, hT, wT, resz, tailleBloc, tailleBloc);

                int dstBase = y*stride3 + x*3;
                for (int dy=0; dy<tailleBloc; ++dy) {
                    int dstRow = dstBase + dy*stride3;
                    int srcRow = dy*tailleBloc*3;
                    memcpy(
                            ImgOut + dstRow,
                            resz    + srcRow,
                            tailleBloc*3
                    );
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
        LOGE("color_histo failed: %s", ex.what());
        return -1;
    }
}

// JNI 接口
extern "C"
int main_color_histo(
        const char* in,
        const char* out,
        int blockSize,
        bool repetition
) {
    return generateColorHisto(in, out, blockSize, repetition);
}
