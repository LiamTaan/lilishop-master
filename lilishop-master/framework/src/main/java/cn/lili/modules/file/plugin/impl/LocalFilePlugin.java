package cn.lili.modules.file.plugin.impl;

import cn.lili.modules.file.entity.enums.OssEnum;
import cn.lili.modules.file.plugin.FilePlugin;
import cn.lili.modules.system.entity.dto.OssSetting;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * 本地文件存储插件，主要用于开发环境上传联调。
 */
public class LocalFilePlugin implements FilePlugin {

    private final OssSetting ossSetting;

    public LocalFilePlugin(OssSetting ossSetting) {
        this.ossSetting = ossSetting;
    }

    @Override
    public OssEnum pluginName() {
        return OssEnum.LOCAL;
    }

    @Override
    public String pathUpload(String filePath, String key) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
            return inputStreamUpload(inputStream, key, Files.probeContentType(Paths.get(filePath)));
        } catch (IOException e) {
            throw new RuntimeException("本地文件上传失败", e);
        }
    }

    @Override
    public String inputStreamUpload(InputStream inputStream, String key, String contentType) {
        Path target = this.resolveTarget(key);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            return this.buildUrl(key);
        } catch (IOException e) {
            throw new RuntimeException("本地文件上传失败", e);
        }
    }

    @Override
    public void deleteFile(List<String> keys) {
        if (keys == null) {
            return;
        }
        for (String key : keys) {
            try {
                Files.deleteIfExists(this.resolveTarget(key));
            } catch (IOException ignored) {
            }
        }
    }

    private Path resolveTarget(String key) {
        return Paths.get(this.ossSetting.getLocalFilePath()).toAbsolutePath().normalize().resolve(key);
    }

    private String buildUrl(String key) {
        String prefix = this.ossSetting.getLocalFileUrlPrefix();
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        return prefix + "/" + key.replace("\\", "/");
    }
}
