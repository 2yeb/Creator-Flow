package com.example.creator_flow.network;

public class NetworkConfig {
    /**
     * 서버 기본 URL.
     *
     * 환경별 설정 (필요 시 교체):
     *   - 백엔드 ngrok 터널 (현재): https://student-disk-shank.ngrok-free.dev/
     *   - 에뮬레이터(AVD)에서 로컬 백엔드: http://10.0.2.2:8000/
     *   - 실기기 + adb reverse (USB): http://localhost:8000/  (먼저 `adb reverse tcp:8000 tcp:8000`)
     *   - 실기기 + 같은 Wi-Fi: http://<PC_IP>:8000/   (예: 192.168.0.10:8000)
     *   - 배포 서버 (HTTPS): https://your-domain.com/
     *
     * HTTP 사용 시 AndroidManifest.xml에 usesCleartextTraffic="true" 필요 (HTTPS는 무관).
     */
    public static final String BASE_URL = "https://student-disk-shank.ngrok-free.dev/";

    /**
     * 시현용 영구 dev JWT 토큰. 백엔드 개발자가 발급 (test@gmail.com 유저).
     * 만료: 1781088673 (Unix epoch)
     * 사용처: RetrofitClient의 인터셉터가 모든 요청에 Authorization 헤더로 자동 첨부.
     *
     * TODO 진짜 배포 시:
     *   - 로그인 흐름 추가 (POST /auth/login)
     *   - TokenManager.saveToken()으로 동적 저장
     *   - 이 상수는 삭제
     */
    public static final String DEV_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
            "eyJzdWIiOiI3ODZkNTQ1MS1mMDk5LTRlZmUtOTc3OC1jNGFlYWMwYmIwYWIiLCJleHAiOjE3ODEwODg2NzN9." +
            "N-UDxE6EUBnipQ74PHSvfxuFsYg2-YsUSwXY59_A7Hw";
}
