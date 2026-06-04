package com.example.creator_flow;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SimulationBusinessFragment extends Fragment {

    // 앞 화면들로부터 전달받은 모델 타입을 보관할 변수
    private String modelType = "Business";

    // XML 내부의 4개 입력창 및 다음 버튼 변수
    private EditText netProfitEdit;       // 희망 순이익
    private EditText marginEdit;       // 마진율
    private EditText salesVolumEdit;  // 목표 판매량
    private EditText MinStocklevelEdit;     // 최소 재고량
    private ImageView nextButton;

    public SimulationBusinessFragment() {
        // Required empty public constructor
    }

    /**
     * 앞선 프래그먼트들로부터 modelType을 안전하게 넘겨받기 위한 팩토리 메서드
     */
    public static SimulationBusinessFragment newInstance(String modelType) {
        SimulationBusinessFragment fragment = new SimulationBusinessFragment();
        Bundle args = new Bundle();
        args.putString("model_type", modelType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 번들에 저장된 modelType 꺼내기
        if (getArguments() != null) {
            modelType = getArguments().getString("model_type", "Business");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simulation_business, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. 뒤로가기 버튼 설정
        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // 2. 우하단 다음 버튼 초기화 (초기 상태는 GONE으로 숨김)
        nextButton = view.findViewById(R.id.btn_next);
        if (nextButton != null) {
            nextButton.setVisibility(View.GONE);
            nextButton.setOnClickListener(v -> goToNextStep());
        }

        // 3. XML의 4가지 수치 입력창(EditText) 찾아오기
        netProfitEdit = view.findViewById(R.id.net_profit_edit);    // 희망 순이익
        marginEdit = view.findViewById(R.id.margin_edit);           // 희망 마진율
        salesVolumEdit = view.findViewById(R.id.sales_volum_edit);  // 목표 판매량
        MinStocklevelEdit = view.findViewById(R.id.min_stocklevel_edit);// 최소 재고량

        // 4. 모든 입력창에 실시간 글자 변경 감지 리스너(TextWatcher) 일괄 등록
        if (netProfitEdit != null && marginEdit != null && salesVolumEdit != null && MinStocklevelEdit != null) {
            netProfitEdit.addTextChangedListener(inputWatcher);
            marginEdit.addTextChangedListener(inputWatcher);
            salesVolumEdit.addTextChangedListener(inputWatcher);
            MinStocklevelEdit.addTextChangedListener(inputWatcher);
        }
    }

    /**
     * 사용자가 키보드로 숫자를 입력할 때마다 실시간으로 호출되는 공통 Watcher
     */
    private final TextWatcher inputWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // 어떤창이든 글자가 바뀔 때마다 4개 창이 모두 채워졌는지 검사합니다.
            checkAllInputsFilled();
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };

    /**
     * 4개의 입력창이 모두 공백 없이 채워졌는지 확인하고 다음 버튼을 제어하는 함수
     */
    private void checkAllInputsFilled() {
        if (netProfitEdit == null || marginEdit == null || salesVolumEdit == null || MinStocklevelEdit == null || nextButton == null) {
            return;
        }

        // 각 입력창의 텍스트 추출 (공백 제거)
        String profitStr = netProfitEdit.getText().toString().trim();
        String marginStr = marginEdit.getText().toString().trim();
        String salesStr = salesVolumEdit.getText().toString().trim();
        String stockStr = MinStocklevelEdit.getText().toString().trim();

        // 단 하나라도 비어있지 않아야 하므로, 4개 문자열이 모두 유효한지 검사합니다.
        if (!profitStr.isEmpty() && !marginStr.isEmpty() && !salesStr.isEmpty() && !stockStr.isEmpty()) {
            nextButton.setVisibility(View.VISIBLE); // 4개 다 채워지면 다음 버튼 나타남
        } else {
            nextButton.setVisibility(View.GONE);    // 하나라도 지워지면 다시 숨김
        }
    }

    /**
     * 다음 시뮬레이션 결과(컨펌) 화면으로 이동하는 함수
     */
    private void goToNextStep() {
        // 이동 전 안전장치: 혹시라도 값이 비어있다면 함수 종료
        if (netProfitEdit.getText().toString().trim().isEmpty() ||
                marginEdit.getText().toString().trim().isEmpty() ||
                salesVolumEdit.getText().toString().trim().isEmpty() ||
                MinStocklevelEdit.getText().toString().trim().isEmpty()) {
            return;
        }

        // 최종 결과 화면 프래그먼트 생성
        Fragment nextFragment = new SimulationConfirmFragment();

        // 사용자가 입력한 값들을 번들에 담아 결과 화면 전달
        Bundle args = new Bundle();
        args.putString("model_type", modelType);
        args.putString("profit_value", netProfitEdit.getText().toString().trim());
        args.putString("margin_value", marginEdit.getText().toString().trim());
        args.putString("sales_value", salesVolumEdit.getText().toString().trim());
        args.putString("stock_value", MinStocklevelEdit.getText().toString().trim());
        nextFragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.main_fragment, nextFragment)
                .addToBackStack(null)
                .commit();
    }
}