// image_ppm.h
#ifndef IMAGE_PPM_H
#define IMAGE_PPM_H

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include <string>
#include <iostream>
#include <array>
#include <vector>
#include <stdexcept>
#include <android/log.h>

using namespace std;

#define LOG_TAG "mosaic"
#define LOGE(fmt, ...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##__VA_ARGS__)

// 如果 calloc 失败，则抛出异常
#define allocation_tableau(nom, type, nombre)                                  \
  do {                                                                         \
    (nom) = (type*)calloc((nombre), sizeof(type));                             \
    if (!(nom)) {                                                              \
      LOGE("calloc failed for %s, count=%d", #nom, nombre);                    \
      throw runtime_error(string("calloc failed for ") + #nom);                \
    }                                                                          \
  } while (0)

typedef unsigned char OCTET;

/** 跳过以 ‘#’ 开头的注释行 */
static void ignorer_commentaires(FILE* f) {
    int c;
    while ((c = fgetc(f)) != EOF) {
        if (c == '#') {
            while ((c = fgetc(f)) != EOF && c != '\n') {}
        } else {
            ungetc(c, f);
            break;
        }
    }
}

/** 写二进制 PPM (P6) */
static void ecrire_image_ppm(const char* path,
                             const OCTET* img,
                             int nb_lignes, int nb_colonnes) {
    FILE* f = fopen(path, "wb");
    if (!f) {
        LOGE("Cannot open PPM for writing: %s", path);
        throw runtime_error(string("Cannot open PPM for writing: ") + path);
    }
    fprintf(f, "P6\n%d %d\n255\n", nb_colonnes, nb_lignes);
    int taille = 3 * nb_colonnes * nb_lignes;
    if ((int)fwrite(img, 1, taille, f) != taille) {
        LOGE("Error writing PPM data: %s", path);
        throw runtime_error(string("Error writing PPM data: ") + path);
    }
    fclose(f);
}

/** 读取 PPM 头部，获取尺寸 */
static void lire_nb_lignes_colonnes_image_ppm(const char* path,
                                              int* nb_lignes,
                                              int* nb_colonnes) {
    FILE* f = fopen(path, "rb");
    if (!f) {
        LOGE("Cannot open PPM header: %s", path);
        throw runtime_error(string("Cannot open PPM header: ") + path);
    }
    char line[256];
    if (!fgets(line, sizeof(line), f) || strncmp(line, "P6", 2) != 0) {
        fclose(f);
        throw runtime_error("Bad PPM magic");
    }
    do {
        if (!fgets(line, sizeof(line), f)) {
            fclose(f);
            throw runtime_error("Unexpected EOF reading PPM dims");
        }
    } while (line[0] == '#');
    if (sscanf(line, "%d %d", nb_colonnes, nb_lignes) != 2) {
        fclose(f);
        throw runtime_error("Invalid PPM dims");
    }
    fclose(f);
}

/** 读取 PPM 数据 (P6) */
static void lire_image_ppm(const char* path,
                           OCTET* img,
                           int /*unused*/) {
    FILE* f = fopen(path, "rb");
    if (!f) {
        LOGE("Cannot open PPM data: %s", path);
        throw runtime_error(string("Cannot open PPM data: ") + path);
    }
    char line[256];
    if (!fgets(line, sizeof(line), f) || strncmp(line, "P6", 2) != 0) {
        fclose(f);
        throw runtime_error("Bad PPM magic");
    }
    do {
        if (!fgets(line, sizeof(line), f)) {
            fclose(f);
            throw runtime_error("Unexpected EOF reading PPM dims");
        }
    } while (line[0] == '#');
    int cols, lines;
    if (sscanf(line, "%d %d", &cols, &lines) != 2) {
        fclose(f);
        throw runtime_error("Invalid PPM dims");
    }
    do {
        if (!fgets(line, sizeof(line), f)) {
            fclose(f);
            throw runtime_error("Unexpected EOF reading PPM maxval");
        }
    } while (line[0] == '#');
    int maxval;
    if (sscanf(line, "%d", &maxval) != 1) {
        fclose(f);
        throw runtime_error("Invalid PPM maxval");
    }
    int expected = cols * lines * 3;
    if ((int)fread(img, 1, expected, f) != expected) {
        LOGE("Error reading PPM data: %s", path);
        fclose(f);
        throw runtime_error(string("Error reading PPM data: ") + path);
    }
    fclose(f);
}

/** 写二进制 PGM (P5) */
static void ecrire_image_pgm(const char* path,
                             const OCTET* img,
                             int nb_lignes, int nb_colonnes) {
    FILE* f = fopen(path, "wb");
    if (!f) {
        LOGE("Cannot open PGM for writing: %s", path);
        throw runtime_error(string("Cannot open PGM for writing: ") + path);
    }
    fprintf(f, "P5\n%d %d\n255\n", nb_colonnes, nb_lignes);
    int taille = nb_colonnes * nb_lignes;
    if ((int)fwrite(img, 1, taille, f) != taille) {
        LOGE("Error writing PGM data: %s", path);
        throw runtime_error(string("Error writing PGM data: ") + path);
    }
    fclose(f);
}

/** 读取 PGM 头部，获取尺寸 */
static void lire_nb_lignes_colonnes_image_pgm(const char* path,
                                              int* nb_lignes,
                                              int* nb_colonnes) {
    FILE* f = fopen(path, "rb");
    if (!f) {
        LOGE("Cannot open PGM header: %s", path);
        throw runtime_error(string("Cannot open PGM header: ") + path);
    }
    char line[256];
    if (!fgets(line, sizeof(line), f) || strncmp(line, "P5", 2) != 0) {
        fclose(f);
        throw runtime_error("Bad PGM magic");
    }
    do {
        if (!fgets(line, sizeof(line), f)) {
            fclose(f);
            throw runtime_error("Unexpected EOF reading PGM dims");
        }
    } while (line[0] == '#');
    if (sscanf(line, "%d %d", nb_colonnes, nb_lignes) != 2) {
        fclose(f);
        throw runtime_error("Invalid PGM dims");
    }
    fclose(f);
}

/** 读取 PGM 数据 (P5) */
static void lire_image_pgm(const char* path,
                           OCTET* img,
                           int /*unused*/) {
    FILE* f = fopen(path, "rb");
    if (!f) {
        LOGE("Cannot open PGM data: %s", path);
        throw runtime_error(string("Cannot open PGM data: ") + path);
    }
    char line[256];
    if (!fgets(line, sizeof(line), f) || strncmp(line, "P5", 2) != 0) {
        fclose(f);
        throw runtime_error("Bad PGM magic");
    }
    do {
        if (!fgets(line, sizeof(line), f)) {
            fclose(f);
            throw runtime_error("Unexpected EOF reading PGM dims");
        }
    } while (line[0] == '#');
    int cols, lines;
    if (sscanf(line, "%d %d", &cols, &lines) != 2) {
        fclose(f);
        throw runtime_error("Invalid PGM dims");
    }
    do {
        if (!fgets(line, sizeof(line), f)) {
            fclose(f);
            throw runtime_error("Unexpected EOF reading PGM maxval");
        }
    } while (line[0] == '#');
    int maxval;
    if (sscanf(line, "%d", &maxval) != 1) {
        fclose(f);
        throw runtime_error("Invalid PGM maxval");
    }
    int expected = cols * lines;
    if ((int)fread(img, 1, expected, f) != expected) {
        LOGE("Error reading PGM data: %s", path);
        fclose(f);
        throw runtime_error(string("Error reading PGM data: ") + path);
    }
    fclose(f);
}

/** 提取 R、G、B 平面 */
static void planR(OCTET* dst, const OCTET* src, int n) { for(int i=0;i<n;i++) dst[i]=src[3*i]; }
static void planV(OCTET* dst, const OCTET* src, int n) { for(int i=0;i<n;i++) dst[i]=src[3*i+1]; }
static void planB(OCTET* dst, const OCTET* src, int n) { for(int i=0;i<n;i++) dst[i]=src[3*i+2]; }

/** 均值缩放（灰度） */
static void resize_imagette(const OCTET* in, int hin, int win,
                            OCTET* out, int hout, int wout) {
    int bh = hin/hout, bw = win/wout;
    for(int y=0;y<hout;y++) for(int x=0;x<wout;x++){
            int sum=0,c=0;
            for(int i=0;i<bh;i++) for(int j=0;j<bw;j++){
                    sum += in[(y*bh+i)*win + (x*bw+j)];
                    c++;
                }
            out[y*wout+x] = sum/c;
        }
}

/** 均值缩放（彩色） */
static void resize_imagetteCouleur(const OCTET* in, int hin, int win,
                                   OCTET* out, int hout, int wout) {
    int bh = hin/hout, bw = win/wout;
    for(int y=0;y<hout;y++) for(int x=0;x<wout;x++){
            int sr=0,sg=0,sb=0,c=0;
            for(int i=0;i<bh;i++) for(int j=0;j<bw;j++){
                    int idx=(y*bh+i)*win*3 + (x*bw+j)*3;
                    sr+=in[idx]; sg+=in[idx+1]; sb+=in[idx+2];
                    c++;
                }
            int di=(y*wout+x)*3;
            out[di]=sr/c; out[di+1]=sg/c; out[di+2]=sb/c;
        }
}

/** 从 ./img_tile/<id>.pgm 加载灰度 tile */
static void loadImagette(int id, OCTET*& img, int& h, int& w, int& taille) {
    string p = "./img_tile/" + to_string(id) + ".pgm";
    lire_nb_lignes_colonnes_image_pgm(p.c_str(), &h, &w);
    taille = h*w;
    allocation_tableau(img, OCTET, taille);
    lire_image_pgm(p.c_str(), img, taille);
}

/** 从 ./img_tile_color/i<id>.ppm 加载彩色 tile */
static void loadImagette_cou(int id, OCTET*& img, int& h, int& w, int& taille) {
    string p = "./img_tile_color/i"+to_string(id)+".ppm";
    lire_nb_lignes_colonnes_image_ppm(p.c_str(), &h, &w);
    int t=h*w;
    allocation_tableau(img, OCTET, 3*t);
    lire_image_ppm(p.c_str(), img, t);
}

#endif // IMAGE_PPM_H
