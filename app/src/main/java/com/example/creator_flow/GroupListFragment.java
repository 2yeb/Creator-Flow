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

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        groupAdapter = new GroupAdapter(
                groupList,
                project -> { navigateToProjectPage(project.getId()); },
                (ProjectItem project, String currentGroupId) -> {
                    showMoveGroupDialog(project, currentGroupId);
                },
                (group, position) -> { showDeleteDialog(group, position); }
        );
        groupRecyclerView.setAdapter(groupAdapter);

        btnGroupDialogOpen.setOnClickListener(v -> addNewGroup());

        // 서버로부터 그룹 목록 불러옴
        fetchGroupListFromServer();

        return view;
    }

    /**
     * 삭제 확인 다이얼로그
     */
    private void showDeleteDialog(GroupItem group, int position) {
        if (getContext() == null) return;

        // '기타' 그룹 삭제 요청시
        if ("unclassified_dummy_id".equals(group.getId())) {
            new AlertDialog.Builder(getContext())
                    .setTitle("안내")
                    .setMessage("'기타'는 삭제할 수 없습니다.")
                    .setPositiveButton("확인", null)
                    .show();
            return; // 서버 삭제 프로세스로 가지 못하게 즉시 중단
        }

        new AlertDialog.Builder(getContext())
                .setTitle("그룹 삭제")
                .setMessage("'" + group.getKeyword() + "' 그룹을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> {
                    // 바로 그룹을 지우지 않고, 연관 태그들을 먼저 해제하는 순차 프로세스 진입
                    startUntaggingProcess(group, position);
                })
                .setNegativeButton("취소", null)
                .show();
    }


    /**
     * GET /groups 그룹 목록을 가져옴
     */
    // GroupListFragment.java 내부의 서버 조회 메서드 교체 및 추가

    private void fetchGroupListFromServer() {
        if (!isAdded() || getContext() == null) return;
        // 1. 서버에서 그룹 목록을 불러옴
        apiService.getGroups().enqueue(new Callback<List<GroupListResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<GroupListResponse>> call, @NonNull Response<List<GroupListResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    groupList.clear();

                    List<GroupListResponse> responses = response.body();

                    // 각 그룹이 생성될 때 번갈아가며 적용될 색상 배열 정의
                    int[][] colorPalette = {
                            { R.color.group_yellow, R.color.group_dark_yellow },
                            { R.color.group_pink,   R.color.group_dark_pink },
                            { R.color.buttoncolor1,   R.color.group_dark_green },
                            { R.color.group_blue,  R.color.group_dark_blue }
                    };

                    // 서버에서 받아온 정식 그룹 틀 준비 + 색상 변경
                    for (int i = 0; i < responses.size(); i++) {
                        GroupListResponse res = responses.get(i);

                        // 현재 순번에 맞는 색상 쌍 추출
                        int[] assignedColors = colorPalette[i % colorPalette.length];
                        int backgroundColor = assignedColors[0]; // 연한 색
                        int strokePointColor = assignedColors[1]; // 진한 색

                        // (※ GroupItem 클래스 생성자에 인자(int strokeColor)를 하나 더 추가하셔야 합니다)
                        GroupItem item = new GroupItem(
                                res.getId(),
                                res.getKeyword(),
                                backgroundColor,   // 배경색
                                strokePointColor,  // 아이콘/텍스트/화살표용 진한 색
                                new ArrayList<>()
                        );
                        groupList.add(item);
                    }

                    // 2단계: 프로젝트 목록을 조회하러 이동
                    fetchRawProjectsAndMap();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GroupListResponse>> call, @NonNull Throwable t) {
                if (isAdded()) Toast.makeText(getContext(), "그룹 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     *  GET projects/{project_id}/groups 로 그룹과 프로젝트 연결
     */
    private void fetchRawProjectsAndMap() {
        apiService.getProjects().enqueue(new Callback<List<ProjectListResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProjectListResponse>> call, @NonNull Response<List<ProjectListResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ProjectListResponse> rawProjects = response.body();

                    // '기타' 그룹 생성
                    GroupItem unclassifiedGroup = new GroupItem("unclassified_dummy_id", "기타", R.color.buttoncolor1, R.color.group_dark_green, new ArrayList<>());

                    final int totalProjects = rawProjects.size();
                    if (totalProjects == 0) {
                        groupList.add(unclassifiedGroup);
                        groupAdapter.notifyDataSetChanged();
                        return;
                    }

                    final int[] completedCount = {0};

                    for (ProjectListResponse rawProject : rawProjects) {
                        // ProjectListResponse 객체의 Getter에 맞춰 UI용 ProjectItem을 생성
                        ProjectItem projectItem = new ProjectItem(
                                rawProject.getId(),
                                rawProject.getName(),
                                rawProject.getStatus()
                        );

                        // 2. ApiService.java 실제 명세에 맞춰 getProjectTag 호출 및 List<JsonObject> 파싱
                        apiService.getProjectTag(projectItem.getId()).enqueue(new Callback<List<JsonObject>>() {
                            @Override
                            public void onResponse(@NonNull Call<List<JsonObject>> call, @NonNull Response<List<JsonObject>> response) {
                                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {

                                    // List<JsonObject>에서 첫 번째 매핑 객체를 안전하게 꺼냅니다.
                                    JsonObject firstGroup = response.body().get(0);

                                    // 매핑 테이블에서 group_id 혹은 id를 문자열로 추출
                                    String matchedGroupId = "";
                                    if (firstGroup.has("id")) {
                                        matchedGroupId = firstGroup.get("id").getAsString();
                                    } else if (firstGroup.has("group_id")) {
                                        matchedGroupId = firstGroup.get("group_id").getAsString();
                                    }

                                    // 내 groupList에서 매칭되는 그룹을 찾아 프로젝트를 바인딩
                                    for (GroupItem g : groupList) {
                                        if (g.getId().equals(matchedGroupId)) {
                                            g.getProjects().add(projectItem);
                                            break;
                                        }
                                    }
                                } else {
                                    // 매핑 정보가 없음 -> '기타' 에 배치
                                    unclassifiedGroup.getProjects().add(projectItem);
                                }

                                // 모든 프로젝트의 비동기 매핑 순회가 끝났는지 트래킹 후 화면 일괄 리프레시
                                completedCount[0]++;
                                if (completedCount[0] == totalProjects) {
                                    groupList.add(unclassifiedGroup);
                                    groupAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<List<JsonObject>> call, @NonNull Throwable t) {
                                unclassifiedGroup.getProjects().add(projectItem);
                                completedCount[0]++;
                                if (completedCount[0] == totalProjects) {
                                    groupList.add(unclassifiedGroup);
                                    groupAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<ProjectListResponse>> call, @NonNull Throwable t) {
                if (isAdded()) Toast.makeText(getContext(), "프로젝트 목록 로드 실패", Toast.LENGTH_SHORT).show();
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

            // 서버로 그룹 생성 요청 전송
            apiService.createGroup(inputKeyword).enqueue(new Callback<GroupListResponse>() {
                @Override
                public void onResponse(@NonNull Call<GroupListResponse> call, @NonNull Response<GroupListResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        GroupListResponse createdGroup = response.body();

                        // 서버에서 생성하여 응답한 실제 ID와 데이터를 가지고 UI 모델 생성
                        GroupItem newGroup = new GroupItem(
                                createdGroup.getId(),
                                createdGroup.getKeyword(),
                                R.color.buttoncolor1, // 기본 생성 칼라 지정
                                R.color.group_dark_green,
                                new ArrayList<>()
                        );

                        groupList.add(newGroup);
                        groupAdapter.notifyItemInserted(groupList.size() - 1);

                        Toast.makeText(getContext(), "그룹이 추가되었습니다.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        fetchGroupListFromServer();
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

    /**
     * 그룹 내부의 모든 프로젝트 태그를 해제
     */
    private void startUntaggingProcess(GroupItem group, int position) {
        List<ProjectItem> linkedProjects = group.getProjects();
        String groupId = group.getId();

        // 만약 그룹에 프로젝트가 하나도 태그되어 있지 않다면 즉시 그룹 삭제로 이동
        if (linkedProjects == null || linkedProjects.isEmpty()) {
            deleteGroupFromServer(groupId, position);
            return;
        }

        final int totalProjects = linkedProjects.size();
        final int[] successCount = {0}; // 비동기 응답 카운트용 배열

        for (ProjectItem project : linkedProjects) {
            apiService.untagGroup(project.getId(), groupId).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        successCount[0]++;

                        // 태그가 전부 안전하게 해제 완료되었을 때만 최종적으로 그룹 삭제 수행
                        if (successCount[0] == totalProjects) {
                            deleteGroupFromServer(groupId, position);
                        }
                    } else {
                        if (isAdded()) {
                            Toast.makeText(getContext(), "일부 프로젝트 태그 해제 실패로 삭제를 중단", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "네트워크 문제로 인해 삭제 처리 중단", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    /**
     *  DELETE /groups/{group_id} 그룹 삭제
     */
    private void deleteGroupFromServer(String groupId, int position) {
        apiService.deleteGroup(groupId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && isAdded()) {
                    // 1. 메모리 리스트에서 데이터 제거
                    groupList.remove(position);
                    // 2. 리사이클러뷰에 삭제 애니메이션 알림
                    groupAdapter.notifyItemRemoved(position);
                    groupAdapter.notifyItemRangeChanged(position, groupList.size());

                    Toast.makeText(getContext(), "태그가 안전하게 삭제", Toast.LENGTH_SHORT).show();
                } else {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "그룹 삭제 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "서버 통신 실패로 그룹 지우기 실패", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void navigateToProjectPage(String projectId) {
        // 1. ProjectPageFragment 인스턴스 생성
        ProjectPageFragment projectPageFragment = ProjectPageFragment.newInstance(projectId);

        // 2. FragmentTransaction을 사용하여 화면 전환
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, projectPageFragment)
                    .addToBackStack(null) // 뒤로가기
                    .commit();
        }
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

    private void showMoveGroupDialog(ProjectItem project, String currentGroupId) {
        if (getContext() == null || groupList == null) return;

        List<String> dialogOptions = new ArrayList<>();
        final List<GroupItem> targetGroups = new ArrayList<>(groupList);

        for (GroupItem group : targetGroups) {
            dialogOptions.add(group.getKeyword());
        }

        new AlertDialog.Builder(getContext())
                .setTitle("프로젝트 그룹 이동")
                .setItems(dialogOptions.toArray(new String[0]), (dialog, which) -> {

                    GroupItem selectedGroup = targetGroups.get(which);
                    String targetGroupId = selectedGroup.getId();

                    boolean isTargetUnclassified = "unclassified_dummy_id".equals(targetGroupId)
                            || "기타".equals(selectedGroup.getKeyword());

                    boolean isCurrentUnclassified = (currentGroupId == null
                            || "unclassified_dummy_id".equals(currentGroupId)
                            || currentGroupId.isEmpty());

                    if (isCurrentUnclassified && isTargetUnclassified) {
                        Toast.makeText(getContext(), "이미 기타 그룹에 속해 있습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!isCurrentUnclassified && currentGroupId.equals(targetGroupId)) {
                        Toast.makeText(getContext(), "이미 그룹에 속해 있습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isTargetUnclassified) {
                        // 그룹 -> 기타 그룹으로 이동 (untag)
                        if (!isCurrentUnclassified) {
                            apiService.untagGroup(project.getId(), currentGroupId).enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                                    if (response.isSuccessful() && isAdded()) {
                                        Toast.makeText(getContext(), "그룹 지정이 해제되었습니다.", Toast.LENGTH_SHORT).show();
                                        fetchGroupListFromServer(); // API 호출로 새로고침
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                                    if (isAdded()) Toast.makeText(getContext(), "네트워크 통신 실패", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        // 다른 그룹으로 이동 (untag -> tag)
                        if (!isCurrentUnclassified) {
                            apiService.untagGroup(project.getId(), currentGroupId).enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                                    if (response.isSuccessful() && isAdded()) {
                                        executeTagGroup(project.getId(), targetGroupId);
                                    } else {
                                        executeTagGroup(project.getId(), targetGroupId);
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                                    if (isAdded()) Toast.makeText(getContext(), "네트워크 통신 실패", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // 원래 기타 상태였다면 바로 새 매핑 생성(POST)
                            executeTagGroup(project.getId(), targetGroupId);
                        }
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    /**
     * project_group_map에 새 데이터 등록 후 체인 새로고침 호출
     */
    private void executeTagGroup(String projectId, String targetGroupId) {
        apiService.tagGroup(projectId, targetGroupId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && isAdded()) {
                    Toast.makeText(getContext(), "그룹이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                    fetchGroupListFromServer(); // API 호출로 새로고침
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isAdded()) Toast.makeText(getContext(), "네트워크 통신 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
}