package com.example.creator_flow;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.creator_flow.databinding.FragmentStatisticsBinding;
import com.example.creator_flow.network.ApiService;
import com.example.creator_flow.network.RetrofitClient;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;

    // 어댑터 선언
    private ProjectStateAdapter planningAdapter;
    private ProjectStateAdapter progressingAdapter;
    private ProjectStateAdapter completedAdapter;
    private TodoAdapter todoAdapter;
    private ApiService apiService;

    private List<TodoItem> todoList = new ArrayList<>();
    private String projectId = "current_project_id"; // To-Do 등록용 예시 ID

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = RetrofitClient.getApi(requireContext());

        // 리사이클러뷰 및 어댑터 초기화
        initProjectRecyclerViews();
        initTodoRecyclerView();

        // 서버로부터 프로젝트 및 To-Do 목록 로드
        fetchProjectsFromServer();
        fetchTodosFromServer();

        // 프로젝트 추가 버튼 -> simulation 페이지로 이동
        binding.projectAddBtn.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, new SimulationFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // To-Do 추가 버튼
        binding.todoAddBtn.setOnClickListener(v -> {
            TodoItem newItem = new TodoItem("");
            todoList.add(newItem);
            todoAdapter.notifyItemInserted(todoList.size() - 1);
            binding.todoRecycler.scrollToPosition(todoList.size() - 1);
        });
    }

    // ── 프로젝트 리사이클러 뷰 & API ───────────────────────────────────
    private void initProjectRecyclerViews() {
        ProjectStateAdapter.OnProjectActionListener projectActionListener = new ProjectStateAdapter.OnProjectActionListener() {
            @Override
            public void onProjectClick(ProjectItem item) {
                Toast.makeText(getContext(), "클릭", Toast.LENGTH_SHORT).show();
                // 1. ProjectPageFragment 인스턴스 생성
                ProjectPageFragment projectPageFragment = ProjectPageFragment.newInstance(item.getId());

                // 2. FragmentTransaction을 사용하여 화면 전환
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment, projectPageFragment)
                            .addToBackStack(null) // 뒤로가기
                            .commit();
                }
            }

            @Override
            public void onProjectUpdated(ProjectItem item, int position) {
                updateProjectOnServer(item, position);
            }
        };

        planningAdapter = new ProjectStateAdapter("planning", projectActionListener);
        progressingAdapter = new ProjectStateAdapter("progressing", projectActionListener);
        completedAdapter = new ProjectStateAdapter("completed", projectActionListener);

        setupVerticalRecyclerView(binding.planningRecycle, planningAdapter);
        setupVerticalRecyclerView(binding.progressingRecycle, progressingAdapter);
        setupVerticalRecyclerView(binding.completedRecycle, completedAdapter);
    }

    private void setupVerticalRecyclerView(RecyclerView recyclerView, ProjectStateAdapter adapter) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(36));
    }

    /**
     *  API 1: 서버에서 전체 프로젝트 목록을 조회 (GET /projects)
     */
    private void fetchProjectsFromServer() {
        apiService.getProjects().enqueue(new Callback<List<ProjectListResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProjectListResponse>> call, @NonNull Response<List<ProjectListResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ProjectListResponse> responseList = response.body();

                    List<ProjectItem> planningList = new ArrayList<>();
                    List<ProjectItem> progressingList = new ArrayList<>();
                    List<ProjectItem> completedList = new ArrayList<>();

                    for (ProjectListResponse res : responseList) {
                        if (res.getStatus() != null) {
                            ProjectItem project = new ProjectItem(res.getId(), res.getName(), res.getStatus(), res.getGroups());

                            switch (res.getStatus().toLowerCase()) {
                                case "planning":
                                    planningList.add(project);
                                    break;
                                case "progressing":
                                    progressingList.add(project);
                                    break;
                                case "completed":
                                    completedList.add(project);
                                    break;
                                default:
                                    planningList.add(project);
                                    break;
                            }
                        }
                    }
                    updateProjectData(planningList, progressingList, completedList);
                } else {
                    Toast.makeText(getContext(), "프로젝트를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProjectListResponse>> call, @NonNull Throwable t) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "네트워크 통신 실패", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     * API 2: 프로젝트 신규 동기화 및 수정 처리 (PUT /projects/{id})
     */
    private void updateProjectOnServer(ProjectItem item, int position) {
        String targetId = item.getId();

        if (targetId == null || targetId.isEmpty()) {
            Toast.makeText(getContext(), "잘못된 프로젝트 접근입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2026-06 백엔드 업데이트로 PUT /projects/{id}는 JSON body(UpdateProjectRequest) 받음.
        RetrofitClient.getApi(requireContext())
                .updateProject(
                        targetId,
                        new com.example.creator_flow.model.UpdateProjectRequest(
                                item.getName(),
                                item.getStatus())
                )
                .enqueue(new Callback<com.example.creator_flow.model.ProjectResponse>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<com.example.creator_flow.model.ProjectResponse> call,
                            @NonNull Response<com.example.creator_flow.model.ProjectResponse> response) {

                        if (!isAdded() || getContext() == null) return;

                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "프로젝트가 반영되었습니다.", Toast.LENGTH_SHORT).show();
                            fetchProjectsFromServer();  // 최신 상태 동기화
                        } else {
                            Toast.makeText(getContext(), "저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            fetchProjectsFromServer();  // 기존 내용으로 롤백
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<com.example.creator_flow.model.ProjectResponse> call,
                            @NonNull Throwable t) {

                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "네트워크 오류로 저장 실패", Toast.LENGTH_SHORT).show();
                        }
                        fetchProjectsFromServer();
                    }
                });
    }

    public void updateProjectData(List<ProjectItem> planning, List<ProjectItem> progressing, List<ProjectItem> completed) {
        if (planningAdapter != null) planningAdapter.setItems(planning);
        if (progressingAdapter != null) progressingAdapter.setItems(progressing);
        if (completedAdapter != null) completedAdapter.setItems(completed);
    }


    // ── To-Do 리사이클러 뷰 & API ───────────────────────────────────

    private void initTodoRecyclerView() {
        todoAdapter = new TodoAdapter(todoList, new TodoAdapter.OnTodoClickListener() {
            @Override
            public void onCheckChanged(TodoItem item, boolean isChecked) {
                updateTodoStatusOnServer(item, isChecked);
            }

            @Override
            public void onDeleteClicked(TodoItem item) {
                deleteTodoFromServer(item);
            }

            @Override
            public void onNewTodoConfirmed(TodoItem item, int position) {
                createTodoOnServer(item, position);
            }
        });

        binding.todoRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.todoRecycler.setAdapter(todoAdapter);
    }

    /**
     *  API 1: 서버에서 전체 to-do 목록을 조회 (GET /todos)
     */
    // 서버에서 전체 To-Do 목록 조회
    private void fetchTodosFromServer() {
        RetrofitClient.getApi(requireContext()).getTodos().enqueue(new Callback<List<TodoListResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<TodoListResponse>> call, @NonNull Response<List<TodoListResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TodoItem> loadedTodos = new ArrayList<>();
                    for (TodoListResponse res : response.body()) {
                        TodoItem todo = new TodoItem(res.getContent());
                        todo.setId(res.getId());
                        todo.setProject_id(res.getProjectId());
                        todo.setDone(res.isDone());
                        loadedTodos.add(todo);
                    }
                    updateTodoData(loadedTodos);
                } else {
                    Log.e("API_ERROR", "조회 실패: " + response.code());
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<TodoListResponse>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "통신 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * API 2: To-Do 생성 (쿼리 파라미터 사용)
     */
    private void createTodoOnServer(TodoItem item, int position) {
        RetrofitClient.getApi(requireContext()).createTodo(item.getContent()).enqueue(new Callback<TodoListResponse>() {
            @Override
            public void onResponse(@NonNull Call<TodoListResponse> call, @NonNull Response<TodoListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 서버에서 생성된 ID를 아이템에 업데이트
                    item.setId(response.body().getId());
                    todoAdapter.notifyItemChanged(position);
                }
            }
            @Override
            public void onFailure(@NonNull Call<TodoListResponse> call, @NonNull Throwable t) { /* 처리 */ }
        });
    }

    /**
     * API 3: To-Do 수정(클릭 상태)
     */
    private void updateTodoStatusOnServer(TodoItem item, boolean isDone) {
        RetrofitClient.getApi(requireContext()).updateTodo(item.getId(), item.getContent(), isDone).enqueue(new Callback<TodoListResponse>() {
            @Override
            public void onResponse(@NonNull Call<TodoListResponse> call, @NonNull Response<TodoListResponse> response) {
                if (response.isSuccessful()) {
                    item.setDone(isDone);
                }
            }
            @Override
            public void onFailure(@NonNull Call<TodoListResponse> call, @NonNull Throwable t) { /* 처리 */ }
        });
    }

    /**
     * API 4: To-Do 삭제
     */
    private void deleteTodoFromServer(TodoItem item) {
        RetrofitClient.getApi(requireContext()).deleteTodo(item.getId()).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful()) {
                    todoList.remove(item);
                    todoAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "삭제 실패", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void updateTodoData(List<TodoItem> newTodoList) {
        if (newTodoList != null) {
            this.todoList = new ArrayList<>(newTodoList);
            if (todoAdapter != null) {
                todoAdapter.updateList(this.todoList);
            }
        }
    }

    private static class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;
        public VerticalSpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);

            // 현재 아이템이 리사이클러뷰의 마지막 아이템이 아닐 때만 아래쪽에 여백(space)을 부여합니다.
            if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1) {
                outRect.bottom = space;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

