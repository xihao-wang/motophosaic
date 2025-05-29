#include <jni.h>
#include <string>
#include <cstring>
#include <android/log.h>
#include <unistd.h>    // for chdir()
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,"mosaic",__VA_ARGS__)


extern "C" {
int main_average(const char* in, const char* out, int blockSize);
int main_histo(const char* in, const char* out, int blockSize);
int main_distribution(const char* in, const char* out, int blockSize, bool withRepetition);
int main_color_average(const char* in, const char* out, int blockSize, bool withRepetition);
int main_color_histo(const char* in, const char* out, int blockSize, bool withRepetition);
int main_color_distribution(const char* in, const char* out, int blockSize, bool withRepetition);
int main_cheat_color(const char* in, const char* out, int blockSize);
int main_cheat_grey(const char* in, const char* out, int blockSize);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_motophosaique_MainActivity_generateMosaic(
        JNIEnv* env,
        jobject /* this */,
        jstring jInputPath,
        jstring jOutputPath,
        jint jBlockSize,
        jstring jMode,
        jstring  jAlgo,
        jboolean jWithRep) {

    const char* inPathC  = env->GetStringUTFChars(jInputPath,  nullptr);
    const char* outPathC = env->GetStringUTFChars(jOutputPath, nullptr);
    const char* modeC    = env->GetStringUTFChars(jMode,    nullptr);
    const char* algo  = env->GetStringUTFChars(jAlgo,  nullptr);
    bool withRep         = (jWithRep == JNI_TRUE);

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

    if (strcmp(modeC, "grey")==0) {
        if      (strcmp(algo,"average")==0)      result = main_average(inPathC, outPathC, jBlockSize);
        else if (strcmp(algo,"histo")==0)        result = main_histo(inPathC, outPathC, jBlockSize);
        else if (strcmp(algo,"distribution")==0) result = main_distribution(inPathC, outPathC, jBlockSize, withRep);
    }
    else if (strcmp(modeC, "color")==0) {
        // e.g. algo == "color_average", "color_histo"...
        if      (strcmp(algo,"color_average")==0)      result = main_color_average(inPathC, outPathC, jBlockSize, withRep);
        else if (strcmp(algo,"color_histo")==0)        result = main_color_histo(inPathC, outPathC, jBlockSize, withRep);
        else if (strcmp(algo,"color_distribution")==0) result = main_color_distribution(inPathC, outPathC, jBlockSize, withRep);
    }
    else if (strcmp(modeC, "object")==0) {
        if      (strcmp(algo,"cheat_grey")==0)
            result = main_cheat_grey(inPathC, outPathC, jBlockSize);
        else if (strcmp(algo,"cheat_color")==0)
            result = main_cheat_color(inPathC, outPathC, jBlockSize);
    }
    env->ReleaseStringUTFChars(jInputPath,  inPathC);
    env->ReleaseStringUTFChars(jOutputPath, outPathC);
    env->ReleaseStringUTFChars(jMode,       modeC);
    env->ReleaseStringUTFChars(jAlgo, algo);
    return result;
}