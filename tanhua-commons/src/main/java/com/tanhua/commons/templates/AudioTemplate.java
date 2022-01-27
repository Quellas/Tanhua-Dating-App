package com.tanhua.commons.templates;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.green.model.v20180509.VoiceSyncScanRequest;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.http.HttpResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.tanhua.commons.properties.AudioProperties;

import java.io.UnsupportedEncodingException;
import java.util.*;

public class AudioTemplate {


    private AudioProperties audioProperties;

    public AudioTemplate(AudioProperties audioProperties) {
        this.audioProperties = audioProperties;
    }

    public boolean audioExamine(String audioUrl) throws UnsupportedEncodingException, ClientException {
        // 请替换成您的AccessKey ID、AccessKey Secret。
        IClientProfile profile = DefaultProfile.getProfile("cn-shanghai", audioProperties.getAccessKeyID(), audioProperties.getAccessKeyecret());
        final IAcsClient client = new DefaultAcsClient(profile);

        VoiceSyncScanRequest asyncScanRequest = new VoiceSyncScanRequest();
        asyncScanRequest.setAcceptFormat(FormatType.JSON); // 指定API返回格式。
        asyncScanRequest.setMethod(com.aliyuncs.http.MethodType.POST); // 指定请求方法。
        asyncScanRequest.setRegionId("cn-shanghai");
        asyncScanRequest.setConnectTimeout(3000);
        // 由于同步语音检测比较耗时，因此建议将超时时间设置在15秒以上。
        asyncScanRequest.setReadTimeout(15000);

        List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
        Map<String, Object> task1 = new LinkedHashMap<String, Object>();
        // 请将下面的地址修改为要检测的语音文件的地址。
        task1.put("url", audioUrl);
        tasks.add(task1);
        JSONObject data = new JSONObject();

        System.out.println("==========Task count:" + tasks.size());
        data.put("scenes", Arrays.asList("antispam"));
        data.put("tasks", tasks);
        asyncScanRequest.setHttpContent(data.toJSONString().getBytes("UTF-8"), "UTF-8", FormatType.JSON);
        System.out.println(JSON.toJSONString(data, true));

        boolean flag = false;
        HttpResponse httpResponse = client.doAction(asyncScanRequest);

        if (httpResponse.isSuccess()) {
            JSONObject scrResponse = JSON.parseObject(new String(httpResponse.getHttpContent(), "UTF-8"));
            System.out.println(JSON.toJSONString(scrResponse, true));
            if (200 == scrResponse.getInteger("code")) {
                JSONArray taskResults = scrResponse.getJSONArray("data");
                for (Object taskResult : taskResults) {
                    Integer code = ((JSONObject) taskResult).getInteger("code");
                    if (200 == code) {
                        System.out.println("task process success, result:" + JSON.toJSONString(taskResult));
                        flag = true;
                    } else {
                        System.out.println("task process fail: " + JSON.toJSONString(taskResult));
                    }
                }
            } else {
                //没有成功
                System.out.println("detect not success. code: " + scrResponse.getInteger("code"));

            }
        } else {
            //响应失败
            System.out.println("response fail:" + new String(httpResponse.getHttpContent(), "UTF-8"));
        }
        return flag;
    }
}
