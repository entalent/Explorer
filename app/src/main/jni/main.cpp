#include "jni.h"
#include <string.h>
#include <stdio.h>

#ifdef __cplusplus
extern "C" {
#endif

char *jstringToCharSequence(JNIEnv *env, jstring jstr);

const int BUF_SIZE = 4096;
unsigned char buffer[BUF_SIZE];

JNIEXPORT jint JNICALL Java_cn_edu_bit_cs_explorer_util_FileUtil_copyFile
        (JNIEnv * env, jobject obj, jstring jstring1, jstring jstring2){

    char *srcFileName = jstringToCharSequence(env, jstring1);
    char *dstFileName = jstringToCharSequence(env, jstring2);


    int c;
    FILE *fpSrc, *fpDest;  //定义两个指向文件的指针
    fpSrc = fopen(srcFileName, "rb");    //以读取二进制的方式打开源文件
    if(fpSrc==NULL){
        printf( "Source file open failure.");  //源文件不存在的时候提示错误
        return 0;
    }
    fpDest = fopen(dstFileName, "wb");  // //以写入二进制的方式打开目标文件
    if(fpDest==NULL){
        printf("Destination file open failure.");
        return 0;
    }
    while((c=fgetc(fpSrc))!=EOF){   //从源文件中读取数据知道结尾
        fputc(c, fpDest);
    }
    fclose(fpSrc);  //关闭文件指针，释放内存
    fclose(fpDest);

    free(srcFileName);
    free(dstFileName);

    return 1;

}

char *jstringToCharSequence(JNIEnv *env, jstring jstr) {
    char* rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("utf-8");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr= (jbyteArray)env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0)
    {
        rtn = (char*)malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}

#ifdef __cplusplus
}
#endif