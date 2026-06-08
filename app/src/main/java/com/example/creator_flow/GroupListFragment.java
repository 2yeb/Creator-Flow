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
        // 1. 먼저 정식 그룹 목록을 서버에서 가져옵니다.
        apiService.getGroups().enqueue(new Callback<List<GroupListResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<GroupListResponse>> call, @NonNull Response<List<GroupListResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GroupListResponse> serverGroups = response.body();

                    // 임시 가공용 리스트 생성
                    List<GroupItem> parsedGroups = new ArrayList<>();

                    // 기존 화면의 색상 테마 설정을 그대로 유지하기 위한 배열
                    int[] themeColors = {
                            R.color.buttoncolor1,
                            R.color.group_yellow, // 프로젝트에 선언된 테마 색상 ID들로 채워주세요
                            R.color.group_pink,
                            R.color.group_blue
                    };

                    for (int i = 0; i < serverGroups.size(); i++) {
                        GroupListResponse g = serverGroups.get(i);
                        // 순서대로 테마 색상을 배정합니다.
                        int colorResId = themeColors[i % themeColors.length];

                        // 각 그룹 객체 생성 (기본값으로 프로젝트 리스트는 우선 빈 리스트 주입)
                        // 만약 백엔드의 getGroups() 응답 내부에 projects 데이터가 이미 포함되어 내려온다면 g.getProjects()를 넣으시면 됩니다.
                        parsedGroups.add(new GroupItem(g.getId(), g.getKeyword(), colorResId, new ArrayList<>()));
                    }

                    // 2. 그룹 파싱이 끝나면, 전체 프로젝트 목록을 조회하러 이동합니다.
                    fetchTotalProjectsAndMerge(parsedGroups);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GroupListResponse>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "그룹 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 전체 프로젝트 리스트를 가져와 어느 그룹에도 속하지 않은 것을 "미분류 프로젝트" 묶음으로 생성합니다.
     */
    private void fetchTotalProjectsAndMerge(List<GroupItem> officialGroups) {
        // ApiService에 정의된 프로젝트 전체 조회 API 호출 (예시: getProjects() 혹은 getAllProjects())
        // 만약 ApiService 인터페이스에 없다면 Call<List<ProjectItem>> 함수를 호출해주어야 합니다.
        apiService.getProjects().enqueue(new Callback<List<ProjectListResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProjectListResponse>> call, @NonNull Response<List<ProjectListResponse>> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {

                    // 1. 서버로부터 받은 원본 ProjectListResponse 리스트
                    List<ProjectListResponse> serverProjects = response.body();

                    // 2. 제네릭 에러를 해결하기 위해 화면 전용 모델인 ProjectItem 리스트로 변환
                    List<ProjectItem> allProjects = new ArrayList<>();
                    for (ProjectListResponse sp : serverProjects) {
                        // ※ ProjectListResponse의 getter 메서드 명칭(getId, getName, getStatus 등)에 맞게 수동 매핑해 줍니다.
                        // 만약 가용 가능한 getter 이름이 다르다면 sp.getxxx() 부분을 실제 명칭으로 수정해 주세요.
                        allProjects.add(new ProjectItem(sp.getId(), sp.getName(), "진행중"));
                    }

                    // 3. 그룹에 노출되고 있는 프로젝트들의 ID를 HashSet에 전부 수집
                    java.util.HashSet<String> groupedProjectIds = new java.util.HashSet<>();
                    for (GroupItem group : officialGroups) {
                        if (group.getProjects() != null) {
                            for (ProjectItem p : group.getProjects()) {
                                groupedProjectIds.add(p.getId());
                            }
                        }
                    }

                    // 4. 전체 프로젝트 목록을 순회하며, 그룹에 포함되지 않은 프로젝트만 선별
                    List<ProjectItem> unclassifiedList = new ArrayList<>();
                    for (ProjectItem p : allProjects) {
                        if (!groupedProjectIds.contains(p.getId())) {
                            unclassifiedList.add(p);
                        }
                    }

                    // 5. 미분류 프로젝트가 단 1개라도 존재한다면 가짜 그룹 탭을 생성
                    if (!unclassifiedList.isEmpty()) {
                        GroupItem unclassifiedDummyGroup = new GroupItem(
                                "unclassified_dummy_id",    // 임의의 가짜 ID 지정
                                "기타",                          // 노출될 타이틀 이름
                                R.color.buttoncolor1,          // 미분류 전용 테마 색상
                                unclassifiedList             // 선별된 프로젝트 목록 주입
                        );
                        // 미분류는 기본적으로 펼침(Expanded) 상태로 적용
                        unclassifiedDummyGroup.setExpanded(true);

                        // 정식 리스트 최하단에 결합
                        officialGroups.add(unclassifiedDummyGroup);
                    }

                    // 6. 최종 병합된 리스트를 원본 리스트에 덮어쓰고 어댑터 갱신
                    groupList.clear();
                    groupList.addAll(officialGroups);
                    groupAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProjectListResponse>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "전체 프로젝트 목록 연동 실패", Toast.LENGTH_SHORT).show();
                }
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

                        // 태그가 전부 안전하게 해제 완료되었을 때만 최종적으로 그룹 삭제 수행!
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

        // 1. 다이얼로그 목록에 표시할 그룹 이름
        List<String> groupNames = new ArrayList<>();
        final List<GroupItem> targetGroups = new ArrayList<>(groupList);

        for (GroupItem group : targetGroups) {
            groupNames.add(group.getKeyword());
        }

        // 2. AlertDialog 빌드 및 목록 아이템 클릭 리스너 설정
        new AlertDialog.Builder(getContext())
                .setTitle("프로젝트 그룹 이동")
                .setItems(groupNames.toArray(new String[0]), (dialog, which) -> {

                    GroupItem selectedGroup = targetGroups.get(which);
                    String targetGroupId = selectedGroup.getId();

                    // [체크] 선택한 대상이 '기타' 그룹인지 판별 (ID 또는 키워드 이름 기준)
                    boolean isTargetUnclassified = "unclassified_dummy_id".equals(targetGroupId)
                            || "기타".equals(selectedGroup.getKeyword());

                    // [체크] 현재 프로젝트의 소속 상태 정의 (null이거나 더미ID면 미분류 상태로 간주)
                    boolean isCurrentUnclassified = (currentGroupId == null
                            || "unclassified_dummy_id".equals(currentGroupId)
                            || currentGroupId.isEmpty());

                    // --------------------------------------------------------
                    // 기존 그룹과 동일한 그룹을 선택한 경우 -> 아무런 작업도 하지 않음
                    // --------------------------------------------------------
                    if (isCurrentUnclassified && isTargetUnclassified) {
                        Toast.makeText(getContext(), "이미 기타 그룹에 속해 있습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!isCurrentUnclassified && currentGroupId.equals(targetGroupId)) {
                        Toast.makeText(getContext(), "이미 해당 그룹에 속해 있습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // --------------------------------------------------------
                    // 다른 그룹을 선택한 경우
                    // --------------------------------------------------------
                    if (isTargetUnclassified) {
                        // [케이스 A] 정식 그룹 -> 기타 그룹으로 이동
                        if (!isCurrentUnclassified) {
                            apiService.untagGroup(project.getId(), currentGroupId).enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                                    if (response.isSuccessful() && isAdded()) {
                                        Toast.makeText(getContext(), "그룹 지정이 해제되었습니다.", Toast.LENGTH_SHORT).show();

                                        // API 변경이 불가능하므로 로컬 메모리에서 프로젝트 위치를 강제로 이동
                                        moveProjectInLocalMemory(project, currentGroupId, targetGroupId);
                                    } else {
                                        if (isAdded()) Toast.makeText(getContext(), "그룹 해제 실패", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                                    if (isAdded()) Toast.makeText(getContext(), "네트워크 통신 실패", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        // [케이스 B] 다른 정식 그룹으로 이동하는 경우 (기존 매핑 해제 후 새 매핑 등록)
                        if (!isCurrentUnclassified) {
                            apiService.untagGroup(project.getId(), currentGroupId).enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                                    if (response.isSuccessful() && isAdded()) {
                                        executeTagGroup(project, currentGroupId, targetGroupId);
                                    } else {
                                        // 예외 방어: 서버 DB가 어떤 이유로 비어있어 DELETE가 실패하더라도 POST 등록을 이어서 진행시킵니다.
                                        executeTagGroup(project, currentGroupId, targetGroupId);
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                                    if (isAdded()) Toast.makeText(getContext(), "네트워크 통신 실패", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // 원래 '기타' 상태였다면 끊어낼 행이 없으므로 바로 새 그룹 태그 수행
                            executeTagGroup(project, currentGroupId, targetGroupId);
                        }
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    /**
     * 프로젝트에 새로운 그룹 태그를 등록
     */
    private void executeTagGroup(ProjectItem project, String oldGroupId, String targetGroupId) {
        apiService.tagGroup(project.getId(), targetGroupId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && isAdded()) {
                    Toast.makeText(getContext(), "그룹이 변경되었습니다.", Toast.LENGTH_SHORT).show();

                    // API 변경이 불가능하므로 로컬 메모리에서 프로젝트 위치를 강제로 이동
                    moveProjectInLocalMemory(project, oldGroupId, targetGroupId);
                } else {
                    if (isAdded()) Toast.makeText(getContext(), "새 그룹 지정 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isAdded()) Toast.makeText(getContext(), "네트워크 통신 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void moveProjectInLocalMemory(ProjectItem project, String oldGroupId, String targetGroupId) {
        if (groupList == null || groupAdapter == null) return;

        ProjectItem targetProjectObject = null;

        // 1. 기존 소속 그룹 리스트에서 해당 프로젝트 객체를 찾아 제거(Remove)합니다.
        for (GroupItem group : groupList) {
            String gId = group.getId();

            // 기존 소속 ID가 일치하거나, 기존 소속이 기타인 경우를 안전하게 매칭
            boolean isOldUnclassified = (oldGroupId == null || "unclassified_dummy_id".equals(oldGroupId) || oldGroupId.isEmpty());
            boolean isGroupUnclassified = ("unclassified_dummy_id".equals(gId) || "기타".equals(group.getKeyword()));

            if ((isOldUnclassified && isGroupUnclassified) || (!isOldUnclassified && oldGroupId.equals(gId))) {
                List<ProjectItem> projects = group.getProjects();
                if (projects != null) {
                    for (int i = 0; i < projects.size(); i++) {
                        if (projects.get(i).getId().equals(project.getId())) {
                            targetProjectObject = projects.remove(i); // 기존 리스트에서 원본 객체를 꺼내며 삭제
                            break;
                        }
                    }
                }
            }
            if (targetProjectObject != null) break;
        }

        // 혹시 리스트 꼬임으로 원본 객체를 못 찾았을 경우를 대비해 파라미터 객체로 백업
        if (targetProjectObject == null) {
            targetProjectObject = project;
        }

        // 2. 이동하고자 하는 새로운 타겟 그룹을 찾아 프로젝트를 add 함
        for (GroupItem group : groupList) {
            String gId = group.getId();
            boolean isTargetUnclassified = "unclassified_dummy_id".equals(targetGroupId) || "기타".equals(group.getKeyword());
            boolean isGroupUnclassified = ("unclassified_dummy_id".equals(gId) || "기타".equals(group.getKeyword()));

            if (isTargetUnclassified && isGroupUnclassified) {
                // 타겟이 '기타' 그룹인 경우
                if (group.getProjects() == null) group.setProjects(new ArrayList<>());
                group.getProjects().add(targetProjectObject);
                break;
            } else if (targetGroupId != null && targetGroupId.equals(gId)) {
                // 타겟이 특정 그룹인 경우
                if (group.getProjects() == null) group.setProjects(new ArrayList<>());
                group.getProjects().add(targetProjectObject);
                break;
            }
        }

        // 3. 어댑터에 데이터가 통째로 변경되었음을 알려 리사이클러뷰를 즉각 다시 그리게 만듭니다.
        groupAdapter.notifyDataSetChanged();
    }
}