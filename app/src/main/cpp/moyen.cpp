#include <jni.h>
#include <string>
#include <vector>
#include <cmath>
#include <cfloat>
#include <android/log.h>
#include "image_ppm.h"

#define LOG_TAG "mosaic"
#define LOGE(fmt, ...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##__VA_ARGS__)

#define NB_BASE_DE_DONNEE 300

static int generatePhotoMosaic(const char* inputPath,
                               const char* outputPath,
                               int tailleBloc) {
    try {
        // 1) 读取输入 PGM
        {
            // 取出 inputPath 的目录部分
            string inStr(inputPath);
            size_t pos = inStr.find_last_of('/');
            if (pos != string::npos) {
                string dir = inStr.substr(0, pos);
                if (chdir(dir.c_str()) != 0) {
                    LOGE("chdir failed to %s", dir.c_str());
                    throw runtime_error("chdir failed");
                }
            }
        }

        // 1) 读取输入 PGM
        int nH, nW;
        lire_nb_lignes_colonnes_image_pgm(inputPath, &nH, &nW);
        int nTaille = nH * nW;
        OCTET* ImgIn = nullptr;
        allocation_tableau(ImgIn, OCTET, nTaille);
        lire_image_pgm(inputPath, ImgIn, nTaille);

        // 2) 分配输出
        OCTET* ImgOut = nullptr;
        allocation_tableau(ImgOut, OCTET, nTaille);

        // 3) 计算所有 tile 的平均值
        vector<float> moy(NB_BASE_DE_DONNEE + 1);
        for (int id = 1; id <= NB_BASE_DE_DONNEE; ++id) {
            OCTET* tile = nullptr;
            int hT, wT, tT;
            loadImagette(id, tile, hT, wT, tT);
            float sum = 0;
            for (int i = 0; i < tT; ++i) sum += tile[i];
            moy[id] = sum / tT;
            free(tile);
        }

        // 4) 瓦片替换
        for (int y = 0; y <= nH - tailleBloc; y += tailleBloc) {
            for (int x = 0; x <= nW - tailleBloc; x += tailleBloc) {
                // 块平均
                float sumB = 0;
                for (int dy = 0; dy < tailleBloc; ++dy)
                    for (int dx = 0; dx < tailleBloc; ++dx)
                        sumB += ImgIn[(y + dy) * nW + (x + dx)];
                sumB /= (tailleBloc * tailleBloc);

                // 找最接近
                float bestD = FLT_MAX;
                int bestId = 1;
                for (int id = 1; id <= NB_BASE_DE_DONNEE; ++id) {
                    float d = fabs(moy[id] - sumB);
                    if (d < bestD) { bestD = d; bestId = id; }
                }

                // 加载 & 缩放
                OCTET* inT = nullptr;
                int hT, wT, tT;
                loadImagette(bestId, inT, hT, wT, tT);
                OCTET* outT = nullptr;
                allocation_tableau(outT, OCTET, tailleBloc * tailleBloc);
                resize_imagette(inT, hT, wT, outT, tailleBloc, tailleBloc);

                // 写回
                for (int dy = 0; dy < tailleBloc; ++dy)
                    for (int dx = 0; dx < tailleBloc; ++dx)
                        ImgOut[(y + dy) * nW + (x + dx)] =
                                outT[dy * tailleBloc + dx];

                free(inT);
                free(outT);
            }
        }

        // 5) 写输出 PGM
        ecrire_image_pgm(outputPath, ImgOut, nH, nW);

        free(ImgIn);
        free(ImgOut);
        return 0;
    }
    catch (const exception& ex) {
        LOGE("generatePhotoMosaic failed: %s", ex.what());
        return -1;
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_motophosaique_MainActivity_generateMosaic(
        JNIEnv* env, jobject /* this */,
        jstring jInputPath, jstring jOutputPath, jint jBlockSize) {

    const char* inputPath  = env->GetStringUTFChars(jInputPath, nullptr);
    const char* outputPath = env->GetStringUTFChars(jOutputPath, nullptr);

    int res = generatePhotoMosaic(inputPath, outputPath, jBlockSize);

    env->ReleaseStringUTFChars(jInputPath, inputPath);
    env->ReleaseStringUTFChars(jOutputPath, outputPath);
    return res;
}
