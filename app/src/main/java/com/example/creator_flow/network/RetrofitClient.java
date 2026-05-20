package com.example.creator_flow.network;

import android.content.Context;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit 싱글톤.
 * 모든 요청에 Authorization: Bearer {token} 헤더를 자동으로 추가한다.
 */
public class RetrofitClient {

    private static Retrofit retrofit;

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {
            TokenManager tokenManager = TokenManager.getInstance(context);

            // JWT 토큰 자동 첨부 인터셉터
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        String token = tokenManager.getToken();

                        Request request = (token != null)
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
