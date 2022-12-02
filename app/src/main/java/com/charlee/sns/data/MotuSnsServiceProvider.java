package com.charlee.sns.data;

import android.os.Build;

import com.charlee.sns.manager.ISnsNetworkParams;
import com.charlee.sns.storage.IStorage;
import com.facebook.animated.gif.BuildConfig;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * 创建MotuSnsService实例。设置全局HTTP参数和JSON转换规则。
 */
public class MotuSnsServiceProvider {
    // HTTP标准头，以及不会随客户端、服务器版本有变化的常量定义
    private static final String HTTP_HEADER_USER_AGENT = "User-Agent";
    private static final String HTTP_HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    private static final String HTTP_HEADER_COOKIE = "Cookie";
    private static final String HTTP_HEADER_HOST = "Host";

    private static final String HTTP_HEADER_ACCEPT = "Accept";
    private static final String ACCEPT = "application/json";

    public static MotuSnsService createMotuSnsService(final IStorage storage, final ISnsNetworkParams networkParams) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.cookieJar(new CookieJar() {
            List<Cookie> cookieStore = new ArrayList<>();
            Cookie cookieItem = null;

            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                cookieStore = new ArrayList<>(cookies);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                if (cookieStore == null) {
                    return null;
                }

                if (cookieItem == null) {
                    cookieItem = createNonPersistentCookie();
                } else {
                    String cookieContent = storage.getLoginCookie();
                    if (cookieContent != null) {
                        if (cookieItem.value().equals(cookieContent) == false) {
                            cookieItem = createNonPersistentCookie();
                        }
                    }
                }

                if (cookieItem != null && cookieStore.contains(cookieItem) == false) {
                    cookieStore.clear();
                    cookieStore.add(cookieItem);
                }

                return cookieStore;
            }

            public Cookie createNonPersistentCookie() {
                String cookieContent = storage.getLoginCookie();
                if (cookieContent == null) {
                    return null;
                }
                return new Cookie.Builder()
                        .domain(getDomain(networkParams.getQAMode()))
                        .path("/")
                        .name("MTSNS_SID")
                        .value(cookieContent.substring(cookieContent.indexOf("=") + 1))
                        .httpOnly()
                        .secure()
                        .build();
            }

        });

        final String appVersion = Constants.MTSNS_APP_VERSION_PREFIX + networkParams.getVersion(null);
        builder.addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Interceptor.Chain chain) throws IOException {
                        if (networkParams == null) {
                            return chain.proceed(chain.request());
                        }

                        // 运行中可能会变化的值
                        String languageTag = Locale.getDefault().toString().replace('_', '-'); // RFC5646
                        String languageRes = networkParams.getLanguage(null);
                        Request original = chain.request();
                        Request.Builder builder = original.newBuilder()
                                .header(HTTP_HEADER_USER_AGENT, Constants.USER_AGENT)
                                .header(HTTP_HEADER_ACCEPT, ACCEPT)
                                .header(Constants.HEADER_MTSNS_APP_VERSION, appVersion)
                                .header(Constants.HEADER_MTSNS_CHANNEL, networkParams.getChannel(null))
                                .header(Constants.HEADER_MTSNS_SDK_VERSION, String.valueOf(Build.VERSION.SDK_INT))
                                .header(Constants.HEADER_MTSNS_ACCEPT_SERVER, Constants.MTSNS_ACCEPT_SERVER)
                                .header(Constants.HEADER_MTSNS_IMEI, networkParams.getIMEI())
                                .header(Constants.HEADER_MTSNS_MTJ_CUID, networkParams.getCuid(null))
                                .header(Constants.HEADER_MTSNS_RES_LANG, languageRes)
                                .header(HTTP_HEADER_ACCEPT_LANGUAGE, languageTag)
                                .method(original.method(), original.body());

                        Request request = builder.build();
                        try {
                            Response response = chain.proceed(request);
                            return response;
                        } catch (IOException ex) {
                            throw ex;
                        }
                    }
                });

        OkHttpClient httpClient = builder
                .writeTimeout(Constants.IMAGE_UPLOAD_TIMEOUT, TimeUnit.SECONDS)
                .build();

        // 设置GSON转换规则
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getServiceUrl(networkParams.getQAMode()))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient)
                .build();

        return retrofit.create(MotuSnsService.class);
    }

    private static String getServiceUrl(boolean qaMode) {
        if (qaMode) {
            return Constants.SERVICE_QA_ENDPOINT;
        }

        boolean isGooglePlayChannel = BuildConfig.FLAVOR.equals("googleplay");
        return isGooglePlayChannel ? Constants.GP_SERVICE_ENDPOINT : Constants.CN_SERVICE_ENDPOINT;
    }

    private static String getDomain(boolean qaMode) {
        if (qaMode) {
            return Constants.SERVICE_QA_ENDPOINT_DOMAIN;
        }

        boolean isGooglePlayChannel = BuildConfig.FLAVOR.equals("googleplay");
        return isGooglePlayChannel ? Constants.GP_SERVICE_ENDPOINT_DOMAIN : Constants.CN_SERVICE_ENDPOINT_DOMAIN;
    }

}
