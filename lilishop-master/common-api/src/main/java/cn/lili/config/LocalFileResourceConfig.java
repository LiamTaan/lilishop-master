package cn.lili.config;

import com.alibaba.fastjson2.JSON;
import cn.lili.modules.file.entity.enums.OssEnum;
import cn.lili.modules.system.entity.dos.Setting;
import cn.lili.modules.system.entity.dto.OssSetting;
import cn.lili.modules.system.entity.enums.SettingEnum;
import cn.lili.modules.system.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * dev 本地文件存储静态资源映射。
 */
@Configuration
public class LocalFileResourceConfig implements WebMvcConfigurer {

    @Autowired
    private SettingService settingService;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        OssSetting ossSetting = this.resolveOssSetting();
        if (!OssEnum.LOCAL.name().equalsIgnoreCase(ossSetting.getType())) {
            return;
        }

        String urlPrefix = ossSetting.getLocalFileUrlPrefix();
        if (!urlPrefix.startsWith("/")) {
            urlPrefix = "/" + urlPrefix;
        }
        if (!urlPrefix.endsWith("/")) {
            urlPrefix = urlPrefix + "/";
        }

        Path storagePath = Paths.get(ossSetting.getLocalFilePath()).toAbsolutePath().normalize();
        String resourceLocation = storagePath.toUri().toString();
        if (!resourceLocation.endsWith("/")) {
            resourceLocation = resourceLocation + "/";
        }

        registry.addResourceHandler(urlPrefix + "**").addResourceLocations(resourceLocation);
    }

    private OssSetting resolveOssSetting() {
        Setting setting = settingService.get(SettingEnum.OSS_SETTING.name());
        if (setting == null || setting.getSettingValue() == null || setting.getSettingValue().isBlank()) {
            return new OssSetting();
        }
        OssSetting ossSetting = JSON.parseObject(setting.getSettingValue(), OssSetting.class);
        return ossSetting == null ? new OssSetting() : ossSetting;
    }
}
