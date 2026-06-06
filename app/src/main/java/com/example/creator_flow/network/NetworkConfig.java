package com.example.creator_flow.network;

public class NetworkConfig {
    /**
     * 서버 기본 URL.
     *
     * 환경별 설정 (필요 시 주석 해제):
     *   - 에뮬레이터(AVD)에서 로컬 백엔드: http://10.0.2.2:8000/
     *   - 실기기 + adb reverse (USB): http://localhost:8000/  (먼저 `adb reverse tcp:8000 tcp:8000`)
     *   - 실기기 + 같은 Wi-Fi: http://<PC_IP>:8000/   (예: 192.168.0.10:8000)
     *   - 배포 서버 (HTTPS): https://your-domain.com/
     *
     * HTTP 사용 시 AndroidManifest.xml에 usesCleartextTraffic="true" 필요.
     */
    public static final String BASE_URL = "https://student-disk-shank.ngrok-free.dev";
}
