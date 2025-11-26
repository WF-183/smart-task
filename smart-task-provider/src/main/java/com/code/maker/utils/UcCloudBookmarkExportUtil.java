package com.code.maker.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * UC云书签导出工具
 *
 * 说明：
 * 1. 该工具用于从UC云书签服务中导出书签，并生成符合鲨鱼浏览器格式的HTML文件。
 * 2. 使用前请确保已获取有效的X-CSRF-TOKEN和Cookie，并替换代码中的相应字段。
 * 3. 该工具会递归处理所有目录和子目录，导出所有书签。
 *
 * 注意事项：
 * - 请勿滥用该工具，以免违反UC云服务的使用条款。
 * - 导出的书签文件请妥善保管，避免泄露个人隐私信息。
 *
 * 实测导出可行 参考nice博客：https://zhuanlan.zhihu.com/p/658796596?utm_psn=1976773131948536663
 *
 * @author DevilMayCry
 */
public class UcCloudBookmarkExportUtil {

    // --- 配置信息 ---
    private static final String HOST = "https://cloud.uc.cn/api/bookmark/listdata";
    private static final String X_CSRF_TOKEN = "xzmSJzsshXJcSZd1fpv67YeQ"; // 替换为你的token
    private static final String COOKIE = "csrfToken=xzmSJzsshXJcSZd1fpv67YeQ; _UP_28A_52_=54; _UP_A4A_11_=wb9cf1999f6c4277bea7f4a6e2b522f9; _UP_D_=pc; _UP_F7E_8D_=SOLOr6H%2B98OjfJ6SguwYW3BZ%2BoezhOy%2FrtOFmrP%2F5Q5wcWnAXRGlxIDsMcOusUQQFD435Ot23OvjYXXua%2FholA28n8jS8UunmecVm%2FRn2dS4GexloG8xl%2BcyJIU4mS68ixz8M%2Bo8ELqPSDIzyPRVXYjKLHiThZj4IyEHeBAPtnvxewvGOJ7QR2lMWS1rXVfbZXRHxH%2Fc5ejTk%2FYYBSlxZ2dk3A9f%2FCMR1wmyyGaOUD3pV4OIH2rmX0VF6jsQF6Mjj3zd%2Fr57GvWdSoxLVcoYHoLcepr%2FpgOMVxJDWZEFuB9OomGCrtfLnabcSYcc6LCvV8heTxgys8xANK4v8a%2BQ2MHioDqQO2Iml2%2B2H3zLzMHfdzjPhIn9KLnVmIzq%2FTTa5GH40CMVkY7o01GI0ATtUO91YJ0UKEIX39siXgbW2M9npW%2F75YhAodfauH%2BIRnbP2kPXVA3ndZjsNGuAsgzm9q7amum4haHWW%2FqRKJrlXaA%3D; st=st9cf620560wayx48dqr1j6702qst2kj; st.sig=ng38ooJFe16I9QAmjs_UGDAsOFCPu7u0n5ud1qzuqAw; token=223dedaa2a29ce2a07dd5d477d8d0f36; token.sig=fwHnnpBMa-f64bcIaMzSwSDzm0K77dHwCWAy9-AO-pI; nick_name=Devil%20May%20Cry; uid=undefined; tfstk=gxvZULDBCtYQy1zICeB44vXvbyBO3tuWSK_fmnxcfNbi6tM2TexD1stcfZReoe_MCfQ68Z-WD-_6fOm2uUYNGE-xXibDoE7X1XMWXhBAn4TqFYtOADj6AI5GSkmcqgnCj0i_uv6An4gQO-ftUtLws7D1nHmFciN0IEfc-DSc0t2GiRV3-wIcntfGiy4hciV0o-ViYHbdmtbDotmFKwIcnZYcnfgPNsuPc1mQHHyECWcV1axG8-2uvifgWhPbF8tGj18vjw5VgpSN_a5VI3spU3xMeiJxqS72cCY1vEkr89vw7dfF3xydC3RH76RZuJ5pt3pcTB3zw3KM7QfkIvoBuexJwOvikR_9iHvcRdmb3aAW29Sph2eGuBADC1BYSVSeQ3J2ggPzkM0sYKdaoSfGvM7SYDuwPsZUT9sScSFA6PSFPcA0MSCG_M7SYDPYM1HdYaiMi; third="; // 替换为你的完整cookie

