#include <jni.h>
#include <string>
#include <cstring>
#include <android/log.h>
#include <unistd.h>    // for chdir()
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,"mosaic",__VA_ARGS__)

// 声明你三种算法
extern "C" {
int main_average(const char* in, const char* out, int blockSize);
int main_histo(const char* in, const char* out, int blockSize);
int main_distribution(const char* in, const char* out, int blockSize, bool withRepetition);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_motophosaique_MainActivity_generateMosaic(
        JNIEnv* env,
        jobject /* this */,
        jstring jInputPath,
        jstring jOutputPath,
        jint jBlockSize,
        jstring jMode,
        jboolean jWithRep) {

    const char* inPathC  = env->GetStringUTFChars(jInputPath,  nullptr);
    const char* outPathC = env->GetStringUTFChars(jOutputPath, nullptr);
    const char* modeC    = env->GetStringUTFChars(jMode,    nullptr);
    bool withRep         = (jWithRep == JNI_TRUE);

    // —— 新增：切换到 input.pgm 所在目录 ——
    {
        std::string inPath(inPathC);
        auto pos = inPath.find_last_of('/');
        if (pos != std::string::npos) {
            std::string dir = inPath.substr(0, pos);
            if (chdir(dir.c_str()) != 0) {
                LOGE("chdir to %s failed", dir.c_str());
            }
        }
    }

    int result = -1;
    if (strcmp(modeC, "average") == 0) {
        result = main_average(inPathC, outPathC, jBlockSize);
    }
    else if (strcmp(modeC, "histo") == 0) {
        result = main_histo(inPathC, outPathC, jBlockSize);
    }
    else if (strcmp(modeC, "distribution") == 0) {
        result = main_distribution(inPathC, outPathC, jBlockSize, withRep);
    }

    env->ReleaseStringUTFChars(jInputPath,  inPathC);
    env->ReleaseStringUTFChars(jOutputPath, outPathC);
    env->ReleaseStringUTFChars(jMode,       modeC);
    return result;
}