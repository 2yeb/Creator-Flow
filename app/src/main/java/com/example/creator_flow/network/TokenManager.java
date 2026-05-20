package com.example.creator_flow.network;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * JWT 토큰을 SharedPreferences에 저장/조회/삭제하는 싱글톤 클래스.
 * 로그인 성공 후 saveToken()으로 저장하고,
 * 이후 모든 API 요청에서 getToken()으로 꺼내 사용한다.
 */
public class TokenManager {

    private static final String PREF_NAME  = "auth_prefs";
    private static final String KEY_TOKEN  = "access_token";

    private static TokenManager instance;
    private final SharedPreferences prefs;

    private TokenManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static TokenManager getInstance(Context context) {
        if (instance == null) instance = new TokenManager(context);
        return instance;
    }

    /** 로그인 후 받은 access_token 저장 */
    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    /** 저장된 token 반환 (없으면 null) */
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    /** 로그아웃 시 토큰 삭제 */
    public void clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply();
    }

    /** 로그인 상태 여부 */
    public boolean isLoggedIn() {
        return getToken() != null;
    }
}
