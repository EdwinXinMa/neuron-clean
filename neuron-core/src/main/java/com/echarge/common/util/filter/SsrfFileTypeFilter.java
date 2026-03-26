package com.echarge.common.util.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import com.echarge.common.exception.NeuronBootException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;

/**
 * @Description: 校验文件敏感后缀
 * @author: Edwin
 * @date: 2026-03-22
 */
@Slf4j
public class SsrfFileTypeFilter {

    /**
     * 允许操作文件类型白名单
     */
    private final static List<String> FILE_TYPE_WHITE_LIST = new ArrayList<>(32);
    /**初始化文件头类型，不够的自行补充*/
    final static HashMap<String, String> FILE_TYPE_MAP = new HashMap<>(8);
    static {
        //图片文件
        FILE_TYPE_WHITE_LIST.add("jpg");
        FILE_TYPE_WHITE_LIST.add("jpeg");
        FILE_TYPE_WHITE_LIST.add("png");
        FILE_TYPE_WHITE_LIST.add("gif");
        FILE_TYPE_WHITE_LIST.add("bmp");
        FILE_TYPE_WHITE_LIST.add("svg");
        FILE_TYPE_WHITE_LIST.add("ico");
        FILE_TYPE_WHITE_LIST.add("heic");

        //文本文件
        FILE_TYPE_WHITE_LIST.add("txt");
        FILE_TYPE_WHITE_LIST.add("doc");
        FILE_TYPE_WHITE_LIST.add("docx");
        FILE_TYPE_WHITE_LIST.add("pdf");
        FILE_TYPE_WHITE_LIST.add("csv");
        FILE_TYPE_WHITE_LIST.add("md");

        //音视频文件
        FILE_TYPE_WHITE_LIST.add("mp4");
        FILE_TYPE_WHITE_LIST.add("avi");
        FILE_TYPE_WHITE_LIST.add("mov");
        FILE_TYPE_WHITE_LIST.add("wmv");
        FILE_TYPE_WHITE_LIST.add("mp3");
        FILE_TYPE_WHITE_LIST.add("wav");

        //表格文件
        FILE_TYPE_WHITE_LIST.add("xls");
        FILE_TYPE_WHITE_LIST.add("xlsx");

        //压缩文件
        FILE_TYPE_WHITE_LIST.add("zip");
        FILE_TYPE_WHITE_LIST.add("rar");
        FILE_TYPE_WHITE_LIST.add("7z");
        FILE_TYPE_WHITE_LIST.add("tar");

        //固件文件
        FILE_TYPE_WHITE_LIST.add("bin");
        FILE_TYPE_WHITE_LIST.add("hex");
        FILE_TYPE_WHITE_LIST.add("fw");

        //app文件后缀
        FILE_TYPE_WHITE_LIST.add("apk");
        FILE_TYPE_WHITE_LIST.add("wgt");

        //幻灯片文件后缀
        FILE_TYPE_WHITE_LIST.add("ppt");
        FILE_TYPE_WHITE_LIST.add("pptx");

        //设置禁止文件的头部标记
        FILE_TYPE_MAP.put("3c25402070616765206c", "jsp");
        FILE_TYPE_MAP.put("3c3f7068700a0a2f2a2a0a202a205048", "php");
        FILE_TYPE_MAP.put("cafebabe0000002e0041", "class");
        FILE_TYPE_MAP.put("494e5345525420494e54", "sql");
    }

    /**
     * @param fileName
     * @return String
     * @description 通过文件后缀名获取文件类型
     */
    private static String getFileTypeBySuffix(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }


    /**
     * 下载文件类型过滤
     *
     * @param filePath
     */
    public static void checkDownloadFileType(String filePath) throws IOException {
        //文件后缀
        String suffix = getFileTypeBySuffix(filePath);
        log.debug(" 【文件下载校验】文件后缀 suffix: {}", suffix);
        boolean isAllowExtension = FILE_TYPE_WHITE_LIST.contains(suffix.toLowerCase());
        //是否允许下载的文件
        if (!isAllowExtension) {
            throw new NeuronBootException("下载失败，存在非法文件类型：" + suffix);
        }
    }

