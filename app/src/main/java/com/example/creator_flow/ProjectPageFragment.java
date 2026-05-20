package com.example.creator_flow;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.example.creator_flow.model.ImageUploadResponse;
import com.example.creator_flow.model.ProjectFile;
import com.example.creator_flow.model.ProjectResponse;
import com.example.creator_flow.model.UpdateProjectRequest;
import com.example.creator_flow.network.RetrofitClient;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 프로젝트 파일(차시) 상세 페이지 Fragment
 * - 차시(파일) 탭을 동적으로 추가/전환할 수 있으며,
 *   각 차시마다 프로젝트명, 카테고리, 날짜를 독립적으로 저장한다.
 * - 선택된 차시에 따라 폴더 UI 색상이 노랑/파랑/보라 순으로 순환된다.
 */
public class ProjectPageFragment extends Fragment {

    /** 서버 프로젝트 ID — newInstance()로 전달받거나 목록에서 선택 시 설정 */
    private String projectId;

    /** 서버 이미지 ID 목록 (삭제 시 사용, photoList와 인덱스 동기화) */
    private final List<String> photoImageIds = new ArrayList<>();

    /** 차시(파일) 목록: 각 ProjectFile이 한 차시의 데이터를 담는다 */
    private List<ProjectFile> fileList;

    /** 카메라 촬영 결과를 받는 런처 */
    private ActivityResultLauncher<Uri> cameraLauncher;

    /** 갤러리에서 이미지를 선택하는 런처 */
    private ActivityResultLauncher<String> galleryLauncher;

    /** 카메라 촬영 시 임시 저장할 파일 URI */
    private Uri cameraImageUri;

    /** 카메라 권한 요청 런처 */
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    /** 현재 선택된 차시의 인덱스 */
    private int selectedIndex = 0;

    /**
     * 탭 전환 중 여부를 나타내는 플래그.
     * switchChasi() 도중 TextWatcher/Spinner 리스너가
     * 잘못된 데이터를 저장하지 않도록 막는다.
     */
    private boolean isSwitching = false;

    // ── 사진 박스 ─────────────────────────────────────────────────────────────
    /** 사진 추가 박스 컨테이너 */
    private LinearLayout photoAddBox;

    /** 현재 추가된 사진 URI 목록 (최대 2개) */
    private final List<Uri> photoList = new ArrayList<>();

    /** 사진 박스에서 편집 중인지 여부 (false = 썸네일 편집) */
    private boolean isEditingPhotoBox = false;

    /** 교체 중인 사진 인덱스 (-1 = 새 사진 추가) */
    private int photoBoxEditingIndex = -1;

    // ── 뷰 참조 ──────────────────────────────────────────────────────────────
    private LinearLayout tabContainer;   // 차시 탭들이 가로로 나열되는 컨테이너
    private View folderBodyBack;         // 폴더 뒷면 배경 (더 연한 색)
    private ConstraintLayout folderBody; // 폴더 본체 (진한 색 + 입력 필드)
    private ImageView folderImage;       // 썸네일 이미지
    private EditText fileName;           // 프로젝트명 입력 필드
    private Spinner spinnerCategory;     // 카테고리 선택 스피너
    private TextView calender;           // 날짜 선택 텍스트뷰
    private ImageButton btnAddChasi;     // 차시 추가 버튼
    private EditText etPrice;            // 단가 입력 필드
    private EditText etRetailPrice;      // 판매가 입력 필드
    private EditText etCommission;       // 판매 수수료(%) 입력 필드
    private LineChart chartUnitPrice;      // 차수별 단가 라인차트
    private PieChart chartProfitStructure; // 수익 구조 파이차트
    private EditText etSoldQuantity;       // 현재 판매량 입력
    private EditText etTargetQuantity;     // 목표 판매량 입력
    private ProgressBar progressSales;     // 판매 달성률 프로그레스바
    private TextView tvSalesPercent;       // 달성 퍼센트 텍스트

    public ProjectPageFragment() {}

