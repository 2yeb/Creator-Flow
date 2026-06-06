package com.example.creator_flow.network;

import android.content.Context;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit 싱글톤.
 * 시현 모드: 모든 요청에 {@link NetworkConfig#DEV_TOKEN}을 Authorization 헤더로 자동 첨부.
 * TokenManager는 향후 로그인 흐름 추가 시 동적 토큰용으로 보존됨 (현재는 미사용).
 */
public class RetrofitClient {

    private static Retrofit retrofit;

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {
            TokenManager tokenManager = TokenManager.getInstance(context);

            // JWT 토큰 자동 첨부 인터셉터
            // 우선순위: TokenManager에 저장된 토큰 > NetworkConfig.DEV_TOKEN (시현용 기본값)
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        String token = tokenManager.getToken();
                        if (token == null) token = NetworkConfig.DEV_TOKEN;

                        Request request = (token != null && !token.isEmpty())
                                ? original.newBuilder()
                                    .header("Authorization", "Bearer " + token)
                                    .build()
                                : original;

                        return chain.proceed(request);
                    })
                    .addInterceptor(new HttpLoggingInterceptor()
                            .setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(NetworkConfig.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApi(Context context) {
        return getInstance(context).create(ApiService.class);
    }
}
