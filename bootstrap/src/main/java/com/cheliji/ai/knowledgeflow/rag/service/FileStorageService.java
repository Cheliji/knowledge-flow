package com.cheliji.ai.knowledgeflow.rag.service;

import com.cheliji.ai.knowledgeflow.rag.dto.StoredFileDTO;
import com.esotericsoftware.kryo.io.Input;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileStorageService {

    /**
     * 上传文件
     */
    StoredFileDTO upload(String bucketName, MultipartFile file) ;


    /**
     * 上传文件
     */
    StoredFileDTO upload(String bucketName, InputStream content,long size,String originalFilename,String contentType) ;


    /**
     * 上传文件
     */
    StoredFileDTO upload(String bucketName,byte[] content,String originalFilename,String contentType) ;


    /**
     * 上传文件（SDK 原生，带自动重试）
     */
    StoredFileDTO reliableUpload(String bucketName,InputStream content,long size,String originalFilename,String contentType) ;


    /**
     * 下载文件
     */
    InputStream openStream(String url) ;


    void deleteByUrl(String url) ;
}
