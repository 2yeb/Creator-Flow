package com.example.creator_flow;

import android.app.AlertDialog;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.creator_flow.network.ApiService;
import com.example.creator_flow.network.RetrofitClient;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GroupListFragment extends Fragment {
    private RecyclerView groupRecyclerView;
    private ImageView btnGroupDialogOpen;
    private GroupAdapter groupAdapter;
    private List<GroupItem> groupList;

    // Retrofit 및 ApiService 멤버 변수 추가
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_list, container, false);

        apiService = RetrofitClient.getApi(requireContext());

        groupRecyclerView = view.findViewById(R.id.grouplist_recycler);
        btnGroupDialogOpen = view.findViewById(R.id.group_dialog_open_btn);

        groupList = new ArrayList<>();
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getContext() != null) {
            int spaceInPx = (int) (12 * getContext().getResources().getDisplayMetrics().density);
            groupRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(spaceInPx));
        }

        groupAdapter = new GroupAdapter(groupList, project -> {
            navigateToProjectDetail(project.getId());
        });
        groupRecyclerView.setAdapter(groupAdapter);

        btnGroupDialogOpen.setOnClickListener(v -> addNewGroup());

        // 서버로부터 그룹 목록 불러옴
        fetchGroupListFromServer();

        return view;
    }

    /**
     * GET /groups 그룹 목록을 가져옴
     */
    private void fetchGroupListFromServer() {
        apiService.getGroups().enqueue(new Callback<List<GroupListResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<GroupListResponse>> call, @NonNull Response<List<GroupListResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    groupList.clear();

                    // 서버 Response 리스트를 UI용 GroupItem 리스트로 변환 및 매핑
                    List<GroupListResponse> networkGroups = response.body();
                    for (int i = 0; i < networkGroups.size(); i++) {
                        GroupListResponse res = networkGroups.get(i);

                        // 예시 테마 색상 설정 (서버에 색상 값이 없다면 순서대로 임의 지정 가능)
                        int colorResId = R.color.buttoncolor1;
                        if (i % 4 == 0) colorResId = R.color.group_yellow;
                        else if (i % 4 == 1) colorResId = R.color.group_pink;
                        else if (i % 4 == 2) colorResId = R.color.group_blue;

                        // 현재 뼈대 구성 단계이므로 하위 프로젝트 목록은 빈 리스트로 초기 지정
                        GroupItem uiItem = new GroupItem(
                                res.getId(),
                                res.getKeyword(),
                                colorResId,
                                new ArrayList<>()
                        );
                        groupList.add(uiItem);
                    }
                    groupAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "그룹 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GroupListResponse>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "네트워크 연결 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * POST /groups 그룹 추가
     */
    private void addNewGroup() {
        if (getContext() == null) return;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_group, null);

        ImageView closeGroupDialog = dialogView.findViewById(R.id.close_group_dialog);
        EditText editGroupname = dialogView.findViewById(R.id.groupname_edit);
        TextView tvGroupNameError = dialogView.findViewById(R.id.group_name_error_tv);
        androidx.cardview.widget.CardView btnAddGroup = dialogView.findViewById(R.id.add_group_btn);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        closeGroupDialog.setOnClickListener(v -> dialog.dismiss());

        btnAddGroup.setOnClickListener(v -> {
            String inputKeyword = editGroupname.getText().toString().trim();

            if (inputKeyword.isEmpty()) {
                tvGroupNameError.setVisibility(View.VISIBLE);
                tvGroupNameError.setText("그룹 이름을 입력해주세요.");
                return;
            }

            // 중복 이름 검사 (클라이언트 최소 검증)
            boolean isDuplicate = false;
            for (GroupItem item : groupList) {
                if (item.getKeyword().equals(inputKeyword)) {
                    isDuplicate = true;
                    break;
                }
            }

            if (isDuplicate) {
                tvGroupNameError.setVisibility(View.VISIBLE);
                tvGroupNameError.setText("이미 존재하는 이름입니다.");
                return;
            }

            tvGroupNameError.setVisibility(View.INVISIBLE);

            // API 요청용 Body 객체 생성
            CreateGroupRequest requestBody = new CreateGroupRequest();
            requestBody.setKeyword(inputKeyword);

            // 서버로 그룹 생성 요청 전송
            apiService.createGroup(requestBody).enqueue(new Callback<GroupListResponse>() {
                @Override
                public void onResponse(@NonNull Call<GroupListResponse> call, @NonNull Response<GroupListResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        GroupListResponse createdGroup = response.body();

                        // 서버에서 생성하여 응답한 실제 ID와 데이터를 가지고 UI 모델 생성
                        GroupItem newGroup = new GroupItem(
                                createdGroup.getId(),
                                createdGroup.getKeyword(),
                                R.color.buttoncolor1, // 기본 생성 칼라 지정
                                new ArrayList<>()
                        );

                        groupList.add(newGroup);
                        groupAdapter.notifyItemInserted(groupList.size() - 1);

                        Toast.makeText(getContext(), "그룹이 추가되었습니다.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getContext(), "그룹 생성 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GroupListResponse> call, @NonNull Throwable t) {
                    Toast.makeText(getContext(), "통신 에러: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        editGroupname.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (dialog.getWindow() != null) {
                    dialog.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | android.view.WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                    dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }

                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager)
                        getContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(editGroupname, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        dialog.show();
        editGroupname.requestFocus();
    }

    private void navigateToProjectDetail(String projectId) {
        Toast.makeText(getContext(), "프로젝트 상세 ID [" + projectId + "] 화면으로 이동합니다.", Toast.LENGTH_SHORT).show();
    }

    private static class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;

        public VerticalSpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1) {
                outRect.bottom = space;
            }
        }
    }
}