    /**
     * 프로젝트 ID를 가지고 Fragment를 생성하는 팩토리 메서드.
     * ProjectFragment에서 이 메서드로 Fragment를 생성해야 한다.
     *
     * @param projectId 서버 프로젝트 UUID
     */
    public static ProjectPageFragment newInstance(String projectId) {
        ProjectPageFragment fragment = new ProjectPageFragment();
        Bundle args = new Bundle();
        args.putString("project_id", projectId);
        fragment.setArguments(args);
        return fragment;
    }

    // ── 생명주기 ──────────────────────────────────────────────────────────────

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 카메라 권한 요청 런처 등록
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), granted -> {
                    if (granted) {
                        launchCamera();
                    } else {
                        Toast.makeText(requireContext(), "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    }
                });

        // 카메라 촬영 결과 런처
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(), success -> {
                    if (success && cameraImageUri != null) {
                        if (isEditingPhotoBox) {
                            handlePhotoBoxResult(cameraImageUri);
                        } else {
                            folderImage.setImageURI(cameraImageUri);
                            folderImage.setTag(cameraImageUri);
                        }
                    }
                    isEditingPhotoBox = false;
                });

        // 갤러리 선택 결과 런처
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) {
                        if (isEditingPhotoBox) {
                            handlePhotoBoxResult(uri);
                        } else {
                            folderImage.setImageURI(uri);
                            folderImage.setTag(uri);
                        }
                    }
                    isEditingPhotoBox = false;
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_page, container, false);

        // 초기 차시 1개 생성
        fileList = new ArrayList<>();
        fileList.add(new ProjectFile(1, null, null, today()));

        // 뷰 바인딩
        tabContainer    = view.findViewById(R.id.folder_tab);
        folderBodyBack  = view.findViewById(R.id.folder_body_back);
        folderBody      = view.findViewById(R.id.folder_body);
        folderImage = view.findViewById(R.id.folder_photo);
        fileName = view.findViewById(R.id.folder_name);
        spinnerCategory = view.findViewById(R.id.folder_category);
        calender = view.findViewById(R.id.folder_calendar);
        btnAddChasi     = view.findViewById(R.id.folder_add);
        photoAddBox      = view.findViewById(R.id.photo_add_box);
        etPrice               = view.findViewById(R.id.et_price);
        etRetailPrice         = view.findViewById(R.id.et_retail_price);
        etCommission          = view.findViewById(R.id.et_commission);
        chartUnitPrice        = view.findViewById(R.id.chart_unit_price);
        chartProfitStructure  = view.findViewById(R.id.chart_profit_structure);
        etSoldQuantity        = view.findViewById(R.id.et_sold_quantity);
        etTargetQuantity      = view.findViewById(R.id.et_target_quantity);
        progressSales         = view.findViewById(R.id.progress_sales);
        tvSalesPercent        = view.findViewById(R.id.tv_sales_percent);

        // Bundle에서 프로젝트 ID 읽기
        if (getArguments() != null) {
            projectId = getArguments().getString("project_id");
        }

        setupSpinner();
        setupListeners();
        setupChart();
        switchChasi(0); // 첫 번째 차시로 초기화
        refreshPhotoBox();

        // 서버에서 프로젝트 데이터 로드
        if (projectId != null) loadProjectFromServer();

        return view;
    }

    // ── 초기화 메서드 ─────────────────────────────────────────────────────────

    /**
     * 카테고리 스피너를 설정한다.
     * - 0번 항목("카테고리")은 힌트용으로, 선택 불가 + 드롭다운에서 숨김 처리.
     * - 항목 선택 시 현재 차시의 category 필드를 업데이트한다.
     */
    private void setupSpinner() {
        String[] categories = getResources().getStringArray(R.array.category_items);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(),
                android.R.layout.simple_spinner_item, categories) {

            /** 선택된 항목 뷰: 흰색, 15sp */
            @Override
            public View getView(int pos, View convertView, ViewGroup parent) {
                View v = super.getView(pos, convertView, parent);
                TextView tv = (TextView) v;
                tv.setTextColor(0xFFFFFFFF);
                tv.setTextSize(15f);
                return v;
            }

            /** 드롭다운 목록 뷰: 어두운 색 */
            @Override
            public View getDropDownView(int pos, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(pos, convertView, parent);
                ((TextView) v).setTextColor(0xFF313131);
                return v;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                // 탭 전환 중에는 저장 생략 (전환 중 발생하는 콜백 무시)
                if (!isSwitching && pos > 0) {
                    fileList.get(selectedIndex).setCategory((String) parent.getItemAtPosition(pos));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * 입력 필드와 버튼의 이벤트 리스너를 등록한다.
     * - 프로젝트명: 텍스트 변경 시 현재 차시에 저장
     * - 날짜: 클릭 시 DatePickerDialog 표시
     * - 차시 추가 버튼: addChasi() 호출
     */
    private void setupListeners() {
        // 프로젝트명 입력 감지
        fileName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (!isSwitching) {
                    fileList.get(selectedIndex).setProjectName(s.toString());
                    saveProjectToServer(); // 서버 저장
                }
            }
        });

        // 날짜 클릭 → DatePickerDialog
        calender.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
            new DatePickerDialog(requireContext(),
                    (dp, year, month, day) -> {
                        Calendar sel = Calendar.getInstance();
                        sel.set(year, month, day);
                        String dateStr = sdf.format(sel.getTime());
                        calender.setText(dateStr);
                        fileList.get(selectedIndex).setDate(dateStr);
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // 판매가 입력 감지 → 수익 구조 파이차트 갱신
        TextWatcher profitChartWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (!isSwitching) updateProfitChart();
            }
        };
        etRetailPrice.addTextChangedListener(profitChartWatcher);
        etCommission.addTextChangedListener(profitChartWatcher);

        // 단가 입력 감지 → 현재 차시에 저장 + 차트 갱신
        etPrice.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (!isSwitching) {
                    try {
                        double price = s.toString().isEmpty() ? 0 : Double.parseDouble(s.toString());
                        fileList.get(selectedIndex).setPrice(price);
                    } catch (NumberFormatException ignored) {}
                    updateChart();
                }
            }
        });

        // 현재/목표 판매량 입력 감지 → 프로그레스바 갱신
        TextWatcher salesWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isSwitching) return;
                try {
                    String soldStr = etSoldQuantity.getText().toString();
                    fileList.get(selectedIndex).setSoldQuantity(
                            soldStr.isEmpty() ? 0 : Integer.parseInt(soldStr));
                } catch (NumberFormatException ignored) {}
                try {
                    String targetStr = etTargetQuantity.getText().toString();
                    fileList.get(selectedIndex).setTargetQuantity(
                            targetStr.isEmpty() ? 0 : Integer.parseInt(targetStr));
                } catch (NumberFormatException ignored) {}
                updateSalesProgress();
            }
        };
        etSoldQuantity.addTextChangedListener(salesWatcher);
        etTargetQuantity.addTextChangedListener(salesWatcher);

        // 차시 추가 버튼
        btnAddChasi.setOnClickListener(v -> addChasi());

        // 썸네일 클릭 → 카메라/갤러리 선택 다이얼로그
        folderImage.setOnClickListener(v -> showImagePickerDialog());
    }

    /**
     * 커스텀 레이아웃 다이얼로그를 표시한다.
     * - "카메라 촬영" 클릭 시 카메라 권한 확인 후 촬영 실행
     * - "갤러리에서 가져오기" 클릭 시 갤러리 실행
     * - X 버튼 클릭 시 다이얼로그 닫기
     */
    private void showImagePickerDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_image_picker, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        // 다이얼로그 배경을 투명하게 (커스텀 배경 적용을 위해)
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btn_close_dialog).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btn_camera).setOnClickListener(v -> {
            dialog.dismiss();
            if (ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA);
            }
        });

        dialogView.findViewById(R.id.btn_gallery).setOnClickListener(v -> {
            dialog.dismiss();
            galleryLauncher.launch("image/*");
        });

        dialog.show();
    }

    /**
     * 카메라 앱을 실행한다.
     * 촬영 결과는 캐시 디렉터리의 임시 파일(camera_temp.jpg)에 저장된다.
     */
    private void launchCamera() {
        File imageFile = new File(requireContext().getCacheDir() + "/images/", "camera_temp.jpg");
        imageFile.getParentFile().mkdirs();
        cameraImageUri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                imageFile);
        cameraLauncher.launch(cameraImageUri);
    }

    // ── 차시 전환 ─────────────────────────────────────────────────────────────

    /**
     * 지정한 인덱스의 차시로 전환한다.
     * 1. isSwitching = true로 설정해 리스너의 잘못된 저장을 막는다.
     * 2. 해당 차시의 데이터를 UI에 반영한다.
     * 3. 폴더 색상을 인덱스에 맞게 변경한다.
     * 4. 탭 목록을 다시 그린다.
     *
     * @param index 전환할 차시의 인덱스 (0-based)
     */
    private void switchChasi(int index) {
        isSwitching = true;

        // 현재 차시 썸네일 URI 저장
        if (folderImage.getDrawable() != null && folderImage.getTag() instanceof Uri) {
            fileList.get(selectedIndex).setThumbnailUri((Uri) folderImage.getTag());
        }

        selectedIndex = index;
        ProjectFile file = fileList.get(index);

        // 썸네일 복원
        Uri thumbUri = file.getThumbnailUri();
        folderImage.setImageURI(thumbUri); // null이면 자동으로 이미지 초기화
        folderImage.setTag(thumbUri);

        // 해당 차시 데이터를 UI에 로드
        fileName.setText(file.getProjectName() != null ? file.getProjectName() : "");
        calender.setText(file.getDate() != null ? file.getDate() : today());
        etPrice.setText(file.getPrice() > 0 ? String.valueOf((int) file.getPrice()) : "");
        etSoldQuantity.setText(file.getSoldQuantity() > 0
                ? String.valueOf(file.getSoldQuantity()) : "");
        etTargetQuantity.setText(file.getTargetQuantity() > 0
                ? String.valueOf(file.getTargetQuantity()) : "");

        // 카테고리 스피너 복원 (저장된 값이 없으면 힌트(0번) 선택)
        ArrayAdapter adapter = (ArrayAdapter) spinnerCategory.getAdapter();
        if (file.getCategory() != null && adapter != null) {
            int pos = adapter.getPosition(file.getCategory());
            if (pos >= 0) spinnerCategory.setSelection(pos);
        } else {
            spinnerCategory.setSelection(0);
        }

        // 폴더 색상 업데이트 (folder_body: 진한 색 / folder_body_back: 연한 색)
        int[] colorRes = getColorPair(index);
        folderBody.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), colorRes[0])));
        folderBodyBack.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), colorRes[1])));

        isSwitching = false;
        updateSalesProgress(); // 프로그레스바 갱신
        refreshTabs(); // 탭 UI 갱신
    }

    // ── 탭 UI ─────────────────────────────────────────────────────────────────

    /**
     * 차시 탭 목록을 다시 그린다.
     * - 모든 탭을 WRAP_CONTENT 너비로 생성하며, 왼쪽부터 순서대로 추가한다.
     * - 선택된 탭만 텍스트를 표시하고, 나머지는 TRANSPARENT로 숨긴다.
     *   (배경 색상으로 탭 위치는 구분 가능)
     * - 각 탭의 배경색은 getColorPair()의 연한 색(back)을 사용한다.
     */
    private void refreshTabs() {
        tabContainer.removeAllViews();
        for (int i = 0; i < fileList.size(); i++) {
            TextView tab = new TextView(requireContext());
            tab.setGravity(Gravity.CENTER);
            tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

            // 탭 배경: 차시 인덱스에 맞는 연한 색으로 tint
            int backColor = ContextCompat.getColor(requireContext(), getColorPair(i)[1]);
            Drawable bg = ContextCompat.getDrawable(requireContext(), R.drawable.project_file_tab).mutate();
            DrawableCompat.setTint(DrawableCompat.wrap(bg), backColor);
            tab.setBackground(bg);

            // 선택된 탭만 텍스트 표시 (비선택 탭은 TRANSPARENT로 숨김)
            tab.setText(fileList.get(i).getChasiNumber() + "차시");
            tab.setTextColor(i == selectedIndex
                    ? ContextCompat.getColor(requireContext(), R.color.text_color)
                    : android.graphics.Color.TRANSPARENT);

            tab.setPadding(dpToPx(16), 0, dpToPx(16), 0);
            tab.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, dpToPx(35)));

            final int idx = i;
            tab.setOnClickListener(v -> switchChasi(idx));
            tabContainer.addView(tab);
        }
    }

    // ── 차시 추가 ─────────────────────────────────────────────────────────────

    /**
     * 새 차시를 추가하고 해당 차시로 전환한다.
     * 차시 번호는 현재 목록 크기 + 1로 자동 부여된다.
     */
    private void addChasi() {
        fileList.add(new ProjectFile(fileList.size() + 1, null, null, today()));
        switchChasi(fileList.size() - 1);
        updateChart();
    }

    // ── 서버 API ──────────────────────────────────────────────────────────────

    /**
     * GET /projects/{id} — 서버에서 프로젝트 데이터를 로드해 UI에 반영한다.
     * 프로젝트명, 카테고리(status), 이미지 목록을 복원한다.
     */
    private void loadProjectFromServer() {
        RetrofitClient.getApi(requireContext())
                .getProject(projectId)
                .enqueue(new Callback<ProjectResponse>() {
                    @Override
                    public void onResponse(Call<ProjectResponse> call, Response<ProjectResponse> response) {
                        if (!isAdded() || response.body() == null) return;
                        ProjectResponse project = response.body();

                        // 프로젝트명 반영
                        if (project.name != null) {
                            fileList.get(selectedIndex).setProjectName(project.name);
                            isSwitching = true;
                            fileName.setText(project.name);
                            isSwitching = false;
                        }

                        // 카테고리(status) 반영
                        if (project.status != null) {
                            fileList.get(selectedIndex).setCategory(project.status);
                            ArrayAdapter adapter = (ArrayAdapter) spinnerCategory.getAdapter();
                            if (adapter != null) {
                                int pos = adapter.getPosition(project.status);
                                if (pos >= 0) spinnerCategory.setSelection(pos);
                            }
                        }

                        // 서버 이미지 목록 복원
                        if (project.images != null) {
                            photoList.clear();
                            photoImageIds.clear();
                            for (ProjectResponse.ProjectImage img : project.images) {
                                photoList.add(Uri.parse(img.imageUrl));
                                photoImageIds.add(img.imageId);
                            }
                            refreshPhotoBox();
                        }
                    }

                    @Override
                    public void onFailure(Call<ProjectResponse> call, Throwable t) {
                        if (isAdded())
                            Toast.makeText(requireContext(), "프로젝트 로드 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * PUT /projects/{id} — 현재 프로젝트명과 카테고리를 서버에 저장한다.
     * 프로젝트명 TextWatcher에서 debounce 없이 호출되므로,
     * 입력이 끝날 때마다 저장된다 (추후 debounce 적용 권장).
     */
    private void saveProjectToServer() {
        if (projectId == null) return;
        ProjectFile current = fileList.get(selectedIndex);
        RetrofitClient.getApi(requireContext())
                .updateProject(projectId, new UpdateProjectRequest(
                        current.getProjectName(),
                        current.getCategory()))
                .enqueue(new Callback<ProjectResponse>() {
                    @Override
                    public void onResponse(Call<ProjectResponse> call, Response<ProjectResponse> response) { }
                    @Override
                    public void onFailure(Call<ProjectResponse> call, Throwable t) {
                        if (isAdded())
                            Toast.makeText(requireContext(), "저장 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * POST /projects/{id}/images — URI를 multipart로 변환해 서버에 업로드한다.
     * 업로드 성공 시 반환된 image_id를 photoImageIds에 저장해 삭제 시 사용한다.
     *
     * @param uri       업로드할 이미지 URI
     * @param listIndex 업로드 후 photoImageIds에 저장할 위치
     */
    private void uploadImageToServer(Uri uri, int listIndex) {
        if (projectId == null) return;
        try {
            // URI → 임시 파일 → Multipart
            File tempFile = new File(requireContext().getCacheDir(), "upload_" + System.currentTimeMillis() + ".jpg");
            InputStream in = requireContext().getContentResolver().openInputStream(uri);
            OutputStream out = new FileOutputStream(tempFile);
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            in.close(); out.close();

            RequestBody reqBody = RequestBody.create(MediaType.parse("image/*"), tempFile);
            MultipartBody.Part part = MultipartBody.Part.createFormData("image", tempFile.getName(), reqBody);

            RetrofitClient.getApi(requireContext())
                    .uploadImage(projectId, part)
                    .enqueue(new Callback<ImageUploadResponse>() {
                        @Override
                        public void onResponse(Call<ImageUploadResponse> call, Response<ImageUploadResponse> response) {
                            if (!isAdded() || response.body() == null) return;
                            // image_id 저장 (삭제 시 사용)
                            if (listIndex < photoImageIds.size()) {
                                photoImageIds.set(listIndex, response.body().imageId);
                            } else {
                                photoImageIds.add(response.body().imageId);
                            }
                        }

                        @Override
                        public void onFailure(Call<ImageUploadResponse> call, Throwable t) {
                            if (isAdded())
                                Toast.makeText(requireContext(), "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "이미지 처리 오류", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * DELETE /projects/{id}/images/{image_id} — 서버에서 이미지를 삭제한다.
     *
     * @param imageId 삭제할 이미지의 서버 UUID
     */
    private void deleteImageFromServer(String imageId) {
        if (projectId == null || imageId == null) return;
        RetrofitClient.getApi(requireContext())
                .deleteImage(projectId, imageId)
                .enqueue(new Callback<com.google.gson.JsonObject>() {
                    @Override
                    public void onResponse(Call<com.google.gson.JsonObject> call,
                                           Response<com.google.gson.JsonObject> response) { }
                    @Override
                    public void onFailure(Call<com.google.gson.JsonObject> call, Throwable t) { }
                });
    }

    // ── 차트 ──────────────────────────────────────────────────────────────────

    /**
     * LineChart 초기 스타일을 설정한다.
     * 데이터는 updateChart()에서 채운다.
     */
    private void setupChart() {
        chartUnitPrice.setDescription(null);
        chartUnitPrice.getLegend().setEnabled(false);
        chartUnitPrice.setTouchEnabled(false);
        chartUnitPrice.setDrawGridBackground(false);
        chartUnitPrice.setBackgroundColor(Color.TRANSPARENT);

        // X축
        XAxis xAxis = chartUnitPrice.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(0xFF888888);
        xAxis.setTextSize(11f);
        xAxis.setGranularity(1f);

        // 왼쪽 Y축
        YAxis leftAxis = chartUnitPrice.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(0xFFDDDDDD);
        leftAxis.setTextColor(0xFF888888);
        leftAxis.setTextSize(11f);
        leftAxis.setAxisMinimum(0f);

        // 오른쪽 Y축 숨김
        chartUnitPrice.getAxisRight().setEnabled(false);

        updateChart();
    }

    /**
     * 각 차시의 단가 데이터를 읽어 LineChart를 갱신한다.
     * 단가가 0인 차시는 제외하고, 최고값 포인트만 레이블을 표시한다.
     */
    private void updateChart() {
        List<Entry> entries = new ArrayList<>();
        String[] labels = new String[fileList.size()];

        for (int i = 0; i < fileList.size(); i++) {
            double price = fileList.get(i).getPrice();
            if (price > 0) entries.add(new Entry(i, (float) price));
            labels[i] = fileList.get(i).getChasiNumber() + "차";
        }

        chartUnitPrice.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chartUnitPrice.getXAxis().setLabelCount(fileList.size());

        if (entries.isEmpty()) {
            chartUnitPrice.clear();
            return;
        }

        // 최고값 계산
        float maxVal = Float.MIN_VALUE;
        for (Entry e : entries) if (e.getY() > maxVal) maxVal = e.getY();
        final float maxValue = maxVal;

        LineDataSet dataSet = new LineDataSet(entries, "단가");
        dataSet.setColor(0xFF7EB4E8);
        dataSet.setCircleColor(0xFF7EB4E8);
        dataSet.setCircleRadius(4f);
        dataSet.setLineWidth(2f);
        dataSet.setDrawFilled(false);
        dataSet.setMode(LineDataSet.Mode.LINEAR);
        dataSet.setDrawValues(true);

        // 최고값만 레이블 표시
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value == maxValue ? String.valueOf((int) value) : "";
            }
        });
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(Color.BLACK);

        chartUnitPrice.setData(new LineData(dataSet));
        chartUnitPrice.invalidate();
    }

    /**
     * 판매가·단가·수수료를 바탕으로 수익 구조 파이차트를 갱신한다.
     * - 판매가가 비어있거나 0 이하이면 차트를 GONE 처리한다.
     * - 슬라이스: 제작원가(파랑), 판매수수료(주황, 수수료 > 0일 때만), 순이익(초록)
     */
    private void updateProfitChart() {
        String retailStr = etRetailPrice.getText().toString().trim();
        if (retailStr.isEmpty()) {
            chartProfitStructure.setVisibility(View.GONE);
            return;
        }
        float sellingPrice  = parseFloat(retailStr);
        float unitCost      = parseFloat(etPrice.getText().toString());
        float commissionPct = parseFloat(etCommission.getText().toString());
        float fee           = sellingPrice * commissionPct / 100f;
        float profit        = sellingPrice - unitCost - fee;

        if (profit < 0 || sellingPrice <= 0) {
            chartProfitStructure.setVisibility(View.GONE);
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        if (unitCost > 0) { entries.add(new PieEntry(unitCost, "제작 원가")); colors.add(0xFFFF8587); }
        if (fee > 0)      { entries.add(new PieEntry(fee,      "판매 수수료")); colors.add(0xFFFFC171); }
        if (profit > 0)   { entries.add(new PieEntry(profit,   "순이익"));    colors.add(0xFF97E1AC); }

        if (entries.isEmpty()) {
            chartProfitStructure.setVisibility(View.GONE);
            return;
        }

        chartProfitStructure.setUsePercentValues(true); // setData() 전에 먼저 설정

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new PercentFormatter(chartProfitStructure));

        PieData data = new PieData(dataSet);
        chartProfitStructure.setData(data);
        chartProfitStructure.setDrawHoleEnabled(true);
        chartProfitStructure.setHoleRadius(45f);
        chartProfitStructure.setTransparentCircleRadius(50f);
        chartProfitStructure.setHoleColor(Color.TRANSPARENT);
        chartProfitStructure.setCenterText(
                String.format(Locale.KOREA, "판매가\n%,d원", (int) sellingPrice));
        chartProfitStructure.setCenterTextSize(13f);
        chartProfitStructure.setCenterTextColor(0xFF444444);
        chartProfitStructure.setDescription(null);
        chartProfitStructure.setDrawEntryLabels(false); // 슬라이스 위 이름 라벨 숨김
        chartProfitStructure.setRotationEnabled(false);
        chartProfitStructure.setTouchEnabled(false);
        chartProfitStructure.getLegend().setEnabled(true);
        chartProfitStructure.getLegend().setTextColor(0xFF444444);
        chartProfitStructure.getLegend().setTextSize(11f);
        chartProfitStructure.setVisibility(View.VISIBLE);
        chartProfitStructure.invalidate();
    }

    /**
     * 현재 차시의 판매량 데이터를 읽어 프로그레스바와 텍스트를 갱신한다.
     * - 목표 판매량이 0이면 진행률 0%로 초기화
     * - 달성률이 100%를 초과하면 100%로 고정
     */
    private void updateSalesProgress() {
        int sold   = fileList.get(selectedIndex).getSoldQuantity();
        int target = fileList.get(selectedIndex).getTargetQuantity();

        if (target <= 0) {
            progressSales.setProgress(0);
            tvSalesPercent.setText("0%");
            return;
        }

        int percent = Math.min((int) ((sold / (float) target) * 100), 100);
        progressSales.setProgress(percent);
        tvSalesPercent.setText(percent + "%");
    }

    /** 문자열을 float으로 변환한다. 빈 문자열이나 파싱 실패 시 0을 반환한다. */
    private float parseFloat(String s) {
        try { return s == null || s.isEmpty() ? 0f : Float.parseFloat(s); }
        catch (NumberFormatException e) { return 0f; }
    }

    // ── 유틸리티 ──────────────────────────────────────────────────────────────

    /**
     * 인덱스에 따른 폴더 색상 쌍을 반환한다.
     * index % 3 → 0: 노랑, 1: 파랑, 2: 보라 순으로 순환.
     *
     * @param index 차시 인덱스
     * @return int[]{진한 색 res, 연한 색 res}
     */
    private int[] getColorPair(int index) {
        switch (index % 3) {
            case 0:  return new int[]{R.color.folder_yellow,  R.color.folder_yellow_back};
            case 1:  return new int[]{R.color.folder_blue,    R.color.folder_blue_back};
            default: return new int[]{R.color.folder_purple,  R.color.folder_purple_back};
        }
    }

    /** 오늘 날짜를 "MMM d, yyyy" 형식(영문)으로 반환한다. 예: "May 10, 2026" */
    private String today() {
        return new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)
                .format(Calendar.getInstance().getTime());
    }

    // ── 사진 박스 ─────────────────────────────────────────────────────────────

    /**
     * 사진 박스를 현재 photoList 상태에 맞게 다시 그린다.
     * - 사진이 있으면 좌측부터 120dp 정사각형으로 표시
     * - 사진이 2개 미만이면 + 버튼 추가 (사진 없으면 전체 너비 차지)
     * - 각 사진 클릭 시 다이얼로그로 교체 가능
     */
    private void refreshPhotoBox() {
        photoAddBox.removeAllViews();

        // 사진 2개: 가운데 정렬 / 그 외: 좌측 정렬
        photoAddBox.setGravity(photoList.size() == 2
                ? Gravity.CENTER
                : Gravity.CENTER_VERTICAL);

        // 추가된 사진들 표시
        for (int i = 0; i < photoList.size(); i++) {
            ImageView photoView = new ImageView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(120), dpToPx(120));
            params.setMarginEnd(dpToPx(12));
            photoView.setLayoutParams(params);
            photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            photoView.setBackground(ContextCompat.getDrawable(
                    requireContext(), R.drawable.project_rounded_solid));
            photoView.setClipToOutline(true);
            photoView.setImageURI(photoList.get(i));

            final int index = i;
            photoView.setOnClickListener(v -> {
                isEditingPhotoBox = true;
                photoBoxEditingIndex = index;
                showImagePickerDialog();
            });

            photoAddBox.addView(photoView);
        }

        // 사진이 2개 미만이면 + 버튼 추가
        if (photoList.size() < 2) {
            ImageView addBtn = new ImageView(requireContext());
            LinearLayout.LayoutParams params;
            if (photoList.isEmpty()) {
                // 사진 없음: + 버튼이 전체 공간 차지
                params = new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            } else {
                // 사진 1개: + 버튼은 사진과 동일한 크기
                params = new LinearLayout.LayoutParams(dpToPx(120), dpToPx(120));
            }
            addBtn.setLayoutParams(params);
            addBtn.setImageDrawable(ContextCompat.getDrawable(
                    requireContext(), R.drawable.ic_add));
            addBtn.setScaleType(ImageView.ScaleType.CENTER);
            addBtn.setColorFilter(0xFF888888, android.graphics.PorterDuff.Mode.SRC_IN);

            addBtn.setOnClickListener(v -> {
                isEditingPhotoBox = true;
                photoBoxEditingIndex = -1;
                showImagePickerDialog();
            });

            photoAddBox.addView(addBtn);
        }
    }

    /**
     * 사진 박스에서 카메라/갤러리 결과를 처리한다.
     * - photoBoxEditingIndex >= 0: 기존 사진 교체
     * - photoBoxEditingIndex == -1: 새 사진 추가
     */
    private void handlePhotoBoxResult(Uri uri) {
        if (photoBoxEditingIndex >= 0 && photoBoxEditingIndex < photoList.size()) {
            // 기존 이미지 교체: 서버에서 이전 이미지 삭제 후 새 이미지 업로드
            String oldImageId = photoBoxEditingIndex < photoImageIds.size()
                    ? photoImageIds.get(photoBoxEditingIndex) : null;
            if (oldImageId != null) deleteImageFromServer(oldImageId);

            photoList.set(photoBoxEditingIndex, uri);
            uploadImageToServer(uri, photoBoxEditingIndex);
        } else if (photoList.size() < 2) {
            // 새 이미지 추가
            int newIndex = photoList.size();
            photoList.add(uri);
            uploadImageToServer(uri, newIndex);
        }
        photoBoxEditingIndex = -1;
        refreshPhotoBox();
    }

    /** dp 값을 현재 화면 밀도에 맞는 px 값으로 변환한다. */
    private int dpToPx(int dp) {
        return (int) (dp * requireContext().getResources().getDisplayMetrics().density);
    }
}
