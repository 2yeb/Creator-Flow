package com.example.creator_flow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import com.example.creator_flow.databinding.ActivityMainBinding;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    /**
     * Activity 컨텍스트 생성 시 한국어 locale로 wrap.
     * → MaterialDatePicker 등 시스템 위젯이 한국어로 표시됨 (디바이스 locale 무관).
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        Locale locale = Locale.KOREAN;
        Locale.setDefault(locale);
        Configuration config = new Configuration(newBase.getResources().getConfiguration());
        config.setLocale(locale);
        super.attachBaseContext(newBase.createConfigurationContext(config));
    }

    ActivityMainBinding binding;
    // 네비게이션 바에 연결할 프래그먼트들 선언
    HomeFragment homeFragment = new HomeFragment();
    ProjectFragment projectFragment = new ProjectFragment();
    SimulationFragment simulationFragment = new SimulationFragment();
    MypageFragment mypageFragment = new MypageFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 콘텐츠가 status bar 영역까지 그려지도록 (edge-to-edge)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 앱 실행시 첫 화면으로 homefragement 사용
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, homeFragment).commit();

        bottomNavigationView();
    }

    private void bottomNavigationView() {   // 하단 네비게이션 클릭시 작동
        binding.bottomNavigation.setOnItemSelectedListener(menuItem -> {
            if (menuItem.getItemId()==R.id.nav_home){
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment,homeFragment).commit();}
            else if (menuItem.getItemId()==R.id.nav_project){
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment,projectFragment).commit();}
            else if (menuItem.getItemId()==R.id.nav_simulation){
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment,simulationFragment).commit();}
            else if (menuItem.getItemId()==R.id.nav_mypage){
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment,mypageFragment).commit();}
            return true;
        });

    }

}

