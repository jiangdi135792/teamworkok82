package com.work.plugin.imports;

import lombok.NonNull;

import java.util.Map;

/**
 * 验证控制界面
 *
 * @author Masato Morita
 */
interface ValidationController
{
    /**
     *
     * 验证输入值
     *
     * @param filePath 上传文件路径
     * @return 验证结果信息
     * @exception NullPointerException 上传文件路径是空
     */
    Map<Integer, Map<Integer,String>> execute(@NonNull String filePath);
}