    /**
     * 上传文件类型过滤
     *
     * @param file
     */
    public static void checkUploadFileType(MultipartFile file) throws Exception {
        checkUploadFileType(file, null);
    }
    
    /**
     * 上传文件类型过滤
     *
     * @param file
     */
    public static void checkUploadFileType(MultipartFile file, String customPath) throws Exception {
        //1. 路径安全校验
        validatePathSecurity(customPath);
        //2. 校验文件后缀和头
        String suffix = getFileType(file, customPath);
        log.info("【文件上传校验】文件后缀 suffix: {}，customPath：{}", suffix, customPath);
        boolean isAllowExtension = FILE_TYPE_WHITE_LIST.contains(suffix.toLowerCase());
        //是否允许下载的文件
        if (!isAllowExtension) {
            throw new NeuronBootException("上传失败，存在非法文件类型：" + suffix);
        }
    }

    /**
     * 通过读取文件头部获得文件类型
     *
     * @param file
     * @return 文件类型
     * @throws Exception
     */

    private static String getFileType(MultipartFile file, String customPath) throws Exception {
        // 代码逻辑说明: [issue/4672]方法造成的文件被占用，注释掉此方法tomcat就能自动清理掉临时文件
        String fileExtendName = null;
        InputStream is = null;
        try {
            is = file.getInputStream();
            byte[] b = new byte[10];
            is.read(b, 0, b.length);
            String fileTypeHex = String.valueOf(bytesToHexString(b));
            String hexPrefix = fileTypeHex.toLowerCase().substring(0, 5);
            for (Map.Entry<String, String> entry : FILE_TYPE_MAP.entrySet()) {
                String key = entry.getKey().toLowerCase();
                // 验证前5个字符比较
                if (key.startsWith(hexPrefix) || hexPrefix.startsWith(key)) {
                    fileExtendName = entry.getValue();
                    break;
                }
            }
            log.debug("-----获取到的指定文件类型------"+fileExtendName);
            // 如果不是上述类型，则判断扩展名
            if (StringUtils.isBlank(fileExtendName)) {
                String fileName = file.getOriginalFilename();
                // 如果无扩展名，则直接返回空串
                if (-1 == fileName.indexOf(".")) {
                    return "";
                }
                // 如果有扩展名，则返回扩展名
                return getFileTypeBySuffix(fileName);
            }
            is.close();
            return fileExtendName;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "";
        }finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * 获得文件头部字符串
     *
     * @param src
     * @return
     */
    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 路径安全校验
     */
    private static void validatePathSecurity(String customPath) throws NeuronBootException {
        if (customPath == null || customPath.trim().isEmpty()) {
            return;
        }

        // 统一分隔符为 /
        String normalized = customPath.replace("\\", "/");

        // 1. 防止路径遍历攻击
        if (normalized.contains("..") || normalized.contains("~")) {
            throw new NeuronBootException("上传业务路径包含非法字符！");
        }

        // 2. 限制路径深度
        int depth = normalized.split("/").length;
        if (depth > 5) {
            throw new NeuronBootException("上传业务路径深度超出限制！");
        }

        // 3. 限制字符集（只允许字母、数字、下划线、横线、斜杠）
        if (!normalized.matches("^[a-zA-Z0-9/_-]+$")) {
            throw new NeuronBootException("上传业务路径包含非法字符！");
        }
    }

    /**
     * 校验文件路径安全性，防止路径遍历攻击
     * @param filePath 文件路径
     */
    public static void checkPathTraversal(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return;
        }
        // 1. 防止路径遍历：不允许 ..
        if (filePath.contains("..")) {
            throw new NeuronBootException("文件路径包含非法字符");
        }
        // 2. 防止URL编码绕过：%2e = .
        String fileLower = filePath.toLowerCase();
        if (fileLower.contains("%2e")) {
            throw new NeuronBootException("文件路径包含非法字符");
        }
    }

    /**
     * 批量校验文件路径安全性（逗号分隔的多个文件路径）
     * @param files 逗号分隔的文件路径
     */
    public static void checkPathTraversalBatch(String files) {
        if (StringUtils.isBlank(files)) {
            return;
        }
        for (String file : files.split(",")) {
            if (StringUtils.isNotBlank(file)) {
                checkPathTraversal(file.trim());
            }
        }
    }
    
}
