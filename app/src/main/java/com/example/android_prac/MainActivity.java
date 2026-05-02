package com.example.android_prac;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.android_prac.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    // 네비게이션 바에 연결할 프래그먼트들 선언
    HomeFragment homeFragment = new HomeFragment();
    ProjectFragment projectFragment = new ProjectFragment();
    SimulationFragment simulationFragment = new SimulationFragment();
    MypageFragment mypageFragment = new MypageFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