    // --- 全局状态 ---
    private final OkHttpClient httpClient;
    private final StringBuilder htmlContentBuilder;
    private int bookmarkCount;

    public UcCloudBookmarkExportUtil() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        this.htmlContentBuilder = new StringBuilder();
        this.bookmarkCount = 0;
    }

    /**
     * 主方法，程序入口
     */
    public static void main(String[] args) {
        UcCloudBookmarkExportUtil exporter = new UcCloudBookmarkExportUtil();
        try {
            exporter.exportBookmarks();
            System.out.println("书签导出完成！");
        } catch (Exception e) {
            System.err.println("导出过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 执行整个导出流程
     * @throws IOException
     * @throws InterruptedException
     */
    public void exportBookmarks() throws IOException, InterruptedException {
        writeHtmlHeader();
        // 直接处理根目录下的内容，而不是作为一个文件夹
        processRootDirectory("0");
        writeHtmlFooter();

        try (FileWriter writer = new FileWriter("bookmark" + UUID.randomUUID().toString() + ".html")) {
            writer.write(htmlContentBuilder.toString());
        }

        System.out.println("生成书签总数: " + bookmarkCount);
    }

    /**
     * 处理根目录（guid="0"）下的内容，直接将其添加到HTML中，不创建额外的H3文件夹
     * @param guid 根目录的GUID
     * @throws IOException
     * @throws InterruptedException
     */
    private void processRootDirectory(String guid) throws IOException, InterruptedException {
        int currentPage = 1;
        while (true) {
            System.out.printf("Processing root directory, dir_guid: %s, cur_page: %d%n", guid, currentPage);

            String responseBody = sendPostRequest(guid, currentPage);
            JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();

            if (!"ok".equals(responseJson.get("msg").getAsString())) {
                System.out.printf("i: %d, quit. Message: %s%n", currentPage, responseJson.get("msg").getAsString());
                break;
            }

            JsonObject dataObject = responseJson.getAsJsonObject("data");
            if (dataObject == null) {
                System.out.println("响应中未找到 'data' 字段。");
                break;
            }

            JsonArray listArray = dataObject.getAsJsonArray("list");
            if (listArray == null) {
                System.out.println("当前页没有书签数据。");
                break;
            }

            for (JsonElement element : listArray) {
                JsonObject item = element.getAsJsonObject();
                String itemGuid = item.get("guid").getAsString();
                String name = item.get("name").getAsString();

                if (item.get("is_directory").getAsInt() == 1) {
                    System.out.println("  -> 发现目录: " + name);
                    // 对于根目录下的文件夹，正常创建H3和DL结构
                    htmlContentBuilder.append("\t<DT><H3>").append(escapeHtml(name)).append("</H3>\n");
                    htmlContentBuilder.append("\t<DL><p>\n");
                    // 调用递归方法处理子目录
                    getAndProcessBookmarks(itemGuid);
                    htmlContentBuilder.append("\t</DL><p>\n");
                } else {
                    String url = item.get("origin_url").getAsString();
                    System.out.println("  -> 发现书签: " + name + " -> " + url);
                    // 对于根目录下的书签，直接添加DT和A标签
                    htmlContentBuilder.append("\t<DT><A HREF=\"").append(escapeHtml(url)).append("\">").append(escapeHtml(name)).append("</A>\n");
                    bookmarkCount++;
                }
            }

            JsonObject metaObject = dataObject.getAsJsonObject("meta");
            if (metaObject == null || !metaObject.get("has_last_page").getAsBoolean()) {
                System.out.printf("dir_guid: %s, 本页结束。%n", guid);
                break;
            }

            currentPage++;
            TimeUnit.MILLISECONDS.sleep(100);
        }
    }

    /**
     * 递归获取指定目录的书签，并处理（写入HTML内容）
     * @param guid 目录的GUID
     * @throws IOException
     * @throws InterruptedException
     */
    private void getAndProcessBookmarks(String guid) throws IOException, InterruptedException {
        int currentPage = 1;
        while (true) {
            System.out.printf("dir_guid: %s, cur_page: %d%n", guid, currentPage);

            String responseBody = sendPostRequest(guid, currentPage);
            JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();

            if (!"ok".equals(responseJson.get("msg").getAsString())) {
                System.out.printf("i: %d, quit. Message: %s%n", currentPage, responseJson.get("msg").getAsString());
                break;
            }

            JsonObject dataObject = responseJson.getAsJsonObject("data");
            if (dataObject == null) {
                System.out.println("响应中未找到 'data' 字段。");
                break;
            }

            JsonArray listArray = dataObject.getAsJsonArray("list");
            if (listArray == null) {
                System.out.println("当前页没有书签数据。");
                break;
            }

            for (JsonElement element : listArray) {
                JsonObject item = element.getAsJsonObject();
                String itemGuid = item.get("guid").getAsString();
                String name = item.get("name").getAsString();

                if (item.get("is_directory").getAsInt() == 1) {
                    System.out.println("  -> 发现目录: " + name);
                    htmlContentBuilder.append("\t\t<DT><H3>").append(escapeHtml(name)).append("</H3>\n"); // 子目录增加缩进，更美观
                    htmlContentBuilder.append("\t\t<DL><p>\n");
                    getAndProcessBookmarks(itemGuid);
                    htmlContentBuilder.append("\t\t</DL><p>\n");
                } else {
                    String url = item.get("origin_url").getAsString();
                    System.out.println("  -> 发现书签: " + name + " -> " + url);
                    htmlContentBuilder.append("\t\t<DT><A HREF=\"").append(escapeHtml(url)).append("\">").append(escapeHtml(name)).append("</A>\n"); // 子书签增加缩进
                    bookmarkCount++;
                }
            }

            JsonObject metaObject = dataObject.getAsJsonObject("meta");
            if (metaObject == null || !metaObject.get("has_last_page").getAsBoolean()) {
                System.out.printf("dir_guid: %s, 本页结束。%n", guid);
                break;
            }

            currentPage++;
            TimeUnit.MILLISECONDS.sleep(100);
        }
    }

    /**
     * 使用OkHttp发送POST请求并返回响应体
     * (此方法与之前版本相同，无需修改)
     */
    private String sendPostRequest(String guid, int page) throws IOException {
        RequestBody requestBody = new FormBody.Builder()
                .add("cur_page", String.valueOf(page))
                .add("type", "phone")
                .add("dir_guid", guid)
                .build();

        Request request = new Request.Builder()
                .url(HOST)
                .addHeader("Accept", "application/json, text/plain, */*")
                .addHeader("Accept-Language", "zh-Hans-CN, zh-Hans; q=0.5")
                .addHeader("Connection", "Keep-Alive")
                .addHeader("Origin", "https://cloud.uc.cn")
                .addHeader("Referer", "https://cloud.uc.cn/home/phone")
                .addHeader("Sec-Fetch-Dest", "empty")
                .addHeader("Sec-Fetch-Mode", "cors")
                .addHeader("Sec-Fetch-Site", "same-origin")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36")
                .addHeader("X-CSRF-TOKEN", X_CSRF_TOKEN)
                .addHeader("Cookie", COOKIE)
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response);
            }
            ResponseBody responseBody = response.body();
            return responseBody != null ? responseBody.string() : "";
        }
    }

    /**
     * 写入HTML文件头部
     * (已根据新格式修改)
     */
    private void writeHtmlHeader() {
        htmlContentBuilder.append("<!DOCTYPE NETSCAPE-Bookmark-file-1>\n");
        htmlContentBuilder.append("<HTML>\n");
        htmlContentBuilder.append("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n");
        htmlContentBuilder.append("<Title>鲨鱼浏览器书签</Title>\n");
        htmlContentBuilder.append("<H1>书签</H1>\n");
    }

    /**
     * 写入HTML文件尾部
     * (已根据新格式修改)
     */
    private void writeHtmlFooter() {
        htmlContentBuilder.append("</HTML>\n");
    }

    /**
     * 简单的HTML转义
     * (此方法与之前版本相同，无需修改)
     */
    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#039;");
    }
}
