package com.work.plugin.imports.data;

import lombok.NonNull;

import java.util.Map;

/**
 * 导入数据处理界面
 *
 * @author Masato Morita
 */
interface DataProcess
{
    /**
     *
     * 实行数据处理
     *
     * @param filePath 上传文件路径
     * @param rows 数据行数
     * @return 验证结果信息
     * @exception NullPointerException 上传文件路径或数据行数是空
     */
    Map<Integer, String> execute(@NonNull String filePath, @NonNull Integer[] rows);
}
