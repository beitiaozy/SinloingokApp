package com.sinloingok.app.controllers.oss;

import com.aliyun.oss.OSSClient;
import com.sinloingok.app.controllers.base.BaseController;
import com.sinloingok.app.dtos.StandardRtnDto;
import com.sinloingok.app.util.NextCodeUtils;
import com.sinloingok.app.util.PropertiesUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

/**
 * 阿里云OSS系统
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 * <p>
 * 2017年5月18日,下午3:25:49
 */
@RestController
@RequestMapping("aliYunOss")
public class AliYunOssController extends BaseController {

    @RequestMapping("upload")
    @ResponseBody
    public StandardRtnDto<?> upload(@RequestParam("file") MultipartFile file,
                                     RedirectAttributes redirectAttributes) throws IOException {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "请选择要上传的文件");
            return error("redirect:/uploadStatus");
        }
        // endpoint以杭州为例，其它region请按实际情况填写
        String endpoint = PropertiesUtils.getProperties().getProperty("ALIOSS_ENDPOINT");

        // accessKey请登录https://ak-console.aliyun.com/#/查看
        String accessKeyId = PropertiesUtils.getProperties().getProperty("ALIOSS_ACCESS_KEY_ID");
        String accessKeySecret = PropertiesUtils.getProperties().getProperty("ALIOSS_ACCESS_KEY_SECRET");

        // BucketNam
        String BucketNam = PropertiesUtils.getProperties().getProperty("ALIOSS_BUCKET_NAM");

        // Key(文件名)
        String key = String.valueOf(NextCodeUtils.next());

        // 文件流

        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);

        // 上传文件
        ossClient.putObject(BucketNam, key, file.getInputStream());

        // 关闭client
        ossClient.shutdown();

        // 获取Url
        StringBuffer sb = new StringBuffer();
        sb.append("https://");
        sb.append(BucketNam);
        sb.append(".");
        sb.append(endpoint);
        sb.append("/");
        sb.append(key);

        return success(sb.toString());
    }
}
